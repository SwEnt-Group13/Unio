package com.android.unio.model.user

import android.util.Log
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

@HiltViewModel
class UserViewModel @Inject constructor(val repository: UserRepository) : ViewModel() {
  private val _user = MutableStateFlow<User?>(null)
  val user: StateFlow<User?> = _user

  private val _refreshState = mutableStateOf(false)
  val refreshState: State<Boolean> = _refreshState

  init {
    Firebase.auth.addAuthStateListener { auth ->
      if (auth.currentUser != null) {
        repository.init { getUserByUid(auth.currentUser!!.uid, true) }
      }
    }
  }

  fun getUsersByUid(uid: String, onSuccess: (User) -> Unit, onFailure: (Exception) -> Unit) {
    repository.getUserWithId(uid, onSuccess, onFailure)
  }

  fun getUserByUid(uid: String, fetchReferences: Boolean = false) {
    if (uid.isEmpty()) {
      return
    }

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
    repository.updateUser(
        user,
        onSuccess = { getUserByUid(user.uid) },
        onFailure = { Log.e("UserViewModel", "Failed to update user", it) })
  }

  fun addUser(user: User, onSuccess: () -> Unit) {
    repository.updateUser(
        user,
        onSuccess = onSuccess,
        onFailure = { Log.e("UserViewModel", "Failed to add user", it) })
    _user.value = user
  }
}
