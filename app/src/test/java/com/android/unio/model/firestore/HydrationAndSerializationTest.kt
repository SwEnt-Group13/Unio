package com.android.unio.model.firestore

import androidx.test.core.app.ApplicationProvider
import com.android.unio.model.association.Association
import com.android.unio.model.association.AssociationRepositoryFirestore
import com.android.unio.model.event.Event
import com.android.unio.model.event.EventRepositoryFirestore
import com.android.unio.model.firestore.transform.hydrate
import com.android.unio.model.firestore.transform.serialize
import com.android.unio.model.map.Location
import com.android.unio.model.user.User
import com.android.unio.model.user.UserRepositoryFirestore
import com.google.firebase.FirebaseApp
import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import junit.framework.TestCase.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.Mockito.`when`
import org.mockito.MockitoAnnotations
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class HydrationAndSerializationTest {
  @Mock private lateinit var user: User
  @Mock private lateinit var userMap: Map<String, Any>
  @Mock private lateinit var userDoc: DocumentSnapshot

  @Mock private lateinit var association: Association
  @Mock private lateinit var associationMap: Map<String, Any>
  @Mock private lateinit var associationDoc: DocumentSnapshot

  @Mock private lateinit var event: Event
  @Mock private lateinit var eventMap: Map<String, Any>
  @Mock private lateinit var eventDoc: DocumentSnapshot

  @Before
  fun setUp() {
    MockitoAnnotations.openMocks(this)

    // Initialize Firebase if necessary
    if (FirebaseApp.getApps(ApplicationProvider.getApplicationContext()).isEmpty()) {
      FirebaseApp.initializeApp(ApplicationProvider.getApplicationContext())
    }

    user =
        User(
            uid = "1",
            email = "1@gmail.com",
            name = "User 1",
            followingAssociations =
                FirestoreReferenceList.fromList(
                    listOf("1", "2"),
                    FirebaseFirestore.getInstance().collection("associations"),
                    AssociationRepositoryFirestore::hydrate))

    association =
        Association(
            uid = "1",
            url = "https://www.example.com",
            acronym = "EX",
            fullName = "Example Association",
            description = "An example association",
            members =
                FirestoreReferenceList.fromList(
                    listOf("1", "2"),
                    FirebaseFirestore.getInstance().collection("users"),
                    UserRepositoryFirestore::hydrate))

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
            organisers =
                FirestoreReferenceList.fromList(
                    listOf("1", "2"),
                    FirebaseFirestore.getInstance().collection("associations"),
                    AssociationRepositoryFirestore::hydrate),
            taggedAssociations =
                FirestoreReferenceList.fromList(
                    listOf("1", "2"),
                    FirebaseFirestore.getInstance().collection("associations"),
                    AssociationRepositoryFirestore::hydrate),
        )

    `when`(userDoc.data).thenReturn(userMap)
    `when`(associationDoc.data).thenReturn(associationMap)
    `when`(eventDoc.data).thenReturn(eventMap)
  }

  @Test
  fun testUserHydrationAndSerialization() {
    val result = UserRepositoryFirestore.hydrate(UserRepositoryFirestore.serialize(user))

    assertEquals(user.uid, result.uid)
    assertEquals(user.name, result.name)
    assertEquals(user.email, result.email)
    assertEquals(user.followingAssociations.list.value, result.followingAssociations.list.value)
  }

  @Test
  fun testAssociationHydrationAndSerialization() {
    val result =
        AssociationRepositoryFirestore.hydrate(
            AssociationRepositoryFirestore.serialize(association))

    assertEquals(association.uid, result.uid)
    assertEquals(association.url, result.url)
    assertEquals(association.acronym, result.acronym)
    assertEquals(association.fullName, result.fullName)
    assertEquals(association.description, result.description)
    assertEquals(association.members.list.value, result.members.list.value)
  }

  @Test
  fun testEventHydrationAndSerialization() {
    val result = EventRepositoryFirestore.hydrate(EventRepositoryFirestore.serialize(event))

    assertEquals(event.uid, result.uid)
    assertEquals(event.title, result.title)
    assertEquals(event.image, result.image)
    assertEquals(event.description, result.description)
    assertEquals(event.catchyDescription, result.catchyDescription)
    assertEquals(event.price, result.price)
    assertEquals(event.date, result.date)
    assertEquals(event.location, result.location)
    assertEquals(event.organisers.list.value, result.organisers.list.value)
    assertEquals(event.taggedAssociations.list.value, result.taggedAssociations.list.value)
  }
}
