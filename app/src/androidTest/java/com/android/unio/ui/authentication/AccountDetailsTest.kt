package com.android.unio.ui.authentication

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsNotDisplayed
import androidx.compose.ui.test.assertTextContains
import androidx.compose.ui.test.assertTextEquals
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import androidx.navigation.NavHostController
import com.android.unio.model.user.UserRepositoryFirestore
import com.android.unio.ui.accountCreation.AccountDetails
import com.android.unio.ui.navigation.NavigationAction
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.FirebaseFirestore
import io.mockk.mockk
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.Mock
import org.mockito.Mockito.`when`
import org.mockito.MockitoAnnotations
import org.mockito.kotlin.any
import org.mockito.kotlin.mock

class AccountDetailsTest {

  @Mock private lateinit var db: FirebaseFirestore
  @Mock private lateinit var collectionReference: CollectionReference

  private lateinit var navigationAction: NavigationAction
  private lateinit var navHostController: NavHostController
  private lateinit var userRepositoryFirestore: UserRepositoryFirestore

  @get:Rule val composeTestRule = createComposeRule()

  @Before
  fun setUp() {
    MockitoAnnotations.openMocks(this)

    navHostController = mock { NavHostController::class.java }
    navigationAction = NavigationAction(navHostController)
    userRepositoryFirestore = mockk()

    `when`(db.collection(any())).thenReturn(collectionReference)

    composeTestRule.setContent { AccountDetails(navigationAction, userRepositoryFirestore) }
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
    composeTestRule.onNodeWithTag("AccountDetailsBioTextField").assertIsDisplayed()
    composeTestRule
        .onNodeWithTag("AccountDetailsBioText", useUnmergedTree = true)
        .assertTextEquals("Bio")
    composeTestRule.onNodeWithTag("AccountDetailsProfilePictureText").assertIsDisplayed()
    composeTestRule
        .onNodeWithTag("AccountDetailsProfilePictureText")
        .assertTextEquals("Maybe add a profile picture?")
    composeTestRule.onNodeWithTag("AccountDetailsProfilePictureIcon").assertIsDisplayed()
    composeTestRule.onNodeWithTag("AccountDetailsInterestsButton").assertIsDisplayed()
    composeTestRule.onNodeWithTag("AccountDetailsSocialsButton").assertIsDisplayed()
    composeTestRule.onNodeWithTag("AccountDetailsContinueButton").assertIsDisplayed()
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
    composeTestRule.onNodeWithTag("AccountDetailsInterestsButton").performClick()
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

  //  @Test
  //  fun testContinueButtonCorrectlyNavigatesToHome(){
  //    composeTestRule.onNodeWithTag("AccountDetailsContinueButton").performClick()
  //    verify(navigationAction).navigateTo(screen = Screen.HOME)
  //  }
}
