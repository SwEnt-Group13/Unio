package com.android.unio.model.event

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.android.unio.model.image.ImageRepositoryFirebaseStorage
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.io.InputStream

/**
 * ViewModel class that manages the event list data and provides it to the UI. It uses an
 * [EventRepository] to load the list of events and exposes them through a [StateFlow] to be
 * observed by the UI.
 *
 * @property repository The [EventRepository] that provides the events.
 */
class EventListViewModel(private val repository: EventRepository) : ViewModel() {

  /**
   * A private mutable state flow that holds the list of events. It is internal to the ViewModel and
   * cannot be modified from the outside.
   */
  private val _events = MutableStateFlow<List<Event>>(emptyList())

  /**
   * A public immutable [StateFlow] that exposes the list of events to the UI. This flow can only be
   * observed and not modified.
   */
  val events: StateFlow<List<Event>> = _events
  private val imageRepository = ImageRepositoryFirebaseStorage()

  /** Initializes the ViewModel by loading the events from the repository. */
  init {
    repository.init { loadEvents() }
  }

  /**
   * Loads the list of events from the repository asynchronously using coroutines and updates the
   * internal [MutableStateFlow].
   */
  private fun loadEvents() {
    // Launch a coroutine in the ViewModel scope to load events asynchronously
    viewModelScope.launch {
      repository.getEvents(
          onSuccess = { eventList ->
            _events.value = eventList // Update the state flow with the loaded events
          },
          onFailure = { exception ->
            // Handle error (e.g., log it, show a message to the user)
            Log.e("EventViewModel", "An error occured while loading events :$exception")
            _events.value = emptyList() // Clear events on failure or handle accordingly
          })
    }
  }

    //TODO: test and comment
    fun findEventById(id: String): Event? {
        _events.value
            .find { it.uid == id }
            ?.let {
                return it
            } ?: return null
    }

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
        { e -> Log.e("ImageRepository", "Failed to store image : $e") })
  }

  /**
   * Companion object that provides a factory for creating instances of [EventListViewModel]. This
   * factory is used to create the ViewModel with the [EventRepositoryMock] dependency.
   */
  companion object {
    /** A factory for creating [EventListViewModel] instances with the [EventRepositoryMock]. */
    val Factory: ViewModelProvider.Factory =
        object : ViewModelProvider.Factory {
          /**
           * Creates an instance of the [EventListViewModel].
           *
           * @param modelClass The class of the ViewModel to create.
           * @return The created ViewModel instance.
           * @throws IllegalArgumentException if the [modelClass] does not match.
           */
          @Suppress("UNCHECKED_CAST")
          override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return EventListViewModel(EventRepositoryMock()) as T
          }
        }
  }
}
