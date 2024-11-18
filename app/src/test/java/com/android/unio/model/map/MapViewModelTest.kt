package com.android.unio.model.map

import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.os.Looper
import androidx.core.content.ContextCompat
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.Priority
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.tasks.OnFailureListener
import com.google.android.gms.tasks.OnSuccessListener
import com.google.android.gms.tasks.Task
import io.mockk.MockKAnnotations
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.mockk
import io.mockk.slot
import io.mockk.verify
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.Shadows.shadowOf

@RunWith(RobolectricTestRunner::class)
class MapViewModelTest {

  @MockK private lateinit var context: Context
  @MockK private lateinit var fusedLocationClient: FusedLocationProviderClient
  @MockK private lateinit var locationTask: Task<Location>

  private lateinit var mapViewModel: MapViewModel

  @Before
  fun setUp() {
    MockKAnnotations.init(this, relaxed = true)

    every {
      ContextCompat.checkSelfPermission(context, android.Manifest.permission.ACCESS_FINE_LOCATION)
    } returns PackageManager.PERMISSION_GRANTED

    every {
      ContextCompat.checkSelfPermission(context, android.Manifest.permission.ACCESS_COARSE_LOCATION)
    } returns PackageManager.PERMISSION_GRANTED

    // This is fine to do because the fusedLocationClient is a mock
    mapViewModel = MapViewModel(fusedLocationClient)
  }

  @Test
  fun testFetchUserLocationWithLocationPermissionGrantedAndLocationReturned() = runTest {
    val mockLatLng = LatLng(46.518831258, 6.559331096)
    val mockLocation =
        Location("mockProvider").apply {
          latitude = mockLatLng.latitude
          longitude = mockLatLng.longitude
        }

    every { fusedLocationClient.lastLocation } returns locationTask
    every { locationTask.addOnSuccessListener(any()) } answers
        {
          (it.invocation.args[0] as OnSuccessListener<Location?>).onSuccess(mockLocation)
          locationTask
        }

    mapViewModel.fetchUserLocation(context)

    // Required for the location Task to complete
    shadowOf(Looper.getMainLooper()).idle()

    val result = mapViewModel.userLocation.first()
    assertEquals(mockLatLng, result)
  }

  @Test
  fun testFetchUserLocationWithSecurityExceptionThrown() = runTest {
    every { fusedLocationClient.lastLocation } returns locationTask
    every { locationTask.addOnFailureListener(any()) } answers
        {
          (it.invocation.args[0] as OnFailureListener).onFailure(
              SecurityException("Security exception"))
          locationTask
        }

    mapViewModel.fetchUserLocation(context)

    // Required for the location Task to complete
    shadowOf(Looper.getMainLooper()).idle()

    val result = mapViewModel.userLocation.first()
    assertNull(result)
  }

  @Test
  fun testFetchUserLocationWithLocationPermissionDenied() = runTest {
    every {
      ContextCompat.checkSelfPermission(context, android.Manifest.permission.ACCESS_FINE_LOCATION)
    } returns PackageManager.PERMISSION_DENIED

    mapViewModel.fetchUserLocation(context)

    val result = mapViewModel.userLocation.first()
    assertEquals(null, result)
  }

  @Test
  fun testStartLocationUpdatesWithPermissionsGranted() = runTest {
    val mockLatLng = LatLng(46.518831258, 6.559331096)
    val mockLocation = Location("mockProvider").apply {
      latitude = mockLatLng.latitude
      longitude = mockLatLng.longitude
    }

    val locationCallbackSlot = slot<LocationCallback>()
    val locationResult = LocationResult.create(listOf(mockLocation))
    val taskMock: Task<Void> = mockk()

    every { fusedLocationClient.requestLocationUpdates(any(), capture(locationCallbackSlot), any()) } returns taskMock
    every { taskMock.addOnSuccessListener(any()) } answers {
      (it.invocation.args[0] as OnSuccessListener<Void>).onSuccess(null)
      taskMock
    }

    mapViewModel.startLocationUpdates(context)

    locationCallbackSlot.captured.onLocationResult(locationResult)

    val result = mapViewModel.userLocation.first()
    assertEquals(mockLatLng, result)
  }

  @Test
  fun testStartLocationUpdatesWithPermissionsDenied() = runTest {
    every {
      ContextCompat.checkSelfPermission(context, android.Manifest.permission.ACCESS_FINE_LOCATION)
    } returns PackageManager.PERMISSION_DENIED

    every {
      ContextCompat.checkSelfPermission(context, android.Manifest.permission.ACCESS_COARSE_LOCATION)
    } returns PackageManager.PERMISSION_DENIED

    mapViewModel.startLocationUpdates(context)

    verify(exactly = 0) {
      fusedLocationClient.requestLocationUpdates(any(), any<LocationCallback>(), any())
    }

    val result = mapViewModel.userLocation.first()
    assertNull(result)
  }

  @Test
  fun testStartLocationUpdatesWithSecurityExceptionThrown() = runTest {
    val locationRequest = LocationRequest.Builder(10000)
      .setMinUpdateIntervalMillis(5000)
      .setPriority(Priority.PRIORITY_BALANCED_POWER_ACCURACY)
      .build()

    val locationCallback = mockk<LocationCallback>(relaxed = true)

    every {
      fusedLocationClient.requestLocationUpdates(
        locationRequest,
        locationCallback,
        Looper.getMainLooper()
      )
    } throws SecurityException("Security exception!")

    mapViewModel.startLocationUpdates(context)

    val result = mapViewModel.userLocation.first()
    assertNull(result)
  }

  @Test
  fun testStopLocationUpdates() = runTest {
    val locationRequest = LocationRequest.Builder(10000)
      .setMinUpdateIntervalMillis(5000)
      .setPriority(Priority.PRIORITY_BALANCED_POWER_ACCURACY)
      .build()

    val locationCallbackSlot = slot<LocationCallback>()
    every {
      fusedLocationClient.requestLocationUpdates(
        locationRequest,
        capture(locationCallbackSlot),
        Looper.getMainLooper()
      )
    } returns mockk()

    every { fusedLocationClient.removeLocationUpdates(any<LocationCallback>()) } returns mockk()

    mapViewModel.startLocationUpdates(context)

    mapViewModel.stopLocationUpdates()

    verify { fusedLocationClient.removeLocationUpdates(locationCallbackSlot.captured) }
  }
}
