package com.android.unio.ui.events

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInRoot
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.unit.times
import androidx.lifecycle.viewmodel.compose.viewModel
import com.android.unio.model.events.Event
import com.android.unio.model.events.EventListViewModel
import kotlinx.coroutines.launch

@Composable
fun EventListOverview(
    eventListViewModel: EventListViewModel = viewModel(factory = EventListViewModel.Factory),
    onAddEvent: () -> Unit,
    onEventClick: (Event) -> Unit
) {
    val events by eventListViewModel.events.collectAsState()
    var selectedTab by remember { mutableStateOf("All") }
    val density = LocalDensity.current.density

    // Define the position for the underline to slide smoothly
    val coroutineScope = rememberCoroutineScope()
    val animatablePosition = remember { Animatable(0f) } // This holds the animated position

    // The width of each tab text
    var allTabWidth by remember { mutableStateOf(0.dp) }
    var followingTabWidth by remember { mutableStateOf(0.dp) }
    var allTabXCoordinate by remember { mutableStateOf(0f) }
    var followingTabXCoordinate by remember { mutableStateOf(0f) }

    val horizontalHeaderPadding = 16.dp

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(
                onClick = onAddEvent,
                modifier = Modifier.testTag("createEventFab")
            ) {
                Icon(imageVector = Icons.Filled.Add, contentDescription = "Add Event")
            }
        },
        content = { paddingValues ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .background(Color.Black)
            ) {
                // Sticky Header
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color.Black)
                        .padding(vertical = 16.dp, horizontal = horizontalHeaderPadding)
                ) {
                    Column {
                        Text(
                            text = "Upcoming Events",
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            style = TextStyle(
                                fontSize = 24.sp // Set the font size to 24 sp (scale-independent pixels)
                            )
                        )
                        Spacer(modifier = Modifier.height(8.dp))

                        // Row with clickable tabs
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween // Aligns the tabs evenly
                        ) {
                            // Clickable text for "All"
                            Text(
                                text = "All",
                                color = if (selectedTab == "All") Color.White else Color.Gray,
                                modifier = Modifier
                                    .clickable {
                                        selectedTab = "All"
                                        coroutineScope.launch {
                                            animatablePosition.animateTo(
                                                0f, // Starting point for "All"
                                                animationSpec = tween(durationMillis = 1000) // Animation duration
                                            )
                                        }
                                    }
                                    .padding(horizontal = 16.dp)
                                    .onGloballyPositioned { coordinates ->
                                        allTabWidth = (coordinates.size.width / density).dp
                                        allTabXCoordinate = coordinates.positionInRoot().x
                                    }
                                    .testTag("tabAll")
                            )

                            // Clickable text for "Following"
                            Text(
                                text = "Following",
                                color = if (selectedTab == "Following") Color.White else Color.Gray,
                                modifier = Modifier
                                    .clickable {
                                        selectedTab = "Following"
                                        coroutineScope.launch {
                                            animatablePosition.animateTo(
                                                1f, // Ending point for "Following"
                                                animationSpec = tween(durationMillis = 1000) // Animation duration
                                            )
                                        }
                                    }
                                    .padding(horizontal = 16.dp)
                                    .onGloballyPositioned { coordinates ->
                                        followingTabWidth = (coordinates.size.width / density).dp
                                        followingTabXCoordinate = coordinates.positionInRoot().x
                                    }
                                    .testTag("tabFollowing")
                            )
                        }

                        // Underline to indicate selected tab with smooth sliding animation
                        Box(
                            modifier = Modifier
                                .fillMaxWidth() // Makes sure the underline spans the entire width
                                .padding(top = 4.dp)
                        ) {
                            val selectedTabWidth = if (selectedTab == "All") allTabWidth else followingTabWidth
                            val selectedTabXCoordinate = if (selectedTab == "All") allTabXCoordinate else followingTabXCoordinate
                            Box(
                                modifier = Modifier
                                    //.offset(x = animatablePosition.value * (followingTabWidth - allTabWidth) + allTabWidth)
                                    .offset(x = ((animatablePosition.value *(followingTabXCoordinate-allTabXCoordinate) + allTabXCoordinate) / density).dp - horizontalHeaderPadding)
                                    .width(selectedTabWidth) // Use the width of the selected tab
                                    .height(2.dp)
                                    .background(Color.Blue)
                            )
                        }
                    }
                }

                // Event List
                if (events.isNotEmpty()) {
                    LazyColumn(
                        contentPadding = PaddingValues(vertical = 8.dp),
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 16.dp)
                    ) {
                        items(events) { event ->
                            EventItem(event = event, onClick = { onEventClick(event) })
                        }
                    }
                } else {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(paddingValues),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            modifier = Modifier.testTag("emptyEventPrompt"),
                            text = "No events available.",
                            color = Color.White
                        )
                    }
                }
            }
        }
    )
}

@Composable
fun EventItem(event: Event, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .clickable(onClick = onClick)
            .testTag("eventListItem")
            .clip(RoundedCornerShape(10.dp))
            .background(brush = Brush.horizontalGradient(
                colors = listOf(Color.Blue, Color.Magenta) // Smooth gradient from blue to magenta
            )),

        ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Date and Status Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = event.date,
                    style = MaterialTheme.typography.bodySmall
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Event Title
            Text(
                text = event.title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )

            // Event Description
            Text(
                text = event.description,
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.padding(top = 4.dp)
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Event Location
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Location: ",
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = event.location,
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }
    }
}

// Extension function to convert pixel size to dp

