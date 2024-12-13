package com.android.unio.model.map.nominatim

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.android.unio.model.map.Location
import com.android.unio.model.map.LocationRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.launch

/**
 * ViewModel for searching locations using Nominatim API.
 *
 * @param repository LocationRepository : Repository for searching locations.
 */
@HiltViewModel
class NominatimLocationSearchViewModel
@Inject
constructor(private val repository: LocationRepository) : ViewModel() {

  /** Query for searching locations. */
  private val _query = MutableStateFlow("")
  val query: StateFlow<String> = _query.asStateFlow()

  /** List of location suggestions. */
  private val _locationSuggestions = MutableStateFlow<List<Location>>(emptyList())
  val locationSuggestions: StateFlow<List<Location>> = _locationSuggestions.asStateFlow()

  /** Launches the ViewModel scope. */
  init {
    collectQuery()
  }

  /**
   * Collects query and fetches location suggestions, using flows.
   *
   * Uses debounce to wait for user to stop typing. Uses flatMapLatest to only show the latest
   * search. Uses catch to handle exceptions and return an empty list for suggestions.
   */
  @OptIn(FlowPreview::class, ExperimentalCoroutinesApi::class)
  private fun collectQuery() {
    viewModelScope.launch {
      _query
          .debounce(1000)
          .filter { it.isNotEmpty() }
          .flatMapLatest { query -> repository.search(query).catch { emit(emptyList()) } }
          .collect { locations -> _locationSuggestions.value = locations }
    }
  }

  /**
   * Sets the query for searching locations.
   *
   * @param query String : Query for searching locations.
   */
  fun setQuery(query: String) {
    _query.value = query
  }
}
