package com.android.unio.model.map.nominatim

import android.util.Log
import com.android.unio.model.map.Location
import com.android.unio.model.map.LocationRepository
import javax.inject.Inject
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

class NominatimLocationRepository @Inject constructor(private val apiService: NominatimApiService) :
    LocationRepository {

  override fun search(query: String): Flow<List<Location>> = flow {
    try {
      val response = apiService.search(query)
      val locations =
          response.map {
            Location(
                latitude = it.lat.toDouble(), longitude = it.lon.toDouble(), name = it.displayName)
          }
      emit(locations)
    } catch (e: Exception) {
      Log.e("NominatimRepository", "Error during search: ", e)
    }
    delay(1000)
  }
}
