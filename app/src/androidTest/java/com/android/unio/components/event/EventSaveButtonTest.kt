package com.android.unio.components.event

import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.test.rule.GrantPermissionRule
import com.android.unio.TearDown
import com.android.unio.mocks.event.MockEvent
import com.android.unio.mocks.user.MockUser
import com.android.unio.model.association.AssociationRepositoryFirestore
import com.android.unio.model.event.Event
import com.android.unio.model.event.EventRepositoryFirestore
import com.android.unio.model.event.EventUserPictureRepositoryFirestore
import com.android.unio.model.event.EventViewModel
import com.android.unio.model.image.ImageRepositoryFirebaseStorage
import com.android.unio.model.notification.NotificationWorker
import com.android.unio.model.save.ConcurrentEventUserRepositoryFirestore
import com.android.unio.model.strings.test_tags.event.EventDetailsTestTags
import com.android.unio.model.user.User
import com.android.unio.model.user.UserRepositoryFirestore
import com.android.unio.model.user.UserViewModel
import com.android.unio.ui.event.EventSaveButton
import io.mockk.MockKAnnotations
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.mockkObject
import io.mockk.spyk
import io.mockk.verify
import me.zhanghai.compose.preference.ProvidePreferenceLocals
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class EventSaveButtonTest : TearDown() {
  @get:Rule val composeTestRule = createComposeRule()

  @get:Rule
  val permissionRule: GrantPermissionRule =
      GrantPermissionRule.grant(android.Manifest.permission.POST_NOTIFICATIONS)

  @MockK private lateinit var eventRepository: EventRepositoryFirestore
  @MockK private lateinit var imageRepository: ImageRepositoryFirebaseStorage
  @MockK private lateinit var associationRepository: AssociationRepositoryFirestore
  @MockK
  private lateinit var eventUserPictureRepositoryFirestore: EventUserPictureRepositoryFirestore
  @MockK
  private lateinit var concurrentEventUserRepositoryFirestore:
      ConcurrentEventUserRepositoryFirestore
  @MockK private lateinit var userRepository: UserRepositoryFirestore
  private lateinit var eventViewModel: EventViewModel
  private lateinit var userViewModel: UserViewModel

  private val testEvent = MockEvent.createMockEvent(uid = "1")
  private val testUser = MockUser.createMockUser(uid = "1")

  @Before
  fun setUp() {
    MockKAnnotations.init(this, relaxed = true)
    mockkObject(NotificationWorker.Companion)
    eventViewModel =
        spyk(
            EventViewModel(
                eventRepository,
                imageRepository,
                associationRepository,
                eventUserPictureRepositoryFirestore,
                concurrentEventUserRepositoryFirestore))

    userViewModel = UserViewModel(userRepository, imageRepository)
    every { userRepository.updateUser(testUser, any(), any()) } answers
        {
          val onSuccess = args[1] as () -> Unit
          onSuccess()
        }
    userViewModel.addUser(testUser) {}

    every { eventRepository.getEvents(any(), any()) } answers
        {
          (it.invocation.args[0] as (List<Event>) -> Unit)(listOf(testEvent))
        }

    every { userRepository.getUserWithId(testUser.uid, {}, {}) } answers
        {
          val onSuccess = args[1] as (User) -> Unit
          onSuccess(testUser)
        }

    eventViewModel.loadEvents()
  }

  private fun setEventSaveButton() {
    composeTestRule.setContent {
      ProvidePreferenceLocals { EventSaveButton(testEvent, eventViewModel, userViewModel) }
    }
  }

  @Test
  fun testEventCardSaveAndUnsaveEventOnline() {
    var indicator = false //saved indicator
    every { concurrentEventUserRepositoryFirestore.updateSave(any(), any(), any(), any()) } answers
        {
          val onSuccess = args[2] as () -> Unit
          onSuccess()
          indicator = !indicator
        }

    setEventSaveButton()
    composeTestRule.onNodeWithTag(EventDetailsTestTags.SAVE_BUTTON).assertExists().performClick()

    Thread.sleep(500)
    assert(indicator) // asserts event is saved

    verify { NotificationWorker.schedule(any(), any()) } // asserts that a notification is scheduled

    composeTestRule.onNodeWithTag(EventDetailsTestTags.SAVE_BUTTON).assertExists().performClick()
    composeTestRule.waitForIdle()
    assert(!indicator) // asserts event is unsaved
  }
}
