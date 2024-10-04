package com.android.unio.model

import com.google.android.gms.tasks.OnSuccessListener
import com.google.android.gms.tasks.Task
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.QueryDocumentSnapshot
import com.google.firebase.firestore.QuerySnapshot
import junit.framework.TestCase.assertEquals
import org.junit.Before
import org.junit.Test
import org.mockito.Mock
import org.mockito.Mockito.`when`
import org.mockito.MockitoAnnotations
import org.mockito.kotlin.any
import org.mockito.kotlin.eq

class AssociationRepositoryFirestoreTest {
  @Mock private lateinit var db: FirebaseFirestore
  @Mock private lateinit var collectionReference: CollectionReference
  @Mock private lateinit var querySnapshot: QuerySnapshot
  @Mock private lateinit var queryDocumentSnapshot1: QueryDocumentSnapshot
  @Mock private lateinit var queryDocumentSnapshot2: QueryDocumentSnapshot
  @Mock private lateinit var task: Task<QuerySnapshot>

  private lateinit var repository: AssociationRepositoryFirestore

  private val association1 =
      Association(
          uid = "1",
          acronym = "ACM",
          fullName = "Association for Computing Machinery",
          description = "ACM is the world's largest educational and scientific computing society.",
          members = mutableListOf("1", "2"))

  private val association2 =
      Association(
          uid = "2",
          acronym = "IEEE",
          fullName = "Institute of Electrical and Electronics Engineers",
          description =
              "IEEE is the world's largest technical professional organization dedicated to advancing technology for the benefit of humanity.",
          members = mutableListOf("3", "4"))

  @Before
  fun setUp() {
    MockitoAnnotations.openMocks(this)

    // When getting the collection, return the task
    `when`(db.collection(eq("associations"))).thenReturn(collectionReference)
    `when`(collectionReference.get()).thenReturn(task)

    // When the task is successful, return the query snapshot
    `when`(task.addOnSuccessListener(any())).thenAnswer { invocation ->
      val callback = invocation.arguments[0] as OnSuccessListener<QuerySnapshot>
      callback.onSuccess(querySnapshot)
      task
    }

    // When the query snapshot is iterated, return the two query document snapshots
    `when`(querySnapshot.iterator())
        .thenReturn(mutableListOf(queryDocumentSnapshot1, queryDocumentSnapshot2).iterator())

    // When the query document snapshots are queried for specific fields, return the fields
    `when`(queryDocumentSnapshot1.id).thenReturn(association1.uid)
    `when`(queryDocumentSnapshot1.getString("acronym")).thenReturn(association1.acronym)
    `when`(queryDocumentSnapshot1.getString("fullName")).thenReturn(association1.fullName)
    `when`(queryDocumentSnapshot1.getString("description")).thenReturn(association1.description)
    `when`(queryDocumentSnapshot1.get("members")).thenReturn(association1.members)

    repository = AssociationRepositoryFirestore(db)
  }

  @Test
  fun testGetAssociations() {
    `when`(queryDocumentSnapshot2.id).thenReturn(association2.uid)
    `when`(queryDocumentSnapshot2.getString("acronym")).thenReturn(association2.acronym)
    `when`(queryDocumentSnapshot2.getString("fullName")).thenReturn(association2.fullName)
    `when`(queryDocumentSnapshot2.getString("description")).thenReturn(association2.description)
    `when`(queryDocumentSnapshot2.get("members")).thenReturn(association2.members)

    repository.getAssociations(
        onSuccess = { associations ->
          assertEquals(2, associations.size)
          assertEquals(association1, associations[0])
          assertEquals(association2, associations[1])
        },
        onFailure = { exception -> assert(false) })
  }

  @Test
  fun testGetAssociationsWithMissingFields() {
    // Only set the ID for the second association, leaving the other fields as null
    `when`(queryDocumentSnapshot2.id).thenReturn(association2.uid)

    repository.getAssociations(
        onSuccess = { associations ->
          assertEquals(2, associations.size)
          assertEquals(association1, associations[0])
          assertEquals(
              Association(
                  uid = association2.uid,
                  acronym = "",
                  fullName = "",
                  description = "",
                  members = emptyList()),
              associations[1])
        },
        onFailure = { exception -> assert(false) })

  }
}
