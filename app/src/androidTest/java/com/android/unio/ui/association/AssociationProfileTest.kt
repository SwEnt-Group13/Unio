package com.android.unio.ui.association

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
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

  @get:Rule val composeTestRule = createComposeRule()

  @Before
  fun setUp() {
    navHostController = mock { NavHostController::class.java }
    navigationAction = NavigationAction(navHostController)
  }

  @Test
  fun testAssociationProfileDisplayed() {
    composeTestRule.setContent { AssociationProfileScreen(navigationAction) }

    composeTestRule.onNodeWithTag("AssociationScreen").assertIsDisplayed()
    composeTestRule.onNodeWithTag("AssociationEventTitle").assertIsDisplayed()
    composeTestRule.onNodeWithTag("AssociationDescription").assertIsDisplayed()
    composeTestRule.onNodeWithTag("AssociationImageHeader").assertIsDisplayed()
    composeTestRule.onNodeWithTag("AssociationProfileTitle").assertIsDisplayed()
    composeTestRule.onNodeWithTag("goBackButton").assertIsDisplayed()
    composeTestRule.onNodeWithTag("AssociationContactMembersTitle").assertIsDisplayed()
    composeTestRule.onNodeWithTag("AssociationContactMembersCard").assertIsDisplayed()
    composeTestRule.onNodeWithTag("AssociationEventCard").assertIsDisplayed()
    composeTestRule.onNodeWithTag("AssociationSeeMoreButton").assertIsDisplayed()
    composeTestRule.onNodeWithTag("AssociationHeaderFollowers").assertIsDisplayed()
    composeTestRule.onNodeWithTag("AssociationHeaderMembers").assertIsDisplayed()
    composeTestRule.onNodeWithTag("AssociationFollowButton").assertIsDisplayed()

    composeTestRule.onNodeWithTag("AssociationRecruitmentRoles").performScrollTo()

    composeTestRule.onNodeWithTag("AssociationRecruitmentTitle").assertIsDisplayed()
    composeTestRule.onNodeWithTag("AssociationRecruitmentDescription").assertIsDisplayed()
    composeTestRule.onNodeWithTag("AssociationRecruitmentRoles").assertIsDisplayed()
  }

  @Test
  fun testGoBackButton() {
    composeTestRule.setContent { AssociationProfileScreen(navigationAction) }

    composeTestRule.onNodeWithTag("goBackButton").performClick()

    verify(navHostController).popBackStack()
  }
}
