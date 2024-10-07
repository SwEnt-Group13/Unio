package com.android.unio.model.events

import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

class EventRepositoryFirestore : EventRepository {
    private val firestore = FirebaseFirestore.getInstance()

    // Change the return type to accept a callback
    override fun getEvents(): List<Event> {
       return emptyList()
    }
}
