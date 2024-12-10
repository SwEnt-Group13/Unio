package com.android.unio.components.event

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.test.performTextClearance
import androidx.compose.ui.test.performTextReplacement
import com.android.unio.TearDown
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
import com.android.unio.model.map.nominatim.NominatimLocationRepository
import com.android.unio.model.map.nominatim.NominatimLocationSearchViewModel
import com.android.unio.model.search.SearchRepository
import com.android.unio.model.search.SearchViewModel
import com.android.unio.model.strings.test_tags.event.EventEditTestTags
import com.android.unio.ui.event.EventEditScreen
import com.android.unio.ui.navigation.NavigationAction
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth
import com.google.firebase.auth.internal.zzac
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import io.mockk.MockKAnnotations
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.mockkStatic
import io.mockk.slot
import io.mockk.spyk
import org.junit.Before
import org.junit.Rule
import org.junit.Test

@HiltAndroidTest
class EventEditTests : TearDown() {
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

  private val mockEvent =
      MockEvent.createMockEvent(
          title = "Sample Event",
          organisers = MockAssociation.createAllMockAssociations(),
          taggedAssociations = MockAssociation.createAllMockAssociations(),
          image = "https://example.com/event_image.png",
          description = "This is a sample event description.",
          catchyDescription = "Catchy tagline!",
          price = 20.0,
          startDate = MockEvent.createMockEvent().startDate,
          endDate = MockEvent.createMockEvent().endDate,
          location = MockEvent.createMockEvent().location,
          types = listOf(MockEvent.createMockEvent().types.first()))

  @MockK
  private lateinit var nominatimLocationRepositoryWithoutFunctionality: NominatimLocationRepository
  private lateinit var nominatimLocationSearchViewModel: NominatimLocationSearchViewModel

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
    eventViewModel =
        spyk(
            EventViewModel(
                eventRepository, imageRepositoryFirestore, associationRepositoryFirestore))

    every { eventViewModel.findEventById(any()) } returns mockEvent
    eventViewModel.selectEvent(mockEvent.uid)

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
    associationViewModel.selectAssociation(associations.first().uid)

    every { associationViewModel.findAssociationById(any()) } returns associations.first()
  }

  @Test
  fun testEventEditTagsDisplayed() {
    nominatimLocationSearchViewModel =
        NominatimLocationSearchViewModel(nominatimLocationRepositoryWithoutFunctionality)
    composeTestRule.setContent {
      EventEditScreen(
          navigationAction,
          searchViewModel,
          associationViewModel,
          eventViewModel,
          nominatimLocationSearchViewModel)
    }

    composeTestRule.waitForIdle()

    composeTestRule.onNodeWithTag(EventEditTestTags.TITLE).assertDisplayComponentInScroll()

    composeTestRule.onNodeWithTag(EventEditTestTags.EVENT_TITLE).assertDisplayComponentInScroll()

    composeTestRule
        .onNodeWithTag(EventEditTestTags.SHORT_DESCRIPTION)
        .assertDisplayComponentInScroll()
    composeTestRule.onNodeWithTag(EventEditTestTags.COAUTHORS).assertDisplayComponentInScroll()

    composeTestRule.onNodeWithTag(EventEditTestTags.DESCRIPTION).assertDisplayComponentInScroll()
    composeTestRule.onNodeWithTag(EventEditTestTags.LOCATION).assertDisplayComponentInScroll()
    composeTestRule.onNodeWithTag(EventEditTestTags.DELETE_BUTTON).assertDisplayComponentInScroll()
    composeTestRule.onNodeWithTag(EventEditTestTags.SAVE_BUTTON).assertDisplayComponentInScroll()
    composeTestRule.onNodeWithTag(EventEditTestTags.END_TIME).assertDisplayComponentInScroll()
    composeTestRule.onNodeWithTag(EventEditTestTags.START_TIME).assertDisplayComponentInScroll()
    composeTestRule.onNodeWithTag(EventEditTestTags.EVENT_IMAGE).assertDisplayComponentInScroll()

    composeTestRule
        .onNodeWithTag(EventEditTestTags.TAGGED_ASSOCIATIONS)
        .assertDisplayComponentInScroll()

    composeTestRule
        .onNodeWithTag(EventEditTestTags.START_DATE_FIELD)
        .performScrollTo()
        .assertIsDisplayed()
        .performClick()
    composeTestRule.onNodeWithTag(EventEditTestTags.START_DATE_PICKER).assertIsDisplayed()
    composeTestRule.onNodeWithText("Cancel").performClick()

    composeTestRule
        .onNodeWithTag(EventEditTestTags.START_TIME_FIELD)
        .performScrollTo()
        .assertIsDisplayed()
        .performClick()
    composeTestRule.onNodeWithTag(EventEditTestTags.START_TIME_PICKER).assertIsDisplayed()
    composeTestRule.onNodeWithText("Cancel").performClick()

    composeTestRule
        .onNodeWithTag(EventEditTestTags.END_DATE_FIELD)
        .performScrollTo()
        .assertIsDisplayed()
        .performClick()
    composeTestRule.onNodeWithTag(EventEditTestTags.END_DATE_PICKER).assertIsDisplayed()
    composeTestRule.onNodeWithText("Cancel").performClick()

    composeTestRule
        .onNodeWithTag(EventEditTestTags.END_TIME_FIELD)
        .performScrollTo()
        .assertIsDisplayed()
        .performClick()
    composeTestRule.onNodeWithTag(EventEditTestTags.END_TIME_PICKER).assertIsDisplayed()
    composeTestRule.onNodeWithText("Cancel").performClick()

    composeTestRule.onNodeWithTag(EventEditTestTags.TAGGED_ASSOCIATIONS).performClick()
    composeTestRule.waitForIdle()
  }

  @Test
  fun testEventCannotBeSavedWhenEmptyField() {
    nominatimLocationSearchViewModel =
        NominatimLocationSearchViewModel(nominatimLocationRepositoryWithoutFunctionality)
    composeTestRule.setContent {
      EventEditScreen(
          navigationAction,
          searchViewModel,
          associationViewModel,
          eventViewModel,
          nominatimLocationSearchViewModel)
    }
    composeTestRule
        .onNodeWithTag(EventEditTestTags.EVENT_TITLE, useUnmergedTree = true)
        .performTextClearance()
    composeTestRule.onNodeWithTag(EventEditTestTags.SAVE_BUTTON).assertIsNotEnabled()
    composeTestRule.waitForIdle()
  }

  @Test
  fun testDeleteButtonWorksCorrectly() {
    nominatimLocationSearchViewModel =
        NominatimLocationSearchViewModel(nominatimLocationRepositoryWithoutFunctionality)
    var shouldBeTrue = false
    every { eventViewModel.deleteEvent(any(), any(), any()) } answers { shouldBeTrue = true }

    composeTestRule.setContent {
      EventEditScreen(
          navigationAction,
          searchViewModel,
          associationViewModel,
          eventViewModel,
          nominatimLocationSearchViewModel)
    }

    composeTestRule.onNodeWithTag(EventEditTestTags.DELETE_BUTTON).performScrollTo().performClick()
    composeTestRule.waitForIdle()
    assert(shouldBeTrue)
  }

  @Test
  fun testSaveButtonSavesNewEvent() {
    nominatimLocationSearchViewModel =
        NominatimLocationSearchViewModel(nominatimLocationRepositoryWithoutFunctionality)
    var shouldBeTrue = false

    val eventSlot = slot<Event>()
    every { eventViewModel.updateEventWithoutImage(capture(eventSlot), any(), any()) } answers
        {
          shouldBeTrue = true
        }

    composeTestRule.setContent {
      EventEditScreen(
          navigationAction,
          searchViewModel,
          associationViewModel,
          eventViewModel,
          nominatimLocationSearchViewModel)
    }
    composeTestRule
        .onNodeWithTag(EventEditTestTags.EVENT_TITLE)
        .performTextReplacement("New Sample Event")

    composeTestRule.onNodeWithTag(EventEditTestTags.SAVE_BUTTON).performScrollTo().performClick()

    composeTestRule.waitForIdle()

    val result = eventSlot.captured
    assert(shouldBeTrue)
    assert(result.title != mockEvent.title)
    assert(result.description == mockEvent.description)
  }
}
