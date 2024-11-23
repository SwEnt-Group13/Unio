package com.android.unio.end2end

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertTextEquals
import androidx.compose.ui.test.isDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.test.performTextClearance
import androidx.compose.ui.test.performTextInput
import androidx.test.filters.LargeTest
import com.android.unio.MainActivity
import com.android.unio.model.strings.test_tags.BottomNavBarTestTags
import com.android.unio.model.strings.test_tags.HomeTestTags
import com.android.unio.model.strings.test_tags.InterestsOverlayTestTags
import com.android.unio.model.strings.test_tags.SocialsOverlayTestTags
import com.android.unio.model.strings.test_tags.UserEditionTestTags
import com.android.unio.model.strings.test_tags.UserProfileTestTags
import com.android.unio.model.user.Interest
import com.android.unio.ui.addNewUserSocial
import dagger.hilt.android.testing.HiltAndroidTest
import org.junit.Rule
import org.junit.Test

@LargeTest
@HiltAndroidTest
class EditUserDetailsTest : EndToEndTest() {

  @get:Rule val composeTestRule = createAndroidComposeRule<MainActivity>()

  @Test
  fun testUserModifiesHisAccountDetails() {

    // Sign in with user
    signInWithUser(composeTestRule, AliceMurphy.EMAIL, AliceMurphy.PASSWORD)

    composeTestRule.waitUntil(10000) {
      composeTestRule.onNodeWithTag(HomeTestTags.SCREEN).isDisplayed()
    }

    // Navigate to the user edition page
    composeTestRule.onNodeWithTag(BottomNavBarTestTags.MY_PROFILE).assertIsDisplayed()
    composeTestRule.onNodeWithTag(BottomNavBarTestTags.MY_PROFILE).performClick()

    composeTestRule.waitUntil(10000) {
      composeTestRule.onNodeWithTag(UserProfileTestTags.SCREEN).isDisplayed()
    }

    composeTestRule.onNodeWithTag(UserProfileTestTags.SETTINGS).assertIsDisplayed()
    composeTestRule.onNodeWithTag(UserProfileTestTags.SETTINGS).performClick()

    composeTestRule.waitUntil(10000) {
      composeTestRule.onNodeWithTag(UserProfileTestTags.BOTTOM_SHEET).isDisplayed()
    }

    composeTestRule.onNodeWithTag(UserProfileTestTags.EDITION).assertIsDisplayed()
    composeTestRule.onNodeWithTag(UserProfileTestTags.EDITION).performClick()

    composeTestRule.waitUntil(10000) {
      composeTestRule.onNodeWithTag(UserEditionTestTags.DISCARD_TEXT).isDisplayed()
    }

    // Change values
    composeTestRule.onNodeWithTag(UserEditionTestTags.FIRST_NAME_TEXT_FIELD).performScrollTo()
    composeTestRule.onNodeWithTag(UserEditionTestTags.FIRST_NAME_TEXT_FIELD).performTextClearance()
    composeTestRule.onNodeWithTag(UserEditionTestTags.FIRST_NAME_TEXT_FIELD).performTextInput("Eva")

    composeTestRule.onNodeWithTag(UserEditionTestTags.LAST_NAME_TEXT_FIELD).performScrollTo()
    composeTestRule.onNodeWithTag(UserEditionTestTags.LAST_NAME_TEXT_FIELD).performTextClearance()
    composeTestRule
        .onNodeWithTag(UserEditionTestTags.LAST_NAME_TEXT_FIELD)
        .performTextInput("Watson")

    composeTestRule.onNodeWithTag(UserEditionTestTags.BIOGRAPHY_TEXT_FIELD).performScrollTo()
    composeTestRule.onNodeWithTag(UserEditionTestTags.BIOGRAPHY_TEXT_FIELD).performTextClearance()
    composeTestRule
        .onNodeWithTag(UserEditionTestTags.BIOGRAPHY_TEXT_FIELD)
        .performTextInput("This is my new Bio")

    composeTestRule
        .onNodeWithTag(UserEditionTestTags.INTERESTS_BUTTON)
        .performScrollTo()
        .performClick()
    val allInterests: Set<Interest> = Interest.entries.map { it }.toMutableSet()

    // Click on all the interests
    allInterests.forEach { interest ->
      composeTestRule
          .onNodeWithTag(InterestsOverlayTestTags.CLICKABLE_ROW + interest.name)
          .performScrollTo()
          .performClick()
    }

    composeTestRule.onNodeWithTag(InterestsOverlayTestTags.SAVE_BUTTON).performClick()

    // Return to the edition screen
    composeTestRule.waitUntil(10000) {
      composeTestRule.onNodeWithTag(UserEditionTestTags.DISCARD_TEXT).isDisplayed()
    }

    // Navigate to the user socails overlay
    composeTestRule
        .onNodeWithTag(UserEditionTestTags.SOCIALS_BUTTON)
        .performScrollTo()
        .performClick()

    // Add some new user socials
    addNewUserSocial(composeTestRule, "evaWat2000", "Facebook")

    composeTestRule
        .onNodeWithTag(SocialsOverlayTestTags.SAVE_BUTTON)
        .performScrollTo()
        .performClick()

    composeTestRule.onNodeWithTag(UserEditionTestTags.SAVE_BUTTON).performScrollTo().performClick()

    // Wait until the user profile screen is displayed
    composeTestRule.waitUntil(10000) {
      composeTestRule.onNodeWithTag(UserProfileTestTags.SCREEN).isDisplayed()
    }

    // Check the new name and biography
    composeTestRule
        .onNodeWithTag(UserProfileTestTags.NAME)
        .performScrollTo()
        .assertTextEquals("Eva Watson")
    composeTestRule
        .onNodeWithTag(UserProfileTestTags.BIOGRAPHY)
        .performScrollTo()
        .assertTextEquals("This is my new Bio")

    // Check that all new interests have been added
    allInterests.filter { it == Interest.ART || it == Interest.TRAVEL }
    allInterests.forEach { interest ->
      composeTestRule
          .onNodeWithTag(UserProfileTestTags.INTEREST_CHIP + interest.title)
          .assertExists()
    }

    // Check that the new user social is here
    composeTestRule.onNodeWithTag(UserProfileTestTags.SOCIAL_BUTTON + "Facebook").assertExists()

    signOutWithUser(composeTestRule)
  }
}
