package com.android.unio.model.event

import android.util.Log
import androidx.lifecycle.ViewModel
import com.android.unio.model.image.ImageRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.io.InputStream
import javax.inject.Inject

class EventUserPictureViewModel @Inject
constructor(
    private val repository: EventUserPictureRepository,
    private val imageRepository: ImageRepository
) : ViewModel() {
    private val _eventPictures = MutableStateFlow<List<EventUserPicture>>(emptyList())
    val eventPictures: StateFlow<List<EventUserPicture>> = _eventPictures.asStateFlow()

    fun addEventUserPicture(
        inputStream: InputStream,
        eventUserPicture: EventUserPicture,
        onSuccess: () -> Unit,
        onFailure: (Exception) -> Unit
    ) {
        eventUserPicture.uid = repository.getNewUid()
        imageRepository.uploadImage(
            inputStream,
            "images/eventPictures/${eventUserPicture.uid}",
            { uri ->
                eventUserPicture.image = uri
                    repository.addEventUserPicture(eventUserPicture, onSuccess, onFailure)
            },
            { e -> Log.e("ImageRepository", "Failed to store image: $e") })
    }
}