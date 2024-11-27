package com.android.unio.end2end

import android.util.Log
import androidx.compose.ui.test.isDisplayed
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextClearance
import androidx.compose.ui.test.performTextInput
import androidx.test.filters.LargeTest
import com.android.unio.model.strings.test_tags.HomeTestTags
import com.android.unio.model.strings.test_tags.WelcomeTestTags
import dagger.hilt.android.testing.HiltAndroidTest
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject
import org.junit.Test
import java.net.URL

@HiltAndroidTest
@LargeTest
class ResetPasswordWelcomeTest : EndToEndTest() {

    @Test
    fun testUserCanResetPassword() {

        composeTestRule.waitUntil(10000) {
            composeTestRule.onNodeWithTag(WelcomeTestTags.SCREEN).isDisplayed()
        }
        composeTestRule.onNodeWithTag(WelcomeTestTags.EMAIL).performTextInput(MarjolaineLemm.EMAIL)
        composeTestRule.onNodeWithTag(WelcomeTestTags.FORGOT_PASSWORD).performClick()

        Thread.sleep(1000)

        simulateResetPassword()

        composeTestRule.onNodeWithTag(WelcomeTestTags.EMAIL).performTextClearance()

        signInWithUser(composeTestRule, MarjolaineLemm.EMAIL, MarjolaineLemm.NEW_PASSWORD)
        composeTestRule.waitUntil(10000){
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