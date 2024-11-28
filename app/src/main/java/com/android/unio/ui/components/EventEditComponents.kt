package com.android.unio.ui.components

import android.net.Uri
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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import coil.compose.rememberAsyncImagePainter
import com.android.unio.R
import com.android.unio.model.association.Association
import com.android.unio.model.strings.FormatStrings.DAY_MONTH_YEAR_FORMAT
import com.android.unio.model.strings.FormatStrings.HOUR_MINUTE_FORMAT
import com.google.firebase.Timestamp
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

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

@Composable
fun DateAndTimePicker(
    dateString: String,
    timeString: String,
    modifier: Modifier,
    initialDate: String,
    initialTime: String,
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
        value = selectedDate?.let { convertMillisToDate(it) } ?: initialDate,
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
        value = selectedTime?.let { convertMillisToTime(it) } ?: initialTime,
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
fun DatePickerModal(onDateSelected: (Long?) -> Unit, onDismiss: () -> Unit) {
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
fun TimePickerDialog(
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
