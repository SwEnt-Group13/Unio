package com.android.unio.components.saved

import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import com.android.unio.TearDown
import com.android.unio.assertDisplayComponentInScroll
import com.android.unio.mocks.association.MockAssociation
import com.android.unio.mocks.event.MockEvent
import com.android.unio.mocks.user.MockUser
import com.android.unio.model.association.AssociationRepositoryFirestore
import com.android.unio.model.event.Event
import com.android.unio.model.event.EventRepositoryFirestore
import com.android.unio.model.event.EventUserPictureRepositoryFirestore
import com.android.unio.model.event.EventViewModel
import com.android.unio.model.image.ImageRepositoryFirebaseStorage
import com.android.unio.model.strings.test_tags.saved.SavedTestTags
import com.android.unio.model.user.UserRepositoryFirestore
import com.android.unio.model.user.UserViewModel
import com.android.unio.ui.navigation.NavigationAction
import com.android.unio.ui.navigation.TopLevelDestination
import com.android.unio.ui.saved.SavedScreen
import com.google.firebase.Timestamp
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import io.mockk.MockKAnnotations
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.spyk
import me.zhanghai.compose.preference.ProvidePreferenceLocals
import org.junit.Before
import org.junit.Rule
import org.junit.Test

@HiltAndroidTest
class SavedTest : TearDown() {
  @get:Rule val composeTestRule = createComposeRule()

  @get:Rule val hiltRule = HiltAndroidRule(this)

  private lateinit var userViewModel: UserViewModel

  // Mock event repository to provide test data.
  @MockK private lateinit var eventRepository: EventRepositoryFirestore
  @MockK private lateinit var userRepository: UserRepositoryFirestore
  @MockK private lateinit var navigationAction: NavigationAction
  @MockK private lateinit var associationRepositoryFirestore: AssociationRepositoryFirestore
  @MockK private lateinit var imageRepository: ImageRepositoryFirebaseStorage
  @MockK
  private lateinit var eventUserPictureRepositoryFirestore: EventUserPictureRepositoryFirestore

  private lateinit var eventViewModel: EventViewModel

  private lateinit var eventList: List<Event>

  @Before
  fun setUp() {
    MockKAnnotations.init(this, relaxed = true)
    hiltRule.inject()

    val asso = MockAssociation.createMockAssociation()
    eventList =
        listOf(
            MockEvent.createMockEvent(organisers = listOf(asso)),
            MockEvent.createMockEvent(title = "I am different", startDate = Timestamp.now()))

    every { navigationAction.navigateTo(any(TopLevelDestination::class)) } returns Unit
    every { navigationAction.navigateTo(any(String::class)) } returns Unit

    every { userRepository.updateUser(any(), any(), any()) } answers
        {
          val onSuccess = args[1] as () -> Unit
          onSuccess()
        }
    every { userRepository.init(any()) } answers
        {
          val onSuccess = args[0] as () -> Unit
          onSuccess()
        }

    userViewModel = spyk(UserViewModel(userRepository, imageRepository))

    every { eventRepository.getEvents(any(), any()) } answers
        {
          val onSuccess = args[0] as (List<Event>) -> Unit
          onSuccess(eventList)
        }
    every { eventRepository.init(any()) } answers
        {
          val onSuccess = args[0] as () -> Unit
          onSuccess()
        }

    eventViewModel =
        EventViewModel(
            eventRepository,
            imageRepository,
            associationRepositoryFirestore,
            eventUserPictureRepositoryFirestore)
  }

  @Test
  fun testSavedScreenWithSavedEvents() {
    userViewModel.addUser(MockUser.createMockUser(savedEvents = eventList)) {}

    composeTestRule.setContent {
      ProvidePreferenceLocals { SavedScreen(navigationAction, eventViewModel, userViewModel) }
    }

    composeTestRule.waitForIdle()

    composeTestRule.onNodeWithTag(SavedTestTags.TITLE).assertDisplayComponentInScroll()
    composeTestRule.onNodeWithTag(SavedTestTags.FAB).assertDisplayComponentInScroll()
    composeTestRule.onNodeWithTag(SavedTestTags.TODAY).assertDisplayComponentInScroll()
    composeTestRule.onNodeWithTag(SavedTestTags.UPCOMING).assertDisplayComponentInScroll()
  }

  @Test
  fun testSavedScreenWithNoSavedEvents() {
    userViewModel.addUser(MockUser.createMockUser(savedEvents = emptyList())) {}

    composeTestRule.setContent {
      ProvidePreferenceLocals { SavedScreen(navigationAction, eventViewModel, userViewModel) }
    }

    composeTestRule.waitForIdle()

    composeTestRule.onNodeWithTag(SavedTestTags.TITLE).assertDisplayComponentInScroll()
    composeTestRule.onNodeWithTag(SavedTestTags.FAB).assertDisplayComponentInScroll()
    composeTestRule.onNodeWithTag(SavedTestTags.NO_EVENTS).assertDisplayComponentInScroll()
  }
}
