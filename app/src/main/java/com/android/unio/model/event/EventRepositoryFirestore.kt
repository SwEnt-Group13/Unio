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

  override fun getNextEventsFromDateToDate(
      startDate: Timestamp,
      endDate: Timestamp,
      onSuccess: (List<Event>) -> Unit,
      onFailure: (Exception) -> Unit
  ) {
    db.collection(EVENT_PATH)
        .whereGreaterThanOrEqualTo("date", startDate)
        .whereLessThan("date", endDate)
        .get()
        .performFirestoreOperation(
            onSuccess = { result ->
              val events = result.mapNotNull { hydrate(it.data) }
              onSuccess(events)
            },
            onFailure = { exception -> onFailure(exception) })
  }

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

  override fun getNewUid(): String {
    return db.collection(EVENT_PATH).document().id
  }

  /** Updates the event in the repository or adds it if it does not exist. */
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
