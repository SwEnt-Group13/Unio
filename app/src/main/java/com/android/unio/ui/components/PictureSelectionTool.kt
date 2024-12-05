package com.android.unio.ui.components

import android.content.ContentValues
import android.net.Uri
import android.provider.MediaStore
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import coil.compose.rememberAsyncImagePainter

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
    onCancel: () -> Unit
) {
  val context = LocalContext.current

  val selectedPictures = remember { mutableStateListOf<Uri>() }

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
          put(MediaStore.Images.Media.DISPLAY_NAME, "new_profile_picture.jpg")
          put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg")
        }
    return resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues)!!
  }

  // User interface for the picture selection tool
  Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.padding(16.dp)) {
    Text("Selected Pictures: ${selectedPictures.size}/$maxPictures")

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
                  contentDescription = "Selected Picture",
                  contentScale = ContentScale.Crop,
                  modifier = Modifier.fillMaxSize())
              Icon(
                  Icons.Default.Close,
                  contentDescription = "Remove Picture",
                  modifier =
                      Modifier.align(Alignment.TopEnd)
                          .clickable { selectedPictures.remove(uri) }
                          .padding(4.dp))
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
            }) {
              Icon(Icons.Default.Add, contentDescription = "Add from Gallery")
              Text("Gallery")
            }
      }

      // Take a picture with the camera if allowed and the max limit isn't reached
      if (allowCamera && selectedPictures.size < maxPictures) {
        OutlinedButton(
            onClick = {
              val uri = createImageUri() // Local stable reference
              cameraImageUri.value = uri
              takePictureLauncher.launch(uri)
            }) {
              Icon(Icons.Default.Add, contentDescription = "Take Picture")
              Text("Camera")
            }
      }
    }

    Spacer(modifier = Modifier.height(16.dp))

    // Buttons for validating or canceling the picture selection
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
      Button(onClick = { onValidate(selectedPictures) }) {
        Icon(Icons.Default.Check, contentDescription = "Validate")
        Text("Validate")
      }

      OutlinedButton(onClick = onCancel) { Text("Cancel") }
    }
  }
}
