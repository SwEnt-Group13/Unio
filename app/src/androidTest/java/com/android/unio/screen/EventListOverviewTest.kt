package com.android.unio.ui.events

import androidx.compose.ui.test.assertHasClickAction
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.unit.ExperimentalUnitApi
import com.android.unio.model.event.Event
import com.android.unio.model.event.EventRepositoryMock
import com.android.unio.model.event.EventViewModel
import com.android.unio.model.search.SearchRepository
import com.android.unio.model.search.SearchViewModel
import com.android.unio.ui.home.HomeScreen
import com.android.unio.ui.navigation.NavigationAction
import com.android.unio.ui.navigation.Screen
import io.mockk.MockKAnnotations
import io.mockk.impl.annotations.MockK
import io.mockk.spyk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runBlockingTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.Mockito.mock
import org.mockito.kotlin.verify

/**
 * Test class for the HomeScreen Composable. This class contains unit tests to validate the behavior
 * of the Event List UI.
 */
@ExperimentalUnitApi
class EventListOverviewTest {

  @get:Rule val composeTestRule = createComposeRule()

  // Mock event repository to provide test data.
  private val mockEventRepository = EventRepositoryMock()
  private lateinit var navigationAction: NavigationAction

  private lateinit var searchViewModel: SearchViewModel
  @MockK(relaxed = true) private lateinit var searchRepository: SearchRepository

  @Before
  fun setUp() {
    MockKAnnotations.init(this)
    searchViewModel = spyk(SearchViewModel(searchRepository))

    navigationAction = mock(NavigationAction::class.java)
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
      val eventViewModel = EventViewModel(emptyEventRepository)
      HomeScreen(
          navigationAction, eventViewModel = eventViewModel, searchViewModel = searchViewModel)
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
      val eventViewModel = EventViewModel(mockEventRepository)
      HomeScreen(
          navigationAction = navigationAction,
          eventViewModel = eventViewModel,
          searchViewModel = searchViewModel)
    }
    composeTestRule.onNodeWithTag("event_MapButton").assertExists()
    composeTestRule.onNodeWithTag("event_MapButton").assertHasClickAction()

    composeTestRule.onNodeWithTag("event_MapButton").performClick()
    verify(navigationAction).navigateTo(Screen.MAP)
  }

  /**
   * Tests the sequence of clicking on the 'Following' tab and then on the 'Add' button to ensure
   * that both actions trigger their respective animations and behaviors.
   */
  @OptIn(ExperimentalCoroutinesApi::class)
  @Test
  fun testClickFollowingAndAdd() = runBlockingTest {
    composeTestRule.setContent {
      val eventViewModel = EventViewModel(mockEventRepository)
      HomeScreen(
          navigationAction, eventViewModel = eventViewModel, searchViewModel = searchViewModel)
    }

    // Ensure the 'Following' tab exists and perform a click.
    composeTestRule.onNodeWithTag("event_tabFollowing").assertExists()
    composeTestRule.onNodeWithTag("event_tabFollowing").performClick()

    // Perform a click on the 'Add' button.
    composeTestRule.onNodeWithTag("event_MapButton").assertExists()
    composeTestRule.onNodeWithTag("event_MapButton").performClick()

    verify(navigationAction).navigateTo(Screen.MAP)
  }
}
