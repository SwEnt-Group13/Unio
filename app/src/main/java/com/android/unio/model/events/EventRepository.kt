package com.android.unio.model.events

/**
 * Interface that defines the contract for an event repository.
 * Implementations of this interface will provide a way to retrieve a list of events.
 */
interface EventRepository {

    /**
     * Retrieves a list of events.
     *
     * @return A list of [Event] objects.
     */
    fun getEvents(): List<Event>
}
