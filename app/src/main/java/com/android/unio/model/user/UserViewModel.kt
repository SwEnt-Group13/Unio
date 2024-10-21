package com.android.unio.model.user

import android.util.Log
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
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
          repository.init { getUserByUid(auth.currentUser!!.uid) }
        }
      }
    } else {
      repository.init {}
    }
  }

  fun getUserByUid(uid: String) {
    _refreshState.value = true
    _user.value = null
    repository.getUserWithId(
        uid,
        onSuccess = { fetchedUser ->
          _refreshState.value = false
          _user.value = fetchedUser
        },
        onFailure = { exception ->
          _refreshState.value = false
          Log.e("UserViewModel", "Failed to fetch user", exception)
        })
  }

  fun refreshUser() {
    _user.value?.let { getUserByUid(it.uid) }
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
