package com.android.unio.model.association

import androidx.test.core.app.ApplicationProvider
import com.android.unio.model.event.EventRepository
import com.android.unio.model.firestore.firestoreReferenceListWith
import com.android.unio.model.user.User
import com.google.firebase.Firebase
import com.google.firebase.FirebaseApp
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.firestore
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkStatic
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
import org.mockito.Mockito.verify
import org.mockito.Mockito.`when`
import org.mockito.MockitoAnnotations
import org.mockito.kotlin.any
import org.mockito.kotlin.eq
import org.robolectric.RobolectricTestRunner
import java.io.InputStream

@RunWith(RobolectricTestRunner::class)
class AssociationViewModelTest {
  private lateinit var db: FirebaseFirestore
  @Mock private lateinit var associationRepository: AssociationRepositoryFirestore
  @Mock private lateinit var collectionReference: CollectionReference
  @Mock private lateinit var inputStream: InputStream
  @Mock private lateinit var eventRepository: EventRepository

  private lateinit var viewModel: AssociationViewModel

  @OptIn(ExperimentalCoroutinesApi::class) private val testDispatcher = UnconfinedTestDispatcher()

  private lateinit var testAssociations: List<Association>

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
            Association(
                uid = "1",
                url = "https://acm.org",
                name = "ACM",
                fullName = "Association for Computing Machinery",
                category = AssociationCategory.SCIENCE_TECH,
                description =
                    "ACM is the world's largest educational and scientific computing society.",
                members = User.firestoreReferenceListWith(listOf("1", "2")),
                followersCount = 0,
                image = ""),
            Association(
                uid = "2",
                url = "https://ieee.org",
                name = "IEEE",
                fullName = "Institute of Electrical and Electronics Engineers",
                category = AssociationCategory.SCIENCE_TECH,
                description = "IEEE is the world's largest technical professional organization.",
                members = User.firestoreReferenceListWith(listOf("3", "4")),
                followersCount = 1,
                image = ""))

    viewModel = AssociationViewModel(associationRepository, eventRepository)
  }

  @OptIn(ExperimentalCoroutinesApi::class)
  @After
  fun tearDown() {
    Dispatchers.resetMain()
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

    val newViewModel = AssociationViewModel(associationRepository, eventRepository)

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
  fun testAddAssociation() {
    `when`(associationRepository.addAssociation(eq(testAssociations[0]), any(), any()))
        .thenAnswer { invocation ->
          val onSuccess = invocation.arguments[0] as () -> Unit
          onSuccess()
        }
    viewModel.addAssociation(
        inputStream,
        testAssociations[0],
        { verify(associationRepository).addAssociation(eq(testAssociations[0]), any(), any()) },
        {})
  }
}
