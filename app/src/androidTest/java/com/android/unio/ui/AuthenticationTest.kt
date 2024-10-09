package com.android.unio.ui

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.android.unio.ui.authentication.LoginScreen
import com.android.unio.ui.authentication.WelcomeScreen
import com.android.unio.ui.navigation.NavigationAction
import com.android.unio.ui.navigation.Screen
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class AuthenticationTest {

  private lateinit var navHostController: NavHostController
  private lateinit var navigationAction: NavigationAction

  @get:Rule val composeTestRule = createComposeRule()

  @Before
  fun setUp() {
    composeTestRule.setContent {
      navHostController = rememberNavController()
      navigationAction = NavigationAction(navHostController)
      NavHost(navController = navHostController, startDestination = Screen.WELCOME) {
        composable(Screen.WELCOME) { WelcomeScreen(navigationAction) }
        composable(Screen.AUTH) { LoginScreen(navigationAction) }
      }
    }
  }

  @Test
  fun testNavigationWelcomeToLogin() {
    composeTestRule.onNodeWithTag("LoginButton").assertIsDisplayed()
    composeTestRule.onNodeWithTag("LoginButton").performClick()
    composeTestRule.onNodeWithTag("LoginScreen").assertIsDisplayed()
  }
}
