package com.android.unio.model.map.nominatim

import retrofit2.http.GET
import retrofit2.http.Headers
import retrofit2.http.Query

interface NominatimApiService {
  @Headers("User-Agent: Unio/0.3 (zafarderie@gmail.com)")
  @GET("search")
  suspend fun search(
      @Query("q") query: String,
      @Query("format") format: String = "json",
      @Query("addressdetails") addressdetails: Int = 1
  ): List<NominatimLocationResponse>
}
