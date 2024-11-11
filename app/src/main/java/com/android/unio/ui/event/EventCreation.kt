package com.android.unio.ui.event

import android.annotation.SuppressLint
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
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TimeInput
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
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
import com.android.unio.model.strings.test_tags.EventCreationTestTags
import com.android.unio.ui.navigation.NavigationAction
import com.android.unio.ui.theme.AppTypography
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@Composable
fun EventCreationScreen(navigationAction: NavigationAction) {
  val context = LocalContext.current
  var name by remember { mutableStateOf("") }
  var shortDescription by remember { mutableStateOf("") }
  var longDescription by remember { mutableStateOf("") }

  Scaffold(
      modifier = Modifier.testTag(EventCreationTestTags.SCREEN),
      content = { padding ->
        Column(
            modifier =
                Modifier.padding(padding)
                    .padding(horizontal = 20.dp, vertical = 20.dp)
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(6.dp),
            horizontalAlignment = CenterHorizontally) {
              Row(
                  modifier = Modifier.fillMaxWidth(),
                  verticalAlignment = Alignment.CenterVertically,
                  horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    IconButton(onClick = { navigationAction.goBack() }) {
                      Icon(
                          Icons.AutoMirrored.Filled.ArrowBack,
                          contentDescription =
                              context.getString(R.string.event_creation_cancel_button))
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
                  modifier =
                      Modifier.fillMaxWidth().testTag(EventCreationTestTags.SHORT_DESCRIPTION),
                  value = shortDescription,
                  onValueChange = { shortDescription = it },
                  label = {
                    Text(context.getString(R.string.event_creation_short_description_label))
                  })

              BannerImagePicker()

              OutlinedTextField(
                  modifier =
                      Modifier.fillMaxWidth().testTag(EventCreationTestTags.COAUTHORS).clickable {
                        Toast.makeText(
                                context, "Coauthors are not implemented yet", Toast.LENGTH_SHORT)
                            .show()
                      },
                  value = "",
                  readOnly = true,
                  onValueChange = {},
                  label = { Text(context.getString(R.string.event_creation_coauthors_label)) })

              OutlinedTextField(
                  modifier =
                      Modifier.fillMaxWidth()
                          .testTag(EventCreationTestTags.TAGGED_ASSOCIATIONS)
                          .clickable {
                            Toast.makeText(
                                    context,
                                    "Tagged Associations are not implemented yet",
                                    Toast.LENGTH_SHORT)
                                .show()
                          },
                  value = "",
                  readOnly = true,
                  onValueChange = {},
                  label = { Text(context.getString(R.string.event_creation_tagged_label)) })

              OutlinedTextField(
                  modifier = Modifier.fillMaxWidth().testTag(EventCreationTestTags.DESCRIPTION),
                  value = longDescription,
                  onValueChange = { longDescription = it },
                  label = { Text(context.getString(R.string.event_creation_description_label)) })

              DateAndTimePicker(
                  context.getString(R.string.event_creation_startdate_label),
                  context.getString(R.string.event_creation_starttime_label),
                  modifier = Modifier.testTag(EventCreationTestTags.START_TIME))

              DateAndTimePicker(
                  context.getString(R.string.event_creation_enddate_label),
                  context.getString(R.string.event_creation_endtime_label),
                  modifier = Modifier.testTag(EventCreationTestTags.END_TIME))

              OutlinedTextField(
                  modifier =
                      Modifier.fillMaxWidth().testTag(EventCreationTestTags.LOCATION).clickable {
                        Toast.makeText(
                                context, "Location is not implemented yet", Toast.LENGTH_SHORT)
                            .show()
                      },
                  value = "",
                  onValueChange = {},
                  label = { Text(context.getString(R.string.event_creation_location_label)) })

              Spacer(modifier = Modifier.width(10.dp))

              Button(
                  modifier = Modifier.testTag(EventCreationTestTags.SAVE_BUTTON),
                  onClick = { navigationAction.goBack() }) {
                    Text(context.getString(R.string.event_creation_save_button))
                  }
            }
      })
}

@Composable
fun BannerImagePicker() {
  val context = LocalContext.current
  var eventBanner by remember { mutableStateOf(Uri.EMPTY) }

  val pickMedia =
      rememberLauncherForActivityResult(
          contract = ActivityResultContracts.PickVisualMedia(),
          onResult = { uri: Uri? -> uri?.let { eventBanner = it } })

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
        if (eventBanner != Uri.EMPTY) {
          Image(
              painter = rememberAsyncImagePainter(eventBanner),
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
fun DateAndTimePicker(dateString: String, timeString: String, modifier: Modifier) {
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
  val formatter = SimpleDateFormat("dd/MM/yy", Locale.getDefault())
  return formatter.format(Date(millis))
}

fun convertMillisToTime(millis: Long): String {
  val formatter = SimpleDateFormat("HH:mm", Locale.getDefault())
  return formatter.format(Date(millis))
}
