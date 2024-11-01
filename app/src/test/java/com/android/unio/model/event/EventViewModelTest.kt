package com.android.unio.model.event

import androidx.test.core.app.ApplicationProvider
import com.android.unio.model.firestore.MockReferenceList
import com.android.unio.model.map.Location
import com.google.firebase.FirebaseApp
import com.google.firebase.Timestamp
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.FirebaseFirestore
import java.io.InputStream
import java.util.GregorianCalendar
import junit.framework.TestCase.assertEquals
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.Mockito.verify
import org.mockito.Mockito.`when`
import org.mockito.MockitoAnnotations
import org.mockito.kotlin.any
import org.mockito.kotlin.eq
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class EventViewModelTest {
  @Mock private lateinit var repository: EventRepositoryFirestore
  @Mock private lateinit var db: FirebaseFirestore
  @Mock private lateinit var collectionReference: CollectionReference
  @Mock private lateinit var inputStream: InputStream

  private lateinit var viewModel: EventListViewModel

  private val event1 =
      Event(
          uid = "1",
          title = "Balelec",
          organisers = MockReferenceList(),
          taggedAssociations = MockReferenceList(),
          image = "https://imageurl.jpg",
          description = "Plus grand festival du monde (non contractuel)",
          price = 40.5,
          date = Timestamp(GregorianCalendar(2004, 7, 1).time),
          location = Location(1.2345, 2.3455, "Somewhere"))
  private val event3 =
      Event(
          uid = "3",
          title = "Tremplin Sysmic",
          organisers = MockReferenceList(),
          taggedAssociations = MockReferenceList(),
          image = "https://imageurl.jpg",
          description = "Plus grand festival du monde (non contractuel)",
          price = 40.5,
          date = Timestamp(GregorianCalendar(2008, 7, 1).time),
          location = Location(1.2345, 2.3455, "Somewhere"))

  private val testEvents = listOf(event1, event3)

  @Before
  fun setUp() {
    MockitoAnnotations.openMocks(this)

    if (FirebaseApp.getApps(ApplicationProvider.getApplicationContext()).isEmpty()) {
      FirebaseApp.initializeApp(ApplicationProvider.getApplicationContext())
    }
    `when`(db.collection(any())).thenReturn(collectionReference)

    viewModel = EventListViewModel(repository)
  }

  @Test
  fun addEventTest() {
    `when`(repository.addEvent(eq(event1), any(), any())).thenAnswer { invocation ->
      val onSuccess = invocation.arguments[0] as () -> Unit
      onSuccess()
    }
    viewModel.addEvent(
        inputStream, event1, { verify(repository).addEvent(eq(event1), any(), any()) }, {})
  }

  @Test
  fun testFindAssociationById() {
    `when`(repository.getEvents(any(), any())).thenAnswer { invocation ->
      val onSuccess = invocation.arguments[0] as (List<Event>) -> Unit
      onSuccess(testEvents)
    }

    viewModel.loadEvents()
    assertEquals(testEvents, viewModel.events.value)

    runBlocking {
      val result = viewModel.events.first()

      assertEquals(2, result.size)
      assertEquals("Balelec", result[0].title)
      assertEquals("Tremplin Sysmic", result[1].title)
    }

    assertEquals(testEvents[0], viewModel.findEventById("1"))
    assertEquals(testEvents[1], viewModel.findEventById("3"))
    assertEquals(null, viewModel.findEventById("2"))
  }
}
