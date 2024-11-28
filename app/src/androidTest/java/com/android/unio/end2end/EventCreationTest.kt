package com.android.unio.end2end

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertTextEquals
import androidx.compose.ui.test.isDisplayed
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextClearance
import androidx.compose.ui.test.performTextInput
import androidx.test.filters.LargeTest
import com.android.unio.assertDisplayComponentInScroll
import com.android.unio.model.strings.test_tags.AssociationProfileTestTags
import com.android.unio.model.strings.test_tags.BottomNavBarTestTags
import com.android.unio.model.strings.test_tags.EventCreationTestTags
import com.android.unio.model.strings.test_tags.ExploreTestTags
import com.android.unio.model.strings.test_tags.HomeTestTags
import dagger.hilt.android.testing.HiltAndroidTest
import org.junit.After
import org.junit.Before
import org.junit.Test

@LargeTest
@HiltAndroidTest
// @UninstallModules(NetworkModule::class)
class EventCreationTest : EndToEndTest() {

  // @Inject lateinit var server: MockWebServer

  @Before
  override fun setUp() {
    hiltRule.inject()

    // server.start()

    super.setUp()
  }

  @After
  override fun tearDown() {
    // server.shutdown()

    super.tearDown()
  }

  @Test
  fun testEventCreation() {
    //        server.enqueue(
    //            MockResponse()
    //                .setBody(
    //                    """
    //                    [
    //                        {
    //                            "lat": "45.512331",
    //                            "lon": "7.559331",
    //                            "display_name": "Mocked Location, Test City, Test Country",
    //                            "address": {
    //                                "road": "Mocked Road",
    //                                "house_number": "123",
    //                                "postcode": "12345",
    //                                "city": "Test City",
    //                                "state": "Test State",
    //                                "country": "Test Country"
    //                            }
    //                        }
    //                    ]
    //                    """
    //                )
    //                .setResponseCode(HttpURLConnection.HTTP_OK)
    //        )

    signInWithUser(composeTestRule, JohnDoe.EMAIL, JohnDoe.PASSWORD)

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

    composeTestRule.onNodeWithTag(EventCreationTestTags.EVENT_TITLE).assertIsDisplayed()
    composeTestRule.onNodeWithTag(EventCreationTestTags.EVENT_TITLE).performTextClearance()
    composeTestRule.onNodeWithTag(EventCreationTestTags.EVENT_TITLE).performTextInput("Test Event")

    composeTestRule.onNodeWithTag(EventCreationTestTags.SHORT_DESCRIPTION).assertIsDisplayed()
    composeTestRule.onNodeWithTag(EventCreationTestTags.SHORT_DESCRIPTION).performTextClearance()
    composeTestRule
        .onNodeWithTag(EventCreationTestTags.SHORT_DESCRIPTION)
        .performTextInput("Test Event Short Description")

    composeTestRule.onNodeWithTag(EventCreationTestTags.DESCRIPTION).assertIsDisplayed()
    composeTestRule.onNodeWithTag(EventCreationTestTags.DESCRIPTION).performTextClearance()
    composeTestRule
        .onNodeWithTag(EventCreationTestTags.DESCRIPTION)
        .performTextInput("Test Event Description")

    composeTestRule
        .onNodeWithTag(EventCreationTestTags.START_TIME)
        .assertIsDisplayed()
        .performClick()

    // Simulate selecting a start date and time (adjust for actual implementation)
    composeTestRule.onNodeWithText("Set Date").performClick()
    composeTestRule.onNodeWithText("2024-11-27").performClick()
    composeTestRule.onNodeWithText("Set Time").performClick()
    composeTestRule.onNodeWithText("12:00 PM").performClick()

    // Verify that the start timestamp is updated
    composeTestRule
        .onNodeWithTag(EventCreationTestTags.START_TIME)
        .assertTextEquals("2024-11-27 12:00 PM")

    // Repeat for the end date picker
    composeTestRule.onNodeWithTag(EventCreationTestTags.END_TIME).assertIsDisplayed().performClick()

    composeTestRule.onNodeWithText("Set Date").performClick()
    composeTestRule.onNodeWithText("2024-11-28").performClick()
    composeTestRule.onNodeWithText("Set Time").performClick()
    composeTestRule.onNodeWithText("03:00 PM").performClick()

    // Verify that the end timestamp is updated
    composeTestRule
        .onNodeWithTag(EventCreationTestTags.END_TIME)
        .assertTextEquals("2024-11-28 03:00 PM")

    // Verify error messages if any
    composeTestRule.onNodeWithTag(EventCreationTestTags.ERROR_TEXT1).assertIsDisplayed()
    composeTestRule.onNodeWithTag(EventCreationTestTags.ERROR_TEXT2).assertDoesNotExist()

    // Web server is mocked
    composeTestRule.onNodeWithTag(EventCreationTestTags.LOCATION).assertIsDisplayed()
    composeTestRule.onNodeWithTag(EventCreationTestTags.LOCATION).performTextClearance()
    composeTestRule
        .onNodeWithTag(EventCreationTestTags.LOCATION)
        .performTextInput("Test Event Location")

    signOutWithUser(composeTestRule)
  }

  private companion object AssociationTarget {
    const val ASSOCIATION_NAME = "Ebou"
    const val ASSOCIATION_MEMBERS = "Renata Mendoza Flores"
  }

  //    @Module
  //    @InstallIn(SingletonComponent::class)
  //    abstract class TestNetworkModule {
  //        companion object {
  //            @Provides
  //            @Singleton
  //            fun provideMockNominatimApiService(server: MockWebServer): NominatimApiService {
  //                return Retrofit.Builder()
  //                    .baseUrl(server.url("/"))
  //                    .addConverterFactory(GsonConverterFactory.create())
  //                    .build()
  //                    .create(NominatimApiService::class.java)
  //            }
  //
  //            @Provides
  //            @Singleton
  //            fun provideMockWebServer(): MockWebServer {
  //                return MockWebServer()
  //            }
  //        }
  //
  //        @Binds
  //        @Singleton
  //        abstract fun bindLocationRepository(
  //            nominatimLocationRepository: NominatimLocationRepository
  //        ): LocationRepository
  //    }

}
