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
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel for searching associations and events. It uses a [SearchRepository] to create the
 * AppSearch database and exposes the results through a [StateFlow] containing a list of
 * respectively [Association] and [Event]
 */
@HiltViewModel
class SearchViewModel @Inject constructor(private val repository: SearchRepository) : ViewModel() {
  private val _associations = MutableStateFlow<List<Association>>(emptyList())
  val associations: StateFlow<List<Association>> = _associations.asStateFlow()

  private val _events = MutableStateFlow<List<Event>>(emptyList())
  val events: StateFlow<List<Event>> = _events.asStateFlow()

  val status = MutableStateFlow(Status.IDLE)

  private var searchJob: Job? = null

  /**
   * Enum class for the search status The IDLE status is used when there is no query message The
   * LOADING status is used when the search is in progress The SUCCESS status is used when the
   * search is successful, this can also be the case if results are empty The ERROR status is used
   * when the search fails
   */
  enum class Status {
    LOADING,
    SUCCESS,
    ERROR,
    IDLE
  }

  enum class SearchType {
    ASSOCIATION,
    EVENT
  }
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
      status.value = Status.LOADING
      val results = repository.searchAssociations(query)
      _associations.value = results
      status.value = Status.SUCCESS
    }
  }

  /**
   * Debounces the search query to avoid making too many requests in a short period of time.
   *
   * @param query The query to search for.
   * @param searchType The type of search to perform.
   */
  fun debouncedSearch(query: String, searchType: SearchType) {
    searchJob?.cancel()
    if (query.isEmpty()) {
      when (searchType) {
        SearchType.EVENT -> clearEvents()
        SearchType.ASSOCIATION -> clearAssociations()
      }
    } else {
      searchJob =
          viewModelScope.launch {
            delay(500)
            when (searchType) {
              SearchType.EVENT -> searchEvents(query)
              SearchType.ASSOCIATION -> searchAssociations(query)
            }
          }
    }
  }

  /** Clears the list of associations and sets the search status to [Status.IDLE]. */
  private fun clearAssociations() {
    _associations.value = emptyList()
    status.value = Status.IDLE
  }

  private fun clearEvents() {
    _events.value = emptyList()
    status.value = Status.IDLE
  }

  /**
   * Searches the events in the search database using the given query and updates the internal
   * [MutableStateFlow].
   *
   * @param query The query to search for.
   */
  fun searchEvents(query: String) {
    viewModelScope.launch {
      status.value = Status.LOADING
      val results = repository.searchEvents(query)
      results.forEach { event -> event.organisers.requestAll() }
      _events.value = results
      status.value = Status.SUCCESS
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
