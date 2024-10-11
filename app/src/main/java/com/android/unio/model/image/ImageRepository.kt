package com.android.unio.model.image

import java.io.InputStream

interface ImageRepository {

  fun uploadImage(
      imageStream: InputStream,
      firebasePath: String,
      onSuccess: (String) -> Unit,
      onFailure: (Exception) -> Unit
  )
}
