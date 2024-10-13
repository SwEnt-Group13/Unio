package com.android.unio.model.association

import com.android.unio.model.firestore.FirestorePaths.USER_PATH
import com.android.unio.model.firestore.FirestoreReferenceList
import com.android.unio.model.user.UserRepositoryFirestore
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.FirebaseFirestore
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
import org.mockito.Mock
import org.mockito.Mockito.verify
import org.mockito.Mockito.`when`
import org.mockito.MockitoAnnotations
import org.mockito.kotlin.any

class AssociationViewModelTest {
  @Mock private lateinit var repository: AssociationRepositoryFirestore
  @Mock private lateinit var db: FirebaseFirestore
  @Mock private lateinit var collectionReference: CollectionReference

  private lateinit var viewModel: AssociationViewModel

  @OptIn(ExperimentalCoroutinesApi::class) private val testDispatcher = UnconfinedTestDispatcher()

  private lateinit var testAssociations: List<Association>

  @Before
  fun setUp() {
    MockitoAnnotations.openMocks(this)
    Dispatchers.setMain(testDispatcher)

    `when`(db.collection(any())).thenReturn(collectionReference)

    testAssociations =
        listOf(
            Association(
                uid = "1",
                acronym = "ACM",
                fullName = "Association for Computing Machinery",
                description =
                    "ACM is the world's largest educational and scientific computing society.",
                members =
                    FirestoreReferenceList.fromList(
                        listOf("1", "2"),
                        db.collection(USER_PATH),
                        UserRepositoryFirestore::hydrate)),
            Association(
                uid = "2",
                acronym = "IEEE",
                fullName = "Institute of Electrical and Electronics Engineers",
                description = "IEEE is the world's largest technical professional organization.",
                members =
                    FirestoreReferenceList.fromList(
                        listOf("3", "4"),
                        db.collection(USER_PATH),
                        UserRepositoryFirestore::hydrate)))

    viewModel = AssociationViewModel(repository)
  }

  @OptIn(ExperimentalCoroutinesApi::class)
  @After
  fun tearDown() {
    Dispatchers.resetMain()
  }

  @Test
  fun testGetAssociations() {
    `when`(repository.getAssociations(any(), any())).thenAnswer { invocation ->
      val onSuccess = invocation.arguments[0] as (List<Association>) -> Unit
      onSuccess(testAssociations)
    }

    viewModel.getAssociations()
    assertEquals(testAssociations, viewModel.associations.value)

    runBlocking {
      val result = viewModel.associations.first()

      assertEquals(2, result.size)
      assertEquals("ACM", result[0].acronym)
      assertEquals("IEEE", result[1].acronym)
    }

    // Verify that the repository method was called
    verify(repository).getAssociations(any(), any())
  }

  @Test
  fun testGetAssociationsError() {
    `when`(repository.getAssociations(any(), any())).thenAnswer { invocation ->
      val onFailure = invocation.arguments[1] as (Exception) -> Unit
      onFailure(Exception("Test exception"))
    }

    viewModel.getAssociations()
    assert(viewModel.associations.value.isEmpty())

    // Verify that the repository method was called
    verify(repository).getAssociations(any(), any())
  }

  @Test
  fun testInitFetchesAssociations() {
    // Mock the init method
    `when`(repository.init(any())).thenAnswer { invocation ->
      val onSuccess = invocation.arguments[0] as () -> Unit
      onSuccess()
    }

    `when`(repository.getAssociations(any(), any())).thenAnswer { invocation ->
      val onSuccess = invocation.arguments[0] as (List<Association>) -> Unit
      onSuccess(testAssociations)
    }

    val newViewModel = AssociationViewModel(repository)

    runBlocking {
      val result = newViewModel.associations.first()
      assertEquals(2, result.size)
    }

    verify(repository).getAssociations(any(), any())
  }

    @Test
    fun testFindAssociationById() {
        `when`(repository.getAssociations(any(), any())).thenAnswer { invocation ->
            val onSuccess = invocation.arguments[0] as (List<Association>) -> Unit
            onSuccess(testAssociations)
        }

        viewModel.getAssociations()
        assertEquals(testAssociations, viewModel.associations.value)

        runBlocking {
            val result = viewModel.associations.first()

            assertEquals(2, result.size)
            assertEquals("ACM", result[0].acronym)
            assertEquals("IEEE", result[1].acronym)
        }

        assertEquals(testAssociations[0], viewModel.findAssociationById("1"))
        assertEquals(testAssociations[1], viewModel.findAssociationById("2"))
        assertEquals(null, viewModel.findAssociationById("3"))
    }
}
