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
import com.android.unio.model.association.AssociationRepositoryFirestore
import com.android.unio.model.association.AssociationViewModel
import com.android.unio.model.event.Event
import com.android.unio.model.event.EventRepositoryFirestore
import com.android.unio.model.event.EventViewModel
import com.android.unio.model.image.ImageRepositoryFirebaseStorage
import com.android.unio.model.user.UserViewModel
import com.android.unio.ui.navigation.NavigationAction
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import io.mockk.MockKAnnotations
import io.mockk.every
import io.mockk.mockk
import io.mockk.spyk
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.Mockito.mock
import org.mockito.Mockito.`when`
import org.mockito.kotlin.verify
import javax.inject.Inject

@HiltAndroidTest
class AssociationProfileTest {
    @get:Rule
    val composeTestRule = createComposeRule()
    @get:Rule
    val hiltRule = HiltAndroidRule(this)
    lateinit var navigationAction: NavigationAction

    private lateinit var associationRepository: AssociationRepositoryFirestore

    @Inject
    lateinit var eventRepository: EventRepositoryFirestore
    private lateinit var userViewModel: UserViewModel

    private lateinit var associationViewModel: AssociationViewModel
    private lateinit var eventViewModel: EventViewModel

    private lateinit var associations: List<Association>
    private lateinit var events: List<Event>

    @Inject
    lateinit var imageRepository: ImageRepositoryFirebaseStorage


    @Before
    fun setUp() {
        MockKAnnotations.init(this)
        hiltRule.inject()

        associations =
            listOf(
                MockAssociation.createMockAssociation(uid = "1"),
                MockAssociation.createMockAssociation(uid = "2")
            )

        events = listOf(MockEvent.createMockEvent(uid = "a"), MockEvent.createMockEvent(uid = "b"))

        navigationAction = NavigationAction(mock(NavHostController::class.java))

        associationRepository = spyk(AssociationRepositoryFirestore(mockk()))

        every { associationRepository.getAssociations(any(), any()) } answers
                {
                    val onSuccess = args[0] as (List<Association>) -> Unit
                    onSuccess(associations)
                }

        every { eventRepository.getEventsOfAssociation(any(), any(), any()) } answers
                {
                    val onSuccess = args[1] as (List<Event>) -> Unit
                    onSuccess(events)
                }

        every { userViewModel.isEventSavedForCurrentUser(any()) } answers
                {
                    events.map { it.uid }.contains(args[0])
                }

        associationViewModel =
            AssociationViewModel(associationRepository, eventRepository, imageRepository)
        associationViewModel.getAssociations()
        eventViewModel = EventViewModel(eventRepository, imageRepository)
    }

    @Test
    fun testAssociationProfileDisplayComponent() {
        composeTestRule.setContent {
            AssociationProfileScreen(
                navigationAction,
                "1",
                associationViewModel,
                userViewModel,
                eventViewModel
            )
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
        if (events.isNotEmpty()) {
            assertDisplayComponentInScroll(
                composeTestRule.onNodeWithTag(
                    "AssociationEventCard-" + events.sortedBy { it.date }[0].uid
                )
            )
        }
        assertDisplayComponentInScroll(composeTestRule.onNodeWithTag("AssociationSeeMoreButton"))
        assertDisplayComponentInScroll(composeTestRule.onNodeWithTag("AssociationContactMembersTitle"))
        assertDisplayComponentInScroll(
            composeTestRule.onNodeWithTag("AssociationRecruitmentDescription")
        )
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
            AssociationProfileScreen(
                navigationAction,
                "1",
                associationViewModel,
                userViewModel,
                eventViewModel
            )
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
            AssociationProfileScreen(
                navigationAction,
                "",
                associationViewModel,
                userViewModel,
                eventViewModel
            )
        }

        `when`(navigationAction.navController.popBackStack()).thenReturn(true)

        composeTestRule.onNodeWithTag("goBackButton").performClick()

        verify(navigationAction.navController).popBackStack()
    }

    @Test
    fun testAssociationProfileGoodId() {
        composeTestRule.setContent {
            AssociationProfileScreen(
                navigationAction,
                "1",
                associationViewModel,
                userViewModel,
                eventViewModel
            )
        }

        assertDisplayComponentInScroll(composeTestRule.onNodeWithTag("AssociationProfileTitle"))
        assertDisplayComponentInScroll(composeTestRule.onNodeWithText(this.associations.first().name))
    }

    @Test
    fun testAssociationProfileBadId() {
        composeTestRule.setContent {
            AssociationProfileScreen(
                navigationAction,
                "IDONOTEXIST",
                associationViewModel,
                userViewModel,
                eventViewModel
            )
        }

    associationViewModel =
        AssociationViewModel(associationRepository, eventRepository, imageRepository)
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

    @Test
    fun testAssociationProfileEmptyUid() {
        composeTestRule.setContent {
            AssociationProfileScreen(
                navigationAction,
                "",
                associationViewModel,
                userViewModel,
                eventViewModel
            )
        }

    composeTestRule.onNodeWithTag("goBackButton").performClick()

    verify(navigationAction.navController).popBackStack()
  }

  @Test
  fun testAssociationProfileGoodId() {
    composeTestRule.setContent {
      AssociationProfileScaffold(
          MockAssociation.createMockAssociation(), navigationAction, userViewModel)
    }

    @Test
    fun testAssociationProfileSpecialCharacterUid() {
        composeTestRule.setContent {
            AssociationProfileScreen(
                navigationAction,
                MockAssociation.Companion.EdgeCaseUid.SPECIAL_CHARACTERS.value,
                associationViewModel,
                userViewModel,
                eventViewModel
            )
        }

  @Test
  fun testAssociationProfileNoId() {
    composeTestRule.setContent {
      AssociationProfileScreen(navigationAction, associationViewModel, userViewModel)
    }

    composeTestRule.onNodeWithTag("AssociationScreen").assertIsNotDisplayed()
  }
}
