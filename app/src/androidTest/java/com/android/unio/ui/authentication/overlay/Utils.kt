package com.android.unio.ui.authentication.overlay

import androidx.compose.ui.test.junit4.ComposeContentTestRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.test.performTextInput

fun addNewUserSocial(composeTestRule: ComposeContentTestRule, username: String, platform: String) {
  composeTestRule.onNodeWithTag("SocialOverlayAddButton").performScrollTo().performClick()
  composeTestRule.onNodeWithTag("SocialPromptTextField").performTextInput(username)
  composeTestRule.onNodeWithTag("SocialPromptDropdownMenuBox").performClick()
  composeTestRule.onNodeWithTag("SocialPromptDropdownMenuItem: $platform").performClick()
  composeTestRule.onNodeWithTag("SocialPromptSaveButton").performClick()
}
