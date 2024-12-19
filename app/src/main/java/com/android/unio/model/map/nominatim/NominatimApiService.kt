package com.android.unio.model.map.nominatim

import retrofit2.http.GET
import retrofit2.http.Headers
import retrofit2.http.Query

/**
 * Nominatim API service interface.
 *
 * This interface defines the methods to interact with the Nominatim API through URL queries, for
 * Retrofit which can build the URL automatically thanks to it.
 */
interface NominatimApiService {
  @Headers("User-Agent: Unio/0.3 (unio.epfl@gmail.com)")
  @GET("search")

  /**
   * Search for a location using the Nominatim API.
   *
   * @param query the query to search for.
   * @param format the format of the response.
   * @param addressdetails whether to include address details in the response.
   */
  suspend fun search(
      @Query("q") query: String,
      @Query("format") format: String = "json",
      @Query("addressdetails") addressdetails: Int = 1
  ): List<NominatimLocationResponse>
}
