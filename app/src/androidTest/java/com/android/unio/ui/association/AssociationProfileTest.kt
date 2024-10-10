package com.android.unio.ui.association

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.navigation.NavHostController
import com.android.unio.ui.navigation.NavigationAction
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify

class AssociationProfileTest {
    private lateinit var navHostController: NavHostController
    private lateinit var navigationAction: NavigationAction

    @get:Rule
    val composeTestRule = createComposeRule()

    @Before
    fun setUp() {
        navHostController = mock { NavHostController::class.java }
        navigationAction = NavigationAction(navHostController)
    }

    @Test
    fun testAssociationProfileDisplayed() {
        composeTestRule.setContent { AssociationProfile(navigationAction) }

        composeTestRule.onNodeWithTag("AssociationProfileTitle").assertIsDisplayed()
        composeTestRule.onNodeWithText("Association Profile").assertIsDisplayed()

        composeTestRule.onNodeWithTag("goBackButton").assertIsDisplayed()

        composeTestRule.onNodeWithTag("AssociationScreen").assertIsDisplayed()
        composeTestRule.onNodeWithText("Association screen").assertIsDisplayed()
    }

    @Test
    fun testGoBackButton() {
        composeTestRule.setContent { AssociationProfile(navigationAction) }

        composeTestRule.onNodeWithTag("goBackButton").performClick()

        verify(navHostController).popBackStack()
    }

}