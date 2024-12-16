package com.android.unio.ui.event

import android.util.Log
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Upload
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import com.android.unio.R
import com.android.unio.model.event.Event
import com.android.unio.model.event.EventUserPicture
import com.android.unio.model.event.EventViewModel
import com.android.unio.model.firestore.emptyFirestoreReferenceList
import com.android.unio.model.strings.test_tags.event.EventDetailsTestTags
import com.android.unio.model.user.User
import com.android.unio.ui.components.PictureSelectionTool
import firestoreReferenceElementWith
import kotlinx.coroutines.launch

/**
 * A Picture picker for Event details screen
 *
 * @param event the [Event] in question
 * @param eventViewModel the [EventViewModel]
 * @param user the authenticated [User]
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EventDetailsPicturePicker(event: Event, eventViewModel: EventViewModel, user: User) {

  val context = LocalContext.current
  val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
  val scope = rememberCoroutineScope()
  var showSheet by remember { mutableStateOf(false) }

  FloatingActionButton(
      onClick = { showSheet = true },
      modifier = Modifier.testTag(EventDetailsTestTags.UPLOAD_PICTURE_BUTTON).padding(15.dp)) {
        Icon(
            imageVector = Icons.Filled.Upload,
            contentDescription = context.getString(R.string.home_content_description_map_button))
      }

  if (showSheet) {
    ModalBottomSheet(
        modifier = Modifier.testTag(EventDetailsTestTags.PICTURE_SELECTION_SHEET),
        sheetState = sheetState,
        onDismissRequest = {
          scope.launch {
            sheetState.hide()
            showSheet = false
          }
        },
        content = {
          PictureSelectionTool(
              maxPictures = 1,
              allowGallery = true,
              allowCamera = true,
              onValidate = { uris ->
                if (uris.isNotEmpty()) {
                  val uriToAdd = uris.first()
                  val inputStream = context.contentResolver.openInputStream(uriToAdd)
                  if (inputStream == null) {
                    Log.e("EventDetailsPicturePicker", "Invalid picture Uri")
                  } else {
                    val newEventPicture =
                        EventUserPicture(
                            uid = "",
                            "",
                            author = User.firestoreReferenceElementWith(user.uid),
                            User.emptyFirestoreReferenceList())
                    eventViewModel.addEventUserPicture(inputStream, event, newEventPicture)
                  }
                }
                scope.launch { sheetState.hide() }
                showSheet = false
              },
              onCancel = {
                scope.launch { sheetState.hide() }
                showSheet = false
              },
              initialSelectedPictures = emptyList())
        })
  }
}
