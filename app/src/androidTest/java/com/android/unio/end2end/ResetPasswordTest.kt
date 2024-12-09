package com.android.unio.end2end

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.isDisplayed
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextClearance
import androidx.compose.ui.test.performTextInput
import androidx.test.filters.LargeTest
import com.android.unio.model.preferences.AppPreferences
import com.android.unio.model.strings.test_tags.navigation.BottomNavBarTestTags
import com.android.unio.model.strings.test_tags.home.HomeTestTags
import com.android.unio.model.strings.test_tags.authentication.ResetPasswordTestTags
import com.android.unio.model.strings.test_tags.settings.SettingsTestTags
import com.android.unio.model.strings.test_tags.user.UserProfileTestTags
import com.android.unio.model.strings.test_tags.authentication.WelcomeTestTags
import dagger.hilt.android.testing.HiltAndroidTest
import org.junit.Test

@LargeTest
@HiltAndroidTest
class ResetPasswordTest : EndToEndTest() {

  @Test
  fun testUserCanResetPasswordInSettings() {
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

    simulateResetPassword(MarjolaineLemm.NEW_PASSWORD)

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

  @Test
  fun testUserCanResetPasswordInWelcomeScreen() {

    composeTestRule.waitUntil(10000) {
      composeTestRule.onNodeWithTag(WelcomeTestTags.SCREEN).isDisplayed()
    }
    composeTestRule.onNodeWithTag(WelcomeTestTags.FORGOT_PASSWORD).performClick()

    // Wait for the reset password screen to appear
    composeTestRule.waitUntil(10000) {
      composeTestRule.onNodeWithTag(ResetPasswordTestTags.SCREEN).isDisplayed()
    }

    // Input a wrong email to make sure that the error text is displayed
    composeTestRule
        .onNodeWithTag(ResetPasswordTestTags.EMAIL_FIELD)
        .performTextInput("not an email")

    composeTestRule.onNodeWithTag(ResetPasswordTestTags.EMAIL_ERROR_TEXT).assertIsDisplayed()

    // Input a correct email and continue with the test
    composeTestRule.onNodeWithTag(ResetPasswordTestTags.EMAIL_FIELD).performTextClearance()

    composeTestRule
        .onNodeWithTag(ResetPasswordTestTags.EMAIL_FIELD)
        .performTextInput(LebronJames.EMAIL)

    composeTestRule.onNodeWithTag(ResetPasswordTestTags.RESET_PASSWORD_BUTTON).performClick()

    Thread.sleep(1000)

    simulateResetPassword(LebronJames.NEW_PASSWORD)

    composeTestRule.waitUntil(10000) {
      composeTestRule.onNodeWithTag(WelcomeTestTags.SCREEN).isDisplayed()
    }

    // Assert that the user cannot login with his old password (stays in the home screen)
    signInWithUser(composeTestRule, LebronJames.EMAIL, LebronJames.OLD_PASSWORD)
    composeTestRule.waitUntil(1000) {
      composeTestRule.onNodeWithTag(WelcomeTestTags.SCREEN).isDisplayed()
    }

    composeTestRule.onNodeWithTag(WelcomeTestTags.EMAIL).performTextClearance()
    composeTestRule.onNodeWithTag(WelcomeTestTags.PASSWORD).performTextClearance()

    signInWithUser(composeTestRule, LebronJames.EMAIL, LebronJames.NEW_PASSWORD)
    composeTestRule.waitUntil(10000) {
      composeTestRule.onNodeWithTag(HomeTestTags.SCREEN).isDisplayed()
    }

    signOutWithUser(composeTestRule)
  }
}
