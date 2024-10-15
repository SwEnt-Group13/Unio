package com.android.unio.model.association

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.google.firebase.Firebase
import com.google.firebase.firestore.firestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class AssociationViewModel(val repository: AssociationRepository) : ViewModel() {
  private val _associations = MutableStateFlow<List<Association>>(emptyList())
  val associations: StateFlow<List<Association>> = _associations

    private val _associationsByCategory = MutableStateFlow<Map<AssociationCategory, List<Association>>>(emptyMap())
    val associationsByCategory: StateFlow<Map<AssociationCategory, List<Association>>> = _associationsByCategory

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
                      getAssociationsByCategory()},
          onFailure = { exception ->
            _associations.value = emptyList()
            Log.e("ExploreViewModel", "Failed to fetch associations", exception)
          })
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

    /**
     * Groups the associations by their category.
     */
    fun getAssociationsByCategory() {
        _associationsByCategory.value = _associations.value.groupBy { it.category }
    }
}
