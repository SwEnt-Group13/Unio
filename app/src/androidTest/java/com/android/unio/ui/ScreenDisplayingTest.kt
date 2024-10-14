package com.android.unio.ui

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.navigation.NavHostController
import com.android.unio.ui.association.AssociationProfileScreen
import com.android.unio.ui.authentication.AccountDetails
import com.android.unio.ui.authentication.EmailVerificationScreen
import com.android.unio.ui.authentication.WelcomeScreen
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

class ScreenDisplayingTest {

  private lateinit var navigationAction: NavigationAction

  @get:Rule val composeTestRule = createComposeRule()

  @Before
  fun setUp() {
    navigationAction = mock { NavHostController::class.java }
  }

  @Test
  fun testWelcomeDisplayed() {
    composeTestRule.setContent { WelcomeScreen(navigationAction) }
    composeTestRule.onNodeWithTag("WelcomeScreen").assertIsDisplayed()
  }

  @Test
  fun testEmailVerificationDisplayed() {
    composeTestRule.setContent { EmailVerificationScreen(navigationAction) }
    composeTestRule.onNodeWithTag("EmailVerificationScreen").assertIsDisplayed()
  }

  @Test
  fun testAccountDetailsDisplayed() {
    composeTestRule.setContent { AccountDetails(navigationAction) }
    composeTestRule.onNodeWithTag("AccountDetails").assertIsDisplayed()
  }

  @Test
  fun testHomeDisplayed() {
    composeTestRule.setContent { HomeScreen(navigationAction) }
    composeTestRule.onNodeWithTag("HomeScreen").assertIsDisplayed()
  }

  @Test
  fun testExploreDisplayed() {
    composeTestRule.setContent { ExploreScreen(navigationAction) }
    composeTestRule.onNodeWithTag("exploreScreen").assertIsDisplayed()
    composeTestRule.onNodeWithTag("searchBar").assertIsDisplayed()
  }

  @Test
  fun testMapDisplayed() {
    composeTestRule.setContent { MapScreen() }
    composeTestRule.onNodeWithTag("MapScreen").assertIsDisplayed()
  }

  @Test
  fun testEventDisplayed() {
    composeTestRule.setContent { EventScreen() }
    composeTestRule.onNodeWithTag("EventScreen").assertIsDisplayed()
  }

  @Test
  fun testEventCreationDisplayed() {
    composeTestRule.setContent { EventCreationScreen() }
    composeTestRule.onNodeWithTag("EventCreationScreen").assertIsDisplayed()
  }

  @Test
  fun testAssociationDisplayed() {
    composeTestRule.setContent { AssociationProfileScreen(navigationAction) }
    composeTestRule.onNodeWithTag("AssociationScreen").assertIsDisplayed()
  }

  @Test
  fun testSavedDisplayed() {
    composeTestRule.setContent { SavedScreen(navigationAction) }
    composeTestRule.onNodeWithTag("SavedScreen").assertIsDisplayed()
  }

  @Test
  fun testSettingsDisplayed() {
    composeTestRule.setContent { SettingsScreen() }
    composeTestRule.onNodeWithTag("SettingsScreen").assertIsDisplayed()
  }

  @Test
  fun testUserProfileDisplayed() {
    composeTestRule.setContent { UserProfileScreen(navigationAction) }
    composeTestRule.onNodeWithTag("UserProfileScreen").assertIsDisplayed()
  }

  @Test
  fun testSomeoneElseUserProfileDisplayed() {
    composeTestRule.setContent { SomeoneElseUserProfileScreen() }
    composeTestRule.onNodeWithTag("SomeoneElseUserProfileScreen").assertIsDisplayed()
  }
}
