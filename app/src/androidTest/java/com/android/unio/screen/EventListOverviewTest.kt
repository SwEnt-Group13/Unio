package com.android.unio.ui.events

import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.unit.ExperimentalUnitApi
import androidx.navigation.NavHostController
import androidx.test.core.app.ApplicationProvider
import com.android.unio.model.event.Event
import com.android.unio.model.event.EventListViewModel
import com.android.unio.model.event.EventRepositoryMock
import com.android.unio.ui.home.HomeScreen
import com.android.unio.ui.navigation.NavigationAction
import com.google.firebase.FirebaseApp
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.kotlin.mock

// import org.robolectric.RobolectricTestRunner

/**
 * Test class for the EventListOverview Composable. This class contains unit tests to validate the
 * behavior of the Event List UI.
 */
@ExperimentalUnitApi
class EventListOverviewTest {

  @get:Rule val composeTestRule = createComposeRule()

  // Mock event repository to provide test data.
  private val mockEventRepository = EventRepositoryMock()
  private lateinit var navHostController: NavHostController
  private lateinit var navigationAction: NavigationAction

  @Before
  fun setUp() {
    if (FirebaseApp.getApps(ApplicationProvider.getApplicationContext()).isEmpty()) {
      FirebaseApp.initializeApp(ApplicationProvider.getApplicationContext())
    }
    navHostController = mock { NavHostController::class.java }
    navigationAction = NavigationAction(navHostController)
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
