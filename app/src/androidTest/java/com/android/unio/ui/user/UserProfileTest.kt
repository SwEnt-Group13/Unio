package com.android.unio.ui.user

import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertTextEquals
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onNodeWithTag
import com.android.unio.mocks.association.MockAssociation
import com.android.unio.mocks.user.MockUser
import com.android.unio.model.association.Association
import com.android.unio.model.search.SearchRepository
import com.android.unio.model.search.SearchViewModel
import com.android.unio.model.strings.test_tags.UserProfileTestTags
import com.android.unio.ui.navigation.NavigationAction
import io.mockk.MockKAnnotations
import io.mockk.clearAllMocks
import io.mockk.coEvery
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.spyk
import io.mockk.unmockkAll
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.TestCoroutineDispatcher
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class UserProfileTest {

  private val testDispatcher = TestCoroutineDispatcher()

  @MockK private lateinit var navigationAction: NavigationAction

  @get:Rule val composeTestRule = createComposeRule()

  private val user = MockUser.createMockUser()

  private lateinit var searchViewModel: SearchViewModel
  @MockK(relaxed = true) private lateinit var searchRepository: SearchRepository

  @Before
  fun setUp() {
    MockKAnnotations.init(this)

    // Set the main dispatcher for testing coroutines
    Dispatchers.setMain(testDispatcher)

    // Set up the repository mock behavior for suspend function
    coEvery { searchRepository.searchAssociations(any()) } returns
        listOf(MockAssociation.createMockAssociation()) // Replace with actual mocked Association

    // Initialize the view model with the mocked repository
    searchViewModel = spyk(SearchViewModel(searchRepository))

    // Set the initial value of the associations flow
    every { searchViewModel.associations } returns
        flowOf(listOf(MockAssociation.createMockAssociation()))
            as StateFlow<List<Association>> // Use a list of mocked Association objects
  }

  @Test
  fun testEverythingIsDisplayed() {
    composeTestRule.setContent {
      UserProfileScreenScaffold(user, navigationAction, false, searchViewModel = searchViewModel) {}
    }

    composeTestRule.onNodeWithTag(UserProfileTestTags.PROFILE_PICTURE).assertExists()

    composeTestRule.onNodeWithTag(UserProfileTestTags.NAME).assertExists()
    composeTestRule
        .onNodeWithTag(UserProfileTestTags.NAME)
        .assertTextEquals("${user.firstName} ${user.lastName}")

    composeTestRule.onNodeWithTag(UserProfileTestTags.BIOGRAPHY).assertExists()
    composeTestRule.onNodeWithTag(UserProfileTestTags.BIOGRAPHY).assertTextEquals(user.biography)

    composeTestRule
        .onAllNodesWithTag(UserProfileTestTags.SOCIAL_BUTTON)
        .assertCountEquals(user.socials.size)
    composeTestRule
        .onAllNodesWithTag(UserProfileTestTags.INTEREST)
        .assertCountEquals(user.interests.size)
  }

  @Test
  fun testBottomSheet() {

    composeTestRule.setContent { UserProfileBottomSheet(true, navigationAction) {} }

    composeTestRule.onNodeWithTag(UserProfileTestTags.BOTTOM_SHEET).assertIsDisplayed()
  }

  @After
  fun tearDown() {
    clearAllMocks()
    unmockkAll()
  }
}
