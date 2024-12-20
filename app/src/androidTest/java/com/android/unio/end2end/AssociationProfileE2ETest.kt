package com.android.unio.end2end

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.isDisplayed
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onFirst
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.test.performTextInput
import androidx.test.filters.LargeTest
import com.android.unio.R
import com.android.unio.assertDisplayComponentInScroll
import com.android.unio.model.association.PermissionType
import com.android.unio.model.strings.test_tags.association.AssociationProfileActionsTestTags
import com.android.unio.model.strings.test_tags.association.AssociationProfileTestTags
import com.android.unio.model.strings.test_tags.explore.ExploreTestTags
import com.android.unio.model.strings.test_tags.home.HomeTestTags
import com.android.unio.model.strings.test_tags.navigation.BottomNavBarTestTags
import com.android.unio.model.strings.test_tags.user.SomeoneElseUserProfileTestTags
import dagger.hilt.android.testing.HiltAndroidTest
import org.junit.Test

@LargeTest
@HiltAndroidTest
class AssociationProfileE2ETest : EndToEndTest() {
  @Test
  fun testAssociationProfileCanGoToSomeoneElseUserProfile() {
    signInWithUser(composeTestRule, JohnDoe.EMAIL, JohnDoe.PASSWORD)

    composeTestRule.waitUntil(10000) {
      composeTestRule.onNodeWithTag(HomeTestTags.SCREEN).isDisplayed()
    }

    composeTestRule.onNodeWithTag(BottomNavBarTestTags.EXPLORE).assertIsDisplayed()
    composeTestRule.onNodeWithTag(BottomNavBarTestTags.EXPLORE).performClick()

    composeTestRule.waitUntil(10000) {
      composeTestRule.onNodeWithTag(ExploreTestTags.EXPLORE_SCAFFOLD_TITLE).isDisplayed()
    }

    composeTestRule.onNodeWithText(ASSOCIATION_NAME).assertDisplayComponentInScroll()
    composeTestRule.onNodeWithText(ASSOCIATION_NAME).performClick()

    composeTestRule.waitUntil(10000) {
      composeTestRule.onNodeWithTag(AssociationProfileTestTags.SCREEN).isDisplayed()
    }
    Thread.sleep(1000)
    composeTestRule.onNodeWithText(ASSOCIATION_MEMBERS).assertDisplayComponentInScroll()
    composeTestRule.onNodeWithText(ASSOCIATION_MEMBERS).performClick()

    composeTestRule.waitUntil(10000) {
      composeTestRule.onNodeWithTag(SomeoneElseUserProfileTestTags.SCREEN).isDisplayed()
    }

    composeTestRule.onNodeWithTag(SomeoneElseUserProfileTestTags.NAME).assertIsDisplayed()

    composeTestRule.waitUntil(10000) {
      composeTestRule.onNodeWithTag(SomeoneElseUserProfileTestTags.GO_BACK).isDisplayed()
    }

    composeTestRule.onNodeWithTag(SomeoneElseUserProfileTestTags.GO_BACK).performClick()

    composeTestRule.waitUntil(10000) {
      composeTestRule.onNodeWithTag(AssociationProfileTestTags.GO_BACK_BUTTON).isDisplayed()
    }

    composeTestRule.onNodeWithTag(AssociationProfileTestTags.GO_BACK_BUTTON).performClick()

    signOutWithUser(composeTestRule)
  }

  @Test
  fun testUserWithoutAccessIsOnlyOnOverview() {
    signInWithUser(composeTestRule, JohnDoe.EMAIL, JohnDoe.PASSWORD)

    composeTestRule.waitUntil(10000) {
      composeTestRule.onNodeWithTag(HomeTestTags.SCREEN).isDisplayed()
    }

    composeTestRule.onNodeWithTag(BottomNavBarTestTags.EXPLORE).assertIsDisplayed()
    composeTestRule.onNodeWithTag(BottomNavBarTestTags.EXPLORE).performClick()

    composeTestRule.waitUntil(10000) {
      composeTestRule.onNodeWithTag(ExploreTestTags.EXPLORE_SCAFFOLD_TITLE).isDisplayed()
    }

    composeTestRule.onNodeWithText(ASSOCIATION_NAME).assertDisplayComponentInScroll()
    composeTestRule.onNodeWithText(ASSOCIATION_NAME).performClick()

    composeTestRule.waitUntil(10000) {
      composeTestRule.onNodeWithTag(AssociationProfileTestTags.SCREEN).isDisplayed()
    }
    Thread.sleep(1000)
    composeTestRule.waitUntil(10000) {
      composeTestRule.onNodeWithTag(AssociationProfileTestTags.CONTACT_MEMBERS_TITLE).isDisplayed()
    }
    composeTestRule.waitUntil(10000) {
      composeTestRule.onNodeWithTag(AssociationProfileTestTags.GO_BACK_BUTTON).isDisplayed()
    }

    composeTestRule.onNodeWithTag(AssociationProfileTestTags.GO_BACK_BUTTON).performClick()

    signOutWithUser(composeTestRule)
  }

  @Test
  fun testUserWithAccessCreateRole() {
    signInWithUser(composeTestRule, Admin.EMAIL, Admin.PASSWORD)

    composeTestRule.waitUntil(10000) {
      composeTestRule.onNodeWithTag(HomeTestTags.SCREEN).isDisplayed()
    }

    composeTestRule.onNodeWithTag(BottomNavBarTestTags.EXPLORE).assertIsDisplayed()
    composeTestRule.onNodeWithTag(BottomNavBarTestTags.EXPLORE).performClick()

    composeTestRule.waitUntil(10000) {
      composeTestRule.onNodeWithTag(ExploreTestTags.EXPLORE_SCAFFOLD_TITLE).isDisplayed()
    }

    composeTestRule.onNodeWithText(ASSOCIATION_NAME).assertDisplayComponentInScroll()
    composeTestRule.onNodeWithText(ASSOCIATION_NAME).performClick()

    composeTestRule.waitUntil(10000) {
      composeTestRule.onNodeWithTag(AssociationProfileTestTags.SCREEN).isDisplayed()
    }
    Thread.sleep(1000)
    composeTestRule.waitUntil(10000) {
      composeTestRule.onNodeWithTag(AssociationProfileTestTags.YOUR_ROLE_TEXT).isDisplayed()
    }
    composeTestRule.waitUntil(10000) {
      composeTestRule.onNodeWithTag(AssociationProfileTestTags.ACTIONS_PAGE).isDisplayed()
    }
    composeTestRule.onNodeWithTag(AssociationProfileTestTags.ACTIONS_PAGE).performClick()

    composeTestRule.onNodeWithTag(AssociationProfileActionsTestTags.CREATE_ROLE).performScrollTo()
    composeTestRule.onNodeWithTag(AssociationProfileActionsTestTags.CREATE_ROLE).performClick()

    composeTestRule.waitUntil(40000) {
      composeTestRule
          .onNodeWithTag(AssociationProfileActionsTestTags.CREATE_ROLE_DISPLAY_NAME)
          .isDisplayed()
    }

    // Enter role display name
    val roleDisplayName = "Test Role"
    composeTestRule
        .onNodeWithTag(AssociationProfileActionsTestTags.CREATE_ROLE_DISPLAY_NAME)
        .performTextInput(roleDisplayName)

    // Pick a color using the color picker (assuming this involves some interaction like a tap)

    // Select some permissions
    composeTestRule
        .onAllNodesWithText(PermissionType.values()[0].stringName)
        .onFirst()
        .performClick() // Select the first permission
    composeTestRule
        .onAllNodesWithText(PermissionType.values()[1].stringName)
        .onFirst()
        .performClick() // Select the second permission

    // Confirm the role creation
    composeTestRule
        .onNodeWithText(
            composeTestRule.activity.getString(
                R.string.association_profile_save_role_dialog_create))
        .performClick()

    // Verify the role was created
    composeTestRule.waitUntil(10000) {
      composeTestRule.onNodeWithText(roleDisplayName).isDisplayed()
    }

    composeTestRule.waitUntil(10000) {
      composeTestRule.onNodeWithTag(AssociationProfileTestTags.OVERVIEW_PAGE).isDisplayed()
    }
    composeTestRule.onNodeWithTag(AssociationProfileTestTags.OVERVIEW_PAGE).performClick()

    composeTestRule.waitUntil(10000) {
      composeTestRule.onNodeWithTag(AssociationProfileTestTags.GO_BACK_BUTTON).isDisplayed()
    }

    composeTestRule.onNodeWithTag(AssociationProfileTestTags.GO_BACK_BUTTON).performClick()

    signOutWithUser(composeTestRule)
  }
  /*
  @Test
  fun testUserWithAccessEditRole() {
    signInWithUser(composeTestRule, Admin.EMAIL, Admin.PASSWORD)

    composeTestRule.waitUntil(10000) {
      composeTestRule.onNodeWithTag(HomeTestTags.SCREEN).isDisplayed()
    }

    composeTestRule.onNodeWithTag(BottomNavBarTestTags.EXPLORE).assertIsDisplayed()
    composeTestRule.onNodeWithTag(BottomNavBarTestTags.EXPLORE).performClick()

    composeTestRule.waitUntil(10000) {
      composeTestRule.onNodeWithTag(ExploreTestTags.EXPLORE_SCAFFOLD_TITLE).isDisplayed()
    }

    composeTestRule.onNodeWithText(ASSOCIATION_NAME).assertDisplayComponentInScroll()
    composeTestRule.onNodeWithText(ASSOCIATION_NAME).performClick()

    composeTestRule.waitUntil(10000) {
      composeTestRule.onNodeWithTag(AssociationProfileTestTags.SCREEN).isDisplayed()
    }
    Thread.sleep(1000)
    composeTestRule.waitUntil(10000) {
      composeTestRule.onNodeWithTag(AssociationProfileTestTags.YOUR_ROLE_TEXT).isDisplayed()
    }
    composeTestRule.waitUntil(10000) {
      composeTestRule.onNodeWithTag(AssociationProfileTestTags.ACTIONS_PAGE).isDisplayed()
    }
    composeTestRule.onNodeWithTag(AssociationProfileTestTags.ACTIONS_PAGE).performClick()

    composeTestRule
        .onNodeWithTag(AssociationProfileActionsTestTags.EDIT_ROLE + "Test Role")
        .performScrollTo()
    composeTestRule
        .onNodeWithTag(AssociationProfileActionsTestTags.EDIT_ROLE + "Test Role")
        .performClick()

    composeTestRule.waitUntil(40000) {
      composeTestRule
          .onNodeWithTag(AssociationProfileActionsTestTags.CREATE_ROLE_DISPLAY_NAME)
          .isDisplayed()
    }

    // Enter role display name
    val roleDisplayName = "Edited Test Role"
    composeTestRule
        .onNodeWithTag(AssociationProfileActionsTestTags.CREATE_ROLE_DISPLAY_NAME)
        .performTextInput("Edited ")

    Espresso.closeSoftKeyboard()

    // Confirm the role creation
    composeTestRule
        .onNodeWithText(
            composeTestRule.activity.getString(R.string.association_profile_save_role_dialog_save))
        .performClick()

    // Verify the role was created
    composeTestRule.waitUntil(10000) {
      composeTestRule.onNodeWithText(roleDisplayName).isDisplayed()
    }

    composeTestRule.waitUntil(10000) {
      composeTestRule.onNodeWithTag(AssociationProfileTestTags.OVERVIEW_PAGE).isDisplayed()
    }
    composeTestRule.onNodeWithTag(AssociationProfileTestTags.OVERVIEW_PAGE).performClick()

    composeTestRule.waitUntil(10000) {
      composeTestRule.onNodeWithTag(AssociationProfileTestTags.GO_BACK_BUTTON).isDisplayed()
    }

    composeTestRule.onNodeWithTag(AssociationProfileTestTags.GO_BACK_BUTTON).performClick()

    signOutWithUser(composeTestRule)
  }

  @Test
  fun testUserWithAccessDeleteRole() {
    signInWithUser(composeTestRule, Admin.EMAIL, Admin.PASSWORD)

    composeTestRule.waitUntil(10000) {
      composeTestRule.onNodeWithTag(HomeTestTags.SCREEN).isDisplayed()
    }

    composeTestRule.onNodeWithTag(BottomNavBarTestTags.EXPLORE).assertIsDisplayed()
    composeTestRule.onNodeWithTag(BottomNavBarTestTags.EXPLORE).performClick()

    composeTestRule.waitUntil(10000) {
      composeTestRule.onNodeWithTag(ExploreTestTags.EXPLORE_SCAFFOLD_TITLE).isDisplayed()
    }

    composeTestRule.onNodeWithText(ASSOCIATION_NAME).assertDisplayComponentInScroll()
    composeTestRule.onNodeWithText(ASSOCIATION_NAME).performClick()

    composeTestRule.waitUntil(10000) {
      composeTestRule.onNodeWithTag(AssociationProfileTestTags.SCREEN).isDisplayed()
    }
    Thread.sleep(1000)
    composeTestRule.waitUntil(10000) {
      composeTestRule.onNodeWithTag(AssociationProfileTestTags.YOUR_ROLE_TEXT).isDisplayed()
    }
    composeTestRule.waitUntil(10000) {
      composeTestRule.onNodeWithTag(AssociationProfileTestTags.ACTIONS_PAGE).isDisplayed()
    }
    composeTestRule.onNodeWithTag(AssociationProfileTestTags.ACTIONS_PAGE).performClick()

    composeTestRule
        .onNodeWithTag(AssociationProfileActionsTestTags.DELETE_ROLE + "Edited Test Role")
        .performScrollTo()
    composeTestRule
        .onNodeWithTag(AssociationProfileActionsTestTags.DELETE_ROLE + "Edited Test Role")
        .performClick()

    composeTestRule.waitUntil(50000) {
      composeTestRule.onNodeWithTag(AssociationProfileTestTags.OVERVIEW_PAGE).isDisplayed()
    }
    composeTestRule.onNodeWithTag(AssociationProfileTestTags.OVERVIEW_PAGE).performClick()

    composeTestRule.waitUntil(10000) {
      composeTestRule.onNodeWithTag(AssociationProfileTestTags.GO_BACK_BUTTON).isDisplayed()
    }

    composeTestRule.onNodeWithTag(AssociationProfileTestTags.GO_BACK_BUTTON).performClick()

    signOutWithUser(composeTestRule)
  }*/

  private companion object AssociationTarget {
    const val ASSOCIATION_NAME = "Ebou"
    const val ASSOCIATION_MEMBERS = "Renata Mendoza Flores"
  }
}
