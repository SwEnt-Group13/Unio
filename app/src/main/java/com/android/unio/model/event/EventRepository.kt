package com.android.unio.model.event

interface EventRepository {
  fun init(onSuccess: () -> Unit)

  fun getEventsOfAssociation(
      association: String,
      onSuccess: (List<Event>) -> Unit,
      onFailure: (Exception) -> Unit
  )

  fun getEventWithId(id: String, onSuccess: (Event) -> Unit, onFailure: (Exception) -> Unit)

  fun getEvents(onSuccess: (List<Event>) -> Unit, onFailure: (Exception) -> Unit)

  fun getNewUid(): String

  fun addEvent(event: Event, onSuccess: () -> Unit, onFailure: (Exception) -> Unit)

  fun deleteEventById(id: String, onSuccess: () -> Unit, onFailure: (Exception) -> Unit)
}
