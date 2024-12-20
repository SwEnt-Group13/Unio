package com.android.unio.components.event

import android.content.ContentResolver
import android.content.res.Resources
import android.net.Uri
import androidx.annotation.AnyRes
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsNotDisplayed
import androidx.compose.ui.test.assertTextEquals
import androidx.compose.ui.test.isDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollToIndex
import androidx.navigation.NavHostController
import androidx.test.espresso.Espresso
import androidx.test.espresso.action.ViewActions
import androidx.test.espresso.matcher.ViewMatchers
import androidx.test.platform.app.InstrumentationRegistry
import com.android.unio.R
import com.android.unio.TearDown
import com.android.unio.assertDisplayComponentInScroll
import com.android.unio.mocks.association.MockAssociation
import com.android.unio.mocks.event.MockEvent
import com.android.unio.mocks.firestore.MockReferenceList
import com.android.unio.mocks.user.MockUser
import com.android.unio.model.association.Association
import com.android.unio.model.association.AssociationRepositoryFirestore
import com.android.unio.model.event.Event
import com.android.unio.model.event.EventRepositoryFirestore
import com.android.unio.model.event.EventUserPicture
import com.android.unio.model.event.EventUserPictureRepositoryFirestore
import com.android.unio.model.event.EventUtils.formatTimestamp
import com.android.unio.model.event.EventViewModel
import com.android.unio.model.firestore.emptyFirestoreReferenceList
import com.android.unio.model.image.ImageRepositoryFirebaseStorage
import com.android.unio.model.map.MapViewModel
import com.android.unio.model.strings.FormatStrings.DAY_MONTH_FORMAT
import com.android.unio.model.strings.test_tags.event.EventDetailsTestTags
import com.android.unio.model.usecase.SaveUseCaseFirestore
import com.android.unio.model.usecase.UserDeletionUseCaseFirestore
import com.android.unio.model.user.User
import com.android.unio.model.user.UserRepositoryFirestore
import com.android.unio.model.user.UserViewModel
import com.android.unio.ui.event.EventScreenScaffold
import com.android.unio.ui.navigation.NavigationAction
import com.android.unio.ui.navigation.Screen
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.firebase.Timestamp
import emptyFirestoreReferenceElement
import io.mockk.MockKAnnotations
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.verify
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import me.zhanghai.compose.preference.ProvidePreferenceLocals
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.Mockito.mock

class EventDetailsTest : TearDown() {
  @MockK private lateinit var navHostController: NavHostController
  private lateinit var navigationAction: NavigationAction

  private lateinit var events: List<Event>
  private lateinit var eventPictures: List<EventUserPicture>
  private lateinit var associations: List<Association>

  private lateinit var fusedLocationProviderClient: FusedLocationProviderClient
  private lateinit var mapViewModel: MapViewModel

  @MockK private lateinit var eventRepository: EventRepositoryFirestore
  @MockK private lateinit var associationRepository: AssociationRepositoryFirestore
  @MockK private lateinit var userRepository: UserRepositoryFirestore
  @MockK private lateinit var userDeletionRepository: UserDeletionUseCaseFirestore
  @MockK private lateinit var imageRepository: ImageRepositoryFirebaseStorage
  @MockK
  private lateinit var eventUserPictureRepositoryFirestore: EventUserPictureRepositoryFirestore
  @MockK private lateinit var concurrentEventUserRepositoryFirestore: SaveUseCaseFirestore

  private lateinit var eventViewModel: EventViewModel
  private lateinit var userViewModel: UserViewModel

  @get:Rule val composeTestRule = createComposeRule()

  private fun Resources.getUri(@AnyRes int: Int): Uri {
    val scheme = ContentResolver.SCHEME_ANDROID_RESOURCE
    val pkg = getResourcePackageName(int)
    val type = getResourceTypeName(int)
    val name = getResourceEntryName(int)
    val uri = "$scheme://$pkg/$type/$name"
    return Uri.parse(uri)
  }

  @Before
  fun setUp() {
    MockKAnnotations.init(this, relaxed = true)
    val context = InstrumentationRegistry.getInstrumentation().targetContext
    val resources = context.applicationContext.resources
    eventPictures =
        listOf(
            EventUserPicture(
                "12",
                resources.getUri(R.drawable.placeholder_pictures).toString(),
                User.emptyFirestoreReferenceElement(),
                User.emptyFirestoreReferenceList()),
            EventUserPicture(
                "34",
                resources.getUri(R.drawable.placeholder_pictures).toString(),
                User.emptyFirestoreReferenceElement(),
                User.emptyFirestoreReferenceList()))
    events =
        listOf(
            MockEvent.createMockEvent(
                uid = "a",
                startDate = Timestamp(Date(2024 - 1900, 6, 20)),
                endDate = Timestamp(Date(2024 - 1900, 6, 21)),
                eventPictures = MockReferenceList(eventPictures)),
            MockEvent.createMockEvent(
                uid = "b",
                startDate = Timestamp(Date(2040 - 1900, 6, 20)),
                endDate = Timestamp(Date(2040 - 1900, 6, 20)),
                eventPictures = MockReferenceList(eventPictures)),
            MockEvent.createMockEvent(
                uid = "a",
                startDate = Timestamp(Date(2024 - 1900, 6, 20)),
                endDate = Timestamp(Date(2024 - 1900, 6, 21)),
                eventPictures = MockReferenceList()))

    associations =
        listOf(
            MockAssociation.createMockAssociation(uid = "c"),
            MockAssociation.createMockAssociation(uid = "d"))

    navigationAction = NavigationAction(navHostController)
    fusedLocationProviderClient = mock()
    mapViewModel = MapViewModel(fusedLocationProviderClient)

    eventViewModel =
        EventViewModel(
            eventRepository,
            imageRepository,
            associationRepository,
            eventUserPictureRepositoryFirestore,
            concurrentEventUserRepositoryFirestore)

    every { eventRepository.getEvents(any(), any()) } answers
        {
          (it.invocation.args[0] as (List<Event>) -> Unit)(events)
        }
    every { userRepository.init(any()) } returns Unit
    every { userRepository.getUserWithId("uid", any(), any()) } answers
        {
          (it.invocation.args[1] as (User) -> Unit)((MockUser.createMockUser()))
        }

    userViewModel = UserViewModel(userRepository, imageRepository, userDeletionRepository)
    userViewModel.getUserByUid("uid")
  }

  private fun setEventScreen(event: Event) {
    composeTestRule.setContent {
      ProvidePreferenceLocals {
        EventScreenScaffold(
            navigationAction, mapViewModel, event, associations, eventViewModel, userViewModel)
      }
    }
  }

  @Test
  fun testEventDetailsDisplayComponent() {
    val event = events[1]
    setEventScreen(event)
    composeTestRule.waitForIdle()

    val formattedStartDateDay =
        formatTimestamp(event.startDate, SimpleDateFormat(DAY_MONTH_FORMAT, Locale.getDefault()))
    val formattedEndDateDay =
        formatTimestamp(event.endDate, SimpleDateFormat(DAY_MONTH_FORMAT, Locale.getDefault()))

    composeTestRule
        .onNodeWithTag(EventDetailsTestTags.SCREEN, true)
        .assertDisplayComponentInScroll()
    composeTestRule
        .onNodeWithTag(EventDetailsTestTags.GO_BACK_BUTTON)
        .assertDisplayComponentInScroll()

    composeTestRule.onNodeWithTag(EventDetailsTestTags.SAVE_BUTTON).assertDisplayComponentInScroll()
    composeTestRule
        .onNodeWithTag(EventDetailsTestTags.SHARE_BUTTON)
        .assertDisplayComponentInScroll()
    composeTestRule
        .onNodeWithTag(EventDetailsTestTags.DETAILS_PAGE)
        .assertDisplayComponentInScroll()

    composeTestRule
        .onNodeWithTag(EventDetailsTestTags.DETAILS_INFORMATION_CARD)
        .assertDisplayComponentInScroll()
    composeTestRule.onNodeWithTag(EventDetailsTestTags.TITLE).assertDisplayComponentInScroll()

    composeTestRule
        .onNodeWithTag("${EventDetailsTestTags.ORGANIZING_ASSOCIATION}0")
        .assertDisplayComponentInScroll()

    composeTestRule
        .onNodeWithTag("${EventDetailsTestTags.ORGANIZING_ASSOCIATION}1")
        .assertDisplayComponentInScroll()

    composeTestRule
        .onNodeWithTag("${EventDetailsTestTags.ASSOCIATION_LOGO}0")
        .assertDisplayComponentInScroll()

    composeTestRule
        .onNodeWithTag("${EventDetailsTestTags.ASSOCIATION_NAME}0")
        .assertDisplayComponentInScroll()

    composeTestRule
        .onNodeWithTag("${EventDetailsTestTags.ASSOCIATION_LOGO}1")
        .assertDisplayComponentInScroll()

    composeTestRule
        .onNodeWithTag("${EventDetailsTestTags.ASSOCIATION_NAME}1")
        .assertDisplayComponentInScroll()

    if (formattedStartDateDay == formattedEndDateDay) {
      composeTestRule.onNodeWithTag(EventDetailsTestTags.HOUR).assertDisplayComponentInScroll()
      composeTestRule
          .onNodeWithTag(EventDetailsTestTags.START_DATE)
          .assertDisplayComponentInScroll()
    } else {
      composeTestRule
          .onNodeWithTag(EventDetailsTestTags.START_DATE)
          .assertDisplayComponentInScroll()
      composeTestRule.onNodeWithTag(EventDetailsTestTags.END_DATE).assertDisplayComponentInScroll()
    }

    composeTestRule
        .onNodeWithTag(EventDetailsTestTags.DETAILS_BODY)
        .assertDisplayComponentInScroll()

    Espresso.onView(ViewMatchers.isRoot()).perform(ViewActions.swipeUp())
    composeTestRule.onNodeWithTag(EventDetailsTestTags.PLACES_REMAINING_TEXT).assertExists()
    composeTestRule.onNodeWithTag(EventDetailsTestTags.DESCRIPTION).assertDisplayComponentInScroll()
    composeTestRule
        .onNodeWithTag(EventDetailsTestTags.LOCATION_ADDRESS, true)
        .assertTextEquals(event.location.name)
        .assertDisplayComponentInScroll()
    composeTestRule.onNodeWithTag(EventDetailsTestTags.MAP_BUTTON).assertDisplayComponentInScroll()
    composeTestRule
        .onNodeWithTag(EventDetailsTestTags.EVENT_DETAILS_PAGER)
        .assertDisplayComponentInScroll()
    composeTestRule.onNodeWithTag(EventDetailsTestTags.EVENT_DETAILS_PAGER).performScrollToIndex(1)
    composeTestRule
        .onNodeWithTag(EventDetailsTestTags.UPLOAD_PICTURE_BUTTON)
        .assertDisplayComponentInScroll()
  }

  @Test
  fun testButtonBehavior() {
    setEventScreen(events[0])
    eventViewModel.loadEvents()
    // Share button
    composeTestRule
        .onNodeWithTag(EventDetailsTestTags.SHARE_BUTTON)
        .assertDisplayComponentInScroll()

    // Save button
    composeTestRule.onNodeWithTag(EventDetailsTestTags.SAVE_BUTTON).assertDisplayComponentInScroll()
    composeTestRule.onNodeWithTag(EventDetailsTestTags.SAVE_BUTTON).performClick()

    // Location button
    Espresso.onView(ViewMatchers.isRoot()).perform(ViewActions.swipeUp())
    composeTestRule.onNodeWithTag(EventDetailsTestTags.MAP_BUTTON).assertDisplayComponentInScroll()
    composeTestRule.onNodeWithTag(EventDetailsTestTags.MAP_BUTTON).performClick()
    verify { navigationAction.navigateTo(Screen.MAP) }
    assert(mapViewModel.highlightedEventUid.value == events[0].uid)
    assert(mapViewModel.centerLocation.value!!.latitude == events[0].location.latitude)
    assert(mapViewModel.centerLocation.value!!.longitude == events[0].location.longitude)
  }

  private fun assertSnackBarIsDisplayed() {
    composeTestRule.onNodeWithTag(EventDetailsTestTags.SNACKBAR_HOST).assertIsDisplayed()
    composeTestRule.onNodeWithTag(EventDetailsTestTags.SNACKBAR_ACTION_BUTTON).performClick()
    composeTestRule.onNodeWithTag(EventDetailsTestTags.SNACKBAR_HOST).assertIsNotDisplayed()
  }

  @Test
  fun testGoBackButton() {
    setEventScreen(events[0])
    composeTestRule.onNodeWithTag(EventDetailsTestTags.GO_BACK_BUTTON).performClick()
    verify { navigationAction.goBack() }
  }

  @Test
  fun testEventDetailsData() {
    val event = events[1]
    setEventScreen(event)
    composeTestRule.onNodeWithText(event.title, substring = true).assertDisplayComponentInScroll()
    composeTestRule
        .onNodeWithText(event.description, substring = true)
        .assertDisplayComponentInScroll()

    Espresso.onView(ViewMatchers.isRoot()).perform(ViewActions.swipeUp())
    composeTestRule
        .onNodeWithText(event.location.name, substring = true)
        .assertDisplayComponentInScroll()

    composeTestRule.onNodeWithTag(EventDetailsTestTags.START_DATE).assertDisplayComponentInScroll()
  }

  private fun goToGallery() {
    composeTestRule
        .onNodeWithTag(EventDetailsTestTags.EVENT_DETAILS_PAGER)
        .assertDisplayComponentInScroll()
    composeTestRule.onNodeWithTag(EventDetailsTestTags.EVENT_DETAILS_PAGER).performScrollToIndex(1)
  }

  @Test
  fun testGalleryDisplays() {
    setEventScreen(events[0])

    goToGallery()

    composeTestRule
        .onNodeWithTag(EventDetailsTestTags.GALLERY_GRID)
        .assertDisplayComponentInScroll()
  }

  @Test
  fun testGalleryDoesNotDisplayWhenFutureStartDate() {
    setEventScreen(events[1])
    goToGallery()
    composeTestRule.onNodeWithTag(EventDetailsTestTags.GALLERY_GRID).assertIsNotDisplayed()
    composeTestRule
        .onNodeWithTag(EventDetailsTestTags.EVENT_NOT_STARTED_TEXT)
        .assertDisplayComponentInScroll()
  }

  @Test
  fun testGalleryDoesNotDisplayWhenNoPictures() {
    setEventScreen(events[2])
    goToGallery()
    composeTestRule.onNodeWithTag(EventDetailsTestTags.GALLERY_GRID).assertIsNotDisplayed()
    composeTestRule
        .onNodeWithTag(EventDetailsTestTags.EVENT_NO_PICTURES_TEXT)
        .assertDisplayComponentInScroll()
  }

  @Test
  fun testFullSizePictureOnClick() {
    eventViewModel.loadEvents()
    eventViewModel.selectEvent(events[0].uid, true)

    setEventScreen(events[0])

    goToGallery()
    composeTestRule.waitUntil(5000) {
      composeTestRule
          .onNodeWithTag(EventDetailsTestTags.USER_EVENT_PICTURE + eventPictures[0].uid)
          .isDisplayed()
    }

    composeTestRule
        .onNodeWithTag(EventDetailsTestTags.USER_EVENT_PICTURE + eventPictures[0].uid)
        .performClick()

    composeTestRule.onNodeWithTag(EventDetailsTestTags.PICTURE_FULL_SCREEN).assertIsDisplayed()
    composeTestRule
        .onNodeWithTag(EventDetailsTestTags.EVENT_PICTURES_ARROW_LEFT)
        .assertIsDisplayed()
    composeTestRule
        .onNodeWithTag(EventDetailsTestTags.EVENT_PICTURES_ARROW_RIGHT)
        .assertIsDisplayed()
  }
}
