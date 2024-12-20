package com.android.unio.model.event

import android.util.Log
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import com.android.unio.model.association.AssociationRepository
import com.android.unio.model.image.ImageRepository
import com.android.unio.model.strings.StoragePathsStrings
import com.android.unio.model.usecase.SaveUseCase
import com.android.unio.model.user.User
import com.google.firebase.Firebase
import com.google.firebase.messaging.messaging
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
    private val saveUseCase: SaveUseCase
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

  private val _refreshState = mutableStateOf(false)
  val refreshState: State<Boolean> = _refreshState

  init {
    repository.init { loadEvents() }
  }

  /** Loads the list of events from the repository and updates the internal [MutableStateFlow]. */
  fun loadEvents() {
    _refreshState.value = true
    repository.getEvents(
        onSuccess = { eventList ->
          eventList.forEach { event -> event.organisers.requestAll() }
          _events.value = eventList
          _refreshState.value = false
        },
        onFailure = { exception ->
          Log.e("EventViewModel", "An error occurred while loading events: $exception")
          _events.value = emptyList()
          _refreshState.value = false
        })
  }

  /**
   * Adds a new event or updates an existing event locally in the ViewModel's state.
   *
   * @param event The event to add or update.
   */
  fun addEditEventLocally(event: Event) {
    val existingEventIndex = _events.value.indexOfFirst { it.uid == event.uid }

    if (existingEventIndex != -1) {
      val updatedEvents = _events.value.toMutableList()
      updatedEvents[existingEventIndex] = event
      _events.value = updatedEvents
    } else {
      _events.value = _events.value + event
    }

    // if the selected event matches the updated event, refresh the selection
    if (_selectedEvent.value?.uid == event.uid) {
      _selectedEvent.value = event
    }
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

  fun refreshEvent() {
    if (_selectedEvent.value == null) {
      return
    }

    _refreshState.value = true
    repository.getEventWithId(
        _selectedEvent.value!!.uid,
        onSuccess = { fetchedEvent ->
          _selectedEvent.value = fetchedEvent
          _selectedEvent.value?.taggedAssociations?.requestAll()
          _selectedEvent.value?.organisers?.requestAll()
          _selectedEvent.value?.eventPictures?.requestAll()

          _refreshState.value = false
        },
        onFailure = { exception ->
          Log.e("EventViewModel", "Failed to fetch event", exception)
          _refreshState.value = false
        })
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
   * Adds an image to the specified event. This method uploads the image to the storage and updates
   * the event's image URL once the upload is successful.
   *
   * This method helps in associating an image with an event, allowing the event to display a visual
   * representation.
   *
   * @param inputStream The input stream of the image to upload. This is typically the raw data of
   *   the image selected by the user.
   * @param event The event to which the image will be added.
   * @param onSuccess A callback that is triggered if the image upload and event update are
   *   successful. It passes the updated event as a parameter.
   * @param onFailure A callback that is triggered if the image upload or event update fails. It
   *   passes the error that occurred as a parameter.
   */
  fun addImageToEvent(
      inputStream: InputStream,
      event: Event,
      onSuccess: (Event) -> Unit,
      onFailure: (Exception) -> Unit
  ) {
    try {
      imageRepository.uploadImage(
          inputStream,
          "${StoragePathsStrings.EVENT_IMAGES}${event.uid}",
          { uri ->
            event.image = uri
            onSuccess(event)
          },
          { error ->
            Log.e("ImageRepository", "Failed to upload image: $error")
            onFailure(error)
          })
    } catch (e: Exception) {
      Log.e("addImageToEvent", "An unexpected error occurred: $e")
      onFailure(e)
    }
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
        // No need to delete the old image as it will be replaced by the new one
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
            { e -> Log.e("EventViewModel", "An error occurred while saving associations: $e") })
        it.events.requestAll()
      }
    })

    // Update events list with the new event
    _events.value = _events.value.map { if (it.uid == event.uid) event else it }

    // Update selected event if the updated event matches the current selected one
    if (_selectedEvent.value?.uid == event.uid) {
      _selectedEvent.value = event
    }
  }

  /**
   * Update an existing event in the repository without updating its image.
   *
   * @param event The event to update.
   * @param onSuccess A callback that is called when the event is successfully updated.
   * @param onFailure A callback that is called when an error occurs while updating the event.
   */
  fun updateEventWithoutImage(
      event: Event,
      onSuccess: () -> Unit,
      onFailure: (Exception) -> Unit,
      updateAssociation: Boolean = true
  ) {
    repository.addEvent(event, onSuccess, onFailure)

    if (updateAssociation) {
      event.organisers.requestAll(
          {
            event.organisers.list.value.forEach {
              if (it.events.contains(event.uid)) it.events.remove(event.uid)
              it.events.add(event.uid)
              associationRepository.saveAssociation(
                  isNewAssociation = false,
                  it,
                  {},
                  { e ->
                    Log.e("EventViewModel", "An error occurred while loading associations: $e")
                  })
              it.events.requestAll()
            }
          },
          lazy = true)
    }

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
      picture: EventUserPicture,
      onSuccess: () -> Unit,
      onFailure: (Exception) -> Unit
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
                event.eventPictures.add(newEventPicture)
                updateEventWithoutImage(
                    event,
                    { onSuccess() },
                    { e ->
                      Log.e("EventViewModel", "An error occurred while updating an event: $e")
                    },
                    false)
              },
              { e ->
                onFailure(e)
                Log.e("EventViewModel", "An error occurred while adding an event picture: $e")
              })
        },
        onFailure = { e -> Log.e("ImageRepository", "Failed to store image: $e") })
  }

  /**
   * Update an existing eventUserPicture without updating its image.
   *
   * @param event The event in question.
   * @param picture The [EventUserPicture] to update
   */
  fun updateEventUserPictureWithoutImage(
      event: Event,
      picture: EventUserPicture,
      onSuccess: () -> Unit,
      onFailure: (Exception) -> Unit
  ) {
    eventUserPictureRepository.addEventUserPicture(
        picture,
        {
          if (!event.eventPictures.contains(picture.uid)) {
            event.eventPictures.add(picture)
          }
          updateEventWithoutImage(
              event,
              {
                _events.value = _events.value.map { if (it.uid == event.uid) event else it }
                onSuccess()
              },
              { e ->
                onFailure(e)
                Log.e("EventViewModel", "An error occurred while updating an event: $e")
              },
              false)
        },
        { e ->
          onFailure(e)
          Log.e("EventViewModel", "An error occurred while adding an event picture: $e")
        })
  }

  fun deleteEventUserPicture(
      uid: String,
      event: Event,
      onSuccess: () -> Unit,
      onFailure: (Exception) -> Unit
  ) {

    eventUserPictureRepository.deleteEventUserPictureById(
        uid,
        {
          event.eventPictures.remove(uid)
          updateEventWithoutImage(
              event,
              {
                _events.value = _events.value.map { if (it.uid == event.uid) event else it }
                onSuccess()
              },
              { e ->
                onFailure(e)
                Log.e("EventViewModel", "An error occurred while updating an event: $e")
              },
              false)
          onSuccess()
        },
        onFailure)
  }

  /**
   * Updates the save status of the user for the target event. If the user has already saved the
   * event, the event's interested count is decremented and the event is removed from the user's
   * saved events. If the user is has not yet saved the event, the event's interested count is
   * incremented and the event is added to the user's saved events.
   *
   * @param target The event to update the saved status for.
   * @param user The user to update the saved status for.
   * @param isUnsaveAction A boolean indicating whether the user is unsave the event.
   * @param updateUser A callback to update the user in the repository.
   */
  fun updateSave(target: Event, user: User, isUnsaveAction: Boolean, updateUser: () -> Unit) {
    val updatedEvent: Event
    val updatedUser: User = user.copy()

    if (isUnsaveAction) {
      val updatedSavedCount = if (target.numberOfSaved - 1 >= 0) target.numberOfSaved - 1 else 0
      updatedEvent = target.copy(numberOfSaved = updatedSavedCount)
      updatedUser.savedEvents.remove(target.uid)
      Firebase.messaging.unsubscribeFromTopic(target.uid)
    } else {
      updatedEvent = target.copy(numberOfSaved = target.numberOfSaved + 1)
      updatedUser.savedEvents.add(target.uid)
      Firebase.messaging.subscribeToTopic(target.uid)
    }
    saveUseCase.updateSave(
        updatedUser,
        updatedEvent,
        {
          _events.value =
              _events.value.map {
                if (it.uid == target.uid) {
                  updatedEvent
                } else it
              }
          _selectedEvent.value = updatedEvent
          updateUser()
        },
        { exception -> Log.e("EventViewModel", "Failed to update save", exception) })
  }
}
