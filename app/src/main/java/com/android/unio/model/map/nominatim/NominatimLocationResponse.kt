package com.android.unio.model.map.nominatim

import com.google.gson.annotations.SerializedName

data class NominatimLocationResponse(
    val lat: String,
    val lon: String,
    @SerializedName("display_name") val displayName: String
)
