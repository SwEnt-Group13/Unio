package com.android.unio.model.event

import com.android.unio.model.association.Association
import com.android.unio.model.firestore.emptyFirestoreReferenceList
import com.android.unio.model.map.Location
import com.google.firebase.Timestamp
import java.util.Date

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

class MockEventRepository : EventRepository {
  private val mockEvents =
      listOf(
          Event(
              uid = "1",
              title = "Sample Event 1",
              organisers = Association.emptyFirestoreReferenceList(),
              taggedAssociations = Association.emptyFirestoreReferenceList(),
              image = "https://example.com/image1.jpg",
              description = "A fun sample event for everyone!",
              catchyDescription = "Don't miss out on this exciting event!",
              price = 10.0,
              date = Timestamp(Date(2024 - 1900, 4, 15)),
              location = Location(latitude = 34.0522, longitude = -118.2437),
              types = listOf(EventType.FESTIVAL, EventType.NIGHT_PARTY)),
          Event(
              uid = "2",
              title = "Sample Event 2",
              organisers = Association.emptyFirestoreReferenceList(),
              taggedAssociations = Association.emptyFirestoreReferenceList(),
              image = "https://example.com/image2.jpg",
              description = "Join us for an unforgettable experience!",
              catchyDescription = "An event you cannot afford to miss!",
              price = 20.0,
              date = Timestamp(Date()),
              location = Location(latitude = 40.7128, longitude = -74.0060),
              types = listOf(EventType.APERITIF, EventType.JAM)))

  override fun getEventsOfAssociation(
      association: String,
      onSuccess: (List<Event>) -> Unit,
      onFailure: (Exception) -> Unit
  ) {
    onSuccess(mockEvents)
  }

  override fun getNextEventsFromDateToDate(
      startDate: Timestamp,
      endDate: Timestamp,
      onSuccess: (List<Event>) -> Unit,
      onFailure: (Exception) -> Unit
  ) {
    val filteredEvents =
        mockEvents.filter { event -> event.date >= startDate && event.date < endDate }
    onSuccess(filteredEvents)
  }

  override fun getEvents(onSuccess: (List<Event>) -> Unit, onFailure: (Exception) -> Unit) {
    onSuccess(mockEvents)
  }

  override fun getNewUid(): String {
    return "mockUid"
  }

  override fun addEvent(event: Event, onSuccess: () -> Unit, onFailure: (Exception) -> Unit) {
    mockEvents.toMutableList().add(event)
    onSuccess()
  }

  override fun deleteEventById(id: String, onSuccess: () -> Unit, onFailure: (Exception) -> Unit) {
    onSuccess()
  }
}
