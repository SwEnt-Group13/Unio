package com.android.unio.model.search

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.android.unio.model.association.Association
import com.android.unio.model.event.Event
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

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

  // Used to debounce the search query
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

  /**
   * Enum class representing the type of search to perform. ASSOCIATION for searching associations,
   * and EVENT for searching events.
   */
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

  /** Clears the list of events and sets the search status to [Status.IDLE]. */
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
}
