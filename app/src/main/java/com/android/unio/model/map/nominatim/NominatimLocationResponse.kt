package com.android.unio.model.map.nominatim

import com.google.gson.annotations.SerializedName

/**
 * Data class representing a response from the Nominatim API. GsonConverter will parse the JSON
 * response automatically into this data class.
 *
 * @property lat The latitude of the location.
 * @property lon The longitude of the location.
 * @property displayName The display name of the location.
 * @property address The address of the location.
 */
data class NominatimLocationResponse(
    val lat: String,
    val lon: String,
    @SerializedName("display_name") val displayName: String,
    val address: Address
)

/**
 * Data class representing the address of a location.
 *
 * @property houseNumber The house number of the location.
 * @property road The road of the location.
 * @property city The city of the location.
 * @property town The town of the location.
 * @property village The village of the location.
 * @property state The state of the location.
 * @property postcode The postcode of the location.
 * @property country The country of the location.
 */
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
