package com.android.unio.ui.home

import androidx.compose.ui.test.assertHasClickAction
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.unit.ExperimentalUnitApi
import com.android.unio.mocks.user.MockUser
import com.android.unio.model.event.Event
import com.android.unio.model.event.EventRepository
import com.android.unio.model.event.EventRepositoryMock
import com.android.unio.model.event.EventViewModel
import com.android.unio.model.image.ImageRepositoryFirebaseStorage
import com.android.unio.model.search.SearchRepository
import com.android.unio.model.search.SearchViewModel
import com.android.unio.model.strings.test_tags.HomeTestTags
import com.android.unio.model.user.UserRepositoryFirestore
import com.android.unio.model.user.UserViewModel
import com.android.unio.ui.navigation.NavigationAction
import com.android.unio.ui.navigation.Screen
import com.android.unio.ui.navigation.TopLevelDestination
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import io.mockk.MockKAnnotations
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.spyk
import io.mockk.verify
import javax.inject.Inject
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runBlockingTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test

/**
 * Test class for the HomeScreen Composable. This class contains unit tests to validate the behavior
 * of the Event List UI.
 */
@HiltAndroidTest
@ExperimentalUnitApi
class HomeTest {

  @get:Rule val composeTestRule = createComposeRule()
  @get:Rule val hiltRule = HiltAndroidRule(this)

  private lateinit var userViewModel: UserViewModel

  // Mock event repository to provide test data.
  @Inject lateinit var mockEventRepository: EventRepository
  @MockK private lateinit var navigationAction: NavigationAction
  @MockK private lateinit var imageRepository: ImageRepositoryFirebaseStorage
  @MockK private lateinit var userRepository: UserRepositoryFirestore

  private lateinit var searchViewModel: SearchViewModel
  @MockK(relaxed = true) private lateinit var searchRepository: SearchRepository

  @Before
  fun setUp() {
    MockKAnnotations.init(this)
    hiltRule.inject()
    searchViewModel = spyk(SearchViewModel(searchRepository))
    every { navigationAction.navigateTo(any(TopLevelDestination::class)) } returns Unit
    every { navigationAction.navigateTo(any(String::class)) } returns Unit
    userViewModel = spyk(UserViewModel(userRepository))
    val user = MockUser.createMockUser()
    every { userRepository.updateUser(user, any(), any()) } answers
        {
          val onSuccess = args[1] as () -> Unit
          onSuccess()
        }
    userViewModel.addUser(user, {})
  }

  /**
   * Tests the UI when the event list is empty. Asserts that the appropriate message is displayed
   * when there are no events available.
   */
  @Test
  fun testEmptyEventList() {
    composeTestRule.setContent {
      val emptyEventRepository =
          object : EventRepositoryMock() {
            override fun getEvents(
                onSuccess: (List<Event>) -> Unit,
                onFailure: (Exception) -> Unit
            ) {
              onSuccess(emptyList())
            }
          }
      val eventViewModel = EventViewModel(emptyEventRepository, imageRepository)
      HomeScreen(navigationAction, eventViewModel, userViewModel, searchViewModel)
    }
    composeTestRule.onNodeWithTag(HomeTestTags.EMPTY_EVENT_PROMPT).assertExists()
    composeTestRule.onNodeWithText("No events available.").assertExists()
  }

  /**
   * Tests the functionality of the Map button. Verifies that clicking the button triggers the
   * expected action.
   */
  @Test
  fun testMapButton() {
    composeTestRule.setContent {
      val eventViewModel = EventViewModel(mockEventRepository, imageRepository)
      HomeScreen(navigationAction, eventViewModel, userViewModel, searchViewModel)
    }
    composeTestRule.onNodeWithTag(HomeTestTags.MAP_BUTTON).assertExists()
    composeTestRule.onNodeWithTag(HomeTestTags.MAP_BUTTON).assertHasClickAction()

    composeTestRule.onNodeWithTag(HomeTestTags.MAP_BUTTON).performClick()
    verify { navigationAction.navigateTo(Screen.MAP) }
  }

  /**
   * Tests the sequence of clicking on the 'Following' tab and then on the 'Map' button to ensure
   * that both actions trigger their respective animations and behaviors.
   */
  @OptIn(ExperimentalCoroutinesApi::class)
  @Test
  fun testClickFollowingAndAdd() = runBlockingTest {
    composeTestRule.setContent {
      val eventViewModel = EventViewModel(mockEventRepository, imageRepository)
      HomeScreen(navigationAction, eventViewModel, userViewModel, searchViewModel)
    }

    composeTestRule.onNodeWithTag(HomeTestTags.TAB_SAVED).assertExists()
    composeTestRule.onNodeWithTag(HomeTestTags.TAB_SAVED).performClick()

    composeTestRule.onNodeWithTag(HomeTestTags.MAP_BUTTON).assertExists()
    composeTestRule.onNodeWithTag(HomeTestTags.MAP_BUTTON).performClick()

    verify { navigationAction.navigateTo(Screen.MAP) }
  }
}