package com.android.unio.ui.events

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.unit.ExperimentalUnitApi
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.android.unio.model.events.Event
import com.android.unio.model.events.EventRepositoryMock
import com.android.unio.model.events.EventListViewModel
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Test class for the EventListOverview Composable.
 * This class contains unit tests to validate the behavior of the Event List UI.
 */
@ExperimentalUnitApi
@RunWith(AndroidJUnit4::class)
class EventListOverviewTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    // Mock event repository to provide test data.
    private val mockEventRepository = EventRepositoryMock()

    /**
     * Tests the functionality of switching between tabs and verifying animations.
     * Ensures that the 'All' tab exists and can be clicked,
     * and verifies the underlying bar's presence when switching tabs.
     */
    @Test
    fun testTabSwitchingAndAnimation() {
        composeTestRule.setContent {
            val eventListViewModel = EventListViewModel(mockEventRepository)
            EventListOverview(
                eventListViewModel = eventListViewModel,
                onAddEvent = { },
                onEventClick = { }
            )
        }

        // Assert that the 'All' tab exists and has a click action.
        composeTestRule.onNodeWithTag("event_tabAll").assertExists()
        composeTestRule.onNodeWithTag("event_tabAll").assertHasClickAction()

        // Assert that the underlying bar exists.
        composeTestRule.onNodeWithTag("event_UnderlyingBar").assertExists()

        // Perform a click on the 'Following' tab.
        composeTestRule.onNodeWithTag("event_tabFollowing").performClick()

        // Assert that the 'Following' tab and the underlying bar still exist.
        composeTestRule.onNodeWithTag("event_tabFollowing").assertExists()
        composeTestRule.onNodeWithTag("event_UnderlyingBar").assertExists()
    }

    /**
     * Tests the UI when the event list is empty.
     * Asserts that the appropriate message is displayed when there are no events available.
     */
    @Test
    fun testEmptyEventList() {
        composeTestRule.setContent {
            val emptyEventRepository = object : EventRepositoryMock() {
                override fun getEvents(): List<Event> = emptyList()
            }
            val eventListViewModel = EventListViewModel(emptyEventRepository)
            EventListOverview(
                eventListViewModel = eventListViewModel,
                onAddEvent = { },
                onEventClick = { }
            )
        }

        // Assert that the empty event prompt is displayed.
        composeTestRule.onNodeWithTag("event_emptyEventPrompt").assertExists()
        composeTestRule.onNodeWithText("No events available.").assertExists()
    }

    /**
     * Tests the UI when there are events available.
     * Asserts that all event items are displayed correctly in the list
     * and checks that each event's title and catchy description are present.
     */
    @Test
    fun testNonEmptyEventList() {
        composeTestRule.setContent {
            val eventListViewModel = EventListViewModel(mockEventRepository)
            EventListOverview(
                eventListViewModel = eventListViewModel,
                onAddEvent = { },
                onEventClick = { }
            )
        }

        // Assert that the number of event list items matches the number of events in the mock repository.
        composeTestRule.onAllNodesWithTag("event_EventListItem", useUnmergedTree = true)
            .assertCountEquals(mockEventRepository.getEvents().size)

        // Verify each event's title and catchy description.
        mockEventRepository.getEvents().forEach { event ->
            composeTestRule.onAllNodesWithTag("event_EventTitle", useUnmergedTree = true)
                .filter(hasText(event.title))
                .assertCountEquals(1)

            composeTestRule.onAllNodesWithTag("event_CatchyDescription", useUnmergedTree = true)
                .filter(hasText(event.catchy_description))
                .assertCountEquals(1)

            /* Uncomment below to check main type if necessary.
            composeTestRule.onAllNodesWithTag("event_EventMainType", useUnmergedTree = true)
                .filter(hasText(event.main_type))
            */

            /* Uncomment below to check if event images exist if necessary.
            composeTestRule.onNodeWithTag("event_EventImage", useUnmergedTree = true)
                .assertExists() // Check that the image exists
            */
        }
    }

    /**
     * Tests the functionality of the Map button.
     * Verifies that clicking the button triggers the expected action.
     */
    @Test
    fun testMapButton() {
        var mapClicked = false

        composeTestRule.setContent {
            val eventListViewModel = EventListViewModel(mockEventRepository)
            EventListOverview(
                eventListViewModel = eventListViewModel,
                onAddEvent = { mapClicked = true },
                onEventClick = { }
            )
        }


        composeTestRule.onNodeWithTag("event_MapButton").performClick()


        assert(mapClicked)
    }
}
