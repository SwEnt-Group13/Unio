package com.android.unio.model.map

import android.util.Log
import com.google.android.libraries.places.api.model.AutocompletePrediction
import com.google.android.libraries.places.api.model.Place
import com.google.android.libraries.places.api.net.FetchPlaceRequest
import com.google.android.libraries.places.api.net.FindAutocompletePredictionsRequest
import com.google.android.libraries.places.api.net.PlacesClient
import javax.inject.Inject

class PlacesSearchRepositoryClient
@Inject constructor(private val placesClient: PlacesClient) : PlacesSearchRepository {

    override fun searchPlaces(
        query: String,
        onResult: (List<AutocompletePrediction>) -> Unit,
        onError: (String) -> Unit
    ) {
        val request = FindAutocompletePredictionsRequest.builder()
            .setQuery(query)
            .build()

        placesClient.findAutocompletePredictions(request)
            .addOnSuccessListener { response ->
                onResult(response.autocompletePredictions)
            }
            .addOnFailureListener { exception ->
                Log.e("PlacesSearchRepository", "Error: ${exception.localizedMessage}")
                onError("Failed to fetch results.")
            }
    }

    // Fetch full place details (including coordinates) using the placeId
    override fun fetchPlaceDetails(
        placeId: String,
        onResult: (Location) -> Unit,
        onError: (String) -> Unit
    ) {
        val placeFields = listOf(Place.Field.ID, Place.Field.DISPLAY_NAME, Place.Field.DISPLAY_NAME, Place.Field.LOCATION)
        val request = FetchPlaceRequest.newInstance(placeId, placeFields)

        placesClient.fetchPlace(request)
            .addOnSuccessListener { response ->
                val place = response.place
                val location = Location(
                    placeId = place.id ?: "",
                    name = place.displayName ?: "",
                    address = place.formattedAddress ?: "",
                    latitude = place.location?.latitude ?: 0.0,
                    longitude = place.location?.longitude ?: 0.0
                )
                onResult(location)
            }
            .addOnFailureListener { exception ->
                Log.e("PlacesSearchRepository", "Error fetching place details: ${exception.localizedMessage}")
                onError("Failed to fetch place details.")
            }
    }
}