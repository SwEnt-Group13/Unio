package com.android.unio.end2end

import android.util.Log
import androidx.compose.ui.test.isDisplayed
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.test.filters.LargeTest
import com.android.unio.model.preferences.AppPreferences
import com.android.unio.model.strings.test_tags.BottomNavBarTestTags
import com.android.unio.model.strings.test_tags.HomeTestTags
import com.android.unio.model.strings.test_tags.SettingsTestTags
import com.android.unio.model.strings.test_tags.UserProfileTestTags
import dagger.hilt.android.testing.HiltAndroidTest
import java.net.URL
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject
import org.junit.Test

@LargeTest
@HiltAndroidTest
class ResetPasswordSettingsTest : EndToEndTest() {

  @Test
  fun testUserCanResetPassword() {
    signInWithUser(composeTestRule, MarjolaineLemm.EMAIL, MarjolaineLemm.OLD_PASSWORD)

    composeTestRule.waitUntil(10000) {
      composeTestRule.onNodeWithTag(HomeTestTags.SCREEN).isDisplayed()
    }

    composeTestRule.onNodeWithTag(BottomNavBarTestTags.MY_PROFILE).performClick()
    composeTestRule.waitUntil(1000) {
      composeTestRule.onNodeWithTag(UserProfileTestTags.SCREEN).isDisplayed()
    }

    composeTestRule.onNodeWithTag(UserProfileTestTags.SETTINGS).performClick()
    composeTestRule.waitUntil(1000) {
      composeTestRule.onNodeWithTag(UserProfileTestTags.BOTTOM_SHEET).isDisplayed()
    }
    composeTestRule.onNodeWithTag(UserProfileTestTags.USER_SETTINGS).performClick()
    composeTestRule.waitUntil(1000) {
      composeTestRule.onNodeWithTag(SettingsTestTags.SCREEN).isDisplayed()
    }

    composeTestRule.onNodeWithTag(AppPreferences.RESET_PASSWORD).performClick()

    Thread.sleep(1000)

    simulateResetPassword()

    composeTestRule.onNodeWithTag(SettingsTestTags.GO_BACK).performClick()
    composeTestRule.waitUntil(1000) {
      composeTestRule.onNodeWithTag(UserProfileTestTags.SCREEN).isDisplayed()
    }
    composeTestRule.onNodeWithTag(UserProfileTestTags.SETTINGS).performClick()
    composeTestRule.waitUntil(1000) {
      composeTestRule.onNodeWithTag(UserProfileTestTags.BOTTOM_SHEET).isDisplayed()
    }
    composeTestRule.onNodeWithTag(UserProfileTestTags.SIGN_OUT).performClick()

    signInWithUser(composeTestRule, MarjolaineLemm.EMAIL, MarjolaineLemm.NEW_PASSWORD)

    composeTestRule.waitUntil(10000) {
      composeTestRule.onNodeWithTag(HomeTestTags.SCREEN).isDisplayed()
    }

    signOutWithUser(composeTestRule)
  }

  private fun simulateResetPassword() {
    val ip = "10.0.2.2"
    val raw = Auth.OOB_URL
    val response = URL(raw).readText()
    Log.d("ResetPasswordSettingsTest", "Response: $response")
    val json = JSONObject(response)
    val resetLink = json.optJSONArray("oobCodes")?.getJSONObject(0)?.optString("oobLink")
    assert(resetLink != null)
    val url = resetLink!! + "&newPassword=${MarjolaineLemm.NEW_PASSWORD}"
    Log.d("ResetPasswordSettingsTest", "Reset link: $url")
    val client = OkHttpClient()
    val request = Request.Builder().url(url.replace("127.0.0.1", ip)).build()

    client.newCall(request).execute()
  }
}
