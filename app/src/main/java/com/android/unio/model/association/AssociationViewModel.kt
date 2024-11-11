package com.android.unio.model.association

import android.util.Log
import androidx.lifecycle.ViewModel
import com.android.unio.model.event.Event
import com.android.unio.model.event.EventRepository
import com.android.unio.model.image.ImageRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import java.io.InputStream
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

@HiltViewModel
class AssociationViewModel
@Inject
constructor(
    private val associationRepository: AssociationRepository,
    private val eventRepository: EventRepository,
    private val imageRepository: ImageRepository
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

  fun getEventsForAssociation(association: Association, onSuccess: (List<Event>) -> Unit) {
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
                  // Update the list with the modified association
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
            // Update the list with the modified association
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

  fun selectAssociation(associationId: String) {
    _selectedAssociation.value =
        findAssociationById(associationId).also {
          it?.events?.requestAll()
          it?.members?.requestAll()
        }
  }
}
