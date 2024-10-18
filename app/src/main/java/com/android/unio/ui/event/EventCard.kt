package com.android.unio.ui.event

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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.FavoriteBorder
import androidx.compose.material3.Icon
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
import androidx.core.net.toUri
import coil.compose.AsyncImage
import com.android.unio.R
import com.android.unio.model.event.Event
import com.android.unio.ui.theme.primaryContainerLight
import com.android.unio.ui.theme.secondaryDark
import com.android.unio.utils.EventUtils.DynamicImage
import com.android.unio.utils.EventUtils.addAlphaToColor
import com.android.unio.utils.EventUtils.formatTimestamp
import java.text.SimpleDateFormat
import java.util.Locale

@Composable
fun EventCard(event: Event, onClick: () -> Unit) {

  Column(
      modifier =
          Modifier.fillMaxWidth()
              .padding(vertical = 8.dp)
              .clickable(onClick = onClick)
              .testTag("event_EventListItem")
              .clip(RoundedCornerShape(10.dp))
              .background(Color(0xFFF0ECF4))) {
        Image(
            painter = DynamicImage(event.image),
            contentDescription = null,
            modifier =
                Modifier.fillMaxWidth()
                    .height(100.dp)
                    .clip(RoundedCornerShape(topStart = 10.dp, topEnd = 10.dp))
                    .testTag("event_EventImage"),
            contentScale = ContentScale.Crop // crop the image to fit
            )

          Box(
              modifier =
                  Modifier.size(28.dp)
                      .clip(RoundedCornerShape(14.dp))
                      .background(Color(0xFF6200EE))
                      .align(Alignment.TopEnd)
                      .padding(4.dp)) {

                // Icon inside the circle
                Icon(Icons.Rounded.FavoriteBorder, contentDescription = "")
              }
        }

        Column(modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)) {
          Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
              Text(
                  modifier =
                      Modifier.padding(vertical = 1.dp, horizontal = 4.dp)
                          .testTag("event_EventTitle")
                          .wrapContentWidth(), // Make sure the text only takes as much space as
                  // needed
                  text = event.title,
                  style = MaterialTheme.typography.labelLarge,
                  fontWeight = FontWeight.Bold,
                  color = Color.Black)

              Spacer(modifier = Modifier.width(6.dp))
              val type: EventType =
                  if (event.types.isEmpty()) {
                    EventType.OTHER
                  } else event.types[0]
              Box(
                  modifier =
                      Modifier.clip(RoundedCornerShape(4.dp))
                          .background(addAlphaToColor(type.color, 200))
                          .wrapContentWidth()) {
                    Text(
                        text = type.text,
                        modifier =
                            Modifier.padding(horizontal = 4.dp, vertical = 4.dp)
                                .testTag("event_EventMainType"),
                        color = Color.Black,
                        style = TextStyle(fontSize = 8.sp))
                  }
            }
            Spacer(modifier = Modifier.width(6.dp))

            Image(
                painter = painterResource(id = R.drawable.clic),
                contentDescription = null,
                modifier =
                    Modifier.size(24.dp)
                        .align(Alignment.CenterVertically)
                        .clip(RoundedCornerShape(5.dp))
                        .testTag("event_ClicImage"))
          }

          Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
              Text(
                  modifier =
                      Modifier.padding(vertical = 1.dp, horizontal = 4.dp)
                          .testTag("event_EventLocation")
                          .wrapContentWidth(), // Make sure the text only takes as much space as
                  // needed
                  text = event.location.name,
                  style = MaterialTheme.typography.bodySmall,
                  color = Color.Black)
            }

            Spacer(modifier = Modifier.width(1.dp))

            Text(
                modifier =
                    Modifier.padding(vertical = 1.dp, horizontal = 4.dp)
                        .testTag("event_EventDate")
                        .wrapContentWidth(),
                text = formatTimestamp(event.date, SimpleDateFormat("MM/dd", Locale.getDefault())),
                style = MaterialTheme.typography.titleMedium,
                color = Color.Black)

            Spacer(modifier = Modifier.width(2.dp))

            Spacer(modifier = Modifier.height(10.dp).width(1.dp).background(primaryContainerLight))

            Spacer(modifier = Modifier.width(2.dp))

            Text(
                modifier = Modifier.testTag("event_EventTime").wrapContentWidth(),
                text = formatTimestamp(event.date, SimpleDateFormat("HH:mm", Locale.getDefault())),
                style = MaterialTheme.typography.bodySmall,
                color = Color.Black)
          }
          Row {
            Spacer(modifier = Modifier.width(4.dp))
            Spacer(
                modifier = Modifier.fillMaxWidth().height(1.dp).background(primaryContainerLight))
          }

          Text(
              modifier =
                  Modifier.padding(vertical = 1.dp, horizontal = 4.dp)
                      .testTag("event_EventCatchyDescription")
                      .wrapContentWidth(),
              text = event.catchyDescription,
              style = MaterialTheme.typography.bodySmall,
              color = Color.Black)
        }
      }
}
