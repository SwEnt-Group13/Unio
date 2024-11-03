package com.android.unio.mocks.event

import com.android.unio.mocks.association.MockAssociation
import com.android.unio.mocks.map.MockLocation
import com.android.unio.model.association.Association
import com.android.unio.model.event.Event
import com.android.unio.model.event.EventType
import com.android.unio.mocks.firestore.MockReferenceList
import com.android.unio.model.map.Location
import com.google.firebase.Timestamp
import com.google.firebase.components.Dependency
import java.util.Date

/** MockEvent class provides edge-case instances of the Event data class for testing purposes. */
class MockEvent {
    companion object {

        /** Enums for each edge-case category **/
        enum class EdgeCaseUid(val value: String) {
            EMPTY(""),
            SPECIAL_CHARACTERS("evènt@123"),
            LONG("event-very-long-id-1234567890123456789012345678901234567890"),
            TYPICAL("event123")
        }

        enum class EdgeCaseTitle(val value: String) {
            EMPTY(""),
            SHORT("Gala"),
            LONG("An Extremely Long Event Title to Test Title Length Limitations and Truncation"),
            SPECIAL_CHARACTERS("Event with special char éñ!")
        }

        enum class EdgeCaseImage(val value: String) {
            EMPTY(""),
            TYPICAL("https://example.com/event_image.png"),
            LONG("https://example.com/very/long/path/to/image/that/may/exceed/length/limits/event_image12345.png"),
            INVALID("invalid-url")
        }

        enum class EdgeCaseDescription(val value: String) {
            EMPTY(""),
            SHORT("Brief event description."),
            LONG("This is a very long event description meant to test how the system handles large text fields.".repeat(10)),
            SPECIAL_CHARACTERS("Description with special characters #, @, $, %, and accents é, ü, ñ.")
        }

        enum class EdgeCaseCatchyDescription(val value: String) {
            EMPTY(""),
            SHORT("Exciting event!"),
            LONG("This catchy description is much longer than usual and might need to be truncated.".repeat(5)),
            SPECIAL_CHARACTERS("Catchy line with special chars #, @, $, %!")
        }

        enum class EdgeCasePrice(val value: Double) {
            FREE(0.0),
            LOW(5.0),
            HIGH(1000.0),
            NEGATIVE(-10.0)  // Invalid negative price
        }

        enum class EdgeCaseDate(val value: Timestamp) {
            PAST(Timestamp(Date(System.currentTimeMillis() - 604800000))),  // One week ago
            TODAY(Timestamp(Date(System.currentTimeMillis()))),
            FUTURE(Timestamp(Date(System.currentTimeMillis() + 604800000))),  // One week in the future
            FAR_FUTURE(Timestamp(Date(System.currentTimeMillis() + 31556952000)))  // 1 year in the future
        }

        /** Edge cases for EventType enums **/
        val edgeCaseEventTypes = EventType.values().toList()

        /** Returns a list of edge-case events based on selected edge cases */
        fun createEdgeCaseMockEvents(
            selectedUids: List<EdgeCaseUid> = EdgeCaseUid.values().toList(),
            selectedTitles: List<EdgeCaseTitle> = EdgeCaseTitle.values().toList(),
            selectedImages: List<EdgeCaseImage> = EdgeCaseImage.values().toList(),
            selectedDescriptions: List<EdgeCaseDescription> = EdgeCaseDescription.values().toList(),
            selectedCatchyDescriptions: List<EdgeCaseCatchyDescription> = EdgeCaseCatchyDescription.values().toList(),
            selectedPrices: List<EdgeCasePrice> = EdgeCasePrice.values().toList(),
            selectedDates: List<EdgeCaseDate> = EdgeCaseDate.values().toList(),
            selectedEventTypes: List<EventType> = edgeCaseEventTypes,
            selectedLocations: List<Location> = listOf(MockLocation.createMockLocation()),
            selectedOrganisers: List<List<Association>> = listOf(MockAssociation.createAllMockAssociations()),
            selectedTaggedAssociations: List<List<Association>> = listOf(MockAssociation.createAllMockAssociations())
        ): List<Event> {
            val events = mutableListOf<Event>()
            for (uid in selectedUids) {
                for (title in selectedTitles) {
                    for (image in selectedImages) {
                        for (description in selectedDescriptions) {
                            for (catchyDescription in selectedCatchyDescriptions) {
                                for (price in selectedPrices) {
                                    for (date in selectedDates) {
                                        for (eventType in selectedEventTypes) {
                                            for (location in selectedLocations) {
                                                for (organisers in selectedOrganisers) {
                                                    for (taggedAssociations in selectedTaggedAssociations) {
                                                        events.add(
                                                            createMockEvent(
                                                                uid = uid.value,
                                                                title = title.value,
                                                                organisers = organisers,
                                                                taggedAssociations = taggedAssociations,
                                                                image = image.value,
                                                                description = description.value,
                                                                catchyDescription = catchyDescription.value,
                                                                price = price.value,
                                                                date = date.value,
                                                                location = location,
                                                                types = listOf(eventType)
                                                            )
                                                        )
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
            return events
        }

        /** Creates a mock Event with specified properties for testing edge cases. */
        fun createMockEvent(
            associationDependency: Boolean = false,
            userDependency: Boolean = false,
            uid: String = "event123",
            title: String = "Sample Event",
            organisers: List<Association> = if (associationDependency){
                emptyList()
            } else{
                MockAssociation.createAllMockAssociations(userDependency = userDependency)
            },
            taggedAssociations: List<Association> = if (associationDependency){
                emptyList()
            } else {
                MockAssociation.createAllMockAssociations()
            },
            image: String = "https://example.com/event_image.png",
            description: String = "This is a sample event description.",
            catchyDescription: String = "Catchy tagline!",
            price: Double = 20.0,
            date: Timestamp = Timestamp(Date(System.currentTimeMillis() + 86400000)),
            location: Location = MockLocation.createMockLocation(),
            types: List<EventType> = listOf(EventType.TRIP)
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
                types = types
            )
        }

        /** Creates a list of mock Events with default properties */
        fun createAllMockEvents(associationDependency: Boolean = false, userDependency: Boolean = false, size: Int = 5): List<Event> {
            return List(size) { createMockEvent(associationDependency = associationDependency, userDependency = userDependency) }
        }
    }
}
