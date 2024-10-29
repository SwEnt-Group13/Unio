package com.android.unio.model.event

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.android.unio.model.image.ImageRepositoryFirebaseStorage
import com.android.unio.model.user.UserRepository
import com.android.unio.model.user.UserRepositoryFirestore
import com.google.firebase.Firebase
import com.google.firebase.firestore.firestore
import java.io.InputStream

open class EventViewModel(val repository: EventRepository, val userRepository: UserRepository) :
    ViewModel() {

  companion object {
    val Factory: ViewModelProvider.Factory =
        object : ViewModelProvider.Factory {
          @Suppress("UNCHECKED_CAST")
          override fun <T : ViewModel> create(modelClass: Class<T>): T {
            // Check if the requested model class is EventViewModel
            if (modelClass.isAssignableFrom(EventViewModel::class.java)) {
              return EventViewModel(
                  EventRepositoryFirestore(Firebase.firestore),
                  UserRepositoryFirestore(Firebase.firestore))
                  as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
          }
        }
  }

  /** Add a new event to the repository. It uploads the event image first, then adds the event. */
  fun addEvent(
      inputStream: InputStream,
      event: Event,
      onSuccess: () -> Unit,
      onFailure: (Exception) -> Unit
  ) {
    ImageRepositoryFirebaseStorage()
        .uploadImage(
            inputStream,
            "images/events/${event.uid}",
            { uri ->
              event.image = uri
              event.uid = repository.getNewUid()
              repository.addEvent(event, onSuccess, onFailure)
            },
            { e -> Log.e("ImageRepository", "Failed to store image: $e") })
        ?: Log.e("EventListViewModel", "ImageRepository is not available in preview mode.")
  }
}
