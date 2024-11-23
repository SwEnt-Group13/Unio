package com.android.unio.ui.event

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertTextEquals
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import com.android.unio.TearDown
import com.android.unio.mocks.association.MockAssociation
import com.android.unio.mocks.event.MockEvent
import com.android.unio.mocks.map.MockLocation
import com.android.unio.model.event.Event
import com.android.unio.model.event.EventType
import com.android.unio.model.strings.test_tags.EventCardTestTags
import com.android.unio.ui.navigation.NavigationAction
import com.android.unio.ui.navigation.Screen
import com.google.firebase.Timestamp
import io.mockk.MockKAnnotations
import java.util.Date
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.Mockito.mock
import org.mockito.kotlin.verify

class EventCardTest : TearDown() {

  @get:Rule val composeTestRule = createComposeRule()

  private lateinit var navigationAction: NavigationAction
  private val imgUrl =
      "https://m.media-amazon.com/images/S/pv-target-images/4be23d776550ebae78e63f21bec3515d3347ac4f44a3fb81e6633cf7a116761e.jpg"

  private val sampleEvent =
      MockEvent.createMockEvent(
          uid = "sample_event_123",
          location = MockLocation.createMockLocation(name = "Sample Location"),
          startDate = Timestamp(Date(2025 - 1900, 6, 20)),
          endDate = Timestamp(Date(2025 - 1900, 6, 20)),
          catchyDescription = "This is a catchy description.")
  private val associations =
      listOf(
          MockAssociation.createMockAssociation(uid = "c"),
          MockAssociation.createMockAssociation(uid = "d"))

  @Before
  fun setUp() {
    MockKAnnotations.init(this, relaxed = true)
    navigationAction = mock(NavigationAction::class.java)
  }

  private fun setEventScreen(event: Event) {
    composeTestRule.setContent {
      EventCardScaffold(
          event, associations, true, { navigationAction.navigateTo(Screen.EVENT_DETAILS) }, {})
    }
  }

  @Test
  fun testEventCardElementsExist() {
    setEventScreen(sampleEvent)

    composeTestRule
        .onNodeWithTag(EventCardTestTags.EVENT_TITLE, useUnmergedTree = true)
        .assertExists()
        .assertTextEquals("Sample Event")

    composeTestRule
        .onNodeWithTag(EventCardTestTags.EVENT_MAIN_TYPE, useUnmergedTree = true)
        .assertExists()
        .assertTextEquals(EventType.TRIP.text)

    composeTestRule
        .onNodeWithTag(EventCardTestTags.EVENT_LOCATION, useUnmergedTree = true)
        .assertExists()
        .assertTextEquals("Sample Location")

    composeTestRule
        .onNodeWithTag(EventCardTestTags.EVENT_DATE, useUnmergedTree = true)
        .assertExists()
        .assertTextEquals("20/07")

    composeTestRule
        .onNodeWithTag(EventCardTestTags.EVENT_TIME, useUnmergedTree = true)
        .assertExists()
        .assertTextEquals("00:00")

    composeTestRule
        .onNodeWithTag(EventCardTestTags.EVENT_CATCHY_DESCRIPTION, useUnmergedTree = true)
        .assertExists()
        .assertTextEquals("This is a catchy description.")
  }

  @Test
  fun testClickOnEventCard() {
    setEventScreen(sampleEvent)
    composeTestRule
        .onNodeWithTag(EventCardTestTags.EVENT_TITLE, useUnmergedTree = true)
        .assertIsDisplayed()
        .performClick()
    verify(navigationAction).navigateTo(Screen.EVENT_DETAILS)
  }

  @Test
  fun testImageFallbackDisplayed() {
    setEventScreen(sampleEvent)

    // Check if the fallback image is displayed when no image is provided
    composeTestRule
        .onNodeWithTag(EventCardTestTags.EVENT_IMAGE, useUnmergedTree = true)
        .assertExists() // Fallback image exists when no image is provided
  }

  @Test
  fun testEventCardWithEmptyUid() {
    val event = MockEvent.createMockEvent(uid = MockEvent.Companion.EdgeCaseUid.EMPTY.value)
    setEventScreen(event)
    composeTestRule
        .onNodeWithTag(EventCardTestTags.EVENT_TITLE, useUnmergedTree = true)
        .assertIsDisplayed() // Ensure the title exists
  }

  @Test
  fun testEventCardWithEmptyTitle() {
    val event = MockEvent.createMockEvent(title = MockEvent.Companion.EdgeCaseTitle.EMPTY.value)
    setEventScreen(event)
    composeTestRule
        .onNodeWithTag(EventCardTestTags.EVENT_TITLE, useUnmergedTree = true)
        .assertTextEquals(MockEvent.Companion.EdgeCaseTitle.EMPTY.value)
  }

  @Test
  fun testEventCardWithInvalidImage() {
    val event = MockEvent.createMockEvent(image = MockEvent.Companion.EdgeCaseImage.INVALID.value)
    setEventScreen(event)
    composeTestRule
        .onNodeWithTag(EventCardTestTags.EVENT_IMAGE, useUnmergedTree = true)
        .assertExists() // Expect image to use fallback
  }

  @Test
  fun testEventCardWithValidImage() {
    val event = MockEvent.createMockEvent(image = imgUrl)
    setEventScreen(event)
    composeTestRule
        .onNodeWithTag(EventCardTestTags.EVENT_IMAGE, useUnmergedTree = true)
        .assertExists() // Expect image to use fallback
  }

  @Test
  fun testEventCardWithEmptyDescription() {
    val event =
        MockEvent.createMockEvent(
            catchyDescription = MockEvent.Companion.EdgeCaseCatchyDescription.EMPTY.value)
    setEventScreen(event)
    composeTestRule
        .onNodeWithTag(EventCardTestTags.EVENT_CATCHY_DESCRIPTION, useUnmergedTree = true)
        .assertTextEquals("") // Expect empty catchy description
  }

  @Test
  fun testEventCardWithSpecialCharactersCatchyDescription() {
    val event =
        MockEvent.createMockEvent(
            catchyDescription =
                MockEvent.Companion.EdgeCaseCatchyDescription.SPECIAL_CHARACTERS.value)
    setEventScreen(event)
    composeTestRule
        .onNodeWithTag(EventCardTestTags.EVENT_CATCHY_DESCRIPTION, useUnmergedTree = true)
        .assertTextEquals(MockEvent.Companion.EdgeCaseCatchyDescription.SPECIAL_CHARACTERS.value)
  }

  @Test
  fun testEventCardWithPastStartAndEndDate() {
    val event =
        MockEvent.createMockEvent(
            startDate = MockEvent.Companion.EdgeCaseDate.PAST.value,
            endDate = MockEvent.Companion.EdgeCaseDate.PAST.value)
    setEventScreen(event)
    composeTestRule
        .onNodeWithTag(EventCardTestTags.EVENT_DATE, useUnmergedTree = true)
        .assertExists()
  }

  @Test
  fun testEventCardWithTodayStartDate() {
    val event = MockEvent.createMockEvent(startDate = MockEvent.Companion.EdgeCaseDate.TODAY.value)
    setEventScreen(event)
    composeTestRule
        .onNodeWithTag(EventCardTestTags.EVENT_DATE, useUnmergedTree = true)
        .assertExists()
  }

  @Test
  fun testEventCardWithFutureStartDate() {
    val event = MockEvent.createMockEvent(startDate = MockEvent.Companion.EdgeCaseDate.FUTURE.value)
    setEventScreen(event)
    composeTestRule
        .onNodeWithTag(EventCardTestTags.EVENT_DATE, useUnmergedTree = true)
        .assertExists()
  }
}
