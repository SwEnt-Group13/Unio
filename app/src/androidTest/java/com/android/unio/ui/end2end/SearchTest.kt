package com.android.unio.ui.end2end

import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertTextEquals
import androidx.compose.ui.test.isDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.test.performTextInput
import androidx.test.filters.LargeTest
import com.android.unio.MainActivity
import com.android.unio.model.strings.test_tags.EventCardTestTags
import com.android.unio.model.strings.test_tags.EventDetailsTestTags
import com.android.unio.model.strings.test_tags.HomeTestTags
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import org.junit.Rule
import org.junit.Test


@LargeTest
@HiltAndroidTest
class SearchTest : EndToEndTest() {

    @get:Rule val composeTestRule = createAndroidComposeRule<MainActivity>()
    @get:Rule val hiltRule = HiltAndroidRule(this)

    @Test
    fun testSearchDisplaysCorrectResultsForEvents(){
        signInWithUser(composeTestRule, User1.EMAIL, User1.PASSWORD)

        composeTestRule.waitUntil (5000) {
            composeTestRule.onNodeWithTag(HomeTestTags.SCREEN).isDisplayed()
        }

        Thread.sleep(20000)
        composeTestRule.onNodeWithTag(HomeTestTags.SEARCH_BAR_INPUT).assertIsDisplayed()
        composeTestRule.onNodeWithTag(HomeTestTags.SEARCH_BAR_INPUT).performTextInput("Weekend")

        //Wait for "server's" response to get the event
        Thread.sleep(5000)
        composeTestRule.onAllNodesWithTag(EventCardTestTags.EVENT_ITEM).assertCountEquals(1)

        composeTestRule.onNodeWithTag(EventCardTestTags.EVENT_ITEM).performClick()

        composeTestRule.waitUntil (5000) {
            composeTestRule.onNodeWithTag(EventDetailsTestTags.SCREEN).isDisplayed()
        }

        composeTestRule.onNodeWithTag(EventDetailsTestTags.TITLE).assertTextEquals("WeekEndSki IC")
    }
}