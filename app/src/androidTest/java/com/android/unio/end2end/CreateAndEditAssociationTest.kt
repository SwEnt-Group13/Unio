package com.android.unio.end2end

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.isDisplayed
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.test.performTextClearance
import androidx.compose.ui.test.performTextInput
import androidx.test.espresso.Espresso
import androidx.test.filters.LargeTest
import com.android.unio.assertDisplayComponentInScroll
import com.android.unio.model.strings.test_tags.association.SaveAssociationTestTags
import com.android.unio.model.strings.test_tags.authentication.PictureSelectionToolTestTags
import com.android.unio.model.strings.test_tags.home.HomeTestTags
import com.android.unio.model.strings.test_tags.navigation.BottomNavBarTestTags
import com.android.unio.model.strings.test_tags.user.UserProfileTestTags
import dagger.hilt.android.testing.HiltAndroidTest
import org.junit.Test

@LargeTest
@HiltAndroidTest
class CreateAndEditAssociationTest : EndToEndTest() {
    @Test
    fun testCreateAndEditAssociation() {

        // Sign in with user
        signInWithUser(composeTestRule, Admin.EMAIL, Admin.PASSWORD)

        composeTestRule.waitUntil(10000) {
            composeTestRule.onNodeWithTag(HomeTestTags.SCREEN).isDisplayed()
        }

        // Navigate to the user edition page
        composeTestRule.onNodeWithTag(BottomNavBarTestTags.MY_PROFILE).assertIsDisplayed()
        composeTestRule.onNodeWithTag(BottomNavBarTestTags.MY_PROFILE).performClick()

        composeTestRule.waitUntil(10000) {
            composeTestRule.onNodeWithTag(UserProfileTestTags.SCREEN).isDisplayed()
        }
        composeTestRule.onNodeWithTag(UserProfileTestTags.SETTINGS).assertIsDisplayed()
        composeTestRule.onNodeWithTag(UserProfileTestTags.SETTINGS).performClick()

        composeTestRule.waitUntil(10000) {
            composeTestRule.onNodeWithTag(UserProfileTestTags.BOTTOM_SHEET).isDisplayed()
        }

        composeTestRule.waitUntil(10000) {
            composeTestRule.onNodeWithTag(UserProfileTestTags.SAVE_ASSOCIATION).isDisplayed()
        }
        composeTestRule.onNodeWithTag(UserProfileTestTags.SAVE_ASSOCIATION).performClick()

        composeTestRule.waitUntil(10000) {
            composeTestRule.onNodeWithTag(SaveAssociationTestTags.SCREEN).isDisplayed()
        }

        composeTestRule
            .onNodeWithTag(SaveAssociationTestTags.CANCEL_BUTTON)
            .performScrollTo() // try to cancel
        composeTestRule.onNodeWithTag(SaveAssociationTestTags.CANCEL_BUTTON).performClick()

        composeTestRule.waitUntil(10000) {
            composeTestRule.onNodeWithTag(UserProfileTestTags.SCREEN).isDisplayed()
        }
        composeTestRule.onNodeWithTag(UserProfileTestTags.SETTINGS).assertIsDisplayed()
        composeTestRule.onNodeWithTag(UserProfileTestTags.SETTINGS).performClick()

        composeTestRule.waitUntil(10000) {
            composeTestRule.onNodeWithTag(UserProfileTestTags.BOTTOM_SHEET).isDisplayed()
        }

        composeTestRule.onNodeWithTag(UserProfileTestTags.SAVE_ASSOCIATION).assertIsDisplayed()
        composeTestRule.onNodeWithTag(UserProfileTestTags.SAVE_ASSOCIATION).performClick()

        composeTestRule.waitUntil(10000) {
            composeTestRule.onNodeWithTag(SaveAssociationTestTags.SCREEN).isDisplayed()
        }

        composeTestRule
            .onNodeWithTag(SaveAssociationTestTags.NAME_TEXT_FIELD)
            .assertDisplayComponentInScroll()
        composeTestRule.onNodeWithTag(SaveAssociationTestTags.NAME_TEXT_FIELD).performScrollTo()
        composeTestRule.onNodeWithTag(SaveAssociationTestTags.NAME_TEXT_FIELD).performTextClearance()
        composeTestRule
            .onNodeWithTag(SaveAssociationTestTags.NAME_TEXT_FIELD)
            .performTextInput("NameAssociation")

        composeTestRule
            .onNodeWithTag(SaveAssociationTestTags.FULL_NAME_TEXT_FIELD)
            .assertDisplayComponentInScroll()
        composeTestRule.onNodeWithTag(SaveAssociationTestTags.FULL_NAME_TEXT_FIELD).performScrollTo()
        composeTestRule
            .onNodeWithTag(SaveAssociationTestTags.FULL_NAME_TEXT_FIELD)
            .performTextClearance()
        composeTestRule
            .onNodeWithTag(SaveAssociationTestTags.FULL_NAME_TEXT_FIELD)
            .performTextInput("FullNameAssociation")

        composeTestRule
            .onNodeWithTag(SaveAssociationTestTags.DESCRIPTION_TEXT_FIELD)
            .assertDisplayComponentInScroll()
        composeTestRule.onNodeWithTag(SaveAssociationTestTags.DESCRIPTION_TEXT_FIELD).performScrollTo()
        composeTestRule
            .onNodeWithTag(SaveAssociationTestTags.DESCRIPTION_TEXT_FIELD)
            .performTextClearance()
        composeTestRule
            .onNodeWithTag(SaveAssociationTestTags.DESCRIPTION_TEXT_FIELD)
            .performTextInput("DescriptionAssociation")

        // picture selector
        composeTestRule.onNodeWithTag(SaveAssociationTestTags.PICTURE_BUTTON).performScrollTo()
        composeTestRule.onNodeWithTag(SaveAssociationTestTags.PICTURE_BUTTON).performClick()
        composeTestRule.waitUntil(10000) {
            composeTestRule.onNodeWithTag(PictureSelectionToolTestTags.CANCEL_BUTTON).isDisplayed()
        }
        composeTestRule.onNodeWithTag(PictureSelectionToolTestTags.CANCEL_BUTTON).performClick()

        composeTestRule
            .onNodeWithTag(SaveAssociationTestTags.URL_TEXT_FIELD)
            .assertDisplayComponentInScroll()
        composeTestRule.onNodeWithTag(SaveAssociationTestTags.URL_TEXT_FIELD).performScrollTo()
        composeTestRule.onNodeWithTag(SaveAssociationTestTags.URL_TEXT_FIELD).performTextClearance()
        composeTestRule
            .onNodeWithTag(SaveAssociationTestTags.URL_TEXT_FIELD)
            .performTextInput("URLAssociation")

        composeTestRule
            .onNodeWithTag(SaveAssociationTestTags.PRINCIPAL_EMAIL_ADDRESS_TEXT)
            .assertDisplayComponentInScroll()
        composeTestRule
            .onNodeWithTag(SaveAssociationTestTags.PRINCIPAL_EMAIL_ADDRESS_TEXT)
            .performScrollTo()
        composeTestRule
            .onNodeWithTag(SaveAssociationTestTags.PRINCIPAL_EMAIL_ADDRESS_TEXT)
            .performTextClearance()
        composeTestRule
            .onNodeWithTag(SaveAssociationTestTags.PRINCIPAL_EMAIL_ADDRESS_TEXT)
            .performTextInput("URLAssociation")

        Espresso.closeSoftKeyboard() // in order to be able to click on the save button
        Thread.sleep(1000)
        composeTestRule.onNodeWithTag(SaveAssociationTestTags.SAVE_BUTTON).performScrollTo()
        composeTestRule.onNodeWithTag(SaveAssociationTestTags.SAVE_BUTTON).performClick()

        signOutWithUser(composeTestRule)
    }
}