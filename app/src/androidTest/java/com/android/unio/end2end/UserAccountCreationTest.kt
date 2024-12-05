package com.android.unio.end2end

import android.util.Log
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertTextContains
import androidx.compose.ui.test.isDisplayed
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import androidx.test.filters.LargeTest
import com.android.unio.assertDisplayComponentInScroll
import com.android.unio.model.strings.test_tags.AccountDetailsTestTags
import com.android.unio.model.strings.test_tags.BottomNavBarTestTags
import com.android.unio.model.strings.test_tags.EmailVerificationTestTags
import com.android.unio.model.strings.test_tags.HomeTestTags
import com.android.unio.model.strings.test_tags.InterestsOverlayTestTags
import com.android.unio.model.strings.test_tags.UserProfileTestTags
import dagger.hilt.android.testing.HiltAndroidTest
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject
import org.junit.Test

@LargeTest
@HiltAndroidTest
class UserAccountCreationTest : EndToEndTest() {
  @Test
  fun testUserCanLoginAndCreateAnAccount() {
    /** Create an account on the welcome screen */
    signInWithUser(composeTestRule, UnverifiedUser.EMAIL, UnverifiedUser.PWD)

    Thread.sleep(10000)

    /** Verify the email */
    val emailVerificationUrl = getLatestEmailVerificationUrl()
    verifyEmail(emailVerificationUrl)

    // This sleep is required to wait for the email verification to complete

    /** Refresh the email verification and continue */
    composeTestRule.waitUntil(10000) {
      composeTestRule.onNodeWithTag(EmailVerificationTestTags.SCREEN).isDisplayed()
    }
    composeTestRule.onNodeWithTag(EmailVerificationTestTags.REFRESH).performClick()

    composeTestRule.waitUntil(10000) {
      composeTestRule.onNodeWithTag(EmailVerificationTestTags.CONTINUE).isDisplayed()
    }
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

    composeTestRule
        .onNodeWithTag(AccountDetailsTestTags.INTERESTS_BUTTON)
        .assertDisplayComponentInScroll()
    composeTestRule.onNodeWithTag(AccountDetailsTestTags.INTERESTS_BUTTON).performClick()

    composeTestRule
        .onNodeWithTag(InterestsOverlayTestTags.CLICKABLE_ROW + "SPORTS")
        .assertDisplayComponentInScroll()

    composeTestRule.onNodeWithTag(InterestsOverlayTestTags.CLICKABLE_ROW + "SPORTS").performClick()

    composeTestRule
        .onNodeWithTag(InterestsOverlayTestTags.CLICKABLE_ROW + "TRAVEL")
        .assertDisplayComponentInScroll()

    composeTestRule.onNodeWithTag(InterestsOverlayTestTags.CLICKABLE_ROW + "TRAVEL").performClick()

    composeTestRule
        .onNodeWithTag(InterestsOverlayTestTags.CLICKABLE_ROW + "FOOD")
        .assertDisplayComponentInScroll()

    composeTestRule.onNodeWithTag(InterestsOverlayTestTags.CLICKABLE_ROW + "FOOD").performClick()
    composeTestRule.onNodeWithTag(InterestsOverlayTestTags.SAVE_BUTTON).assertIsDisplayed()
    composeTestRule.onNodeWithTag(InterestsOverlayTestTags.SAVE_BUTTON).performClick()

    composeTestRule.onNodeWithTag(AccountDetailsTestTags.PROFILE_PICTURE_ICON).assertIsDisplayed()
    composeTestRule.onNodeWithTag(AccountDetailsTestTags.PROFILE_PICTURE_ICON).performClick()

    // balablablaGoBack !dsajdsajdbsakjdsa

    composeTestRule
        .onNodeWithTag(AccountDetailsTestTags.CONTINUE_BUTTON)
        .assertDisplayComponentInScroll()

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
    composeTestRule.onNodeWithTag(UserProfileTestTags.INTEREST_CHIP + "SPORTS").assertExists()
    composeTestRule.onNodeWithTag(UserProfileTestTags.INTEREST_CHIP + "TRAVEL").assertExists()
    composeTestRule.onNodeWithTag(UserProfileTestTags.INTEREST_CHIP + "FOOD").assertExists()

    signOutWithUser(composeTestRule)
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
}
