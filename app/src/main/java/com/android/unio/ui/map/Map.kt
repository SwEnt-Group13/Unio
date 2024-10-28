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
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.lifecycle.viewmodel.compose.viewModel
import com.android.unio.R
import com.android.unio.model.association.Association
import com.android.unio.model.event.Event
import com.android.unio.model.event.EventListViewModel
import com.android.unio.model.firestore.emptyFirestoreReferenceList
import com.android.unio.model.map.Location
import com.android.unio.resources.ResourceManager.getString
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MapScreen(
    navigationAction: NavigationAction,
    eventListViewModel: EventListViewModel = viewModel(factory = EventListViewModel.Factory)
) {
  Scaffold(
      modifier = Modifier.testTag("MapScreen"),
      topBar = {
        TopAppBar(
            title = { Text("Event map", modifier = Modifier.testTag("MapTitle")) },
            navigationIcon = {
              IconButton(
                  onClick = { navigationAction.goBack() },
                  modifier = Modifier.testTag("goBackButton")) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Outlined.ArrowBack,
                        contentDescription = getString(R.string.association_go_back))
                  }
            })
      }) { pd ->
        EventMap(pd, eventListViewModel)
      }
}

@Composable
fun EventMap(pd: PaddingValues, eventListViewModel: EventListViewModel) {
  val cameraPositionState = rememberCameraPositionState {
    position = CameraPosition.fromLatLngZoom(LatLng(46.518831258, 6.559331096), 10f)
  }

  val events = eventListViewModel.events.collectAsState()

  /** Mock arbitrary saved event for all users. */
  val arbitrarySavedEvents: List<Event> =
      listOf(
          Event(
              uid = "123456789",
              title = "Arbitrary Saved Event",
              organisers = Association.emptyFirestoreReferenceList(),
              taggedAssociations = Association.emptyFirestoreReferenceList(),
              description = "This is the description of an arbitrary saved event.",
              catchyDescription = "This is an arbitrary saved event.",
              date =
                  Timestamp(
                      Calendar.getInstance()
                          .apply { add(Calendar.DAY_OF_YEAR, 1) }
                          .time), // tomorrow's date
              location =
                  Location(
                      latitude = 46.51848436506024, longitude = 6.568259761045008), // Rolex Center
          ))

  GoogleMap(
      modifier = Modifier.padding(pd).testTag("googleMaps"),
      cameraPositionState = cameraPositionState) {
        // Display saved events
        arbitrarySavedEvents.forEach { event ->
          event.location.let {
            val bitmap =
                BitmapFactory.decodeResource(LocalContext.current.resources, R.drawable.favorite_pinpoint)
            val scaledBitmap = Bitmap.createScaledBitmap(bitmap, 64, 64, false)
            val customIcon = BitmapDescriptorFactory.fromBitmap(scaledBitmap)
            Marker(
                state = MarkerState(position = LatLng(it.latitude, it.longitude)),
                title = event.title,
                snippet = event.description,
                icon = customIcon)
          }
        }

        // Display all events (should refactor to the ones that are soon to happen when more events
        // are added)
        events.value.forEach { event ->
          event.location.let {
            Marker(
                state = MarkerState(position = LatLng(it.latitude, it.longitude)),
                title = event.title,
                snippet = event.description,
                icon = BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE))
          }
        }
      }
}
