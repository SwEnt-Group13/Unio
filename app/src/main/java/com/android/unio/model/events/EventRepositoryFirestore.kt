package com.android.unio.model.events

import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

class EventRepositoryFirestore : EventRepository {
    private val firestore = FirebaseFirestore.getInstance()

    override suspend fun getEvents(): List<Event> {
        val eventsSnapshot = firestore.collection("events").get().await()
        return eventsSnapshot.documents.mapNotNull { it.toObject(Event::class.java) }
    }
}
