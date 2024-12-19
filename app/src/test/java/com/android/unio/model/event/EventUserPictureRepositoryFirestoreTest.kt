//package com.android.unio.model.event
//
//import com.android.unio.model.firestore.FirestorePaths.EVENT_USER_PICTURES_PATH
//import com.android.unio.model.user.User
//import com.google.android.gms.tasks.Task
//import com.google.firebase.Firebase
//import com.google.firebase.firestore.CollectionReference
//import com.google.firebase.firestore.DocumentReference
//import com.google.firebase.firestore.FirebaseFirestore
//import com.google.firebase.firestore.firestore
//import firestoreReferenceElementWith
//import io.mockk.MockKAnnotations
//import io.mockk.every
//import io.mockk.mockk
//import io.mockk.mockkStatic
//import org.junit.Assert.assertEquals
//import org.junit.Before
//import org.junit.Test
//import org.mockito.Mock
//import org.mockito.Mockito.`when`
//import org.mockito.MockitoAnnotations
//import org.mockito.kotlin.any
//
//class EventUserPictureRepositoryFirestoreTest {
//  private lateinit var db: FirebaseFirestore
//  private lateinit var eventUserPictureRepository: EventUserPictureRepositoryFirestore
//
//  @Mock private lateinit var collectionReference: CollectionReference
//  @Mock private lateinit var documentReference: DocumentReference
//  @Mock private lateinit var voidTask: Task<Void>
//
//  private val eventUserPicture =
//      EventUserPicture(
//          uid = "1",
//          image = "http://image.fr",
//          author = User.firestoreReferenceElementWith("1"),
//          likes = 2)
//
//  @Before
//  fun setUp() {
//    MockitoAnnotations.openMocks(this)
//    MockKAnnotations.init(this, relaxed = true)
//    db = mockk()
//    mockkStatic(FirebaseFirestore::class)
//    every { Firebase.firestore } returns db
//    every { db.collection(EVENT_USER_PICTURES_PATH) } returns collectionReference
//
//    eventUserPictureRepository = EventUserPictureRepositoryFirestore(db)
//  }
//
//  @Test
//  fun testAddEventUserPicture() {
//    `when`(collectionReference.document(eventUserPicture.uid)).thenReturn(documentReference)
//    `when`(voidTask.addOnSuccessListener(any())).thenReturn(voidTask)
//    `when`(documentReference.set(any())).thenReturn(voidTask)
//    eventUserPictureRepository.addEventUserPicture(
//        eventUserPicture, onSuccess = {}, onFailure = { e -> throw e })
//  }
//
//  @Test
//  fun testGetNewUid() {
//    val testUid = "TOTALLYNEWUID"
//    `when`(collectionReference.document()).thenReturn(documentReference)
//    `when`(documentReference.id).thenReturn(testUid)
//    assertEquals(eventUserPictureRepository.getNewUid(), testUid)
//  }
//
//  @Test
//  fun testDeleteEventById() {
//    `when`(collectionReference.document(eventUserPicture.uid)).thenReturn(documentReference)
//    `when`(voidTask.addOnSuccessListener(any())).thenReturn(voidTask)
//    `when`(documentReference.delete()).thenReturn(voidTask)
//    eventUserPictureRepository.deleteEventUserPictureById(
//        eventUserPicture.uid, {}, { e -> throw e })
//  }
//}
