package com.android.unio.ui.authentication.overlay

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.test.performTextInput
import com.android.unio.model.user.UserSocial
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class SocialOverlayTest {
    val userSocials = emptyList<UserSocial>().toMutableList()

    @get:Rule val composeTestRule = createComposeRule()

    @Before
    fun setUp(){
        composeTestRule.setContent { SocialOverlay({}, {}, userSocials) }
    }

    @Test
    fun everythingIsDisplayedWhenBlank(){
        composeTestRule.onNodeWithTag("SocialOverlayTitle").assertIsDisplayed()
        composeTestRule.onNodeWithTag("SocialOverlaySubtitle").assertIsDisplayed()
        composeTestRule.onNodeWithTag("SocialOverlayAddButton").assertIsDisplayed()
        composeTestRule.onNodeWithTag("SocialOverlaySaveButton").assertIsDisplayed()
    }

    @Test
    fun testSocialPromptAppearsWhenAddButtonClicked(){
        composeTestRule.onNodeWithTag("SocialOverlayAddButton").performScrollTo().performClick()
        composeTestRule.onNodeWithTag("SocialPromptCard").assertIsDisplayed()
    }

    @Test
    fun testCorrectlyAddsNewUserSocial(){
        addNewUserSocial("facebook_username", "Facebook")
        composeTestRule.onNodeWithTag("SocialOverlayClickableRow: Facebook").assertIsDisplayed()
    }

    @Test
    fun testCorrectlyDeletesUserSocial(){
        addNewUserSocial("facebook_username", "Facebook")
        composeTestRule.onNodeWithTag("SocialOverlayCloseIcon: Facebook", useUnmergedTree = true).performScrollTo().performClick()
        composeTestRule.onNodeWithTag("SocialOverlayClickableRow: Facebook").assertDoesNotExist()
    }

    @Test
    fun testCancelButtonExistsSocialPrompt(){
        composeTestRule.onNodeWithTag("SocialOverlayAddButton").performScrollTo().performClick()
        composeTestRule.onNodeWithTag("SocialPromptCancelButton").performClick()
        composeTestRule.onNodeWithTag("SocialPromptCard").assertDoesNotExist()
    }

    @Test
    fun testDisplayErrorWithIncorrectInput(){
        addNewUserSocial("", "Facebook")
        composeTestRule.onNodeWithTag("SocialPromptErrorText", useUnmergedTree = true).assertIsDisplayed()

    }

    //Helper function to add a new UserSocial
    private fun addNewUserSocial(username: String, platform: String){
        composeTestRule.onNodeWithTag("SocialOverlayAddButton").performScrollTo().performClick()
        composeTestRule.onNodeWithTag("SocialPromptTextField").performTextInput(username)
        composeTestRule.onNodeWithTag("SocialPromptDropdownMenuBox").performClick()
        composeTestRule.onNodeWithTag("SocialPromptDropdownMenuItem: $platform").performClick()
        composeTestRule.onNodeWithTag("SocialPromptSaveButton").performClick()
    }
}