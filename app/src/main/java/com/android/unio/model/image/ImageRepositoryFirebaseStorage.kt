package com.android.unio.model.image

import com.google.firebase.storage.FirebaseStorage
import java.io.InputStream

class ImageRepositoryFirebaseStorage(
    private val storage: FirebaseStorage = FirebaseStorage.getInstance()
) : ImageRepository {

  private val storageRef = storage.reference

  /** Helper function that gets the downloadUrl of an image. Is used after calling uploadImage. */
  fun getImageUrl(
      firebasePath: String,
      onSuccess: (String) -> Unit,
      onFailure: (Exception) -> Unit
  ) {
    val pathReference = storageRef.child(firebasePath)

    pathReference.downloadUrl
        .addOnSuccessListener { url -> onSuccess(url.toString()) }
        .addOnFailureListener { e -> onFailure(e) }
  }

  /** Uploads an image stream to Firebase Storage. Gives a downloadUrl to onSuccess. */
  override fun uploadImage(
      imageStream: InputStream,
      firebasePath: String,
      onSuccess: (String) -> Unit,
      onFailure: (Exception) -> Unit
  ) {
    val path = storageRef.child(firebasePath)
    val uploadTask = path.putStream(imageStream)

    uploadTask
        .addOnSuccessListener {
          getImageUrl(firebasePath, onSuccess = onSuccess, onFailure = onFailure)
        }
        .addOnFailureListener { exception -> onFailure(exception) }
  }
}
