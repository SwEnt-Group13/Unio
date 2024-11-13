package com.android.unio.ui.association

import android.content.Context
import androidx.compose.ui.platform.LocalContext
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
import com.android.unio.R
import com.android.unio.mocks.association.MockAssociation
import com.android.unio.mocks.event.MockEvent
import com.android.unio.model.association.Association
import com.android.unio.model.association.AssociationRepositoryFirestore
import com.android.unio.model.association.AssociationViewModel
import com.android.unio.model.event.Event
import com.android.unio.model.event.EventRepositoryFirestore
import com.android.unio.model.event.EventViewModel
import com.android.unio.model.firestore.emptyFirestoreReferenceList
import com.android.unio.model.follow.ConcurrentAssociationUserRepositoryFirestore
import com.android.unio.model.image.ImageRepositoryFirebaseStorage
import com.android.unio.model.strings.test_tags.AssociationProfileTestTags
import com.android.unio.model.user.User
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

  private lateinit var navigationAction: NavigationAction

  private lateinit var associationRepository: AssociationRepositoryFirestore

  @MockK private lateinit var eventRepository: EventRepositoryFirestore
  @MockK private lateinit var concurrentAssociationUserRepository: ConcurrentAssociationUserRepositoryFirestore

  private lateinit var eventViewModel: EventViewModel

  @MockK private lateinit var userRepository: UserRepositoryFirestore
  private lateinit var userViewModel: UserViewModel

  private lateinit var associationViewModel: AssociationViewModel

  private lateinit var associations: List<Association>
  private lateinit var events: List<Event>

  @MockK private lateinit var imageRepository: ImageRepositoryFirebaseStorage

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

      every{userRepository.init(any())} just runs


    every { concurrentAssociationUserRepository.updateFollow(any(), any(), any(), any()) } answers
        {
          val onSuccess = args[2] as () -> Unit
          onSuccess()
        }

      userViewModel = UserViewModel(userRepository)
    val user = User(
        uid = "1",
        email = "",
        firstName = "",
        lastName = "",
        biography = "",
        savedEvents = Event.emptyFirestoreReferenceList(),
        followedAssociations = Association.emptyFirestoreReferenceList(),
        joinedAssociations = Association.emptyFirestoreReferenceList(),
        interests = emptyList(),
        socials = emptyList(),
        profilePicture = "",
    )

      every { userRepository.getUserWithId(any(), any(), any()) } answers
              {
                  val onSuccess = args[1] as (User) -> Unit
                  onSuccess(user)
              }
    every { userRepository.updateUser(user, any(), any()) } answers
        {
          val onSuccess = args[1] as () -> Unit
          onSuccess()
        }
    userViewModel.addUser(user, {})

    associationViewModel =
        AssociationViewModel(
            associationRepository,
            eventRepository,
            imageRepository,
            concurrentAssociationUserRepository)
    associationViewModel.getAssociations()
    associationViewModel.selectAssociation(associations.first().uid)
  }

  @Test
  fun testAssociationProfileDisplayComponent() {
    composeTestRule.setContent {
      AssociationProfileScaffold(
          navigationAction, userViewModel, eventViewModel, associationViewModel) {}
    }
    composeTestRule.waitForIdle()

    assertDisplayComponentInScroll(composeTestRule.onNodeWithTag(AssociationProfileTestTags.SCREEN))
    assertDisplayComponentInScroll(
        composeTestRule.onNodeWithTag(AssociationProfileTestTags.GO_BACK_BUTTON))

    assertDisplayComponentInScroll(
        composeTestRule.onNodeWithTag(AssociationProfileTestTags.IMAGE_HEADER))
    assertDisplayComponentInScroll(composeTestRule.onNodeWithTag(AssociationProfileTestTags.TITLE))
    assertDisplayComponentInScroll(
        composeTestRule.onNodeWithTag(AssociationProfileTestTags.SHARE_BUTTON))
    assertDisplayComponentInScroll(
        composeTestRule.onNodeWithTag(AssociationProfileTestTags.HEADER_FOLLOWERS))
    assertDisplayComponentInScroll(
        composeTestRule.onNodeWithTag(AssociationProfileTestTags.HEADER_MEMBERS))
    assertDisplayComponentInScroll(
        composeTestRule.onNodeWithTag(AssociationProfileTestTags.FOLLOW_BUTTON))
    assertDisplayComponentInScroll(
        composeTestRule.onNodeWithTag(AssociationProfileTestTags.DESCRIPTION))
    assertDisplayComponentInScroll(
        composeTestRule.onNodeWithTag(AssociationProfileTestTags.EVENT_TITLE))
    assertDisplayComponentInScroll(
        composeTestRule.onNodeWithTag(AssociationProfileTestTags.CONTACT_MEMBERS_TITLE))
    assertDisplayComponentInScroll(
        composeTestRule.onNodeWithTag(AssociationProfileTestTags.RECRUITMENT_DESCRIPTION))
    assertDisplayComponentInScroll(
        composeTestRule.onNodeWithTag(AssociationProfileTestTags.RECRUITMENT_ROLES))
  }

  private fun assertDisplayComponentInScroll(compose: SemanticsNodeInteraction) {
    if (compose.isNotDisplayed()) {
      compose.performScrollTo()
    }
    compose.assertIsDisplayed()
  }

  @Test
  fun testFollowAssociation() {
    var context: Context? = null
    composeTestRule.setContent {
      context = LocalContext.current
      AssociationProfileScaffold(
          navigationAction, userViewModel, eventViewModel, associationViewModel) {}
    }
    val currentCount = associationViewModel.selectedAssociation.value!!.followersCount

    assertDisplayComponentInScroll(
        composeTestRule.onNodeWithTag(AssociationProfileTestTags.FOLLOW_BUTTON))
    composeTestRule
        .onNodeWithText(context!!.getString(R.string.association_follow))
        .assertIsDisplayed()

    // Follow operation
    composeTestRule.onNodeWithTag(AssociationProfileTestTags.FOLLOW_BUTTON).performClick()
    assert(userViewModel.user.value?.followedAssociations!!.contains(associations.first().uid))
    assert(associationViewModel.selectedAssociation.value!!.followersCount == currentCount + 1)
    composeTestRule
        .onNodeWithText(context!!.getString(R.string.association_unfollow))
        .assertIsDisplayed()
    composeTestRule.waitForIdle()
    // Unfollow operation
    composeTestRule.onNodeWithTag(AssociationProfileTestTags.FOLLOW_BUTTON).performClick()
    composeTestRule
        .onNodeWithText(context!!.getString(R.string.association_follow))
        .assertIsDisplayed()
    assert(!userViewModel.user.value?.followedAssociations!!.contains(associations.first().uid))
    assert(associationViewModel.selectedAssociation.value!!.followersCount == currentCount)
  }

  @Test
  fun testButtonBehavior() {
    composeTestRule.setContent {
      AssociationProfileScaffold(
          navigationAction, userViewModel, eventViewModel, associationViewModel) {}
    }
    // Share button
    assertDisplayComponentInScroll(
        composeTestRule.onNodeWithTag(AssociationProfileTestTags.SHARE_BUTTON))
    composeTestRule.onNodeWithTag(AssociationProfileTestTags.SHARE_BUTTON).performClick()
    assertSnackBarIsDisplayed()

    // Roles buttons
    assertDisplayComponentInScroll(
        composeTestRule.onNodeWithTag(AssociationProfileTestTags.TREASURER_ROLES))
    composeTestRule.onNodeWithTag(AssociationProfileTestTags.TREASURER_ROLES).performClick()
    assertSnackBarIsDisplayed()
    assertDisplayComponentInScroll(
        composeTestRule.onNodeWithTag(AssociationProfileTestTags.DESIGNER_ROLES))
    composeTestRule.onNodeWithTag(AssociationProfileTestTags.DESIGNER_ROLES).performClick()
    assertSnackBarIsDisplayed()
  }

  private fun assertSnackBarIsDisplayed() {
    composeTestRule.onNodeWithTag(AssociationProfileTestTags.SNACKBAR_HOST).assertIsDisplayed()
    composeTestRule.onNodeWithTag(AssociationProfileTestTags.SNACKBAR_ACTION_BUTTON).performClick()
    composeTestRule.onNodeWithTag(AssociationProfileTestTags.SNACKBAR_HOST).assertIsNotDisplayed()
  }

  @Test
  fun testGoBackButton() {
    composeTestRule.setContent {
      AssociationProfileScaffold(
          navigationAction, userViewModel, eventViewModel, associationViewModel) {}
    }

    `when`(navigationAction.navController.popBackStack()).thenReturn(true)

    composeTestRule.onNodeWithTag(AssociationProfileTestTags.GO_BACK_BUTTON).performClick()

    verify(navigationAction.navController).popBackStack()
  }

  @Test
  fun testAssociationProfileGoodId() {
    composeTestRule.setContent {
      AssociationProfileScaffold(
          navigationAction, userViewModel, eventViewModel, associationViewModel) {}
    }

    assertDisplayComponentInScroll(composeTestRule.onNodeWithTag(AssociationProfileTestTags.TITLE))
    assertDisplayComponentInScroll(composeTestRule.onNodeWithText(this.associations.first().name))
  }

  @Test
  fun testAssociationProfileNoId() {
    associationViewModel.selectAssociation("3")
    composeTestRule.setContent {
      AssociationProfileScreen(
          navigationAction, associationViewModel, userViewModel, eventViewModel)
    }

    composeTestRule.onNodeWithTag(AssociationProfileTestTags.SCREEN).assertIsNotDisplayed()
  }

    @After
    fun tearDown() {
        clearAllMocks()
        unmockkAll()
    }
}
