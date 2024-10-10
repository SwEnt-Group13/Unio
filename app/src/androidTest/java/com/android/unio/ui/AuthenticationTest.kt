package com.android.unio.ui

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import com.android.unio.ui.authentication.WelcomeScreen
import com.android.unio.ui.navigation.NavigationAction
import com.android.unio.ui.navigation.Screen
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.kotlin.eq
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify

class AuthenticationTest {

  private lateinit var navigationAction: NavigationAction

  @get:Rule val composeTestRule = createComposeRule()

  @Before
  fun setUp() {
    navigationAction = mock { NavigationAction::class.java }
    composeTestRule.setContent { WelcomeScreen(navigationAction) }
  }

  @Test
  fun testNavigationWelcomeToLogin() {
    composeTestRule.onNodeWithTag("LoginButton").assertIsDisplayed()
    composeTestRule.onNodeWithTag("LoginButton").performClick()
    verify(navigationAction).navigateTo(eq(Screen.AUTH))
  }
}
