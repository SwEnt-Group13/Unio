package com.android.unio.model.search

import android.content.Context
import android.net.ConnectivityManager
import androidx.appsearch.app.AppSearchBatchResult
import androidx.appsearch.app.AppSearchSession
import androidx.appsearch.app.PutDocumentsRequest
import androidx.appsearch.app.RemoveByDocumentIdRequest
import androidx.appsearch.app.SearchResult
import androidx.appsearch.app.SearchResults
import androidx.appsearch.app.SetSchemaResponse
import androidx.appsearch.localstorage.LocalStorage
import androidx.test.core.app.ApplicationProvider
import com.android.unio.model.association.Association
import com.android.unio.model.association.AssociationCategory
import com.android.unio.model.association.AssociationDocument
import com.android.unio.model.association.AssociationRepository
import com.android.unio.model.association.Member
import com.android.unio.model.association.Role
import com.android.unio.model.association.toAssociationDocument
import com.android.unio.model.event.Event
import com.android.unio.model.event.EventDocument
import com.android.unio.model.event.EventRepository
import com.android.unio.model.event.EventUserPicture
import com.android.unio.model.event.toEventDocument
import com.android.unio.model.firestore.emptyFirestoreReferenceList
import com.android.unio.model.firestore.firestoreReferenceListWith
import com.android.unio.model.map.Location
import com.android.unio.model.user.User
import com.google.common.util.concurrent.Futures.immediateFuture
import com.google.common.util.concurrent.ListenableFuture
import com.google.firebase.Firebase
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.auth
import firestoreReferenceElementWith
import io.mockk.MockKAnnotations
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.slot
import io.mockk.unmockkStatic
import io.mockk.verify
import java.util.GregorianCalendar
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.cancel
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.Shadows

@RunWith(RobolectricTestRunner::class)
@OptIn(ExperimentalCoroutinesApi::class)
class SearchRepositoryTest {

  private val testDispatcher = UnconfinedTestDispatcher()
  private val testScope = TestScope(testDispatcher)

  @MockK private lateinit var firebaseAuth: FirebaseAuth

  @MockK private lateinit var firebaseUser: FirebaseUser

  @MockK private lateinit var mockSession: AppSearchSession

  @MockK private lateinit var mockAssociationRepository: AssociationRepository

  @MockK private lateinit var mockEventRepository: EventRepository

  private lateinit var searchRepository: SearchRepository

  private val association1 =
      Association(
          uid = "1",
          url = "https://www.acm.org/",
          name = "ACM",
          fullName = "Association for Computing Machinery",
          category = AssociationCategory.SCIENCE_TECH,
          description = "ACM is the world's largest educational and scientific computing society.",
          followersCount = 1,
          members = listOf(Member(User.firestoreReferenceElementWith("1"), Role.GUEST)),
          roles = listOf(Role.GUEST),
          image = "https://www.example.com/image.jpg",
          events = Event.firestoreReferenceListWith(listOf("1", "2")),
          principalEmailAddress = "example@adress.com")

  private val association2 =
      Association(
          uid = "2",
          url = "https://www.ieee.org/",
          name = "IEEE",
          fullName = "Institute of Electrical and Electronics Engineers",
          category = AssociationCategory.SCIENCE_TECH,
          description =
              "IEEE is the world's largest technical professional organization dedicated to advancing technology for the benefit of humanity.",
          followersCount = 1,
          members = listOf(Member(User.firestoreReferenceElementWith("2"), Role.GUEST)),
          roles = listOf(Role.GUEST),
          image = "https://www.example.com/image.jpg",
          events = Event.firestoreReferenceListWith(listOf("3", "4")),
          principalEmailAddress = "example2@adress.com")

  private val event1 =
      Event(
          uid = "1",
          title = "Balelec",
          organisers = Association.emptyFirestoreReferenceList(),
          taggedAssociations = Association.emptyFirestoreReferenceList(),
          image = "https://imageurl.jpg",
          description = "Plus grand festival du monde (non contractuel)",
          price = 40.5,
          startDate = Timestamp(GregorianCalendar(2004, 7, 1).time),
          location = Location(1.2345, 2.3455, "Somewhere"),
          maxNumberOfPlaces = -1,
          eventPictures = EventUserPicture.emptyFirestoreReferenceList())
  private val event2 =
      Event(
          uid = "2",
          title = "Tremplin Sysmic",
          organisers = Association.emptyFirestoreReferenceList(),
          taggedAssociations = Association.emptyFirestoreReferenceList(),
          image = "https://imageurl.jpg",
          description = "Plus grand festival du monde (non contractuel)",
          price = 40.5,
          startDate = Timestamp(GregorianCalendar(2008, 7, 1).time),
          location = Location(1.2345, 2.3455, "Somewhere"),
          maxNumberOfPlaces = -1,
          eventPictures = EventUserPicture.emptyFirestoreReferenceList())

  @Before
  fun setUp() {
    MockKAnnotations.init(this)
    Dispatchers.setMain(testDispatcher)

    mockkStatic(FirebaseAuth::class)
    every { Firebase.auth } returns firebaseAuth
    every { firebaseAuth.addAuthStateListener(any()) } answers
        {
          val authStateChange = it.invocation.args[0] as FirebaseAuth.AuthStateListener
          authStateChange.onAuthStateChanged(firebaseAuth)
        }
    every { firebaseAuth.currentUser } returns firebaseUser

    mockkStatic(LocalStorage::class)
    every { LocalStorage.createSearchSessionAsync(any()) } returns immediateFuture(mockSession)

    searchRepository =
        SearchRepository(
            ApplicationProvider.getApplicationContext(),
            mockAssociationRepository,
            mockEventRepository)

    searchRepository.session = mockSession
  }

  @After
  fun tearDown() {
    unmockkStatic(LocalStorage::class)
    Dispatchers.resetMain()
    testScope.cancel()
  }

  @Test
  fun `test init fetches event and association data`() =
      testScope.runTest {
        every { firebaseUser.isEmailVerified } returns true
        every { mockSession.setSchemaAsync(any()) } returns
            immediateFuture(SetSchemaResponse.Builder().build())
        every { mockSession.putAsync(any()) } returns
            immediateFuture(AppSearchBatchResult.Builder<String, Void>().build())
        every { mockAssociationRepository.getAssociations(any(), any()) } answers
            {
              val onSuccess = firstArg<(List<Association>) -> Unit>()
              onSuccess(listOf(association1, association2))
            }
        every { mockEventRepository.getEvents(any(), any()) } answers
            {
              val onSuccess = firstArg<(List<Event>) -> Unit>()
              onSuccess(listOf(event1, event2))
            }

        searchRepository.init()

        verify { mockAssociationRepository.getAssociations(any(), any()) }
        verify { mockEventRepository.getEvents(any(), any()) }
      }

  @Test
  fun `test addAssociations calls putAsync with correct documents`() =
      testScope.runTest {
        // Arrange
        val associations = listOf(association1, association2)
        val associationDocuments = associations.map { it.toAssociationDocument() }

        every { mockSession.putAsync(any()) } returns
            immediateFuture(AppSearchBatchResult.Builder<String, Void>().build())

        // Act
        searchRepository.addAssociations(associations)

        // Assert
        val requestSlot = slot<PutDocumentsRequest>()
        verify { mockSession.putAsync(capture(requestSlot)) }

        val actualDocuments = requestSlot.captured.genericDocuments
        assertEquals(associationDocuments.size, actualDocuments.size)

        associationDocuments.forEach { expectedDoc ->
          val matchingActualDoc = actualDocuments.find { it.id == expectedDoc.uid }
          assertNotNull(matchingActualDoc)

          assertEquals(expectedDoc.namespace, matchingActualDoc!!.namespace)
          assertEquals(expectedDoc.uid, matchingActualDoc.id)
          assertEquals(expectedDoc.name, matchingActualDoc.getPropertyString("name"))
          assertEquals(expectedDoc.fullName, matchingActualDoc.getPropertyString("fullName"))
          assertEquals(expectedDoc.description, matchingActualDoc.getPropertyString("description"))
        }
      }

  @Test
  fun `test addEvents calls putAsync with correct documents`() =
      testScope.runTest {
        // Arrange
        val events = listOf(event1, event2)
        val eventDocuments = events.map { it.toEventDocument() }

        every { mockSession.putAsync(any()) } returns
            immediateFuture(AppSearchBatchResult.Builder<String, Void>().build())

        // Act
        searchRepository.addEvents(events)

        // Assert
        val requestSlot = slot<PutDocumentsRequest>()
        verify { mockSession.putAsync(capture(requestSlot)) }

        val actualDocuments = requestSlot.captured.genericDocuments
        assertEquals(eventDocuments.size, actualDocuments.size)

        eventDocuments.forEach { expectedDoc ->
          val matchingActualDoc = actualDocuments.find { it.id == expectedDoc.uid }
          assertNotNull(matchingActualDoc)
        }
      }

  @Test
  fun `test remove calls removeAsync with correct uid`() =
      testScope.runTest {
        // Arrange
        val uid = "1"
        every { mockSession.removeAsync(any<RemoveByDocumentIdRequest>()) } returns
            immediateFuture(AppSearchBatchResult.Builder<String, Void>().build())

        // Act
        searchRepository.remove(uid)

        // Assert
        val requestSlot = slot<RemoveByDocumentIdRequest>()
        verify { mockSession.removeAsync(capture(requestSlot)) }
        assertEquals(setOf(uid), requestSlot.captured.ids)
      }

  @Test
  fun `test searchAssociations returns correct associations online`() =
      testScope.runTest {
        // Arrange
        val query = "ACM"

        val mockSearchResults: SearchResults = mockk()
        every { mockSession.search(any(), any()) } returns mockSearchResults

        val mockSearchResult: SearchResult = mockk()
        val associationDocument = association1.toAssociationDocument()

        every { mockSearchResult.getDocument(AssociationDocument::class.java) } returns
            associationDocument

        val mockFuture: ListenableFuture<List<SearchResult>> =
            immediateFuture(listOf(mockSearchResult))
        every { mockSearchResults.nextPageAsync } returns mockFuture

        every { mockAssociationRepository.getAssociationWithId(any(), any(), any()) } answers
            {
              val id = firstArg<String>()
              val onSuccess = secondArg<(Association) -> Unit>()
              onSuccess(association1)
            }

        // Act
        val resultAssociations = searchRepository.searchAssociations(query)

        // Assert
        assertEquals(listOf(association1), resultAssociations)
      }

  @Test
  fun `test searchAssociations returns correct associations offline`() =
      testScope.runTest {
        val connectivityManager =
            ApplicationProvider.getApplicationContext<Context>()
                .getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

        // Use Robolectric Shadow to simulate no network
        Shadows.shadowOf(connectivityManager).setActiveNetworkInfo(null)

        // Arrange
        val query = "ACM"

        val mockSearchResults: SearchResults = mockk()
        every { mockSession.search(any(), any()) } returns mockSearchResults

        val mockSearchResult: SearchResult = mockk()
        val associationDocument = association1.toAssociationDocument()

        every { mockSearchResult.getDocument(AssociationDocument::class.java) } returns
            associationDocument

        val mockFuture: ListenableFuture<List<SearchResult>> =
            immediateFuture(listOf(mockSearchResult))
        every { mockSearchResults.nextPageAsync } returns mockFuture

        every { mockAssociationRepository.getAssociationWithId(any(), any(), any()) } answers
            {
              val id = firstArg<String>()
              val onSuccess = secondArg<(Association) -> Unit>()
              onSuccess(association1)
            }

        // Act
        val resultAssociations = searchRepository.searchAssociations(query)

        // Assert
        assertEquals(listOf(association1), resultAssociations)
      }

  @Test
  fun `test searchEvents returns correct events`() =
      testScope.runTest {
        // Arrange
        val query = "Balelec"
        val mockSearchResults: SearchResults = mockk()
        every { mockSession.search(any(), any()) } returns mockSearchResults

        val mockSearchResult: SearchResult = mockk()
        val eventDocument = event1.toEventDocument()

        every { mockSearchResult.getDocument(EventDocument::class.java) } returns eventDocument
        val mockFuture: ListenableFuture<List<SearchResult>> =
            immediateFuture(listOf(mockSearchResult))
        every { mockSearchResults.nextPageAsync } returns mockFuture

        every { mockEventRepository.getEventWithId(any(), any(), any()) } answers
            {
              val id = firstArg<String>()
              val onSuccess = secondArg<(Event) -> Unit>()
              onSuccess(event1)
            }

        // Act
        val resultEvents = searchRepository.searchEvents(query)

        // Assert
        assertEquals(listOf(event1), resultEvents)
      }

  @Test
  fun `test closeSession closes the session and sets it to null`() =
      testScope.runTest {
        // Arrange
        every { mockSession.close() } returns Unit

        // Act
        searchRepository.closeSession()

        // Assert
        verify { mockSession.close() }
        assertNull(searchRepository.session)
      }
}
