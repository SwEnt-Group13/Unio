package com.android.unio.components.event

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsNotDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import com.android.unio.TearDown
import com.android.unio.mocks.event.MockEvent
import com.android.unio.mocks.user.MockUser
import com.android.unio.model.association.AssociationRepositoryFirestore
import com.android.unio.model.event.Event
import com.android.unio.model.event.EventRepositoryFirestore
import com.android.unio.model.event.EventUserPictureRepositoryFirestore
import com.android.unio.model.event.EventViewModel
import com.android.unio.model.image.ImageRepositoryFirebaseStorage
import com.android.unio.model.usecase.SaveUseCaseFirestore
import com.android.unio.model.strings.test_tags.event.EventDetailsTestTags
import com.android.unio.model.user.User
import com.android.unio.ui.event.EventDetailsPicturePicker
import io.mockk.MockKAnnotations
import io.mockk.impl.annotations.MockK
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class EventDetailsPicturePickerTest : TearDown() {

  @get:Rule val composeTestRule = createComposeRule()
  private lateinit var testEvent: Event
  private lateinit var testUser: User
  @MockK private lateinit var eventRepository: EventRepositoryFirestore
  @MockK private lateinit var imageRepository: ImageRepositoryFirebaseStorage
  @MockK private lateinit var associationRepository: AssociationRepositoryFirestore
  @MockK
  private lateinit var eventUserPictureRepositoryFirestore: EventUserPictureRepositoryFirestore
  @MockK
  private lateinit var concurrentEventUserRepositoryFirestore:
          SaveUseCaseFirestore

  private lateinit var eventViewModel: EventViewModel

  fun setPicturePicker() {
    composeTestRule.setContent { EventDetailsPicturePicker(testEvent, eventViewModel, testUser) }
  }

  @Before
  fun setUp() {
    MockKAnnotations.init(this, relaxed = true)
    testEvent = MockEvent.createMockEvent(uid = "2")
    testUser = MockUser.createMockUser(uid = "74", firstName = "John")

    eventViewModel =
        EventViewModel(
            eventRepository,
            imageRepository,
            associationRepository,
            eventUserPictureRepositoryFirestore,
            concurrentEventUserRepositoryFirestore)
  }

  @Test
  fun testButtonIsDisplayed() {
    setPicturePicker()
    composeTestRule.onNodeWithTag(EventDetailsTestTags.UPLOAD_PICTURE_BUTTON).assertIsDisplayed()
  }

  @Test
  fun testPicturePickerIsDisplayed() {

    setPicturePicker()
    composeTestRule
        .onNodeWithTag(EventDetailsTestTags.PICTURE_SELECTION_SHEET)
        .assertIsNotDisplayed()
    composeTestRule.onNodeWithTag(EventDetailsTestTags.UPLOAD_PICTURE_BUTTON).performClick()
    composeTestRule.onNodeWithTag(EventDetailsTestTags.PICTURE_SELECTION_SHEET).assertIsDisplayed()
  }
}
