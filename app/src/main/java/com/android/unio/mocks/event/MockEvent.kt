package com.android.unio.mocks.event

import com.android.unio.mocks.association.MockAssociation
import com.android.unio.mocks.map.MockLocation
import com.android.unio.model.association.Association
import com.android.unio.model.event.Event
import com.android.unio.model.event.EventType
import com.android.unio.model.firestore.MockReferenceList
import com.android.unio.model.map.Location
import com.google.firebase.Timestamp
import java.util.Date
import kotlin.random.Random

/** MockEvent class provides sample instances of the Event data class for testing purposes. */
class MockEvent {
  companion object {
    /**
     * Creates a mock Event with customizable properties.
     *
     * @param uid Event ID
     * @param title Event title
     * @param organisers List of associations organizing the event
     * @param taggedAssociations List of associations tagged in the event
     * @param image URL to event image
     * @param description Event description
     * @param catchyDescription Short catchy description for the event
     * @param price Event price
     * @param date Event date and time
     * @param location Event location
     * @param types List of event types
     */
    fun createMockEvent(
        uid: String = generateRandomUid(),
        title: String = "Event ${Random.nextInt(1000)}",
        organisers: List<Association> = MockAssociation.createAllMockAssociations(),
        taggedAssociations: List<Association> = MockAssociation.createAllMockAssociations(),
        image: String = "https://example.com/event_image_${Random.nextInt(1000)}.png",
        description: String = "This is a detailed description of a random event.",
        catchyDescription: String = "Catchy event tagline!",
        price: Double = Random.nextDouble(5.0, 100.0),
        date: Timestamp = Timestamp(Date(Date().time + Random.nextLong(86400000, 604800000))),
        location: Location = MockLocation.createMockLocation(),
        types: List<EventType> = listOf(getRandomEventType())
    ): Event {
      return Event(
          uid = uid,
          title = title,
          organisers = MockReferenceList(organisers),
          taggedAssociations = MockReferenceList(taggedAssociations),
          image = image,
          description = description,
          catchyDescription = catchyDescription,
          price = price,
          date = date,
          location = location,
          types = types)
    }

    /** Generates a random UID for the event. */
    private fun generateRandomUid(): String {
      return "event${Random.nextInt(1000, 9999)}"
    }

    /** Returns a random EventType. */
    private fun getRandomEventType(): EventType {
      return EventType.values().random()
    }

    /** Creates a list of mock Events with random properties for testing purposes. */
    fun createAllMockEvents(size: Int = Random.nextInt(1, 11)): List<Event> {
      return List(size) { createMockEvent() }
    }
  }
}
