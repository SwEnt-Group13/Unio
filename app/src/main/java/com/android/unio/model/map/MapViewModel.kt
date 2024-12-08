package com.android.unio.model.map

import android.content.Context
import android.content.pm.PackageManager
import android.os.Looper
import android.util.Log
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModel
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.Priority
import com.google.android.gms.maps.model.LatLng
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

const val REFRESH_INTERVAL_MILLIS: Long = 10000
const val MIN_REFRESH_INTERVAL_MILLIS: Long = 5000

@HiltViewModel
class MapViewModel
@Inject
constructor(private val fusedLocationClient: FusedLocationProviderClient) : ViewModel() {

  /** State flow that holds the user's location. */
  private val _userLocation = MutableStateFlow<LatLng?>(null)
  val userLocation: StateFlow<LatLng?> = _userLocation.asStateFlow()

  /** Callback for location updates, which parses the location and updates the [_userLocation]. */
  private var locationCallback: LocationCallback? = null

  /**
   * State flow that holds the center location of the map desired, mainly used for opening the map
   * from an event detail page.
   */
  private val _centerLocation = MutableStateFlow<LatLng?>(null)
  val centerLocation: StateFlow<LatLng?> = _centerLocation.asStateFlow()

  /** State flow that holds the uid of the event to highlight on the map. */
  private val _highlightedEventUid = MutableStateFlow<String?>(null)
  val highlightedEventUid: StateFlow<String?> = _highlightedEventUid.asStateFlow()

  /**
   * Sets a highlighted event on the map given a uid and location.
   *
   * @param uid the uid of the event to highlight.
   * @param location the location of the event to center the map on.
   */
  fun setHighlightedEvent(uid: String?, location: Location?) {
    uid?.let { _highlightedEventUid.value = uid }
    location?.let { _centerLocation.value = LatLng(it.latitude, it.longitude) }
  }

  /** Clears the highlighted event on the map. */
  fun clearHighlightedEvent() {
    _highlightedEventUid.value = null
    _centerLocation.value = null
  }

  /** Fetches the user's location and updates the [_userLocation] state flow. */
  fun fetchUserLocation(context: Context) {
    if (hasLocationPermissions(context)) {
      try {
        fusedLocationClient.lastLocation.addOnSuccessListener { location ->
          location?.let { _userLocation.value = LatLng(it.latitude, it.longitude) }
        }
      } catch (e: SecurityException) {
        Log.e("MapViewModel", "Permission for location access was revoked: ${e.localizedMessage}")
      }
    } else {
      Log.e("MapViewModel", "Location permission is not granted.")
    }
  }

  /**
   * Starts location updates which automatically update the [_userLocation] state flow.
   *
   * @param context the context to use for requesting location updates.
   */
  fun startLocationUpdates(context: Context) {
    if (hasLocationPermissions(context)) {
      val locationRequest =
          LocationRequest.Builder(REFRESH_INTERVAL_MILLIS)
              .setMinUpdateIntervalMillis(MIN_REFRESH_INTERVAL_MILLIS)
              .setPriority(Priority.PRIORITY_BALANCED_POWER_ACCURACY)
              .build()

      locationCallback =
          object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult) {
              locationResult.lastLocation?.let {
                _userLocation.value = LatLng(it.latitude, it.longitude)
              }
            }
          }

      try {
        fusedLocationClient.requestLocationUpdates(
            locationRequest, locationCallback!!, Looper.getMainLooper())
      } catch (e: SecurityException) {
        Log.e("MapViewModel", "Permission for location access was revoked: ${e.localizedMessage}")
      }
    } else {
      Log.e("MapViewModel", "Location permission is not granted.")
    }
  }

  /** Stops location updates. */
  fun stopLocationUpdates() {
    locationCallback?.let { fusedLocationClient.removeLocationUpdates(it) }
  }

  /**
   * Checks if the app has location permissions.
   *
   * @param context the context to check for location permissions.
   * @return true if the used has given location permissions, false otherwise.
   */
  fun hasLocationPermissions(context: Context): Boolean {
    return ContextCompat.checkSelfPermission(
        context, android.Manifest.permission.ACCESS_FINE_LOCATION) ==
        PackageManager.PERMISSION_GRANTED ||
        ContextCompat.checkSelfPermission(
            context, android.Manifest.permission.ACCESS_COARSE_LOCATION) ==
            PackageManager.PERMISSION_GRANTED
  }
}
