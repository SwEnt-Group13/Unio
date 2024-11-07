package com.android.unio.ui.authentication

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertTextContains
import androidx.compose.ui.test.isDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import androidx.test.filters.LargeTest
import com.android.unio.MainActivity
import com.google.api.AnnotationsProto.http
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import com.google.firebase.firestore.firestore
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import java.io.File

@LargeTest
class EndToEndTest {
  @get:Rule val composeTestRule = createAndroidComposeRule<MainActivity>()

  @Before
  fun setUp() {
    /*Test that the emulators are indeed running*/
    verifyEmulatorsAreRunning()

    Firebase.firestore.useEmulator("10.0.2.2", 8080)
    Firebase.auth.useEmulator("10.0.2.2", 9099)

  }

  @Test
  fun testUserCanLoginAndCreateAnAccount() {
    flushFirestoreDatabase()
    flushAuthenticationClients()

    Thread.sleep(3000)

    /** Create an account on the welcome screen */
    composeTestRule.onNodeWithTag("WelcomeScreen").assertIsDisplayed()
    composeTestRule.onNodeWithTag("WelcomeEmail").performTextInput(EMAIL)
    composeTestRule.onNodeWithTag("WelcomePassword").performTextInput(PWD)

    composeTestRule.onNodeWithTag("WelcomeButton").performClick()

    composeTestRule.waitForIdle()
    Thread.sleep(1000)

    /** Verify the email */
    val emailVerificationUrl = getLatestEmailVerificationUrl()
    verifyEmail(emailVerificationUrl)

    // This sleep is required to wait for the email verification to complete
    Thread.sleep(5000)

    /** Refresh the email verification and continue */
    composeTestRule.onNodeWithTag("EmailVerificationScreen").assertIsDisplayed()
    Thread.sleep(1000)
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
    composeTestRule.waitUntil(2000) {
      composeTestRule.onNodeWithTag("HomeScreen").isDisplayed()
    }

    /** Navigate to the profile screen */
    composeTestRule.onNodeWithTag("My Profile").performClick()

    composeTestRule.waitForIdle()

    composeTestRule.onNodeWithTag("UserProfileScreen").assertIsDisplayed()
    composeTestRule.onNodeWithTag("UserProfileName").assertTextContains("$FIRST_NAME $LAST_NAME")
    composeTestRule.onNodeWithTag("UserProfileBiography").assertTextContains(BIOGRAPHY)
    composeTestRule.onNodeWithTag("UserProfileInterest: SPORTS", useUnmergedTree = true).assertIsDisplayed()
    composeTestRule.onNodeWithTag("UserProfileInterest: MUSIC", useUnmergedTree = true).assertIsDisplayed()
    composeTestRule.onNodeWithTag("UserProfileInterest: ART", useUnmergedTree = true).assertIsDisplayed()


    Thread.sleep(10000)
  }

  private fun verifyEmulatorsAreRunning(){
    val client = OkHttpClient()
    val request = Request.Builder()
      .url(FIRESTORE_URL)
      .build()

    val response = client.newCall(request).execute()
    val data = response.body?.string()
    assert(data!!.contains("Ok")){"Start your emulators before running the end to end test"}
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

  private fun flushAuthenticationClients(){
    val client = OkHttpClient()

    val request = Request.Builder()
      .url(FLUSH_AUTH_URL)
      .delete()
      .build()

    client.newCall(request).execute()
  }

  private fun flushFirestoreDatabase(){
    val client = OkHttpClient()

    val request = Request.Builder()
      .url(FLUSH_FIRESTORE_URL)
      .delete()
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
    const val FIRESTORE_URL = "http://10.0.2.2:8080"
    const val FLUSH_FIRESTORE_URL = "http://10.0.2.2:8080/emulator/v1/projects/unio-1b8ee/databases/(default)/documents"
    const val FLUSH_AUTH_URL = "http://10.0.2.2:9099/emulator/v1/projects/unio-1b8ee/accounts"
  }
}