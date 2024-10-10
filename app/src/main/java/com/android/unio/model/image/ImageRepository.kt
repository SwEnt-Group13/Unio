package com.android.unio.model.image

import com.google.firebase.storage.UploadTask.TaskSnapshot
import java.io.InputStream

interface ImageRepository {
    fun getImageUrl(
        firebasePath: String,
        onSuccess: (String) -> Unit,
        onFailure: (Exception) -> Unit
    )

    fun uploadImage(
        imageStream: InputStream,
        firebasePath: String,
        onSuccess: (TaskSnapshot) -> Unit,
        onFailure: (Exception) -> Unit
    )
}