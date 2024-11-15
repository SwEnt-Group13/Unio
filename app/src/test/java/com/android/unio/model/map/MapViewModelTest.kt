package com.android.unio.model.map

import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.os.Looper
import androidx.core.content.ContextCompat
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.tasks.OnFailureListener
import com.google.android.gms.tasks.OnSuccessListener
import com.google.android.gms.tasks.Task
import io.mockk.MockKAnnotations
import io.mockk.every
import io.mockk.impl.annotations.MockK
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
            ContextCompat.checkSelfPermission(
                context,
                android.Manifest.permission.ACCESS_FINE_LOCATION
            )
        } returns PackageManager.PERMISSION_GRANTED

        every {
            ContextCompat.checkSelfPermission(
                context,
                android.Manifest.permission.ACCESS_COARSE_LOCATION
            )
        } returns PackageManager.PERMISSION_GRANTED

        // This is fine to do because the fusedLocationClient is a mock
        mapViewModel = MapViewModel(fusedLocationClient)
    }

    @Test
    fun testFetchUserLocationWithLocationPermissionGrantedAndLocationReturned() = runTest {
        val mockLatLng = LatLng(46.518831258, 6.559331096)
        val mockLocation = Location("mockProvider").apply {
            latitude = mockLatLng.latitude
            longitude = mockLatLng.longitude
        }

        every { fusedLocationClient.lastLocation } returns locationTask
        every { locationTask.addOnSuccessListener(any()) } answers {
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
        every { locationTask.addOnFailureListener(any()) } answers {
            (it.invocation.args[0] as OnFailureListener).onFailure(SecurityException("Security exception"))
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
            ContextCompat.checkSelfPermission(
                context,
                android.Manifest.permission.ACCESS_FINE_LOCATION
            )
        } returns PackageManager.PERMISSION_DENIED

        mapViewModel.fetchUserLocation(context)

        val result = mapViewModel.userLocation.first()
        assertEquals(null, result)
    }
}