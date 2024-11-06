package com.android.unio.model.event

import com.android.unio.model.association.Association
import com.android.unio.model.firestore.emptyFirestoreReferenceList
import com.android.unio.model.map.Location
import com.google.firebase.Timestamp
import java.util.Date
import java.util.UUID
import javax.inject.Inject

/**
 * A mock implementation of the EventRepository interface. This class is used for testing purposes
 * to provide a predefined list of events.
 *
 * Since the actual EventRepository is not implemented yet, this mock repository allows for easy
 * testing with specific data.
 */
open class EventRepositoryMock @Inject constructor() : EventRepository {
  override fun init(onSuccess: () -> Unit) {
    // This is a mock, so we assume the repository is initialized successfully
    onSuccess()
  }

  /**
   * Retrieves a list of mock events.
   *
   * @return A list of [Event] objects with predefined data for testing.
   */
  override fun getEvents(onSuccess: (List<Event>) -> Unit, onFailure: (Exception) -> Unit) {
    try {
      val events =
          listOf(
              Event(
                  uid = UUID.randomUUID().toString(),
                  title = "WESKIC",
                  organisers = Association.emptyFirestoreReferenceList(),
                  taggedAssociations = Association.emptyFirestoreReferenceList(),
                  image = "weskic",
                  description =
                      "The Summer Festival features live music, food stalls, and various activities for all ages.",
                  catchyDescription = "Come to the best event of the Coaching IC!",
                  price = 0.0,
                  date = Timestamp(Date(2024 - 1900, 6, 20)), // July 20, 2024
                  location = Location(0.0, 0.0, "USA"),
                  types = listOf(EventType.TRIP)),
              Event(
                  uid = UUID.randomUUID().toString(),
                  title = "Oktoberweekdnsjandjas",
                  organisers = Association.emptyFirestoreReferenceList(),
                  taggedAssociations = Association.emptyFirestoreReferenceList(),
                  image = "oktoberweek",
                  description =
                      "An evening of networking with industry leaders and innovators. Don't miss out!",
                  catchyDescription = "There never enough beersssssss!",
                  price = 10.0,
                  date = Timestamp(Date(2024 - 1900, 4, 15)), // May 15, 2024
                  location = Location(1.0, 1.0, "USA"),
                  types = listOf(EventType.OTHER)),
              Event(
                  uid = UUID.randomUUID().toString(),
                  title = "SwissTech Talk",
                  organisers = Association.emptyFirestoreReferenceList(),
                  taggedAssociations = Association.emptyFirestoreReferenceList(),
                  image = "swisstechtalk",
                  description =
                      "Learn Kotlin from scratch with real-world examples and expert guidance.",
                  catchyDescription = "Don't miss the chant de section!",
                  price = 0.0,
                  date = Timestamp(Date(2024 - 1900, 2, 10)), // March 10, 2024
                  location = Location(2.0, 2.0, "USA"),
                  types = listOf(EventType.OTHER)),
              Event(
                  uid = UUID.randomUUID().toString(),
                  title = "Lapin Vert",
                  organisers = Association.emptyFirestoreReferenceList(),
                  taggedAssociations = Association.emptyFirestoreReferenceList(),
                  image = "lapin_vert",
                  description =
                      "Join us for an unforgettable evening featuring local artists and musicians.",
                  catchyDescription = "Venez, il y a des gens sympa!",
                  price = 0.0,
                  date = Timestamp(Date(2024 - 1900, 8, 25)), // September 25, 2024
                  location = Location(3.0, 3.0, "USA"),
                  types = listOf(EventType.OTHER)),
              Event(
                  uid = UUID.randomUUID().toString(),
                  title = "Choose your coach!",
                  organisers = Association.emptyFirestoreReferenceList(),
                  taggedAssociations = Association.emptyFirestoreReferenceList(),
                  image = "chooseyourcoach",
                  description =
                      "Participate in various sports activities and enjoy food and entertainment.",
                  catchyDescription = "Pick the best one!",
                  price = 5.0,
                  date = Timestamp(Date(2024 - 1900, 5, 5)), // June 5, 2024
                  location = Location(4.0, 4.0, "USA"),
                  types = listOf(EventType.SPORT)),
              Event(
                  uid = UUID.randomUUID().toString(),
                  title = "Concert",
                  organisers = Association.emptyFirestoreReferenceList(),
                  taggedAssociations = Association.emptyFirestoreReferenceList(),
                  image = "antoinoxlephar",
                  description =
                      "A workshop dedicated to teaching strategies for successful social media marketing.",
                  catchyDescription = "Best concert everrrrr!",
                  price = 15.0,
                  date = Timestamp(Date(2024 - 1900, 7, 30)), // August 30, 2024
                  location = Location(5.0, 5.0, "USA"),
                  types = listOf(EventType.OTHER)),
              Event(
                  uid = UUID.randomUUID().toString(),
                  title = "Jam Session: Local Artists",
                  organisers = Association.emptyFirestoreReferenceList(),
                  taggedAssociations = Association.emptyFirestoreReferenceList(),
                  image = "photo_2024_10_08_14_57_48",
                  description =
                      "An evening of music with local artists. Bring your instruments or just enjoy the show!",
                  catchyDescription = "Support local talent in this open jam session!",
                  price = 0.0,
                  date = Timestamp(Date(2024 - 1900, 3, 12)), // April 12, 2024
                  location = Location(6.0, 6.0, "USA"),
                  types = listOf(EventType.JAM)))
      onSuccess(events)
    } catch (e: Exception) {
      onFailure(e)
    }
  }

  override fun getEventWithId(
      id: String,
      onSuccess: (Event) -> Unit,
      onFailure: (Exception) -> Unit
  ) {}

  // Mock implementation for getting events by association
  override fun getEventsOfAssociation(
      association: String,
      onSuccess: (List<Event>) -> Unit,
      onFailure: (Exception) -> Unit
  ) {
    // Filter mock events by tagged associations
    getEvents(
        { events ->
          onSuccess(events)
        }, // Now filtering for empty tagged associations
        onFailure)
  }

  // Mock implementation for getting events between two dates
  override fun getNextEventsFromDateToDate(
      startDate: Timestamp,
      endDate: Timestamp,
      onSuccess: (List<Event>) -> Unit,
      onFailure: (Exception) -> Unit
  ) {
    // Filter mock events by date range
    getEvents(
        { events -> onSuccess(events.filter { it.date >= startDate && it.date <= endDate }) },
        onFailure)
  }

  // Mock implementation to generate a new UID
  override fun getNewUid(): String {
    return UUID.randomUUID().toString()
  }

  // Mock implementation to add an event
  override fun addEvent(event: Event, onSuccess: () -> Unit, onFailure: (Exception) -> Unit) {
    // This is a mock, so we assume the event is added successfully
    onSuccess()
  }

  // Mock implementation to delete an event by ID
  override fun deleteEventById(id: String, onSuccess: () -> Unit, onFailure: (Exception) -> Unit) {
    // This is a mock, so we assume the event is deleted successfully
    onSuccess()
  }
}
