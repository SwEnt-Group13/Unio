package com.android.unio.ui.association

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
import com.android.unio.mocks.association.MockAssociation
import com.android.unio.mocks.event.MockEvent
import com.android.unio.model.association.Association
import com.android.unio.model.association.AssociationRepository
import com.android.unio.model.association.AssociationViewModel
import com.android.unio.model.event.Event
import com.android.unio.model.event.EventRepository
import com.android.unio.model.image.ImageRepository
import com.android.unio.model.user.UserViewModel
import com.android.unio.ui.navigation.NavigationAction
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.Mock
import org.mockito.Mockito.mock
import org.mockito.Mockito.`when`
import org.mockito.MockitoAnnotations
import org.mockito.kotlin.any
import org.mockito.kotlin.verify

@HiltAndroidTest
class AssociationProfileTest {

  lateinit var navigationAction: NavigationAction

  @Mock lateinit var associationRepository: AssociationRepository
  @Mock lateinit var eventRepository: EventRepository
  @Mock lateinit var userViewModel: UserViewModel

  private lateinit var associationViewModel: AssociationViewModel

  private lateinit var associations: List<Association>
  private lateinit var events: List<Event>

  @Mock lateinit var imageRepository: ImageRepository

  @get:Rule val composeTestRule = createComposeRule()
  @get:Rule val hiltRule = HiltAndroidRule(this)

  @Before
  fun setUp() {
    MockitoAnnotations.openMocks(this)
    hiltRule.inject()

    associations =
        listOf(
            MockAssociation.createMockAssociation(uid = "1"),
            MockAssociation.createMockAssociation(uid = "2"))

    events = listOf(MockEvent.createMockEvent(uid = "a"), MockEvent.createMockEvent(uid = "b"))

    navigationAction = NavigationAction(mock(NavHostController::class.java))

    `when`(associationRepository.getAssociations(any(), any())).thenAnswer { invocation ->
      val onSuccess = invocation.arguments[0] as (List<Association>) -> Unit
      onSuccess(associations)
    }
    `when`(eventRepository.getEventsOfAssociation(any(), any(), any())).thenAnswer { invocation ->
      val onSuccess = invocation.arguments[1] as (List<Event>) -> Unit
      onSuccess(events)
    }

    associationViewModel =
        AssociationViewModel(associationRepository, eventRepository, imageRepository)
    associationViewModel.getAssociations()
  }

  @Test
  fun testAssociationProfileDisplayComponent() {
    composeTestRule.setContent {
      AssociationProfileScreen(navigationAction, "1", associationViewModel, userViewModel)
    }
    composeTestRule.waitForIdle()

    assertDisplayComponentInScroll(composeTestRule.onNodeWithTag("AssociationScreen"))
    assertDisplayComponentInScroll(composeTestRule.onNodeWithTag("goBackButton"))

    assertDisplayComponentInScroll(composeTestRule.onNodeWithTag("AssociationImageHeader"))
    assertDisplayComponentInScroll(composeTestRule.onNodeWithTag("AssociationProfileTitle"))
    assertDisplayComponentInScroll(composeTestRule.onNodeWithTag("associationShareButton"))
    assertDisplayComponentInScroll(composeTestRule.onNodeWithTag("AssociationHeaderFollowers"))
    assertDisplayComponentInScroll(composeTestRule.onNodeWithTag("AssociationHeaderMembers"))
    assertDisplayComponentInScroll(composeTestRule.onNodeWithTag("AssociationFollowButton"))
    assertDisplayComponentInScroll(composeTestRule.onNodeWithTag("AssociationDescription"))
    assertDisplayComponentInScroll(composeTestRule.onNodeWithTag("AssociationEventTitle"))
    assertDisplayComponentInScroll(composeTestRule.onNodeWithTag("AssociationEventCard-a"))
    assertDisplayComponentInScroll(composeTestRule.onNodeWithTag("AssociationSeeMoreButton"))
    assertDisplayComponentInScroll(composeTestRule.onNodeWithTag("AssociationContactMembersTitle"))
    assertDisplayComponentInScroll(
        composeTestRule.onNodeWithTag("AssociationRecruitmentDescription"))
    assertDisplayComponentInScroll(composeTestRule.onNodeWithTag("AssociationRecruitmentRoles"))
  }

  private fun assertDisplayComponentInScroll(compose: SemanticsNodeInteraction) {
    if (compose.isNotDisplayed()) {
      compose.performScrollTo()
    }
    compose.assertIsDisplayed()
  }

  @Test
  fun testButtonBehavior() {
    composeTestRule.setContent {
      AssociationProfileScreen(navigationAction, "1", associationViewModel, userViewModel)
    }
    // Share button
    assertDisplayComponentInScroll(composeTestRule.onNodeWithTag("associationShareButton"))
    composeTestRule.onNodeWithTag("associationShareButton").performClick()
    assertSnackBarIsDisplayed()

    // Follow button
    assertDisplayComponentInScroll(composeTestRule.onNodeWithTag("AssociationFollowButton"))
    composeTestRule.onNodeWithTag("AssociationFollowButton").performClick()
    assertSnackBarIsDisplayed()

    // See more button
    assertDisplayComponentInScroll(composeTestRule.onNodeWithTag("AssociationSeeMoreButton"))
    composeTestRule.onNodeWithTag("AssociationSeeMoreButton").performClick()
    assertDisplayComponentInScroll(composeTestRule.onNodeWithTag("AssociationEventCard-b"))

    // Roles buttons
    assertDisplayComponentInScroll(composeTestRule.onNodeWithTag("AssociationTreasurerRoles"))
    composeTestRule.onNodeWithTag("AssociationTreasurerRoles").performClick()
    assertSnackBarIsDisplayed()
    assertDisplayComponentInScroll(composeTestRule.onNodeWithTag("AssociationDesignerRoles"))
    composeTestRule.onNodeWithTag("AssociationDesignerRoles").performClick()
    assertSnackBarIsDisplayed()
  }

  private fun assertSnackBarIsDisplayed() {
    composeTestRule.onNodeWithTag("associationSnackbarHost").assertIsDisplayed()
    composeTestRule.onNodeWithTag("snackbarActionButton").performClick()
    composeTestRule.onNodeWithTag("associationSnackbarHost").assertIsNotDisplayed()
  }

  @Test
  fun testGoBackButton() {
    composeTestRule.setContent {
      AssociationProfileScreen(navigationAction, "", associationViewModel, userViewModel)
    }

    `when`(navigationAction.navController.popBackStack()).thenReturn(true)

    composeTestRule.onNodeWithTag("goBackButton").performClick()

    verify(navigationAction.navController).popBackStack()
  }

  @Test
  fun testAssociationProfileGoodId() {
    composeTestRule.setContent {
      AssociationProfileScreen(navigationAction, "1", associationViewModel, userViewModel)
    }

    assertDisplayComponentInScroll(composeTestRule.onNodeWithTag("AssociationProfileTitle"))
    assertDisplayComponentInScroll(composeTestRule.onNodeWithText(this.associations.first().name))
  }

  @Test
  fun testAssociationProfileBadId() {
    composeTestRule.setContent {
      AssociationProfileScreen(navigationAction, "IDONOTEXIST", associationViewModel, userViewModel)
    }

    assertDisplayComponentInScroll(composeTestRule.onNodeWithTag("associationNotFound"))
  }

  @Test
  fun testAssociationProfileEmptyUid() {
    composeTestRule.setContent {
      AssociationProfileScreen(navigationAction, "", associationViewModel, userViewModel)
    }

    assertDisplayComponentInScroll(composeTestRule.onNodeWithTag("associationNotFound"))
  }

  @Test
  fun testAssociationProfileSpecialCharacterUid() {
    composeTestRule.setContent {
      AssociationProfileScreen(
          navigationAction,
          MockAssociation.Companion.EdgeCaseUid.SPECIAL_CHARACTERS.value,
          associationViewModel,
          userViewModel)
    }

    assertDisplayComponentInScroll(composeTestRule.onNodeWithTag("AssociationProfileTitle"))
  }

  @Test
  fun testAssociationProfileLongUid() {
    composeTestRule.setContent {
      AssociationProfileScreen(
          navigationAction,
          MockAssociation.Companion.EdgeCaseUid.LONG.value,
          associationViewModel,
          userViewModel)
    }

    assertDisplayComponentInScroll(composeTestRule.onNodeWithTag("AssociationProfileTitle"))
  }

  @Test
  fun testAssociationProfileTypicalUid() {
    composeTestRule.setContent {
      AssociationProfileScreen(
          navigationAction,
          MockAssociation.Companion.EdgeCaseUid.TYPICAL.value,
          associationViewModel,
          userViewModel)
    }

    assertDisplayComponentInScroll(composeTestRule.onNodeWithTag("AssociationProfileTitle"))
  }
}
