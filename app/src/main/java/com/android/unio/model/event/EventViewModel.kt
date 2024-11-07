package com.android.unio.model.event

import android.util.Log
import androidx.lifecycle.ViewModel
import com.android.unio.model.image.ImageRepository
import com.android.unio.model.user.UserRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import java.io.InputStream
import javax.inject.Inject

@HiltViewModel
open class EventViewModel
@Inject
constructor(
    val repository: EventRepository,
    val userRepository: UserRepository,
    val imageRepository: ImageRepository
) : ViewModel() {

  /** Add a new event to the repository. It uploads the event image first, then adds the event. */
  fun addEvent(
      inputStream: InputStream,
      event: Event,
      onSuccess: () -> Unit,
      onFailure: (Exception) -> Unit
  ) {
    imageRepository.uploadImage(
        inputStream,
        "images/events/${event.uid}",
        { uri ->
          event.image = uri
          event.uid = repository.getNewUid()
          repository.addEvent(event, onSuccess, onFailure)
        },
        { e -> Log.e("ImageRepository", "Failed to store image: $e") })
  }
}
