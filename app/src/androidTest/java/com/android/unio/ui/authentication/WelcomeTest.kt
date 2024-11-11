package com.android.unio.ui.authentication

import androidx.compose.ui.test.assertHasClickAction
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performTextInput
import com.android.unio.model.strings.test_tags.WelcomeTestTags
import org.junit.Rule
import org.junit.Test

class WelcomeTest {

  @get:Rule val composeTestRule = createComposeRule()

  @Test
  fun testWelcomeIsDisplayed() {
    composeTestRule.setContent { WelcomeScreen() }
    composeTestRule.onNodeWithTag(WelcomeTestTags.EMAIL).assertIsDisplayed()
    composeTestRule.onNodeWithTag(WelcomeTestTags.PASSWORD).assertIsDisplayed()
    composeTestRule.onNodeWithTag(WelcomeTestTags.BUTTON).assertIsDisplayed()
    composeTestRule.onNodeWithTag(WelcomeTestTags.BUTTON).assertHasClickAction()
    composeTestRule.onNodeWithTag(WelcomeTestTags.BUTTON).assertIsNotEnabled()
  }

  @Test
  fun testButtonEnables() {
    composeTestRule.setContent { WelcomeScreen() }
    composeTestRule.onNodeWithTag(WelcomeTestTags.BUTTON).assertIsNotEnabled()

    composeTestRule.onNodeWithTag(WelcomeTestTags.EMAIL).performTextInput("john.doe@epfl.ch")
    composeTestRule.onNodeWithTag(WelcomeTestTags.PASSWORD).performTextInput("123456")

    composeTestRule.onNodeWithTag(WelcomeTestTags.BUTTON).assertIsEnabled()
  }
}
