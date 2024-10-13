package com.android.unio.ui.authentication

import androidx.compose.ui.test.assertHasClickAction
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performTextInput
import com.android.unio.ui.navigation.NavigationAction
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.kotlin.mock

class WelcomeTest {

  private lateinit var navigationAction: NavigationAction

  @get:Rule val composeTestRule = createComposeRule()

  @Before
  fun setUp() {
    navigationAction = mock { NavigationAction::class.java }
    composeTestRule.setContent { WelcomeScreen(navigationAction) }
  }

  @Test
  fun testWelcomeIsDisplayed() {
    composeTestRule.onNodeWithTag("WelcomeEmail").assertIsDisplayed()
    composeTestRule.onNodeWithTag("WelcomePassword").assertIsDisplayed()
    composeTestRule.onNodeWithTag("WelcomePassword").assertIsDisplayed()
    composeTestRule.onNodeWithTag("WelcomeButton").assertIsDisplayed()
    composeTestRule.onNodeWithTag("WelcomeButton").assertHasClickAction()
    composeTestRule.onNodeWithTag("WelcomeButton").assertIsNotEnabled()
  }

  @Test
  fun testButtonEnables() {
    composeTestRule.onNodeWithTag("WelcomeButton").assertIsNotEnabled()

    composeTestRule.onNodeWithTag("WelcomeEmail").performTextInput("john.doe@epfl.ch")
    composeTestRule.onNodeWithTag("WelcomePassword").performTextInput("1234")

    composeTestRule.onNodeWithTag("WelcomeButton").assertIsEnabled()
  }
}
