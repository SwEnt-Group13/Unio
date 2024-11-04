package com.android.unio.ui.authentication.overlay

import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsOn
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import com.android.unio.model.strings.test_tags.InterestsOverlayTestTags
import com.android.unio.model.user.Interest
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class InterestOverlayTest {
  val interests = Interest.entries.map { it to mutableStateOf(false) }.toMutableList()

  @get:Rule val composeTestRule = createComposeRule()

  @Before
  fun setUp() {
    composeTestRule.setContent { InterestOverlay({}, {}, interests) }
  }

  @Test
  fun testEverythingIsDisplayed() {
    composeTestRule.onNodeWithTag(InterestsOverlayTestTags.TITLE_TEXT).assertIsDisplayed()
    composeTestRule.onNodeWithTag(InterestsOverlayTestTags.SUBTITLE_TEXT).assertIsDisplayed()
    composeTestRule.onNodeWithTag(InterestsOverlayTestTags.SAVE_BUTTON).assertIsDisplayed()

    interests.forEachIndexed { index, pair ->
      composeTestRule
          .onNodeWithTag(InterestsOverlayTestTags.TEXT + pair.first.name, useUnmergedTree = true)
          .assertExists()
      composeTestRule
          .onNodeWithTag(InterestsOverlayTestTags.CHECKBOX + pair.first.name)
          .assertExists()

      if (index != interests.size - 1) {
        composeTestRule.onNodeWithTag(InterestsOverlayTestTags.DIVIDER + "$index").assertExists()
      }
    }
  }

  @Test
  fun testWhenCheckBoxCheckedInterestStateChanges() {
    interests.forEachIndexed { index, pair ->
      composeTestRule
          .onNodeWithTag(InterestsOverlayTestTags.CLICKABLE_ROW + "$index")
          .performScrollTo()
          .performClick()
      composeTestRule
          .onNodeWithTag(InterestsOverlayTestTags.CHECKBOX + pair.first.name)
          .assertIsOn()
    }
  }
}
