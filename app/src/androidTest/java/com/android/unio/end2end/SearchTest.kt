package com.android.unio.end2end

import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertTextEquals
import androidx.compose.ui.test.isDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import androidx.test.filters.LargeTest
import com.android.unio.MainActivity
import com.android.unio.model.strings.test_tags.AssociationProfileTestTags
import com.android.unio.model.strings.test_tags.BottomNavBarTestTags
import com.android.unio.model.strings.test_tags.EventCardTestTags
import com.android.unio.model.strings.test_tags.EventDetailsTestTags
import com.android.unio.model.strings.test_tags.ExploreContentTestTags
import com.android.unio.model.strings.test_tags.HomeTestTags
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import dagger.hilt.android.testing.HiltAndroidTest
import org.junit.Rule
import org.junit.Test

@LargeTest
@HiltAndroidTest
class SearchTest : EndToEndTest() {
  @get:Rule val composeTestRule = createAndroidComposeRule<MainActivity>()

  @Test
  fun testSearchDisplaysCorrectResultsForEvents() {
    if (Firebase.auth.currentUser == null) {
      signInWithUser(composeTestRule, User1.EMAIL, User1.PASSWORD)
    }

    composeTestRule.waitUntil(10000) {
      composeTestRule.onNodeWithTag(HomeTestTags.SCREEN).isDisplayed()
    }

    composeTestRule.onNodeWithTag(HomeTestTags.SEARCH_BAR_INPUT).assertIsDisplayed()
    composeTestRule
        .onNodeWithTag(HomeTestTags.SEARCH_BAR_INPUT)
        .performTextInput(EVENT_SEARCH_INPUT)

    // Wait for "server's" response to get the event
    Thread.sleep(5000)
    composeTestRule.onAllNodesWithTag(EventCardTestTags.EVENT_ITEM).assertCountEquals(1)

    composeTestRule.onNodeWithTag(EventCardTestTags.EVENT_ITEM).performClick()

    composeTestRule.waitUntil(10000) {
      composeTestRule.onNodeWithTag(EventDetailsTestTags.SCREEN).isDisplayed()
    }

    composeTestRule.onNodeWithTag(EventDetailsTestTags.TITLE).assertTextEquals(EXPECTED_EVENT_NAME)

    composeTestRule.onNodeWithTag(EventDetailsTestTags.GO_BACK_BUTTON).performClick()

    signOutWithUser(composeTestRule)
  }

  @Test
  fun testSearchDiplaysCorrectResultsForAssociations() {
    signInWithUser(composeTestRule, User1.EMAIL, User1.PASSWORD)

    composeTestRule.waitUntil(10000) {
      composeTestRule.onNodeWithTag(HomeTestTags.SCREEN).isDisplayed()
    }

    composeTestRule.onNodeWithTag(BottomNavBarTestTags.EXPLORE).assertIsDisplayed()
    composeTestRule.onNodeWithTag(BottomNavBarTestTags.EXPLORE).performClick()

    composeTestRule.waitUntil(10000) {
      composeTestRule.onNodeWithTag(ExploreContentTestTags.TITLE_TEXT).isDisplayed()
    }

    composeTestRule.onNodeWithTag(ExploreContentTestTags.SEARCH_BAR).assertIsDisplayed()
    composeTestRule
        .onNodeWithTag(ExploreContentTestTags.SEARCH_BAR_INPUT)
        .performTextInput(ASSOCIATION_SEARCH_INPUT)

    // Wait for the server's response to get the association
    composeTestRule.waitUntil(10000) {
      composeTestRule
          .onNodeWithTag(
              ExploreContentTestTags.ASSOCIATION_EXPLORE_RESULT + EXPECTED_ASSOCIATION_NAME)
          .isDisplayed()
    }

    composeTestRule
        .onNodeWithTag(
            ExploreContentTestTags.ASSOCIATION_EXPLORE_RESULT + EXPECTED_ASSOCIATION_NAME)
        .performClick()

    composeTestRule.waitUntil(10000) {
      composeTestRule.onNodeWithTag(AssociationProfileTestTags.SCREEN).isDisplayed()
    }

    composeTestRule
        .onNodeWithTag(AssociationProfileTestTags.TITLE)
        .assertTextEquals(EXPECTED_ASSOCIATION_NAME)

    composeTestRule.onNodeWithTag(AssociationProfileTestTags.GO_BACK_BUTTON).performClick()

    signOutWithUser(composeTestRule)
  }

  private companion object {
    const val EVENT_SEARCH_INPUT = "Weekend"
    const val ASSOCIATION_SEARCH_INPUT = "music"
    const val EXPECTED_EVENT_NAME = "WeekEndSki IC"
    const val EXPECTED_ASSOCIATION_NAME = "Musical"
  }
}
