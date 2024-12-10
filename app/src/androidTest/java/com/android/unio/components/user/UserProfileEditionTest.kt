package com.android.unio.components.user

import android.net.ConnectivityManager
import android.net.Network
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsNotDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.test.performTextClearance
import androidx.compose.ui.test.performTextInput
import androidx.core.content.ContextCompat
import androidx.core.content.ContextCompat.getSystemService
import com.android.unio.TearDown
import com.android.unio.addNewUserSocial
import com.android.unio.assertDisplayComponentInScroll
import com.android.unio.mocks.user.MockUser
import com.android.unio.model.strings.TextLengthSamples
import com.android.unio.model.strings.test_tags.authentication.InterestsOverlayTestTags
import com.android.unio.model.strings.test_tags.authentication.SocialsOverlayTestTags
import com.android.unio.model.strings.test_tags.user.UserEditionTestTags
import com.android.unio.model.user.User
import com.android.unio.ui.navigation.NavigationAction
import com.android.unio.ui.navigation.Screen
import com.android.unio.ui.user.UserProfileEditionScreenScaffold
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import io.mockk.MockKAnnotations
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.mockk
import io.mockk.mockkStatic
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

  @MockK private lateinit var connectivityManager: ConnectivityManager

  private lateinit var user: User
  private var isOnlineUpdated: Boolean = false

  @Before
  fun setUp() {
    MockKAnnotations.init(this, relaxed = true)

    // Mocking the navigationAction object
    navigationAction = mock(NavigationAction::class.java)
    `when`(navigationAction.getCurrentRoute()).thenReturn(Screen.EDIT_PROFILE)

    user = MockUser.createMockUser(interests = emptyList(), profilePicture = "")

    val onOfflineChange = { newUser: User ->
      user = newUser
      isOnlineUpdated = false
    }

    val onOnlineChange = { newUser: User ->
      user = newUser
      isOnlineUpdated = true
    }

    mockkStatic(Network::class)
    mockkStatic(ContextCompat::class)
    every { getSystemService(any(), ConnectivityManager::class.java) } returns connectivityManager

    composeTestRule.setContent {
      UserProfileEditionScreenScaffold(
          user,
          { navigationAction.goBack() },
          { uri, method -> method("") },
          onOnlineChange,
          onOfflineChange,
          {})
    }
  }

  @Test
  fun testUpdateUserOffline() {
    every { connectivityManager?.activeNetwork } returns null

    composeTestRule
        .onNodeWithTag(UserEditionTestTags.FIRST_NAME_TEXT_FIELD)
        .assertDisplayComponentInScroll()
    composeTestRule.onNodeWithTag(UserEditionTestTags.FIRST_NAME_TEXT_FIELD).performTextClearance()
    composeTestRule.onNodeWithTag(UserEditionTestTags.FIRST_NAME_TEXT_FIELD).performClick()
    composeTestRule
        .onNodeWithTag(UserEditionTestTags.FIRST_NAME_TEXT_FIELD)
        .performTextInput(UserUpdate.FIRST_NAME)

    composeTestRule
        .onNodeWithTag(UserEditionTestTags.LAST_NAME_TEXT_FIELD)
        .assertDisplayComponentInScroll()
    composeTestRule.onNodeWithTag(UserEditionTestTags.LAST_NAME_TEXT_FIELD).performTextClearance()
    composeTestRule.onNodeWithTag(UserEditionTestTags.LAST_NAME_TEXT_FIELD).performClick()
    composeTestRule
        .onNodeWithTag(UserEditionTestTags.LAST_NAME_TEXT_FIELD)
        .performTextInput(UserUpdate.LAST_NAME)

    composeTestRule
        .onNodeWithTag(UserEditionTestTags.BIOGRAPHY_TEXT_FIELD)
        .assertDisplayComponentInScroll()
    composeTestRule.onNodeWithTag(UserEditionTestTags.BIOGRAPHY_TEXT_FIELD).performTextClearance()
    composeTestRule.onNodeWithTag(UserEditionTestTags.BIOGRAPHY_TEXT_FIELD).performClick()
    composeTestRule
        .onNodeWithTag(UserEditionTestTags.BIOGRAPHY_TEXT_FIELD)
        .performTextInput(UserUpdate.BIOGRAPHY)

    composeTestRule.onNodeWithTag(UserEditionTestTags.SAVE_BUTTON).assertDisplayComponentInScroll()
    composeTestRule.onNodeWithTag(UserEditionTestTags.SAVE_BUTTON).performClick()

    assert(user.firstName == UserUpdate.FIRST_NAME)
    assert(user.lastName == UserUpdate.LAST_NAME)
    assert(user.biography == UserUpdate.BIOGRAPHY)
    assert(!isOnlineUpdated)
  }

  @Test
  fun testUpdateUserOnline() {
    every { connectivityManager?.activeNetwork } returns mockk<Network>()

    composeTestRule
        .onNodeWithTag(UserEditionTestTags.FIRST_NAME_TEXT_FIELD)
        .assertDisplayComponentInScroll()
    composeTestRule.onNodeWithTag(UserEditionTestTags.FIRST_NAME_TEXT_FIELD).performTextClearance()
    composeTestRule.onNodeWithTag(UserEditionTestTags.FIRST_NAME_TEXT_FIELD).performClick()
    composeTestRule
        .onNodeWithTag(UserEditionTestTags.FIRST_NAME_TEXT_FIELD)
        .performTextInput(UserUpdate.FIRST_NAME)

    composeTestRule
        .onNodeWithTag(UserEditionTestTags.LAST_NAME_TEXT_FIELD)
        .assertDisplayComponentInScroll()
    composeTestRule.onNodeWithTag(UserEditionTestTags.LAST_NAME_TEXT_FIELD).performTextClearance()
    composeTestRule.onNodeWithTag(UserEditionTestTags.LAST_NAME_TEXT_FIELD).performClick()
    composeTestRule
        .onNodeWithTag(UserEditionTestTags.LAST_NAME_TEXT_FIELD)
        .performTextInput(UserUpdate.LAST_NAME)

    composeTestRule
        .onNodeWithTag(UserEditionTestTags.BIOGRAPHY_TEXT_FIELD)
        .assertDisplayComponentInScroll()
    composeTestRule.onNodeWithTag(UserEditionTestTags.BIOGRAPHY_TEXT_FIELD).performTextClearance()
    composeTestRule.onNodeWithTag(UserEditionTestTags.BIOGRAPHY_TEXT_FIELD).performClick()
    composeTestRule
        .onNodeWithTag(UserEditionTestTags.BIOGRAPHY_TEXT_FIELD)
        .performTextInput(UserUpdate.BIOGRAPHY)

    composeTestRule.onNodeWithTag(UserEditionTestTags.SAVE_BUTTON).assertDisplayComponentInScroll()
    composeTestRule.onNodeWithTag(UserEditionTestTags.SAVE_BUTTON).performClick()

    assert(user.firstName == UserUpdate.FIRST_NAME)
    assert(user.lastName == UserUpdate.LAST_NAME)
    assert(user.biography == UserUpdate.BIOGRAPHY)
    assert(isOnlineUpdated)
  }

  @Test
  fun testEverythingIsDisplayed() {
    every { connectivityManager?.activeNetwork } returns mockk<Network>()

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
    every { connectivityManager?.activeNetwork } returns mockk<Network>()

    composeTestRule
        .onNodeWithTag(UserEditionTestTags.INTERESTS_BUTTON)
        .performScrollTo()
        .performClick()
    composeTestRule.onNodeWithTag(InterestsOverlayTestTags.TITLE_TEXT).assertIsDisplayed()
  }

  @Test
  fun testSocialsButtonWorksCorrectly() {
    every { connectivityManager?.activeNetwork } returns mockk<Network>()

    composeTestRule
        .onNodeWithTag(UserEditionTestTags.SOCIALS_BUTTON)
        .performScrollTo()
        .performClick()
    composeTestRule.onNodeWithTag(SocialsOverlayTestTags.TITLE_TEXT).assertIsDisplayed()
  }

  @Test
  fun testAddingInterestsCorrectlyModifiesTheFlowRow() {
    every { connectivityManager?.activeNetwork } returns mockk<Network>()

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
    every { connectivityManager?.activeNetwork } returns mockk<Network>()

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
    every { connectivityManager?.activeNetwork } returns mockk<Network>()

    composeTestRule
        .onNodeWithTag(UserEditionTestTags.INTERESTS_BUTTON)
        .performScrollTo()
        .performClick()
    composeTestRule.onNodeWithTag(InterestsOverlayTestTags.SAVE_BUTTON).performClick()
    composeTestRule.onNodeWithTag(InterestsOverlayTestTags.TITLE_TEXT).assertIsNotDisplayed()
  }

  @Test
  fun testCorrectlyDisplaysErrorWhenFirstNameIsEmpty() {
    every { connectivityManager?.activeNetwork } returns mockk<Network>()

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

  @Test
  fun testCorrectlyDisplaysCharacterCountForTextFields() {
    composeTestRule
        .onNodeWithTag(UserEditionTestTags.FIRST_NAME_TEXT_FIELD)
        .performScrollTo()
        .performTextClearance()
    composeTestRule
        .onNodeWithTag(UserEditionTestTags.FIRST_NAME_TEXT_FIELD)
        .performTextInput(TextLengthSamples.SMALL)
    composeTestRule
        .onNodeWithTag(UserEditionTestTags.FIRST_NAME_CHARACTER_COUNTER, useUnmergedTree = true)
        .assertExists()
    composeTestRule.onNodeWithTag(UserEditionTestTags.FIRST_NAME_TEXT_FIELD).performTextClearance()

    composeTestRule
        .onNodeWithTag(UserEditionTestTags.LAST_NAME_TEXT_FIELD)
        .performScrollTo()
        .performTextClearance()
    composeTestRule
        .onNodeWithTag(UserEditionTestTags.LAST_NAME_TEXT_FIELD)
        .performScrollTo()
        .performTextInput(TextLengthSamples.SMALL)
    composeTestRule
        .onNodeWithTag(UserEditionTestTags.LAST_NAME_CHARACTER_COUNTER, useUnmergedTree = true)
        .assertExists()
    composeTestRule.onNodeWithTag(UserEditionTestTags.LAST_NAME_TEXT_FIELD).performTextClearance()

    composeTestRule
        .onNodeWithTag(UserEditionTestTags.BIOGRAPHY_TEXT_FIELD)
        .performScrollTo()
        .performTextClearance()
    composeTestRule
        .onNodeWithTag(UserEditionTestTags.BIOGRAPHY_TEXT_FIELD)
        .performTextInput(TextLengthSamples.LARGE)
    composeTestRule
        .onNodeWithTag(UserEditionTestTags.BIOGRAPHY_CHARACTER_COUNTER, useUnmergedTree = true)
        .assertExists()
    composeTestRule.onNodeWithTag(UserEditionTestTags.BIOGRAPHY_TEXT_FIELD).performTextClearance()
  }

  object UserUpdate {
    const val FIRST_NAME = "Johnny"
    const val LAST_NAME = "Däpp"
    const val BIOGRAPHY = "Ich bin ein Testbenutzer"
  }
}
