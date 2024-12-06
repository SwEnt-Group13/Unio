package com.android.unio.components.event

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertTextEquals
import androidx.compose.ui.test.isDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.test.performTextClearance
import androidx.compose.ui.test.performTextInput
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
import com.android.unio.model.map.nominatim.NominatimApiService
import com.android.unio.model.map.nominatim.NominatimLocationRepository
import com.android.unio.model.map.nominatim.NominatimLocationSearchViewModel
import com.android.unio.model.search.SearchRepository
import com.android.unio.model.search.SearchViewModel
import com.android.unio.model.strings.TextLengthSamples
import com.android.unio.model.strings.test_tags.EventCreationOverlayTestTags
import com.android.unio.model.strings.test_tags.EventCreationTestTags
import com.android.unio.ui.event.EventCreationScreen
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
import io.mockk.spyk
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.net.HttpURLConnection

@HiltAndroidTest
class EventCreationTest : TearDown() {
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

  @MockK
  private lateinit var nominatimLocationRepositoryWithoutFunctionality: NominatimLocationRepository
  private lateinit var nominatimLocationSearchViewModel: NominatimLocationSearchViewModel

  private lateinit var server: MockWebServer
  private lateinit var apiService: NominatimApiService
  private lateinit var nominatimLocationRepository: NominatimLocationRepository
  private lateinit var mockResponseBody: String

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
        EventViewModel(eventRepository, imageRepositoryFirestore, associationRepositoryFirestore)

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
    nominatimLocationSearchViewModel =
        NominatimLocationSearchViewModel(nominatimLocationRepositoryWithoutFunctionality)
    composeTestRule.setContent {
      EventCreationScreen(
          navigationAction,
          searchViewModel,
          associationViewModel,
          eventViewModel,
          nominatimLocationSearchViewModel)
    }

    composeTestRule.waitForIdle()

    composeTestRule.onNodeWithTag(EventCreationTestTags.TITLE).assertDisplayComponentInScroll()
    composeTestRule
        .onNodeWithTag(EventCreationTestTags.EVENT_IMAGE)
        .assertDisplayComponentInScroll()
    composeTestRule
        .onNodeWithTag(EventCreationTestTags.EVENT_TITLE)
        .assertDisplayComponentInScroll()

    composeTestRule
        .onNodeWithTag(EventCreationTestTags.SHORT_DESCRIPTION)
        .assertDisplayComponentInScroll()
    composeTestRule.onNodeWithTag(EventCreationTestTags.COAUTHORS).assertDisplayComponentInScroll()

    composeTestRule
        .onNodeWithTag(EventCreationTestTags.DESCRIPTION)
        .assertDisplayComponentInScroll()
    composeTestRule.onNodeWithTag(EventCreationTestTags.LOCATION).assertDisplayComponentInScroll()
    composeTestRule
        .onNodeWithTag(EventCreationTestTags.SAVE_BUTTON)
        .assertDisplayComponentInScroll()
    composeTestRule.onNodeWithTag(EventCreationTestTags.END_TIME).assertDisplayComponentInScroll()
    composeTestRule.onNodeWithTag(EventCreationTestTags.START_TIME).assertDisplayComponentInScroll()

    composeTestRule
        .onNodeWithTag(EventCreationTestTags.TAGGED_ASSOCIATIONS)
        .assertDisplayComponentInScroll()

    composeTestRule
        .onNodeWithTag(EventCreationTestTags.START_DATE_FIELD)
        .performScrollTo()
        .assertIsDisplayed()
        .performClick()
    composeTestRule.onNodeWithTag(EventCreationTestTags.START_DATE_PICKER).assertIsDisplayed()
    composeTestRule.onNodeWithText("Cancel").performClick()

    composeTestRule
        .onNodeWithTag(EventCreationTestTags.START_TIME_FIELD)
        .performScrollTo()
        .assertIsDisplayed()
        .performClick()
    composeTestRule.onNodeWithTag(EventCreationTestTags.START_TIME_PICKER).assertIsDisplayed()
    composeTestRule.onNodeWithText("Cancel").performClick()

    composeTestRule
        .onNodeWithTag(EventCreationTestTags.END_DATE_FIELD)
        .performScrollTo()
        .assertIsDisplayed()
        .performClick()
    composeTestRule.onNodeWithTag(EventCreationTestTags.END_DATE_PICKER).assertIsDisplayed()
    composeTestRule.onNodeWithText("Cancel").performClick()

    composeTestRule
        .onNodeWithTag(EventCreationTestTags.END_TIME_FIELD)
        .performScrollTo()
        .assertIsDisplayed()
        .performClick()
    composeTestRule.onNodeWithTag(EventCreationTestTags.END_TIME_PICKER).assertIsDisplayed()
    composeTestRule.onNodeWithText("Cancel").performClick()

    composeTestRule.onNodeWithTag(EventCreationTestTags.TAGGED_ASSOCIATIONS).performClick()
    composeTestRule.waitForIdle()

    composeTestRule
        .onNodeWithTag(EventCreationOverlayTestTags.SCREEN)
        .assertDisplayComponentInScroll()

    composeTestRule
        .onNodeWithTag(EventCreationOverlayTestTags.TITLE)
        .assertDisplayComponentInScroll()
    composeTestRule
        .onNodeWithTag(EventCreationOverlayTestTags.BODY)
        .assertDisplayComponentInScroll()

    composeTestRule
        .onNodeWithTag(EventCreationOverlayTestTags.SEARCH_BAR_INPUT)
        .assertDisplayComponentInScroll()

    composeTestRule
        .onNodeWithTag(EventCreationOverlayTestTags.CANCEL)
        .assertDisplayComponentInScroll()
    composeTestRule
        .onNodeWithTag(EventCreationOverlayTestTags.SAVE)
        .assertDisplayComponentInScroll()
  }

  @Test
  fun testCorrectlyDisplaysCharacterCountForTextFields() {
    composeTestRule
        .onNodeWithTag(EventCreationTestTags.EVENT_TITLE)
        .performScrollTo()
        .performTextInput(TextLengthSamples.SMALL)
    composeTestRule.onNodeWithTag(EventCreationTestTags.TITLE_CHARACTER_COUNTER).assertIsDisplayed()
    composeTestRule.onNodeWithTag(EventCreationTestTags.EVENT_TITLE).performTextClearance()

    composeTestRule
        .onNodeWithTag(EventCreationTestTags.SHORT_DESCRIPTION)
        .performScrollTo()
        .performTextInput(TextLengthSamples.MEDIUM)
    composeTestRule
        .onNodeWithTag(EventCreationTestTags.SHORT_DESCRIPTION_CHARACTER_COUNTER)
        .assertIsDisplayed()
    composeTestRule.onNodeWithTag(EventCreationTestTags.SHORT_DESCRIPTION).performTextClearance()

    composeTestRule
        .onNodeWithTag(EventCreationTestTags.DESCRIPTION)
        .performScrollTo()
        .performTextInput(TextLengthSamples.LARGE)
    composeTestRule
        .onNodeWithTag(EventCreationTestTags.DESCRIPTION_CHARACTER_COUNTER)
        .assertIsDisplayed()
    composeTestRule.onNodeWithTag(EventCreationTestTags.DESCRIPTION).performTextClearance()
  }

  @Test
  fun testLocationInputFunctionality() {
    server = MockWebServer()
    server.start()

    apiService =
        Retrofit.Builder()
            .baseUrl(server.url("/"))
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(NominatimApiService::class.java)

    nominatimLocationRepository = NominatimLocationRepository(apiService)

    mockResponseBody =
        """
                [
                    {
                        "lat": "45.512331",
                        "lon": "7.559331",
                        "display_name": "Test Address, Test City, Test Country",
                        "address": {
                            "road": "Test Road",
                            "house_number": "123",
                            "postcode": "12345",
                            "city": "Test City",
                            "state": "Test State",
                            "country": "Test Country"
                        }
                    }
                ]
            """
            .trimIndent()
    nominatimLocationSearchViewModel = NominatimLocationSearchViewModel(nominatimLocationRepository)

    composeTestRule.setContent {
      EventCreationScreen(
          navigationAction,
          searchViewModel,
          associationViewModel,
          eventViewModel,
          nominatimLocationSearchViewModel)
    }

    composeTestRule.waitForIdle()

    val query = "Test Query"

    server.enqueue(
        MockResponse().setBody(mockResponseBody).setResponseCode(HttpURLConnection.HTTP_OK))

    composeTestRule.onNodeWithTag(EventCreationTestTags.LOCATION).performTextClearance()
    composeTestRule.onNodeWithTag(EventCreationTestTags.LOCATION).performTextInput(query)

    composeTestRule.waitUntil(10000) {
      composeTestRule
          .onNodeWithTag(EventCreationTestTags.LOCATION_SUGGESTION_ITEM_LATITUDE + "45.512331")
          .isDisplayed()
    }

    composeTestRule
        .onNodeWithTag(EventCreationTestTags.LOCATION_SUGGESTION_ITEM_LATITUDE + "45.512331")
        .performClick()

    composeTestRule.waitForIdle()

    composeTestRule
        .onNodeWithTag(EventCreationTestTags.LOCATION, useUnmergedTree = true)
        .assertTextEquals(
            "Test Road, 123, 12345 Test City, Test State, Test Country", includeEditableText = true)

    server.shutdown()
  }
}
