package com.android.unio.model.association

import android.util.Log
import androidx.lifecycle.ViewModel
import com.android.unio.model.event.EventRepository
import com.android.unio.model.follow.ConcurrentAssociationUserRepository
import com.android.unio.model.image.ImageRepository
import com.android.unio.model.user.User
import com.google.firebase.Firebase
import com.google.firebase.messaging.messaging
import dagger.hilt.android.lifecycle.HiltViewModel
import java.io.InputStream
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * ViewModel class that manages the association list data and provides it to the UI. It exposes a
 * list of associations, a selected association, as wel as a list of associations grouped by
 * category through a [StateFlow] to be observed by the UI.
 *
 * @property associationRepository The [AssociationRepository] that provides the associations.
 * @property eventRepository The [EventRepository] that provides the events of the association.
 * @property imageRepository The [ImageRepository] that provides the images of the association.
 * @property concurrentAssociationUserRepository The [ConcurrentAssociationUserRepository] that
 *   provides the functionality of the follow button.
 */
@HiltViewModel
class AssociationViewModel
@Inject
constructor(
    private val associationRepository: AssociationRepository,
    private val eventRepository: EventRepository,
    private val imageRepository: ImageRepository,
    private val concurrentAssociationUserRepository: ConcurrentAssociationUserRepository
) : ViewModel() {

  private val _associations = MutableStateFlow<List<Association>>(emptyList())
  val associations: StateFlow<List<Association>> = _associations.asStateFlow()

  private val _associationsByCategory =
      MutableStateFlow<Map<AssociationCategory, List<Association>>>(emptyMap())
  val associationsByCategory: StateFlow<Map<AssociationCategory, List<Association>>> =
      _associationsByCategory.asStateFlow()

  private val _selectedAssociation = MutableStateFlow<Association?>(null)
  val selectedAssociation: StateFlow<Association?> = _selectedAssociation

  init {
    associationRepository.init { getAssociations() }
  }

  /**
   * Get the user from a member
   *
   * @param member The member to get the user from
   * @return The user of the member
   */
  fun getUserFromMember(member: Member): StateFlow<User?> {
    return member.user.element
  }

  /**
   * Fetches the user from a member
   *
   * @param member The member to fetch the user from
   */
  private fun fetchUserFromMember(member: Member) {
    member.user.fetch()
  }

  /**
   * Fetches all associations from the repository and updates the [_associations] and
   * [_associationsByCategory] state flows. If the fetch fails, the [_associations] state flow is
   * set to an empty list.
   */
  fun getAssociations() {
    associationRepository.getAssociations(
        onSuccess = { fetchedAssociations ->
          _associations.value = fetchedAssociations
          _associationsByCategory.value = fetchedAssociations.groupBy { it.category }
        },
        onFailure = { exception ->
          _associations.value = emptyList()
          Log.e("ExploreViewModel", "Failed to fetch associations", exception)
        })
  }

  /**
   * Updates the follow status of the user for the target association. If the user is following the
   * association, the association's follower count is decremented and the association is removed
   * from the user's followed associations. If the user is not following the association, the
   * association's follower count is incremented and the association is added to the user's followed
   * associations.
   *
   * @param target The association to update the follow status for.
   * @param user The user to update the follow status for.
   * @param isUnfollowAction A boolean indicating whether the user is unfollowing the association.
   * @param updateUser A callback to update the user in the repository.
   */
  fun updateFollow(
      target: Association,
      user: User,
      isUnfollowAction: Boolean,
      updateUser: () -> Unit
  ) {
    val updatedAssociation: Association
    val updatedUser: User = user.copy()
    if (isUnfollowAction) {
      val updatedFollowCount = if (target.followersCount - 1 >= 0) target.followersCount - 1 else 0
      updatedAssociation = target.copy(followersCount = updatedFollowCount)
      updatedUser.followedAssociations.remove(target.uid)
      Firebase.messaging.unsubscribeFromTopic(target.uid)
    } else {
      updatedAssociation = target.copy(followersCount = target.followersCount + 1)
      updatedUser.followedAssociations.add(target.uid)
      Firebase.messaging.subscribeToTopic(target.uid)
    }
    concurrentAssociationUserRepository.updateFollow(
        updatedUser,
        updatedAssociation,
        {
          _associations.value =
              _associations.value.map {
                if (it.uid == target.uid) {
                  updatedAssociation
                } else it
              }
          _selectedAssociation.value = updatedAssociation
          updateUser()
        },
        { exception -> Log.e("AssociationViewModel", "Failed to update follow", exception) })
  }

  /**
   * Saves an association to the repository. If an image stream is provided, the image is uploaded
   * to Firebase Storage and the image URL is saved to the association. If the image stream is null,
   * the association is saved without an image.
   *
   * @param association The association to save.
   * @param imageStream The image stream to upload to Firebase Storage.
   * @param onSuccess A callback that is called when the association is successfully saved.
   * @param onFailure A callback that is called when an error occurs while saving the association.
   */
  fun saveAssociation(
      association: Association,
      imageStream: InputStream?,
      onSuccess: () -> Unit,
      onFailure: (Exception) -> Unit
  ) {
    if (imageStream != null) {
      imageRepository.uploadImage(
          imageStream = imageStream,
          firebasePath = "images/associations/${association.uid}",
          onSuccess = { imageUrl ->
            val updatedAssociation = association.copy(image = imageUrl)
            associationRepository.saveAssociation(
                updatedAssociation,
                {
                  _associations.value =
                      _associations.value.map {
                        if (it.uid == updatedAssociation.uid) updatedAssociation else it
                      }
                  onSuccess()
                },
                onFailure)
          },
          onFailure = { exception ->
            Log.e("ImageRepository", "Failed to store image: $exception")
            onFailure(exception)
          })
    } else {
      associationRepository.saveAssociation(
          association,
          {
            _associations.value =
                _associations.value.map { if (it.uid == association.uid) association else it }
            onSuccess()
          },
          onFailure)
    }
  }

  /**
   * Finds an association, in the association list, by its ID.
   *
   * @param id The ID of the association to find.
   * @return The association with the given ID, or null if no such association exists.
   */
  fun findAssociationById(id: String): Association? {
    return _associations.value.find { it.uid == id }
  }

  /**
   * Selects an association by its ID and updates the [_selectedAssociation] state flow. It also
   * fetches the events and members of the selected association.
   *
   * @param associationId The ID of the association to select.
   */
  fun selectAssociation(associationId: String) {
    _selectedAssociation.value =
        findAssociationById(associationId).also { it ->
          it?.events?.requestAll()
          it?.members?.forEach { fetchUserFromMember(it) }
        }
  }
}
