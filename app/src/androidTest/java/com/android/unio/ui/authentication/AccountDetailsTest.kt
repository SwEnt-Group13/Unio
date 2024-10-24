package com.android.unio.ui.authentication

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsNotDisplayed
import androidx.compose.ui.test.assertTextContains
import androidx.compose.ui.test.assertTextEquals
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.test.performTextInput
import com.android.unio.model.user.UserViewModel
import com.android.unio.ui.accountCreation.AccountDetails
import com.android.unio.ui.navigation.NavigationAction
import com.android.unio.ui.navigation.Screen
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth
import com.google.firebase.auth.internal.zzac
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

class AccountDetailsTest {

  @MockK private lateinit var firebaseAuth: FirebaseAuth
  private lateinit var navigationAction: NavigationAction
  @MockK private lateinit var userViewModel: UserViewModel

  // This is the implementation of the abstract method getUid() from FirebaseUser.
  // Because it is impossible to mock abstract method, this is the only way to mock it.
  @MockK private lateinit var mockFirebaseUser: zzac

  @get:Rule val composeTestRule = createComposeRule()

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

    // Mocking the navigationAction object
    navigationAction = mock(NavigationAction::class.java)
    `when`(navigationAction.getCurrentRoute()).thenReturn(Screen.ACCOUNT_DETAILS)

    composeTestRule.setContent { AccountDetails(navigationAction, userViewModel) }
  }

  @Test
  fun testEverythingIsDisplayed() {
    composeTestRule.onNodeWithTag("AccountDetailsTitleText").assertIsDisplayed()
    composeTestRule
        .onNodeWithTag("AccountDetailsTitleText")
        .assertTextEquals("Tell us about yourself")
    composeTestRule.onNodeWithTag("AccountDetailsFirstNameTextField").assertIsDisplayed()
    composeTestRule
        .onNodeWithTag("AccountDetailsFirstNameText", useUnmergedTree = true)
        .assertTextEquals("First name")
    composeTestRule.onNodeWithTag("AccountDetailsLastNameTextField").assertIsDisplayed()
    composeTestRule
        .onNodeWithTag("AccountDetailsLastNameText", useUnmergedTree = true)
        .assertTextEquals("Last name")
    composeTestRule.onNodeWithTag("AccountDetailsBioTextField").assertExists()
    composeTestRule
        .onNodeWithTag("AccountDetailsBioText", useUnmergedTree = true)
        .assertTextEquals("Bio")
    composeTestRule.onNodeWithTag("AccountDetailsProfilePictureText").assertExists()
    composeTestRule
        .onNodeWithTag("AccountDetailsProfilePictureText")
        .assertTextEquals("Maybe add a profile picture?")
    composeTestRule.onNodeWithTag("AccountDetailsProfilePictureIcon").assertExists()
    composeTestRule.onNodeWithTag("AccountDetailsInterestsButton").assertExists()
    composeTestRule.onNodeWithTag("AccountDetailsSocialsButton").assertExists()
    composeTestRule.onNodeWithTag("AccountDetailsContinueButton").assertExists()
  }

  @Test
  fun testOutLinedTextFieldsWorkCorrectly() {
    composeTestRule.onNodeWithTag("AccountDetailsFirstNameTextField").performTextInput("John")
    composeTestRule.onNodeWithTag("AccountDetailsLastNameTextField").performTextInput("Doe")
    composeTestRule.onNodeWithTag("AccountDetailsBioTextField").performTextInput("I am a student")

    composeTestRule.onNodeWithTag("AccountDetailsFirstNameTextField").assertTextContains("John")
    composeTestRule.onNodeWithTag("AccountDetailsLastNameTextField").assertTextContains("Doe")
    composeTestRule.onNodeWithTag("AccountDetailsBioTextField").assertTextContains("I am a student")
  }

  @Test
  fun testInterestsButtonWorksCorrectly() {
    composeTestRule.onNodeWithTag("AccountDetailsInterestsButton").performScrollTo().performClick()
    composeTestRule.onNodeWithTag("InterestOverlayTitle").assertIsDisplayed()
  }

  @Test
  fun testAddingInterestsCorrectlyModifiesTheFlowRow() {
    composeTestRule.onNodeWithTag("AccountDetailsInterestsButton").performClick()
    composeTestRule.onNodeWithTag("InterestOverlayClickableRow: 0").performClick()
    composeTestRule.onNodeWithTag("InterestOverlayClickableRow: 1").performClick()

    composeTestRule.onNodeWithTag("AccountDetailsInterestChip: 0").assertExists()
    composeTestRule.onNodeWithTag("AccountDetailsInterestChip: 1").assertExists()
  }

  @Test
  fun testCorrectlyExitsTheOverlayScreen() {
    composeTestRule.onNodeWithTag("AccountDetailsInterestsButton").performClick()
    composeTestRule.onNodeWithTag("InterestOverlaySaveButton").performClick()
    composeTestRule.onNodeWithTag("InterestOverlayTitle").assertIsNotDisplayed()
  }

  @Test
  fun testContinueButtonCorrectlyNavigatesToHome() {
    composeTestRule.onNodeWithTag("AccountDetailsContinueButton").performScrollTo().performClick()
    verify(navigationAction).navigateTo(screen = Screen.HOME)
  }
}
