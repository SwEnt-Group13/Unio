package com.android.unio.model.authentication

import androidx.lifecycle.ViewModel
import com.android.unio.model.user.UserRepository
import com.android.unio.ui.navigation.Route
import com.android.unio.ui.navigation.Screen
import com.google.firebase.auth.AuthCredential
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestoreException
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * ViewModel class that manages the authentication state of the user. It uses a [FirebaseAuth] to
 * verify the authentication state of the user and provides the [authState] to be observed by the
 * UI.
 *
 * @property firebaseAuth The [FirebaseAuth] instance that is used to verify the authentication
 *   state of the user.
 * @property userRepository The [UserRepository] that provides the user data.
 */
@HiltViewModel
class AuthViewModel
@Inject
constructor(private val firebaseAuth: FirebaseAuth, private val userRepository: UserRepository) :
    ViewModel() {

  private val _authState = MutableStateFlow<String?>(null)
  val authState: StateFlow<String?>
    get() = _authState.asStateFlow()

  var credential: AuthCredential? = null

  init {
    addAuthStateVerifier()
  }

  /**
   * Send a password reset email to the email given in parameter. If the email is sent correctly the
   * [onSuccess] function is called, otherwise the [onFailure] function is called with the exception
   * that occurred.
   *
   * @param email [String] : The email to send the password reset email to.
   * @param onSuccess [() -> Unit] : The function to call if the email is sent correctly.
   * @param onFailure [(Exception) -> Unit] : The function to call if an exception occurs.
   */
  fun sendEmailResetPassword(email: String, onSuccess: () -> Unit, onFailure: (Exception) -> Unit) {
    firebaseAuth.sendPasswordResetEmail(email).addOnCompleteListener { task ->
      if (task.isSuccessful) {
        onSuccess()
      } else {
        onFailure(task.exception!!)
      }
    }
  }

  /**
   * Verifies the authentication state of the user. If the user should be redirected, the
   * [authState] is updated.
   *
   * In the edge case where the user signs out of the application when he hasn't yet verified his
   * email but has created his account in firebase auth, and then restarts the app, the user will be
   * redirected to the welcome screen as the credentials are necessary to the login process of the
   * user (the credentials are equal to the ones inputted by the user in the Welcome screen)
   *
   * In the edge case that the user leaves the app after verifying his email, but has not yet
   * entered his account details, the user will be redirected straight to the Account Details
   * screen. This is done by handling the error given by FirebaseFirestore, if the document is not
   * found, but the user exists in auth with a verified email, this means that he hasn't yet
   * inputted his account details.
   */
  private fun addAuthStateVerifier() {
    firebaseAuth.registerAuthStateListener { auth ->
      val user = auth.currentUser
      if (user != null) {
        if (user.isEmailVerified) {
          userRepository.getUserWithId(
              user.uid,
              onSuccess = { _authState.value = Screen.HOME },
              onFailure = { error ->
                if (error is FirebaseFirestoreException &&
                    error.code == FirebaseFirestoreException.Code.NOT_FOUND) {
                  _authState.value = Screen.ACCOUNT_DETAILS
                } else {
                  _authState.value = Screen.WELCOME
                }
              })
        } else {
          if (credential == null) {
            _authState.value = Route.AUTH
          } else {
            _authState.value = Screen.EMAIL_VERIFICATION
          }
        }
      } else {
        _authState.value = Route.AUTH
      }
    }
  }
}
