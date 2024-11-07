package com.android.unio.model.association

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.android.unio.model.event.Event
import com.android.unio.model.event.EventRepository
import com.android.unio.model.event.EventRepositoryFirestore
import com.android.unio.model.image.ImageRepositoryFirebaseStorage
import com.google.firebase.Firebase
import com.google.firebase.firestore.firestore
import java.io.InputStream
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class AssociationViewModel(
    private val associationRepository: AssociationRepository,
    private val eventRepository: EventRepository
) : ViewModel() {
  private val _associations = MutableStateFlow<List<Association>>(emptyList())
  val associations: StateFlow<List<Association>> = _associations
  private val imageRepository = ImageRepositoryFirebaseStorage()

  private val _associationsByCategory =
      MutableStateFlow<Map<AssociationCategory, List<Association>>>(emptyMap())
  val associationsByCategory: StateFlow<Map<AssociationCategory, List<Association>>> =
      _associationsByCategory

  init {
    associationRepository.init { getAssociations() }
  }

  companion object {
    val Factory: ViewModelProvider.Factory =
        object : ViewModelProvider.Factory {
          @Suppress("UNCHECKED_CAST")
          override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return AssociationViewModel(
                AssociationRepositoryFirestore(Firebase.firestore),
                EventRepositoryFirestore(Firebase.firestore))
                as T
          }
        }
  }

  fun getEventsForAssociation(association: Association, onSuccess: (List<Event>) -> Unit) {
    viewModelScope.launch {
      eventRepository.getEventsOfAssociation(
          association.uid,
          onSuccess = onSuccess,
          onFailure = { exception ->
            Log.e(
                "ExploreViewModel",
                "Failed to get events for association ${association.fullName}",
                exception)
          })
    }
  }

  /**
   * Fetches all associations from the repository and updates the [_associations] and
   * [_associationsByCategory] state flows. If the fetch fails, the [_associations] state flow is
   * set to an empty list.
   */
  fun getAssociations() {
    viewModelScope.launch {
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
  }

  fun addAssociation(
      inputStream: InputStream,
      association: Association,
      onSuccess: () -> Unit,
      onFailure: (Exception) -> Unit
  ) {
    viewModelScope.launch {
      imageRepository.uploadImage(
          inputStream,
          "images/associations/${association.uid}",
          { uri ->
            association.image = uri
            associationRepository.saveAssociation(association, onSuccess, onFailure)
          },
          { e -> Log.e("ImageRepository", "Failed to store image : $e") })
    }
  }

    fun saveAssociation(
        association: Association,
        imageStream: InputStream?,
        onSuccess: () -> Unit,
        onFailure: (Exception) -> Unit
    ) {
        viewModelScope.launch {
            if (imageStream != null) {
                imageRepository.uploadImage(
                    imageStream = imageStream,
                    firebasePath = "images/associations/${association.uid}",
                    onSuccess = { imageUrl ->
                        val updatedAssociation = association.copy(image = imageUrl)
                        associationRepository.saveAssociation(updatedAssociation, {
                            // Update the list with the modified association
                            Log.d("AssociationViewModel", "Association saved with updated image.")
                            _associations.value = _associations.value.map {
                                if (it.uid == updatedAssociation.uid) updatedAssociation else it
                            }
                            onSuccess()
                        }, onFailure)
                    },
                    onFailure = { exception ->
                        Log.e("ImageRepository", "Failed to store image: $exception")
                        onFailure(exception)
                    }
                )
            } else {
                associationRepository.saveAssociation(association, {
                    Log.d("AssociationViewModel", "Association saved without image update.")
                    // Update the list with the modified association
                    _associations.value = _associations.value.map {
                        if (it.uid == association.uid) association else it
                    }
                    onSuccess()
                }, onFailure)
            }
        }
    }




    /**
   * Finds an association, in the association list, by its ID.
   *
   * @param id The ID of the association to find.
   * @return The association with the given ID, or null if no such association exists.
   */
  fun findAssociationById(id: String): Association? {
    _associations.value
        .find { it.uid == id }
        ?.let {
          return it
        } ?: return null
  }
}
