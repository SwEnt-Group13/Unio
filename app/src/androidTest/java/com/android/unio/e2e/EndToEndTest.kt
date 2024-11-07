package com.android.unio.e2e

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.isDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import androidx.test.filters.LargeTest
import com.android.unio.MainActivity
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import com.google.firebase.firestore.firestore
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject
import org.junit.Before
import org.junit.Rule
import org.junit.Test

@LargeTest
class EndToEndTest {
  @get:Rule val composeTestRule = createAndroidComposeRule<MainActivity>()

  @Before
  fun setUp() {
    Firebase.firestore.useEmulator("10.0.2.2", 8080)
    Firebase.auth.useEmulator("10.0.2.2", 9099)
  }

  @Test
  fun `Test account creation flow`() {
    /** Create an account on the welcome screen */
    composeTestRule.onNodeWithTag("WelcomeScreen").assertIsDisplayed()
    composeTestRule.onNodeWithTag("WelcomeEmail").performTextInput(EMAIL)
    composeTestRule.onNodeWithTag("WelcomePassword").performTextInput(PWD)

    composeTestRule.onNodeWithTag("WelcomeButton").performClick()

    composeTestRule.waitForIdle()

    /** Verify the email */
    val emailVerificationUrl = getLatestEmailVerificationUrl()
    verifyEmail(emailVerificationUrl)

    // This sleep is required to wait for the email verification to complete
    Thread.sleep(5000)

    /** Refresh the email verification and continue */
    composeTestRule.onNodeWithTag("EmailVerificationScreen").assertIsDisplayed()
    composeTestRule.onNodeWithTag("EmailVerificationRefresh").performClick()

    composeTestRule.waitForIdle()

    composeTestRule.onNodeWithTag("EmailVerificationContinue").performClick()
    composeTestRule.onNodeWithTag("AccountDetails").assertIsDisplayed()

    composeTestRule.waitForIdle()

    /** Fill in the account details */
    composeTestRule.onNodeWithTag("AccountDetailsFirstNameTextField").performTextInput(FIRST_NAME)
    composeTestRule.onNodeWithTag("AccountDetailsLastNameTextField").performTextInput(LAST_NAME)
    composeTestRule.onNodeWithTag("AccountDetailsBioTextField").performTextInput(BIOGRAPHY)
    composeTestRule.onNodeWithTag("AccountDetailsInterestsButton").performClick()
    composeTestRule.onNodeWithTag("InterestOverlayClickableRow: 0").performClick()
    composeTestRule.onNodeWithTag("InterestOverlayClickableRow: 1").performClick()
    composeTestRule.onNodeWithTag("InterestOverlayClickableRow: 2").performClick()
    composeTestRule.onNodeWithTag("InterestsOverlayContinueButton").performClick()

    composeTestRule.onNodeWithTag("AccountDetailsContinueButton").performClick()

    // Wait until "HomeScreen" is displayed
    composeTestRule.waitUntil(10000) {
      composeTestRule.onNodeWithTag("HomeScreen").isDisplayed()
    }

    /** Navigate to the profile screen */
    composeTestRule.onNodeWithTag("My Profile").performClick()

    composeTestRule.waitForIdle()

    composeTestRule.onNodeWithTag("UserProfileScreen").assertIsDisplayed()

    Thread.sleep(10000)
  }

  private fun getLatestEmailVerificationUrl(): String {
    val client = OkHttpClient()

    val oobRequest = Request.Builder()
      .url(OOB_URL)
      .build();

    val response = client.newCall(oobRequest).execute()

    val data = response.body?.string()
    val json = JSONObject(data ?: "")
    val codes = json.getJSONArray("oobCodes")
    return codes.getJSONObject(codes.length() - 1).getString("oobLink")
  }

  private fun verifyEmail(url: String) {
    val client = OkHttpClient()

    val request = Request.Builder()
      .url(url.replace("127.0.0.1", "10.0.2.2"))
      .build()

    client.newCall(request).execute()
  }

  companion object {
    const val EMAIL = "alexeithornber@gmail.com"
    const val PWD = "123456"

    const val FIRST_NAME = "Alexei"
    const val LAST_NAME = "Thornber"
    const val BIOGRAPHY = "I am a software engineer"

    const val OOB_URL = "http://10.0.2.2:9099/emulator/v1/projects/unio-1b8ee/oobCodes"
  }
}