package com.android.unio.model.search

import androidx.appsearch.app.AppSearchBatchResult
import androidx.appsearch.app.AppSearchSession
import androidx.appsearch.app.PutDocumentsRequest
import androidx.appsearch.app.SearchResult
import androidx.appsearch.app.SearchResults
import androidx.appsearch.app.SetSchemaResponse
import androidx.appsearch.localstorage.LocalStorage
import androidx.test.core.app.ApplicationProvider
import com.android.unio.model.association.Association
import com.android.unio.model.association.AssociationCategory
import com.android.unio.model.association.AssociationDocument
import com.android.unio.model.association.AssociationRepository
import com.android.unio.model.association.toAssociationDocument
import com.android.unio.model.event.EventRepository
import com.android.unio.model.firestore.firestoreReferenceListWith
import com.android.unio.model.user.User
import com.google.common.util.concurrent.Futures
import com.google.common.util.concurrent.Futures.immediateFuture
import io.mockk.MockKAnnotations
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.slot
import io.mockk.unmockkStatic
import io.mockk.verify
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

@RunWith(RobolectricTestRunner::class)
// @RunWith(AndroidJUnit4::class)
@OptIn(ExperimentalCoroutinesApi::class)
class SearchRepositoryTest {

  // Do I need to replace the main test dispatcher with an unconfined test dispatcher?
  private val testDispatcher = UnconfinedTestDispatcher()
  private val testScope = TestScope(testDispatcher)

  @MockK private lateinit var mockSession: AppSearchSession

  @MockK private lateinit var mockAssociationRepository: AssociationRepository
  @MockK private lateinit var mockEventRepository: EventRepository

  private lateinit var searchRepository: SearchRepository

  val association1 =
      Association(
          uid = "1",
          url = "https://www.acm.org/",
          name = "ACM",
          fullName = "Association for Computing Machinery",
          category = AssociationCategory.SCIENCE_TECH,
          description = "ACM is the world's largest educational and scientific computing society.",
          members = User.firestoreReferenceListWith(listOf("1", "2")),
          image = "https://www.example.com/image.jpg")

  val association2 =
      Association(
          uid = "2",
          url = "https://www.ieee.org/",
          name = "IEEE",
          fullName = "Institute of Electrical and Electronics Engineers",
          category = AssociationCategory.SCIENCE_TECH,
          description =
              "IEEE is the world's largest technical professional organization dedicated to advancing technology for the benefit of humanity.",
          members = User.firestoreReferenceListWith(listOf("3", "4")),
          image = "https://www.example.com/image.jpg")

  @Before
  fun setUp() {
    MockKAnnotations.init(this)
    Dispatchers.setMain(testDispatcher)

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
  fun `test init starts listening for updates`() =
      testScope.runTest {
        every { mockSession.setSchemaAsync(any()) } returns
            immediateFuture(SetSchemaResponse.Builder().build())
        every { mockSession.putAsync(any()) } returns
            immediateFuture(AppSearchBatchResult.Builder<String, Void>().build())
        every { mockAssociationRepository.getAssociations(any(), any()) } answers
            {
              val onSuccess = firstArg<(List<Association>) -> Unit>()
              onSuccess(listOf(association1, association2))
            }
        searchRepository.init()
        verify { mockAssociationRepository.getAssociations(any(), any()) }
      }

  @Test
  fun `test addAssociations calls putAsync with correct documents`() =
      testScope.runTest {
        // Arrange
        val associations = listOf(association1, association2)
        val associationDocuments = associations.map { it.toAssociationDocument() }

        // Mock the putAsync method to return a successful result
        every { mockSession.putAsync(any()) } returns
            Futures.immediateFuture(AppSearchBatchResult.Builder<String, Void>().build())

        // Act
        searchRepository.addAssociations(associations)

        // Assert
        val requestSlot = slot<PutDocumentsRequest>()
        verify { mockSession.putAsync(capture(requestSlot)) }

        val actualDocuments = requestSlot.captured.genericDocuments
        assertEquals(associationDocuments.size, actualDocuments.size)

        // Compare each document
        associationDocuments.forEach { expectedDoc ->
          val matchingActualDoc = actualDocuments.find { it.id == expectedDoc.uid }
          assertNotNull(matchingActualDoc)

          // Compare fields
          assertEquals(expectedDoc.namespace, matchingActualDoc!!.namespace)
          assertEquals(expectedDoc.uid, matchingActualDoc.id)
          assertEquals(expectedDoc.url, matchingActualDoc.getPropertyString("url"))
          assertEquals(expectedDoc.name, matchingActualDoc.getPropertyString("name"))
          assertEquals(expectedDoc.fullName, matchingActualDoc.getPropertyString("fullName"))
          assertEquals(expectedDoc.description, matchingActualDoc.getPropertyString("description"))
        }
      }

  @Test
  fun `test searchAssociations returns correct associations`() =
      testScope.runTest {
        // Arrange
        val query = "ACM"

        // Mock the session.search method to return a mock SearchResults
        val mockSearchResults: SearchResults = mockk()
        every { mockSession.search(any(), any()) } returns mockSearchResults

        // Mock the SearchResults.nextPageAsync.get() to return lists of SearchResult
        val mockSearchResult: SearchResult = mockk()
        val associationDocument = association1.toAssociationDocument()
        every { mockSearchResult.getDocument(AssociationDocument::class.java) } returns
            associationDocument

        // Simulate two pages: first with results, second empty
        every { mockSearchResults.nextPageAsync.get() } returnsMany
            listOf(listOf(mockSearchResult), emptyList())

        // Mock associationRepository.getAssociationWithId
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
  fun `test closeSession closes the session and sets it to null`() =
      testScope.runTest {
        // Mock session.close()
        every { mockSession.close() } returns Unit

        // Act
        searchRepository.closeSession()

        // Assert
        verify { mockSession.close() }
        assertNull(searchRepository.session)
      }
}
