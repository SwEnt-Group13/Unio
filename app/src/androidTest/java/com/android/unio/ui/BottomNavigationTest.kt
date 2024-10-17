package com.android.unio.ui

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.navigation.NavHostController
import com.android.unio.ui.home.HomeScreen
import com.android.unio.ui.navigation.NavigationAction
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.kotlin.mock

class BottomNavigationTest {

  private lateinit var navHostController: NavHostController
  private lateinit var navigationAction: NavigationAction

  @get:Rule val composeTestRule = createComposeRule()

  @Before
  fun setUp() {
    navHostController = mock { NavHostController::class.java }
    navigationAction = NavigationAction(navHostController)
  }

  @Test
  fun testBottomNavigationMenuDisplayed() {
    composeTestRule.setContent { HomeScreen(navigationAction, onAddEvent = {}, onEventClick = {}) }
    composeTestRule.onNodeWithTag("bottomNavigationMenu").assertIsDisplayed()
  }
}
