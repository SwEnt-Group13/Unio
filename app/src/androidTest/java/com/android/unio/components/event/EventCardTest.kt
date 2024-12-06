package com.android.unio.components.event

import android.app.NotificationManager
import android.content.Context
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertTextEquals
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.rule.GrantPermissionRule
import com.android.unio.TearDown
import com.android.unio.mocks.association.MockAssociation
import com.android.unio.mocks.event.MockEvent
import com.android.unio.mocks.map.MockLocation
import com.android.unio.mocks.user.MockUser
import com.android.unio.model.association.AssociationRepositoryFirestore
import com.android.unio.model.event.Event
import com.android.unio.model.event.EventRepositoryFirestore
import com.android.unio.model.event.EventType
import com.android.unio.model.event.EventViewModel
import com.android.unio.model.image.ImageRepositoryFirebaseStorage
import com.android.unio.model.notification.NotificationWorker
import com.android.unio.model.strings.test_tags.EventCardTestTags
import com.android.unio.model.user.UserRepositoryFirestore
import com.android.unio.model.user.UserViewModel
import com.android.unio.ui.event.EventCard
import com.android.unio.ui.navigation.NavigationAction
import com.android.unio.ui.navigation.Screen
import com.google.firebase.Timestamp
import io.mockk.MockKAnnotations
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.just
import io.mockk.mockk
import io.mockk.mockkObject
import io.mockk.runs
import io.mockk.spyk
import io.mockk.verify
import java.util.Date
import me.zhanghai.compose.preference.ProvidePreferenceLocals
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class EventCardTest : TearDown() {

  @get:Rule val composeTestRule = createComposeRule()

  @get:Rule
  val permissionRule = GrantPermissionRule.grant(android.Manifest.permission.POST_NOTIFICATIONS)

  private lateinit var navigationAction: NavigationAction
  private val imgUrl =
      "https://m.media-amazon.com/images/S/pv-target-images/4be23d776550ebae78e63f21bec3515d3347ac4f44a3fb81e6633cf7a116761e.jpg"

  private val sampleEvent =
      MockEvent.createMockEvent(
          uid = "sample_event_123",
          location = MockLocation.createMockLocation(name = "Sample Location"),
          startDate = Timestamp(Date(2025 - 1900, 6, 20)),
          endDate = Timestamp(Date(2025 - 1900, 6, 20)),
          catchyDescription = "This is a catchy description.")
  private val associations =
      listOf(
          MockAssociation.createMockAssociation(uid = "c"),
          MockAssociation.createMockAssociation(uid = "d"))

  @MockK private lateinit var userRepository: UserRepositoryFirestore
  private lateinit var userViewModel: UserViewModel
  private lateinit var eventViewModel: EventViewModel
  @MockK private lateinit var eventRepository: EventRepositoryFirestore
  @MockK private lateinit var imageRepository: ImageRepositoryFirebaseStorage
  @MockK private lateinit var associationRepository: AssociationRepositoryFirestore
  private lateinit var context: Context

  @Before
  fun setUp() {
    MockKAnnotations.init(this, relaxed = true)
    navigationAction = mockk()
    mockkObject(NotificationWorker.Companion)
    context = InstrumentationRegistry.getInstrumentation().targetContext
    val user = MockUser.createMockUser(followedAssociations = associations, savedEvents = listOf())
    every { NotificationWorker.schedule(any(), any()) } just runs
    every { NotificationWorker.unschedule(any(), any()) } just runs
    eventViewModel = spyk(EventViewModel(eventRepository, imageRepository, associationRepository))
    userViewModel = UserViewModel(userRepository, imageRepository)
    every { userRepository.updateUser(user, any(), any()) } answers
        {
          val onSuccess = args[1] as () -> Unit
          onSuccess()
        }
    userViewModel.addUser(user, {})

    every { navigationAction.navigateTo(Screen.EVENT_DETAILS) } just runs
    every { eventRepository.getEvents(any(), any()) }
  }

  private fun setEventScreen(event: Event) {
    composeTestRule.setContent {
      ProvidePreferenceLocals {
        EventCard(navigationAction, event, userViewModel, eventViewModel, true)
      }
    }
  }

  @Test
  fun testEventCardElementsExist() {
    setEventScreen(sampleEvent)

    composeTestRule
        .onNodeWithTag(EventCardTestTags.EVENT_TITLE, useUnmergedTree = true)
        .assertExists()
        .assertTextEquals("Sample Event")

    composeTestRule
        .onNodeWithTag(EventCardTestTags.EVENT_MAIN_TYPE, useUnmergedTree = true)
        .assertExists()
        .assertTextEquals(EventType.TRIP.text)

    composeTestRule
        .onNodeWithTag(EventCardTestTags.EVENT_LOCATION, useUnmergedTree = true)
        .assertExists()
        .assertTextEquals("Sample Location")

    composeTestRule
        .onNodeWithTag(EventCardTestTags.EVENT_DATE, useUnmergedTree = true)
        .assertExists()
        .assertTextEquals("20/07")

    composeTestRule
        .onNodeWithTag(EventCardTestTags.EVENT_TIME, useUnmergedTree = true)
        .assertExists()
        .assertTextEquals("00:00")

    composeTestRule
        .onNodeWithTag(EventCardTestTags.EVENT_CATCHY_DESCRIPTION, useUnmergedTree = true)
        .assertExists()
        .assertTextEquals("This is a catchy description.")
    composeTestRule
        .onNodeWithTag(EventCardTestTags.EVENT_SAVE_BUTTON, useUnmergedTree = true)
        .assertExists()

    composeTestRule
        .onNodeWithTag(EventCardTestTags.EDIT_BUTTON, useUnmergedTree = true)
        .assertExists()
  }

  @Test
  fun testClickOnEventCard() {
    setEventScreen(sampleEvent)
    composeTestRule
        .onNodeWithTag(EventCardTestTags.EVENT_TITLE, useUnmergedTree = true)
        .assertIsDisplayed()
        .performClick()
    verify { navigationAction.navigateTo(Screen.EVENT_DETAILS) }
  }

  @Test
  fun testImageFallbackDisplayed() {
    setEventScreen(sampleEvent)

    // Check if the fallback image is displayed when no image is provided
    composeTestRule
        .onNodeWithTag(EventCardTestTags.EVENT_IMAGE, useUnmergedTree = true)
        .assertExists() // Fallback image exists when no image is provided
  }

  @Test
  fun testEventCardWithEmptyUid() {
    val event = MockEvent.createMockEvent(uid = MockEvent.Companion.EdgeCaseUid.EMPTY.value)
    setEventScreen(event)
    composeTestRule
        .onNodeWithTag(EventCardTestTags.EVENT_TITLE, useUnmergedTree = true)
        .assertIsDisplayed() // Ensure the title exists
  }

  @Test
  fun testEventCardWithEmptyTitle() {
    val event = MockEvent.createMockEvent(title = MockEvent.Companion.EdgeCaseTitle.EMPTY.value)
    setEventScreen(event)
    composeTestRule
        .onNodeWithTag(EventCardTestTags.EVENT_TITLE, useUnmergedTree = true)
        .assertTextEquals(MockEvent.Companion.EdgeCaseTitle.EMPTY.value)
  }

  @Test
  fun testEventCardWithInvalidImage() {
    val event = MockEvent.createMockEvent(image = MockEvent.Companion.EdgeCaseImage.INVALID.value)
    setEventScreen(event)
    composeTestRule
        .onNodeWithTag(EventCardTestTags.EVENT_IMAGE, useUnmergedTree = true)
        .assertExists() // Expect image to use fallback
  }

  @Test
  fun testEventCardWithValidImage() {
    val event = MockEvent.createMockEvent(image = imgUrl)
    setEventScreen(event)
    composeTestRule
        .onNodeWithTag(EventCardTestTags.EVENT_IMAGE, useUnmergedTree = true)
        .assertExists() // Expect image to use fallback
  }

  @Test
  fun testEventCardWithEmptyDescription() {
    val event =
        MockEvent.createMockEvent(
            catchyDescription = MockEvent.Companion.EdgeCaseCatchyDescription.EMPTY.value)
    setEventScreen(event)
    composeTestRule
        .onNodeWithTag(EventCardTestTags.EVENT_CATCHY_DESCRIPTION, useUnmergedTree = true)
        .assertTextEquals("") // Expect empty catchy description
  }

  @Test
  fun testEventCardWithSpecialCharactersCatchyDescription() {
    val event =
        MockEvent.createMockEvent(
            catchyDescription =
                MockEvent.Companion.EdgeCaseCatchyDescription.SPECIAL_CHARACTERS.value)
    setEventScreen(event)
    composeTestRule
        .onNodeWithTag(EventCardTestTags.EVENT_CATCHY_DESCRIPTION, useUnmergedTree = true)
        .assertTextEquals(MockEvent.Companion.EdgeCaseCatchyDescription.SPECIAL_CHARACTERS.value)
  }

  @Test
  fun testEventCardWithPastStartAndEndDate() {
    val event =
        MockEvent.createMockEvent(
            startDate = MockEvent.Companion.EdgeCaseDate.PAST.value,
            endDate = MockEvent.Companion.EdgeCaseDate.PAST.value)
    setEventScreen(event)
    composeTestRule
        .onNodeWithTag(EventCardTestTags.EVENT_DATE, useUnmergedTree = true)
        .assertExists()
  }

  @Test
  fun testEventCardWithTodayStartDate() {
    val event = MockEvent.createMockEvent(startDate = MockEvent.Companion.EdgeCaseDate.TODAY.value)
    setEventScreen(event)
    composeTestRule
        .onNodeWithTag(EventCardTestTags.EVENT_DATE, useUnmergedTree = true)
        .assertExists()
  }

  @Test
  fun testEventCardWithFutureStartDate() {
    val event = MockEvent.createMockEvent(startDate = MockEvent.Companion.EdgeCaseDate.FUTURE.value)
    setEventScreen(event)
    composeTestRule
        .onNodeWithTag(EventCardTestTags.EVENT_DATE, useUnmergedTree = true)
        .assertExists()
  }

  @Test
  fun testEventCardSaveAndUnsaveEventOnline() {
    var indicator = false
    every { eventViewModel.updateEventWithoutImage(any(), any(), any()) } answers
        {
          indicator = !indicator
        }
    val event =
        MockEvent.createMockEvent(
            startDate = Timestamp(Date((Timestamp.now().seconds + 4 * 3600) * 1000)))

    setEventScreen(event)
    composeTestRule.onNodeWithTag(EventCardTestTags.EVENT_SAVE_BUTTON).assertExists().performClick()

    Thread.sleep(500)
    assert(indicator)
    verify { NotificationWorker.schedule(any(), any()) }

    composeTestRule.onNodeWithTag(EventCardTestTags.EVENT_SAVE_BUTTON).assertExists().performClick()
    composeTestRule.waitForIdle()
    assert(!indicator)
  }

  @After
  override fun tearDown() {
    super.tearDown()
    val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
    manager.cancelAll()
  }
}
