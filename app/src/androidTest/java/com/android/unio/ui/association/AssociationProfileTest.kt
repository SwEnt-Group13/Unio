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
import androidx.preference.PreferenceManager
import androidx.test.platform.app.InstrumentationRegistry
import com.android.unio.mocks.association.MockAssociation
import com.android.unio.mocks.event.MockEvent
import com.android.unio.mocks.user.MockUser
import com.android.unio.model.association.Association
import com.android.unio.model.association.AssociationRepositoryFirestore
import com.android.unio.model.association.AssociationViewModel
import com.android.unio.model.event.Event
import com.android.unio.model.event.EventRepositoryFirestore
import com.android.unio.model.event.EventViewModel
import com.android.unio.model.image.ImageRepositoryFirebaseStorage
import com.android.unio.model.user.UserRepositoryFirestore
import com.android.unio.model.user.UserViewModel
import com.android.unio.ui.navigation.NavigationAction
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import io.mockk.MockKAnnotations
import io.mockk.clearAllMocks
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.just
import io.mockk.mockk
import io.mockk.runs
import io.mockk.spyk
import io.mockk.unmockkAll
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.Mockito.mock
import org.mockito.Mockito.`when`
import org.mockito.kotlin.verify

@HiltAndroidTest
class AssociationProfileTest {

  lateinit var navigationAction: NavigationAction

  private lateinit var associationRepository: AssociationRepositoryFirestore

  @MockK lateinit var eventRepository: EventRepositoryFirestore
  private lateinit var eventViewModel: EventViewModel

  @MockK lateinit var userRepository: UserRepositoryFirestore
  private lateinit var userViewModel: UserViewModel

  private lateinit var associationViewModel: AssociationViewModel

  private lateinit var associations: List<Association>
  private lateinit var events: List<Event>

  @MockK lateinit var imageRepository: ImageRepositoryFirebaseStorage

  @get:Rule val composeTestRule = createComposeRule()
  @get:Rule val hiltRule = HiltAndroidRule(this)

  @Before
  fun setUp() {
    MockKAnnotations.init(this)
    hiltRule.inject()

    associations =
        listOf(
            MockAssociation.createMockAssociation(uid = "1"),
            MockAssociation.createMockAssociation(uid = "2"))

    events = listOf(MockEvent.createMockEvent(uid = "a"), MockEvent.createMockEvent(uid = "b"))

    navigationAction = NavigationAction(mock(NavHostController::class.java))

    associationRepository = spyk(AssociationRepositoryFirestore(mockk()))

    every { eventRepository.init(any()) } just runs

    every { eventRepository.getEvents(any(), any()) } answers
        {
          val onSuccess = args[0] as (List<Event>) -> Unit
          onSuccess(events)
        }
    eventViewModel = EventViewModel(eventRepository, imageRepository)
    eventViewModel.loadEvents()
    eventViewModel.selectEvent(events.first().uid)

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
    userViewModel = UserViewModel(userRepository)
    val user = MockUser.createMockUser()
    every { userRepository.updateUser(user, any(), any()) } answers
        {
          val onSuccess = args[1] as () -> Unit
          onSuccess()
        }
    userViewModel.addUser(user, {})

    associationViewModel =
        AssociationViewModel(associationRepository, eventRepository, imageRepository)
    associationViewModel.getAssociations()
  }

  @Test
  fun testAssociationProfileDisplayComponent() {
    composeTestRule.setContent {
      AssociationProfileScaffold(
          MockAssociation.createMockAssociation(),
          navigationAction,
          userViewModel,
          eventViewModel) {}
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
          MockAssociation.createMockAssociation(),
          navigationAction,
          userViewModel,
          eventViewModel) {}
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
          MockAssociation.createMockAssociation(),
          navigationAction,
          userViewModel,
          eventViewModel) {}
    }

    `when`(navigationAction.navController.popBackStack()).thenReturn(true)

    composeTestRule.onNodeWithTag("goBackButton").performClick()

    verify(navigationAction.navController).popBackStack()
  }

  @Test
  fun testAssociationProfileGoodId() {
    composeTestRule.setContent {
      AssociationProfileScaffold(
          MockAssociation.createMockAssociation(),
          navigationAction,
          userViewModel,
          eventViewModel) {}
    }

    assertDisplayComponentInScroll(composeTestRule.onNodeWithTag("AssociationProfileTitle"))
    assertDisplayComponentInScroll(composeTestRule.onNodeWithText(this.associations.first().name))
  }

  @Test
  fun testAssociationProfileNoId() {
    composeTestRule.setContent {
      AssociationProfileScreen(
          navigationAction, associationViewModel, userViewModel, eventViewModel)
    }

    composeTestRule.onNodeWithTag("AssociationScreen").assertIsNotDisplayed()
  }

    @After
    fun tearDown() {
        clearAllMocks()
        unmockkAll()
    }
}
