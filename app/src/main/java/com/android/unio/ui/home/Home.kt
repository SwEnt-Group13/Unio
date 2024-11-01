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
import androidx.compose.material.icons.filled.Place
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.android.unio.model.event.Event
import com.android.unio.model.event.EventCard
import com.android.unio.model.event.EventListViewModel
import com.android.unio.model.user.UserViewModel
import com.android.unio.ui.event.EventCard
import com.android.unio.ui.navigation.BottomNavigationMenu
import com.android.unio.ui.navigation.LIST_TOP_LEVEL_DESTINATION
import com.android.unio.ui.navigation.NavigationAction
import com.android.unio.ui.navigation.Route
import com.android.unio.ui.navigation.Screen
import com.android.unio.ui.theme.AppTypography
import kotlinx.coroutines.launch

@Composable
fun HomeScreen(
    navigationAction: NavigationAction,
    eventListViewModel: EventListViewModel = viewModel(factory = EventListViewModel.Factory),
    userViewModel: UserViewModel = viewModel(factory = UserViewModel.Factory)
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
        FloatingActionButton(
            onClick = { navigationAction.navigateTo(Screen.MAP) },
            modifier = Modifier.testTag("event_MapButton")) {
              Icon(imageVector = Icons.Filled.Place, contentDescription = "Map button")
            }
      },
      bottomBar = {
        BottomNavigationMenu(
            { navigationAction.navigateTo(it.route) }, LIST_TOP_LEVEL_DESTINATION, Route.HOME)
      },
      modifier = Modifier.testTag("HomeScreen"),
      content = { paddingValues ->
        Column(modifier = Modifier.fillMaxSize().padding(paddingValues)) {
          // Sticky Header
          Box(
              modifier =
                  Modifier.fillMaxWidth()
                      .padding(vertical = 16.dp, horizontal = horizontalHeaderPadding)
                      .testTag("event_Header")) {
                Column {
                  Text(text = "Upcoming Events", style = AppTypography.headlineMedium)
                  Spacer(modifier = Modifier.height(8.dp))

                  Row(
                      modifier = Modifier.fillMaxWidth(),
                      horizontalArrangement = Arrangement.SpaceBetween) {
                        Text(
                            text = "All",
                            color =
                                if (selectedTab == "All") MaterialTheme.colorScheme.primary
                                else MaterialTheme.colorScheme.inversePrimary,
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
                            color =
                                if (selectedTab == "Following") MaterialTheme.colorScheme.primary
                                else MaterialTheme.colorScheme.inversePrimary,
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
                  items(events) { event -> EventCard(event = event, userViewModel = userViewModel) }
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
