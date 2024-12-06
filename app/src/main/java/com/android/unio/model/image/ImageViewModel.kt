package com.android.unio.model.image

import android.util.Log
import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import java.io.InputStream
import javax.inject.Inject

@HiltViewModel
class ImageViewModel @Inject constructor(private val repository: ImageRepository) : ViewModel() {
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
}
