package com.android.unio.model.user

import android.util.Log
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.android.unio.model.authentication.registerAuthStateListener
import com.android.unio.model.image.ImageRepository
import com.android.unio.model.strings.StoragePathsStrings
import com.google.firebase.Firebase
import com.google.firebase.auth.AuthCredential
import com.google.firebase.auth.auth
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.Job
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

@HiltViewModel
class UserViewModel
@Inject
constructor(
    private val userRepository: UserRepository,
    private val imageRepository: ImageRepository
) : ViewModel() {
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
      userRepository: UserRepository,
      imageRepository: ImageRepository,
      initializeWithAuthenticatedUser: Boolean
  ) : this(userRepository, imageRepository) {
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

  /**
   * Deletes the user with the given userId from firebase auth, storage (for the profile picture)
   * and firestore
   *
   * @param userId The Id of the corresponding user we want to delete
   * @return true if all three method were successful and false otherwise
   */
  suspend fun deleteUser(
      userId: String,
      deleteWithProfilePicture: Boolean,
      onSuccess: () -> Unit,
      onFailure: (Exception) -> Unit
  ) {
    try {
      coroutineScope {

        // Only delete the profile picture if the user has one!
        if (deleteWithProfilePicture) {
          val imageTask = async {
            imageRepository.deleteImage(
                StoragePathsStrings.USER_IMAGES + userId,
                onSuccess = {
                  Log.i("UserDeletion", "Successfully deleted user's profile picture")
                },
                onFailure = { onFailure(it) })
          }
          imageTask.await()
        }

        val authTask = async {
          userRepository.deleteUserInAuth(
              userId,
              onSuccess = { Log.i("UserDeletion", "User deleted successfully") },
              onFailure = {
                Log.e("UserDeletion", "Failed to delete user", it)
                onFailure(it)
              })
        }

        val firestoreTask = async {
          userRepository.deleteUserInFirestore(
              userId,
              onSuccess = { Log.i("UserDeletion", "Successfully deleted user from firestore") },
              onFailure = { onFailure(it) })
        }

        authTask.await()
        firestoreTask.await()
      }
    } catch (e: Exception) {
      if (e.message == null) {
        onSuccess()
      }

      Log.e("UserDeletion", "Failed to delete user: ${e.message}", e)
      onFailure(e)
    }
  }
}
