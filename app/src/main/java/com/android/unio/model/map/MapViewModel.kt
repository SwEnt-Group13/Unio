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

  private var locationCallback: LocationCallback? = null

  private val _centerLocation = MutableStateFlow<LatLng?>(null)
  val centerLocation: StateFlow<LatLng?> = _centerLocation.asStateFlow()

  /**
   * Sets a center location for the map given a location
   *
   * @param location the location to center the map on.
   */
  fun setCenterLocation(location: Location?) {
    if (location != null) {
      _centerLocation.value = LatLng(location.latitude, location.longitude)
    } else {
      _centerLocation.value = null
    }
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

  fun stopLocationUpdates() {
    locationCallback?.let { fusedLocationClient.removeLocationUpdates(it) }
  }

  fun hasLocationPermissions(context: Context): Boolean {
    return ContextCompat.checkSelfPermission(
        context, android.Manifest.permission.ACCESS_FINE_LOCATION) ==
        PackageManager.PERMISSION_GRANTED ||
        ContextCompat.checkSelfPermission(
            context, android.Manifest.permission.ACCESS_COARSE_LOCATION) ==
            PackageManager.PERMISSION_GRANTED
  }
}
