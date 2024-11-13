package com.android.unio.ui.authentication

import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertTextContains
import androidx.compose.ui.test.isDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import androidx.test.filters.LargeTest
import com.android.unio.MainActivity
import com.android.unio.model.hilt.module.FirebaseAuthModule
import com.android.unio.model.hilt.module.FirebaseModule
import com.android.unio.model.strings.test_tags.AccountDetailsTestTags
import com.android.unio.model.strings.test_tags.BottomNavBarTestTags
import com.android.unio.model.strings.test_tags.EmailVerificationTestTags
import com.android.unio.model.strings.test_tags.HomeTestTags
import com.android.unio.model.strings.test_tags.InterestsOverlayTestTags
import com.android.unio.model.strings.test_tags.UserProfileTestTags
import com.android.unio.model.strings.test_tags.WelcomeTestTags
import com.android.unio.ui.flushAuthenticationClients
import com.android.unio.ui.flushFirestoreDatabase
import com.android.unio.ui.verifyEmulatorsAreRunning
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.firestore
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import dagger.hilt.android.testing.UninstallModules
import dagger.hilt.components.SingletonComponent
import io.mockk.clearAllMocks
import io.mockk.unmockkAll
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test

@LargeTest
@HiltAndroidTest
@UninstallModules(FirebaseModule::class, FirebaseAuthModule::class)
class UserAccountCreationTest {
  @get:Rule val composeTestRule = createAndroidComposeRule<MainActivity>()
  @get:Rule val hiltRule = HiltAndroidRule(this)

  @Module
  @InstallIn(SingletonComponent::class)
  object FirebaseModule {

    @Provides
    fun provideFirebaseFirestore(): FirebaseFirestore {
      Firebase.firestore.useEmulator("10.0.2.2", 8080)
      return Firebase.firestore
    }
  }

  @Module
  @InstallIn(SingletonComponent::class)
  object FirebaseAuthModule {

    @Provides
    fun provideFirebaseAuth(): FirebaseAuth {
      Firebase.auth.useEmulator("10.0.2.2", 9099)
      return FirebaseAuth.getInstance()
    }
  }

  @Before
  fun setUp() {

    hiltRule.inject()
    /*Test that the emulators are indeed running*/
    verifyEmulatorsAreRunning()
    flushAuthenticationClients()
    flushFirestoreDatabase()
  }

  @Test
  fun testUserCanLoginAndCreateAnAccount() {
    /** Create an account on the welcome screen */
    composeTestRule.onNodeWithTag(WelcomeTestTags.SCREEN).assertIsDisplayed()
    composeTestRule.onNodeWithTag(WelcomeTestTags.EMAIL).performTextInput(EMAIL)
    composeTestRule.onNodeWithTag(WelcomeTestTags.PASSWORD).performTextInput(PWD)

    composeTestRule.onNodeWithTag(WelcomeTestTags.BUTTON).performClick()

    Thread.sleep(5000)

    /** Verify the email */
    val emailVerificationUrl = getLatestEmailVerificationUrl()
    verifyEmail(emailVerificationUrl)

    // This sleep is required to wait for the email verification to complete
    Thread.sleep(5000)

    /** Refresh the email verification and continue */
    composeTestRule.onNodeWithTag(EmailVerificationTestTags.SCREEN).assertIsDisplayed()
    composeTestRule.onNodeWithTag(EmailVerificationTestTags.REFRESH).performClick()

    //    Thread.sleep(5000)

    composeTestRule.onNodeWithTag(EmailVerificationTestTags.CONTINUE).performClick()
    composeTestRule.onNodeWithTag(AccountDetailsTestTags.TITLE_TEXT).assertExists()

    /** Fill in the account details */
    composeTestRule
        .onNodeWithTag(AccountDetailsTestTags.FIRST_NAME_TEXT_FIELD)
        .performTextInput(FIRST_NAME)
    composeTestRule
        .onNodeWithTag(AccountDetailsTestTags.LAST_NAME_TEXT_FIELD)
        .performTextInput(LAST_NAME)
    composeTestRule
        .onNodeWithTag(AccountDetailsTestTags.BIOGRAPHY_TEXT_FIELD)
        .performTextInput(BIOGRAPHY)
    composeTestRule.onNodeWithTag(AccountDetailsTestTags.INTERESTS_BUTTON).performClick()
    composeTestRule.onNodeWithTag(InterestsOverlayTestTags.CLICKABLE_ROW + "0").performClick()
    composeTestRule.onNodeWithTag(InterestsOverlayTestTags.CLICKABLE_ROW + "1").performClick()
    composeTestRule.onNodeWithTag(InterestsOverlayTestTags.CLICKABLE_ROW + "2").performClick()
    composeTestRule.onNodeWithTag(InterestsOverlayTestTags.SAVE_BUTTON).performClick()

    composeTestRule.onNodeWithTag(AccountDetailsTestTags.CONTINUE_BUTTON).performClick()

    // Wait until "HomeScreen" is displayed
    composeTestRule.waitUntil { composeTestRule.onNodeWithTag(HomeTestTags.SCREEN).isDisplayed() }

    // Wait until the bottom nav bar is displayed
    composeTestRule.waitUntil(10000) {
      composeTestRule.onNodeWithTag(BottomNavBarTestTags.MY_PROFILE).isDisplayed()
    }

    /** Navigate to the profile screen */
    composeTestRule.onNodeWithTag(BottomNavBarTestTags.MY_PROFILE).performClick()

    composeTestRule.waitUntil {
      composeTestRule.onNodeWithTag(UserProfileTestTags.SCREEN).isDisplayed()
    }

    composeTestRule.onNodeWithTag(UserProfileTestTags.SCREEN).assertIsDisplayed()
    composeTestRule
        .onNodeWithTag(UserProfileTestTags.NAME)
        .assertTextContains("$FIRST_NAME $LAST_NAME")
    composeTestRule.onNodeWithTag(UserProfileTestTags.BIOGRAPHY).assertTextContains(BIOGRAPHY)
    composeTestRule.onAllNodesWithTag(UserProfileTestTags.INTEREST).assertCountEquals(3)
  }

  private fun getLatestEmailVerificationUrl(): String {
    val client = OkHttpClient()

    val oobRequest = Request.Builder().url(OOB_URL).build()

    val response = client.newCall(oobRequest).execute()

    val data = response.body?.string()
    val json = JSONObject(data ?: "")
    val codes = json.getJSONArray("oobCodes")
    return codes.getJSONObject(codes.length() - 1).getString("oobLink")
  }

  private fun verifyEmail(url: String) {
    val client = OkHttpClient()

    val request = Request.Builder().url(url.replace("127.0.0.1", "10.0.2.2")).build()

    client.newCall(request).execute()
  }

  companion object {
    const val EMAIL = "ishinzqyR6S@gmail.com"
    const val PWD = "123456"

    const val FIRST_NAME = "John"
    const val LAST_NAME = "Doe"
    const val BIOGRAPHY = "I am a software engineer"

    const val OOB_URL = "http://10.0.2.2:9099/emulator/v1/projects/unio-1b8ee/oobCodes"
    const val FIRESTORE_URL = "http://10.0.2.2:8080"
    const val FLUSH_FIRESTORE_URL =
        "http://10.0.2.2:8080/emulator/v1/projects/unio-1b8ee/databases/(default)/documents"
    const val FLUSH_AUTH_URL = "http://10.0.2.2:9099/emulator/v1/projects/unio-1b8ee/accounts"
  }

  @After
  fun tearDown() {
    clearAllMocks()
    unmockkAll()
  }
}
