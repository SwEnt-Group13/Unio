package com.android.unio.model.event

import android.util.Log
import androidx.lifecycle.ViewModel
import com.android.unio.model.association.AssociationRepository
import com.android.unio.model.image.ImageRepository
import com.android.unio.model.strings.StoragePathsStrings
import dagger.hilt.android.lifecycle.HiltViewModel
import java.io.InputStream
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * ViewModel class that manages the event list data and provides it to the UI. It uses an
 * [EventRepository] to load the list of events and exposes them through a [StateFlow] to be
 * observed by the UI. It also exposes a [StateFlow] of the selected event to be observed by the UI.
 *
 * @property repository The [EventRepository] that provides the events.
 * @property imageRepository The [ImageRepository] that provides the images.
 * @property associationRepository The [AssociationRepository] that provides the associations.
 */
@HiltViewModel
class EventViewModel
@Inject
constructor(
    private val repository: EventRepository,
    private val imageRepository: ImageRepository,
    private val associationRepository: AssociationRepository,
    private val eventUserPictureRepository: EventUserPictureRepository,
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
   * Updates the selected event in the ViewModel.
   *
   * @param eventId the ID of the event to select.
   * @param loadPictures if true, loads the user pictures from firestore
   */
  fun selectEvent(eventId: String, loadPictures: Boolean = false) {
    _selectedEvent.value =
        findEventById(eventId).also {
          it?.taggedAssociations?.requestAll()
          if (loadPictures) {
            it?.eventPictures?.requestAll()
          }
        }
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
   *
   * @param inputStream The input stream of the image to upload.
   * @param event The event to add.
   * @param onSuccess A callback that is called when the event is successfully added.
   * @param onFailure A callback that is called when an error occurs while adding the event.
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
        StoragePathsStrings.EVENT_IMAGES + event.uid,
        { uri ->
          event.image = uri
          repository.addEvent(event, onSuccess, onFailure)
        },
        { e -> Log.e("ImageRepository", "Failed to store image: $e") })

    event.organisers.requestAll({
      event.organisers.list.value.forEach {
        it.events.add(event.uid)
        associationRepository.saveAssociation(
            isNewAssociation = false,
            it,
            { it.events.requestAll() },
            { e -> Log.e("EventViewModel", "An error occurred while loading associations: $e") })
      }
    })
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
        // no need to delete the old image as it will be replaced by the new one
        StoragePathsStrings.EVENT_IMAGES + event.uid,
        { uri ->
          event.image = uri
          repository.addEvent(event, onSuccess, onFailure)
        },
        { e -> Log.e("ImageRepository", "Failed to store image: $e") })

    event.organisers.requestAll({
      event.organisers.list.value.forEach {
        if (it.events.contains(event.uid)) it.events.remove(event.uid)
        it.events.add(event.uid)
        associationRepository.saveAssociation(
            isNewAssociation = false,
            it,
            {},
            { e -> Log.e("EventViewModel", "An error occurred while loading associations: $e") })
        it.events.requestAll()
      }
    })

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

    event.organisers.requestAll({
      event.organisers.list.value.forEach {
        if (it.events.contains(event.uid)) it.events.remove(event.uid)
        it.events.add(event.uid)
        associationRepository.saveAssociation(
            isNewAssociation = false,
            it,
            {},
            { e -> Log.e("EventViewModel", "An error occurred while loading associations: $e") })
        it.events.requestAll()
      }
    })

    _events.value = _events.value.filter { it.uid != event.uid } // Remove the outdated event
    _events.value += event
  }

  /**
   * Deletes an event from the repository.
   *
   * @param event The event to delete.
   * @param onSuccess A callback that is called when the event is successfully deleted.
   * @param onFailure A callback that is called when an error occurs while deleting the event.
   */
  fun deleteEvent(event: Event, onSuccess: () -> Unit, onFailure: (Exception) -> Unit) {
    repository.deleteEventById(
        event.uid,
        onSuccess = {
          _events.value = _events.value.filter { it.uid != event.uid }
          onSuccess()
        },
        onFailure = { exception ->
          Log.e("EventViewModel", "An error occurred while deleting event: $exception")
          onFailure(exception)
        })
    if (event.image.isNotBlank()) {
      imageRepository.deleteImage(
          StoragePathsStrings.EVENT_IMAGES + event.uid,
          {},
          { exception ->
            Log.e("EventViewModel", "An error occured while deleting event banner: $exception")
          })
    }

    event.eventPictures.uids.forEach { uid ->
      imageRepository.deleteImage(
          StoragePathsStrings.EVENT_USER_PICTURES + uid,
          {
            eventUserPictureRepository.deleteEventUserPictureById(
                uid,
                {},
                { exception ->
                  Log.e(
                      "EventViewModel",
                      "An error occured while deleting event's user pictures from Firestore: $exception")
                })
          },
          { exception ->
            Log.e(
                "EventViewModel",
                "An error occured while deleting event's user pictures from Firebase Storage: $exception")
          })
    }

    event.organisers.requestAll({
      event.organisers.list.value.forEach {
        it.events.remove(event.uid)
        associationRepository.saveAssociation(
            isNewAssociation = false,
            it,
            {},
            { e -> Log.e("EventViewModel", "An error occurred while loading associations: $e") })
        it.events.requestAll()
      }
    })
  }

  /**
   * Add an EventUserPicture to the database and updates the related event.
   *
   * @param pictureInputStream The inputStream of the image to add.
   * @param event The event the picture is related to.
   * @param picture The EventUserPicture object, with number of likes and author.
   */
  fun addEventUserPicture(
      pictureInputStream: InputStream,
      event: Event,
      picture: EventUserPicture
  ) {
    val picId = eventUserPictureRepository.getNewUid()
    imageRepository.uploadImage(
        pictureInputStream,
        StoragePathsStrings.EVENT_USER_PICTURES + picId,
        onSuccess = { imageUri ->
          val newEventPicture = picture.copy(uid = picId, image = imageUri)
          eventUserPictureRepository.addEventUserPicture(
              newEventPicture,
              {
                event.eventPictures.add(newEventPicture.uid)
                updateEventWithoutImage(
                    event,
                    { event.eventPictures.requestAll(lazy = true) },
                    { e ->
                      Log.e("EventViewModel", "An error occurred while updating an event: $e")
                    })
              },
              { e ->
                Log.e("EventViewModel", "An error occurred while adding an event picture: $e")
              })
        },
        onFailure = { e -> Log.e("ImageRepository", "Failed to store image: $e") })
  }
}
