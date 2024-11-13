package com.android.unio.ui.authentication.overlay

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import com.android.unio.model.strings.test_tags.SocialsOverlayTestTags
import com.android.unio.model.user.UserSocial
import io.mockk.clearAllMocks
import io.mockk.unmockkAll
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class SocialOverlayTest {
  private val userSocials = emptyList<UserSocial>().toMutableList()

  @get:Rule val composeTestRule = createComposeRule()

  @Before
  fun setUp() {
    composeTestRule.setContent { SocialOverlay({}, {}, userSocials) }
  }

  @Test
  fun everythingIsDisplayedWhenBlank() {
    composeTestRule.onNodeWithTag(SocialsOverlayTestTags.TITLE_TEXT).assertIsDisplayed()
    composeTestRule.onNodeWithTag(SocialsOverlayTestTags.DESCRIPTION_TEXT).assertIsDisplayed()
    composeTestRule.onNodeWithTag(SocialsOverlayTestTags.ADD_BUTTON).assertIsDisplayed()
    composeTestRule.onNodeWithTag(SocialsOverlayTestTags.SAVE_BUTTON).assertIsDisplayed()
  }

  @Test
  fun testSocialPromptAppearsWhenAddButtonClicked() {
    composeTestRule
        .onNodeWithTag(SocialsOverlayTestTags.ADD_BUTTON)
        .performScrollTo()
        .performClick()
    composeTestRule.onNodeWithTag(SocialsOverlayTestTags.PROMPT_CARD).assertIsDisplayed()
  }

  @Test
  fun testCorrectlyAddsNewUserSocial() {
    addNewUserSocial(composeTestRule, "facebook_username", "Facebook")
    composeTestRule
        .onNodeWithTag(SocialsOverlayTestTags.CLICKABLE_ROW + "Facebook")
        .assertIsDisplayed()
  }

  @Test
  fun testCorrectlyDeletesUserSocial() {
    addNewUserSocial(composeTestRule, "facebook_username", "Facebook")
    composeTestRule
        .onNodeWithTag(SocialsOverlayTestTags.ICON + "Facebook", useUnmergedTree = true)
        .performScrollTo()
        .performClick()
    composeTestRule
        .onNodeWithTag(SocialsOverlayTestTags.CLICKABLE_ROW + "Facebook")
        .assertDoesNotExist()
  }

  @Test
  fun testCancelButtonExistsSocialPrompt() {
    composeTestRule
        .onNodeWithTag(SocialsOverlayTestTags.ADD_BUTTON)
        .performScrollTo()
        .performClick()
    composeTestRule.onNodeWithTag(SocialsOverlayTestTags.PROMPT_CANCEL_BUTTON).performClick()
    composeTestRule.onNodeWithTag(SocialsOverlayTestTags.PROMPT_CARD).assertDoesNotExist()
  }

  @Test
  fun testDisplayErrorWithIncorrectInput() {
    addNewUserSocial(composeTestRule, "", "Facebook")
    composeTestRule
        .onNodeWithTag(SocialsOverlayTestTags.PROMPT_ERROR, useUnmergedTree = true)
        .assertIsDisplayed()
  }

  @After
  fun tearDown() {
    clearAllMocks()
    unmockkAll()
  }
}
