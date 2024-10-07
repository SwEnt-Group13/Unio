package com.android.unio.model.event

import com.google.firebase.Timestamp

interface EventRepository {
    fun getEventsOfAssociation(
        association: String,
        onSuccess: (List<Event>) -> Unit,
        onFailure: (Exception) -> Unit
    )

    fun getNextEventsFromDateToDate(
        startDate: Timestamp,
        endDate: Timestamp,
        onSuccess: (List<Event>) -> Unit,
        onFailure: (Exception) -> Unit
    )

    fun getEvents(onSuccess: (List<Event>) -> Unit, onFailure: (Exception) -> Unit)
    fun getNewUid(): String
    fun addEvent(event: Event, onSuccess: () -> Unit, onFailure: (Exception) -> Unit)
    fun deleteEventById(id: String, onSuccess: () -> Unit, onFailure: (Exception) -> Unit)
}