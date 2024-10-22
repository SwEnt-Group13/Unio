package com.android.unio.ui.events

import androidx.compose.ui.test.assertHasClickAction
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.unit.ExperimentalUnitApi
import androidx.navigation.NavHostController
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.android.unio.model.event.Event
import com.android.unio.model.event.EventListViewModel
import com.android.unio.model.event.EventRepositoryMock
import com.android.unio.ui.home.HomeScreen
import com.android.unio.ui.navigation.NavigationAction
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.kotlin.mock

/**
 * Test class for the EventListOverview Composable. This class contains unit tests to validate the
 * behavior of the Event List UI.
 */
@ExperimentalUnitApi
@RunWith(AndroidJUnit4::class)
class EventListOverviewTest {

  @get:Rule val composeTestRule = createComposeRule()

  // Mock event repository to provide test data.
  private val mockEventRepository = EventRepositoryMock()
  private lateinit var navHostController: NavHostController
  private lateinit var navigationAction: NavigationAction

  @Before
  fun setUp() {
    navHostController = mock { NavHostController::class.java }
    navigationAction = NavigationAction(navHostController)
  }

  /**
   * Tests the functionality of switching between tabs and verifying animations. Ensures that the
   * 'All' tab exists and can be clicked, and verifies the underlying bar's presence when switching
   * tabs.
   */
  @Test
  fun testTabSwitchingAndAnimation() {

    composeTestRule.setContent {
      val eventListViewModel = EventListViewModel(mockEventRepository)
      HomeScreen(navigationAction, eventListViewModel = eventListViewModel)
    }

    composeTestRule.onNodeWithTag("event_tabAll").assertExists()
    composeTestRule.onNodeWithTag("event_tabAll").assertHasClickAction()

    composeTestRule.onNodeWithTag("event_UnderlyingBar").assertExists()

    composeTestRule.onNodeWithTag("event_tabFollowing").performClick()

    composeTestRule.onNodeWithTag("event_tabFollowing").assertExists()
    composeTestRule.onNodeWithTag("event_UnderlyingBar").assertExists()
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
              // Return an empty list for testing
              onSuccess(emptyList())
            }
          }
      val eventListViewModel = EventListViewModel(emptyEventRepository)
      HomeScreen(navigationAction, eventListViewModel = eventListViewModel)
    }

    composeTestRule.onNodeWithTag("event_emptyEventPrompt").assertExists()
    composeTestRule.onNodeWithText("No events available.").assertExists()
  }
}
