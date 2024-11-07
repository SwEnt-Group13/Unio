package com.android.unio.model.user

import android.util.Log
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import com.google.firebase.firestore.firestore
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class UserViewModel(val repository: UserRepository, initializeWithAuthenticatedUser: Boolean) :
    ViewModel() {
  private val _user = MutableStateFlow<User?>(null)
  val user: StateFlow<User?> = _user

  private val _refreshState = mutableStateOf(false)
  val refreshState: State<Boolean> = _refreshState

  private val debounceInterval: Long = 500

  private var updateJob: Job? = null

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

  fun updateUserDebounced(user: User) {
    updateJob?.cancel()
    updateJob =
        viewModelScope.launch {
          delay(debounceInterval)
          updateUser(user)
        }
  }

  fun addUser(user: User, onSuccess: () -> Unit) {
    repository.updateUser(
        user,
        onSuccess = onSuccess,
        onFailure = { Log.e("UserViewModel", "Failed to add user", it) })
    _user.value = user
  }

  private fun getCurrentUserOrError(): User? {
    val currentUser = _user.value
    if (currentUser == null) {
      Log.w("UserViewModel", "No user available in _user")
      return null
    } else {
      return currentUser
    }
  }

  fun saveEventForCurrentUser(eventUid: String, onSuccess: () -> Unit) {
    val currentUser = getCurrentUserOrError() ?: return

    currentUser.savedEvents.add(eventUid)
    onSuccess()
  }

  fun unSaveEventForCurrentUser(eventUid: String, onSuccess: () -> Unit) {
    val currentUser = getCurrentUserOrError() ?: return

    if (isEventSavedForCurrentUser(eventUid)) {
      currentUser.savedEvents.remove(eventUid)
      onSuccess()
    } else {
      Log.w("UserViewModel", "Event not found in savedEvents")
    }
  }

  fun isEventSavedForCurrentUser(eventUid: String): Boolean {
    val currentUser = getCurrentUserOrError() ?: return false
    return currentUser.savedEvents.contains(eventUid)
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
