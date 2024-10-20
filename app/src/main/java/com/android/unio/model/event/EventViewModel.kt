package com.android.unio.model.event

import android.util.Log
import androidx.lifecycle.ViewModel
import com.android.unio.model.image.ImageRepositoryFirebaseStorage
import com.android.unio.model.user.UserRepositoryFirestore
import com.google.firebase.auth.FirebaseAuth
import java.io.InputStream

class EventViewModel(private val repository: EventRepository, private val userRepository: UserRepositoryFirestore) : ViewModel() {

    // Image repository - avoid initializing in preview mode
    private val imageRepository by lazy {
        if (!isPreviewMode()) ImageRepositoryFirebaseStorage() else null
    }

    /**
     * Helper function to determine if the app is running in preview mode.
     */
    private fun isPreviewMode(): Boolean {
        // Check if the current environment is a preview (used in Compose Previews)
        return android.os.Build.FINGERPRINT.contains("generic") ||
                android.os.Build.FINGERPRINT.contains("emulator") ||
                android.os.Build.MODEL.contains("sdk_gphone")
    }

    /**
     * Add a new event to the repository. It uploads the event image first, then adds the event.
     */
    fun addEvent(
        inputStream: InputStream,
        event: Event,
        onSuccess: () -> Unit,
        onFailure: (Exception) -> Unit
    ) {
        imageRepository?.uploadImage(
            inputStream,
            "images/events/${event.uid}",
            { uri ->
                event.image = uri
                event.uid = repository.getNewUid()
                repository.addEvent(event, onSuccess, onFailure)
            },
            { e -> Log.e("ImageRepository", "Failed to store image: $e") }
        ) ?: Log.e("EventListViewModel", "ImageRepository is not available in preview mode.")
    }

    
}