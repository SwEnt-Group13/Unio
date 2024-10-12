package com.android.unio.model.event

import android.util.Log
import com.android.unio.model.firestore.FirestorePaths.EVENT_PATH
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
            onFailure(IllegalArgumentException("No event id was provided"))
        } else {
            db.collection(EVENT_PATH).document(event.uid).set(event).addOnCompleteListener { task ->
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

    companion object {
        fun hydrate(doc: DocumentSnapshot): Event? {
            val event = doc.toObject(Event::class.java)
            if (event == null) {
                Log.e("EventRepositoryFirestore", "Error while converting db document to Event object")
            }
            return event
        }
    }
}