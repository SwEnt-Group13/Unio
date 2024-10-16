package com.android.unio.model.event

import com.android.unio.model.association.Association
import com.android.unio.model.firestore.FirestorePaths.EVENT_PATH
import com.android.unio.model.map.Location
import com.google.android.gms.tasks.OnSuccessListener
import com.google.android.gms.tasks.Task
import com.google.firebase.Firebase
import com.google.firebase.Timestamp
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.QueryDocumentSnapshot
import com.google.firebase.firestore.QuerySnapshot
import com.google.firebase.firestore.firestore
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkStatic
import java.util.GregorianCalendar
import junit.framework.TestCase.assertEquals
import org.junit.Assert.assertThrows
import org.junit.Before
import org.junit.Test
import org.mockito.Mock
import org.mockito.Mockito.`when`
import org.mockito.MockitoAnnotations
import org.mockito.kotlin.any

class EventRepositoryFirestoreTest {
  private lateinit var db: FirebaseFirestore

  @Mock private lateinit var collectionReference: CollectionReference

  @Mock private lateinit var documentReference: DocumentReference

  @Mock private lateinit var query: Query

  @Mock private lateinit var querySnapshot: QuerySnapshot

  @Mock private lateinit var queryDocumentSnapshot1: QueryDocumentSnapshot
  @Mock private lateinit var map1: Map<String, Any>

  @Mock private lateinit var queryDocumentSnapshot2: QueryDocumentSnapshot
  @Mock private lateinit var map2: Map<String, Any>

  @Mock private lateinit var queryDocumentSnapshot3: QueryDocumentSnapshot
  @Mock private lateinit var map3: Map<String, Any>

  @Mock private lateinit var getTask: Task<QuerySnapshot>

  @Mock private lateinit var voidTask: Task<Void>

  private lateinit var repository: EventRepositoryFirestore
  private val defaultEvent =
      Event(
          organisers = Association.emptyFirestoreReferenceList(),
          taggedAssociations = Association.emptyFirestoreReferenceList())
  private val event1 =
      Event(
          uid = "1",
          title = "Balelec",
          organisers = Association.emptyFirestoreReferenceList(),
          taggedAssociations = Association.emptyFirestoreReferenceList(),
          image = "",
          description = "Plus grand festival du monde (non contractuel)",
          price = 40.5,
          date = Timestamp(GregorianCalendar(2004, 7, 1).time),
          location = Location(1.2345, 2.3455, "Somewhere"))
  private val event3 =
      Event(
          uid = "3",
          title = "Tremplin Sysmic",
          organisers = Association.emptyFirestoreReferenceList(),
          taggedAssociations = Association.emptyFirestoreReferenceList(),
          image = "",
          description = "Plus grand festival du monde (non contractuel)",
          price = 40.5,
          date = Timestamp(GregorianCalendar(2008, 7, 1).time),
          location = Location(1.2345, 2.3455, "Somewhere"))

  @Before
  fun setUp() {
    MockitoAnnotations.openMocks(this)

    // When getting the collection, return the task
    db = mockk()
    mockkStatic(FirebaseFirestore::class)
    every { Firebase.firestore } returns db
    every { db.collection(EVENT_PATH) } returns collectionReference

    `when`(collectionReference.get()).thenReturn(getTask)

    // When the task is successful, return the query snapshot
    `when`(getTask.addOnSuccessListener(any())).thenAnswer { invocation ->
      val callback = invocation.arguments[0] as OnSuccessListener<QuerySnapshot>
      callback.onSuccess(querySnapshot)
      getTask
    }

    // When the query snapshot is iterated, return the two query document snapshots
    `when`(querySnapshot.iterator())
        .thenReturn(
            mutableListOf(queryDocumentSnapshot1, queryDocumentSnapshot2, queryDocumentSnapshot3)
                .iterator())

    // When the query document snapshots are converted to events, return the events
    `when`(queryDocumentSnapshot1.data).thenReturn(map1)
    `when`(queryDocumentSnapshot2.data).thenReturn(map2)
    `when`(queryDocumentSnapshot3.data).thenReturn(map3)

    // Only test the uid field, the other fields are tested by HydrationAndSerializationTest
    `when`(map1["uid"]).thenReturn(event1.uid)
    `when`(map2["uid"]).thenReturn(defaultEvent.uid)
    `when`(map3["uid"]).thenReturn(event3.uid)

    repository = EventRepositoryFirestore(db)
  }

  /** Asserts that getEvents returns all events */
  @Test
  fun testGetEvents() {

    repository.getEvents(
        onSuccess = { events ->
          assertEquals(3, events.size)

          assertEquals(event1.uid, events[0].uid)
          assertEquals(defaultEvent.uid, events[1].uid)
          assertEquals(event3.uid, events[2].uid)
        },
        onFailure = { e -> throw e })
  }

  /** Asserts that getEventsOfAssociation calls the right methods. */
  @Test
  fun testGetEventsOfAssociation() {
    val asso1 = "Balelec"
    val asso2 = "EPFL"
    `when`(collectionReference.whereArrayContains("organisers", asso1)).thenReturn(query)
    `when`(query.get()).thenReturn(getTask)
    `when`(querySnapshot.iterator()).thenReturn(mutableListOf(queryDocumentSnapshot1).iterator())

    repository.getEventsOfAssociation(
        "Balelec",
        onSuccess = { events ->
          assertEquals(1, events.size)
          assertEquals(event1.uid, events[0].uid)
        },
        onFailure = { e -> throw e })

    `when`(collectionReference.whereArrayContains("organisers", asso2)).thenReturn(query)
    `when`(querySnapshot.iterator())
        .thenReturn(mutableListOf(queryDocumentSnapshot1, queryDocumentSnapshot3).iterator())

    repository.getEventsOfAssociation(
        "EPFL",
        onSuccess = { events ->
          assertEquals(2, events.size)
          assert(events.any { it.uid == event1.uid })
          assert(events.any { it.uid == event3.uid })
        },
        onFailure = { e -> throw e })
  }

  /** Asserts that getNextEventsFromDateToDate calls the right methods. */
  @Test
  fun testGetNextEventsFromDateToDate() {
    val startDate = Timestamp(GregorianCalendar(2003, 7, 1).time)
    val endDate = Timestamp(GregorianCalendar(2005, 7, 1).time)
    `when`(collectionReference.whereGreaterThanOrEqualTo("date", startDate)).thenReturn(query)
    `when`(query.whereLessThan("date", endDate)).thenReturn(query)
    `when`(query.get()).thenReturn(getTask)
    `when`(querySnapshot.iterator()).thenReturn(mutableListOf(queryDocumentSnapshot1).iterator())

    repository.getNextEventsFromDateToDate(
        startDate,
        endDate,
        { events ->
          assertEquals(events.size, 1)
          assertEquals(events[0].uid, event1.uid)
        },
        { e -> throw e })
  }

  /** Asserts that db.collection(EVENT_PATH).document(event.uid).set(event) is called */
  @Test
  fun testAddEvent() {
    `when`(collectionReference.document(event1.uid)).thenReturn(documentReference)
    `when`(documentReference.set(any())).thenReturn(voidTask)
    repository.addEvent(event1, {}, { e -> throw e })
  }

  /**
   * Assert that calling addEvent with an event that has a blank id return an
   * IllegalArgumentException.
   */
  @Test
  fun testAddEventBlankId() {
    repository.addEvent(defaultEvent, {}) { e ->
      assertThrows(IllegalArgumentException::class.java) { throw e }
    }
  }

  /** Assert that deleteEventById calls db.collection(EVENT_PATH).document(id).delete() */
  @Test
  fun testDeleteEventById() {
    `when`(collectionReference.document(event1.uid)).thenReturn(documentReference)
    `when`(documentReference.delete()).thenReturn(voidTask)
    repository.deleteEventById(event1.uid, {}, { e -> throw e })
  }
}
