package com.android.unio.model.image

import com.android.unio.model.firestore.performFirestoreOperation
import com.google.firebase.storage.FirebaseStorage
import java.io.InputStream
import javax.inject.Inject

class ImageRepositoryFirebaseStorage @Inject constructor(storage: FirebaseStorage) :
    ImageRepository {

  private val storageRef = storage.reference

  /**
   * Helper function that gets the downloadUrl of an image. Is used after calling uploadImage.
   *
   * @param firebasePath The path to the image in Firebase Storage.
   * @param onSuccess The callback that is called when the operation is successful.
   * @param onFailure The callback that is called when the operation fails.
   */
  override fun getImageUrl(
      firebasePath: String,
      onSuccess: (String) -> Unit,
      onFailure: (Exception) -> Unit
  ) {
    val pathReference = storageRef.child(firebasePath)

    pathReference.downloadUrl.performFirestoreOperation(
        onSuccess = { url -> onSuccess(url.toString()) }, onFailure = { e -> onFailure(e) })
  }

  /**
   * Uploads an image stream to Firebase Storage. Gives a downloadUrl to onSuccess.
   *
   * @param imageStream The image stream to upload.
   * @param firebasePath The path to the image in Firebase Storage.
   * @param onSuccess The callback that is called when the operation is successful.
   * @param onFailure The callback that is called when the operation fails.
   */
  override fun uploadImage(
      imageStream: InputStream,
      firebasePath: String,
      onSuccess: (String) -> Unit,
      onFailure: (Exception) -> Unit
  ) {
    val path = storageRef.child(firebasePath)
    val uploadTask = path.putStream(imageStream)

    uploadTask.performFirestoreOperation(
        onSuccess = { getImageUrl(firebasePath, onSuccess, onFailure) },
        onFailure = { e -> onFailure(e) })
  }

  /**
   * Deletes an image from Firebase Storage.
   *
   * @param firebasePath The path to the image in Firebase Storage.
   * @param onSuccess The callback that is called when the operation is successful.
   * @param onFailure The callback that is called when the operation fails.
   */
  override fun deleteImage(
      firebasePath: String,
      onSuccess: () -> Unit,
      onFailure: (Exception) -> Unit
  ) {
    val path = storageRef.child(firebasePath)

    path.delete().addOnCompleteListener { task ->
      if (task.isSuccessful) {
        onSuccess()
      } else {
        onFailure(task.exception!!)
      }
    }
  }
}
