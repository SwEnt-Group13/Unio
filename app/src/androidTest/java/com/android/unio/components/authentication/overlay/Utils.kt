package com.android.unio.components.authentication.overlay

import androidx.compose.ui.test.junit4.ComposeContentTestRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.test.performTextInput
import com.android.unio.model.strings.test_tags.SocialsOverlayTestTags

fun addNewUserSocial(composeTestRule: ComposeContentTestRule, username: String, platform: String) {
  composeTestRule.onNodeWithTag(SocialsOverlayTestTags.ADD_BUTTON).performScrollTo().performClick()
  composeTestRule.onNodeWithTag(SocialsOverlayTestTags.PROMPT_TEXT_FIELD).performTextInput(username)
  composeTestRule.onNodeWithTag(SocialsOverlayTestTags.PROMPT_DROP_BOX).performClick()
  composeTestRule
      .onNodeWithTag(SocialsOverlayTestTags.PROMPT_DROP_BOX_ITEM + "$platform")
      .performClick()
  composeTestRule.onNodeWithTag(SocialsOverlayTestTags.PROMPT_SAVE_BUTTON).performClick()
}
