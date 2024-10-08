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

class ExploreViewModel(val repository: AssociationRepositoryFirestore) : ViewModel() {
  private val _associations = MutableStateFlow<List<Association>>(emptyList())
  val associations: StateFlow<List<Association>> = _associations

  init {
    repository.init { fetchAssociations() }
  }

  companion object {
    val Factory: ViewModelProvider.Factory =
        object : ViewModelProvider.Factory {
          @Suppress("UNCHECKED_CAST")
          override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return ExploreViewModel(AssociationRepositoryFirestore(Firebase.firestore)) as T
          }
        }
  }

  fun fetchAssociations() {
    viewModelScope.launch {
      repository.getAssociations(
          onSuccess = { fetchedAssociations -> _associations.value = fetchedAssociations },
          onFailure = { exception ->
            _associations.value = emptyList()
            Log.e("ExploreViewModel", "Failed to fetch associations", exception)
          })
    }
  }
}
