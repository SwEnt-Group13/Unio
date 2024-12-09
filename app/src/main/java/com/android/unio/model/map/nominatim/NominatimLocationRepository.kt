package com.android.unio.model.map.nominatim

import android.util.Log
import com.android.unio.model.map.Location
import com.android.unio.model.map.LocationRepository
import javax.inject.Inject
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

/**
 * Repository for searching locations using the Nominatim API.
 *
 * @param apiService The Nominatim API service.
 */
class NominatimLocationRepository @Inject constructor(private val apiService: NominatimApiService) :
    LocationRepository {

  /**
   * Searches for locations using the Nominatim API.
   *
   * @param query The search query.
   * @return A flow of suggested locations.
   */
  override fun search(query: String): Flow<List<Location>> = flow {
    try {
      val response = apiService.search(query)
      val locations =
          response.map {
            val addressComponents =
                listOfNotNull(
                    it.address.road,
                    it.address.houseNumber,
                    it.address.postcode,
                    (it.address.village ?: it.address.town ?: it.address.city ?: it.address.municipality),
                    (it.address.region ?: it.address.county),
                    it.address.state,
                    it.address.country)

            val shortFormattedAddress = addressComponents.joinToString(", ")
            Location(
                latitude = it.lat.toDouble(),
                longitude = it.lon.toDouble(),
                name = shortFormattedAddress)
          }
      delay(1000) // Simulate 1 second delay to respect the API rate limit
      emit(locations)
    } catch (e: Exception) {
      Log.e("NominatimRepository", "Error during search: ", e)
    }
  }
}
