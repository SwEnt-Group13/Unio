package com.android.unio.model.user

import com.android.unio.model.association.AssociationRepositoryFirestore
import com.android.unio.model.firestore.FirestoreReferenceList
import com.google.firebase.firestore.FirebaseFirestore
import junit.framework.TestCase.assertEquals
import org.junit.Before
import org.junit.Test
import org.mockito.Mock
import org.mockito.MockitoAnnotations

class UserTest {
  @Mock private lateinit var db: FirebaseFirestore

  @Before
  fun setUp() {
    MockitoAnnotations.openMocks(this)
  }

  @Test
  fun testUser() {
    val user =
        User(
            "1",
            "John",
            "john@example.com",
            FirestoreReferenceList.empty(
                db, "associations", AssociationRepositoryFirestore::hydrate))
    assertEquals("1", user.id)
    assertEquals("John", user.name)
    assertEquals("john@example.com", user.email)
  }
}
