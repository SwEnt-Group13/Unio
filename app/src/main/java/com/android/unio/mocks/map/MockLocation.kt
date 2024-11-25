package com.android.unio.mocks.map

import com.android.unio.model.map.Location

/**
 * MockLocation class provides edge-case instances of the Location data class for testing purposes.
 */
class MockLocation {
  companion object {

    /** Enums for each edge-case category * */
    enum class EdgeCaseLatitude(val value: Double) {
      MIN(-90.0),
      MAX(90.0),
      INVALID_LATITUDE(-91.0), // Invalid latitude
      TYPICAL(45.0) // A typical valid latitude
    }

    enum class EdgeCaseLongitude(val value: Double) {
      MIN(-180.0),
      MAX(180.0),
      INVALID_LONGITUDE(-181.0), // Invalid longitude
      TYPICAL(90.0) // A typical valid longitude
    }

    enum class EdgeCaseName(val value: String) {
      EMPTY(""),
      SHORT("A"),
      LONG("This is a very long location name that exceeds typical lengths for testing purposes."),
      SPECIAL_CHARACTERS("Location #1@"),
      TYPICAL("10 downing street")
    }

    /** Returns a list of edge-case locations based on selected edge cases */
    fun createEdgeCaseMockLocations(
        selectedLatitudes: List<EdgeCaseLatitude> = EdgeCaseLatitude.entries,
        selectedLongitudes: List<EdgeCaseLongitude> = EdgeCaseLongitude.entries,
        selectedNames: List<EdgeCaseName> = EdgeCaseName.entries
    ): List<Location> {
      val locations = mutableListOf<Location>()
      for (latitude in selectedLatitudes) {
        for (longitude in selectedLongitudes) {
          for (name in selectedNames) {
            locations.add(
                createMockLocation(
                    latitude = latitude.value, longitude = longitude.value, name = name.value))
          }
        }
      }
      return locations
    }

    /** Creates a mock Location with specified properties for testing edge cases. */
    fun createMockLocation(
        latitude: Double = EdgeCaseLatitude.TYPICAL.value,
        longitude: Double = EdgeCaseLongitude.TYPICAL.value,
        name: String = EdgeCaseName.TYPICAL.value
    ): Location {
      return Location(latitude = latitude, longitude = longitude, name = name)
    }
  }
}
