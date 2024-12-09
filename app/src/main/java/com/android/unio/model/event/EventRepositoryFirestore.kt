package com.android.unio.model.event

import com.android.unio.model.authentication.registerAuthStateListener
import com.android.unio.model.firestore.FirestorePaths.EVENT_PATH
import com.android.unio.model.firestore.performFirestoreOperation
import com.android.unio.model.firestore.registerSnapshotListener
import com.android.unio.model.firestore.transform.hydrate
import com.android.unio.model.firestore.transform.serialize
import com.google.firebase.Firebase
import com.google.firebase.Timestamp
import com.google.firebase.auth.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.MetadataChanges
import javax.inject.Inject

class EventRepositoryFirestore @Inject constructor(private val db: FirebaseFirestore) :
    EventRepository {

  override fun init(onSuccess: () -> Unit) {
    Firebase.auth.registerAuthStateListener {
      if (it.currentUser != null) {
        onSuccess()
      }
    }
  }

  override fun getEventsOfAssociation(
      association: String,
      onSuccess: (List<Event>) -> Unit,
      onFailure: (Exception) -> Unit
  ) {
    db.collection(EVENT_PATH)
        .whereArrayContains("organisers", association)
        .get()
        .performFirestoreOperation(
            onSuccess = { result ->
              val events = result.mapNotNull { hydrate(it.data) }
              onSuccess(events)
            },
            onFailure = { exception -> onFailure(exception) })
  }

  /**
   * Gets the event with the given id. Here, instead of using success and failure listener directly,
   * we use a Snapshot Listener that call directly the callback when a read/write are made on the
   * local (cache) database.
   *
   * @param id [String] : the id of the event to get.
   * @param onSuccess [(Event) -> Unit] : the callback to call when the event is found.
   * @param onFailure [(Exception) -> Unit] : the callback to call when an error occurs.
   */
  override fun getEventWithId(
      id: String,
      onSuccess: (Event) -> Unit,
      onFailure: (Exception) -> Unit
  ) {
    db.collection(EVENT_PATH).document(id).registerSnapshotListener(MetadataChanges.EXCLUDE) {
        documentSnapshot,
        exception ->
      if (exception != null) {
        onFailure(exception)
        return@registerSnapshotListener
      }
      if (documentSnapshot != null && documentSnapshot.exists()) {
        onSuccess(hydrate(documentSnapshot.data))
      }
    }
  }

  /**
   * Fetches all events from Firestore and calls the onSuccess callback with the list of events.
   *
   * @param onSuccess [(List<Event>) -> Unit] : The callback to call when the events are fetched.
   * @param onFailure [(Exception) -> Unit] : The callback to call when the fetch fails.
   */
  override fun getEvents(onSuccess: (List<Event>) -> Unit, onFailure: (Exception) -> Unit) {
    db.collection(EVENT_PATH)
        .get()
        .performFirestoreOperation(
            onSuccess = { result ->
              val events = result.mapNotNull { hydrate(it.data) }
              onSuccess(events)
            },
            onFailure = { exception -> onFailure(exception) })
  }

  /**
   * Generates a new unique id for an event.
   *
   * @return [String] : the new unique id.
   */
  override fun getNewUid(): String {
    return db.collection(EVENT_PATH).document().id
  }

  /**
   * Updates the event in the repository or adds it if it does not exist.
   *
   * @param event [Event] : the event to add or update.
   * @param onSuccess [() -> Unit] : the callback to call when the event is added or updated.
   * @param onFailure [(Exception) -> Unit] : the callback to call when the operation fails.
   */
  override fun addEvent(event: Event, onSuccess: () -> Unit, onFailure: (Exception) -> Unit) {
    if (event.uid.isBlank()) {
      onFailure(IllegalArgumentException("No event id was provided"))
    } else {
      db.collection(EVENT_PATH)
          .document(event.uid)
          .set(serialize(event))
          .performFirestoreOperation(
              onSuccess = { onSuccess() }, onFailure = { exception -> onFailure(exception) })
    }
  }

  /**
   * Deletes the event with the given id.
   *
   * @param id [String] : the id of the event to delete.
   * @param onSuccess [() -> Unit] : the callback to call when the event is deleted.
   * @param onFailure [(Exception) -> Unit] : the callback to call when the operation fails.
   */
  override fun deleteEventById(id: String, onSuccess: () -> Unit, onFailure: (Exception) -> Unit) {
    db.collection(EVENT_PATH)
        .document(id)
        .delete()
        .performFirestoreOperation(
            onSuccess = { onSuccess() }, onFailure = { exception -> onFailure(exception) })
  }

  // Note: the following line is needed to add external methods to the companion object
  companion object
}
