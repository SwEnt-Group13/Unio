package com.android.unio.model.firestore

import com.google.android.gms.tasks.OnSuccessListener
import com.google.android.gms.tasks.Task
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.mockito.Mock
import org.mockito.MockitoAnnotations
import org.mockito.kotlin.any
import org.mockito.kotlin.timeout
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever

class FirestoreReferenceListTest {

  @Mock private lateinit var mockFirestore: FirebaseFirestore
  @Mock private lateinit var mockCollectionRef: CollectionReference
  @Mock private lateinit var mockDocumentRef: DocumentReference
  @Mock private lateinit var mockSnapshot: DocumentSnapshot
  @Mock private lateinit var mockTask: Task<DocumentSnapshot>
  @Mock private lateinit var firestoreReferenceList: FirestoreReferenceList<String>

  @Before
  fun setup() {
    MockitoAnnotations.openMocks(this)

    whenever(mockFirestore.collection(any())).thenReturn(mockCollectionRef)
    whenever(mockCollectionRef.document(any())).thenReturn(mockDocumentRef)
    whenever(mockDocumentRef.get()).thenReturn(mockTask)
    whenever(mockTask.addOnSuccessListener(any())).thenAnswer { invocation ->
      val thread = Thread {
        Thread.sleep(100)
        val callback = invocation.arguments[0] as OnSuccessListener<DocumentSnapshot>
        callback.onSuccess(mockSnapshot)
      }
      thread.start()
      mockTask
    }

    firestoreReferenceList =
        FirestoreReferenceList(mockFirestore, "testPath") { snapshot ->
          snapshot.getString("data") ?: ""
        }
  }

  @Test
  fun `test requestAll fetches documents and updates list`() = runTest {
    whenever(mockSnapshot.getString("data")).thenReturn("Item1", "Item2")

    // Add UIDs and call requestAll
    firestoreReferenceList.addAll(listOf("uid1", "uid2"))
    firestoreReferenceList.requestAll()

    // Verify firestore calls after 200ms
    verify(mockFirestore, timeout(200).times(2)).collection("testPath")
    verify(mockCollectionRef, timeout(200).times(2)).document(any())
    verify(mockDocumentRef, timeout(200).times(2)).get()
  }

  @Test
  fun `test requestAll clears list before updating`() = runTest {
    // Set initial state
    firestoreReferenceList.addAll(listOf("uid1", "uid2"))
    firestoreReferenceList.requestAll()

    // Verify the list is cleared
    assertEquals(0, firestoreReferenceList.list.value.size)
  }

  @Test
  fun `test fromList creates FirestoreReferenceList with UIDs`() = runTest {
    val list = listOf("uid1", "uid2")
    val fromList =
        FirestoreReferenceList.fromList(list, mockFirestore, "testPath") { snapshot ->
          snapshot.getString("data") ?: ""
        }

    assertEquals(emptyList<String>(), fromList.list.first())
  }

  @Test
  fun `test empty creates FirestoreReferenceList without UIDs`() = runTest {
    val emptyList =
        FirestoreReferenceList.empty(mockFirestore, "testPath") { snapshot ->
          snapshot.getString("data") ?: ""
        }

    assertEquals(emptyList<String>(), emptyList.list.first())
  }
}
