package com.android.unio.model.image

import android.net.Uri
import androidx.core.net.toUri
import com.google.android.gms.tasks.OnSuccessListener
import com.google.android.gms.tasks.Task
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.google.firebase.storage.UploadTask
import com.google.firebase.storage.UploadTask.TaskSnapshot
import java.io.FileInputStream
import org.junit.Before
import org.junit.Test
import org.mockito.Mock
import org.mockito.Mockito.`when`
import org.mockito.MockitoAnnotations
import org.mockito.kotlin.any

class ImageRepositoryFirebaseStorageTest {

    @Mock private lateinit var storage: FirebaseStorage

    @Mock private lateinit var storageRef: StorageReference

    @Mock private lateinit var task: Task<Uri>

    @Mock private lateinit var uri: Uri

    @Mock private lateinit var taskSnapshot: TaskSnapshot

    @Mock private lateinit var fileInputStream: FileInputStream

    @Mock private lateinit var uploadTask: UploadTask

    private lateinit var repository: ImageRepositoryFirebaseStorage

    @Before
    fun setUp() {
        MockitoAnnotations.openMocks(this)
        `when`(storage.reference).thenReturn(storageRef)
        `when`(storageRef.child(any())).thenReturn(storageRef)
        `when`(storageRef.downloadUrl).thenReturn(task)

        `when`(storageRef.putStream(fileInputStream)).thenReturn(uploadTask)
        `when`(uploadTask.addOnSuccessListener(any())).thenAnswer { invocation ->
            val callback = invocation.arguments[0] as OnSuccessListener<TaskSnapshot>
            callback.onSuccess(taskSnapshot)
            uploadTask
        }

        `when`(task.addOnSuccessListener(any())).thenAnswer { invocation ->
            val callback = invocation.arguments[0] as OnSuccessListener<Uri>
            callback.onSuccess(uri)
            task
        }

        repository = ImageRepositoryFirebaseStorage(storage)
    }

    /**
     * Asserts that uploadImage calls the right functions and returns a string that can be converted
     * to Uri format.
     */
    @Test
    fun uploadImageTest() {
        repository.uploadImage(
            fileInputStream, "images/test.jpg", { stringUrl -> stringUrl.toUri() }, { e -> throw e })
    }
}