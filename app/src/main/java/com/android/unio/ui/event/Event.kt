package com.android.unio.ui.event

import android.annotation.SuppressLint
import android.util.Log
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.lifecycle.viewmodel.compose.viewModel
import com.android.unio.model.event.EventListViewModel
import com.android.unio.ui.association.AssociationProfileScaffold
import com.android.unio.ui.navigation.NavigationAction

private val DEBUG_MESSAGE = "<DEBUG> Not implemented yet"

@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@Composable
fun EventScreen(
  navigationAction: NavigationAction,
  eventId: String,
  eventListViewModel: EventListViewModel = viewModel(factory = EventListViewModel.Factory)
) {

  val event = eventListViewModel.getEventById(eventId, {}, {})
    ?: run {
      Log.e("AssociationProfile", "Association not found")
      return AssociationProfileScaffold(
        title = "<Association Profile>", navigationAction = navigationAction
      ) { padding ->
        Column(modifier = Modifier.padding(padding)) {
          Text(
            text = "Association not found. Shouldn't happen.",
            modifier = Modifier.testTag("associationNotFound"),
            color = Color.Red
          )
        }
      }
    }

  Scaffold(modifier = Modifier.testTag("EventScreen")) { Text("Event screen") }
}
