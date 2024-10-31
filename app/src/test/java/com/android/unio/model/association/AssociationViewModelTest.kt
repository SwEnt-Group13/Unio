package com.android.unio.model.association

import androidx.test.core.app.ApplicationProvider
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
import org.mockito.Mockito.verify
import org.mockito.Mockito.`when`
import org.mockito.MockitoAnnotations
import org.mockito.kotlin.any
import org.mockito.kotlin.eq
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class AssociationViewModelTest {
  private lateinit var db: FirebaseFirestore
  @Mock private lateinit var repository: AssociationRepositoryFirestore
  @Mock private lateinit var collectionReference: CollectionReference
  @Mock private lateinit var inputStream: InputStream

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
            MockAssociation.createMockAssociation(name = "ACM", uid = "1"),
            MockAssociation.createMockAssociation(name = "IEEE", uid = "2"))

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
      assertEquals("ACM", result[0].name)
      assertEquals("IEEE", result[1].name)
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
      assertEquals("ACM", result[0].name)
      assertEquals("IEEE", result[1].name)
    }

    assertEquals(testAssociations[0], viewModel.findAssociationById("1"))
    assertEquals(testAssociations[1], viewModel.findAssociationById("2"))
    assertEquals(null, viewModel.findAssociationById("3"))
  }

  @Test
  fun testAddAssociation() {
    `when`(repository.addAssociation(eq(testAssociations[0]), any(), any())).thenAnswer { invocation
      ->
      val onSuccess = invocation.arguments[0] as () -> Unit
      onSuccess()
    }
    viewModel.addAssociation(
        inputStream,
        testAssociations[0],
        { verify(repository).addAssociation(eq(testAssociations[0]), any(), any()) },
        {})
  }
}
