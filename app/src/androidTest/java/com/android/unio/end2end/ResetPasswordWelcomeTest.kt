package com.android.unio.end2end

import androidx.compose.ui.test.isDisplayed
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextClearance
import androidx.compose.ui.test.performTextInput
import androidx.test.filters.LargeTest
import com.android.unio.model.strings.test_tags.HomeTestTags
import com.android.unio.model.strings.test_tags.WelcomeTestTags
import dagger.hilt.android.testing.HiltAndroidTest
import org.junit.Test

@HiltAndroidTest
@LargeTest
class ResetPasswordWelcomeTest : EndToEndTest() {

  @Test
  fun testUserCanResetPassword() {

    composeTestRule.waitUntil(10000) {
      composeTestRule.onNodeWithTag(WelcomeTestTags.SCREEN).isDisplayed()
    }
    composeTestRule.onNodeWithTag(WelcomeTestTags.FORGOT_PASSWORD).performClick()

    // A Toast appears here because no email is entered. We just check if the welcome screen is
    // still displayed
    composeTestRule.onNodeWithTag(WelcomeTestTags.SCREEN).isDisplayed()

    composeTestRule.onNodeWithTag(WelcomeTestTags.EMAIL).performTextInput(MarjolaineLemm.EMAIL)
    composeTestRule.onNodeWithTag(WelcomeTestTags.FORGOT_PASSWORD).performClick()

    Thread.sleep(1000)

    simulateResetPassword()

    composeTestRule.onNodeWithTag(WelcomeTestTags.EMAIL).performTextClearance()

    signInWithUser(composeTestRule, MarjolaineLemm.EMAIL, MarjolaineLemm.NEW_PASSWORD)
    composeTestRule.waitUntil(10000) {
      composeTestRule.onNodeWithTag(HomeTestTags.SCREEN).isDisplayed()
    }

    signOutWithUser(composeTestRule)
  }
}
