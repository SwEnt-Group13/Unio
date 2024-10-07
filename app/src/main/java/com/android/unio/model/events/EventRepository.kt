package com.android.unio.model.events

interface EventRepository {
    fun getEvents(): List<Event> //Marks the function as suspendable, allowing it to be called within a coroutine.
}
