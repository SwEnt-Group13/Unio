package com.android.unio.ui.event

import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.FilterQuality
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.net.toUri
import com.android.unio.R
import com.android.unio.model.association.Association
import com.android.unio.model.event.Event
import com.android.unio.model.event.EventType
import com.android.unio.model.event.EventUtils.addAlphaToColor
import com.android.unio.model.event.EventUtils.formatTimestamp
import com.android.unio.model.event.EventViewModel
import com.android.unio.model.strings.FormatStrings.DAY_MONTH_FORMAT
import com.android.unio.model.strings.FormatStrings.HOUR_MINUTE_FORMAT
import com.android.unio.model.strings.test_tags.event.EventCardTestTags
import com.android.unio.model.user.UserViewModel
import com.android.unio.ui.image.AsyncImageWrapper
import com.android.unio.ui.navigation.NavigationAction
import com.android.unio.ui.navigation.Screen
import com.android.unio.ui.theme.AppTypography
import java.text.SimpleDateFormat
import java.util.Locale

const val SECONDS_IN_AN_HOUR = 3600

/**
 * A composable function to display an event card.
 *
 * @param navigationAction The navigation action to use.
 * @param event The event to display.
 * @param userViewModel The [UserViewModel] to use.
 * @param eventViewModel The [EventViewModel] to use.
 * @param shouldBeEditable Whether the event card should be editable.
 */
@Composable
fun EventCard(
    navigationAction: NavigationAction,
    event: Event,
    userViewModel: UserViewModel,
    eventViewModel: EventViewModel,
    shouldBeEditable: Boolean =
        false // To be changed in the future once permissions are implemented
) {
  val context = LocalContext.current
  val user by userViewModel.user.collectAsState()
  val associations by event.organisers.list.collectAsState()

  if (user == null) {
    Log.e("EventCard", "User not found.")
    Toast.makeText(context, "An error occurred.", Toast.LENGTH_SHORT).show()
    return
  }

  EventCardScaffold(
      event,
      associations,
      onClickEventCard = {
        eventViewModel.selectEvent(event.uid, true)
        navigationAction.navigateTo(Screen.EVENT_DETAILS)
      },
      onClickEditButton = {
        eventViewModel.selectEvent(event.uid)
        navigationAction.navigateTo(Screen.EDIT_EVENT)
      },
      shouldBeEditable = shouldBeEditable,
      eventViewModel = eventViewModel,
      userViewModel = userViewModel)
}

/**
 * The content of the event card.
 *
 * @param event The event to display.
 * @param organisers The list of associations organising the event.
 * @param isSaved Whether the event is saved.
 * @param onClickEventCard Callback when the event card is clicked.
 * @param onClickSaveButton Callback when the save button is clicked.
 * @param onClickEditButton Callback when the edit button is clicked.
 * @param shouldBeEditable Whether the event card should be editable.
 */
@Composable
fun EventCardScaffold(
    event: Event,
    organisers: List<Association>,
    onClickEventCard: () -> Unit,
    onClickEditButton: () -> Unit,
    shouldBeEditable: Boolean,
    eventViewModel: EventViewModel,
    userViewModel: UserViewModel
) {
  val context = LocalContext.current
  val events by eventViewModel.events.collectAsState()

  Column(
      modifier =
          Modifier.fillMaxWidth()
              .padding(vertical = 8.dp)
              .testTag(EventCardTestTags.EVENT_ITEM)
              .clip(RoundedCornerShape(10.dp))
              .background(MaterialTheme.colorScheme.primaryContainer)
              .clickable { onClickEventCard() }) {

        // Event image section, displays the main event image or a placeholder if the URL is invalid

        Box(modifier = Modifier.fillMaxWidth().height(100.dp)) {
          AsyncImageWrapper(
              imageUri = event.image.toUri(),
              contentDescription =
                  context.getString(R.string.event_card_content_description_event_image),
              modifier =
                  Modifier.fillMaxWidth()
                      .height(100.dp)
                      .clip(RoundedCornerShape(topStart = 10.dp, topEnd = 10.dp))
                      .testTag(EventCardTestTags.EVENT_IMAGE),
              contentScale = ContentScale.Crop // crop the image to fit
              )

          if (shouldBeEditable) {
            Box(
                modifier =
                    Modifier.align(Alignment.TopStart)
                        .padding(4.dp)
                        .clip(RoundedCornerShape(4.dp))
                        .background(MaterialTheme.colorScheme.surfaceContainer)) {
                  Text(
                      " ${events.first{it.uid == event.uid}.numberOfSaved} " +
                          context.getString(R.string.event_card_interested_string) +
                          " ",
                      color = MaterialTheme.colorScheme.onSecondaryContainer)
                }
          }
          // Save button icon on the top right corner of the image, allows the user to save/unsave
          // the event
          Row(
              modifier = Modifier.align(Alignment.TopEnd).padding(2.dp),
              horizontalArrangement = Arrangement.SpaceBetween) {
                if (shouldBeEditable) {
                  IconButton(
                      modifier =
                          Modifier.size(28.dp)
                              .clip(RoundedCornerShape(14.dp))
                              .background(MaterialTheme.colorScheme.inversePrimary)
                              .padding(4.dp)
                              .testTag(EventCardTestTags.EDIT_BUTTON),
                      onClick = { onClickEditButton() }) {
                        Icon(
                            imageVector = Icons.Outlined.Edit,
                            contentDescription =
                                context.getString(
                                    R.string.event_card_content_description_edit_association),
                            tint = Color.White)
                      }
                }
                Spacer(modifier = Modifier.width(2.dp))

                EventSaveButton(event, eventViewModel, userViewModel)
              }
        }

        // Event details section, including title, type, location, and time

        Column(modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)) {

          // Row containing event title and type label

          Row(
              verticalAlignment = Alignment.CenterVertically,
              horizontalArrangement = Arrangement.SpaceBetween,
              modifier = Modifier.fillMaxWidth()) {

                // Title of the event
                Text(
                    modifier =
                        Modifier.weight(1f)
                            .padding(vertical = 1.dp, horizontal = 4.dp)
                            .testTag(EventCardTestTags.EVENT_TITLE),
                    text = event.title,
                    style = AppTypography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis)

                // Row displaying event type and organiser associations' logos
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.End,
                    modifier = Modifier.wrapContentWidth()) {
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
                                text = context.getString(type.text),
                                modifier =
                                    Modifier.padding(horizontal = 4.dp, vertical = 4.dp)
                                        .testTag(EventCardTestTags.EVENT_MAIN_TYPE),
                                color = MaterialTheme.colorScheme.scrim,
                                style = TextStyle(fontSize = 8.sp))
                          }

                      Spacer(modifier = Modifier.width(6.dp))

                      // Organiser associations' logos on the right of the title and type
                      for (i in organisers.indices) {
                        AsyncImageWrapper(
                            imageUri = organisers[i].image.toUri(),
                            contentDescription =
                                context.getString(
                                    R.string.event_card_content_description_association_logo),
                            modifier =
                                Modifier.testTag("${EventCardTestTags.ASSOCIATION_LOGO}$i")
                                    .size(24.dp)
                                    .align(Alignment.CenterVertically)
                                    .padding(end = 3.dp)
                                    .clip(RoundedCornerShape(5.dp)),
                            contentScale = ContentScale.Crop,
                            placeholderResourceId = R.drawable.adec,
                            filterQuality = FilterQuality.None)
                      }
                    }
              }

          // Row displaying event location and formatted date/time details

          Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
              Text(
                  modifier =
                      Modifier.padding(vertical = 1.dp, horizontal = 4.dp)
                          .testTag(EventCardTestTags.EVENT_LOCATION)
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
                    Modifier.padding(vertical = 1.dp, horizontal = 0.dp)
                        .testTag(EventCardTestTags.EVENT_DATE),
                text =
                    formatTimestamp(
                        event.startDate, SimpleDateFormat(DAY_MONTH_FORMAT, Locale.getDefault())),
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
                modifier = Modifier.testTag(EventCardTestTags.EVENT_TIME).wrapContentWidth(),
                text =
                    formatTimestamp(
                        event.startDate, SimpleDateFormat(HOUR_MINUTE_FORMAT, Locale.getDefault())),
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
                      .testTag(EventCardTestTags.EVENT_CATCHY_DESCRIPTION)
                      .wrapContentWidth(),
              text = event.catchyDescription,
              style = MaterialTheme.typography.bodySmall,
              color = MaterialTheme.colorScheme.onSurface)
        }
      }
}
