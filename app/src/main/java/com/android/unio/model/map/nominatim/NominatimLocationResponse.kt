package com.android.unio.model.map.nominatim

import com.google.gson.annotations.SerializedName

data class NominatimLocationResponse(
    val lat: String,
    val lon: String,
    @SerializedName("display_name") val displayName: String,
    val address: Address
)

data class Address(
    @SerializedName("house_number") val houseNumber: String?,
    val road: String,
    val city: String?,
    val town: String?,
    val village: String?,
    val state: String,
    val postcode: String,
    val country: String
)
