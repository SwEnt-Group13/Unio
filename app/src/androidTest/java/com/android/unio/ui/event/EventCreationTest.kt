package com.android.unio.ui.event

import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import com.android.unio.assertDisplayComponentInScroll
import com.android.unio.mocks.association.MockAssociation
import com.android.unio.mocks.event.MockEvent
import com.android.unio.mocks.user.MockUser
import com.android.unio.model.association.AssociationRepositoryFirestore
import com.android.unio.model.association.AssociationViewModel
import com.android.unio.model.event.Event
import com.android.unio.model.event.EventRepositoryFirestore
import com.android.unio.model.event.EventViewModel
import com.android.unio.model.follow.ConcurrentAssociationUserRepositoryFirestore
import com.android.unio.model.image.ImageRepositoryFirebaseStorage
import com.android.unio.model.search.SearchRepository
import com.android.unio.model.search.SearchViewModel
import com.android.unio.model.strings.test_tags.EventCreationOverlayTestTags
import com.android.unio.model.strings.test_tags.EventCreationTestTags
import com.android.unio.ui.navigation.NavigationAction
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth
import com.google.firebase.auth.internal.zzac
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import io.mockk.MockKAnnotations
import io.mockk.clearAllMocks
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.mockkStatic
import io.mockk.spyk
import io.mockk.unmockkAll
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test

@HiltAndroidTest
class EventCreationTest {
  val user = MockUser.createMockUser(uid = "1")
  @MockK lateinit var navigationAction: NavigationAction
  @MockK private lateinit var firebaseAuth: FirebaseAuth

  // This is the implementation of the abstract method getUid() from FirebaseUser.
  // Because it is impossible to mock abstract method, this is the only way to mock it.
  @MockK private lateinit var mockFirebaseUser: zzac

  @get:Rule val composeTestRule = createComposeRule()
  @get:Rule val hiltRule = HiltAndroidRule(this)

  val events = listOf(MockEvent.createMockEvent())
  @MockK private lateinit var eventRepository: EventRepositoryFirestore
  private lateinit var eventViewModel: EventViewModel

  private lateinit var searchViewModel: SearchViewModel
  @MockK(relaxed = true) private lateinit var searchRepository: SearchRepository

  private lateinit var associationViewModel: AssociationViewModel
  @MockK private lateinit var associationRepositoryFirestore: AssociationRepositoryFirestore
  @MockK private lateinit var eventRepositoryFirestore: EventRepositoryFirestore
  @MockK private lateinit var imageRepositoryFirestore: ImageRepositoryFirebaseStorage
  @MockK
  private lateinit var concurrentAssociationUserRepositoryFirestore:
      ConcurrentAssociationUserRepositoryFirestore

  @Before
  fun setUp() {
    MockKAnnotations.init(this, relaxed = true)
    hiltRule.inject()

    mockkStatic(FirebaseAuth::class)
    every { Firebase.auth } returns firebaseAuth
    every { firebaseAuth.currentUser } returns mockFirebaseUser

    every { eventRepository.getEvents(any(), any()) } answers
        {
          val onSuccess = args[0] as (List<Event>) -> Unit
          onSuccess(events)
        }
    eventViewModel = EventViewModel(eventRepository, imageRepositoryFirestore)

    searchViewModel = spyk(SearchViewModel(searchRepository))
    associationViewModel =
        spyk(
            AssociationViewModel(
                associationRepositoryFirestore,
                eventRepositoryFirestore,
                imageRepositoryFirestore,
                concurrentAssociationUserRepositoryFirestore))

    val associations = MockAssociation.createAllMockAssociations(size = 2)

    every { associationViewModel.findAssociationById(any()) } returns associations.first()
    every { associationViewModel.getEventsForAssociation(any(), any()) } answers
        {
          val onSuccess = args[1] as (List<Event>) -> Unit
          onSuccess(emptyList())
        }
  }

  @Test
  fun testEventCreationTagsDisplayed() {
    composeTestRule.setContent {
      EventCreationScreen(navigationAction, searchViewModel, associationViewModel, eventViewModel)
    }

    composeTestRule.waitForIdle()

    assertDisplayComponentInScroll(composeTestRule.onNodeWithTag(EventCreationTestTags.TITLE))
    assertDisplayComponentInScroll(composeTestRule.onNodeWithTag(EventCreationTestTags.EVENT_IMAGE))
    assertDisplayComponentInScroll(composeTestRule.onNodeWithTag(EventCreationTestTags.EVENT_TITLE))
    assertDisplayComponentInScroll(
        composeTestRule.onNodeWithTag(EventCreationTestTags.SHORT_DESCRIPTION))
    assertDisplayComponentInScroll(composeTestRule.onNodeWithTag(EventCreationTestTags.COAUTHORS))

    assertDisplayComponentInScroll(composeTestRule.onNodeWithTag(EventCreationTestTags.DESCRIPTION))
    assertDisplayComponentInScroll(composeTestRule.onNodeWithTag(EventCreationTestTags.LOCATION))
    assertDisplayComponentInScroll(composeTestRule.onNodeWithTag(EventCreationTestTags.SAVE_BUTTON))
    assertDisplayComponentInScroll(composeTestRule.onNodeWithTag(EventCreationTestTags.END_TIME))
    assertDisplayComponentInScroll(composeTestRule.onNodeWithTag(EventCreationTestTags.START_TIME))
    assertDisplayComponentInScroll(
        composeTestRule.onNodeWithTag(EventCreationTestTags.TAGGED_ASSOCIATIONS))

    composeTestRule.onNodeWithTag(EventCreationTestTags.TAGGED_ASSOCIATIONS).performClick()
    composeTestRule.waitForIdle()

    assertDisplayComponentInScroll(
        composeTestRule.onNodeWithTag(EventCreationOverlayTestTags.SCREEN))
    assertDisplayComponentInScroll(
        composeTestRule.onNodeWithTag(EventCreationOverlayTestTags.TITLE))
    assertDisplayComponentInScroll(composeTestRule.onNodeWithTag(EventCreationOverlayTestTags.BODY))
    assertDisplayComponentInScroll(
        composeTestRule.onNodeWithTag(EventCreationOverlayTestTags.SEARCH_BAR_INPUT))
    assertDisplayComponentInScroll(
        composeTestRule.onNodeWithTag(EventCreationOverlayTestTags.CANCEL))
    assertDisplayComponentInScroll(composeTestRule.onNodeWithTag(EventCreationOverlayTestTags.SAVE))
  }

  @After
  fun teardown() {
    clearAllMocks()
    unmockkAll()
  }
}
