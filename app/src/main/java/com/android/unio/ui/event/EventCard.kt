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
import androidx.compose.material.icons.rounded.Favorite
import androidx.compose.material.icons.rounded.FavoriteBorder
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.net.toUri
import coil.compose.AsyncImage
import coil.request.CachePolicy
import coil.request.ImageRequest
import com.android.unio.R
import com.android.unio.model.event.Event
import com.android.unio.model.event.EventType
import com.android.unio.model.event.EventViewModel
import com.android.unio.model.user.UserViewModel
import com.android.unio.ui.navigation.NavigationAction
import com.android.unio.ui.navigation.Screen
import com.android.unio.ui.theme.AppTypography
import com.android.unio.utils.EventUtils.addAlphaToColor
import com.android.unio.utils.EventUtils.formatTimestamp
import java.text.SimpleDateFormat
import java.util.Locale

@Composable
fun EventCard(
    navigationAction: NavigationAction,
    event: Event,
    userViewModel: UserViewModel,
    eventViewModel: EventViewModel
) {
  val user by userViewModel.user.collectAsState()
  var isSaved by remember { mutableStateOf(false) }
  LaunchedEffect(event.uid) { isSaved = userViewModel.isEventSavedForCurrentUser(event.uid) }

  val imgUrl = event.image.toUri()
  val placeholderImg = R.drawable.adec
  val imageRequest =
      ImageRequest.Builder(LocalContext.current)
          .data(imgUrl)
          .memoryCacheKey(imgUrl.toString())
          .diskCacheKey(imgUrl.toString())
          .placeholder(placeholderImg)
          .error(placeholderImg)
          .fallback(placeholderImg)
          .diskCachePolicy(CachePolicy.ENABLED)
          .memoryCachePolicy(CachePolicy.ENABLED)
          .crossfade(true)
          .build()

  Column(
      modifier =
          Modifier.fillMaxWidth()
              .padding(vertical = 8.dp)
              .testTag("event_EventListItem")
              .clip(RoundedCornerShape(10.dp))
              .background(MaterialTheme.colorScheme.primaryContainer)
              .clickable {
                eventViewModel.selectEvent(event.uid)
                navigationAction.navigateTo(Screen.EVENT_DETAILS)
              }) {

        // Event image section, displays the main event image or a placeholder if the URL is invalid

        Box(modifier = Modifier.fillMaxWidth().height(100.dp)) {
          AsyncImage(
              model = imageRequest,
              contentDescription = "Image of the event",
              modifier =
                  Modifier.fillMaxWidth()
                      .height(100.dp)
                      .clip(RoundedCornerShape(topStart = 10.dp, topEnd = 10.dp))
                      .testTag("event_EventImage"),
              contentScale = ContentScale.Crop // crop the image to fit
              )

          // Save button icon on the top right corner of the image, allows the user to save/unsave
          // the event

          Box(
              modifier =
                  Modifier.size(28.dp)
                      .clip(RoundedCornerShape(14.dp))
                      .background(MaterialTheme.colorScheme.inversePrimary)
                      .align(Alignment.TopEnd)
                      .clickable {
                        if (user == null) return@clickable
                        if (isSaved) {
                          userViewModel.unSaveEventForCurrentUser(
                              event.uid, onSuccess = { isSaved = false })
                        } else {
                          userViewModel.saveEventForCurrentUser(
                              event.uid, onSuccess = { isSaved = true })
                        }
                        userViewModel.updateUserDebounced(user!!)
                      }
                      .padding(4.dp)) {
                Icon(
                    imageVector =
                        if (isSaved) Icons.Rounded.Favorite else Icons.Rounded.FavoriteBorder,
                    contentDescription = if (isSaved) "Saved" else "Not saved",
                    tint = if (isSaved) Color.Red else Color.White)
              }
        }

        // Event details section, including title, type, location, and time

        Column(modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)) {

          // Row containing event title and type label

          Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
              Text(
                  modifier =
                      Modifier.padding(vertical = 1.dp, horizontal = 4.dp)
                          .testTag("event_EventTitle")
                          .wrapContentWidth(), // Make sure the text only takes as much space as
                  // needed
                  text = event.title,
                  style = AppTypography.labelLarge,
                  color = MaterialTheme.colorScheme.onSurface)

              Spacer(modifier = Modifier.width(6.dp))

              // Display event type (e.g., Music, Sports) with colored background

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
                        color = MaterialTheme.colorScheme.scrim,
                        style = TextStyle(fontSize = 8.sp))
                  }
            }
            Spacer(modifier = Modifier.width(6.dp))

            // Static association logo on the right of the title and type

            Image(
                painter = painterResource(id = R.drawable.clic),
                contentDescription = "Association logo",
                modifier =
                    Modifier.size(24.dp)
                        .align(Alignment.CenterVertically)
                        .clip(RoundedCornerShape(5.dp))
                        .testTag("event_ClicImage"))
          }

          // Row displaying event location and formatted date/time details

          Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
              Text(
                  modifier =
                      Modifier.padding(vertical = 1.dp, horizontal = 4.dp)
                          .testTag("event_EventLocation")
                          .wrapContentWidth(), // Make sure the text only takes as much space as
                  // needed
                  text = event.location.name,
                  style = AppTypography.bodySmall,
                  color = MaterialTheme.colorScheme.onSurface)
            }

            Spacer(modifier = Modifier.width(1.dp))

            // Date display for the event

            Text(
                modifier =
                    Modifier.padding(vertical = 1.dp, horizontal = 0.dp).testTag("event_EventDate"),
                text = formatTimestamp(event.date, SimpleDateFormat("dd/MM", Locale.getDefault())),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface)

            Spacer(modifier = Modifier.width(2.dp))

            Spacer(
                modifier =
                    Modifier.height(10.dp)
                        .width(1.dp)
                        .background(MaterialTheme.colorScheme.primary))

            Spacer(modifier = Modifier.width(2.dp))

            // Time display for the event

            Text(
                modifier = Modifier.testTag("event_EventTime").wrapContentWidth(),
                text = formatTimestamp(event.date, SimpleDateFormat("HH:mm", Locale.getDefault())),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface)
          }

          // Divider line below location and time row

          Row {
            Spacer(modifier = Modifier.width(4.dp))
            Spacer(
                modifier =
                    Modifier.fillMaxWidth()
                        .height(1.dp)
                        .background(MaterialTheme.colorScheme.primary))
          }

          // Catchy description section with a brief highlight of the event

          Text(
              modifier =
                  Modifier.padding(vertical = 1.dp, horizontal = 4.dp)
                      .testTag("event_EventCatchyDescription")
                      .wrapContentWidth(),
              text = event.catchyDescription,
              style = MaterialTheme.typography.bodySmall,
              color = MaterialTheme.colorScheme.onSurface)
        }
      }
}
