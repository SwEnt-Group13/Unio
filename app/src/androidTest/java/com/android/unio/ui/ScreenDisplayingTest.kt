package com.android.unio.ui

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import com.android.unio.ui.association.AssociationScreen
import com.android.unio.ui.authentication.LoginScreen
import com.android.unio.ui.event.EventCreationScreen
import com.android.unio.ui.event.EventScreen
import com.android.unio.ui.explore.ExploreScreen
import com.android.unio.ui.home.HomeScreen
import com.android.unio.ui.map.MapScreen
import com.android.unio.ui.saved.SavedScreen
import com.android.unio.ui.settings.SettingsScreen
import org.junit.Rule
import org.junit.Test

class ScreenDisplayingTest() {

  @get:Rule val composeTestRule = createComposeRule()

  @Test
  fun testHomeDisplayed() {
    composeTestRule.setContent { HomeScreen() }
    composeTestRule.onNodeWithTag("HomeScreen").assertIsDisplayed()
  }

  @Test
  fun testExploreDisplayed() {
    composeTestRule.setContent { ExploreScreen() }
    composeTestRule.onNodeWithTag("ExploreScreen").assertIsDisplayed()
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
  fun testLoginDisplayed() {
    composeTestRule.setContent { LoginScreen() }
    composeTestRule.onNodeWithTag("LoginScreen").assertIsDisplayed()
  }

  @Test
  fun testAssociationDisplayed() {
    composeTestRule.setContent { AssociationScreen() }
    composeTestRule.onNodeWithTag("AssociationScreen").assertIsDisplayed()
  }

  @Test
  fun testSavedDisplayed() {
    composeTestRule.setContent { SavedScreen() }
    composeTestRule.onNodeWithTag("SavedScreen").assertIsDisplayed()
  }

  @Test
  fun testSettingsDisplayed() {
    composeTestRule.setContent { SettingsScreen() }
    composeTestRule.onNodeWithTag("SettingsScreen").assertIsDisplayed()
  }
}
