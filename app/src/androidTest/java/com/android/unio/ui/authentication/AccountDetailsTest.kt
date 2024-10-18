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
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.android.unio.model.user.UserRepositoryFirestore
import com.android.unio.ui.accountCreation.AccountDetails
import com.android.unio.ui.navigation.NavigationAction
import com.android.unio.ui.navigation.Screen
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.auth
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.FirebaseFirestore
import com.kaspersky.components.kautomator.common.Environment
import io.mockk.MockKAnnotations
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.mockk
import io.mockk.mockkStatic
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.MockitoAnnotations
import org.mockito.kotlin.verify

@RunWith(AndroidJUnit4::class)
class AccountDetailsTest {

  @Mock private lateinit var db: FirebaseFirestore
  @Mock private lateinit var collectionReference: CollectionReference

  @MockK private lateinit var navigationAction: NavigationAction
  @MockK private lateinit var navHostController: NavHostController
  @MockK private lateinit var userRepositoryFirestore: UserRepositoryFirestore

  private lateinit var mockFirebaseUser: FirebaseUser
  private lateinit var mockFirebaseAuth: FirebaseAuth

  @get:Rule val composeTestRule = createComposeRule()

  @Before
  fun setUp() {
    MockitoAnnotations.initMocks(this)

    // Initialize MockK annotations if you have any @MockK annotations
    MockKAnnotations.init(this, relaxed = true)

    // Mock FirebaseAuth and FirebaseUser
    mockFirebaseAuth = mockk(relaxed = true)
    mockFirebaseUser = mockk(relaxed = true)

    // Mock the static Firebase.auth to return our mockFirebaseAuth
    mockkStatic(Firebase::class)
    mockkStatic(com.google.firebase.ktx.Firebase::class)
    every { Firebase.auth } returns mockFirebaseAuth

    // Set up the behavior for mockFirebaseAuth and mockFirebaseUser
    every { mockFirebaseAuth.currentUser } returns mockFirebaseUser
    every { mockFirebaseUser.uid } returns "mocked-uid"

    navHostController = mockk(relaxed = true)
    navigationAction = NavigationAction(navHostController)
    userRepositoryFirestore = mockk(relaxed = true)

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

  @Test
  fun testContinueButtonCorrectlyNavigatesToHome() {
    composeTestRule.onNodeWithTag("AccountDetailsContinueButton").performClick()
    verify(navHostController).navigate(Screen.HOME)
  }
}
