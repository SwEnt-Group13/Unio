package com.android.unio.model.image

import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.UploadTask.TaskSnapshot
import java.io.InputStream

class ImageRepositoryFirebaseStorage(private val storage: FirebaseStorage = FirebaseStorage.getInstance()) :
    ImageRepository {

    private val storageRef = storage.reference

    override fun getImageUrl(
        firebasePath: String,
        onSuccess: (String) -> Unit,
        onFailure: (Exception) -> Unit
    ) {
        val pathReference = storageRef.child(firebasePath)

        pathReference.downloadUrl.addOnSuccessListener { url -> onSuccess(url.toString()) }
            .addOnFailureListener { e -> onFailure(e) }
    }

    override fun uploadImage(
        imageStream: InputStream,
        firebasePath: String,
        onSuccess: (TaskSnapshot) -> Unit,
        onFailure: (Exception) -> Unit
    ) {
        val uploadTask = storageRef.child(firebasePath).putStream(imageStream)

        uploadTask.addOnSuccessListener { taskSnapshot -> onSuccess(taskSnapshot) }
            .addOnFailureListener { exception -> onFailure(exception) }


    }

}