package com.android.unio.mocks.map

import com.android.unio.model.map.Location
import kotlin.random.Random

/** MockLocation class provides sample instances of the Location data class for testing purposes. */
class MockLocation {
  companion object {
    /**
     * Creates a mock Location with customizable properties.
     *
     * @param latitude Latitude of the location
     * @param longitude Longitude of the location
     * @param name Name or address of the location
     */
    fun createMockLocation(
        latitude: Double = Random.nextDouble(-90.0, 90.0),
        longitude: Double = Random.nextDouble(-180.0, 180.0),
        name: String = "Location ${Random.nextInt(1, 1000)}"
    ): Location {
      return Location(latitude, longitude, name)
    }
  }
}
