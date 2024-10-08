package com.android.unio.ui

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import com.android.unio.ui.home.HomeScreen
import com.android.unio.ui.navigation.NavigationAction
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.kotlin.mock

class BottomNavigationTest {

    private lateinit var navigationAction: NavigationAction

    @get:Rule
    val composeTestRule = createComposeRule()

    @Before
    fun setUp() {
        navigationAction = mock { NavigationAction::class.java }
    }

    @Test
    fun testBottomNavigationMenuDisplayed() {
        composeTestRule.setContent {
            HomeScreen(navigationAction)
        }
        composeTestRule.onNodeWithTag("bottomNavigationMenu").assertIsDisplayed()
    }
}