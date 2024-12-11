package com.android.unio.model.image

import android.util.Log
import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import java.io.InputStream
import javax.inject.Inject

@HiltViewModel
class ImageViewModel @Inject constructor(private val repository: ImageRepository) : ViewModel() {

  /**
   * Uploads an image stream to Firebase Storage.
   *
   * @param path The path to the image in Firebase Storage.
   * @param onSuccess The callback that is called when the operation is successful.
   * @param onFailure The callback that is called when the operation fails.
   */
  fun uploadImage(
      inputStream: InputStream,
      path: String,
      onSuccess: (String) -> Unit,
      onFailure: () -> Unit
  ) {
    repository.uploadImage(inputStream, path, onSuccess) { exception ->
      Log.e("ImageViewModel", "Error uploading image: $exception")
      onFailure()
    }
  }
  /**
   * Deletes an image from Firebase Storage.
   *
   * @param path The path to the image in Firebase Storage.
   * @param onSuccess The callback that is called when the operation is successful.
   * @param onFailure The callback that is called when the operation fails.
   */
  // TODO: add test
  fun deleteImage(path: String, onSuccess: () -> Unit, onFailure: () -> Unit) {
    repository.deleteImage(path, onSuccess) { exception ->
      Log.e("ImageViewModel", "Error deleting image: $exception")
      onFailure()
    }
  }
}
