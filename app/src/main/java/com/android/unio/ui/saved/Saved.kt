package com.android.unio.ui.saved

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Map
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.android.unio.model.event.EventListViewModel
import com.android.unio.model.user.UserViewModel
import com.android.unio.ui.event.EventCard
import com.android.unio.ui.navigation.BottomNavigationMenu
import com.android.unio.ui.navigation.LIST_TOP_LEVEL_DESTINATION
import com.android.unio.ui.navigation.NavigationAction
import com.android.unio.ui.navigation.Route
import com.android.unio.ui.theme.AppTypography
import com.android.unio.ui.theme.primaryContainerLight
import com.google.firebase.Timestamp

@Composable
fun SavedScreen(
    navigationAction: NavigationAction,
    eventListViewModel: EventListViewModel = viewModel(factory = EventListViewModel.Factory),
    userViewModel: UserViewModel = viewModel(factory = UserViewModel.Factory)
) {
  val events by eventListViewModel.events.collectAsState()
  val coroutineScope = rememberCoroutineScope()

  // Filter events based on whether they are saved for the current user
  val savedEventsForUser =
      events.filter { event -> userViewModel.isEventSavedForCurrentUser(event.uid) }

  val today = Timestamp.now()
  val todayEvents = savedEventsForUser.filter { it.date == today }
  val upcomingEvents = savedEventsForUser.filter { it.date > today }

  var textWidth by remember { mutableStateOf(0.dp) }
  val density = LocalDensity.current

  Scaffold(
      floatingActionButton = {
        FloatingActionButton(onClick = {}, modifier = Modifier.testTag("event_MapButton")) {
          Icon(imageVector = Icons.Filled.Map, contentDescription = "Map")
        }
      },
      bottomBar = {
        BottomNavigationMenu(
            { navigationAction.navigateTo(it.route) }, LIST_TOP_LEVEL_DESTINATION, Route.SAVED)
      },
      modifier = Modifier.testTag("HomeScreen"),
      content = { paddingValues ->
        Column(modifier = Modifier.fillMaxSize().padding(paddingValues).background(Color.White)) {
          // Sticky Header
          Box(
              modifier =
                  Modifier.fillMaxWidth()
                      .background(Color.White)
                      .padding(vertical = 16.dp, horizontal = 16.dp)
                      .testTag("event_Header")) {
                Text(
                    text = "Saved Events",
                    fontWeight = FontWeight.Bold,
                    color = Color.Black,
                    style = TextStyle(fontSize = 24.sp))
                Spacer(modifier = Modifier.height(8.dp))
              }

          LazyColumn(
              contentPadding = PaddingValues(vertical = 2.dp),
              modifier = Modifier.fillMaxSize().padding(horizontal = 32.dp)) {
                if (todayEvents.isNotEmpty()) {
                  item {
                    Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                      Text(
                          text = "Today",
                          fontWeight = FontWeight.Bold,
                          color = Color.Gray,
                          style = AppTypography.bodyMedium,
                          modifier =
                              Modifier.onGloballyPositioned { coordinates ->
                                    textWidth = with(density) { coordinates.size.width.toDp() }
                                  }
                                  .padding(vertical = 8.dp),
                          textAlign = TextAlign.Center)
                    }
                  }
                  item {
                    Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                      Spacer(
                          modifier =
                              Modifier.width(textWidth + 40.dp)
                                  .height(1.dp)
                                  .background(primaryContainerLight))
                    }
                  }
                  item { Spacer(modifier = Modifier.height(10.dp)) }

                  items(todayEvents) { event ->
                    EventCard(event = event, userViewModel = userViewModel)
                  }
                }

                if (upcomingEvents.isNotEmpty()) {
                  item {
                    Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                      Text(
                          text = "Upcoming Events",
                          fontWeight = FontWeight.Bold,
                          color = Color.Gray,
                          style = AppTypography.bodyMedium,
                          modifier =
                              Modifier.onGloballyPositioned { coordinates ->
                                    textWidth = with(density) { coordinates.size.width.toDp() }
                                  }
                                  .padding(vertical = 8.dp),
                          textAlign = TextAlign.Center)
                    }
                  }
                  item {
                    Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                      Spacer(
                          modifier =
                              Modifier.width(textWidth + 40.dp)
                                  .height(1.dp)
                                  .background(primaryContainerLight))
                    }
                  }

                  item { Spacer(modifier = Modifier.height(10.dp)) }

                  items(upcomingEvents) { event ->
                    EventCard(event = event, userViewModel = userViewModel)
                  }
                }

                // Show "No events available" message if both lists are empty
                if (todayEvents.isEmpty() && upcomingEvents.isEmpty()) {
                  item {
                    Box(
                        modifier = Modifier.fillMaxSize().padding(paddingValues),
                        contentAlignment = Alignment.Center) {
                          Text(
                              modifier = Modifier.testTag("event_emptyEventPrompt"),
                              text = "No events available.",
                              color = Color.Gray)
                        }
                  }
                }
              }
        }
      })
}
