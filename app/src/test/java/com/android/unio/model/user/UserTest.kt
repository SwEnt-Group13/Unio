package com.android.unio.model.user

import com.android.unio.model.association.AssociationRepositoryFirestore
import com.android.unio.model.firestore.FirestorePaths.ASSOCIATION_PATH
import com.android.unio.model.firestore.FirestoreReferenceList
import com.android.unio.model.firestore.transform.hydrate
import com.android.unio.model.event.EventRepositoryFirestore
import com.android.unio.model.firestore.FirestorePaths.EVENT_PATH
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.FirebaseFirestore
import junit.framework.TestCase.assertEquals
import org.junit.Before
import org.junit.Test
import org.mockito.Mock
import org.mockito.Mockito.`when`
import org.mockito.MockitoAnnotations
import org.mockito.kotlin.any

class UserTest {
    @Mock private lateinit var db: FirebaseFirestore
    @Mock private lateinit var associationCollectionReference: CollectionReference
    @Mock private lateinit var eventCollectionReference: CollectionReference

    @Before
    fun setUp() {
        MockitoAnnotations.openMocks(this)

        `when`(db.collection(any())).thenReturn(associationCollectionReference)
        `when`(db.collection(EVENT_PATH)).thenReturn(eventCollectionReference)
    }

    @Test
    fun testUser() {
        val user = User(
            uid = "1",
            name = "John",
            email = "john@example.com",
            followingAssociations = FirestoreReferenceList.empty(
                db.collection(ASSOCIATION_PATH), AssociationRepositoryFirestore::hydrate
            ),
            savedEvents = FirestoreReferenceList.empty(
                db.collection(EVENT_PATH), EventRepositoryFirestore::hydrate // Initialize savedEvents
            )
        )

        assertEquals("1", user.uid)
        assertEquals("John", user.name)
        assertEquals("john@example.com", user.email)
        assertEquals(emptyList<String>(), user.savedEvents.list.value) // Validate savedEvents
    }
}
