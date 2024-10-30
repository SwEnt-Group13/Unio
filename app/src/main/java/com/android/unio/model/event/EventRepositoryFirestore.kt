package com.android.unio.model.event

import com.android.unio.model.firestore.FirestorePaths.EVENT_PATH
import com.android.unio.model.firestore.transform.hydrate
import com.android.unio.model.firestore.transform.serialize
import com.google.firebase.Timestamp
import com.google.firebase.firestore.FirebaseFirestore
import javax.inject.Inject

class EventRepositoryFirestore @Inject constructor(private val db: FirebaseFirestore) :
    EventRepository {

  override fun getEventsOfAssociation(
      association: String,
      onSuccess: (List<Event>) -> Unit,
      onFailure: (Exception) -> Unit
  ) {
    db.collection(EVENT_PATH)
        .whereArrayContains("organisers", association)
        .get()
        .addOnSuccessListener { result ->
          val events = result.mapNotNull { hydrate(it.data) }
          onSuccess(events)
        }
        .addOnFailureListener { exception -> onFailure(exception) }
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
        .addOnSuccessListener { result ->
          val events = result.mapNotNull { hydrate(it.data) }
          onSuccess(events)
        }
        .addOnFailureListener { exception -> onFailure(exception) }
  }

  override fun getEvents(onSuccess: (List<Event>) -> Unit, onFailure: (Exception) -> Unit) {
    db.collection(EVENT_PATH)
        .get()
        .addOnSuccessListener { result ->
          val events = result.mapNotNull { hydrate(it.data) }
          onSuccess(events)
        }
        .addOnFailureListener { exception -> onFailure(exception) }
  }

  override fun getNewUid(): String {
    return db.collection(EVENT_PATH).document().id
  }

  override fun addEvent(event: Event, onSuccess: () -> Unit, onFailure: (Exception) -> Unit) {
    if (event.uid.isBlank()) {
      onFailure(IllegalArgumentException("No event id was provided"))
    } else {
      db.collection(EVENT_PATH).document(event.uid).set(serialize(event)).addOnCompleteListener {
          task ->
        if (task.isSuccessful) {
          onSuccess()
        } else {
          onFailure(task.exception ?: Exception("Failed to add an event"))
        }
      }
    }
  }

  override fun deleteEventById(id: String, onSuccess: () -> Unit, onFailure: (Exception) -> Unit) {
    db.collection(EVENT_PATH).document(id).delete().addOnCompleteListener { task ->
      if (task.isSuccessful) {
        onSuccess()
      } else {
        onFailure(task.exception ?: Exception("Failed to delete event"))
      }
    }
  }

  // Note: the following line is needed to add external methods to the companion object
  companion object
}
