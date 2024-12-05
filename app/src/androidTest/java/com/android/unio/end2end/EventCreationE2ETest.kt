package com.android.unio.end2end

import android.app.Activity
import android.app.Instrumentation
import android.content.Intent
import android.net.Uri
import androidx.compose.material3.DatePickerDialog
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertTextEquals
import androidx.compose.ui.test.hasContentDescription
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.isDisplayed
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.test.performScrollToNode
import androidx.compose.ui.test.performTextClearance
import androidx.compose.ui.test.performTextInput
import androidx.test.espresso.intent.Intents
import androidx.test.espresso.intent.Intents.intending
import androidx.test.espresso.intent.matcher.IntentMatchers.anyIntent
import androidx.test.filters.LargeTest
import androidx.test.platform.app.InstrumentationRegistry
import com.android.unio.R
import com.android.unio.assertDisplayComponentInScroll
import com.android.unio.model.hilt.module.NetworkModule
import com.android.unio.model.map.LocationRepository
import com.android.unio.model.map.nominatim.NominatimApiService
import com.android.unio.model.map.nominatim.NominatimLocationRepository
import com.android.unio.model.strings.test_tags.AssociationProfileTestTags
import com.android.unio.model.strings.test_tags.BottomNavBarTestTags
import com.android.unio.model.strings.test_tags.EventCreationTestTags
import com.android.unio.model.strings.test_tags.EventDetailsTestTags
import com.android.unio.model.strings.test_tags.ExploreTestTags
import com.android.unio.model.strings.test_tags.HomeTestTags
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.testing.HiltAndroidTest
import dagger.hilt.android.testing.UninstallModules
import dagger.hilt.components.SingletonComponent
import java.net.HttpURLConnection
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale
import javax.inject.Inject
import javax.inject.Singleton
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.After
import org.junit.Before
import org.junit.Test
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

@LargeTest
@HiltAndroidTest
@UninstallModules(NetworkModule::class)
class EventCreationE2ETest : EndToEndTest() {

  /** The [MockWebServer] instance used to mock the location search API. */
  @Inject lateinit var mockWebServer: MockWebServer

  /** The date formatter Material3 uses to format the date in the date picker. */
  private val dateFormatter: DateTimeFormatter =
      DateTimeFormatter.ofPattern("EEEE, MMMM d, yyyy", Locale.getDefault())

  /** The [Context] used to access resources. */
  private val context = InstrumentationRegistry.getInstrumentation().targetContext

  /** The response body to be used by the mocked web client. */
  private lateinit var mockResponseBody: String

  @Before
  override fun setUp() {
    super.setUp()
    hiltRule.inject()

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
    mockWebServer.enqueue(MockResponse().setBody(mockResponseBody))

    Intents.init()
  }

  @After
  override fun tearDown() {
    super.tearDown()
    mockWebServer.shutdown()
    Intents.release()
  }

  /**
   * Selects a date in the [DatePickerDialog] with the given [day] and [pickerTag]. The [pickerTag]
   * is used to find the [DatePickerDialog] in the test.
   */
  private fun selectDate(day: Int, pickerTag: String) {
    composeTestRule.onNodeWithTag(pickerTag).assertIsDisplayed()

    val currentDate = LocalDate.now()
    val dateToSelect = LocalDate.of(currentDate.year, currentDate.month, day)
    val dateString = dateToSelect.format(dateFormatter)

    composeTestRule.onNodeWithText(dateString).performClick()

    composeTestRule
        .onNodeWithText(context.getString(R.string.event_creation_dialog_ok))
        .performClick()
  }

  /**
   * Selects a time in the [DatePickerDialog] with the given [hour], [minute], and [pickerTag]. The
   * [pickerTag] is used to find the [DatePickerDialog] in the test.
   */
  private fun selectTime(hour: Int, minute: Int, pickerTag: String) {
    composeTestRule.onNodeWithTag(pickerTag).assertIsDisplayed()

    val hourNodeInteraction =
        composeTestRule.onNode(
            hasContentDescription(
                "for hour"), // The content description of the hour picker used by Material3
            useUnmergedTree = true)

    hourNodeInteraction.performTextClearance()
    hourNodeInteraction.performTextInput(hour.toString())

    val minutesNodeInteraction =
        composeTestRule.onNode(
            hasContentDescription(
                "for minutes"), // The content description of the minute picker used by Material3
            useUnmergedTree = true)

    minutesNodeInteraction.performTextClearance()
    minutesNodeInteraction.performTextInput(minute.toString())

    composeTestRule
        .onNodeWithText(context.getString(R.string.event_creation_dialog_ok))
        .performClick()
  }

  private fun setDateTime(
      dateFieldTag: String,
      datePickerTag: String,
      timeFieldTag: String,
      timePickerTag: String,
      day: Int,
      hour: Int,
      minute: Int
  ) {
    composeTestRule.onNodeWithTag(dateFieldTag).performScrollTo().performClick()
    selectDate(day, datePickerTag)

    composeTestRule.onNodeWithTag(timeFieldTag).performScrollTo().performClick()
    selectTime(hour, minute, timePickerTag)
  }

  @Test
  fun testEventCreation() {
    // Sign in with the admin user
    signInWithUser(composeTestRule, Admin.EMAIL, Admin.PASSWORD)

    // Navigate to the event creation screen
    composeTestRule.waitUntil(10000) {
      composeTestRule.onNodeWithTag(HomeTestTags.SCREEN).isDisplayed()
    }

    // Navigate to the Explore screen
    composeTestRule.onNodeWithTag(BottomNavBarTestTags.EXPLORE).assertIsDisplayed()
    composeTestRule.onNodeWithTag(BottomNavBarTestTags.EXPLORE).performClick()
    composeTestRule.waitUntil(10000) {
      composeTestRule.onNodeWithTag(ExploreTestTags.EXPLORE_SCAFFOLD_TITLE).isDisplayed()
    }

    // Navigate to the "Ebou" Association Profile screen
    composeTestRule.onNodeWithText(ASSOCIATION_NAME).assertDisplayComponentInScroll()
    composeTestRule.onNodeWithText(ASSOCIATION_NAME).performClick()
    composeTestRule.waitUntil(10000) {
      composeTestRule.onNodeWithTag(AssociationProfileTestTags.SCREEN).isDisplayed()
    }

    // Click on the "Add Event" button
    composeTestRule.onNodeWithTag(AssociationProfileTestTags.ADD_EVENT_BUTTON).performClick()
    composeTestRule.waitUntil(10000) {
      composeTestRule.onNodeWithTag(EventCreationTestTags.SCREEN).isDisplayed()
    }

    // Fill in the event creation form
    composeTestRule
        .onNodeWithTag(EventCreationTestTags.EVENT_TITLE)
        .performScrollTo()
        .performTextInput(EVENT_TITLE)

    composeTestRule
        .onNodeWithTag(EventCreationTestTags.SHORT_DESCRIPTION)
        .performScrollTo()
        .performTextInput(EVENT_SHORT_DESCRIPTION)

    composeTestRule
        .onNodeWithTag(EventCreationTestTags.DESCRIPTION)
        .performScrollTo()
        .performTextInput(EVENT_DESCRIPTION)

    // Handle the image picker
    val context = InstrumentationRegistry.getInstrumentation().targetContext
    val resourceId = R.drawable.chooseyourcoach
    val fakeImageUri = Uri.parse("android.resource://${context.packageName}/$resourceId")
    val resultData = Intent()
    resultData.data = fakeImageUri

    intending(anyIntent())
        .respondWith(Instrumentation.ActivityResult(Activity.RESULT_OK, resultData))

    // Click on the image picker
    composeTestRule
        .onNodeWithTag(EventCreationTestTags.EVENT_IMAGE)
        .performScrollTo()
        .performClick()

    // Set Start Date and Time
    setDateTime(
        dateFieldTag = EventCreationTestTags.START_DATE_FIELD,
        datePickerTag = EventCreationTestTags.START_DATE_PICKER,
        timeFieldTag = EventCreationTestTags.START_TIME_FIELD,
        timePickerTag = EventCreationTestTags.START_TIME_PICKER,
        day = 15,
        hour = 10,
        minute = 30)

    // Set End Date and Time
    setDateTime(
        dateFieldTag = EventCreationTestTags.END_DATE_FIELD,
        datePickerTag = EventCreationTestTags.END_DATE_PICKER,
        timeFieldTag = EventCreationTestTags.END_TIME_FIELD,
        timePickerTag = EventCreationTestTags.END_TIME_PICKER,
        day = 16,
        hour = 11,
        minute = 25)

    // Select a mocked location with the mocked web client
    val query = "Test Query"

    mockWebServer.enqueue(
        MockResponse().setBody(mockResponseBody).setResponseCode(HttpURLConnection.HTTP_OK))

    // Write the query in the Location input field.
    composeTestRule
        .onNodeWithTag(EventCreationTestTags.LOCATION)
        .performScrollTo()
        .performTextClearance()
    composeTestRule.onNodeWithTag(EventCreationTestTags.LOCATION).performTextInput(query)

    // Wait for the location suggestions to load and select it.
    composeTestRule.waitUntil(10000) {
      composeTestRule
          .onNodeWithTag(EventCreationTestTags.LOCATION_SUGGESTION_ITEM_LATITUDE + EVENT_LATITUDE)
          .performScrollTo()
          .isDisplayed()
    }

    composeTestRule
        .onNodeWithTag(EventCreationTestTags.LOCATION_SUGGESTION_ITEM_LATITUDE + EVENT_LATITUDE)
        .performScrollTo()
        .performClick()

    composeTestRule.waitForIdle()

    // Assert that the location has been correctly chosen
    composeTestRule
        .onNodeWithTag(EventCreationTestTags.LOCATION, useUnmergedTree = true)
        .assertTextEquals(EVENT_FORMATTED_ADDRESS, includeEditableText = true)

    // Submit the event
    composeTestRule.waitForIdle()
    composeTestRule
        .onNodeWithTag(EventCreationTestTags.SAVE_BUTTON)
        .performScrollTo()
        .performClick()

    // Go back to the Home screen
    composeTestRule.waitUntil(10000) {
      composeTestRule.onNodeWithTag(AssociationProfileTestTags.SCREEN).isDisplayed()
    }

    composeTestRule.onNodeWithTag(AssociationProfileTestTags.GO_BACK_BUTTON).performClick()

    composeTestRule.waitUntil(10000) {
      composeTestRule.onNodeWithTag(ExploreTestTags.EXPLORE_SCAFFOLD_TITLE).isDisplayed()
    }

    // Navigate to the Home screen
    composeTestRule.onNodeWithTag(BottomNavBarTestTags.HOME).performClick()
    composeTestRule.waitUntil(10000) {
      composeTestRule.onNodeWithTag(HomeTestTags.SCREEN).isDisplayed()
    }

    // Wait for the event to load
    composeTestRule.waitForIdle()

    // Scroll to the event in the list
    composeTestRule.onNodeWithTag(HomeTestTags.EVENT_LIST).performScrollToNode(hasText(EVENT_TITLE))

    // Assert that the event is displayed
    composeTestRule.onNodeWithText(EVENT_TITLE).assertIsDisplayed()
    composeTestRule.onNodeWithText(EVENT_SHORT_DESCRIPTION).assertIsDisplayed()

    // Assert that the rest of the details are displayed
    composeTestRule.onNodeWithText(EVENT_TITLE).performScrollTo().performClick()
    composeTestRule.onNodeWithText(EVENT_DESCRIPTION).assertIsDisplayed()
    composeTestRule.onNodeWithText(EVENT_FORMATTED_ADDRESS).assertIsDisplayed()

    // Go back to the Home screen
    composeTestRule.onNodeWithTag(EventDetailsTestTags.GO_BACK_BUTTON).performClick()
    composeTestRule.waitUntil(10000) {
      composeTestRule.onNodeWithTag(HomeTestTags.SCREEN).isDisplayed()
    }

    // Sign out
    signOutWithUser(composeTestRule)
  }

  private companion object TestStrings {
    const val ASSOCIATION_NAME = "Ebou"

    const val EVENT_TITLE = "Test Event"
    const val EVENT_SHORT_DESCRIPTION = "This is a short description."
    const val EVENT_DESCRIPTION = "This is a detailed description of the test event."
    const val EVENT_FORMATTED_ADDRESS = "Test Road, 123, 12345 Test City, Test State, Test Country"
    const val EVENT_LATITUDE = "45.512331"
  }

  @Module
  @InstallIn(SingletonComponent::class)
  abstract class TestNetworkModule {

    companion object {
      @Provides
      @Singleton
      fun provideMockWebServer(): MockWebServer {
        return MockWebServer()
      }

      @Provides
      @Singleton
      fun provideNominatimApiService(mockWebServer: MockWebServer): NominatimApiService {
        return Retrofit.Builder()
            .baseUrl(mockWebServer.url("/"))
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(NominatimApiService::class.java)
      }
    }

    @Binds
    @Singleton
    abstract fun bindLocationRepository(
        nominatimLocationRepository: NominatimLocationRepository
    ): LocationRepository
  }
}
