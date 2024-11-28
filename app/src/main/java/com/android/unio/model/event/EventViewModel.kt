package com.android.unio.model.event

import android.util.Log
import androidx.lifecycle.ViewModel
import com.android.unio.model.association.AssociationRepository
import com.android.unio.model.image.ImageRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import java.io.InputStream
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * ViewModel class that manages the event list data and provides it to the UI. It uses an
 * [EventRepository] to load the list of events and exposes them through a [StateFlow] to be
 * observed by the UI.
 *
 * @property repository The [EventRepository] that provides the events.
 */
@HiltViewModel
class EventViewModel
@Inject
constructor(
    private val repository: EventRepository,
    private val imageRepository: ImageRepository,
    private val associationRepository: AssociationRepository
) : ViewModel() {

  /**
   * A private mutable state flow that holds the list of events. It is internal to the ViewModel and
   * cannot be modified from the outside.
   */
  private val _events = MutableStateFlow<List<Event>>(emptyList())

  /**
   * A public immutable [StateFlow] that exposes the list of events to the UI. This flow can only be
   * observed and not modified.
   */
  val events: StateFlow<List<Event>> = _events.asStateFlow()

  private val _selectedEvent = MutableStateFlow<Event?>(null)

  val selectedEvent: StateFlow<Event?> = _selectedEvent.asStateFlow()

  init {
    repository.init { loadEvents() }
  }

  /** Loads the list of events from the repository and updates the internal [MutableStateFlow]. */
  fun loadEvents() {
    repository.getEvents(
        onSuccess = { eventList ->
          eventList.forEach { event -> event.organisers.requestAll() }
          _events.value = eventList
        },
        onFailure = { exception ->
          Log.e("EventViewModel", "An error occurred while loading events: $exception")
          _events.value = emptyList()
        })
  }

  /**
   * Selects an event given its id.
   *
   * @param eventId the ID of the event to select.
   */
  fun selectEvent(eventId: String) {
    _selectedEvent.value = findEventById(eventId).also { it?.taggedAssociations?.requestAll() }
  }

  /**
   * Finds an event in the event list by its ID.
   *
   * @param id The ID of the event to find.
   * @return The event with the given ID, or null if no such event exists.
   */
  fun findEventById(id: String): Event? {
    return _events.value.find { it.uid == id }
  }

  /**
   * Add a new event to the repository. It uploads the event image first, then adds the event. It
   * then adds it to the _events stateflow
   */
  fun addEvent(
      inputStream: InputStream,
      event: Event,
      onSuccess: () -> Unit,
      onFailure: (Exception) -> Unit
  ) {
    event.uid = repository.getNewUid() // Generate a new UID for the event
    imageRepository.uploadImage(
        inputStream,
        "images/events/${event.uid}",
        { uri ->
          event.image = uri
          repository.addEvent(event, onSuccess, onFailure)
        },
        { e -> Log.e("ImageRepository", "Failed to store image: $e") })
    associationRepository.getAssociations(
        onSuccess = { associationList ->
          associationList
              .filter { event.organisers.contains(it.uid) }
              .forEach {
                it.events.add(event.uid)
                associationRepository.saveAssociation(it, {}, {})
              }
        },
        onFailure = { exception ->
          Log.e("EventViewModel", "An error occurred while loading associations: $exception")
        })
    event.organisers.requestAll(onSuccess)
    _events.value += event
  }

  /**
   * Update an existing event in the repository with a new image. It uploads the event image first,
   * then updates the event.
   *
   * @param inputStream The input stream of the image to upload.
   * @param event The event to update.
   * @param onSuccess A callback that is called when the event is successfully updated.
   * @param onFailure A callback that is called when an error occurs while updating the event.
   */
  fun updateEvent(
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
          repository.addEvent(event, onSuccess, onFailure)
        },
        { e -> Log.e("ImageRepository", "Failed to store image: $e") })
    associationRepository.getAssociations(
        onSuccess = { associationList ->
          associationList
              .filter { event.organisers.contains(it.uid) }
              .forEach {
                it.events.add(event.uid)
                associationRepository.saveAssociation(it, {}, {})
              }
        },
        onFailure = { exception ->
          Log.e("EventViewModel", "An error occurred while loading associations: $exception")
        })
    event.organisers.requestAll(onSuccess)
    _events.value = _events.value.filter { it.uid != event.uid } // Remove the outdated event
    _events.value += event
  }

  /**
   * Update an existing event in the repository without updating its image.
   *
   * @param event The event to update.
   * @param onSuccess A callback that is called when the event is successfully updated.
   * @param onFailure A callback that is called when an error occurs while updating the event.
   */
  fun updateEventWithoutImage(event: Event, onSuccess: () -> Unit, onFailure: (Exception) -> Unit) {
    repository.addEvent(event, onSuccess, onFailure)
    event.organisers.requestAll(onSuccess)
    associationRepository.getAssociations(
        onSuccess = { associationList ->
          associationList
              .filter { event.organisers.contains(it.uid) }
              .forEach {
                it.events.add(event.uid)
                associationRepository.saveAssociation(it, {}, {})
              }
        },
        onFailure = { exception ->
          Log.e("EventViewModel", "An error occurred while loading associations: $exception")
        })
    _events.value = _events.value.filter { it.uid != event.uid } // Remove the outdated event
    _events.value += event
  }

  /**
   * Deletes an event from the repository.
   *
   * @param eventId The UID of the event to delete.
   * @param onSuccess A callback that is called when the event is successfully deleted.
   * @param onFailure A callback that is called when an error occurs while deleting the event.
   */
  fun deleteEvent(eventId: String, onSuccess: () -> Unit, onFailure: (Exception) -> Unit) {
    repository.deleteEventById(
        eventId,
        onSuccess = {
          _events.value = _events.value.filter { it.uid != eventId }
          onSuccess()
        },
        onFailure = { exception ->
          Log.e("EventViewModel", "An error occurred while deleting event: $exception")
        })
  }
}
