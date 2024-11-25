package com.android.unio.model.map

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.android.libraries.places.api.model.AutocompletePrediction
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class PlacesSearchViewModel
@Inject
constructor(private val repository: PlacesSearchRepository) : ViewModel() {

    private val locationSuggestions_ = MutableStateFlow<List<AutocompletePrediction>>(emptyList())
    val locationSuggestions: StateFlow<List<AutocompletePrediction>> = locationSuggestions_.asStateFlow()

    private val query_ = MutableStateFlow("")
    val query: StateFlow<String> = query_.asStateFlow()

    private val selectedLocation_ = MutableStateFlow<Location?>(null)
    val selectedLocation: StateFlow<Location?> = selectedLocation_.asStateFlow()

    init {
        viewModelScope.launch {
            query_
                .filter { it.isNotEmpty() }
                .collectLatest { query ->
                    Log.e("PlacesSearchViewModel", "Searching for $query")
                    repository.searchPlaces(
                        query,
                        onResult = { predictions ->
                            locationSuggestions_.value = predictions
                        },
                        onError = { Log.e("PlacesSearchViewModel", "Error fetching places.") }
                    )
                }
        }
    }

    fun setQuery(query: String) {
        query_.value = query
    }

    fun selectLocation(prediction: AutocompletePrediction) {
        repository.fetchPlaceDetails(prediction.placeId, { locationWithCoordinates ->
            selectedLocation_.value = locationWithCoordinates
        }, { error ->
            Log.e("PlacesSearchViewModel", "Error fetching place details: $error")
        })
    }
}