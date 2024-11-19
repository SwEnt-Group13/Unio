package com.android.unio.model.user

import android.util.Log
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
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

  private val _followedAssociations = MutableStateFlow(emptyList<String>())
  val followedAssociations: StateFlow<List<String>> = _followedAssociations.asStateFlow()

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
      Firebase.auth.addAuthStateListener { auth ->
        if (auth.currentUser != null) {
          userRepository.init { getUserByUid(auth.currentUser!!.uid, true) }
        }
      }
    } else {
      userRepository.init {}
    }
  }

  fun getUsersByUid(uid: String, onSuccess: (User) -> Unit, onFailure: (Exception) -> Unit) {
    userRepository.getUserWithId(uid, onSuccess, onFailure)
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
          setFollowedAssociations(getFollowedAssociationsEventUID())
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
    userRepository.updateUser(
        user,
        onSuccess = { getUserByUid(user.uid) },
        onFailure = { Log.e("UserViewModel", "Failed to update user", it) })
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

  fun getFollowedAssociationsEventUID(): List<String> {
    val followedAsso = _user.value?.followedAssociations?.uids ?: emptyList()
    return followedAsso
  }

  private fun setFollowedAssociations(associations: List<String>) {
    _followedAssociations.value = associations
  }

  fun isEventSavedForCurrentUser(eventUid: String): Boolean {
    val currentUser = getCurrentUserOrError() ?: return false
    return currentUser.savedEvents.contains(eventUid)
  }

  fun setCredential(credential: AuthCredential?) {
    _credential = credential
  }
}
