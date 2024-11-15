package com.android.unio.ui.end2end

import android.util.Log
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
import com.android.unio.model.strings.test_tags.AccountDetailsTestTags
import com.android.unio.model.strings.test_tags.BottomNavBarTestTags
import com.android.unio.model.strings.test_tags.EmailVerificationTestTags
import com.android.unio.model.strings.test_tags.HomeTestTags
import com.android.unio.model.strings.test_tags.InterestsOverlayTestTags
import com.android.unio.model.strings.test_tags.UserProfileTestTags
import com.android.unio.ui.assertDisplayComponentInScroll
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import io.mockk.clearAllMocks
import io.mockk.unmockkAll
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject
import org.junit.After
import org.junit.Rule
import org.junit.Test

@LargeTest
@HiltAndroidTest
class UserAccountCreationTest : EndToEndTest() {
  @get:Rule val composeTestRule = createAndroidComposeRule<MainActivity>()
  @get:Rule val hiltRule = HiltAndroidRule(this)

  @Test
  fun testUserCanLoginAndCreateAnAccount() {
    /** Create an account on the welcome screen */
    signInWithUser(composeTestRule, UnverifiedUser.EMAIL, UnverifiedUser.PWD)

    Thread.sleep(5000)

    /** Verify the email */
    val emailVerificationUrl = getLatestEmailVerificationUrl()
    verifyEmail(emailVerificationUrl)

    // This sleep is required to wait for the email verification to complete
    Thread.sleep(5000)

    /** Refresh the email verification and continue */
    composeTestRule.onNodeWithTag(EmailVerificationTestTags.SCREEN).assertIsDisplayed()
    composeTestRule.onNodeWithTag(EmailVerificationTestTags.REFRESH).performClick()

    Thread.sleep(5000)

    composeTestRule.onNodeWithTag(EmailVerificationTestTags.CONTINUE).performClick()
    composeTestRule.onNodeWithTag(AccountDetailsTestTags.TITLE_TEXT).assertExists()

    /** Fill in the account details */
    composeTestRule
        .onNodeWithTag(AccountDetailsTestTags.FIRST_NAME_TEXT_FIELD)
        .performTextInput(UnverifiedUser.FIRST_NAME)
    composeTestRule
        .onNodeWithTag(AccountDetailsTestTags.LAST_NAME_TEXT_FIELD)
        .performTextInput(UnverifiedUser.LAST_NAME)
    composeTestRule
        .onNodeWithTag(AccountDetailsTestTags.BIOGRAPHY_TEXT_FIELD)
        .performTextInput(UnverifiedUser.BIOGRAPHY)
    assertDisplayComponentInScroll(
        composeTestRule.onNodeWithTag(AccountDetailsTestTags.INTERESTS_BUTTON))
    composeTestRule.onNodeWithTag(AccountDetailsTestTags.INTERESTS_BUTTON).performClick()
    assertDisplayComponentInScroll(
        composeTestRule.onNodeWithTag(InterestsOverlayTestTags.CLICKABLE_ROW + "0"))

    composeTestRule.onNodeWithTag(InterestsOverlayTestTags.CLICKABLE_ROW + "0").performClick()

    assertDisplayComponentInScroll(
        composeTestRule.onNodeWithTag(InterestsOverlayTestTags.CLICKABLE_ROW + "1"))

    composeTestRule.onNodeWithTag(InterestsOverlayTestTags.CLICKABLE_ROW + "1").performClick()

    assertDisplayComponentInScroll(
        composeTestRule.onNodeWithTag(InterestsOverlayTestTags.CLICKABLE_ROW + "2"))

    composeTestRule.onNodeWithTag(InterestsOverlayTestTags.CLICKABLE_ROW + "2").performClick()
    composeTestRule.onNodeWithTag(InterestsOverlayTestTags.SAVE_BUTTON).assertIsDisplayed()
    composeTestRule.onNodeWithTag(InterestsOverlayTestTags.SAVE_BUTTON).performClick()

    assertDisplayComponentInScroll(
        composeTestRule.onNodeWithTag(AccountDetailsTestTags.CONTINUE_BUTTON))

    composeTestRule.onNodeWithTag(AccountDetailsTestTags.CONTINUE_BUTTON).performClick()

    // Wait until "HomeScreen" is displayed
    composeTestRule.waitUntil(10000) {
      composeTestRule.onNodeWithTag(HomeTestTags.SCREEN).isDisplayed()
    }

    // Wait until the bottom nav bar is displayed
    composeTestRule.waitUntil(10000) {
      composeTestRule.onNodeWithTag(BottomNavBarTestTags.MY_PROFILE).isDisplayed()
    }

    /** Navigate to the profile screen */
    composeTestRule.onNodeWithTag(BottomNavBarTestTags.MY_PROFILE).performClick()

    composeTestRule.waitUntil(10000) {
      composeTestRule.onNodeWithTag(UserProfileTestTags.SCREEN).isDisplayed()
    }

    composeTestRule
        .onNodeWithTag(UserProfileTestTags.NAME)
        .assertTextContains("${UnverifiedUser.FIRST_NAME} ${UnverifiedUser.LAST_NAME}")
    composeTestRule
        .onNodeWithTag(UserProfileTestTags.BIOGRAPHY)
        .assertTextContains(UnverifiedUser.BIOGRAPHY)
    composeTestRule.onAllNodesWithTag(UserProfileTestTags.INTEREST).assertCountEquals(3)
  }

  private fun getLatestEmailVerificationUrl(): String {
    val client = OkHttpClient()

    val oobRequest = Request.Builder().url(Auth.OOB_URL).build()

    val response = client.newCall(oobRequest).execute()

    val data = response.body?.string()
    val json = JSONObject(data ?: "")
    val codes = json.getJSONArray("oobCodes")
    if (codes.length() == 0) {
      Log.e("EndToEndTest", "No email verification codes found. Data: $data")
      throw Exception("No email verification codes found.")
    }
    return codes.getJSONObject(codes.length() - 1).getString("oobLink")
  }

  private fun verifyEmail(url: String) {
    val client = OkHttpClient()

    val request = Request.Builder().url(url.replace("127.0.0.1", "10.0.2.2")).build()

    client.newCall(request).execute()
  }

  @After
  fun tearDown() {
    clearAllMocks()
    unmockkAll()
  }
}
