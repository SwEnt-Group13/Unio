package com.android.unio.model.user

import android.content.Context
import android.util.Log
import android.widget.Toast
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.android.unio.ui.navigation.NavigationAction
import com.android.unio.ui.navigation.Screen
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import com.google.firebase.firestore.firestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class UserViewModel(val repository: UserRepository, initializeWithAuthenticatedUser: Boolean) :
    ViewModel() {
  private val _user = MutableStateFlow<User?>(null)
  val user: StateFlow<User?> = _user

  private val _refreshState = mutableStateOf(false)
  val refreshState: State<Boolean> = _refreshState

  init {
    if (initializeWithAuthenticatedUser) {
      Firebase.auth.addAuthStateListener { auth ->
        if (auth.currentUser != null) {
          repository.init { getUserByUid(auth.currentUser!!.uid, false) }
        }
      }
    } else {
      repository.init {}
    }
  }

  fun getUserByUid(uid: String, fetchReferences: Boolean = false) {
    _refreshState.value = true
    _user.value = null
    repository.getUserWithId(
        uid,
        onSuccess = { fetchedUser ->
          _user.value = fetchedUser

          if (fetchReferences) {
            _user.value?.let {
              var first = true
              val handleSuccess = {
                if (first) {
                  first = false
                } else {
                  _refreshState.value = false
                }
              }
              it.joinedAssociations.requestAll(handleSuccess)
              it.followedAssociations.requestAll(handleSuccess)
            }
          } else {
            _refreshState.value = false
          }
        },
        onFailure = { exception ->
          _refreshState.value = false
          Log.e("UserViewModel", "Failed to fetch user", exception)
        })
  }

  fun refreshUser() {
    _user.value?.let { getUserByUid(it.uid, true) }
  }

  fun updateUser(user: User) {
    repository.updateUser(user, onSuccess = { getUserByUid(user.uid) }, onFailure = {
      Log.e("UserViewModel", "Failed to update user", it)
    })
  }

  fun addUser(user: User, navigationAction: NavigationAction, context: Context) {
    repository.updateUser(user,
      onSuccess = {
        Toast.makeText(context, "Account Created Successfully", Toast.LENGTH_SHORT).show()
        navigationAction.navigateTo(Screen.HOME)
      },
      onFailure = {
      Log.e("UserViewModel", "Failed to add user", it)
    })
  }

  companion object {
    val Factory: ViewModelProvider.Factory =
        object : ViewModelProvider.Factory {
          @Suppress("UNCHECKED_CAST")
          override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return UserViewModel(UserRepositoryFirestore(Firebase.firestore), false) as T
          }
        }
  }
}
