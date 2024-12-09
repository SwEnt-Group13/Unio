package com.android.unio.model.map

/**
 * Data class representing a location.
 *
 * @property latitude The latitude of the location.
 * @property longitude The longitude of the location.
 * @property name The name of the location.
 */
data class Location(val latitude: Double = 0.0, val longitude: Double = 0.0, val name: String = "")
