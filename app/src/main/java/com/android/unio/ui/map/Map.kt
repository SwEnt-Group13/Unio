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
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
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
import com.android.unio.model.strings.test_tags.map.MapTestTags
import com.android.unio.model.user.UserViewModel
import com.android.unio.ui.navigation.NavigationAction
import com.android.unio.ui.theme.mapUserLocationCircleFiller
import com.android.unio.ui.theme.mapUserLocationCircleStroke
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.BitmapDescriptorFactory
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
import kotlinx.coroutines.launch

val EPFL_COORDINATES = LatLng(46.518831258, 6.559331096)
const val APPROXIMATE_CIRCLE_RADIUS = 30.0
const val APPROXIMATE_CIRCLE_OUTLINE_WIDTH = 2f
const val INITIAL_ZOOM_LEVEL = 15f

/**
 * The MapScreen composable displays a map with markers for events. This composable is a Scaffold
 * that handles the permission logic and displays the TopAppBar and FloatingActionButton. The
 * EventMap composable is used to display the GoogleMap and markers.
 *
 * @param navigationAction the navigation action to use to navigate back
 * @param eventViewModel the EventViewModel to get the events
 * @param userViewModel the UserViewModel to get the user and saved events
 * @param mapViewModel the MapViewModel to get the center and user location
 */
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
  val userLocation by mapViewModel.userLocation.collectAsState()
  var isMyLocationEnabled by remember { mutableStateOf(false) }
  var showApproximateCircle by remember { mutableStateOf(false) }
  val scope = rememberCoroutineScope()

  val permissionLauncher =
      rememberLauncherForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) {
          permissions ->
        when {
          permissions.getOrDefault(Manifest.permission.ACCESS_FINE_LOCATION, false) -> {
            mapViewModel.startLocationUpdates(context)
            isMyLocationEnabled = true
            showApproximateCircle = false
          }
          permissions.getOrDefault(Manifest.permission.ACCESS_COARSE_LOCATION, false) -> {
            mapViewModel.startLocationUpdates(context)
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
            Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION))
  }

  // Check what permissions are already granted
  LaunchedEffect(Unit) {
    when (PackageManager.PERMISSION_GRANTED) {
      ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) -> {
        isMyLocationEnabled = true
        showApproximateCircle = false
        mapViewModel.startLocationUpdates(context)
      }
      ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) -> {
        isMyLocationEnabled = false
        showApproximateCircle = true
        requestPermissions()
        mapViewModel.startLocationUpdates(context)
      }
      else -> {
        requestPermissions()
      }
    }
  }

  /** Stop location updates when the screen is disposed. */
  DisposableEffect(Unit) { onDispose { mapViewModel.stopLocationUpdates() } }

  Scaffold(
      modifier = Modifier.testTag(MapTestTags.SCREEN),
      topBar = {
        TopAppBar(
            title = {
              Text(
                  context.getString(R.string.map_event_title),
                  modifier = Modifier.testTag(MapTestTags.TITLE))
            },
            navigationIcon = {
              IconButton(
                  onClick = {
                    mapViewModel.clearHighlightedEvent()
                    navigationAction.goBack()
                  },
                  modifier = Modifier.testTag(MapTestTags.GO_BACK_BUTTON)) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Outlined.ArrowBack,
                        contentDescription = context.getString(R.string.map_event_go_back_button))
                  }
            })
      },
      floatingActionButton = {
        FloatingActionButton(
            modifier = Modifier.padding(bottom = 80.dp).testTag(MapTestTags.CENTER_ON_USER_FAB),
            onClick = {
              userLocation?.let {
                scope.launch {
                  cameraPositionState.animate(
                      CameraUpdateFactory.newLatLngZoom(it, INITIAL_ZOOM_LEVEL))
                }
              }
            }) {
              Icon(
                  Icons.Default.MyLocation,
                  contentDescription =
                      context.getString(R.string.map_content_description_center_on_user))
            }
      }) { pd ->
        EventMap(
            pd,
            eventViewModel,
            userViewModel,
            cameraPositionState,
            isMyLocationEnabled,
            showApproximateCircle,
            userLocation,
            mapViewModel)
      }
}

/**
 * The EventMap composable displays the GoogleMap with markers for events. It also displays the
 * user's approximate location if only coarse location is available.
 *
 * @param pd the padding values to apply to the GoogleMap
 * @param eventViewModel the EventViewModel to get the events
 * @param userViewModel the UserViewModel to get the user and saved events
 * @param cameraPositionState the CameraPositionState to use for the GoogleMap
 * @param isMyLocationEnabled whether the user's location is enabled
 * @param showApproximateCircle whether to show the approximate circle
 * @param userLocation the user's location
 */
@Composable
fun EventMap(
    pd: PaddingValues,
    eventViewModel: EventViewModel,
    userViewModel: UserViewModel,
    cameraPositionState: CameraPositionState,
    isMyLocationEnabled: Boolean,
    showApproximateCircle: Boolean,
    userLocation: LatLng?,
    mapViewModel: MapViewModel
) {
  val user by userViewModel.user.collectAsState()
  val savedEvents by user!!.savedEvents.list.collectAsState()
  val events by eventViewModel.events.collectAsState()
  val markerStates = remember { mutableMapOf<String, MarkerState>() }
  LaunchedEffect(user) { user?.savedEvents?.requestAll() }

  GoogleMap(
      modifier = Modifier.padding(pd).testTag(MapTestTags.GOOGLE_MAPS),
      cameraPositionState = cameraPositionState,
      properties = MapProperties(isMyLocationEnabled = isMyLocationEnabled),
      uiSettings = MapUiSettings(myLocationButtonEnabled = false)) {
        // Display the user's approximate location if only coarse location is available
        if (showApproximateCircle && userLocation != null) {
          Circle(
              center = userLocation,
              radius = APPROXIMATE_CIRCLE_RADIUS,
              fillColor = mapUserLocationCircleFiller,
              strokeColor = mapUserLocationCircleStroke,
              strokeWidth = APPROXIMATE_CIRCLE_OUTLINE_WIDTH,
              tag = MapTestTags.LOCATION_APPROXIMATE_CIRCLE)
        }

        // Display saved events
        savedEvents.forEach { event ->
          if (event.startDate.toDate() > Calendar.getInstance().time) {
            DisplayEventMarker(event, R.drawable.favorite_pinpoint) { uid, markerState ->
              markerStates[uid] = markerState
            }
          }
        }

        // Display all events (should refactor to the ones that are soon to happen when more events
        // are added)
        events
            .filterNot { event -> savedEvents.any { it.uid == event.uid } }
            .forEach { event ->
              if (event.startDate.toDate() > Calendar.getInstance().time) {
                DisplayEventMarker(event, null) { uid, markerState ->
                  markerStates[uid] = markerState
                }
              }
            }
      }

  val centerLocation by mapViewModel.centerLocation.collectAsState()
  val highlightedEventUid by mapViewModel.highlightedEventUid.collectAsState()
  var initialCentered = false
  val scope = rememberCoroutineScope()

  // Center the map on the user's location or the center location.
  LaunchedEffect(userLocation, centerLocation, highlightedEventUid) {
    if (!initialCentered) {
      if (centerLocation != null) {
        scope.launch {
          cameraPositionState.animate(
              CameraUpdateFactory.newLatLngZoom(centerLocation!!, INITIAL_ZOOM_LEVEL))

          // Highlight the event with the given uid if the map is opened from and event details'
          // page.
          highlightedEventUid?.let { markerStates[it]?.showInfoWindow() }
        }
      } else if (userLocation != null) {
        scope.launch {
          cameraPositionState.animate(
              CameraUpdateFactory.newLatLngZoom(userLocation, INITIAL_ZOOM_LEVEL))
        }
      } else {
        scope.launch {
          cameraPositionState.animate(
              CameraUpdateFactory.newLatLngZoom(EPFL_COORDINATES, INITIAL_ZOOM_LEVEL))
        }
      }
      initialCentered = true
    }
  }
}

/**
 * Display an event marker on the map.
 *
 * @param event the event to display
 * @param customIconResId the resource id of the custom icon to use for the marker
 * @param onMarkerCreated the callback to call when the marker is created, defined to allow passing
 *   the marker state to the caller
 * @return a marker for the event
 */
@Composable
fun DisplayEventMarker(
    event: Event,
    customIconResId: Int?,
    onMarkerCreated: (String, MarkerState) -> Unit
) {
  val timer = timeUntilEvent(event.startDate)
  event.location.let { location ->
    val pinPointIcon =
        if (customIconResId != null) {
          val bitmap = BitmapFactory.decodeResource(LocalContext.current.resources, customIconResId)
          val scaledBitmap = Bitmap.createScaledBitmap(bitmap, 96, 96, false)
          BitmapDescriptorFactory.fromBitmap(scaledBitmap)
        } else {
          BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE)
        }

    val markerState = MarkerState(position = LatLng(location.latitude, location.longitude))
    onMarkerCreated(event.uid, markerState)

    Marker(
        contentDescription = "Event: ${event.title}",
        state = markerState,
        title = event.title,
        snippet = "$timer - ${event.description}",
        icon = pinPointIcon)
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
