package com.android.unio.ui.events

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
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
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInRoot
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.android.unio.R
import com.android.unio.model.events.Event
import com.android.unio.model.events.EventListViewModel
import com.android.unio.model.events.EventRepositoryMock
import com.android.unio.model.events.getColorForEventType
import kotlinx.coroutines.launch


@Preview(showBackground = true)
@Composable
fun EventListOverviewPreview() {
    // Create an instance of EventRepositoryMock
    val mockEventRepository = EventRepositoryMock()

    // Create the ViewModel with the mock repository
    val eventListViewModel = EventListViewModel(mockEventRepository)

    // Preview with the ViewModel
    EventListOverview(
        eventListViewModel = eventListViewModel,
        onAddEvent = { /* Handle Add Event Click */ },
        onEventClick = { /* Handle Event Item Click */ }
    )
}


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
                            .padding(horizontal = 32.dp)
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
    val backgroundColor = Color(0xFF2596BE)
    val backgroundImage = painterResource(id = R.drawable.photo_2024_10_08_14_57_48)

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
            .clickable(onClick = onClick)
            .testTag("eventListItem")
            .clip(RoundedCornerShape(10.dp))
            .background(Color.Transparent) // Set background to transparent to see the image
    ) {
        // Background Image
        Image(
            painter = DynamicImage(event.picture),
            contentDescription = null,
            modifier = Modifier
                .matchParentSize() // Ensure the image takes up the full size of the Box
                .clip(RoundedCornerShape(10.dp)), // Apply the same shape as the box
            contentScale = ContentScale.Crop // Crop the image to fit
        )

        /* Overlay gradient (optional, if you want to keep it)
        Box(
            modifier = Modifier
                .matchParentSize()
                .background(brush = Brush.verticalGradient(
                    colors = listOf(backgroundColor, darkenColor(backgroundColor, 0.4f))
                ))
                .alpha(0.6f) // Optional: Add alpha to make the text more readable
        )*/

        Column(modifier = Modifier.padding(16.dp)) {
            Spacer(modifier = Modifier.height(8.dp))

            // Event Title
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(4.dp))
                        .background(addAlphaToColor(Color.Black, 120))
                ){
                    Text(
                        modifier = Modifier
                            .padding(vertical = 1.dp, horizontal = 4.dp),
                        text = event.title,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }

                Spacer(modifier = Modifier.width(6.dp))
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(4.dp))
                        .background(addAlphaToColor(getColorForEventType(event.main_type), 200))
                ) {
                    Text(
                        text = event.main_type,
                        modifier = Modifier.padding(horizontal = 4.dp, vertical = 4.dp),
                        color = Color.White,
                        style = TextStyle(fontSize = 8.sp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(4.dp))
                    .background(addAlphaToColor(Color.Black, 120))
            ){
                Text(
                    text = event.catchy_description,
                    style = TextStyle(fontSize = 10.sp),
                    color = Color.White,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }

            Spacer(modifier = Modifier.height(8.dp))
        }
    }
}

// Extension function to convert pixel size to dp

fun getContrastingColor(backgroundColor: Color): Color {
    // Define a list of candidate colors
    val candidateColors = listOf(
        Color.Black,
        Color.White,
        Color.Red,
        Color.Green,
        Color.Blue,
        Color.Yellow,
        Color.Cyan,
        Color.Magenta,
        Color.Gray
    )

    // Extract RGB components from the Color
    val rBg = backgroundColor.red
    val gBg = backgroundColor.green
    val bBg = backgroundColor.blue

    // Calculate the luminance of the background color
    val luminanceBg = 0.2126 * rBg + 0.7152 * gBg + 0.0722 * bBg

    // Function to calculate the contrast ratio
    fun contrastRatio(color: Color): Double {
        val r = color.red
        val g = color.green
        val b = color.blue

        // Calculate the luminance of the candidate color
        val luminanceColor = 0.2126 * r + 0.7152 * g + 0.0722 * b

        // Calculate the contrast ratio (luminanceColor + 0.05) / (luminanceBg + 0.05)
        return if (luminanceBg < luminanceColor) {
            (luminanceColor + 0.05) / (luminanceBg + 0.05)
        } else {
            (luminanceBg + 0.05) / (luminanceColor + 0.05)
        }
    }

    // Find the color with the highest contrast ratio
    return candidateColors.maxByOrNull { contrastRatio(it) } ?: Color.White // Fallback to white if no candidates
}

fun getContrastingColorBlackAndWhite(backgroundColor: Color): Color {
    // Extract RGB components from the Color
    val r = backgroundColor.red
    val g = backgroundColor.green
    val b = backgroundColor.blue

    // Calculate luminance using the formula
    val luminance = 0.2126 * r + 0.7152 * g + 0.0722 * b

    // Return black if the luminance is high, otherwise return white
    return if (luminance > 0.5) Color.Black else Color.White
}



fun darkenColor(color: Color, factor: Float): Color {
    val r = (color.red * 255).toInt()
    val g = (color.green * 255).toInt()
    val b = (color.blue * 255).toInt()

    // Darken the color by a factor (0.0 to 1.0)
    val darkenedR = (r * (1 - factor)).coerceIn(0f, 255f).toInt()
    val darkenedG = (g * (1 - factor)).coerceIn(0f, 255f).toInt()
    val darkenedB = (b * (1 - factor)).coerceIn(0f, 255f).toInt()

    return Color(darkenedR, darkenedG, darkenedB)
}


fun addAlphaToColor(color: Color, alpha: Int): Color {
    // Extract RGB components
    val red = (color.red * 255).toInt()
    val green = (color.green * 255).toInt()
    val blue = (color.blue * 255).toInt()

    // Return a new Color with the specified alpha
    return Color(red, green, blue, alpha)
}


@Composable
fun DynamicImage(eventImageName: String): Painter {
    val context = LocalContext.current

    // Get the resource ID using the string name
    val resourceId = context.resources.getIdentifier(eventImageName, "drawable", context.packageName)

    // Load the image using painterResource
    val painter = if (resourceId != 0) {
        return painterResource(id = resourceId)
    } else {
        // Fallback to a default image or handle the case when the image doesn't exist
        return painterResource(id = R.drawable.weskic) // Replace with your default image
    }

}
