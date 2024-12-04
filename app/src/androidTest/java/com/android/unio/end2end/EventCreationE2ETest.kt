package com.android.unio.end2end

import android.app.Activity
import android.app.Instrumentation
import android.content.Intent
import android.net.Uri
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertTextEquals
import androidx.compose.ui.test.isDisplayed
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.test.performTextClearance
import androidx.compose.ui.test.performTextInput
import androidx.test.espresso.intent.Intents
import androidx.test.espresso.intent.Intents.intending
import androidx.test.espresso.intent.matcher.IntentMatchers.anyIntent
import androidx.test.filters.LargeTest
import com.android.unio.assertDisplayComponentInScroll
import com.android.unio.model.hilt.module.NetworkModule
import com.android.unio.model.map.LocationRepository
import com.android.unio.model.map.nominatim.NominatimApiService
import com.android.unio.model.map.nominatim.NominatimLocationRepository
import com.android.unio.model.strings.test_tags.AssociationProfileTestTags
import com.android.unio.model.strings.test_tags.BottomNavBarTestTags
import com.android.unio.model.strings.test_tags.EventCreationTestTags
import com.android.unio.model.strings.test_tags.ExploreTestTags
import com.android.unio.model.strings.test_tags.HomeTestTags
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.testing.HiltAndroidTest
import dagger.hilt.android.testing.UninstallModules
import dagger.hilt.components.SingletonComponent
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
  private val mockWebServer: MockWebServer = MockWebServer()

  @Before
  override fun setUp() {
      super.setUp()
    mockWebServer.start()

    val mockResponseBody =
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

  private fun selectDate(datePickerTag: String, day: String) {
    composeTestRule.waitUntil(10000) {
      composeTestRule.onNodeWithTag(datePickerTag, useUnmergedTree = true).isDisplayed()
    }

    composeTestRule.onNodeWithText(day, useUnmergedTree = true).performClick()

    composeTestRule.onNodeWithText("OK", ignoreCase = true).performClick()
  }

  private fun selectTime(timePickerTag: String, hour: Int, minute: Int) {
    composeTestRule.waitUntil(10000) { composeTestRule.onNodeWithTag(timePickerTag).isDisplayed() }

    composeTestRule.onNodeWithText("Hour", useUnmergedTree = true).performTextClearance()
    composeTestRule.onNodeWithText("Hour", useUnmergedTree = true).performTextInput(hour.toString())

    composeTestRule.onNodeWithText("Minute", useUnmergedTree = true).performTextClearance()
    composeTestRule
        .onNodeWithText("Minute", useUnmergedTree = true)
        .performTextInput(minute.toString())

    composeTestRule.onNodeWithText("OK", ignoreCase = true).performClick()
  }

  private fun setDateTime(
      dateFieldTag: String,
      datePickerTag: String,
      timeFieldTag: String,
      timePickerTag: String,
      day: String,
      hour: Int,
      minute: Int
  ) {
    composeTestRule.onNodeWithTag(dateFieldTag).performScrollTo().performClick()
    selectDate(datePickerTag, day)

    composeTestRule.onNodeWithTag(timeFieldTag).performScrollTo().performClick()
    selectTime(timePickerTag, hour, minute)
  }

  @Test
  fun testEventCreation() {
    signInWithUser(composeTestRule, JohnDoe.EMAIL, JohnDoe.PASSWORD)

    // Navigate to the event creation screen
    composeTestRule.waitUntil(10000) {
      composeTestRule.onNodeWithTag(HomeTestTags.SCREEN).isDisplayed()
    }

    composeTestRule.onNodeWithTag(BottomNavBarTestTags.EXPLORE).assertIsDisplayed()
    composeTestRule.onNodeWithTag(BottomNavBarTestTags.EXPLORE).performClick()
    composeTestRule.waitUntil(10000) {
      composeTestRule.onNodeWithTag(ExploreTestTags.EXPLORE_SCAFFOLD_TITLE).isDisplayed()
    }

    composeTestRule.onNodeWithText(ASSOCIATION_NAME).assertDisplayComponentInScroll()
    composeTestRule.onNodeWithText(ASSOCIATION_NAME).performClick()
    composeTestRule.waitUntil(10000) {
      composeTestRule.onNodeWithTag(AssociationProfileTestTags.SCREEN).isDisplayed()
    }

    composeTestRule.onNodeWithTag(AssociationProfileTestTags.ADD_EVENT_BUTTON).performClick()
    composeTestRule.waitUntil(10000) {
      composeTestRule.onNodeWithTag(EventCreationTestTags.SCREEN).isDisplayed()
    }

    // Fill in the event creation form
    composeTestRule.onNodeWithTag(EventCreationTestTags.EVENT_TITLE).performTextInput("Test Event")

    composeTestRule
        .onNodeWithTag(EventCreationTestTags.SHORT_DESCRIPTION)
        .performTextInput("This is a short description.")

    composeTestRule
        .onNodeWithTag(EventCreationTestTags.DESCRIPTION)
        .performTextInput("This is a detailed description of the test event.")

    // Handle the image picker
    val fakeImageUri = Uri.parse("android.resource://com.android.unio/drawable/test_image")
    val resultData = Intent()
    resultData.data = fakeImageUri

    intending(anyIntent())
        .respondWith(Instrumentation.ActivityResult(Activity.RESULT_OK, resultData))

    // Click on the image picker
    composeTestRule.onNodeWithTag(EventCreationTestTags.EVENT_IMAGE).performClick()

    // Set Start Date and Time
    setDateTime(
        dateFieldTag = EventCreationTestTags.START_DATE_FIELD,
        datePickerTag = EventCreationTestTags.START_DATE_PICKER,
        timeFieldTag = EventCreationTestTags.START_TIME_FIELD,
        timePickerTag = EventCreationTestTags.START_TIME_PICKER,
        day = "15",
        hour = 10,
        minute = 30)

    setDateTime(
        dateFieldTag = EventCreationTestTags.END_DATE_FIELD,
        datePickerTag = EventCreationTestTags.END_DATE_PICKER,
        timeFieldTag = EventCreationTestTags.END_TIME_FIELD,
        timePickerTag = EventCreationTestTags.END_TIME_PICKER,
        day = "15",
        hour = 12,
        minute = 0)

    // Select a mocked location with the mocked web client
    val query = "Test Query"
    composeTestRule.onNodeWithTag(EventCreationTestTags.LOCATION).performTextClearance()
    composeTestRule.onNodeWithTag(EventCreationTestTags.LOCATION).performTextInput(query)

    composeTestRule.waitUntil(10000) {
      composeTestRule
          .onNodeWithTag(EventCreationTestTags.LOCATION_SUGGESTION_ITEM + "45.512331")
          .isDisplayed()
    }
    composeTestRule
        .onNodeWithTag(EventCreationTestTags.LOCATION_SUGGESTION_ITEM + "45.512331")
        .performClick()

    composeTestRule
        .onNodeWithTag(EventCreationTestTags.LOCATION)
        .assertTextEquals("Test Road, 123, 12345 Test City, Test State, Test Country")

    composeTestRule.onNodeWithTag(EventCreationTestTags.SAVE_BUTTON).performClick()

    // Verify that the event appears on the Home screen

    signOutWithUser(composeTestRule)
  }

  private companion object AssociationTarget {
    const val ASSOCIATION_NAME = "Ebou"
    const val ASSOCIATION_MEMBERS = "Renata Mendoza Flores"
  }

  @Module
  @InstallIn(SingletonComponent::class)
  abstract class TestNetworkModule {

    companion object {
      @Provides
      @Singleton
      fun provideNominatimApiService(): NominatimApiService {
        return Retrofit.Builder()
            .baseUrl("http://127.0.0.1:8080/")
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
