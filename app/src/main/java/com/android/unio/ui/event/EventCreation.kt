package com.android.unio.ui.event

import android.net.Uri
import android.util.Log
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.waitForUpOrCancellation
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.InputChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TimeInput
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Alignment.Companion.CenterHorizontally
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.input.pointer.PointerEventPass
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import coil.compose.rememberAsyncImagePainter
import com.android.unio.R
import com.android.unio.model.association.Association
import com.android.unio.model.association.AssociationViewModel
import com.android.unio.model.event.Event
import com.android.unio.model.event.EventViewModel
import com.android.unio.model.firestore.firestoreReferenceListWith
import com.android.unio.model.map.Location
import com.android.unio.model.search.SearchViewModel
import com.android.unio.model.strings.FormatStrings.DAY_MONTH_YEAR_FORMAT
import com.android.unio.model.strings.FormatStrings.HOUR_MINUTE_FORMAT
import com.android.unio.model.strings.test_tags.EventCreationTestTags
import com.android.unio.ui.event.overlay.AssociationsOverlay
import com.android.unio.ui.navigation.NavigationAction
import com.android.unio.ui.theme.AppTypography
import com.google.firebase.Timestamp
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun EventCreationScreen(
    navigationAction: NavigationAction,
    searchViewModel: SearchViewModel,
    associationViewModel: AssociationViewModel,
    eventViewModel: EventViewModel
) {
  val context = LocalContext.current
  val scrollState = rememberScrollState()
  var showCoauthorsOverlay by remember { mutableStateOf(false) }
  var showTaggedOverlay by remember { mutableStateOf(false) }

  var name by remember { mutableStateOf("") }
  var shortDescription by remember { mutableStateOf("") }
  var longDescription by remember { mutableStateOf("") }

  var coauthorsAndBoolean =
      associationViewModel.associations.collectAsState().value.map { it to mutableStateOf(false) }

  var taggedAndBoolean =
      associationViewModel.associations.collectAsState().value.map { it to mutableStateOf(false) }

  var startTimestamp: Timestamp? by remember { mutableStateOf(null) }
  var endTimestamp: Timestamp? by remember { mutableStateOf(null) }

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
                eventViewModel.addEvent(
                    inputStream,
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
                        location = Location(),
                    ),
                    onSuccess = { navigationAction.goBack() },
                    onFailure = {
                      Toast.makeText(context, context.getString(R.string.event_creation_failed), Toast.LENGTH_SHORT).show()
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

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun AssociationChips(
    associations: List<Pair<Association, MutableState<Boolean>>>,
) {
  val context = LocalContext.current
  FlowRow {
    associations.forEach { (association, selected) ->
      if (selected.value) {
        InputChip(
            label = { Text(association.name) },
            onClick = {},
            selected = selected.value,
            avatar = {
              Icon(
                  Icons.Default.Close,
                  contentDescription = context.getString(R.string.associations_overlay_remove),
                  modifier = Modifier.clickable { selected.value = !selected.value })
            })
      }
    }
  }
}

@Composable
private fun BannerImagePicker(eventBannerUri: MutableState<Uri>) {
  val context = LocalContext.current

  val pickMedia =
      rememberLauncherForActivityResult(
          contract = ActivityResultContracts.PickVisualMedia(),
          onResult = { uri: Uri? -> uri?.let { eventBannerUri.value = it } })

  Box(
      modifier =
          Modifier.size(390.dp, 100.dp)
              .clip(RoundedCornerShape(4.dp))
              .testTag(EventCreationTestTags.EVENT_IMAGE)
              .clickable {
                pickMedia.launch(
                    PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
              },
      contentAlignment = Alignment.Center) {
        if (eventBannerUri.value != Uri.EMPTY) {
          Image(
              painter = rememberAsyncImagePainter(eventBannerUri.value),
              contentDescription =
                  context.getString(R.string.event_creation_selected_image_description),
              modifier = Modifier.fillMaxSize(),
              contentScale = ContentScale.Crop)
        } else {
          Image(
              painter = painterResource(id = R.drawable.adec),
              contentDescription =
                  context.getString(R.string.event_creation_placeholder_image_description),
              modifier = Modifier.fillMaxSize(),
              contentScale = ContentScale.Crop)
          Text(
              text = context.getString(R.string.event_creation_image_label),
              modifier = Modifier.align(Alignment.Center))
        }
      }
}

@Composable
private fun DateAndTimePicker(
    dateString: String,
    timeString: String,
    modifier: Modifier,
    onTimestamp: (Timestamp) -> Unit
) {
  var isDatePickerVisible by remember { mutableStateOf(false) }
  var isTimePickerVisible by remember { mutableStateOf(false) }
  var selectedDate by remember { mutableStateOf<Long?>(null) }
  var selectedTime by remember { mutableStateOf<Long?>(null) }
  val context = LocalContext.current

  Row(
      modifier.fillMaxWidth(),
  ) {
    OutlinedTextField(
        modifier =
            Modifier.weight(1f).pointerInput(Unit) {
              awaitEachGesture {
                // Modifier.clickable doesn't work for text fields, so we use Modifier.pointerInput
                // in the Initial pass to observe events before the text field consumes them
                // in the Main pass.
                awaitFirstDown(pass = PointerEventPass.Initial)
                val upEvent = waitForUpOrCancellation(pass = PointerEventPass.Initial)
                if (upEvent != null) {
                  isDatePickerVisible = true
                }
              }
            },
        value = selectedDate?.let { convertMillisToDate(it) } ?: "",
        readOnly = true,
        onValueChange = {},
        trailingIcon = { Icon(Icons.Default.DateRange, contentDescription = "Select date") },
        placeholder = { Text(context.getString(R.string.event_creation_placeholder_date_input)) },
        label = { Text(dateString) })
    Spacer(modifier = Modifier.weight(0.05f))
    OutlinedTextField(
        modifier =
            Modifier.weight(1f).pointerInput(Unit) {
              awaitEachGesture {
                // Modifier.clickable doesn't work for text fields, so we use Modifier.pointerInput
                // in the Initial pass to observe events before the text field consumes them
                // in the Main pass.
                awaitFirstDown(pass = PointerEventPass.Initial)
                val upEvent = waitForUpOrCancellation(pass = PointerEventPass.Initial)
                if (upEvent != null) {
                  isTimePickerVisible = true
                }
              }
            },
        value = selectedTime?.let { convertMillisToTime(it) } ?: "",
        readOnly = true,
        onValueChange = {},
        trailingIcon = { Icon(Icons.Default.AccessTime, contentDescription = "Select date") },
        placeholder = { Text(context.getString(R.string.event_creation_placeholder_time_input)) },
        label = { Text(timeString) })
  }

  if (isDatePickerVisible) {
    DatePickerModal(
        onDateSelected = {
          selectedDate = it
          isDatePickerVisible = false
        },
        onDismiss = { isDatePickerVisible = false })
  }

  if (isTimePickerVisible) {
    TimePickerModal(
        onTimeSelected = {
          selectedTime = it
          isTimePickerVisible = false
        },
        onDismiss = { isTimePickerVisible = false })
  }

  if (selectedDate != null && selectedTime != null) {
    onTimestamp(Timestamp(Date(selectedDate!! + selectedTime!!)))
  }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DatePickerModal(onDateSelected: (Long?) -> Unit, onDismiss: () -> Unit) {
  val datePickerState = rememberDatePickerState()
  val context = LocalContext.current

  DatePickerDialog(
      onDismissRequest = onDismiss,
      confirmButton = {
        TextButton(
            onClick = {
              onDateSelected(datePickerState.selectedDateMillis)
              onDismiss()
            }) {
              Text(context.getString(R.string.event_creation_dialog_ok))
            }
      },
      dismissButton = {
        TextButton(onClick = onDismiss) {
          Text(context.getString(R.string.event_creation_dialog_cancel))
        }
      }) {
        DatePicker(state = datePickerState)
      }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TimePickerModal(onTimeSelected: (Long?) -> Unit, onDismiss: () -> Unit) {
  val timePickerState = rememberTimePickerState(is24Hour = true)

  TimePickerDialog(
      onDismiss = onDismiss,
      onConfirm = {
        onTimeSelected(
            timePickerState.hour * 60 * 60 * 1000L - 3600000 + timePickerState.minute * 60 * 1000L)
        onDismiss()
      }) {
        TimeInput(state = timePickerState)
      }
}

/**
 * A Dialog that is the analog of the DatePickerDialog, but for TimePicker as it currently does not
 * exist in the Material3 library.
 */
@Composable
private fun TimePickerDialog(
    onDismiss: () -> Unit,
    onConfirm: () -> Unit,
    content: @Composable () -> Unit
) {
  val context = LocalContext.current
  AlertDialog(
      onDismissRequest = onDismiss,
      dismissButton = {
        TextButton(onClick = { onDismiss() }) {
          Text(context.getString(R.string.event_creation_dialog_cancel))
        }
      },
      confirmButton = {
        TextButton(onClick = { onConfirm() }) {
          Text(context.getString(R.string.event_creation_dialog_ok))
        }
      },
      text = { content() })
}

fun convertMillisToDate(millis: Long): String {
  val formatter = SimpleDateFormat(DAY_MONTH_YEAR_FORMAT, Locale.getDefault())
  return formatter.format(Date(millis))
}

fun convertMillisToTime(millis: Long): String {
  val formatter = SimpleDateFormat(HOUR_MINUTE_FORMAT, Locale.getDefault())
  return formatter.format(Date(millis))
}
