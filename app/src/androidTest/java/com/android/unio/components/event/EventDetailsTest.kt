package com.android.unio.components.event

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsNotDisplayed
import androidx.compose.ui.test.assertTextEquals
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.navigation.NavHostController
import com.android.unio.TearDown
import com.android.unio.assertDisplayComponentInScroll
import com.android.unio.mocks.association.MockAssociation
import com.android.unio.mocks.event.MockEvent
import com.android.unio.model.association.Association
import com.android.unio.model.event.Event
import com.android.unio.model.event.EventUtils.formatTimestamp
import com.android.unio.model.map.MapViewModel
import com.android.unio.model.strings.FormatStrings.DAY_MONTH_FORMAT
import com.android.unio.model.strings.test_tags.EventDetailsTestTags
import com.android.unio.ui.event.EventScreenScaffold
import com.android.unio.ui.navigation.NavigationAction
import com.android.unio.ui.navigation.Screen
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.firebase.Timestamp
import io.mockk.MockKAnnotations
import io.mockk.impl.annotations.MockK
import io.mockk.verify
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.Mockito.mock

class EventDetailsTest : TearDown() {
  @MockK private lateinit var navHostController: NavHostController
  private lateinit var navigationAction: NavigationAction

  private lateinit var events: List<Event>
  private lateinit var associations: List<Association>

  private lateinit var fusedLocationProviderClient: FusedLocationProviderClient
  private lateinit var mapViewModel: MapViewModel

  @get:Rule val composeTestRule = createComposeRule()

  @Before
  fun setUp() {
    MockKAnnotations.init(this, relaxed = true)
    events =
        listOf(
            MockEvent.createMockEvent(
                uid = "a",
                startDate = Timestamp(Date(2024 - 1900, 6, 20)),
                endDate = Timestamp(Date(2024 - 1900, 6, 21))),
            MockEvent.createMockEvent(
                uid = "b",
                startDate = Timestamp(Date(2025 - 1900, 6, 20)),
                endDate = Timestamp(Date(2025 - 1900, 6, 20))))
    associations =
        listOf(
            MockAssociation.createMockAssociation(uid = "c"),
            MockAssociation.createMockAssociation(uid = "d"))

    navigationAction = NavigationAction(navHostController)
    fusedLocationProviderClient = mock()
    mapViewModel = MapViewModel(fusedLocationProviderClient)
  }

  private fun setEventScreen(event: Event) {

    composeTestRule.setContent {
      EventScreenScaffold(navigationAction, mapViewModel, event, associations, true) {}
    }
  }

  @Test
  fun testEventDetailsDisplayComponent() {
    val event = events[1]
    setEventScreen(event)
    composeTestRule.waitForIdle()

    val formattedStartDateDay =
        formatTimestamp(event.startDate, SimpleDateFormat(DAY_MONTH_FORMAT, Locale.getDefault()))
    val formattedEndDateDay =
        formatTimestamp(event.endDate, SimpleDateFormat(DAY_MONTH_FORMAT, Locale.getDefault()))

    composeTestRule
        .onNodeWithTag(EventDetailsTestTags.SCREEN, true)
        .assertDisplayComponentInScroll()
    composeTestRule
        .onNodeWithTag(EventDetailsTestTags.GO_BACK_BUTTON)
        .assertDisplayComponentInScroll()

    composeTestRule.onNodeWithTag(EventDetailsTestTags.SAVE_BUTTON).assertDisplayComponentInScroll()
    composeTestRule
        .onNodeWithTag(EventDetailsTestTags.SHARE_BUTTON)
        .assertDisplayComponentInScroll()
    composeTestRule
        .onNodeWithTag(EventDetailsTestTags.DETAILS_PAGE)
        .assertDisplayComponentInScroll()

    composeTestRule
        .onNodeWithTag(EventDetailsTestTags.DETAILS_INFORMATION_CARD)
        .assertDisplayComponentInScroll()
    composeTestRule.onNodeWithTag(EventDetailsTestTags.TITLE).assertDisplayComponentInScroll()

    composeTestRule
        .onNodeWithTag("${EventDetailsTestTags.ORGANIZING_ASSOCIATION}0")
        .assertDisplayComponentInScroll()

    composeTestRule
        .onNodeWithTag("${EventDetailsTestTags.ORGANIZING_ASSOCIATION}1")
        .assertDisplayComponentInScroll()

    composeTestRule
        .onNodeWithTag("${EventDetailsTestTags.ASSOCIATION_LOGO}0")
        .assertDisplayComponentInScroll()

    composeTestRule
        .onNodeWithTag("${EventDetailsTestTags.ASSOCIATION_NAME}0")
        .assertDisplayComponentInScroll()

    composeTestRule
        .onNodeWithTag("${EventDetailsTestTags.ASSOCIATION_LOGO}1")
        .assertDisplayComponentInScroll()

    composeTestRule
        .onNodeWithTag("${EventDetailsTestTags.ASSOCIATION_NAME}1")
        .assertDisplayComponentInScroll()

    if (formattedStartDateDay == formattedEndDateDay) {
      composeTestRule.onNodeWithTag(EventDetailsTestTags.HOUR).assertDisplayComponentInScroll()
      composeTestRule
          .onNodeWithTag(EventDetailsTestTags.START_DATE)
          .assertDisplayComponentInScroll()
    } else {
      composeTestRule
          .onNodeWithTag(EventDetailsTestTags.START_DATE)
          .assertDisplayComponentInScroll()
      composeTestRule.onNodeWithTag(EventDetailsTestTags.END_DATE).assertDisplayComponentInScroll()
    }

    composeTestRule
        .onNodeWithTag(EventDetailsTestTags.DETAILS_BODY)
        .assertDisplayComponentInScroll()

    composeTestRule.onNodeWithTag(EventDetailsTestTags.PLACES_REMAINING_TEXT).assertExists()
    composeTestRule.onNodeWithTag(EventDetailsTestTags.DESCRIPTION).assertDisplayComponentInScroll()
    composeTestRule
        .onNodeWithTag(EventDetailsTestTags.LOCATION_ADDRESS, true)
        .assertTextEquals(event.location.name)
        .assertDisplayComponentInScroll()
    composeTestRule.onNodeWithTag(EventDetailsTestTags.MAP_BUTTON).assertDisplayComponentInScroll()
  }

  @Test
  fun testButtonBehavior() {
    setEventScreen(events[0])
    // Share button
    composeTestRule
        .onNodeWithTag(EventDetailsTestTags.SHARE_BUTTON)
        .assertDisplayComponentInScroll()
    composeTestRule.onNodeWithTag(EventDetailsTestTags.SHARE_BUTTON).performClick()
    assertSnackBarIsDisplayed()

    // Save button
    composeTestRule.onNodeWithTag(EventDetailsTestTags.SAVE_BUTTON).assertDisplayComponentInScroll()
    composeTestRule.onNodeWithTag(EventDetailsTestTags.SAVE_BUTTON).performClick()

    // Location button
    composeTestRule.onNodeWithTag(EventDetailsTestTags.MAP_BUTTON).assertDisplayComponentInScroll()
    composeTestRule.onNodeWithTag(EventDetailsTestTags.MAP_BUTTON).performClick()
    verify { navigationAction.navigateTo(Screen.MAP) }
    assert(mapViewModel.highlightedEventUid.value == events[0].uid)
    assert(mapViewModel.centerLocation.value!!.latitude == events[0].location.latitude)
    assert(mapViewModel.centerLocation.value!!.longitude == events[0].location.longitude)
  }

  private fun assertSnackBarIsDisplayed() {
    composeTestRule.onNodeWithTag(EventDetailsTestTags.SNACKBAR_HOST).assertIsDisplayed()
    composeTestRule.onNodeWithTag(EventDetailsTestTags.SNACKBAR_ACTION_BUTTON).performClick()
    composeTestRule.onNodeWithTag(EventDetailsTestTags.SNACKBAR_HOST).assertIsNotDisplayed()
  }

  @Test
  fun testGoBackButton() {
    setEventScreen(events[0])
    composeTestRule.onNodeWithTag(EventDetailsTestTags.GO_BACK_BUTTON).performClick()
    verify { navigationAction.goBack() }
  }

  @Test
  fun testEventDetailsData() {
    val event = events[1]
    setEventScreen(event)
    composeTestRule.onNodeWithText(event.title).assertDisplayComponentInScroll()
    composeTestRule.onNodeWithText(event.description).assertDisplayComponentInScroll()
    composeTestRule.onNodeWithText(event.location.name).assertDisplayComponentInScroll()
    composeTestRule.onNodeWithTag(EventDetailsTestTags.START_DATE).assertDisplayComponentInScroll()
  }
}
