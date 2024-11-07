package com.android.unio.model.authentication

import android.util.Log
import androidx.lifecycle.ViewModel
import com.android.unio.model.user.UserRepository
import com.android.unio.ui.navigation.Route
import com.android.unio.ui.navigation.Screen
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

@HiltViewModel
class AuthViewModel
@Inject
constructor(private val firebaseAuth: FirebaseAuth, private val userRepository: UserRepository) :
    ViewModel() {

  private val _authState = MutableStateFlow<String?>(null)
  val authState: StateFlow<String?>
    get() = _authState

  init {
    addAuthStateVerifier()
  }

  /**
   * Verifies the authentication state of the user. If the user should be redirected, the
   * [authState] is updated.
   */
  private fun addAuthStateVerifier() {
    firebaseAuth.addAuthStateListener { auth ->
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
