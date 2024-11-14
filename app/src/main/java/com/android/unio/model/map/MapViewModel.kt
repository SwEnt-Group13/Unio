package com.android.unio.model.map

import android.content.Context
import android.content.pm.PackageManager
import android.util.Log
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModel
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.maps.model.LatLng
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

@HiltViewModel
class MapViewModel
@Inject
constructor(private val fusedLocationClient: FusedLocationProviderClient) : ViewModel() {

  /** State flow that holds the user's location. */
  private val _userLocation = MutableStateFlow<LatLng?>(null)
  val userLocation: StateFlow<LatLng?> = _userLocation.asStateFlow()

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

  fun hasLocationPermissions(context: Context): Boolean {
    return ContextCompat.checkSelfPermission(
        context, android.Manifest.permission.ACCESS_FINE_LOCATION) ==
        PackageManager.PERMISSION_GRANTED ||
        ContextCompat.checkSelfPermission(
            context, android.Manifest.permission.ACCESS_COARSE_LOCATION) ==
            PackageManager.PERMISSION_GRANTED
  }
}
