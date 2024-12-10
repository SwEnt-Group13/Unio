package com.android.unio.model.user

import android.util.Log
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.android.unio.model.authentication.registerAuthStateListener
import com.android.unio.model.event.Event
import com.android.unio.model.image.ImageRepository
import com.android.unio.model.strings.StoragePathsStrings
import com.android.unio.ui.event.SECONDS_IN_AN_HOUR
import com.google.firebase.Firebase
import com.google.firebase.Timestamp
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

  private var updateJob: Job? = null
  private var initializeWithAuthenticatedUser: Boolean = true

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

  /**
   * Updates the user with the data from the corresponding User [uid] and fetches the user's
   * associations (joined and followed) if [fetchReferences] is set to true.
   */
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

  /**
   * Calls the [getUserByUid] method with the [User.uid] of the current user with the
   * fetchReferences parameter set to true.
   */
  fun refreshUser() {
    _user.value?.let { getUserByUid(it.uid, true) }
  }

  /**
   * Calls the [getUserByUid] method with the [User.uid] of the selected user with the
   * fetchReferences parameter set to true.
   */
  fun refreshSomeoneElseUser() {
    _selectedSomeoneElseUser.value?.let { getUserByUid(it.uid, true) }
  }

  /**
   * Updates the user with the provided [User] object.
   *
   * @param user The [User] object to update the user with.
   */
  fun updateUser(user: User) {
    userRepository.updateUser(
        user,
        onSuccess = { getUserByUid(user.uid) },
        onFailure = { Log.e("UserViewModel", "Failed to update user", it) })
  }

  /**
   * Updates the user with the provided [User] object after a debounce interval.
   *
   * @param user The [User] object to update the user with.
   * @param interval The debounce interval in milliseconds. Will take the default value of
   *   [debounceInterval] if not provided.
   */
  fun updateUserDebounced(user: User, interval: Long = DEBOUNCE_INTERVAL) {
    updateJob?.cancel()
    updateJob =
        viewModelScope.launch {
          delay(interval)
          updateUser(user)
        }
  }

  /**
   * Saves the provided [event] to the user's saved events list and sets up a notification for the
   * event if it starts more than 3 hours from now.
   *
   * @param event The [Event] object to save.
   * @param setupNotification The function to call to set up a notification for the event.
   */
  fun saveEvent(event: Event, setupNotification: () -> Unit) {
    val newUser = _user.value!!.copy()
    if (event.startDate.seconds - Timestamp.now().seconds > 3 * SECONDS_IN_AN_HOUR) {
      setupNotification()
    }
    newUser.savedEvents.add(event.uid)
    updateUserDebounced(newUser)
  }

  /**
   * Removes the provided [event] from the user's saved events list and removes the notification for
   * the event.
   *
   * @param event The [Event] object to unsave.
   * @param removeNotification The function to call to remove the notification for the event.
   */
  fun unsaveEvent(event: Event, removeNotification: () -> Unit) {
    val newUser = _user.value!!.copy()
    removeNotification()
    newUser.savedEvents.remove(event.uid)
    updateUserDebounced(newUser)
  }

  /**
   * Adds the provided [User] to the database
   *
   * @param user The [User] object to add.
   * @param onSuccess Callback if addition is successful.
   */
  fun addUser(user: User, onSuccess: () -> Unit) {
    userRepository.updateUser(
        user,
        onSuccess = onSuccess,
        onFailure = { Log.e("UserViewModel", "Failed to add user", it) })
    _user.value = user
  }

  /**
   * Selects a user to view their profile.
   *
   * @param user The [User] object to view.
   */
  fun setSomeoneElseUser(user: User) {
    _selectedSomeoneElseUser.value = user
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

  companion object {
    private const val DEBOUNCE_INTERVAL: Long = 500
  }
}
