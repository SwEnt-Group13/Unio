package com.android.unio.ui.map

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
import androidx.compose.ui.platform.testTag
import androidx.lifecycle.viewmodel.compose.viewModel
import com.android.unio.R
import com.android.unio.model.event.EventListViewModel
import com.android.unio.resources.ResourceManager.getString
import com.android.unio.ui.navigation.NavigationAction
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.MarkerState
import com.google.maps.android.compose.rememberCameraPositionState

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
fun EventMap(
    pd: PaddingValues,
    eventListViewModel: EventListViewModel
) {
  val cameraPositionState = rememberCameraPositionState {
    position = CameraPosition.fromLatLngZoom(LatLng(46.518831258, 6.559331096), 10f)
  }

  val events = eventListViewModel.events.collectAsState()

  GoogleMap(
      modifier = Modifier.padding(pd).testTag("googleMaps"),
      cameraPositionState = cameraPositionState) {
        events.value.forEach { event ->
          event.location.let {
            Marker(
                state = MarkerState(position = LatLng(it.latitude, it.longitude)),
                title = event.title,
                snippet = event.description)
          }
        }
      }
}
