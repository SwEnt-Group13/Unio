package com.android.unio.components.user

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsNotDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.test.performTextClearance
import com.android.unio.TearDown
import com.android.unio.addNewUserSocial
import com.android.unio.mocks.user.MockUser
import com.android.unio.model.strings.test_tags.InterestsOverlayTestTags
import com.android.unio.model.strings.test_tags.SocialsOverlayTestTags
import com.android.unio.model.strings.test_tags.UserEditionTestTags
import com.android.unio.ui.navigation.NavigationAction
import com.android.unio.ui.navigation.Screen
import com.android.unio.ui.user.UserProfileEditionScreenContent
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import io.mockk.MockKAnnotations
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.Mockito.mock
import org.mockito.Mockito.`when`

@HiltAndroidTest
class UserProfileEditionTest : TearDown() {
  private lateinit var navigationAction: NavigationAction

  @get:Rule val composeTestRule = createComposeRule()
  @get:Rule val hiltRule = HiltAndroidRule(this)

  @Before
  fun setUp() {
    MockKAnnotations.init(this, relaxed = true)

    // Mocking the navigationAction object
    navigationAction = mock(NavigationAction::class.java)
    `when`(navigationAction.getCurrentRoute()).thenReturn(Screen.EDIT_PROFILE)

    val user = MockUser.createMockUser(interests = emptyList(), profilePicture = "")

    composeTestRule.setContent {
      UserProfileEditionScreenContent(
          user,
          onDiscardChanges = { navigationAction.goBack() },
          { uri, method -> method("") },
          {},
          {})
    }
  }

  @Test
  fun testEverythingIsDisplayed() {
    composeTestRule.onNodeWithTag(UserEditionTestTags.DISCARD_TEXT).assertIsDisplayed()
    composeTestRule
        .onNodeWithTag(UserEditionTestTags.FIRST_NAME_TEXT, useUnmergedTree = true)
        .assertIsDisplayed()
    composeTestRule
        .onNodeWithTag(UserEditionTestTags.LAST_NAME_TEXT, useUnmergedTree = true)
        .assertIsDisplayed()
    composeTestRule.onNodeWithTag(UserEditionTestTags.BIOGRAPHY_TEXT_FIELD).assertExists()
    composeTestRule.onNodeWithTag(UserEditionTestTags.PROFILE_PICTURE_ICON).assertExists()
    composeTestRule.onNodeWithTag(UserEditionTestTags.INTERESTS_BUTTON).assertExists()
    composeTestRule.onNodeWithTag(UserEditionTestTags.SOCIALS_BUTTON).assertExists()
    composeTestRule.onNodeWithTag(UserEditionTestTags.SAVE_BUTTON).assertExists()
  }

  @Test
  fun testInterestsButtonWorksCorrectly() {
    composeTestRule
        .onNodeWithTag(UserEditionTestTags.INTERESTS_BUTTON)
        .performScrollTo()
        .performClick()
    composeTestRule.onNodeWithTag(InterestsOverlayTestTags.TITLE_TEXT).assertIsDisplayed()
  }

  @Test
  fun testSocialsButtonWorksCorrectly() {
    composeTestRule
        .onNodeWithTag(UserEditionTestTags.SOCIALS_BUTTON)
        .performScrollTo()
        .performClick()
    composeTestRule.onNodeWithTag(SocialsOverlayTestTags.TITLE_TEXT).assertIsDisplayed()
  }

  @Test
  fun testAddingInterestsCorrectlyModifiesTheFlowRow() {
    composeTestRule
        .onNodeWithTag(UserEditionTestTags.INTERESTS_BUTTON)
        .performScrollTo()
        .performClick()
    composeTestRule
        .onNodeWithTag(InterestsOverlayTestTags.CLICKABLE_ROW + "SPORTS")
        .performScrollTo()
        .performClick()
    composeTestRule
        .onNodeWithTag(InterestsOverlayTestTags.CLICKABLE_ROW + "GAMING")
        .performScrollTo()
        .performClick()
    composeTestRule.onNodeWithTag(InterestsOverlayTestTags.SAVE_BUTTON).performClick()

    composeTestRule.onNodeWithTag(UserEditionTestTags.INTERESTS_CHIP + "SPORTS").assertExists()
    composeTestRule.onNodeWithTag(UserEditionTestTags.INTERESTS_CHIP + "GAMING").assertExists()
  }

  @Test
  fun testAddingSocialsCorrectlyModifiesTheFlowRow() {
    composeTestRule
        .onNodeWithTag(UserEditionTestTags.SOCIALS_BUTTON)
        .performScrollTo()
        .performClick()
    addNewUserSocial(composeTestRule, "snap_username", "Snapchat")
    addNewUserSocial(composeTestRule, "facebook_username", "Facebook")
    composeTestRule.onNodeWithTag(SocialsOverlayTestTags.SAVE_BUTTON).performClick()
    composeTestRule
        .onNodeWithTag(UserEditionTestTags.SOCIALS_CHIP + "Snapchat", true)
        .assertExists()
    composeTestRule
        .onNodeWithTag(UserEditionTestTags.SOCIALS_CHIP + "Facebook", true)
        .assertExists()
  }

  @Test
  fun testCorrectlyExitsInterestOverlayScreen() {
    composeTestRule
        .onNodeWithTag(UserEditionTestTags.INTERESTS_BUTTON)
        .performScrollTo()
        .performClick()
    composeTestRule.onNodeWithTag(InterestsOverlayTestTags.SAVE_BUTTON).performClick()
    composeTestRule.onNodeWithTag(InterestsOverlayTestTags.TITLE_TEXT).assertIsNotDisplayed()
  }

  @Test
  fun testCorrectlyDisplaysErrorWhenFirstNameIsEmpty() {
    composeTestRule.onNodeWithTag(UserEditionTestTags.FIRST_NAME_TEXT_FIELD).performTextClearance()
    composeTestRule.onNodeWithTag(UserEditionTestTags.LAST_NAME_TEXT_FIELD).performTextClearance()

    composeTestRule.onNodeWithTag(UserEditionTestTags.SAVE_BUTTON).performScrollTo().performClick()
    composeTestRule
        .onNodeWithTag(UserEditionTestTags.FIRST_NAME_ERROR_TEXT, useUnmergedTree = true)
        .assertExists()
    composeTestRule
        .onNodeWithTag(UserEditionTestTags.LAST_NAME_ERROR_TEXT, useUnmergedTree = true)
        .assertExists()
  }
}
