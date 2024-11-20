package com.android.unio.model.firestore

import com.android.unio.model.association.Association
import com.android.unio.model.association.AssociationCategory
import com.android.unio.model.association.AssociationRepositoryFirestore
import com.android.unio.model.event.Event
import com.android.unio.model.event.EventRepositoryFirestore
import com.android.unio.model.firestore.transform.hydrate
import com.android.unio.model.firestore.transform.serialize
import com.android.unio.model.map.Location
import com.android.unio.model.user.Interest
import com.android.unio.model.user.Social
import com.android.unio.model.user.User
import com.android.unio.model.user.UserRepositoryFirestore
import com.android.unio.model.user.UserSocial
import com.google.firebase.Timestamp
import junit.framework.TestCase.assertEquals
import junit.framework.TestCase.assertTrue
import kotlin.reflect.full.memberProperties
import org.junit.Test

class HydrationAndSerializationTest {

  private val user =
      User(
          uid = "1",
          email = "1@gmail.com",
          firstName = "userFirst",
          lastName = "userLast",
          biography = "An example user",
          followedAssociations = Association.firestoreReferenceListWith(listOf("1", "2")),
          savedEvents = Event.firestoreReferenceListWith(listOf("1", "2")),
          joinedAssociations = Association.firestoreReferenceListWith(listOf("1", "2")),
          interests = listOf(Interest.SPORTS, Interest.MUSIC),
          socials =
              listOf(
                  UserSocial(Social.INSTAGRAM, "Insta"), UserSocial(Social.WEBSITE, "example.com")),
          profilePicture = "https://www.example.com/image")

  private val association =
      Association(
          uid = "1",
          url = "https://www.example.com",
          name = "EX",
          fullName = "Example Association",
          category = AssociationCategory.ARTS,
          description = "An example association",
          members = User.firestoreReferenceListWith(listOf("1", "2")),
          followersCount = 0,
          image = "https://www.example.com/image.jpg",
          events = Event.firestoreReferenceListWith(listOf("1", "2")),
          principalEmailAddress = "example@adress.com",
          adminUid = "1")

  private val event =
      Event(
          uid = "1",
          title = "Event 1",
          organisers = Association.firestoreReferenceListWith(listOf("1", "2")),
          taggedAssociations = Association.firestoreReferenceListWith(listOf("1", "2")),
          image = "https://www.example.com/image.jpg",
          description = "An example event",
          catchyDescription = "An example event",
          price = 0.0,
          startDate = Timestamp.now(),
          endDate = Timestamp.now(),
          location = Location(latitude = 0.0, longitude = 0.0, name = "Example Location"),
          placesRemaining = -1)

  /** Round-trip tests for serialization and hydration of user, association, and event instances. */
  @Test
  fun testUserHydrationAndSerialization() {
    val serialized = UserRepositoryFirestore.serialize(user)

    assertEquals(user.uid, serialized["uid"])
    assertEquals(user.email, serialized["email"])
    assertEquals(user.firstName, serialized["firstName"])
    assertEquals(user.lastName, serialized["lastName"])
    assertEquals(user.biography, serialized["biography"])
    assertEquals(user.followedAssociations.uids, serialized["followedAssociations"])
    assertEquals(user.joinedAssociations.uids, serialized["joinedAssociations"])
    assertEquals(user.interests.map { it.name }, serialized["interests"])
    assertEquals(
        user.socials.map { mapOf("social" to it.social.name, "content" to it.content) },
        serialized["socials"])
    assertEquals(user.profilePicture, serialized["profilePicture"])

    val hydrated = UserRepositoryFirestore.hydrate(serialized)

    assertEquals(user.uid, hydrated.uid)
    assertEquals(user.email, hydrated.email)
    assertEquals(user.firstName, hydrated.firstName)
    assertEquals(user.lastName, hydrated.lastName)
    assertEquals(user.biography, hydrated.biography)
    assertEquals(user.followedAssociations.uids, hydrated.followedAssociations.uids)
    assertEquals(user.joinedAssociations.uids, hydrated.joinedAssociations.uids)
    assertEquals(user.interests, hydrated.interests)
    assertEquals(user.socials, hydrated.socials)
    assertEquals(user.profilePicture, hydrated.profilePicture)
  }

  @Test
  fun testAssociationHydrationAndSerialization() {
    val serialized = AssociationRepositoryFirestore.serialize(association)

    assertEquals(association.uid, serialized["uid"])
    assertEquals(association.url, serialized["url"])
    assertEquals(association.name, serialized["name"])
    assertEquals(association.fullName, serialized["fullName"])
    assertEquals(association.description, serialized["description"])
    assertEquals(association.members.uids, serialized["members"])
    assertEquals(association.image, serialized["image"])
    assertEquals(association.events.uids, serialized["events"])

    val hydrated = AssociationRepositoryFirestore.hydrate(serialized)

    assertEquals(association.uid, hydrated.uid)
    assertEquals(association.url, hydrated.url)
    assertEquals(association.name, hydrated.name)
    assertEquals(association.fullName, hydrated.fullName)
    assertEquals(association.description, hydrated.description)
    assertEquals(association.members.list.value, hydrated.members.list.value)
    assertEquals(association.image, hydrated.image)
    assertEquals(association.events.list.value, hydrated.events.list.value)
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
    assertEquals(event.startDate, serialized["startDate"])
    assertEquals(event.endDate, serialized["endDate"])
    assertEquals(event.location.name, (serialized["location"] as Map<String, Any>)["name"])
    assertEquals(event.location.latitude, (serialized["location"] as Map<String, Any>)["latitude"])
    assertEquals(
        event.location.longitude, (serialized["location"] as Map<String, Any>)["longitude"])
    assertEquals(event.organisers.uids, serialized["organisers"])
    assertEquals(event.taggedAssociations.uids, serialized["taggedAssociations"])
    assertEquals(event.placesRemaining, serialized["placesRemaining"])

    val hydrated = EventRepositoryFirestore.hydrate(serialized)

    assertEquals(event.uid, hydrated.uid)
    assertEquals(event.title, hydrated.title)
    assertEquals(event.image, hydrated.image)
    assertEquals(event.description, hydrated.description)
    assertEquals(event.catchyDescription, hydrated.catchyDescription)
    assertEquals(event.price, hydrated.price)
    assertEquals(event.startDate, hydrated.startDate)
    assertEquals(event.endDate, hydrated.endDate)
    assertEquals(event.location, hydrated.location)
    assertEquals(event.organisers.uids, hydrated.organisers.uids)
    assertEquals(event.taggedAssociations.uids, hydrated.taggedAssociations.uids)
    assertEquals(event.placesRemaining, hydrated.placesRemaining)
  }

  /** Test hydration when the map misses fields. */
  @Test
  fun testUserHydrationWithMissingFields() {
    val serialized = emptyMap<String, Any>()

    val hydrated = UserRepositoryFirestore.hydrate(serialized)

    assertEquals("", hydrated.uid)
    assertEquals("", hydrated.email)
    assertEquals("", hydrated.firstName)
    assertEquals("", hydrated.lastName)
    assertEquals("", hydrated.biography)
    assertEquals(emptyList<String>(), hydrated.followedAssociations.list.value)
    assertEquals(emptyList<String>(), hydrated.joinedAssociations.list.value)
    assertEquals(emptyList<Interest>(), hydrated.interests)
    assertEquals(emptyList<UserSocial>(), hydrated.socials)
    assertEquals("", hydrated.profilePicture)
  }

  @Test
  fun testAssociationHydrationWithMissingFields() {
    val serialized = emptyMap<String, Any>()

    val hydrated = AssociationRepositoryFirestore.hydrate(serialized)

    assertEquals("", hydrated.uid)
    assertEquals("", hydrated.url)
    assertEquals("", hydrated.name)
    assertEquals("", hydrated.fullName)
    assertEquals("", hydrated.description)
    assertEquals(emptyList<User>(), hydrated.members.list.value)
    assertEquals("", hydrated.image)
    assertEquals(emptyList<Event>(), hydrated.events.list.value)
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
    assertEquals(Timestamp(0, 0), hydrated.startDate)
    assertEquals(Timestamp(0, 0), hydrated.endDate)
    assertEquals(Location(), hydrated.location)
    assertEquals(emptyList<Association>(), hydrated.organisers.list.value)
    assertEquals(emptyList<Association>(), hydrated.taggedAssociations.list.value)
    assertEquals(-1, hydrated.placesRemaining)
  }

  /** Test that serialization includes all data class fields. */
  @Test
  fun testUserSerializationHasAllFields() {
    val classMembers = User::class.memberProperties.map { it.name }

    val serialized = UserRepositoryFirestore.serialize(user)

    classMembers.forEach {
      assertTrue("User serialization is missing field '$it'.", serialized.containsKey(it))
    }
  }

  @Test
  fun testAssociationSerializationHasAllFields() {
    val classMembers = Association::class.memberProperties.map { it.name }

    val serialized = AssociationRepositoryFirestore.serialize(association)

    classMembers.forEach {
      assertTrue("Association serialization is missing field '$it'.", serialized.containsKey(it))
    }
  }

  @Test
  fun testEventSerializationHasAllFields() {
    val classMembers = Event::class.memberProperties.map { it.name }

    val serialized = EventRepositoryFirestore.serialize(event)

    classMembers.forEach {
      assertTrue("Event serialization is missing field '$it'.", serialized.containsKey(it))
    }
  }
}
