package com.android.unio.ui.home

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
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
import androidx.compose.ui.layout.positionInRoot
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import com.android.unio.model.event.Event
import com.android.unio.ui.event.EventCard
import com.android.unio.model.event.EventListViewModel
import com.android.unio.model.event.EventRepository
import com.android.unio.model.event.EventRepositoryMock
import com.android.unio.model.event.EventViewModel
import com.android.unio.model.event.MockEventRepository
import com.android.unio.model.event.PreviewEventViewModel
import com.android.unio.model.user.MockUserRepository
import com.android.unio.ui.navigation.BottomNavigationMenu
import com.android.unio.ui.navigation.LIST_TOP_LEVEL_DESTINATION
import com.android.unio.ui.navigation.NavigationAction
import com.android.unio.ui.navigation.Route
import kotlinx.coroutines.launch

@Preview(showBackground = true)
@Composable
fun EventListOverviewPreview() {

  val mockEventRepository = EventRepositoryMock()

  val eventListViewModel = EventListViewModel(mockEventRepository as EventRepository)

  HomeScreen(
      NavigationAction(NavHostController(LocalContext.current)),
      eventListViewModel = eventListViewModel,
      onAddEvent = {},
      onEventClick = {},
      eventViewModel = viewModel(factory = EventViewModel.Factory))
}

@Composable
fun HomeScreen(
    navigationAction: NavigationAction,
    eventListViewModel: EventListViewModel = viewModel(factory = EventListViewModel.Factory),
    onAddEvent: () -> Unit,
    onEventClick: (Event) -> Unit,
    eventViewModel: EventViewModel = viewModel(factory = EventViewModel.Factory)
) {
  val events by eventListViewModel.events.collectAsState()
  var selectedTab by remember { mutableStateOf("All") }
  val density = LocalDensity.current.density

  val coroutineScope = rememberCoroutineScope()
  val animatablePosition = remember { Animatable(0f) }

  var allTabWidth by remember { mutableStateOf(0.dp) }
  var followingTabWidth by remember { mutableStateOf(0.dp) }
  var allTabXCoordinate by remember { mutableStateOf(0f) }
  var followingTabXCoordinate by remember { mutableStateOf(0f) }

  val horizontalHeaderPadding = 16.dp

  Scaffold(
      floatingActionButton = {
        FloatingActionButton(onClick = onAddEvent, modifier = Modifier.testTag("event_MapButton")) {
          Icon(imageVector = Icons.Filled.Add, contentDescription = "Add Event")
        }
      },
      bottomBar = {
        BottomNavigationMenu(
            { navigationAction.navigateTo(it.route) }, LIST_TOP_LEVEL_DESTINATION, Route.HOME)
      },
      modifier = Modifier.testTag("HomeScreen"),
      content = { paddingValues ->
        Column(modifier = Modifier.fillMaxSize().padding(paddingValues).background(Color.Black)) {
          // Sticky Header
          Box(
              modifier =
                  Modifier.fillMaxWidth()
                      .background(Color.Black)
                      .padding(vertical = 16.dp, horizontal = horizontalHeaderPadding)
                      .testTag("event_Header")) {
                Column {
                  Text(
                      text = "Upcoming Events",
                      fontWeight = FontWeight.Bold,
                      color = Color.White,
                      style = TextStyle(fontSize = 24.sp))
                  Spacer(modifier = Modifier.height(8.dp))

                  Row(
                      modifier = Modifier.fillMaxWidth(),
                      horizontalArrangement = Arrangement.SpaceBetween) {
                        Text(
                            text = "All",
                            color = if (selectedTab == "All") Color.White else Color.Gray,
                            modifier =
                                Modifier.clickable {
                                      selectedTab = "All"
                                      coroutineScope.launch {
                                        animatablePosition.animateTo(
                                            0f, // Starting point for "All"
                                            animationSpec =
                                                tween(durationMillis = 1000) // Animation duration
                                            )
                                      }
                                    }
                                    .padding(horizontal = 16.dp)
                                    .onGloballyPositioned { coordinates ->
                                      allTabWidth = (coordinates.size.width / density).dp
                                      allTabXCoordinate = coordinates.positionInRoot().x
                                    }
                                    .testTag("event_tabAll"))

                        // Clickable text for "Following"
                        Text(
                            text = "Following",
                            color = if (selectedTab == "Following") Color.White else Color.Gray,
                            modifier =
                                Modifier.clickable {
                                      selectedTab = "Following"
                                      coroutineScope.launch {
                                        animatablePosition.animateTo(
                                            1f, // Ending point for "Following"
                                            animationSpec =
                                                tween(durationMillis = 1000) // Animation duration
                                            )
                                      }
                                    }
                                    .padding(horizontal = 16.dp)
                                    .onGloballyPositioned { coordinates ->
                                      followingTabWidth = (coordinates.size.width / density).dp
                                      followingTabXCoordinate = coordinates.positionInRoot().x
                                    }
                                    .testTag("event_tabFollowing"))
                      }

                  // Underline to indicate selected tab with smooth sliding animation
                  Box(
                      modifier =
                          Modifier.fillMaxWidth() // Makes sure the underline spans the entire width
                              .padding(top = 4.dp)) {
                        val selectedTabWidth =
                            if (selectedTab == "All") allTabWidth else followingTabWidth
                        Box(
                            modifier =
                                Modifier.offset(
                                        x =
                                            ((animatablePosition.value *
                                                    (followingTabXCoordinate - allTabXCoordinate) +
                                                    allTabXCoordinate) / density)
                                                .dp - horizontalHeaderPadding)
                                    .width(selectedTabWidth) // Use the width of the selected tab
                                    .height(2.dp)
                                    .background(Color.Blue)
                                    .testTag("event_UnderlyingBar"))
                      }
                }
              }

          // Event List
          if (events.isNotEmpty()) {
            LazyColumn(
                contentPadding = PaddingValues(vertical = 8.dp),
                modifier = Modifier.fillMaxSize().padding(horizontal = 32.dp)) {
                  items(events) { event ->
                    EventCard(event = event, viewModel = eventViewModel, onClick = { onEventClick(event) })
                  }
                }
          } else {
            Box(
                modifier = Modifier.fillMaxSize().padding(paddingValues),
                contentAlignment = Alignment.Center) {
                  Text(
                      modifier = Modifier.testTag("event_emptyEventPrompt"),
                      text = "No events available.",
                      color = Color.White)
                }
          }
        }
      })
}
