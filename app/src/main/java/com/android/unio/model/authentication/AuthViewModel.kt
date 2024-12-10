package com.android.unio.model.authentication

import android.util.Log
import androidx.lifecycle.ViewModel
import com.android.unio.model.user.UserRepository
import com.android.unio.ui.navigation.Route
import com.android.unio.ui.navigation.Screen
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
   */
  private fun addAuthStateVerifier() {
    firebaseAuth.registerAuthStateListener { auth ->
      Log.d("NavigationAuthScreen", "${auth.currentUser?.uid}")

      val user = auth.currentUser
      if (user != null) {
        if (user.isEmailVerified) {
          Log.d("NavigationAuthScreen", "${auth.currentUser?.uid} : mail is verified")
          userRepository.getUserWithId(
              user.uid,
              onSuccess = {
                _authState.value = Screen.HOME
              },
              onFailure =  { error ->
                Log.d("NavigationAuthScreen", "We are in a good start : $error")

                if(error is FirebaseFirestoreException && error.code == FirebaseFirestoreException.Code.NOT_FOUND){
                  _authState.value = Screen.ACCOUNT_DETAILS
                }else{
                  Log.e("UnioApp", "Error fetching account details: $error")
                  _authState.value = Screen.WELCOME
                }
              })
        } else {
          _authState.value = Screen.EMAIL_VERIFICATION
        }
      } else {
        _authState.value = Route.AUTH
      }
    }
  }
}
