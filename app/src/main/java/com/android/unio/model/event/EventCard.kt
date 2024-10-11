package com.android.unio.model.event

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.android.unio.R

@Composable
fun EventItem(event: Event, onClick: () -> Unit) {
    val backgroundColor = Color(0xFF2596BE)
    val backgroundImage = painterResource(id = R.drawable.photo_2024_10_08_14_57_48)

    Box(
        modifier =
        Modifier.fillMaxWidth()
            .padding(vertical = 8.dp)
            .clickable(onClick = onClick)
            .testTag("event_EventListItem")
            .clip(RoundedCornerShape(10.dp))
            .background(Color.Transparent)) {
        // Background Image
        Image(
            painter = DynamicImage(event.image),
            contentDescription = null,
            modifier =
            Modifier.matchParentSize() // Ensure the image takes up the full size of the Box
                .clip(RoundedCornerShape(10.dp)) // Apply the same shape as the box
                .testTag("event_EventImage"),
            contentScale = ContentScale.Crop // Crop the image to fit
        )

        Column(modifier = Modifier.padding(16.dp)) {
            Spacer(modifier = Modifier.height(8.dp))

            // Event Title
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier =
                    Modifier.clip(RoundedCornerShape(4.dp))
                        .background(addAlphaToColor(Color.Black, 120))) {
                    Text(
                        modifier =
                        Modifier.padding(vertical = 1.dp, horizontal = 4.dp)
                            .testTag("event_EventTitle"),
                        text = event.title,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color.White)
                }

                Spacer(modifier = Modifier.width(6.dp))
                Box(
                    modifier =
                    Modifier.clip(RoundedCornerShape(4.dp))
                        .background(addAlphaToColor(event.types.get(0).color, 200))) {
                    Text(
                        text = event.types.get(0).text,
                        modifier =
                        Modifier.padding(horizontal = 4.dp, vertical = 4.dp)
                            .testTag("event_EventMainType"),
                        color = Color.White,
                        style = TextStyle(fontSize = 8.sp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            Box(
                modifier =
                Modifier.clip(RoundedCornerShape(4.dp))
                    .background(addAlphaToColor(Color.Black, 120))) {
                Text(
                    text = event.catchyDescription,
                    style = TextStyle(fontSize = 10.sp),
                    color = Color.White,
                    modifier = Modifier.padding(vertical = 2.dp).testTag("event_CatchyDescription"))
            }

            Spacer(modifier = Modifier.height(8.dp))
        }
    }
}