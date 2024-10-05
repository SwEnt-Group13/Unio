package com.android.unio.ui

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertTextEquals
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextClearance
import androidx.compose.ui.test.performTextInput
import com.android.unio.ui.explore.ExploreScreen
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.Mockito.mock
import org.mockito.Mockito.`when`
import org.mockito.kotlin.any
import org.mockito.kotlin.never
import org.mockito.kotlin.verify

class ExploreScreen {
    @get:Rule val composeTestRule = createComposeRule()

    @Test
    fun displayAllComponents() {
        composeTestRule.setContent { ExploreScreen() }
        composeTestRule.onNodeWithTag("exploreScreen").assertIsDisplayed()
        composeTestRule.onNodeWithTag("searchBar").assertIsDisplayed()
    }

}