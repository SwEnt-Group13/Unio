package com.android.unio.model.search

/**
 * Need to have a context, and mock classes in order to make it work Init should work, Need to test
 * individually add and remove association Need to mock the associationRepository and see if the
 * listener works Need to see if search works on mock data
 */
// File: SearchRepositoryTest.kt

import androidx.appsearch.app.AppSearchSession
import androidx.test.core.app.ApplicationProvider
import com.android.unio.model.association.Association
import com.android.unio.model.association.AssociationCategory
import com.android.unio.model.association.AssociationRepository
import com.android.unio.model.firestore.firestoreReferenceListWith
import com.android.unio.model.user.User
import io.mockk.MockKAnnotations
import io.mockk.every
import io.mockk.impl.annotations.MockK
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.cancel
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mockito.any
import org.mockito.Mockito.verify
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
@OptIn(ExperimentalCoroutinesApi::class)
class SearchRepositoryTest {


  @MockK private lateinit var mockAssociationRepository: AssociationRepository
  @MockK private lateinit var mockSession: AppSearchSession

  private lateinit var searchRepository: SearchRepository

  private val testDispatcher = UnconfinedTestDispatcher()
  private val testScope = TestScope(testDispatcher)

  val association1 =
  Association(
  uid = "1",
  url = "https://www.acm.org/",
  name = "ACM",
  fullName = "Association for Computing Machinery",
  category = AssociationCategory.SCIENCE_TECH,
  description =
  "ACM is the world's largest educational and scientific computing society.",
  members = User.firestoreReferenceListWith(listOf("1", "2")))

  val association2 =
  Association(
  uid = "2",
  url = "https://www.ieee.org/",
  name = "IEEE",
  fullName = "Institute of Electrical and Electronics Engineers",
  category = AssociationCategory.SCIENCE_TECH,
  description =
  "IEEE is the world's largest technical professional organization dedicated to advancing technology for the benefit of humanity.",
  members = User.firestoreReferenceListWith(listOf("3", "4")))
  @Before
  fun setUp() {
    MockKAnnotations.init(this)
    Dispatchers.setMain(testDispatcher)

    searchRepository = SearchRepository(ApplicationProvider.getApplicationContext(), mockAssociationRepository)
    searchRepository.session = mockSession

    every {mockAssociationRepository.getAssociations(any(), any())} answers {
      val onSuccess = firstArg<(List<Association>) -> Unit>()
      onSuccess(listOf(association1, association2))
    }

  }

  @After
  fun tearDown() {
    Dispatchers.resetMain()
    testScope.cancel()
  }

  @Test
  fun `test init sets up schema and starts listening for updates`() = runTest {
    searchRepository.init()
    verify(mockAssociationRepository).getAssociations(any(), any())
  }
}
