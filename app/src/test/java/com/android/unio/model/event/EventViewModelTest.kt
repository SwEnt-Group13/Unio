package com.android.unio.model.event

import androidx.lifecycle.ViewModel
import androidx.test.core.app.ApplicationProvider
import com.android.unio.model.firestore.ReferenceList
import com.android.unio.model.map.Location
import com.android.unio.model.user.UserRepositoryFirestore
import com.google.firebase.FirebaseApp
import com.google.firebase.Timestamp
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.FirebaseFirestore
import java.io.InputStream
import java.util.GregorianCalendar
import junit.framework.Assert.assertTrue
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
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
  @Mock private lateinit var usrRepository: UserRepositoryFirestore
  @Mock private lateinit var db: FirebaseFirestore
  @Mock private lateinit var collectionReference: CollectionReference
  @Mock private lateinit var inputStream: InputStream

  private lateinit var eventViewModel: EventViewModel

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

  @Before
  fun setUp() {
    MockitoAnnotations.openMocks(this)

    if (FirebaseApp.getApps(ApplicationProvider.getApplicationContext()).isEmpty()) {
      FirebaseApp.initializeApp(ApplicationProvider.getApplicationContext())
    }
    `when`(db.collection(any())).thenReturn(collectionReference)

    eventViewModel = EventViewModel(repository, usrRepository)
  }

  @Test
  fun addEventTest() {
    `when`(repository.addEvent(eq(event1), any(), any())).thenAnswer { invocation ->
      val onSuccess = invocation.arguments[0] as () -> Unit
      onSuccess()
    }
    eventViewModel.addEvent(
        inputStream, event1, { verify(repository).addEvent(eq(event1), any(), any()) }, {})
  }

  @Test
  fun `Factory creates EventViewModel with correct dependencies`() {
    val viewModel = EventViewModel.Factory.create(EventViewModel::class.java)
    assertTrue(viewModel is EventViewModel)

    val eventViewModel = viewModel as EventViewModel
    assertTrue(eventViewModel.repository is EventRepositoryFirestore)
    assertTrue(eventViewModel.userRepository is UserRepositoryFirestore)
  }

  @Test(expected = IllegalArgumentException::class)
  fun `Factory throws IllegalArgumentException for unsupported ViewModel class`() {
    EventViewModel.Factory.create(UnsupportedViewModel::class.java)
  }

  class UnsupportedViewModel : ViewModel()
}

class MockReferenceList<T>(elements: List<T> = emptyList()) : ReferenceList<T> {
  private val _list = MutableStateFlow(elements)
  override val list: StateFlow<List<T>> = _list

  override fun add(uid: String) {}

  override fun addAll(uids: List<String>) {}

  override fun remove(uid: String) {}

  override fun requestAll(onSuccess: () -> Unit) {}

  override fun contains(uid: String): Boolean {
    return false
  }
}
