package com.android.unio.ui.event

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import com.android.unio.model.association.Association
import com.android.unio.model.event.Event
import com.android.unio.model.event.EventType
import com.android.unio.model.event.MockEventRepository
import com.android.unio.model.event.PreviewEventViewModel
import com.android.unio.model.firestore.firestoreReferenceListWith
import com.android.unio.model.map.Location
import com.android.unio.model.user.MockUserRepository
import com.google.firebase.Timestamp
import java.util.*
import org.junit.Rule
import org.junit.Test

class EventCardTest {

  @get:Rule val composeTestRule = createComposeRule()

  private val sampleEvent =
      Event(
          uid = "sample_event_123",
          title = "Sample Event",
          organisers = Association.firestoreReferenceListWith(listOf("1234")),
          taggedAssociations = Association.firestoreReferenceListWith(listOf("1234")),
          image = "", // No image to test fallback behavior
          description = "This is a detailed description of the sample event.",
          catchyDescription = "This is a catchy description.",
          price = 20.0,
          date = Timestamp(Date(2024 - 1900, 6, 20)),
          location = Location(0.0, 0.0, "Sample Location"),
          types = listOf(EventType.TRIP))

  private val viewModel = PreviewEventViewModel(MockEventRepository(), MockUserRepository())

  @Test
  fun testEventCardElementsExist() {
    composeTestRule.setContent { EventCard(event = sampleEvent, viewModel = viewModel) }

    composeTestRule
        .onNodeWithTag("event_EventTitle", useUnmergedTree = true)
        .assertExists()
        .assertTextEquals("Sample Event")

    composeTestRule
        .onNodeWithTag("event_EventMainType", useUnmergedTree = true)
        .assertExists()
        .assertTextEquals(EventType.TRIP.text)

    composeTestRule
        .onNodeWithTag("event_EventLocation", useUnmergedTree = true)
        .assertExists()
        .assertTextEquals("Sample Location")

    composeTestRule
        .onNodeWithTag("event_EventDate", useUnmergedTree = true)
        .assertExists()
        .assertTextEquals("20/07")

    composeTestRule
        .onNodeWithTag("event_EventTime", useUnmergedTree = true)
        .assertExists()
        .assertTextEquals("00:00")

    composeTestRule
        .onNodeWithTag("event_EventCatchyDescription", useUnmergedTree = true)
        .assertExists()
        .assertTextEquals("This is a catchy description.")
  }

  @Test
  fun testImageFallbackDisplayed() {
    composeTestRule.setContent { EventCard(event = sampleEvent, viewModel = viewModel) }

    // Check if the fallback image is displayed when no image is provided
    composeTestRule
        .onNodeWithTag("event_EventImage", useUnmergedTree = true)
        .assertExists() // Fallback image exists when no image is provided
  }
}
