package com.android.unio.model.event

import com.google.firebase.firestore.DocumentReference

interface EventRepository {
  fun init(onSuccess: () -> Unit)

  fun getEventWithId(id: String, onSuccess: (Event) -> Unit, onFailure: (Exception) -> Unit)

  fun getEventRef(uid: String): DocumentReference

  fun getEvents(onSuccess: (List<Event>) -> Unit, onFailure: (Exception) -> Unit)

  fun getNewUid(): String

  fun addEvent(event: Event, onSuccess: () -> Unit, onFailure: (Exception) -> Unit)

  fun deleteEventById(id: String, onSuccess: () -> Unit, onFailure: (Exception) -> Unit)
}
