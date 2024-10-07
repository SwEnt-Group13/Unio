package com.android.unio.model.events

interface EventRepository {
    fun getEvents(): List<Event>
}
