package com.android.unio.model.map.nominatim

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.android.unio.model.map.Location
import com.android.unio.model.map.LocationRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

@HiltViewModel
class NominatimLocationSearchViewModel
@Inject
constructor(private val repository: LocationRepository) : ViewModel() {

  private val _query = MutableStateFlow("")
  val query: StateFlow<String> = _query.asStateFlow()

  private val _locationSuggestions = MutableStateFlow<List<Location>>(emptyList())
  val locationSuggestions: StateFlow<List<Location>> = _locationSuggestions.asStateFlow()

  fun setQuery(query: String) {
    _query.value = query
  }

  init {
    collectQuery()
  }

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
}
