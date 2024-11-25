package com.android.unio.model.map

import com.google.android.libraries.places.api.model.AutocompletePrediction

interface PlacesSearchRepository {
  fun searchPlaces(
      query: String,
      onResult: (List<AutocompletePrediction>) -> Unit,
      onError: (String) -> Unit
  )

  fun fetchPlaceDetails(placeId: String, onResult: (Location) -> Unit, onError: (String) -> Unit)
}
