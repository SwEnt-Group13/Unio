package com.android.unio.model.user

import android.util.Log
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.android.unio.model.association.Association
import com.android.unio.model.authentication.registerAuthStateListener
import com.android.unio.model.event.Event
import com.android.unio.model.image.ImageRepository
import com.android.unio.model.strings.StoragePathsStrings
import com.android.unio.model.usecase.UserDeletionUseCase
import com.android.unio.ui.event.SECONDS_IN_AN_HOUR
import com.google.firebase.Firebase
import com.google.firebase.Timestamp
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
    private val imageRepository: ImageRepository,
    private val userDeletionUseCase: UserDeletionUseCase
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
      userDeletionUseCase: UserDeletionUseCase,
      initializeWithAuthenticatedUser: Boolean
  ) : this(userRepository, imageRepository, userDeletionUseCase) {
    this.initializeWithAuthenticatedUser = initializeWithAuthenticatedUser
  }

  init {
    if (initializeWithAuthenticatedUser) {
      Firebase.auth.registerAuthStateListener { auth ->
        if (auth.currentUser != null) {
          userRepository.init { getUserByUid(auth.currentUser!!.uid, true) }
          userDeletionUseCase.init {}
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
   * Deletes the user and from the database and update all its dependencies. If
   * [deleteWithProfilePicture] is set to true, the user's profile picture will also be deleted.
   *
   * @param user The [User] object to delete.
   * @param deleteWithProfilePicture Whether to delete the user's profile picture or not.
   * @param onSuccess Callback if deletion is successful.
   * @param onFailure Callback if deletion fails.
   */
  suspend fun deleteUser(
      user: User,
      deleteWithProfilePicture: Boolean,
      onSuccess: () -> Unit,
      onFailure: (Exception) -> Unit
  ) {
    val userId = user.uid
    val eventToUpdate: MutableList<Event> = mutableListOf()
    val associationToUpdate: MutableList<Association> = mutableListOf()
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
          user.savedEvents.list.value.forEach { event ->
            updateOrAdd(eventToUpdate, event) { it.copy(numberOfSaved = it.numberOfSaved - 1) }
          }
          user.followedAssociations.list.value.forEach { association ->
            updateOrAdd(associationToUpdate, association) {
              it.copy(followersCount = it.followersCount - 1)
            }
          }

          user.joinedAssociations.list.value.forEach { association ->
            updateOrAdd(associationToUpdate, association) { current ->
              association.copy(members = current.members.filter { it.uid != userId })
            }
          }

          userDeletionUseCase.deleteUser(
              userId,
              eventToUpdate,
              associationToUpdate,
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

  /**
   * Check if the item is already in the list. If it is, update it with the provided operation,
   * otherwise add it to the list with the provided operation applied.
   *
   * @param list The list to add/update the item in.
   * @param item The item to add/update.
   * @param operation The operation to apply to the item.
   */
  private fun <T> updateOrAdd(list: MutableList<T>, item: T, operation: (T) -> T) {
    if (list.contains(item)) {
      val index = list.indexOf(item)
      val current = list[index]
      val newItem = operation(current)
      list[index] = newItem
    } else {
      val newItem = operation(item)
      list.add(newItem)
    }
  }

  companion object {
    private const val DEBOUNCE_INTERVAL: Long = 500
  }
}
