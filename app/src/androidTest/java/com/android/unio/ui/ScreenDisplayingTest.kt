package com.android.unio.ui

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.navigation.NavHostController
import com.android.unio.ui.association.AssociationScreen
import com.android.unio.ui.authentication.LoginScreen
import com.android.unio.ui.event.EventCreationScreen
import com.android.unio.ui.event.EventScreen
import com.android.unio.ui.explore.ExploreScreen
import com.android.unio.ui.home.HomeScreen
import com.android.unio.ui.map.MapScreen
import com.android.unio.ui.navigation.NavigationAction
import com.android.unio.ui.saved.SavedScreen
import com.android.unio.ui.settings.SettingsScreen
import com.android.unio.ui.user.SomeoneElseUserProfileScreen
import com.android.unio.ui.user.UserProfileScreen
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.kotlin.mock

class ScreenDisplayingTest() {

  private lateinit var navHostController: NavHostController
  private lateinit var navigationAction: NavigationAction

  @get:Rule val composeTestRule = createComposeRule()

  @Before
  fun setUp() {
    navHostController = mock { NavHostController::class.java }
    navigationAction = NavigationAction(navHostController)
  }

  @Test
  fun testHomeDisplayed() {
    composeTestRule.setContent { HomeScreen(navigationAction) }
    composeTestRule.onNodeWithTag("HomeScreen").assertIsDisplayed()
    composeTestRule.onNodeWithText("Home screen").assertIsDisplayed()
  }

  @Test
  fun testExploreDisplayed() {
    composeTestRule.setContent { ExploreScreen(navigationAction) }
    composeTestRule.onNodeWithTag("exploreScreen").assertIsDisplayed()
  }

  @Test
  fun testMapDisplayed() {
    composeTestRule.setContent { MapScreen() }
    composeTestRule.onNodeWithTag("MapScreen").assertIsDisplayed()
    composeTestRule.onNodeWithText("Map screen").assertIsDisplayed()
  }

  @Test
  fun testEventDisplayed() {
    composeTestRule.setContent { EventScreen() }
    composeTestRule.onNodeWithTag("EventScreen").assertIsDisplayed()
    composeTestRule.onNodeWithText("Event screen").assertIsDisplayed()
  }

  @Test
  fun testEventCreationDisplayed() {
    composeTestRule.setContent { EventCreationScreen() }
    composeTestRule.onNodeWithTag("EventCreationScreen").assertIsDisplayed()
    composeTestRule.onNodeWithText("Event creation screen").assertIsDisplayed()
  }

  @Test
  fun testLoginDisplayed() {
    composeTestRule.setContent { LoginScreen() }
    composeTestRule.onNodeWithTag("LoginScreen").assertIsDisplayed()
    composeTestRule.onNodeWithText("Login screen").assertIsDisplayed()
  }

  @Test
  fun testAssociationDisplayed() {
    composeTestRule.setContent { AssociationScreen() }
    composeTestRule.onNodeWithTag("AssociationScreen").assertIsDisplayed()
    composeTestRule.onNodeWithText("Association screen").assertIsDisplayed()
  }

  @Test
  fun testSavedDisplayed() {
    composeTestRule.setContent { SavedScreen() }
    composeTestRule.onNodeWithTag("SavedScreen").assertIsDisplayed()
    composeTestRule.onNodeWithText("Saved screen").assertIsDisplayed()
  }

  @Test
  fun testSettingsDisplayed() {
    composeTestRule.setContent { SettingsScreen() }
    composeTestRule.onNodeWithTag("SettingsScreen").assertIsDisplayed()
    composeTestRule.onNodeWithText("Settings screen").assertIsDisplayed()
  }

  @Test
  fun testUserProfileDisplayed() {
    composeTestRule.setContent { UserProfileScreen() }
    composeTestRule.onNodeWithTag("UserProfileScreen").assertIsDisplayed()
    composeTestRule.onNodeWithText("User profile screen").assertIsDisplayed()
  }

  @Test
  fun testSOmeoneElseUserProfileDisplayed() {
    composeTestRule.setContent { SomeoneElseUserProfileScreen() }
    composeTestRule.onNodeWithTag("SomeoneElseUserProfileScreen").assertIsDisplayed()
    composeTestRule.onNodeWithText("Someone else user profile screen").assertIsDisplayed()
  }
}
