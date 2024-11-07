package com.android.unio.ui.events

import androidx.compose.ui.test.assertHasClickAction
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.unit.ExperimentalUnitApi
import com.android.unio.model.event.Event
import com.android.unio.model.event.EventListViewModel
import com.android.unio.model.event.EventRepository
import com.android.unio.model.event.EventRepositoryMock
import com.android.unio.model.image.ImageRepositoryFirebaseStorage
import com.android.unio.model.user.UserRepositoryFirestore
import com.android.unio.model.user.UserViewModel
import com.android.unio.ui.home.HomeScreen
import com.android.unio.ui.navigation.NavigationAction
import com.android.unio.ui.navigation.Screen
import com.android.unio.ui.navigation.TopLevelDestination
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import io.mockk.MockKAnnotations
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.verify
import javax.inject.Inject
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runBlockingTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test

// import org.robolectric.RobolectricTestRunner

/**
 * Test class for the EventListOverview Composable. This class contains unit tests to validate the
 * behavior of the Event List UI.
 */
@HiltAndroidTest
@ExperimentalUnitApi
class EventListOverviewTest {

  @get:Rule val composeTestRule = createComposeRule()
  @get:Rule val hiltRule = HiltAndroidRule(this)

  private lateinit var userViewModel: UserViewModel
  private lateinit var eventListViewModel: EventListViewModel

  // Mock event repository to provide test data.
  @Inject lateinit var mockEventRepository: EventRepository
  @MockK private lateinit var navigationAction: NavigationAction
  @MockK private lateinit var imageRepository: ImageRepositoryFirebaseStorage
  @MockK private lateinit var userRepository: UserRepositoryFirestore

  @Before
  fun setUp() {
    MockKAnnotations.init(this)
    hiltRule.inject()
    every { navigationAction.navigateTo(any(TopLevelDestination::class)) } returns Unit
    every { navigationAction.navigateTo(any(String::class)) } returns Unit
    userViewModel = UserViewModel(userRepository)
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
      eventListViewModel = EventListViewModel(emptyEventRepository, imageRepository)
      HomeScreen(navigationAction, eventListViewModel, userViewModel)
    }
    composeTestRule.onNodeWithTag("event_emptyEventPrompt").assertExists()
    composeTestRule.onNodeWithText("No events available.").assertExists()
  }

  /**
   * Tests the functionality of the Map button. Verifies that clicking the button triggers the
   * expected action.
   */
  @Test
  fun testMapButton() {
    composeTestRule.setContent {
      eventListViewModel = EventListViewModel(mockEventRepository, imageRepository)
      HomeScreen(navigationAction = navigationAction, eventListViewModel, userViewModel)
    }
    composeTestRule.onNodeWithTag("event_MapButton").assertExists()
    composeTestRule.onNodeWithTag("event_MapButton").assertHasClickAction()

    composeTestRule.onNodeWithTag("event_MapButton").performClick()
    verify { navigationAction.navigateTo(Screen.MAP) }
  }

  /**
   * Tests the sequence of clicking on the 'Following' tab and then on the 'Add' button to ensure
   * that both actions trigger their respective animations and behaviors.
   */
  @OptIn(ExperimentalCoroutinesApi::class)
  @Test
  fun testClickFollowingAndAdd() = runBlockingTest {
    composeTestRule.setContent {
      val eventListViewModel = EventListViewModel(mockEventRepository, imageRepository)
      HomeScreen(navigationAction, eventListViewModel, userViewModel)
    }

    // Ensure the 'Following' tab exists and perform a click.
    composeTestRule.onNodeWithTag("event_tabFollowing").assertExists()
    composeTestRule.onNodeWithTag("event_tabFollowing").performClick()

    // Perform a click on the 'Add' button.
    composeTestRule.onNodeWithTag("event_MapButton").assertExists()
    composeTestRule.onNodeWithTag("event_MapButton").performClick()

    verify { navigationAction.navigateTo(Screen.MAP) }
  }
}
