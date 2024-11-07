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
import com.android.unio.model.user.UserViewModel
import com.android.unio.ui.navigation.NavigationAction
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.FirebaseFirestore
import io.mockk.mockk
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.Mock
import org.mockito.Mockito.`when`
import org.mockito.MockitoAnnotations
import org.mockito.kotlin.any
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify

class AssociationProfileTest {
  private lateinit var navHostController: NavHostController
  private lateinit var navigationAction: NavigationAction
  @Mock private lateinit var collectionReference: CollectionReference
  @Mock private lateinit var db: FirebaseFirestore
  @Mock private lateinit var associationRepository: AssociationRepository
  @Mock private lateinit var eventRepository: EventRepository
  private lateinit var associationViewModel: AssociationViewModel

  private lateinit var userViewModel: UserViewModel

  private lateinit var associations: List<Association>
  private lateinit var events: List<Event>

  @get:Rule val composeTestRule = createComposeRule()

  @Before
  fun setUp() {
    MockitoAnnotations.openMocks(this)

    userViewModel = mockk(relaxed = true)

    associations =
        listOf(
            MockAssociation.createMockAssociation(uid = "1"),
            MockAssociation.createMockAssociation(uid = "2"))

    events = listOf(MockEvent.createMockEvent(uid = "a"), MockEvent.createMockEvent(uid = "b"))

    `when`(db.collection(any())).thenReturn(collectionReference)
    `when`(associationRepository.getAssociations(any(), any())).thenAnswer { invocation ->
      val onSuccess = invocation.arguments[0] as (List<Association>) -> Unit
      onSuccess(associations)
    }
    `when`(eventRepository.getEventsOfAssociation(any(), any(), any())).thenAnswer { invocation ->
      val onSuccess = invocation.arguments[1] as (List<Event>) -> Unit
      onSuccess(events)
    }

    navHostController = mock { NavHostController::class.java }
    navigationAction = NavigationAction(navHostController)

    associationViewModel = AssociationViewModel(associationRepository, eventRepository)
    associationViewModel.getAssociations()
  }

  @Test
  fun testAssociationProfileDisplayComponent() {
    composeTestRule.setContent {
      AssociationProfileScaffold(
          MockAssociation.createMockAssociation(), navigationAction, userViewModel)
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
      AssociationProfileScaffold(
          MockAssociation.createMockAssociation(), navigationAction, userViewModel)
    }
    // Share button
    assertDisplayComponentInScroll(composeTestRule.onNodeWithTag("associationShareButton"))
    composeTestRule.onNodeWithTag("associationShareButton").performClick()
    assertSnackBarIsDisplayed()

    // Follow button
    assertDisplayComponentInScroll(composeTestRule.onNodeWithTag("AssociationFollowButton"))
    composeTestRule.onNodeWithTag("AssociationFollowButton").performClick()
    assertSnackBarIsDisplayed()

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
      AssociationProfileScaffold(
          MockAssociation.createMockAssociation(), navigationAction, userViewModel)
    }

    composeTestRule.onNodeWithTag("goBackButton").performClick()

    verify(navHostController).popBackStack()
  }

  @Test
  fun testAssociationProfileGoodId() {
    composeTestRule.setContent {
      AssociationProfileScaffold(
          MockAssociation.createMockAssociation(), navigationAction, userViewModel)
    }

    assertDisplayComponentInScroll(composeTestRule.onNodeWithTag("AssociationProfileTitle"))
    assertDisplayComponentInScroll(composeTestRule.onNodeWithText(this.associations.first().name))
  }
}
