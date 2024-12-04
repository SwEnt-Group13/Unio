package com.android.unio.model.user

import android.util.Log
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.android.unio.model.authentication.registerAuthStateListener
import com.google.firebase.Firebase
import com.google.firebase.auth.AuthCredential
import com.google.firebase.auth.auth
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

@HiltViewModel
class UserViewModel @Inject constructor(private val userRepository: UserRepository) : ViewModel() {
  private val _user = MutableStateFlow<User?>(null)
  val user: StateFlow<User?> = _user.asStateFlow()

  private val _selectedSomeoneElseUser = MutableStateFlow<User?>(null)
  val selectedSomeoneElseUser: StateFlow<User?> = _selectedSomeoneElseUser.asStateFlow()

  private val _refreshState = mutableStateOf(false)
  val refreshState: State<Boolean> = _refreshState

  private val debounceInterval: Long = 500

  private var updateJob: Job? = null
  private var initializeWithAuthenticatedUser: Boolean = true

  private var _credential: AuthCredential? = null
  val credential: AuthCredential?
    get() = _credential

  constructor(
      repository: UserRepository,
      initializeWithAuthenticatedUser: Boolean
  ) : this(repository) {
    this.initializeWithAuthenticatedUser = initializeWithAuthenticatedUser
  }

  init {
    if (initializeWithAuthenticatedUser) {
      Firebase.auth.registerAuthStateListener { auth ->
        if (auth.currentUser != null) {
          userRepository.init { getUserByUid(auth.currentUser!!.uid, true) }
        }
      }
    } else {
      userRepository.init {}
    }
  }

  fun getUserByUid(uid: String, fetchReferences: Boolean = false) {
    if (uid.isEmpty()) {
      return
    }
    _refreshState.value = true
    userRepository.getUserWithId(
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

  fun refreshSomeoneElseUser() {
    _selectedSomeoneElseUser.value?.let { getUserByUid(it.uid, true) }
  }

  fun updateUser(user: User) {
    userRepository.updateUser(
        user,
        onSuccess = { getUserByUid(user.uid) },
        onFailure = { Log.e("UserViewModel", "Failed to update user", it) })
  }

  fun deleteUserDocument(userUid: String, onSuccess: () -> Unit, onFailure: (Exception) -> Unit) {

    if (userUid != user.value!!.uid) {
      Log.e("UserDeletionFirestore", "UserUid does not match current user uid")
      return
    }

    userRepository.deleteUserInFirestore(
        userUid, onSuccess = { onSuccess() }, onFailure = { onFailure(it) })
  }

  fun updateUserDebounced(user: User, interval: Long = debounceInterval) {
    updateJob?.cancel()
    updateJob =
        viewModelScope.launch {
          delay(interval)
          updateUser(user)
        }
  }

  fun addUser(user: User, onSuccess: () -> Unit) {
    userRepository.updateUser(
        user,
        onSuccess = onSuccess,
        onFailure = { Log.e("UserViewModel", "Failed to add user", it) })
    _user.value = user
  }

  fun setSomeoneElseUser(user: User) {
    _selectedSomeoneElseUser.value = user
  }

  fun setCredential(credential: AuthCredential?) {
    _credential = credential
  }
}
