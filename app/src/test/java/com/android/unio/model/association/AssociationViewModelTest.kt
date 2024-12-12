package com.android.unio.model.association

import androidx.test.core.app.ApplicationProvider
import com.android.unio.mocks.association.MockAssociation
import com.android.unio.model.event.Event
import com.android.unio.model.event.EventRepository
import com.android.unio.model.firestore.emptyFirestoreReferenceList
import com.android.unio.model.follow.ConcurrentAssociationUserRepository
import com.android.unio.model.image.ImageRepository
import com.android.unio.model.user.User
import com.google.firebase.Firebase
import com.google.firebase.FirebaseApp
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.firestore
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkStatic
import java.io.InputStream
import junit.framework.TestCase.assertEquals
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.Mockito.mock
import org.mockito.Mockito.never
import org.mockito.Mockito.verify
import org.mockito.Mockito.`when`
import org.mockito.MockitoAnnotations
import org.mockito.kotlin.any
import org.mockito.kotlin.eq
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class AssociationViewModelTest {
  private lateinit var db: FirebaseFirestore
  @Mock private lateinit var associationRepository: AssociationRepositoryFirestore
  @Mock private lateinit var collectionReference: CollectionReference
  @Mock private lateinit var inputStream: InputStream
  @Mock private lateinit var eventRepository: EventRepository
  @Mock private lateinit var imageRepository: ImageRepository
  @Mock
  private lateinit var concurrentAssociationUserRepository: ConcurrentAssociationUserRepository

  private lateinit var viewModel: AssociationViewModel

  @OptIn(ExperimentalCoroutinesApi::class) private val testDispatcher = UnconfinedTestDispatcher()

  private lateinit var testAssociations: List<Association>

  private lateinit var user: User

  @Before
  fun setUp() {
    MockitoAnnotations.openMocks(this)
    Dispatchers.setMain(testDispatcher)
    if (FirebaseApp.getApps(ApplicationProvider.getApplicationContext()).isEmpty()) {
      FirebaseApp.initializeApp(ApplicationProvider.getApplicationContext())
    }
    db = mockk()
    mockkStatic(FirebaseFirestore::class)
    every { Firebase.firestore } returns db
    every { db.collection(any()) } returns collectionReference

    testAssociations =
        listOf(
            MockAssociation.createMockAssociation(uid = "1", name = "ACM"),
            MockAssociation.createMockAssociation(uid = "2", name = "IEEE"))

    viewModel =
        AssociationViewModel(
            associationRepository,
            eventRepository,
            imageRepository,
            concurrentAssociationUserRepository)

    user =
        User(
            uid = "1",
            email = "",
            firstName = "",
            lastName = "",
            biography = "",
            savedEvents = Event.emptyFirestoreReferenceList(),
            followedAssociations = Association.emptyFirestoreReferenceList(),
            joinedAssociations = Association.emptyFirestoreReferenceList(),
            interests = emptyList(),
            socials = emptyList(),
            profilePicture = "")
  }

  @OptIn(ExperimentalCoroutinesApi::class)
  @After
  fun tearDown() {
    Dispatchers.resetMain()
  }

  @Test
  fun testUpdateFollowIncrement() {
    `when`(concurrentAssociationUserRepository.updateFollow(any(), any(), any(), any()))
        .thenAnswer { invocation ->
          val onSuccess = invocation.arguments[2] as () -> Unit
          onSuccess()
        }
    val association = MockAssociation.createMockAssociation(uid = "1", name = "ACM")
    val followCount = association.followersCount
    viewModel.selectAssociation(association.uid)
    val updateUser = { user.followedAssociations.add(association.uid) }
    viewModel.updateFollow(association, user, false, updateUser)
    assert(user.followedAssociations.contains(association.uid))
    assert(viewModel.selectedAssociation.value?.followersCount == followCount + 1)
  }

  @Test
  fun testUpdateFollowDecrement() {
    `when`(concurrentAssociationUserRepository.updateFollow(any(), any(), any(), any()))
        .thenAnswer { invocation ->
          val onSuccess = invocation.arguments[2] as () -> Unit
          onSuccess()
        }
    val association = MockAssociation.createMockAssociation(uid = "1", name = "ACM")
    val followCount = association.followersCount
    viewModel.selectAssociation(association.uid)
    val updateUser = { user.followedAssociations.remove(association.uid) }
    viewModel.updateFollow(association, user, true, updateUser)
    assert(!user.followedAssociations.contains(association.uid))
    assert(viewModel.selectedAssociation.value?.followersCount == followCount - 1)
  }

  @Test
  fun testGetAssociations() {
    `when`(associationRepository.getAssociations(any(), any())).thenAnswer { invocation ->
      val onSuccess = invocation.arguments[0] as (List<Association>) -> Unit
      onSuccess(testAssociations)
    }

    viewModel.getAssociations()
    assertEquals(testAssociations, viewModel.associations.value)

    runBlocking {
      val result = viewModel.associations.first()

      assertEquals(2, result.size)
      assertEquals("ACM", result[0].name)
      assertEquals("IEEE", result[1].name)
    }

    // Verify that the repository method was called
    verify(associationRepository).getAssociations(any(), any())
  }

  @Test
  fun testGetAssociationsError() {
    `when`(associationRepository.getAssociations(any(), any())).thenAnswer { invocation ->
      val onFailure = invocation.arguments[1] as (Exception) -> Unit
      onFailure(Exception("Test exception"))
    }

    viewModel.getAssociations()
    assert(viewModel.associations.value.isEmpty())

    // Verify that the repository method was called
    verify(associationRepository).getAssociations(any(), any())
  }

  @Test
  fun testInitFetchesAssociations() {
    // Mock the init method
    `when`(associationRepository.init(any())).thenAnswer { invocation ->
      val onSuccess = invocation.arguments[0] as () -> Unit
      onSuccess()
    }

    `when`(associationRepository.getAssociations(any(), any())).thenAnswer { invocation ->
      val onSuccess = invocation.arguments[0] as (List<Association>) -> Unit
      onSuccess(testAssociations)
    }

    val newViewModel =
        AssociationViewModel(
            associationRepository,
            eventRepository,
            imageRepository,
            concurrentAssociationUserRepository)

    runBlocking {
      val result = newViewModel.associations.first()
      assertEquals(2, result.size)
    }

    verify(associationRepository).getAssociations(any(), any())
  }

  @Test
  fun testFindAssociationById() {
    `when`(associationRepository.getAssociations(any(), any())).thenAnswer { invocation ->
      val onSuccess = invocation.arguments[0] as (List<Association>) -> Unit
      onSuccess(testAssociations)
    }

    viewModel.getAssociations()
    assertEquals(testAssociations, viewModel.associations.value)

    runBlocking {
      val result = viewModel.associations.first()

      assertEquals(2, result.size)
      assertEquals("ACM", result[0].name)
      assertEquals("IEEE", result[1].name)
    }

    assertEquals(testAssociations[0], viewModel.findAssociationById("1"))
    assertEquals(testAssociations[1], viewModel.findAssociationById("2"))
    assertEquals(null, viewModel.findAssociationById("3"))
  }

  @Test
  fun testSaveAssociationWithImageStreamSuccess() {
    val association = testAssociations[0]
    val imageUrl = "https://example.com"

    `when`(imageRepository.uploadImage(any(), any(), any(), any())).thenAnswer { invocation ->
      val onSuccess = invocation.getArgument(2) as (String) -> Unit
      onSuccess(imageUrl)
    }

    `when`(associationRepository.saveAssociation(eq(false), any(), any(), any())).thenAnswer { invocation ->
      val onSuccess = invocation.getArgument(2) as () -> Unit
      onSuccess()
    }

    val onSuccess = mock<() -> Unit>()
    viewModel.saveAssociation(isNewAssociation = false, association, inputStream, onSuccess, {})

    verify(imageRepository).uploadImage(eq(inputStream), any(), any(), any())
    verify(associationRepository)
        .saveAssociation(eq(false), eq(association.copy(image = imageUrl)), any(), any())
    verify(onSuccess).invoke()
  }

  @Test
  fun testSaveAssociationWithImageStreamFailure() {
    val association = testAssociations[0]
    val failureException = Exception("Upload failed")

    `when`(imageRepository.uploadImage(any(), any(), any(), any())).thenAnswer { invocation ->
      val onFailure = invocation.getArgument(3) as (Exception) -> Unit
      onFailure(failureException)
    }

    val onFailure = mock<(Exception) -> Unit>()
    viewModel.saveAssociation(isNewAssociation = false, association, inputStream, {}, onFailure)

    verify(imageRepository)
        .uploadImage(eq(inputStream), eq("images/associations/${association.uid}"), any(), any())
    verify(associationRepository, never()).saveAssociation(eq(false), any(), any(), any())
    verify(onFailure).invoke(failureException)
  }

  @Test
  fun testSaveAssociationNoImageStreamSuccess() {
    val association = testAssociations[0]

    `when`(associationRepository.saveAssociation(eq(false), any(), any(), any())).thenAnswer { invocation ->
      val onSuccess = invocation.getArgument(2) as () -> Unit
      onSuccess()
    }

    val onSuccess = mock<() -> Unit>()
    viewModel.saveAssociation(isNewAssociation = false, association, null, onSuccess, {})

    verify(associationRepository).saveAssociation(eq(false), eq(association), any(), any())
    verify(onSuccess).invoke()
  }

  @Test
  fun testSaveAssociationNoImageStreamFailure() {
    val association = testAssociations[0]
    val failureException = Exception("Save failed")

    `when`(associationRepository.saveAssociation(eq(false),any(), any(), any())).thenAnswer { invocation ->
      val onFailure = invocation.getArgument(3) as (Exception) -> Unit
      onFailure(failureException)
    }

    val onFailure = mock<(Exception) -> Unit>()
    viewModel.saveAssociation(isNewAssociation = false, association, null, {}, onFailure)

    verify(associationRepository).saveAssociation(eq(false), eq(association), any(), any())
    verify(onFailure).invoke(failureException)
  }
}
