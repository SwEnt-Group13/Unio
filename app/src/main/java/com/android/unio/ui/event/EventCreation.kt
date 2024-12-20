package com.android.unio.ui.event

import android.net.Uri
import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.outlined.Clear
import androidx.compose.material.icons.rounded.Clear
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Alignment.Companion.CenterHorizontally
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import com.android.unio.R
import com.android.unio.mocks.firestore.MockReferenceList
import com.android.unio.model.association.Association
import com.android.unio.model.association.AssociationViewModel
import com.android.unio.model.event.Event
import com.android.unio.model.event.EventType
import com.android.unio.model.event.EventViewModel
import com.android.unio.model.firestore.firestoreReferenceListWith
import com.android.unio.model.functions.addEditEventCloudFunction
import com.android.unio.model.map.Location
import com.android.unio.model.map.nominatim.NominatimLocationSearchViewModel
import com.android.unio.model.search.SearchViewModel
import com.android.unio.model.strings.test_tags.event.EventCreationTestTags
import com.android.unio.model.utils.TextLength
import com.android.unio.model.utils.Utils
import com.android.unio.ui.components.BannerImagePicker
import com.android.unio.ui.components.Chips
import com.android.unio.ui.components.DateAndTimePicker
import com.android.unio.ui.components.NominatimLocationPicker
import com.android.unio.ui.event.overlay.AssociationsOverlay
import com.android.unio.ui.event.overlay.EventTypeOverlay
import com.android.unio.ui.navigation.NavigationAction
import com.android.unio.ui.theme.AppTypography
import com.google.firebase.Timestamp
import kotlinx.coroutines.flow.MutableStateFlow

/**
 * A screen that allows users to create an event.
 *
 * @param navigationAction The navigation action to use.
 * @param searchViewModel The [SearchViewModel] to use.
 * @param associationViewModel The [AssociationViewModel] to use.
 * @param eventViewModel The [EventViewModel] to use.
 * @param locationSearchViewModel The [NominatimLocationSearchViewModel] to use.
 */
@Composable
fun EventCreationScreen(
    navigationAction: NavigationAction,
    searchViewModel: SearchViewModel,
    associationViewModel: AssociationViewModel,
    eventViewModel: EventViewModel,
    locationSearchViewModel: NominatimLocationSearchViewModel
) {
  val context = LocalContext.current
  val scrollState = rememberScrollState()
  var showCoauthorsOverlay by remember { mutableStateOf(false) }
  var showTaggedOverlay by remember { mutableStateOf(false) }
  var showEventTypeOverlay by remember { mutableStateOf(false) }

  var name by remember { mutableStateOf("") }
  var shortDescription by remember { mutableStateOf("") }
  var longDescription by remember { mutableStateOf("") }

  val eventTypeFlow = remember {
    MutableStateFlow(EventType.entries.map { it to mutableStateOf(false) }.toList())
  }

  val types by eventTypeFlow.collectAsState()

  var coauthorsAndBoolean =
      associationViewModel.associations.collectAsState().value.map { it to mutableStateOf(false) }

  var taggedAndBoolean =
      associationViewModel.associations.collectAsState().value.map { it to mutableStateOf(false) }

  var startTimestamp: Timestamp? by remember { mutableStateOf(null) }
  var endTimestamp: Timestamp? by remember { mutableStateOf(null) }

  var selectedLocation by remember { mutableStateOf<Location?>(null) }

  val eventBannerUri = remember { mutableStateOf<Uri>(Uri.EMPTY) }

  Scaffold(modifier = Modifier.testTag(EventCreationTestTags.SCREEN)) { padding ->
    Column(
        modifier =
            Modifier.padding(padding).padding(20.dp).fillMaxWidth().verticalScroll(scrollState),
        verticalArrangement = Arrangement.spacedBy(6.dp),
        horizontalAlignment = CenterHorizontally) {
          Row(
              modifier = Modifier.fillMaxWidth(),
              verticalAlignment = Alignment.CenterVertically,
              horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                IconButton(onClick = { navigationAction.goBack() }) {
                  Icon(
                      Icons.AutoMirrored.Filled.ArrowBack,
                      contentDescription = context.getString(R.string.event_creation_cancel_button))
                }
                Text(
                    context.getString(R.string.event_creation_title),
                    style = AppTypography.headlineSmall,
                    modifier = Modifier.testTag(EventCreationTestTags.TITLE))
              }

          OutlinedTextField(
              modifier = Modifier.fillMaxWidth().testTag(EventCreationTestTags.EVENT_TITLE),
              value = name,
              isError = name.isEmpty(),
              supportingText = {
                if (name.isEmpty()) {
                  Text(context.getString(R.string.event_creation_name_error))
                }
              },
              onValueChange = {
                if (Utils.checkInputLength(it, TextLength.SMALL)) {
                  name = it
                }
              },
              label = {
                Row(
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.CenterVertically) {
                      Text(
                          modifier = Modifier.padding(4.dp),
                          text = context.getString(R.string.event_creation_name_label))

                      if (Utils.checkInputLengthIsClose(name, TextLength.SMALL)) {
                        Text(
                            text = "${name.length}/${TextLength.SMALL.length}",
                            modifier =
                                Modifier.testTag(EventCreationTestTags.TITLE_CHARACTER_COUNTER))
                      }
                    }
              },
              trailingIcon = {
                IconButton(
                    onClick = { name = "" },
                    enabled = name.isNotEmpty(),
                    modifier = Modifier.testTag(EventCreationTestTags.EVENT_TITLE_CLEAR_BUTTON)) {
                      Icon(
                          imageVector = Icons.Outlined.Clear,
                          contentDescription =
                              context.getString(
                                  R.string.event_creation_content_description_clear_title))
                    }
              })

          OutlinedTextField(
              modifier = Modifier.fillMaxWidth().testTag(EventCreationTestTags.SHORT_DESCRIPTION),
              value = shortDescription,
              isError = shortDescription.isEmpty(),
              supportingText = {
                if (shortDescription.isEmpty()) {
                  Text(context.getString(R.string.event_creation_short_description_error))
                }
              },
              onValueChange = {
                if (Utils.checkInputLength(it, TextLength.MEDIUM)) {
                  shortDescription = it
                }
              },
              label = {
                Row(
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.CenterVertically) {
                      Text(
                          modifier = Modifier.padding(4.dp),
                          text = context.getString(R.string.event_creation_short_description_label))

                      if (Utils.checkInputLengthIsClose(shortDescription, TextLength.MEDIUM)) {
                        Text(
                            text = "${shortDescription.length}/${TextLength.MEDIUM.length}",
                            modifier =
                                Modifier.testTag(
                                    EventCreationTestTags.SHORT_DESCRIPTION_CHARACTER_COUNTER))
                      }
                    }
              },
              trailingIcon = {
                IconButton(
                    onClick = { shortDescription = "" },
                    enabled = shortDescription.isNotEmpty(),
                    modifier =
                        Modifier.testTag(
                            EventCreationTestTags.EVENT_SHORT_DESCRIPTION_CLEAR_BUTTON)) {
                      Icon(
                          imageVector = Icons.Outlined.Clear,
                          contentDescription =
                              context.getString(
                                  R.string
                                      .event_creation_content_description_clear_short_description))
                    }
              })

          BannerImagePicker(
              eventBannerUri, modifier = Modifier.testTag(EventCreationTestTags.EVENT_IMAGE))

          OutlinedButton(
              modifier = Modifier.fillMaxWidth().testTag(EventCreationTestTags.COAUTHORS),
              onClick = { showCoauthorsOverlay = true }) {
                Icon(
                    Icons.Default.Add,
                    contentDescription =
                        context.getString(R.string.social_overlay_content_description_add))
                Text(context.getString(R.string.event_creation_coauthors_label))
              }

          Chips(coauthorsAndBoolean, getName = { it.name })

          OutlinedButton(
              modifier = Modifier.fillMaxWidth().testTag(EventCreationTestTags.TAGGED_ASSOCIATIONS),
              onClick = { showTaggedOverlay = true }) {
                Icon(
                    Icons.Default.Add,
                    contentDescription =
                        context.getString(R.string.social_overlay_content_description_add))
                Text(context.getString(R.string.event_creation_tagged_label))
              }

          Chips(taggedAndBoolean, getName = { it.name })

          OutlinedButton(
              modifier = Modifier.fillMaxWidth().testTag(EventCreationTestTags.EVENT_TYPE),
              onClick = { showEventTypeOverlay = true }) {
                Icon(
                    Icons.Default.Add,
                    contentDescription =
                        context.getString(R.string.social_overlay_content_description_add))
                Text(context.getString(R.string.event_creation_type))
              }

          Chips(types, getName = { context.getString(it.text) })

          OutlinedTextField(
              modifier = Modifier.fillMaxWidth().testTag(EventCreationTestTags.DESCRIPTION),
              value = longDescription,
              isError = longDescription.isEmpty(),
              supportingText = {
                if (longDescription.isEmpty()) {
                  Text(context.getString(R.string.event_creation_description_error))
                }
              },
              onValueChange = {
                if (Utils.checkInputLength(it, TextLength.LARGE)) {
                  longDescription = it
                }
              },
              label = {
                Row(
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.CenterVertically) {
                      Text(
                          modifier = Modifier.padding(4.dp),
                          text = context.getString(R.string.event_creation_description_label))

                      if (Utils.checkInputLengthIsClose(longDescription, TextLength.LARGE)) {
                        Text(
                            text = "${longDescription.length}/${TextLength.LARGE.length}",
                            modifier =
                                Modifier.testTag(
                                    EventCreationTestTags.DESCRIPTION_CHARACTER_COUNTER))
                      }
                    }
              })

          DateAndTimePicker(
              context.getString(R.string.event_creation_startdate_label),
              context.getString(R.string.event_creation_starttime_label),
              modifier = Modifier.testTag(EventCreationTestTags.START_TIME),
              null,
              null,
              EventCreationTestTags.START_DATE_FIELD,
              EventCreationTestTags.START_TIME_FIELD,
              EventCreationTestTags.START_DATE_PICKER,
              EventCreationTestTags.START_TIME_PICKER) {
                startTimestamp = it
              }

          DateAndTimePicker(
              context.getString(R.string.event_creation_enddate_label),
              context.getString(R.string.event_creation_endtime_label),
              modifier = Modifier.testTag(EventCreationTestTags.END_TIME),
              null,
              null,
              EventCreationTestTags.END_DATE_FIELD,
              EventCreationTestTags.END_TIME_FIELD,
              EventCreationTestTags.END_DATE_PICKER,
              EventCreationTestTags.END_TIME_PICKER) {
                endTimestamp = it
              }
          if (startTimestamp != null && endTimestamp != null) {
            if (startTimestamp!! > endTimestamp!!) {
              Text(
                  text = context.getString(R.string.event_creation_end_before_start),
                  modifier = Modifier.testTag(EventCreationTestTags.ERROR_TEXT1),
                  color = MaterialTheme.colorScheme.error)
            }
            if (startTimestamp!! == endTimestamp!!) {
              Text(
                  text = context.getString(R.string.event_creation_end_equals_start),
                  modifier = Modifier.testTag(EventCreationTestTags.ERROR_TEXT2),
                  color = MaterialTheme.colorScheme.error)
            }
          }

          NominatimLocationPicker(
              locationSearchViewModel,
              null,
              EventCreationTestTags.LOCATION,
              EventCreationTestTags.LOCATION_SUGGESTION_ITEM_LATITUDE) {
                selectedLocation = it
              }

          Spacer(modifier = Modifier.width(10.dp))

          Button(
              modifier = Modifier.testTag(EventCreationTestTags.SAVE_BUTTON),
              enabled =
                  name.isNotEmpty() &&
                      shortDescription.isNotEmpty() &&
                      longDescription.isNotEmpty() &&
                      startTimestamp != null &&
                      endTimestamp != null &&
                      startTimestamp!! < endTimestamp!! &&
                      eventBannerUri.value != Uri.EMPTY &&
                      selectedLocation != null,
              onClick = {
                try {
                  val inputStream = context.contentResolver.openInputStream(eventBannerUri.value)!!
                  val newEvent =
                      Event(
                          uid = "", // This gets overwritten by eventViewModel.addEvent
                          title = name,
                          organisers =
                              Association.firestoreReferenceListWith(
                                  (coauthorsAndBoolean
                                          .filter { it.second.value }
                                          .map { it.first.uid } +
                                          associationViewModel.selectedAssociation.value!!.uid)
                                      .distinct()),
                          taggedAssociations =
                              Association.firestoreReferenceListWith(
                                  taggedAndBoolean.filter { it.second.value }.map { it.first.uid }),
                          image = eventBannerUri.value.toString(),
                          description = longDescription,
                          catchyDescription = shortDescription,
                          price = 0.0,
                          startDate = startTimestamp!!,
                          endDate = endTimestamp!!,
                          location = selectedLocation!!,
                          types = types.filter { it.second.value }.map { it.first },
                          eventPictures = MockReferenceList(),
                      )

                  // First step: Add the image to the event
                  eventViewModel.addImageToEvent(
                      inputStream,
                      newEvent,
                      onSuccess = { eventWithImage ->
                        // Second step: Call the cloud function to save the event
                        addEditEventCloudFunction(
                            eventWithImage,
                            associationViewModel.selectedAssociation.value!!.uid,
                            onSuccess = { response ->
                              // Handle successful cloud function execution
                              Log.d("EventCreation", "Event successfully created: $response")
                              associationViewModel.addEditEventLocally(
                                  eventWithImage) // Update locally
                              eventViewModel.addEditEventLocally(eventWithImage)
                              navigationAction.goBack() // Navigate back
                            },
                            onError = { error ->
                              // Handle error from cloud function
                              Log.e(
                                  "EventCreation",
                                  "Failed to save event via cloud function: $error")
                              Toast.makeText(
                                      context,
                                      context.getString(R.string.event_creation_failed),
                                      Toast.LENGTH_SHORT)
                                  .show()
                            },
                            isNewEvent = true)
                      },
                      onFailure = { error ->
                        // Handle error from adding image
                        Log.e("EventCreation", "Failed to add image to event: $error")
                        Toast.makeText(
                                context,
                                context.getString(R.string.event_creation_failed),
                                Toast.LENGTH_SHORT)
                            .show()
                      })
                } catch (e: Exception) {
                  Log.e("EventCreation", "Unexpected error during event creation: $e")
                  Toast.makeText(
                          context,
                          context.getString(R.string.event_creation_failed),
                          Toast.LENGTH_SHORT)
                      .show()
                }
              }) {
                Text(context.getString(R.string.event_creation_save_button))
              }
        }

    if (showCoauthorsOverlay) {
      AssociationsOverlay(
          onDismiss = { showCoauthorsOverlay = false },
          onSave = { coauthors ->
            coauthorsAndBoolean = coauthors
            showCoauthorsOverlay = false
          },
          associations = coauthorsAndBoolean,
          searchViewModel = searchViewModel,
          headerText = context.getString(R.string.associations_overlay_coauthors_title),
          bodyText = context.getString(R.string.associations_overlay_coauthors_description))
    }

    if (showTaggedOverlay) {
      AssociationsOverlay(
          onDismiss = { showTaggedOverlay = false },
          onSave = { tagged ->
            taggedAndBoolean = tagged
            showTaggedOverlay = false
          },
          associations = taggedAndBoolean,
          searchViewModel = searchViewModel,
          headerText = context.getString(R.string.associations_overlay_tagged_title),
          bodyText = context.getString(R.string.associations_overlay_tagged_description))
    }

    if (showEventTypeOverlay) {
      EventTypeOverlay(
          onDismiss = { showEventTypeOverlay = false },
          onSave = { types ->
            eventTypeFlow.value = types
            showEventTypeOverlay = false
          },
          types = types)
    }
  }
}
