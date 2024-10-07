package com.android.unio.model.events

import java.util.UUID

//As the EventRepository is not made yet, and to test easily with specific data

class EventRepositoryMock : EventRepository {
    override fun getEvents(): List<Event> {
        return listOf(
            Event(
                id = UUID.randomUUID().toString(),
                title = "Android Conference",
                description = "A conference about the latest in Android development.",
                date = "ok",
                location = "San Francisco, CA",
            ),
            Event(
                id = UUID.randomUUID().toString(),
                title = "Kotlin Workshop",
                description = "Hands-on workshop for Kotlin enthusiasts.",
                date = "ok",
                location = "New York, NY",
            ),
            Event(
                id = UUID.randomUUID().toString(),
                title = "Jetpack Compose Seminar",
                description = "Learn about building UIs with Jetpack Compose.",
                date = "ok",
                location = "Chicago, IL",
            ),
            Event(
                id = UUID.randomUUID().toString(),
                title = "Jetpack Compose Seminar",
                description = "Learn about building UIs with Jetpack Compose.",
                date = "ok",
                location = "Chicago, IL",
            ),
            Event(
                id = UUID.randomUUID().toString(),
                title = "Jetpack Compose Seminar",
                description = "Learn about building UIs with Jetpack Compose.",
                date = "ok",
                location = "Chicago, IL",
            ),
            Event(
                id = UUID.randomUUID().toString(),
                title = "Jetpack Compose Seminar",
                description = "Learn about building UIs with Jetpack Compose.",
                date = "ok",
                location = "Chicago, IL",
            ),
            Event(
                id = UUID.randomUUID().toString(),
                title = "Jetpack Compose Seminar",
                description = "Learn about building UIs with Jetpack Compose.",
                date = "ok",
                location = "Chicago, IL",
            )
        )
    }
}
