package com.android.unio.ui.authentication

import android.os.Looper
import androidx.compose.ui.test.assertHasClickAction
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performTextInput
import androidx.test.core.app.ApplicationProvider
import com.android.unio.model.association.Association
import com.android.unio.model.firestore.emptyFirestoreReferenceList
import com.android.unio.model.user.Interest
import com.android.unio.model.user.Social
import com.android.unio.model.user.User
import com.android.unio.model.user.UserRepositoryFirestore
import com.android.unio.model.user.UserSocial
import com.android.unio.ui.navigation.NavigationAction
import com.android.unio.ui.navigation.Screen
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.internal.zzac
import io.mockk.MockKAnnotations
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.mockkStatic
import io.mockk.slot
import io.mockk.verify
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class WelcomeTest {

  @MockK private lateinit var navigationAction: NavigationAction
  @MockK private lateinit var userRepositoryFirestore: UserRepositoryFirestore
  @MockK private lateinit var firebaseAuth: FirebaseAuth

  // Because it is impossible to mock the FirebaseUser's abstract method, this is the only way to
  // mock it.
  @MockK private lateinit var firebaseUser: zzac

  private lateinit var userNonEmptyFirstName: User
  private lateinit var userEmptyFirstName: User

  @get:Rule val composeTestRule = createComposeRule()

  @Before
  fun setUp() {
    MockKAnnotations.init(this, relaxed = true)
    if (Looper.myLooper() == null) {
      Looper.prepare() // Prepare Looper for tests
    }

    // Initialize Firebase if necessary
    if (FirebaseApp.getApps(ApplicationProvider.getApplicationContext()).isEmpty()) {
      FirebaseApp.initializeApp(ApplicationProvider.getApplicationContext())
    }

    mockkStatic(FirebaseAuth::class)
    every { FirebaseAuth.getInstance() } returns firebaseAuth
    every { firebaseAuth.currentUser } returns firebaseUser

    userNonEmptyFirstName =
        User(
            "1",
            "john@example.com",
            "John",
            "Doe",
            "An example user",
            Association.emptyFirestoreReferenceList(),
            Association.emptyFirestoreReferenceList(),
            listOf(Interest.SPORTS, Interest.MUSIC),
            listOf(
                UserSocial(Social.INSTAGRAM, "Insta"), UserSocial(Social.WEBSITE, "example.com")),
            "https://www.example.com/image")

    userEmptyFirstName =
        User(
            "1",
            "john@example.com",
            "",
            "Doe",
            "An example user",
            Association.emptyFirestoreReferenceList(),
            Association.emptyFirestoreReferenceList(),
            listOf(Interest.SPORTS, Interest.MUSIC),
            listOf(
                UserSocial(Social.INSTAGRAM, "Insta"), UserSocial(Social.WEBSITE, "example.com")),
            "https://www.example.com/image")
  }

  @Test
  fun testWelcomeIsDisplayed() {
    composeTestRule.setContent { WelcomeScreen(navigationAction, userRepositoryFirestore) }
    composeTestRule.onNodeWithTag("WelcomeEmail").assertIsDisplayed()
    composeTestRule.onNodeWithTag("WelcomePassword").assertIsDisplayed()
    composeTestRule.onNodeWithTag("WelcomePassword").assertIsDisplayed()
    composeTestRule.onNodeWithTag("WelcomeButton").assertIsDisplayed()
    composeTestRule.onNodeWithTag("WelcomeButton").assertHasClickAction()
    composeTestRule.onNodeWithTag("WelcomeButton").assertIsNotEnabled()
  }

  @Test
  fun testButtonEnables() {
    composeTestRule.setContent { WelcomeScreen(navigationAction, userRepositoryFirestore) }
    composeTestRule.onNodeWithTag("WelcomeButton").assertIsNotEnabled()

    composeTestRule.onNodeWithTag("WelcomeEmail").performTextInput("john.doe@epfl.ch")
    composeTestRule.onNodeWithTag("WelcomePassword").performTextInput("123456")

    composeTestRule.onNodeWithTag("WelcomeButton").assertIsEnabled()
  }

  @Test
  fun testUserIsNull() {
    every { firebaseAuth.currentUser } returns null

    composeTestRule.setContent { WelcomeScreen(navigationAction, userRepositoryFirestore) }

    // Use MockK's slot to capture the AuthStateListener
    val authStateListenerSlot = slot<FirebaseAuth.AuthStateListener>()
    verify { firebaseAuth.addAuthStateListener(capture(authStateListenerSlot)) }
    authStateListenerSlot.captured.onAuthStateChanged(firebaseAuth)

    verify { navigationAction.navigateTo(Screen.WELCOME) }
  }

  @Test
  fun testUserIsAuthenticatedAndEmailVerifiedWithProfile() {
    every { firebaseUser.isEmailVerified } returns true
    every { userRepositoryFirestore.getUserWithId(any(), any(), any()) } answers
        {
          val onSuccess = it.invocation.args[1] as (User) -> Unit
          onSuccess(userNonEmptyFirstName)
        }

    composeTestRule.setContent { WelcomeScreen(navigationAction, userRepositoryFirestore) }

    // Use MockK's slot to capture the AuthStateListener
    val authStateListenerSlot = slot<FirebaseAuth.AuthStateListener>()
    verify { firebaseAuth.addAuthStateListener(capture(authStateListenerSlot)) }
    authStateListenerSlot.captured.onAuthStateChanged(firebaseAuth)

    verify { navigationAction.navigateTo(Screen.HOME) }
  }

  @Test
  fun testUserIsAuthenticatedAndEmailVerifiedWithEmptyName() {
    every { firebaseUser.isEmailVerified } returns true
    every { userRepositoryFirestore.getUserWithId(any(), any(), any()) } answers
        {
          val onSuccess = it.invocation.args[1] as (User) -> Unit
          onSuccess(userEmptyFirstName)
        }

    composeTestRule.setContent { WelcomeScreen(navigationAction, userRepositoryFirestore) }

    // Use MockK's slot to capture the AuthStateListener
    val authStateListenerSlot = slot<FirebaseAuth.AuthStateListener>()
    verify { firebaseAuth.addAuthStateListener(capture(authStateListenerSlot)) }
    authStateListenerSlot.captured.onAuthStateChanged(firebaseAuth)

    verify { navigationAction.navigateTo(Screen.ACCOUNT_DETAILS) }
  }

  @Test
  fun testUserIsAuthenticatedAndEmailNotVerified() {
    every { firebaseUser.isEmailVerified } returns false

    composeTestRule.setContent { WelcomeScreen(navigationAction, userRepositoryFirestore) }

    // Use MockK's slot to capture the AuthStateListener
    val authStateListenerSlot = slot<FirebaseAuth.AuthStateListener>()
    verify { firebaseAuth.addAuthStateListener(capture(authStateListenerSlot)) }
    authStateListenerSlot.captured.onAuthStateChanged(firebaseAuth)

    verify { navigationAction.navigateTo(Screen.EMAIL_VERIFICATION) }
  }

  @Test
  fun testUserIsNotAuthenticated() {
    every { firebaseAuth.currentUser } returns null

    composeTestRule.setContent { WelcomeScreen(navigationAction, userRepositoryFirestore) }

    // Use MockK's slot to capture the AuthStateListener
    val authStateListenerSlot = slot<FirebaseAuth.AuthStateListener>()
    verify { firebaseAuth.addAuthStateListener(capture(authStateListenerSlot)) }
    authStateListenerSlot.captured.onAuthStateChanged(firebaseAuth)

    verify(exactly = 0) { navigationAction.navigateTo(any<String>()) }
  }
}
