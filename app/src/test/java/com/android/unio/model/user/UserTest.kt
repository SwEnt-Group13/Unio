package com.android.unio.model.user

import androidx.test.core.app.ApplicationProvider
import com.android.unio.model.association.Association
import com.google.firebase.FirebaseApp
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.FirebaseFirestore
import junit.framework.TestCase.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.Mockito.`when`
import org.mockito.MockitoAnnotations
import org.mockito.kotlin.any
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class UserTest {
  @Mock private lateinit var db: FirebaseFirestore
  @Mock private lateinit var collectionReference: CollectionReference

  @Before
  fun setUp() {
    MockitoAnnotations.openMocks(this)

    // Initialize Firebase if necessary
    if (FirebaseApp.getApps(ApplicationProvider.getApplicationContext()).isEmpty()) {
      FirebaseApp.initializeApp(ApplicationProvider.getApplicationContext())
    }

    `when`(db.collection(any())).thenReturn(collectionReference)
  }

  @Test
  fun testUser() {
    val user = User("1", "John", "john@example.com", Association.emptyFirestoreReferenceList())
    assertEquals("1", user.uid)
    assertEquals("John", user.name)
    assertEquals("john@example.com", user.email)
  }
}
