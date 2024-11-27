package com.android.unio.ui.event

import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.clickable
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
import androidx.core.net.toUri
import com.android.unio.R
import com.android.unio.model.association.Association
import com.android.unio.model.association.AssociationViewModel
import com.android.unio.model.event.Event
import com.android.unio.model.event.EventViewModel
import com.android.unio.model.firestore.firestoreReferenceListWith
import com.android.unio.model.map.Location
import com.android.unio.model.search.SearchViewModel
import com.android.unio.model.strings.test_tags.EventCreationTestTags
import com.android.unio.ui.event.overlay.AssociationsOverlay
import com.android.unio.ui.navigation.NavigationAction
import com.android.unio.ui.theme.AppTypography
import com.google.firebase.Timestamp

@Composable
fun EventEditScreen(
    navigationAction: NavigationAction,
    searchViewModel: SearchViewModel,
    associationViewModel: AssociationViewModel,
    eventViewModel: EventViewModel
) {
  val context = LocalContext.current
  val scrollState = rememberScrollState()
  var showCoauthorsOverlay by remember { mutableStateOf(false) }
  var showTaggedOverlay by remember { mutableStateOf(false) }

  var event = remember { eventViewModel.selectedEvent.value!! }

  var name by remember { mutableStateOf(event.title) }
  var shortDescription by remember { mutableStateOf(event.catchyDescription) }
  var longDescription by remember { mutableStateOf(event.description) }

  var coauthorsAndBoolean =
      associationViewModel.associations.collectAsState().value.map {
        it to
            (if (event.organisers.contains(it.uid)) mutableStateOf(true) else mutableStateOf(false))
      }

  var taggedAndBoolean =
      associationViewModel.associations.collectAsState().value.map {
        it to
            (if (event.taggedAssociations.contains(it.uid)) mutableStateOf(true)
            else mutableStateOf(false))
      }

  var startTimestamp: Timestamp? by remember { mutableStateOf(event.startDate) }
  var endTimestamp: Timestamp? by remember { mutableStateOf(event.endDate) }

  val eventBannerUri = remember { mutableStateOf<Uri>(event.image.toUri()) }

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
              onValueChange = { name = it },
              label = { Text(context.getString(R.string.event_creation_name_label)) })

          OutlinedTextField(
              modifier = Modifier.fillMaxWidth().testTag(EventCreationTestTags.SHORT_DESCRIPTION),
              value = shortDescription,
              onValueChange = { shortDescription = it },
              label = { Text(context.getString(R.string.event_creation_short_description_label)) })

          BannerImagePicker(eventBannerUri)

          OutlinedButton(
              modifier = Modifier.fillMaxWidth().testTag(EventCreationTestTags.COAUTHORS),
              onClick = { showCoauthorsOverlay = true }) {
                Icon(
                    Icons.Default.Add,
                    contentDescription =
                        context.getString(R.string.social_overlay_content_description_add))
                Text(context.getString(R.string.event_creation_coauthors_label))
              }

          AssociationChips(coauthorsAndBoolean)

          OutlinedButton(
              modifier = Modifier.fillMaxWidth().testTag(EventCreationTestTags.TAGGED_ASSOCIATIONS),
              onClick = { showTaggedOverlay = true }) {
                Icon(
                    Icons.Default.Add,
                    contentDescription =
                        context.getString(R.string.social_overlay_content_description_add))
                Text(context.getString(R.string.event_creation_tagged_label))
              }

          AssociationChips(taggedAndBoolean)

          OutlinedTextField(
              modifier = Modifier.fillMaxWidth().testTag(EventCreationTestTags.DESCRIPTION),
              value = longDescription,
              onValueChange = { longDescription = it },
              label = { Text(context.getString(R.string.event_creation_description_label)) })

          DateAndTimePicker(
              context.getString(R.string.event_creation_startdate_label),
              context.getString(R.string.event_creation_starttime_label),
              modifier = Modifier.testTag(EventCreationTestTags.START_TIME)) {
                startTimestamp = it
              }

          DateAndTimePicker(
              context.getString(R.string.event_creation_enddate_label),
              context.getString(R.string.event_creation_endtime_label),
              modifier = Modifier.testTag(EventCreationTestTags.END_TIME)) {
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

          OutlinedTextField(
              modifier =
                  Modifier.fillMaxWidth().testTag(EventCreationTestTags.LOCATION).clickable {
                    Toast.makeText(context, "Location is not implemented yet", Toast.LENGTH_SHORT)
                        .show()
                  },
              value = "",
              onValueChange = {},
              label = { Text(context.getString(R.string.event_creation_location_label)) })

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
                      eventBannerUri.value != Uri.EMPTY,
              onClick = {
                val inputStream = context.contentResolver.openInputStream(eventBannerUri.value)!!
                eventViewModel.updateEvent(
                    inputStream,
                    Event(
                        uid = event.uid,
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
                        location = Location(),
                    ),
                    onSuccess = { navigationAction.goBack() },
                    onFailure = {
                      Toast.makeText(
                              context,
                              context.getString(R.string.event_creation_failed),
                              Toast.LENGTH_SHORT)
                          .show()
                    })
              }) {
                Text(context.getString(R.string.event_creation_save_button))
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
        }
  }
}
