package com.android.unio.model.events

interface EventRepository {
    suspend fun getEvents(): List<Event>
}
