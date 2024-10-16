package com.android.unio.model.association

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.android.unio.model.image.ImageRepositoryFirebaseStorage
import com.google.firebase.Firebase
import com.google.firebase.firestore.firestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.io.InputStream

class AssociationViewModel(val repository: AssociationRepository) : ViewModel() {
    private val _associations = MutableStateFlow<List<Association>>(emptyList())
    val associations: StateFlow<List<Association>> = _associations
    private val imageRepository = ImageRepositoryFirebaseStorage()

  private val _associationsByCategory =
      MutableStateFlow<Map<AssociationCategory, List<Association>>>(emptyMap())
  val associationsByCategory: StateFlow<Map<AssociationCategory, List<Association>>> =
      _associationsByCategory

  init {
    repository.init { getAssociations() }
  }

  companion object {
    val Factory: ViewModelProvider.Factory =
        object : ViewModelProvider.Factory {
          @Suppress("UNCHECKED_CAST")
          override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return AssociationViewModel(AssociationRepositoryFirestore(Firebase.firestore)) as T
          }
        }
  }

    fun getAssociations() {
    viewModelScope.launch {
      repository.getAssociations(
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
                    repository.addAssociation(association, onSuccess, onFailure)
                },
                { e -> Log.e("ImageRepository", "Failed to store image : $e") })
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
