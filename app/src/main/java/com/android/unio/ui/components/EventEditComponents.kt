package com.android.unio.ui.components

import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.waitForUpOrCancellation
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.InputChip
import androidx.compose.material3.OutlinedTextField
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
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.input.pointer.PointerEventPass
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.PopupProperties
import coil.compose.rememberAsyncImagePainter
import com.android.unio.R
import com.android.unio.model.association.Association
import com.android.unio.model.map.Location
import com.android.unio.model.map.nominatim.NominatimLocationSearchViewModel
import com.android.unio.model.strings.FormatStrings.DAY_MONTH_YEAR_FORMAT
import com.android.unio.model.strings.FormatStrings.HOUR_MINUTE_FORMAT
import com.google.firebase.Timestamp
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

const val DROP_DOWN_MAX_CHARACTERS = 40
const val DROP_DOWN_MAX_ROWS = 3

/**
 * Composable for the location picker that uses the Nominatim API to search for locations. It
 * consists of a text field and a dropdown menu with location suggestions.
 *
 * @param locationSearchViewModel NominatimLocationSearchViewModel : ViewModel for searching
 *   locations.
 * @param initialLocation Location? : Initial location to pre-fill the text field.
 * @param textFieldTestTag String : Test tag for the text field.
 * @param dropdownTestTag String : Test tag for the dropdown menu.
 * @param onLocationSelected (Location) -> Unit : Lambda that is called when a location is selected.
 */
@Composable
fun NominatimLocationPicker(
    locationSearchViewModel: NominatimLocationSearchViewModel,
    initialLocation: Location?,
    textFieldTestTag: String,
    dropdownTestTag: String,
    onLocationSelected: (Location) -> Unit
) {
  val context = LocalContext.current

  val locationQuery by locationSearchViewModel.query.collectAsState()
  val locationSuggestions by locationSearchViewModel.locationSuggestions.collectAsState()
  var showDropdown by remember { mutableStateOf(false) }
  var toast: Toast? by remember { mutableStateOf(null) }

  var shouldDisplayInitialLocation by remember { mutableStateOf(true) }

  Box(modifier = Modifier.fillMaxWidth()) {
    OutlinedTextField(
        value = if (shouldDisplayInitialLocation) initialLocation?.name ?: "" else locationQuery,
        onValueChange = {
          locationSearchViewModel.setQuery(it)
          shouldDisplayInitialLocation = false
          showDropdown = true
        },
        label = { Text(context.getString(R.string.event_creation_location_label)) },
        placeholder = { Text(context.getString(R.string.event_creation_location_input_label)) },
        modifier = Modifier.fillMaxWidth().testTag(textFieldTestTag))

    DropdownMenu(
        expanded = showDropdown && locationSuggestions.isNotEmpty(),
        onDismissRequest = { showDropdown = false },
        properties = PopupProperties(focusable = false),
        modifier = Modifier.fillMaxWidth().heightIn(max = 200.dp)) {
          locationSuggestions.take(DROP_DOWN_MAX_ROWS).forEach { location ->
            DropdownMenuItem(
                text = {
                  Text(
                      text =
                          location.name.take(DROP_DOWN_MAX_CHARACTERS) +
                              if (location.name.length > DROP_DOWN_MAX_CHARACTERS)
                                  context.getString(
                                      R.string.event_creation_location_dropdown_points)
                              else "",
                      maxLines = 1)
                },
                onClick = {
                  locationSearchViewModel.setQuery(location.name)
                  onLocationSelected(location)
                  showDropdown = false
                },
                modifier = Modifier.padding(8.dp).testTag(dropdownTestTag + location.latitude))
            HorizontalDivider()
          }

          if (locationSuggestions.size > DROP_DOWN_MAX_ROWS) {
            DropdownMenuItem(
                text = { Text(context.getString(R.string.event_creation_location_dropdown_more)) },
                onClick = {
                  if (toast != null) {
                    toast?.cancel()
                  }

                  toast =
                      Toast.makeText(
                          context, "Continue typing to see more results", Toast.LENGTH_SHORT)
                  toast?.show()
                },
                modifier = Modifier.padding(8.dp))
          }
        }
  }
}

/**
 * Composable for the association chips that show the selected associations.
 *
 * @param associations List<Pair<Association, MutableState<Boolean>>> : List of associations and
 *   their selected state.
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun AssociationChips(
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

/**
 * Composable for the banner image picker that allows the user to select an image from the gallery.
 *
 * @param eventBannerUri MutableState<Uri> : MutableState that holds the URI of the selected image.
 * @param modifier Modifier : Modifier for the banner image picker.
 */
@Composable
fun BannerImagePicker(eventBannerUri: MutableState<Uri>, modifier: Modifier) {
  val context = LocalContext.current

  val pickMedia =
      rememberLauncherForActivityResult(
          contract = ActivityResultContracts.PickVisualMedia(),
          onResult = { uri: Uri? -> uri?.let { eventBannerUri.value = it } })

  Box(
      modifier.size(390.dp, 100.dp).clip(RoundedCornerShape(4.dp)).clickable {
        pickMedia.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
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

/**
 * Composable for the date and time picker that allows the user to select a date and time.
 *
 * @param dateString String : Label for the date field.
 * @param timeString String : Label for the time field.
 * @param modifier Modifier : Modifier for the date and time picker.
 * @param initialDate Long? : Initial date in milliseconds. Used to pre-fill the date field.
 * @param initialTime Long? : Initial time in milliseconds. Used to pre-fill the time field.
 * @param dateFieldTestTag String : Test tag for the date field.
 * @param timeFieldTestTag String : Test tag for the time field.
 * @param datePickerTestTag String : Test tag for the date picker.
 * @param timePickerTestTag String : Test tag for the time picker.
 * @param onTimestamp (Timestamp) -> Unit : Lambda that is called when a timestamp is selected.
 */
@Composable
fun DateAndTimePicker(
    dateString: String,
    timeString: String,
    modifier: Modifier,
    initialDate: Long?,
    initialTime: Long?,
    dateFieldTestTag: String,
    timeFieldTestTag: String,
    datePickerTestTag: String,
    timePickerTestTag: String,
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
            Modifier.testTag(dateFieldTestTag).weight(1f).pointerInput(Unit) {
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
        value =
            selectedDate?.let { convertMillisToDate(it) }
                ?: initialDate?.let { convertMillisToDate(it) }
                ?: "",
        readOnly = true,
        onValueChange = {},
        trailingIcon = {
          Icon(
              Icons.Default.DateRange,
              contentDescription = context.getString(R.string.event_edit_date_picker_desc))
        },
        placeholder = { Text(context.getString(R.string.event_creation_placeholder_date_input)) },
        label = { Text(dateString) })
    Spacer(modifier = Modifier.weight(0.05f))
    OutlinedTextField(
        modifier =
            Modifier.testTag(timeFieldTestTag).weight(1f).pointerInput(Unit) {
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
        value =
            selectedTime?.let { convertMillisToTime(it) }
                ?: initialTime?.let { convertMillisToTime(it) }
                ?: "",
        readOnly = true,
        onValueChange = {},
        trailingIcon = {
          Icon(
              Icons.Default.AccessTime,
              contentDescription = context.getString(R.string.event_edit_time_picker_desc))
        },
        placeholder = { Text(context.getString(R.string.event_creation_placeholder_time_input)) },
        label = { Text(timeString) })
  }

  if (isDatePickerVisible) {
    DatePickerModal(
        modifier = Modifier.testTag(datePickerTestTag),
        onDateSelected = {
          selectedDate = it
          isDatePickerVisible = false
        },
        onDismiss = { isDatePickerVisible = false })
  }

  if (isTimePickerVisible) {
    TimePickerModal(
        modifier = Modifier.testTag(timePickerTestTag),
        onTimeSelected = {
          selectedTime = it
          isTimePickerVisible = false
        },
        onDismiss = { isTimePickerVisible = false })
  }

  // Allows to only partially fill the date and time fields
  if (selectedDate != null && selectedTime != null) {
    onTimestamp(Timestamp(Date(selectedDate!! + selectedTime!!)))
  } else if (selectedDate != null && initialTime != null) {
    onTimestamp(Timestamp(Date(selectedDate!! + initialTime)))
  } else if (initialDate != null && selectedTime != null) {
    onTimestamp(Timestamp(Date(initialDate + selectedTime!!)))
  }
}

/**
 * Composable for the date picker modal that allows the user to select a date.
 *
 * @param onDateSelected (Long?) -> Unit : Lambda that is called when a date is selected.
 * @param onDismiss () -> Unit : Lambda that is called when the modal is dismissed.
 * @param modifier Modifier : Modifier for the date picker modal.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DatePickerModal(
    onDateSelected: (Long?) -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
  val datePickerState = rememberDatePickerState()
  val context = LocalContext.current

  DatePickerDialog(
      modifier = modifier,
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

/**
 * Composable for the time picker modal that allows the user to select a time.
 *
 * @param onTimeSelected (Long?) -> Unit : Lambda that is called when a time is selected.
 * @param onDismiss () -> Unit : Lambda that is called when the modal is dismissed.
 * @param modifier Modifier : Modifier for the time picker modal.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TimePickerModal(onTimeSelected: (Long?) -> Unit, onDismiss: () -> Unit, modifier: Modifier) {
  val timePickerState = rememberTimePickerState(is24Hour = true)

  TimePickerDialog(
      modifier = modifier,
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
 *
 * @param onDismiss: () -> Unit: Lambda that is called when the dialog is dismissed.
 * @param onConfirm: () -> Unit: Lambda that is called when the dialog is confirmed.
 * @param modifier: Modifier: Modifier for the dialog.
 * @param content: @Composable () -> Unit: Content of the dialog.
 */
@Composable
fun TimePickerDialog(
    onDismiss: () -> Unit,
    onConfirm: () -> Unit,
    modifier: Modifier,
    content: @Composable () -> Unit
) {
  val context = LocalContext.current
  AlertDialog(
      modifier = modifier,
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

/**
 * Converts milliseconds to a date string in the format "dd/MM/yy".
 *
 * @param millis: Long: Milliseconds to convert.
 * @return String: Date string in the format "dd/MM/yy".
 */
fun convertMillisToDate(millis: Long): String {
  val formatter = SimpleDateFormat(DAY_MONTH_YEAR_FORMAT, Locale.getDefault())
  return formatter.format(Date(millis))
}

/**
 * Converts milliseconds to a time string in the format "HH:mm".
 *
 * @param millis: Long: Milliseconds to convert.
 * @return String: Time string in the format "HH:mm".
 */
fun convertMillisToTime(millis: Long): String {
  val formatter = SimpleDateFormat(HOUR_MINUTE_FORMAT, Locale.getDefault())
  return formatter.format(Date(millis))
}

/**
 * Returns the time of the hours and minutes component in milliseconds from the timestamp in seconds
 *
 * @param timestamp: Timestamp
 * @return Long how many milliseconds have passed since the beginning of the day
 */
fun getHHMMInMillisFromTimestamp(timestamp: Timestamp): Long {
  return ((timestamp.toDate().hours - 1) * 60 + timestamp.toDate().minutes) * 60 * 1000L
}
