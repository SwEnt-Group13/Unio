package com.android.unio.ui.map

import android.Manifest
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import com.android.unio.R
import com.android.unio.model.event.Event
import com.android.unio.model.event.EventViewModel
import com.android.unio.model.map.MapViewModel
import com.android.unio.model.strings.MapStrings
import com.android.unio.model.strings.test_tags.MapTestTags
import com.android.unio.model.user.UserViewModel
import com.android.unio.ui.navigation.NavigationAction
import com.android.unio.ui.theme.mapUserLocationCircleFiller
import com.android.unio.ui.theme.mapUserLocationCircleStroke
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.firebase.Timestamp
import com.google.maps.android.compose.CameraPositionState
import com.google.maps.android.compose.Circle
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.MapProperties
import com.google.maps.android.compose.MapUiSettings
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.MarkerState
import com.google.maps.android.compose.rememberCameraPositionState
import java.util.Calendar
import java.util.concurrent.TimeUnit

val EPFL_COORDINATES = LatLng(46.518831258, 6.559331096)
const val APPROXIMATE_CIRCLE_RADIUS = 30.0
const val APPROXIMATE_CIRCLE_OUTLINE_WIDTH = 2f
const val INITIAL_ZOOM_LEVEL = 15f

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MapScreen(
    navigationAction: NavigationAction,
    eventViewModel: EventViewModel,
    userViewModel: UserViewModel,
    mapViewModel: MapViewModel,
) {
    val context = LocalContext.current
    val cameraPositionState = rememberCameraPositionState()
    val centerLocation by mapViewModel.centerLocation.collectAsState()
    val userLocation by mapViewModel.userLocation.collectAsState()
    var initialCentered by remember { mutableStateOf(false) }
    var isMyLocationEnabled by remember { mutableStateOf(false) }
    var showApproximateCircle by remember { mutableStateOf(false) }

    val permissionLauncher =
        rememberLauncherForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { permissions ->
            when {
                permissions.getOrDefault(Manifest.permission.ACCESS_FINE_LOCATION, false) -> {
                    mapViewModel.fetchUserLocation(context)
                    isMyLocationEnabled = true
                    showApproximateCircle = false
                }

                permissions.getOrDefault(Manifest.permission.ACCESS_COARSE_LOCATION, false) -> {
                    mapViewModel.fetchUserLocation(context)
                    isMyLocationEnabled = false
                    showApproximateCircle = true
                }

                else -> {
                    Log.e("MapScreen", "Location permission is not granted.")
                }
            }
        }

    /** Request location permissions. */
    fun requestPermissions() {
        permissionLauncher.launch(
            arrayOf(
                Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION
            )
        )
    }

    // Check what permissions are already granted
    LaunchedEffect(Unit) {
        when (PackageManager.PERMISSION_GRANTED) {
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) -> {
                isMyLocationEnabled = true
                showApproximateCircle = false
                mapViewModel.fetchUserLocation(context)
            }

            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) -> {
                isMyLocationEnabled = false
                showApproximateCircle = true
                requestPermissions()
                mapViewModel.fetchUserLocation(context)
            }

            else -> {
                requestPermissions()
            }
        }
        // this line seems to not do anything !
        cameraPositionState.position =
          CameraPosition.fromLatLngZoom(userLocation ?: EPFL_COORDINATES, INITIAL_ZOOM_LEVEL)
    }

    // Center map on the center location initially if available
    LaunchedEffect(userLocation) {
        if (userLocation != null && centerLocation == null) {
            cameraPositionState.position =
                CameraPosition.fromLatLngZoom(userLocation!!, INITIAL_ZOOM_LEVEL)
            initialCentered = true
        } else if (centerLocation != null) {
            cameraPositionState.position =
                CameraPosition.fromLatLngZoom(centerLocation!!, INITIAL_ZOOM_LEVEL)
        } else {
            cameraPositionState.position =
                CameraPosition.fromLatLngZoom(EPFL_COORDINATES, INITIAL_ZOOM_LEVEL)
        }
    }

    Scaffold(
        modifier = Modifier.testTag(MapTestTags.SCREEN),
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        context.getString(R.string.map_event_title),
                        modifier = Modifier.testTag(MapTestTags.TITLE)
                    )
                },
                navigationIcon = {
                    IconButton(
                        onClick = {
                            mapViewModel.setCenterLocation(null)
                            navigationAction.goBack() },
                        modifier = Modifier.testTag(MapTestTags.GO_BACK_BUTTON)
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Outlined.ArrowBack,
                            contentDescription = context.getString(R.string.map_event_go_back_button)
                        )
                    }
                })
        },
        floatingActionButton = {
            FloatingActionButton(
                modifier = Modifier
                    .padding(bottom = 80.dp)
                    .testTag(MapTestTags.CENTER_ON_USER_FAB),
                onClick = {
                    userLocation?.let {
                        cameraPositionState.position = CameraPosition.fromLatLngZoom(it, 15f)
                    }
                }) {
                Icon(
                    Icons.Default.MyLocation,
                    contentDescription =
                    context.getString(R.string.map_content_description_center_on_user)
                )
            }
        }) { pd ->
        EventMap(
            pd,
            eventViewModel,
            userViewModel,
            cameraPositionState,
            isMyLocationEnabled,
            showApproximateCircle,
            userLocation
        )
    }
}

@Composable
fun EventMap(
    pd: PaddingValues,
    eventViewModel: EventViewModel,
    userViewModel: UserViewModel,
    cameraPositionState: CameraPositionState,
    isMyLocatonEnabled: Boolean,
    showApproximateCircle: Boolean,
    userLocation: LatLng?
) {
    val events = eventViewModel.events.collectAsState()

    val user by userViewModel.user.collectAsState()
    val savedEvents by user!!.savedEvents.list.collectAsState()
    LaunchedEffect(user) { user?.savedEvents?.requestAll() }

    GoogleMap(
        modifier = Modifier
            .padding(pd)
            .testTag(MapTestTags.GOOGLE_MAPS),
        cameraPositionState = cameraPositionState,
        properties = MapProperties(isMyLocationEnabled = isMyLocatonEnabled),
        uiSettings = MapUiSettings(myLocationButtonEnabled = false)
    ) {
        // Display the user's approximate location if only coarse location is available
        if (showApproximateCircle && userLocation != null) {
            Circle(
                center = userLocation,
                radius = APPROXIMATE_CIRCLE_RADIUS,
                fillColor = mapUserLocationCircleFiller,
                strokeColor = mapUserLocationCircleStroke,
                strokeWidth = APPROXIMATE_CIRCLE_OUTLINE_WIDTH,
                tag = MapTestTags.LOCATION_APPROXIMATE_CIRCLE
            )
        }

        // Display saved events
        savedEvents.forEach { event ->
            if (event.date.toDate() > Calendar.getInstance().time) {
                DisplayEventMarker(event, R.drawable.favorite_pinpoint)
            }
        }

        // Display all events (should refactor to the ones that are soon to happen when more events
        // are added)
        events.value
            .filterNot { event -> savedEvents.any { it.uid == event.uid } }
            .forEach { event ->
                if (event.date.toDate() > Calendar.getInstance().time) {
                    DisplayEventMarker(event, null)
                }
            }
    }
}

/**
 * Display an event marker on the map.
 *
 * @param event the event to display
 * @param customIconResId the resource id of the custom icon to use for the marker
 * @return a marker for the event
 */
@Composable
fun DisplayEventMarker(event: Event, customIconResId: Int?) {
    val timer = timeUntilEvent(event.date)
    event.location.let { location ->
        val pinPointIcon =
            if (customIconResId != null) {
                val bitmap =
                    BitmapFactory.decodeResource(LocalContext.current.resources, customIconResId)
                val scaledBitmap = Bitmap.createScaledBitmap(bitmap, 96, 96, false)
                BitmapDescriptorFactory.fromBitmap(scaledBitmap)
            } else {
                BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE)
            }

        Marker(
            contentDescription = "Event: ${event.title}",
            state = MarkerState(position = LatLng(location.latitude, location.longitude)),
            title = event.title,
            snippet = "$timer - ${event.description}",
            icon = pinPointIcon
        )
    }
}

/**
 * Calculate the time until an event occurs.
 *
 * @param eventTimestamp the timestamp of the event
 * @return a string giving information about the time until the event occurs
 */
fun timeUntilEvent(eventTimestamp: Timestamp): String {
    val currentTime = Timestamp.now()
    val timeDifference = eventTimestamp.seconds - currentTime.seconds

    if (timeDifference < 0) return MapStrings.EVENT_ALREADY_OCCURED

    val days = TimeUnit.SECONDS.toDays(timeDifference)
    val hours = TimeUnit.SECONDS.toHours(timeDifference) % 24
    return if (days > 0) "In $days days, $hours hours" else "In $hours hours"
}
