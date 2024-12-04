package com.android.unio.model.authentication

import android.util.Log
import androidx.lifecycle.ViewModel
import com.android.unio.model.user.UserRepository
import com.android.unio.ui.navigation.Route
import com.android.unio.ui.navigation.Screen
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

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

  fun deleteAccount(userId: String, onSuccess: () -> Unit, onFailure: (Exception) -> Unit) {

    userRepository.deleteUserInAuth(
        userId,
        onSuccess = {
          Log.i("AuthViewModel", "User deleted successfully")
          onSuccess()
        },
        onFailure = {
          Log.e("AuthViewModel", "Failed to delete user", it)
          onFailure(it)
        })
  }

  /**
   * Verifies the authentication state of the user. If the user should be redirected, the
   * [authState] is updated.
   */
  private fun addAuthStateVerifier() {
    firebaseAuth.registerAuthStateListener { auth ->
      val user = auth.currentUser
      if (user != null) {
        if (user.isEmailVerified) {
          userRepository.getUserWithId(
              user.uid,
              {
                _authState.value =
                    if (it.firstName.isNotEmpty()) {
                      Screen.HOME
                    } else {
                      Screen.ACCOUNT_DETAILS
                    }
              },
              {
                Log.e("UnioApp", "Error fetching account details: $it")
                _authState.value = Screen.WELCOME
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
