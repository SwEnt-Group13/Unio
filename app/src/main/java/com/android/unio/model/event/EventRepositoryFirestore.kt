package com.android.unio.model.event

import android.util.Log
import com.android.unio.R
import com.android.unio.model.firestore.FirestorePaths.EVENT_PATH
import com.android.unio.resources.ResourceManager
import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore

class EventRepositoryFirestore(private val db: FirebaseFirestore) : EventRepository {

  override fun getEventsOfAssociation(
      association: String,
      onSuccess: (List<Event>) -> Unit,
      onFailure: (Exception) -> Unit
  ) {
    db.collection(EVENT_PATH)
        .whereArrayContains("organisers", association)
        .get()
        .addOnSuccessListener { result ->
          val events = result.mapNotNull { event -> hydrate(event) }
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
          val events = result.mapNotNull { event -> hydrate(event) }
          onSuccess(events)
        }
        .addOnFailureListener { exception -> onFailure(exception) }
  }

  override fun getEvents(onSuccess: (List<Event>) -> Unit, onFailure: (Exception) -> Unit) {
    db.collection(EVENT_PATH)
        .get()
        .addOnSuccessListener { result ->
          val events = result.mapNotNull { doc -> hydrate(doc) }
          onSuccess(events)
        }
        .addOnFailureListener { exception -> onFailure(exception) }
  }

  override fun getNewUid(): String {
    return db.collection(EVENT_PATH).document().id
  }

  override fun addEvent(event: Event, onSuccess: () -> Unit, onFailure: (Exception) -> Unit) {
    if (event.uid.isBlank()) {
      val errorMsg = ResourceManager.getString(R.string.event_error_no_id_provided)
      onFailure(IllegalArgumentException(errorMsg))
    } else {
      db.collection(EVENT_PATH).document(event.uid).set(event).addOnCompleteListener { task ->
        if (task.isSuccessful) {
          onSuccess()
        } else {
          val errorMsg =
              task.exception?.localizedMessage
                  ?: ResourceManager.getString(R.string.event_error_failed_to_add)
          onFailure(Exception(errorMsg))
        }
      }
    }
  }

  override fun deleteEventById(id: String, onSuccess: () -> Unit, onFailure: (Exception) -> Unit) {
    db.collection(EVENT_PATH).document(id).delete().addOnCompleteListener { task ->
      if (task.isSuccessful) {
        onSuccess()
      } else {
        val errorMsg =
            task.exception?.localizedMessage
                ?: ResourceManager.getString(R.string.event_error_failed_to_delete)
        onFailure(Exception(errorMsg))
      }
    }
  }

  companion object {
    private const val TAG = "EventRepositoryFirestore"

    fun hydrate(doc: DocumentSnapshot): Event? {
      val event = doc.toObject(Event::class.java)
      if (event == null) {
        val errorMsg = ResourceManager.getString(R.string.event_error_converting_document)
        Log.e(TAG, errorMsg)
      }
      return event
    }
  }
}
