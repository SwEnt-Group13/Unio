package com.android.unio.model.firestore

import com.android.unio.model.association.Association
import com.android.unio.model.association.AssociationRepositoryFirestore
import com.android.unio.model.event.Event
import com.android.unio.model.event.EventRepositoryFirestore
import com.android.unio.model.firestore.transform.hydrate
import com.android.unio.model.firestore.transform.serialize
import com.android.unio.model.map.Location
import com.android.unio.model.user.User
import com.android.unio.model.user.UserRepositoryFirestore
import com.google.firebase.Firebase
import com.google.firebase.Timestamp
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.firestore
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkStatic
import junit.framework.TestCase.assertEquals
import org.junit.Before
import org.junit.Test
import org.mockito.MockitoAnnotations

class HydrationAndSerializationTest {
  private lateinit var db: FirebaseFirestore
  private lateinit var user: User
  private lateinit var association: Association
  private lateinit var event: Event

  @Before
  fun setUp() {
    MockitoAnnotations.openMocks(this)

    db = mockk()
    mockkStatic(FirebaseFirestore::class)
    every { Firebase.firestore } returns db

    user =
        User(
            uid = "1",
            email = "1@gmail.com",
            name = "User 1",
            followingAssociations = Association.firestoreReferenceListWith(listOf("1", "2")))

    association =
        Association(
            uid = "1",
            url = "https://www.example.com",
            acronym = "EX",
            fullName = "Example Association",
            description = "An example association",
            members = User.firestoreReferenceListWith(listOf("1", "2")))

    event =
        Event(
            uid = "1",
            title = "Event 1",
            image = "https://www.example.com/image.jpg",
            description = "An example event",
            catchyDescription = "An example event",
            price = 0.0,
            date = Timestamp.now(),
            location = Location(latitude = 0.0, longitude = 0.0, name = "Example Location"),
            organisers = Association.firestoreReferenceListWith(listOf("1", "2")),
            taggedAssociations = Association.firestoreReferenceListWith(listOf("1", "2")))
  }

  /** Round-trip tests for serialization and hydration of user, association, and event instances. */
  @Test
  fun testUserHydrationAndSerialization() {
    val serialized = UserRepositoryFirestore.serialize(user)

    assertEquals(user.uid, serialized["uid"])
    assertEquals(user.name, serialized["name"])
    assertEquals(user.email, serialized["email"])
    assertEquals(user.followingAssociations.list.value, serialized["followingAssociations"])

    val hydrated = UserRepositoryFirestore.hydrate(serialized)

    assertEquals(user.uid, hydrated.uid)
    assertEquals(user.name, hydrated.name)
    assertEquals(user.email, hydrated.email)
    assertEquals(user.followingAssociations.list.value, hydrated.followingAssociations.list.value)
  }

  @Test
  fun testAssociationHydrationAndSerialization() {
    val serialized = AssociationRepositoryFirestore.serialize(association)

    assertEquals(association.uid, serialized["uid"])
    assertEquals(association.url, serialized["url"])
    assertEquals(association.acronym, serialized["acronym"])
    assertEquals(association.fullName, serialized["fullName"])
    assertEquals(association.description, serialized["description"])
    assertEquals(association.members.list.value, serialized["members"])

    val hydrated = AssociationRepositoryFirestore.hydrate(serialized)

    assertEquals(association.uid, hydrated.uid)
    assertEquals(association.url, hydrated.url)
    assertEquals(association.acronym, hydrated.acronym)
    assertEquals(association.fullName, hydrated.fullName)
    assertEquals(association.description, hydrated.description)
    assertEquals(association.members.list.value, hydrated.members.list.value)
  }

  @Test
  fun testEventHydrationAndSerialization() {
    val serialized = EventRepositoryFirestore.serialize(event)

    assertEquals(event.uid, serialized["uid"])
    assertEquals(event.title, serialized["title"])
    assertEquals(event.image, serialized["image"])
    assertEquals(event.description, serialized["description"])
    assertEquals(event.catchyDescription, serialized["catchyDescription"])
    assertEquals(event.price, serialized["price"])
    assertEquals(event.date, serialized["date"])
    assertEquals(event.location.name, (serialized["location"] as Map<String, Any>)["name"])
    assertEquals(event.location.latitude, (serialized["location"] as Map<String, Any>)["latitude"])
    assertEquals(
        event.location.longitude, (serialized["location"] as Map<String, Any>)["longitude"])
    assertEquals(event.organisers.list.value, serialized["organisers"])
    assertEquals(event.taggedAssociations.list.value, serialized["taggedAssociations"])

    val hydrated = EventRepositoryFirestore.hydrate(serialized)

    assertEquals(event.uid, hydrated.uid)
    assertEquals(event.title, hydrated.title)
    assertEquals(event.image, hydrated.image)
    assertEquals(event.description, hydrated.description)
    assertEquals(event.catchyDescription, hydrated.catchyDescription)
    assertEquals(event.price, hydrated.price)
    assertEquals(event.date, hydrated.date)
    assertEquals(event.location, hydrated.location)
    assertEquals(event.organisers.list.value, hydrated.organisers.list.value)
    assertEquals(event.taggedAssociations.list.value, hydrated.taggedAssociations.list.value)
  }

  /** Test hydration when the map misses fields. */
  @Test
  fun testUserHydrationWithMissingFields() {
    val serialized = emptyMap<String, Any>()

    val hydrated = UserRepositoryFirestore.hydrate(serialized)

    assertEquals("", hydrated.uid)
    assertEquals("", hydrated.name)
    assertEquals("", hydrated.email)
    assertEquals(emptyList<String>(), hydrated.followingAssociations.list.value)
  }

  @Test
  fun testAssociationHydrationWithMissingFields() {
    val serialized = emptyMap<String, Any>()

    val hydrated = AssociationRepositoryFirestore.hydrate(serialized)

    assertEquals("", hydrated.uid)
    assertEquals("", hydrated.url)
    assertEquals("", hydrated.acronym)
    assertEquals("", hydrated.fullName)
    assertEquals("", hydrated.description)
    assertEquals(emptyList<String>(), hydrated.members.list.value)
  }

  @Test
  fun testEventHydrationWithMissingFields() {
    val serialized = emptyMap<String, Any>()

    val hydrated = EventRepositoryFirestore.hydrate(serialized)

    assertEquals("", hydrated.uid)
    assertEquals("", hydrated.title)
    assertEquals("", hydrated.image)
    assertEquals("", hydrated.description)
    assertEquals("", hydrated.catchyDescription)
    assertEquals(0.0, hydrated.price)
    assertEquals(Timestamp(0, 0), hydrated.date)
    assertEquals(Location(), hydrated.location)
    assertEquals(emptyList<String>(), hydrated.organisers.list.value)
    assertEquals(emptyList<String>(), hydrated.taggedAssociations.list.value)
  }
}
