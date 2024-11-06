package com.android.unio.model.search

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.android.unio.model.association.Association
import com.android.unio.model.association.AssociationRepository
import com.android.unio.model.event.Event
import com.android.unio.model.event.EventRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

/**
 * ViewModel for searching associations and events. It uses a [SearchRepository] to create the
 * AppSearch database and exposes the results through a [StateFlow] containing a list of
 * respectively [Association] and [Event]
 */
@HiltViewModel
class SearchViewModel @Inject constructor(private val repository: SearchRepository) : ViewModel() {
  private val _associations = MutableStateFlow<List<Association>>(emptyList())
  val associations: StateFlow<List<Association>> = _associations

  private val _events = MutableStateFlow<List<Event>>(emptyList())
  val events: StateFlow<List<Event>> = _events

  /** Initializes the ViewModel by creating the search database and connecting it to the session. */
  init {
    viewModelScope.launch { repository.init() }
  }

  /**
   * Searches the associations in the search database using the given query and updates the internal
   * [MutableStateFlow].
   *
   * @param query The query to search for.
   */
  fun searchAssociations(query: String) {
    viewModelScope.launch {
      val results = repository.searchAssociations(query)
      _associations.value = results
    }
  }

  /**
   * Searches the events in the search database using the given query and updates the internal
   * [MutableStateFlow].
   *
   * @param query The query to search for.
   */
  fun searchEvents(query: String) {
    viewModelScope.launch {
      val results = repository.searchEvents(query)
      _events.value = results
    }
  }

  public override fun onCleared() {
    super.onCleared()
    repository.closeSession()
  }

  /**
   * Factory for creating a [SearchViewModel] with a constructor that takes a [SearchRepository].
   */
  companion object {
    fun provideFactory(
        context: Context,
        associationRepository: AssociationRepository,
        eventRepository: EventRepository
    ): ViewModelProvider.Factory {
      val appContext = context.applicationContext
      return object : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
          if (modelClass.isAssignableFrom(SearchViewModel::class.java)) {
            val repository = SearchRepository(appContext, associationRepository, eventRepository)
            return SearchViewModel(repository) as T
          }
          throw IllegalArgumentException("Unknown ViewModel class")
        }
      }
    }
  }
}
