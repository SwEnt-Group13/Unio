package com.android.unio.model.events

import java.util.UUID

/**
 * A mock implementation of the EventRepository interface. This class is used for testing purposes
 * to provide a predefined list of events.
 *
 * Since the actual EventRepository is not implemented yet, this mock repository allows for easy
 * testing with specific data.
 */
open class EventRepositoryMock : EventRepository {

    /**
     * Retrieves a list of mock events.
     *
     * @return A list of [Event] objects with predefined data for testing.
     */
    override fun getEvents(): List<Event> {
        return listOf(
            Event(
                id = UUID.randomUUID().toString(),
                title = "WESKIC",
                catchy_description = "Come to the best event of the Coaching IC!",
                description =
                "The Summer Festival features live music, food stalls, and various activities for all ages.",
                date = "2024-07-20",
                location = "Central Park, New York",
                main_type = EventType.TRIP,
                picture = "weskic"),
            Event(
                id = UUID.randomUUID().toString(),
                title = "Oktoberweek",
                catchy_description = "There never enough beersssssss!",
                description =
                "An evening of networking with industry leaders and innovators. Don't miss out!",
                date = "2024-05-15",
                location = "Downtown Conference Center, Los Angeles",
                main_type = EventType.OTHER,
                picture = "oktoberweek"),
            Event(
                id = UUID.randomUUID().toString(),
                title = "SwissTech Talk",
                catchy_description = "Don't miss the chant de section!",
                description = "Learn Kotlin from scratch with real-world examples and expert guidance.",
                date = "2024-03-10",
                location = "Tech Hub, San Francisco",
                main_type = EventType.OTHER,
                picture = "swisstechtalk"),
            Event(
                id = UUID.randomUUID().toString(),
                title = "Lapin Vert",
                catchy_description = "Venez, il y a des gens sympa!",
                description =
                "Join us for an unforgettable evening featuring local artists and musicians.",
                date = "2024-09-25",
                location = "Art Gallery, Miami",
                main_type = EventType.OTHER,
                picture = "lapin_vert"),
            Event(
                id = UUID.randomUUID().toString(),
                title = "Choose your coach!",
                catchy_description = "Pick the best one!",
                description =
                "Participate in various sports activities and enjoy food and entertainment.",
                date = "2024-06-05",
                location = "City Stadium, Chicago",
                main_type = EventType.SPORT,
                picture = "chooseyourcoach"),
            Event(
                id = UUID.randomUUID().toString(),
                title = "Concert",
                catchy_description = "Best concert everrrrr!",
                description =
                "A workshop dedicated to teaching strategies for successful social media marketing.",
                date = "2024-08-30",
                location = "Innovation Center, Austin",
                main_type = EventType.OTHER,
                picture = "antoinoxlephar"),
            Event(
                id = UUID.randomUUID().toString(),
                title = "Jam Session: Local Artists",
                catchy_description = "Support local talent in this open jam session!",
                description =
                "An evening of music with local artists. Bring your instruments or just enjoy the show!",
                date = "2024-04-12",
                location = "Community Center, Seattle",
                main_type = EventType.JAM,
                picture = "photo_2024_10_08_14_57_48")
        )
    }
}
