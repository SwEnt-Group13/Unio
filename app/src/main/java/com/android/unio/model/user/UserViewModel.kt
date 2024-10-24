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
          repository.init { getUserByUid(auth.currentUser!!.uid, true) }
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
