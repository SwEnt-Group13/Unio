package com.android.unio.ui.event

import androidx.compose.ui.test.SemanticsNodeInteraction
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsNotDisplayed
import androidx.compose.ui.test.isNotDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.navigation.NavHostController
import com.android.unio.mocks.event.MockEvent
import com.android.unio.model.event.Event
import com.android.unio.ui.navigation.NavigationAction
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.MockitoAnnotations
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify

class EventDetailsTest {
  private lateinit var navHostController: NavHostController
  private lateinit var navigationAction: NavigationAction

  private lateinit var events: List<Event>

  @get:Rule val composeTestRule = createComposeRule()

  @Before
  fun setUp() {
    MockitoAnnotations.openMocks(this)
    events = listOf(MockEvent.createMockEvent(uid = "a"), MockEvent.createMockEvent(uid = "b"))

    navHostController = mock { NavHostController::class.java }
    navigationAction = NavigationAction(navHostController)
  }

  private fun setEventScreen() {

    composeTestRule.setContent { EventScreenScaffold(navigationAction, events[0], true) {} }
  }

  @Test
  fun testEventDetailsDisplayComponent() {
    setEventScreen()
    composeTestRule.waitForIdle()

    assertDisplayComponentInScroll(composeTestRule.onNodeWithTag("EventScreen", true))
    assertDisplayComponentInScroll(composeTestRule.onNodeWithTag("goBackButton"))

    assertDisplayComponentInScroll(composeTestRule.onNodeWithTag("eventSaveButton"))
    assertDisplayComponentInScroll(composeTestRule.onNodeWithTag("eventShareButton"))
    assertDisplayComponentInScroll(composeTestRule.onNodeWithTag("eventDetailsPage"))
    assertDisplayComponentInScroll(composeTestRule.onNodeWithTag("eventDetailsInformationCard"))
    assertDisplayComponentInScroll(composeTestRule.onNodeWithTag("eventTitle"))
    assertDisplayComponentInScroll(composeTestRule.onNodeWithTag("eventOrganisingAssociation0"))
    assertDisplayComponentInScroll(composeTestRule.onNodeWithTag("eventOrganisingAssociation1"))
    assertDisplayComponentInScroll(composeTestRule.onNodeWithTag("associationLogo0"))
    assertDisplayComponentInScroll(composeTestRule.onNodeWithTag("associationName0"))
    assertDisplayComponentInScroll(composeTestRule.onNodeWithTag("associationLogo1"))
    assertDisplayComponentInScroll(composeTestRule.onNodeWithTag("associationName1"))
    assertDisplayComponentInScroll(composeTestRule.onNodeWithTag("eventStartHour"))
    assertDisplayComponentInScroll(composeTestRule.onNodeWithTag("eventDate"))
    assertDisplayComponentInScroll(composeTestRule.onNodeWithTag("eventDetailsBody"))
    assertDisplayComponentInScroll(composeTestRule.onNodeWithTag("placesRemainingText"))
    assertDisplayComponentInScroll(composeTestRule.onNodeWithTag("eventDescription"))
    assertDisplayComponentInScroll(composeTestRule.onNodeWithTag("eventLocation"))
    assertDisplayComponentInScroll(composeTestRule.onNodeWithTag("mapButton"))
    assertDisplayComponentInScroll(composeTestRule.onNodeWithTag("signUpButton"))
  }

  private fun assertDisplayComponentInScroll(compose: SemanticsNodeInteraction) {
    if (compose.isNotDisplayed()) {
      compose.performScrollTo()
    }
    compose.assertIsDisplayed()
  }

  @Test
  fun testButtonBehavior() {
    setEventScreen()
    // Share button
    assertDisplayComponentInScroll(composeTestRule.onNodeWithTag("eventShareButton"))
    composeTestRule.onNodeWithTag("eventShareButton").performClick()
    assertSnackBarIsDisplayed()

    // Save button
    assertDisplayComponentInScroll(composeTestRule.onNodeWithTag("eventSaveButton"))
    composeTestRule.onNodeWithTag("eventSaveButton").performClick()

    // Location button
    assertDisplayComponentInScroll(composeTestRule.onNodeWithTag("mapButton"))
    composeTestRule.onNodeWithTag("mapButton").performClick()
    assertSnackBarIsDisplayed()

    // Sign-up button
    assertDisplayComponentInScroll(composeTestRule.onNodeWithTag("signUpButton"))
    composeTestRule.onNodeWithTag("signUpButton").performClick()
    assertSnackBarIsDisplayed()
  }

  private fun assertSnackBarIsDisplayed() {
    composeTestRule.onNodeWithTag("eventSnackbarHost").assertIsDisplayed()
    composeTestRule.onNodeWithTag("snackbarActionButton").performClick()
    composeTestRule.onNodeWithTag("eventSnackbarHost").assertIsNotDisplayed()
  }

  @Test
  fun testGoBackButton() {
    setEventScreen()
    composeTestRule.onNodeWithTag("goBackButton").performClick()
    verify(navHostController).popBackStack()
  }

  @Test
  fun testEventDetailsData() {
    val event = events[0]
    setEventScreen()
    assertDisplayComponentInScroll(composeTestRule.onNodeWithText(event.title))
    assertDisplayComponentInScroll(composeTestRule.onNodeWithText(event.description))
    assertDisplayComponentInScroll(composeTestRule.onNodeWithText(event.location.name))
  }
}
