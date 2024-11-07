package com.android.unio.model.association

import android.util.Log
import androidx.lifecycle.ViewModel
import com.android.unio.model.event.Event
import com.android.unio.model.event.EventRepository
import com.android.unio.model.image.ImageRepositoryFirebaseStorage
import java.io.InputStream
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

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

  fun addAssociation(
      inputStream: InputStream,
      association: Association,
      onSuccess: () -> Unit,
      onFailure: (Exception) -> Unit
  ) {
    imageRepository.uploadImage(
        inputStream,
        "images/associations/${association.uid}",
        { uri ->
          association.image = uri
          associationRepository.addAssociation(association, onSuccess, onFailure)
        },
        { e -> Log.e("ImageRepository", "Failed to store image : $e") })
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
