package com.android.unio.model.event

import android.os.Looper
import com.android.unio.mocks.event.MockEvent
import com.android.unio.model.firestore.FirestorePaths.EVENT_PATH
import com.google.android.gms.tasks.OnSuccessListener
import com.google.android.gms.tasks.Task
import com.google.firebase.Firebase
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.auth
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
import junit.framework.TestCase.assertTrue
import org.junit.Assert.assertFalse
import org.junit.Assert.assertThrows
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.ArgumentCaptor
import org.mockito.Captor
import org.mockito.Mock
import org.mockito.Mockito.verify
import org.mockito.Mockito.`when`
import org.mockito.MockitoAnnotations
import org.mockito.kotlin.any
import org.robolectric.RobolectricTestRunner
import org.robolectric.Shadows.shadowOf

@RunWith(RobolectricTestRunner::class)
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

  @Mock private lateinit var auth: FirebaseAuth
  @Mock private lateinit var firebaseUser: FirebaseUser
  @Captor
  private lateinit var authStateListenerCaptor: ArgumentCaptor<FirebaseAuth.AuthStateListener>

  private lateinit var repository: EventRepositoryFirestore
  private val event1 =
      MockEvent.createMockEvent(
          uid = "1",
          title = "Balelec",
          startDate = Timestamp(GregorianCalendar(2004, 7, 1).time),
          endDate = Timestamp(GregorianCalendar(2005, 7, 1).time))
  private val defaultEvent =
      MockEvent.createMockEvent(
          uid = "", title = "Default Event") // This will simulate the default event
  private val event3 =
      MockEvent.createMockEvent(
          uid = "3",
          title = "Tremplin Sysmic",
          startDate = Timestamp(GregorianCalendar(2004, 7, 1).time),
          endDate = Timestamp(GregorianCalendar(2005, 7, 1).time))

  @Before
  fun setUp() {
    MockitoAnnotations.openMocks(this)

    // When getting the collection, return the task
    db = mockk()
    mockkStatic(FirebaseFirestore::class)
    every { Firebase.firestore } returns db
    every { db.collection(EVENT_PATH) } returns collectionReference

    mockkStatic(FirebaseAuth::class)
    every { Firebase.auth } returns auth

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

  @Test
  fun testInitUserAuthenticated() {
    `when`(auth.currentUser).thenReturn(firebaseUser)
    var onSuccessCalled = false
    val onSuccess = { onSuccessCalled = true }

    repository.init(onSuccess)

    verify(auth).addAuthStateListener(authStateListenerCaptor.capture())
    authStateListenerCaptor.value.onAuthStateChanged(auth)

    shadowOf(Looper.getMainLooper()).idle()

    assertTrue(onSuccessCalled)
  }

  @Test
  fun testInitUserNotAuthenticated() {
    `when`(auth.currentUser).thenReturn(null)
    var onSuccessCalled = false
    val onSuccess = { onSuccessCalled = true }

    repository.init(onSuccess)

    verify(auth).addAuthStateListener(authStateListenerCaptor.capture())
    authStateListenerCaptor.value.onAuthStateChanged(auth)

    shadowOf(Looper.getMainLooper()).idle()

    assertFalse(onSuccessCalled)
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
    `when`(voidTask.addOnSuccessListener(any())).thenReturn(voidTask)
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
    `when`(voidTask.addOnSuccessListener(any())).thenReturn(voidTask)
    `when`(documentReference.delete()).thenReturn(voidTask)
    repository.deleteEventById(event1.uid, {}, { e -> throw e })
  }
}
