package com.android.unio.end2end

import androidx.compose.ui.test.isDisplayed
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.test.filters.LargeTest
import com.android.unio.model.strings.test_tags.authentication.EmailVerificationTestTags
import com.android.unio.model.strings.test_tags.authentication.WelcomeTestTags
import com.android.unio.model.strings.test_tags.home.HomeTestTags
import com.android.unio.model.strings.test_tags.navigation.BottomNavBarTestTags
import com.android.unio.model.strings.test_tags.user.UserEditionTestTags
import com.android.unio.model.strings.test_tags.user.UserProfileTestTags
import dagger.hilt.android.testing.HiltAndroidTest
import org.junit.Test

@LargeTest
@HiltAndroidTest
class UserDeletionTest : EndToEndTest() {
    @Test
    fun userCanDeleteHisAccount() {
        signInWithUser(composeTestRule, UserToDelete.EMAIL, UserToDelete.PASSWORD)

        composeTestRule.waitUntil(10000) {
            composeTestRule.onNodeWithTag(HomeTestTags.SCREEN).isDisplayed()
        }

        composeTestRule.onNodeWithTag(BottomNavBarTestTags.MY_PROFILE).performClick()

        composeTestRule.waitUntil(10000) {
            composeTestRule.onNodeWithTag(UserProfileTestTags.SCREEN).isDisplayed()
        }

        composeTestRule.onNodeWithTag(UserProfileTestTags.SETTINGS).performClick()

        composeTestRule.onNodeWithTag(UserProfileTestTags.EDITION).performClick()

        composeTestRule.waitUntil(10000) {
            composeTestRule.onNodeWithTag(UserEditionTestTags.DISCARD_TEXT).isDisplayed()
        }

        composeTestRule
            .onNodeWithTag(UserEditionTestTags.DELETE_BUTTON)
            .performScrollTo()
            .performClick()
        composeTestRule.onNodeWithTag(UserEditionTestTags.DELETE_CONFIRMATION).performClick()

        composeTestRule.waitUntil(10000) {
            composeTestRule.onNodeWithTag(WelcomeTestTags.SCREEN).isDisplayed()
        }

        signInWithUser(composeTestRule, UserToDelete.EMAIL, UserToDelete.PASSWORD)

        composeTestRule.waitUntil(10000) {
            composeTestRule.onNodeWithTag(EmailVerificationTestTags.SCREEN).isDisplayed()
        }
    }
}