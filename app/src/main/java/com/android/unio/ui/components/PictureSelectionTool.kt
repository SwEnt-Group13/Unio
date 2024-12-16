package com.android.unio.ui.components

import android.content.ContentValues
import android.net.Uri
import android.provider.MediaStore
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import coil.compose.rememberAsyncImagePainter
import com.android.unio.R
import com.android.unio.model.strings.test_tags.authentication.PictureSelectionToolTestTags

/**
 * A composable function to select and display pictures from the gallery or camera. It allows the
 * user to add pictures, see selected pictures in a grid, and validate the selection.
 *
 * @param maxPictures The maximum number of pictures that can be selected.
 * @param allowGallery Boolean to specify if selecting from the gallery is allowed.
 * @param allowCamera Boolean to specify if taking pictures with the camera is allowed.
 * @param onValidate Lambda to handle the selected pictures after validation.
 * @param onCancel Lambda to handle the cancellation of the selection.
 */
@Composable
fun PictureSelectionTool(
    maxPictures: Int,
    allowGallery: Boolean,
    allowCamera: Boolean,
    onValidate: (List<Uri>) -> Unit,
    onCancel: () -> Unit,
    initialSelectedPictures: List<Uri>
) {
  val context = LocalContext.current

  val selectedPictures = remember { mutableStateListOf<Uri>() }

  // Initialize selectedPictures with initialSelectedPictures
  LaunchedEffect(initialSelectedPictures) {
    selectedPictures.clear() // Clear existing pictures, if any
    selectedPictures.addAll(initialSelectedPictures) // Add the initial selected pictures
  }

  // Launcher for selecting multiple images from the gallery
  val pickMediaLauncher =
      rememberLauncherForActivityResult(ActivityResultContracts.PickMultipleVisualMedia()) { uris ->
        if (uris != null) {
          selectedPictures.addAll(uris.take(maxPictures - selectedPictures.size))
        }
      }

  val cameraImageUri = remember { mutableStateOf<Uri?>(null) }
  // Launcher for taking a picture with the camera
  val takePictureLauncher =
      rememberLauncherForActivityResult(ActivityResultContracts.TakePicture()) { success ->
        if (success && cameraImageUri.value != null) {
          selectedPictures.add(cameraImageUri.value!!)
        }
      }

  /**
   * Creates a temporary URI for storing an image taken by the camera.
   *
   * @return A URI pointing to the location where the new image will be stored.
   */
  fun createImageUri(): Uri {
    val resolver = context.contentResolver
    val contentValues =
        ContentValues().apply {
          put(
              MediaStore.Images.Media.DISPLAY_NAME,
              PictureSelectionToolTestTags.NEW_PROFILE_PICTURE)
          put(MediaStore.Images.Media.MIME_TYPE, PictureSelectionToolTestTags.IMAGE_JPEG)
        }
    return resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues)!!
  }

  // User interface for the picture selection tool
  Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.padding(16.dp)) {
    Text(
        context.getString(R.string.selection_tool_selected_picture_text) +
            ": ${selectedPictures.size}/$maxPictures")

    // Display selected pictures in a scrollable grid with a fixed number of columns
    LazyVerticalGrid(
        columns = GridCells.Fixed(3),
        modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
        contentPadding = PaddingValues(4.dp)) {
          items(selectedPictures.size) { index ->
            val uri = selectedPictures[index]
            Box(modifier = Modifier.size(100.dp).clip(CircleShape).padding(4.dp)) {
              // Display the selected image
              Image(
                  painter = rememberAsyncImagePainter(uri),
                  contentDescription =
                      context.getString(R.string.selection_tool_selected_picture_text),
                  contentScale = ContentScale.Crop,
                  modifier =
                      Modifier.fillMaxSize().testTag(PictureSelectionToolTestTags.SELECTED_PICTURE))
              Icon(
                  Icons.Default.Close,
                  contentDescription = context.getString(R.string.selection_tool_remove_picture),
                  modifier =
                      Modifier.align(Alignment.TopEnd)
                          .clickable { selectedPictures.remove(uri) }
                          .padding(4.dp)
                          .testTag(PictureSelectionToolTestTags.REMOVE_PICTURE))
            }
          }
        }

    Spacer(modifier = Modifier.height(16.dp))

    // Buttons to add pictures from the gallery or camera
    Row(horizontalArrangement = Arrangement.SpaceEvenly, modifier = Modifier.fillMaxWidth()) {
      // Add picture from the gallery if allowed and the max limit isn't reached
      if (allowGallery && selectedPictures.size < maxPictures) {
        OutlinedButton(
            onClick = {
              pickMediaLauncher.launch(
                  PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
            },
            modifier = Modifier.testTag(PictureSelectionToolTestTags.GALLERY_ADD)) {
              Icon(
                  Icons.Default.Add,
                  contentDescription =
                      context.getString(R.string.selection_tool_add_gallery_content_description))
              Text(context.getString(R.string.selection_tool_gallery_text))
            }
      }

      // Take a picture with the camera if allowed and the max limit isn't reached
      if (allowCamera && selectedPictures.size < maxPictures) {
        OutlinedButton(
            onClick = {
              val uri = createImageUri() // Local stable reference
              cameraImageUri.value = uri
              takePictureLauncher.launch(uri)
            },
            modifier = Modifier.testTag(PictureSelectionToolTestTags.CAMERA_ADD)) {
              Icon(
                  Icons.Default.Add,
                  contentDescription = context.getString(R.string.selection_tool_take_picture))
              Text(context.getString(R.string.selection_tool_camera_text))
            }
      }
    }

    Spacer(modifier = Modifier.height(16.dp))

    // Buttons for validating or canceling the picture selection
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
      Button(
          onClick = { onValidate(selectedPictures) },
          modifier = Modifier.testTag(PictureSelectionToolTestTags.VALIDATE_BUTTON)) {
            Icon(
                Icons.Default.Check,
                contentDescription = context.getString(R.string.selection_tool_validate_text))
            Text(context.getString(R.string.selection_tool_validate_text))
          }

      OutlinedButton(
          onClick = onCancel,
          modifier = Modifier.testTag(PictureSelectionToolTestTags.CANCEL_BUTTON)) {
            Text(context.getString(R.string.selection_tool_cancel_text))
          }
    }
  }
}
