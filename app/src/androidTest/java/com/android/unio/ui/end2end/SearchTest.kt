package com.android.unio.ui.end2end

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.isDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.test.performTextInput
import androidx.test.filters.LargeTest
import com.android.unio.MainActivity
import com.android.unio.model.strings.test_tags.HomeTestTags
import com.android.unio.model.strings.test_tags.WelcomeTestTags
import com.android.unio.ui.end2end.UserAccountCreationTest.Companion.EMAIL
import com.android.unio.ui.end2end.UserAccountCreationTest.Companion.PWD
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import org.junit.Rule


@LargeTest
@HiltAndroidTest
class SearchTest : EndToEndTest() {

    @get:Rule val composeTestRule = createAndroidComposeRule<MainActivity>()
    @get:Rule val hiltRule = HiltAndroidRule(this)


    fun testSearchDisplaysCorrectResults(){
        composeTestRule.onNodeWithTag(WelcomeTestTags.SCREEN).assertIsDisplayed()
        composeTestRule.onNodeWithTag(WelcomeTestTags.EMAIL).performTextInput(User1.EMAIL)
        composeTestRule.onNodeWithTag(WelcomeTestTags.PASSWORD).performTextInput(User1.PASSWORD)

        composeTestRule.waitUntil (5000) {
            composeTestRule.onNodeWithTag(HomeTestTags.SCREEN).isDisplayed()
        }

        composeTestRule.onNodeWithTag(HomeTestTags.SEARCH_BAR_INPUT).assertIsDisplayed()
        composeTestRule.onNodeWithTag(HomeTestTags.SEARCH_BAR_INPUT).performTextInput("Weekendski")

        //Wait for "server's" response to get the event

//        composeTestRule.waitUntil (5000) {
//        }




    }
}