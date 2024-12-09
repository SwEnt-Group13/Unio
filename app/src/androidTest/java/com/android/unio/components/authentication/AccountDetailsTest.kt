package com.android.unio.components.authentication

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsNotDisplayed
import androidx.compose.ui.test.assertTextContains
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.test.performTextClearance
import androidx.compose.ui.test.performTextInput
import com.android.unio.TearDown
import com.android.unio.addNewUserSocial
import com.android.unio.model.image.ImageRepositoryFirebaseStorage
import com.android.unio.model.image.ImageViewModel
import com.android.unio.model.strings.TextLengthSamples
import com.android.unio.model.strings.test_tags.AccountDetailsTestTags
import com.android.unio.model.strings.test_tags.InterestsOverlayTestTags
import com.android.unio.model.strings.test_tags.SocialsOverlayTestTags
import com.android.unio.model.user.UserViewModel
import com.android.unio.ui.authentication.AccountDetailsScreen
import com.android.unio.ui.navigation.NavigationAction
import com.android.unio.ui.navigation.Screen
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth
import com.google.firebase.auth.internal.zzac
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
import org.mockito.kotlin.verify

@HiltAndroidTest
class AccountDetailsTest : TearDown() {

  @MockK private lateinit var firebaseAuth: FirebaseAuth
  private lateinit var navigationAction: NavigationAction
  @MockK private lateinit var userViewModel: UserViewModel
  @MockK private lateinit var imageRepository: ImageRepositoryFirebaseStorage

  private lateinit var imageViewModel: ImageViewModel

  // This is the implementation of the abstract method getUid() from FirebaseUser.
  // Because it is impossible to mock abstract method, this is the only way to mock it.
  @MockK private lateinit var mockFirebaseUser: zzac

  @get:Rule val composeTestRule = createComposeRule()
  @get:Rule val hiltRule = HiltAndroidRule(this)

  @Before
  fun setUp() {
    MockKAnnotations.init(this, relaxed = true)

    // Mocking the Firebase.auth object and it's behaviour
    mockkStatic(FirebaseAuth::class)
    every { Firebase.auth } returns firebaseAuth
    every { firebaseAuth.currentUser } returns mockFirebaseUser
    every { mockFirebaseUser.uid } returns "mocked-uid"

    // Mocking the UserRepositoryFirestore object
    userViewModel = mockk(relaxed = true)
    every { userViewModel.addUser(any(), any()) } answers
        {
          val onSuccess = it.invocation.args[1] as () -> Unit
          onSuccess()
        }

    // Mocking the navigationAction object
    navigationAction = mock(NavigationAction::class.java)
    `when`(navigationAction.getCurrentRoute()).thenReturn(Screen.ACCOUNT_DETAILS)

    imageViewModel = ImageViewModel(imageRepository)

    composeTestRule.setContent {
      AccountDetailsScreen(navigationAction, userViewModel, imageViewModel)
    }
  }

  @Test
  fun testEverythingIsDisplayed() {
    composeTestRule.onNodeWithTag(AccountDetailsTestTags.TITLE_TEXT).assertIsDisplayed()
    composeTestRule
        .onNodeWithTag(AccountDetailsTestTags.FIRST_NAME_TEXT, useUnmergedTree = true)
        .assertIsDisplayed()
    composeTestRule
        .onNodeWithTag(AccountDetailsTestTags.LAST_NAME_TEXT, useUnmergedTree = true)
        .assertIsDisplayed()
    composeTestRule.onNodeWithTag(AccountDetailsTestTags.BIOGRAPHY_TEXT_FIELD).assertExists()
    composeTestRule.onNodeWithTag(AccountDetailsTestTags.PROFILE_PICTURE_TEXT).assertExists()
    composeTestRule.onNodeWithTag(AccountDetailsTestTags.PROFILE_PICTURE_ICON).assertExists()
    composeTestRule.onNodeWithTag(AccountDetailsTestTags.INTERESTS_BUTTON).assertExists()
    composeTestRule.onNodeWithTag(AccountDetailsTestTags.SOCIALS_BUTTON).assertExists()
    composeTestRule.onNodeWithTag(AccountDetailsTestTags.CONTINUE_BUTTON).assertExists()
  }

  @Test
  fun testOutLinedTextFieldsWorkCorrectly() {
    composeTestRule
        .onNodeWithTag(AccountDetailsTestTags.FIRST_NAME_TEXT_FIELD)
        .performTextInput("John")
    composeTestRule
        .onNodeWithTag(AccountDetailsTestTags.LAST_NAME_TEXT_FIELD)
        .performTextInput("Doe")
    composeTestRule
        .onNodeWithTag(AccountDetailsTestTags.BIOGRAPHY_TEXT_FIELD)
        .performTextInput("I am a student")

    composeTestRule
        .onNodeWithTag(AccountDetailsTestTags.FIRST_NAME_TEXT_FIELD)
        .assertTextContains("John")
    composeTestRule
        .onNodeWithTag(AccountDetailsTestTags.LAST_NAME_TEXT_FIELD)
        .assertTextContains("Doe")
    composeTestRule
        .onNodeWithTag(AccountDetailsTestTags.BIOGRAPHY_TEXT_FIELD)
        .assertTextContains("I am a student")
  }

  @Test
  fun testInterestsButtonWorksCorrectly() {
    composeTestRule
        .onNodeWithTag(AccountDetailsTestTags.INTERESTS_BUTTON)
        .performScrollTo()
        .performClick()
    composeTestRule.onNodeWithTag(InterestsOverlayTestTags.TITLE_TEXT).assertIsDisplayed()
  }

  @Test
  fun testSocialsButtonWorksCorrectly() {
    composeTestRule
        .onNodeWithTag(AccountDetailsTestTags.SOCIALS_BUTTON)
        .performScrollTo()
        .performClick()
    composeTestRule.onNodeWithTag(SocialsOverlayTestTags.TITLE_TEXT).assertIsDisplayed()
  }

  @Test
  fun testAddingInterestsCorrectlyModifiesTheFlowRow() {
    composeTestRule.onNodeWithTag(AccountDetailsTestTags.INTERESTS_BUTTON).performClick()
    composeTestRule
        .onNodeWithTag(InterestsOverlayTestTags.CLICKABLE_ROW + "SPORTS")
        .performScrollTo()
        .performClick()
    composeTestRule
        .onNodeWithTag(InterestsOverlayTestTags.CLICKABLE_ROW + "GAMING")
        .performScrollTo()
        .performClick()
    composeTestRule.onNodeWithTag(InterestsOverlayTestTags.SAVE_BUTTON).performClick()

    composeTestRule.onNodeWithTag(AccountDetailsTestTags.INTERESTS_CHIP + "SPORTS").assertExists()
    composeTestRule.onNodeWithTag(AccountDetailsTestTags.INTERESTS_CHIP + "GAMING").assertExists()
  }

  @Test
  fun testAddingSocialsCorrectlyModifiesTheFlowRow() {
    composeTestRule
        .onNodeWithTag(AccountDetailsTestTags.SOCIALS_BUTTON)
        .performScrollTo()
        .performClick()
    addNewUserSocial(composeTestRule, "facebook_username", "Facebook")
    addNewUserSocial(composeTestRule, "instagram_username", "Instagram")

    composeTestRule.waitForIdle()

    composeTestRule
        .onNodeWithTag(SocialsOverlayTestTags.SAVE_BUTTON)
        .performScrollTo()
        .performClick()
    composeTestRule
        .onNodeWithTag(AccountDetailsTestTags.SOCIALS_CHIP + "Facebook")
        .performScrollTo()
        .assertIsDisplayed()
    composeTestRule
        .onNodeWithTag(AccountDetailsTestTags.SOCIALS_CHIP + "Instagram", true)
        .performScrollTo()
        .assertIsDisplayed()
  }

  @Test
  fun testCorrectlyExitsInterestOverlayScreen() {
    composeTestRule.onNodeWithTag(AccountDetailsTestTags.INTERESTS_BUTTON).performClick()
    composeTestRule.onNodeWithTag(InterestsOverlayTestTags.SAVE_BUTTON).performClick()
    composeTestRule.onNodeWithTag(InterestsOverlayTestTags.TITLE_TEXT).assertIsNotDisplayed()
  }

  @Test
  fun testCorrectlyDisplaysErrorWhenFirstNameIsEmpty() {
    composeTestRule
        .onNodeWithTag(AccountDetailsTestTags.CONTINUE_BUTTON)
        .performScrollTo()
        .performClick()
    composeTestRule
        .onNodeWithTag(AccountDetailsTestTags.FIRST_NAME_ERROR_TEXT, useUnmergedTree = true)
        .assertIsDisplayed()
    composeTestRule
        .onNodeWithTag(AccountDetailsTestTags.LAST_NAME_ERROR_TEXT, useUnmergedTree = true)
        .assertIsDisplayed()
  }

  @Test
  fun testCorrectlyDisplaysCharacterCountForTextFields() {
    composeTestRule
        .onNodeWithTag(AccountDetailsTestTags.FIRST_NAME_TEXT_FIELD)
        .performScrollTo()
        .performTextInput(TextLengthSamples.SMALL)
    composeTestRule
        .onNodeWithTag(AccountDetailsTestTags.FIRST_NAME_CHARACTER_COUNTER, useUnmergedTree = true)
        .assertExists()
    composeTestRule
        .onNodeWithTag(AccountDetailsTestTags.FIRST_NAME_TEXT_FIELD)
        .performTextClearance()

    composeTestRule
        .onNodeWithTag(AccountDetailsTestTags.LAST_NAME_TEXT_FIELD)
        .performScrollTo()
        .performTextInput(TextLengthSamples.SMALL)
    composeTestRule
        .onNodeWithTag(AccountDetailsTestTags.LAST_NAME_CHARACTER_COUNTER, useUnmergedTree = true)
        .assertExists()
    composeTestRule
        .onNodeWithTag(AccountDetailsTestTags.LAST_NAME_TEXT_FIELD)
        .performTextClearance()

    composeTestRule
        .onNodeWithTag(AccountDetailsTestTags.BIOGRAPHY_TEXT_FIELD)
        .performScrollTo()
        .performTextInput(TextLengthSamples.LARGE)
    composeTestRule
        .onNodeWithTag(AccountDetailsTestTags.BIOGRAPHY_CHARACTER_COUNTER, useUnmergedTree = true)
        .assertExists()
    composeTestRule
        .onNodeWithTag(AccountDetailsTestTags.BIOGRAPHY_TEXT_FIELD)
        .performTextClearance()
  }

  @Test
  fun testContinueButtonCorrectlyNavigatesToHome() {
    composeTestRule
        .onNodeWithTag(AccountDetailsTestTags.FIRST_NAME_TEXT_FIELD)
        .performTextInput("John")
    composeTestRule
        .onNodeWithTag(AccountDetailsTestTags.LAST_NAME_TEXT_FIELD)
        .performTextInput("Doe")
    composeTestRule
        .onNodeWithTag(AccountDetailsTestTags.CONTINUE_BUTTON)
        .performScrollTo()
        .performClick()
    verify(navigationAction).navigateTo(screen = Screen.HOME)
  }
}
