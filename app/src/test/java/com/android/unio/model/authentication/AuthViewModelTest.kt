package com.android.unio.model.authentication

import androidx.test.core.app.ApplicationProvider
import com.android.unio.model.association.Association
import com.android.unio.model.event.Event
import com.android.unio.model.firestore.emptyFirestoreReferenceList
import com.android.unio.model.user.Interest
import com.android.unio.model.user.Social
import com.android.unio.model.user.User
import com.android.unio.model.user.UserRepository
import com.android.unio.model.user.UserSocial
import com.android.unio.ui.navigation.Route
import com.android.unio.ui.navigation.Screen
import com.google.firebase.Firebase
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth
import com.google.firebase.auth.internal.zzac
import io.mockk.MockKAnnotations
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.mockkStatic
import io.mockk.slot
import io.mockk.verify
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class AuthViewModelTest {

  @MockK private lateinit var firebaseAuth: FirebaseAuth
  @MockK private lateinit var userRepository: UserRepository

  // Because it is impossible to mock the FirebaseUser's abstract method, this is the only way to
  // mock it.
  @MockK private lateinit var firebaseUser: zzac

  private lateinit var authViewModel: AuthViewModel
  private lateinit var userNonEmptyFirstName: User
  private lateinit var userEmptyFirstName: User

  @Before
  fun setUp() {
    MockKAnnotations.init(this, relaxed = true)

    // Initialize Firebase if necessary
    if (FirebaseApp.getApps(ApplicationProvider.getApplicationContext()).isEmpty()) {
      FirebaseApp.initializeApp(ApplicationProvider.getApplicationContext())
    }

    mockkStatic(FirebaseAuth::class)
    every { Firebase.auth } returns firebaseAuth
    every { firebaseAuth.currentUser } returns firebaseUser

    userNonEmptyFirstName =
        User(
            uid = "1",
            email = "john@example.com",
            firstName = "John",
            lastName = "Doe",
            biography = "An example user",
            followedAssociations = Association.emptyFirestoreReferenceList(),
            joinedAssociations = Association.emptyFirestoreReferenceList(),
            savedEvents = Event.emptyFirestoreReferenceList(),
            interests = listOf(Interest.SPORTS, Interest.MUSIC),
            socials =
                listOf(
                    UserSocial(Social.INSTAGRAM, "Insta"),
                    UserSocial(Social.WEBSITE, "example.com")),
            profilePicture = "https://www.example.com/image")

    userEmptyFirstName =
        User(
            uid = "1",
            email = "john@example.com",
            firstName = "",
            lastName = "Doe",
            biography = "An example user",
            followedAssociations = Association.emptyFirestoreReferenceList(),
            joinedAssociations = Association.emptyFirestoreReferenceList(),
            savedEvents = Event.emptyFirestoreReferenceList(),
            interests = listOf(Interest.SPORTS, Interest.MUSIC),
            socials =
                listOf(
                    UserSocial(Social.INSTAGRAM, "Insta"),
                    UserSocial(Social.WEBSITE, "example.com")),
            profilePicture = "https://www.example.com/image")
  }

  // Use MockK's slot to capture the AuthStateListener and trigger it
  private fun triggerAuthStateListener(firebaseAuth: FirebaseAuth) {
    val authStateListenerSlot = slot<FirebaseAuth.AuthStateListener>()
    verify { firebaseAuth.addAuthStateListener(capture(authStateListenerSlot)) }
    authStateListenerSlot.captured.onAuthStateChanged(firebaseAuth)
  }

  @Test
  fun testUserIsNull() {
    every { firebaseAuth.currentUser } returns null

    authViewModel = AuthViewModel(firebaseAuth, userRepository)

    triggerAuthStateListener(firebaseAuth)

    assertEquals(Route.AUTH, authViewModel.authState.value)
  }

  @Test
  fun testUserIsAuthenticatedAndEmailVerifiedWithProfile() {
    every { firebaseUser.isEmailVerified } returns true
    every { userRepository.getUserWithId(any(), any(), any()) } answers
        {
          val onSuccess = it.invocation.args[1] as (User) -> Unit
          onSuccess(userNonEmptyFirstName)
        }

    authViewModel = AuthViewModel(firebaseAuth, userRepository)

    triggerAuthStateListener(firebaseAuth)

    assertEquals(Screen.HOME, authViewModel.authState.value)
  }

  @Test
  fun testUserIsAuthenticatedAndEmailVerifiedWithEmptyName() {
    every { firebaseUser.isEmailVerified } returns true
    every { userRepository.getUserWithId(any(), any(), any()) } answers
        {
          val onSuccess = it.invocation.args[1] as (User) -> Unit
          onSuccess(userEmptyFirstName)
        }

    authViewModel = AuthViewModel(firebaseAuth, userRepository)

    triggerAuthStateListener(firebaseAuth)

    assertEquals(Screen.ACCOUNT_DETAILS, authViewModel.authState.value)
  }

  @Test
  fun testUserIsAuthenticatedAndEmailNotVerified() {
    every { firebaseUser.isEmailVerified } returns false

    authViewModel = AuthViewModel(firebaseAuth, userRepository)

    triggerAuthStateListener(firebaseAuth)

    assertEquals(Screen.EMAIL_VERIFICATION, authViewModel.authState.value)
  }

  @Test
  fun testErrorFetchingAccountDetails() {
    every { firebaseUser.isEmailVerified } returns true
    every { userRepository.getUserWithId(any(), any(), any()) } answers
        {
          val onFailure = it.invocation.args[2] as (Exception) -> Unit
          onFailure(Exception("Test exception"))
        }

    authViewModel = AuthViewModel(firebaseAuth, userRepository)

    triggerAuthStateListener(firebaseAuth)

    assertEquals(Screen.WELCOME, authViewModel.authState.value)
  }
}
