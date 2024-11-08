package com.android.unio.ui.map

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import com.android.unio.R
import com.android.unio.model.event.Event
import com.android.unio.model.event.EventViewModel
import com.android.unio.model.strings.MapStrings
import com.android.unio.model.user.UserViewModel
import com.android.unio.ui.navigation.NavigationAction
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.firebase.Timestamp
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.MarkerState
import com.google.maps.android.compose.rememberCameraPositionState
import java.util.Calendar
import java.util.concurrent.TimeUnit

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MapScreen(
    navigationAction: NavigationAction,
    eventViewModel: EventViewModel,
    userViewModel: UserViewModel
) {
  val context = LocalContext.current
  Scaffold(
      modifier = Modifier.testTag("MapScreen"),
      topBar = {
        TopAppBar(
            title = {
              Text(
                  context.getString(R.string.map_event_title),
                  modifier = Modifier.testTag("MapTitle"))
            },
            navigationIcon = {
              IconButton(
                  onClick = { navigationAction.goBack() },
                  modifier = Modifier.testTag("goBackButton")) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Outlined.ArrowBack,
                        contentDescription = context.getString(R.string.map_event_go_back_button))
                  }
            })
      }) { pd ->
        EventMap(pd, eventViewModel, userViewModel)
      }
}

@Composable
fun EventMap(pd: PaddingValues, eventViewModel: EventViewModel, userViewModel: UserViewModel) {
  val epflCameraPosition = CameraPosition.fromLatLngZoom(LatLng(46.518831258, 6.559331096), 10f)
  val cameraPositionState = rememberCameraPositionState { position = epflCameraPosition }

  val events = eventViewModel.events.collectAsState()

  val user by userViewModel.user.collectAsState()
  val savedEvents by user!!.savedEvents.list.collectAsState()
  LaunchedEffect(user) {
    if (user != null) {
      user!!.savedEvents.requestAll()
    }
  }

  GoogleMap(
      modifier = Modifier.padding(pd).testTag("googleMaps"),
      cameraPositionState = cameraPositionState) {
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
          val bitmap = BitmapFactory.decodeResource(LocalContext.current.resources, customIconResId)
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
