package com.android.unio.ui.event

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import com.android.unio.mocks.event.MockEvent
import com.android.unio.mocks.map.MockLocation
import com.android.unio.model.event.EventType
import com.android.unio.model.user.UserRepositoryFirestore
import com.android.unio.model.user.UserViewModel
import com.google.firebase.Firebase
import com.google.firebase.Timestamp
import com.google.firebase.firestore.firestore
import java.util.*
import org.junit.Rule
import org.junit.Test

class EventCardTest {

  @get:Rule val composeTestRule = createComposeRule()

  private val sampleEvent =
      MockEvent.createMockEvent(
          uid = "sample_event_123",
          location = MockLocation.createMockLocation(name = "Sample Location"),
          date = Timestamp(Date(2024 - 1900, 6, 20)),
          catchyDescription = "This is a catchy description.")

  private val userViewModel = UserViewModel(UserRepositoryFirestore(Firebase.firestore), false)

  @Test
  fun testEventCardElementsExist() {
    composeTestRule.setContent { EventCard(event = sampleEvent, userViewModel = userViewModel) }

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
    composeTestRule.setContent { EventCard(event = sampleEvent, userViewModel = userViewModel) }

    // Check if the fallback image is displayed when no image is provided
    composeTestRule
        .onNodeWithTag("event_EventImage", useUnmergedTree = true)
        .assertExists() // Fallback image exists when no image is provided
  }

  @Test
  fun testEventCardWithEmptyUid() {
    val event = MockEvent.createMockEvent(uid = MockEvent.Companion.EdgeCaseUid.EMPTY.value)
    composeTestRule.setContent { EventCard(event = event, userViewModel = userViewModel) }
    composeTestRule.onNodeWithTag("event_EventTitle").assertExists() // Ensure the title exists
  }

  @Test
  fun testEventCardWithSpecialCharactersUid() {
    val event =
        MockEvent.createMockEvent(uid = MockEvent.Companion.EdgeCaseUid.SPECIAL_CHARACTERS.value)
    composeTestRule.setContent { EventCard(event = event, userViewModel = userViewModel) }
    composeTestRule.onNodeWithTag("event_EventTitle").assertExists() // Ensure the title exists
  }

  @Test
  fun testEventCardWithLongUid() {
    val event = MockEvent.createMockEvent(uid = MockEvent.Companion.EdgeCaseUid.LONG.value)
    composeTestRule.setContent { EventCard(event = event, userViewModel = userViewModel) }
    composeTestRule.onNodeWithTag("event_EventTitle").assertExists() // Ensure the title exists
  }

  @Test
  fun testEventCardWithTypicalUid() {
    val event = MockEvent.createMockEvent(uid = MockEvent.Companion.EdgeCaseUid.TYPICAL.value)
    composeTestRule.setContent { EventCard(event = event, userViewModel = userViewModel) }
    composeTestRule.onNodeWithTag("event_EventTitle").assertExists() // Ensure the title exists
  }

  @Test
  fun testEventCardWithEmptyTitle() {
    val event = MockEvent.createMockEvent(title = MockEvent.Companion.EdgeCaseTitle.EMPTY.value)
    composeTestRule.setContent { EventCard(event = event, userViewModel = userViewModel) }
    composeTestRule
        .onNodeWithTag("event_EventTitle")
        .assertTextEquals(MockEvent.Companion.EdgeCaseTitle.EMPTY.value)
  }

  @Test
  fun testEventCardWithShortTitle() {
    val event = MockEvent.createMockEvent(title = MockEvent.Companion.EdgeCaseTitle.SHORT.value)
    composeTestRule.setContent { EventCard(event = event, userViewModel = userViewModel) }
    composeTestRule
        .onNodeWithTag("event_EventTitle")
        .assertTextEquals(MockEvent.Companion.EdgeCaseTitle.SHORT.value)
  }

  @Test
  fun testEventCardWithLongTitle() {
    val event = MockEvent.createMockEvent(title = MockEvent.Companion.EdgeCaseTitle.LONG.value)
    composeTestRule.setContent { EventCard(event = event, userViewModel = userViewModel) }
    composeTestRule
        .onNodeWithTag("event_EventTitle")
        .assertTextEquals(MockEvent.Companion.EdgeCaseTitle.LONG.value)
  }

  @Test
  fun testEventCardWithSpecialCharactersTitle() {
    val event =
        MockEvent.createMockEvent(
            title = MockEvent.Companion.EdgeCaseTitle.SPECIAL_CHARACTERS.value)
    composeTestRule.setContent { EventCard(event = event, userViewModel = userViewModel) }
    composeTestRule
        .onNodeWithTag("event_EventTitle")
        .assertTextEquals(MockEvent.Companion.EdgeCaseTitle.SPECIAL_CHARACTERS.value)
  }

  /** Test each edge case for Event Image */
  @Test
  fun testEventCardWithEmptyImage() {
    val event = MockEvent.createMockEvent(image = MockEvent.Companion.EdgeCaseImage.EMPTY.value)
    composeTestRule.setContent { EventCard(event = event, userViewModel = userViewModel) }
    composeTestRule.onNodeWithTag("event_EventImage").assertExists() // Expect fallback image
  }

  @Test
  fun testEventCardWithTypicalImage() {
    val event = MockEvent.createMockEvent(image = MockEvent.Companion.EdgeCaseImage.TYPICAL.value)
    composeTestRule.setContent { EventCard(event = event, userViewModel = userViewModel) }
    composeTestRule.onNodeWithTag("event_EventImage").assertExists() // Expect image to exist
  }

  @Test
  fun testEventCardWithLongImage() {
    val event = MockEvent.createMockEvent(image = MockEvent.Companion.EdgeCaseImage.LONG.value)
    composeTestRule.setContent { EventCard(event = event, userViewModel = userViewModel) }
    composeTestRule.onNodeWithTag("event_EventImage").assertExists() // Expect image to exist
  }

  @Test
  fun testEventCardWithInvalidImage() {
    val event = MockEvent.createMockEvent(image = MockEvent.Companion.EdgeCaseImage.INVALID.value)
    composeTestRule.setContent { EventCard(event = event, userViewModel = userViewModel) }
    composeTestRule.onNodeWithTag("event_EventImage").assertExists() // Expect image to exist
  }

  /** Test each edge case for Event Description */

  /** Test each edge case for Event Catchy Description */
  @Test
  fun testEventCardWithEmptyCatchyDescription() {
    val event =
        MockEvent.createMockEvent(
            catchyDescription = MockEvent.Companion.EdgeCaseCatchyDescription.EMPTY.value)
    composeTestRule.setContent { EventCard(event = event, userViewModel = userViewModel) }
    composeTestRule
        .onNodeWithTag("event_EventCatchyDescription")
        .assertTextEquals("") // Expect empty catchy description
  }

  @Test
  fun testEventCardWithShortCatchyDescription() {
    val event =
        MockEvent.createMockEvent(
            catchyDescription = MockEvent.Companion.EdgeCaseCatchyDescription.SHORT.value)
    composeTestRule.setContent { EventCard(event = event, userViewModel = userViewModel) }
    composeTestRule
        .onNodeWithTag("event_EventCatchyDescription")
        .assertTextEquals(MockEvent.Companion.EdgeCaseCatchyDescription.SHORT.value)
  }

  @Test
  fun testEventCardWithLongCatchyDescription() {
    val event =
        MockEvent.createMockEvent(
            catchyDescription = MockEvent.Companion.EdgeCaseCatchyDescription.LONG.value)
    composeTestRule.setContent { EventCard(event = event, userViewModel = userViewModel) }
    composeTestRule
        .onNodeWithTag("event_EventCatchyDescription")
        .assertTextEquals(MockEvent.Companion.EdgeCaseCatchyDescription.LONG.value)
  }

  @Test
  fun testEventCardWithSpecialCharactersCatchyDescription() {
    val event =
        MockEvent.createMockEvent(
            catchyDescription =
                MockEvent.Companion.EdgeCaseCatchyDescription.SPECIAL_CHARACTERS.value)
    composeTestRule.setContent { EventCard(event = event, userViewModel = userViewModel) }
    composeTestRule
        .onNodeWithTag("event_EventCatchyDescription")
        .assertTextEquals(MockEvent.Companion.EdgeCaseCatchyDescription.SPECIAL_CHARACTERS.value)
  }

  /** Test each edge case for Event Date */
  @Test
  fun testEventCardWithPastDate() {
    val event = MockEvent.createMockEvent(date = MockEvent.Companion.EdgeCaseDate.PAST.value)
    composeTestRule.setContent { EventCard(event = event, userViewModel = userViewModel) }
    composeTestRule.onNodeWithTag("event_EventDate").assertExists()
  }

  @Test
  fun testEventCardWithTodayDate() {
    val event = MockEvent.createMockEvent(date = MockEvent.Companion.EdgeCaseDate.TODAY.value)
    composeTestRule.setContent { EventCard(event = event, userViewModel = userViewModel) }
    composeTestRule.onNodeWithTag("event_EventDate").assertExists()
  }

  @Test
  fun testEventCardWithFutureDate() {
    val event = MockEvent.createMockEvent(date = MockEvent.Companion.EdgeCaseDate.FUTURE.value)
    composeTestRule.setContent { EventCard(event = event, userViewModel = userViewModel) }
    composeTestRule.onNodeWithTag("event_EventDate").assertExists()
  }

  @Test
  fun testEventCardWithFarFutureDate() {
    val event = MockEvent.createMockEvent(date = MockEvent.Companion.EdgeCaseDate.FAR_FUTURE.value)
    composeTestRule.setContent { EventCard(event = event, userViewModel = userViewModel) }
    composeTestRule.onNodeWithTag("event_EventDate").assertExists()
  }
}
