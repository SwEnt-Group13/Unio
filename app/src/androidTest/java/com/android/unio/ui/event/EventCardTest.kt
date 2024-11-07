package com.android.unio.ui.event

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertTextEquals
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import com.android.unio.mocks.event.MockEvent
import com.android.unio.mocks.map.MockLocation
import com.android.unio.model.event.Event
import com.android.unio.model.event.EventRepositoryFirestore
import com.android.unio.model.event.EventType
import com.android.unio.model.event.EventViewModel
import com.android.unio.model.image.ImageRepositoryFirebaseStorage
import com.android.unio.model.user.UserRepositoryFirestore
import com.android.unio.model.user.UserViewModel
import com.android.unio.ui.navigation.NavigationAction
import com.google.firebase.Timestamp
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.Mockito.mock
import org.mockito.MockitoAnnotations
import java.util.Date
import javax.inject.Inject

@HiltAndroidTest
class EventCardTest {

  @get:Rule val composeTestRule = createComposeRule()
  @get:Rule val hiltRule = HiltAndroidRule(this)
  private lateinit var navigationAction: NavigationAction

  private val sampleEvent =
      MockEvent.createMockEvent(
          uid = "sample_event_123",
          location = MockLocation.createMockLocation(name = "Sample Location"),
          date = Timestamp(Date(2024 - 1900, 6, 20)),
          catchyDescription = "This is a catchy description.")
  @Inject lateinit var userRepositoryFirestore: UserRepositoryFirestore
  private lateinit var userViewModel: UserViewModel
    @Inject lateinit var eventRepositoryFirestore: EventRepositoryFirestore
    private lateinit var eventViewModel: EventViewModel
    @Inject lateinit var imageRepository: ImageRepositoryFirebaseStorage

  @Before
  fun setUp() {
    MockitoAnnotations.openMocks(this)
    hiltRule.inject()
    navigationAction = mock(NavigationAction::class.java)

    userViewModel = UserViewModel(userRepositoryFirestore)
      eventViewModel = EventViewModel(eventRepositoryFirestore, imageRepository)
  }

  private fun setEventScreen(event: Event) {
    composeTestRule.setContent {
      EventCard(
          navigationAction = navigationAction,
          event = event,
          userViewModel = userViewModel,
          eventViewModel)
    }
  }

  @Test
  fun testEventCardElementsExist() {
    setEventScreen(sampleEvent)

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
    setEventScreen(sampleEvent)

    // Check if the fallback image is displayed when no image is provided
    composeTestRule
        .onNodeWithTag("event_EventImage", useUnmergedTree = true)
        .assertExists() // Fallback image exists when no image is provided
  }

  @Test
  fun testEventCardWithEmptyUid() {
    val event = MockEvent.createMockEvent(uid = MockEvent.Companion.EdgeCaseUid.EMPTY.value)
    setEventScreen(event)
    composeTestRule
        .onNodeWithTag("event_EventTitle", useUnmergedTree = true)
        .assertIsDisplayed() // Ensure the title exists
  }

  @Test
  fun testEventCardWithSpecialCharactersUid() {
    val event =
        MockEvent.createMockEvent(uid = MockEvent.Companion.EdgeCaseUid.SPECIAL_CHARACTERS.value)
    setEventScreen(event)
    composeTestRule
        .onNodeWithTag("event_EventTitle", useUnmergedTree = true)
        .assertExists() // Ensure the title exists
  }

  @Test
  fun testEventCardWithLongUid() {
    val event = MockEvent.createMockEvent(uid = MockEvent.Companion.EdgeCaseUid.LONG.value)
    setEventScreen(event)
    composeTestRule
        .onNodeWithTag("event_EventTitle", useUnmergedTree = true)
        .assertExists() // Ensure the title exists
  }

  @Test
  fun testEventCardWithTypicalUid() {
    val event = MockEvent.createMockEvent(uid = MockEvent.Companion.EdgeCaseUid.TYPICAL.value)
    setEventScreen(event)
    composeTestRule
        .onNodeWithTag("event_EventTitle", useUnmergedTree = true)
        .assertExists() // Ensure the title exists
  }

  @Test
  fun testEventCardWithEmptyTitle() {
    val event = MockEvent.createMockEvent(title = MockEvent.Companion.EdgeCaseTitle.EMPTY.value)
    setEventScreen(event)
    composeTestRule
        .onNodeWithTag("event_EventTitle", useUnmergedTree = true)
        .assertTextEquals(MockEvent.Companion.EdgeCaseTitle.EMPTY.value)
  }

  @Test
  fun testEventCardWithShortTitle() {
    val event = MockEvent.createMockEvent(title = MockEvent.Companion.EdgeCaseTitle.SHORT.value)
    setEventScreen(event)
    composeTestRule
        .onNodeWithTag("event_EventTitle", useUnmergedTree = true)
        .assertTextEquals(MockEvent.Companion.EdgeCaseTitle.SHORT.value)
  }

  @Test
  fun testEventCardWithLongTitle() {
    val event = MockEvent.createMockEvent(title = MockEvent.Companion.EdgeCaseTitle.LONG.value)
    setEventScreen(event)
    composeTestRule
        .onNodeWithTag("event_EventTitle", useUnmergedTree = true)
        .assertTextEquals(MockEvent.Companion.EdgeCaseTitle.LONG.value)
  }

  @Test
  fun testEventCardWithSpecialCharactersTitle() {
    val event =
        MockEvent.createMockEvent(
            title = MockEvent.Companion.EdgeCaseTitle.SPECIAL_CHARACTERS.value)
    setEventScreen(event)
    composeTestRule
        .onNodeWithTag("event_EventTitle", useUnmergedTree = true)
        .assertTextEquals(MockEvent.Companion.EdgeCaseTitle.SPECIAL_CHARACTERS.value)
  }

  /** Test each edge case for Event Image */
  @Test
  fun testEventCardWithEmptyImage() {
    val event = MockEvent.createMockEvent(image = MockEvent.Companion.EdgeCaseImage.EMPTY.value)
    setEventScreen(event)
    composeTestRule
        .onNodeWithTag("event_EventImage", useUnmergedTree = true)
        .assertExists() // Expect fallback image
  }

  @Test
  fun testEventCardWithTypicalImage() {
    val event = MockEvent.createMockEvent(image = MockEvent.Companion.EdgeCaseImage.TYPICAL.value)
    setEventScreen(event)
    composeTestRule
        .onNodeWithTag("event_EventImage", useUnmergedTree = true)
        .assertExists() // Expect image to exist
  }

  @Test
  fun testEventCardWithLongImage() {
    val event = MockEvent.createMockEvent(image = MockEvent.Companion.EdgeCaseImage.LONG.value)
    setEventScreen(event)
    composeTestRule
        .onNodeWithTag("event_EventImage", useUnmergedTree = true)
        .assertExists() // Expect image to exist
  }

  @Test
  fun testEventCardWithInvalidImage() {
    val event = MockEvent.createMockEvent(image = MockEvent.Companion.EdgeCaseImage.INVALID.value)
    setEventScreen(event)
    composeTestRule
        .onNodeWithTag("event_EventImage", useUnmergedTree = true)
        .assertExists() // Expect image to exist
  }

  /** Test each edge case for Event Description */

  /** Test each edge case for Event Catchy Description */
  @Test
  fun testEventCardWithEmptyCatchyDescription() {
    val event =
        MockEvent.createMockEvent(
            catchyDescription = MockEvent.Companion.EdgeCaseCatchyDescription.EMPTY.value)
    setEventScreen(event)
    composeTestRule
        .onNodeWithTag("event_EventCatchyDescription", useUnmergedTree = true)
        .assertTextEquals("") // Expect empty catchy description
  }

  @Test
  fun testEventCardWithShortCatchyDescription() {
    val event =
        MockEvent.createMockEvent(
            catchyDescription = MockEvent.Companion.EdgeCaseCatchyDescription.SHORT.value)
    setEventScreen(event)
    composeTestRule
        .onNodeWithTag("event_EventCatchyDescription", useUnmergedTree = true)
        .assertTextEquals(MockEvent.Companion.EdgeCaseCatchyDescription.SHORT.value)
  }

  @Test
  fun testEventCardWithLongCatchyDescription() {
    val event =
        MockEvent.createMockEvent(
            catchyDescription = MockEvent.Companion.EdgeCaseCatchyDescription.LONG.value)
    setEventScreen(event)
    composeTestRule
        .onNodeWithTag("event_EventCatchyDescription", useUnmergedTree = true)
        .assertTextEquals(MockEvent.Companion.EdgeCaseCatchyDescription.LONG.value)
  }

  @Test
  fun testEventCardWithSpecialCharactersCatchyDescription() {
    val event =
        MockEvent.createMockEvent(
            catchyDescription =
                MockEvent.Companion.EdgeCaseCatchyDescription.SPECIAL_CHARACTERS.value)
    setEventScreen(event)
    composeTestRule
        .onNodeWithTag("event_EventCatchyDescription", useUnmergedTree = true)
        .assertTextEquals(MockEvent.Companion.EdgeCaseCatchyDescription.SPECIAL_CHARACTERS.value)
  }

  /** Test each edge case for Event Date */
  @Test
  fun testEventCardWithPastDate() {
    val event = MockEvent.createMockEvent(date = MockEvent.Companion.EdgeCaseDate.PAST.value)
    setEventScreen(event)
    composeTestRule.onNodeWithTag("event_EventDate", useUnmergedTree = true).assertExists()
  }

  @Test
  fun testEventCardWithTodayDate() {
    val event = MockEvent.createMockEvent(date = MockEvent.Companion.EdgeCaseDate.TODAY.value)
    setEventScreen(event)
    composeTestRule.onNodeWithTag("event_EventDate", useUnmergedTree = true).assertExists()
  }

  @Test
  fun testEventCardWithFutureDate() {
    val event = MockEvent.createMockEvent(date = MockEvent.Companion.EdgeCaseDate.FUTURE.value)
    setEventScreen(event)
    composeTestRule.onNodeWithTag("event_EventDate", useUnmergedTree = true).assertExists()
  }

  @Test
  fun testEventCardWithFarFutureDate() {
    val event = MockEvent.createMockEvent(date = MockEvent.Companion.EdgeCaseDate.FAR_FUTURE.value)
    setEventScreen(event)
    composeTestRule.onNodeWithTag("event_EventDate", useUnmergedTree = true).assertExists()
  }
}
