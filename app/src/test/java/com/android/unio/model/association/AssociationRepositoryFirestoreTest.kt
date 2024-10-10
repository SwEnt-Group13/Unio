package com.android.unio.model.association

import androidx.test.core.app.ApplicationProvider
import com.android.unio.model.firestore.FirestorePaths.USER_PATH
import com.android.unio.model.firestore.FirestoreReferenceList
import com.android.unio.model.user.UserRepositoryFirestore
import com.google.android.gms.tasks.OnSuccessListener
import com.google.android.gms.tasks.Task
import com.google.android.gms.tasks.Tasks
import com.google.firebase.FirebaseApp
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.QueryDocumentSnapshot
import com.google.firebase.firestore.QuerySnapshot
import junit.framework.TestCase.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.Mockito.`when`
import org.mockito.MockitoAnnotations
import org.mockito.kotlin.any
import org.mockito.kotlin.eq
import org.mockito.kotlin.verify
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class AssociationRepositoryFirestoreTest {
  @Mock private lateinit var db: FirebaseFirestore
  @Mock private lateinit var collectionReference: CollectionReference
  @Mock private lateinit var querySnapshot: QuerySnapshot
  @Mock private lateinit var queryDocumentSnapshot1: QueryDocumentSnapshot
  @Mock private lateinit var queryDocumentSnapshot2: QueryDocumentSnapshot
  @Mock private lateinit var documentReference: DocumentReference
  @Mock private lateinit var querySnapshotTask: Task<QuerySnapshot>
  @Mock private lateinit var documentSnapshotTask: Task<DocumentSnapshot>

  private lateinit var repository: AssociationRepositoryFirestore

  private lateinit var association1: Association
  private lateinit var association2: Association

  @Before
  fun setUp() {
    MockitoAnnotations.openMocks(this)

    // Initialize Firebase if necessary
    if (FirebaseApp.getApps(ApplicationProvider.getApplicationContext()).isEmpty()) {
      FirebaseApp.initializeApp(ApplicationProvider.getApplicationContext())
    }

    association1 =
        Association(
            uid = "1",
            acronym = "ACM",
            fullName = "Association for Computing Machinery",
            description =
                "ACM is the world's largest educational and scientific computing society.",
            members =
                FirestoreReferenceList.fromList(
                    listOf("1", "2"), db, USER_PATH, UserRepositoryFirestore::hydrate))

    association2 =
        Association(
            uid = "2",
            acronym = "IEEE",
            fullName = "Institute of Electrical and Electronics Engineers",
            description =
                "IEEE is the world's largest technical professional organization dedicated to advancing technology for the benefit of humanity.",
            members =
                FirestoreReferenceList.fromList(
                    listOf("3", "4"), db, USER_PATH, UserRepositoryFirestore::hydrate))

    // When getting the collection, return the task
    `when`(db.collection(eq("associations"))).thenReturn(collectionReference)
    `when`(collectionReference.get()).thenReturn(querySnapshotTask)
    `when`(collectionReference.document(eq(association1.uid))).thenReturn(documentReference)
    `when`(documentReference.get()).thenReturn(documentSnapshotTask)

    // When the query snapshot is iterated, return the two query document snapshots
    `when`(querySnapshot.iterator())
        .thenReturn(mutableListOf(queryDocumentSnapshot1, queryDocumentSnapshot2).iterator())

    // When the task is successful, return the query snapshot
    `when`(querySnapshotTask.addOnSuccessListener(any())).thenAnswer { invocation ->
      val callback = invocation.arguments[0] as OnSuccessListener<QuerySnapshot>
      callback.onSuccess(querySnapshot)
      querySnapshotTask
    }

    `when`(documentSnapshotTask.addOnSuccessListener(any())).thenAnswer { invocation ->
      val callback = invocation.arguments[0] as OnSuccessListener<DocumentSnapshot>
      callback.onSuccess(queryDocumentSnapshot1)
      documentSnapshotTask
    }

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
          assertEquals(association1.uid, associations[0].uid)
          assertEquals(association1.acronym, associations[0].acronym)
          assertEquals(association1.fullName, associations[0].fullName)
          assertEquals(association1.description, associations[0].description)

          assertEquals(association2.uid, associations[1].uid)
          assertEquals(association2.acronym, associations[1].acronym)
          assertEquals(association2.fullName, associations[1].fullName)
          assertEquals(association2.description, associations[1].description)
        },
        onFailure = { exception -> assert(false) })
  }

  @Test
  fun testGetAssociationsWithMissingFields() {
    // Only set the ID for the second association, leaving the other fields as null
    `when`(queryDocumentSnapshot2.id).thenReturn(association2.uid)

    repository.getAssociations(
        onSuccess = { associations ->
          val emptyAssociation =
              Association(
                  uid = association2.uid,
                  members = FirestoreReferenceList.empty(db, "", UserRepositoryFirestore::hydrate))

          assertEquals(2, associations.size)

          assertEquals(association1.uid, associations[0].uid)
          assertEquals(association1.acronym, associations[0].acronym)
          assertEquals(association1.fullName, associations[0].fullName)
          assertEquals(association1.description, associations[0].description)

          assertEquals(emptyAssociation.uid, associations[1].uid)
          assertEquals("", associations[1].acronym)
          assertEquals("", associations[1].fullName)
          assertEquals("", associations[1].description)
        },
        onFailure = { exception -> assert(false) })
  }

  @Test
  fun testGetAssociationWithId() {
    repository.getAssociationWithId(
        association1.uid,
        onSuccess = { association ->
          assertEquals(association1.uid, association.uid)
          assertEquals(association1.acronym, association.acronym)
          assertEquals(association1.fullName, association.fullName)
          assertEquals(association1.description, association.description)
        },
        onFailure = { exception -> assert(false) })
  }

    @Test
    fun testAddAssociationSuccess() {
        `when`(documentReference.set(association1)).thenReturn(Tasks.forResult(null))

        repository.addAssociation(
            association1,
            onSuccess = { assert(true) },
            onFailure = { assert(false) }
        )

        verify(documentReference).set(association1)
    }

    @Test
    fun testAddAssociationFailure() {
        `when`(documentReference.set(association1)).thenReturn(Tasks.forException(Exception()))

        repository.addAssociation(
            association1,
            onSuccess = { assert(false) },
            onFailure = { assert(true) }
        )

        verify(documentReference).set(association1)
    }

    @Test
    fun testUpdateAssociationSuccess() {
        `when`(documentReference.set(association1)).thenReturn(Tasks.forResult(null))

        repository.updateAssociation(
            association1,
            onSuccess = { assert(true) },
            onFailure = { assert(false) }
        )

        verify(documentReference).set(association1)
    }

    @Test
    fun testUpdateAssociationFailure() {
        `when`(documentReference.set(association1)).thenReturn(Tasks.forException(Exception()))

        repository.updateAssociation(
            association1,
            onSuccess = { assert(false) },
            onFailure = { assert(true) }
        )

        verify(documentReference).set(association1)
    }

    @Test
    fun testDeleteAssociationByIdSuccess() {
        `when`(documentReference.delete()).thenReturn(Tasks.forResult(null))

        repository.deleteAssociationById(
            association1.uid,
            onSuccess = { assert(true) },
            onFailure = { assert(false) }
        )

        verify(documentReference).delete()
    }

    @Test
    fun testDeleteAssociationByIdFailure() {
        `when`(documentReference.delete()).thenReturn(Tasks.forException(Exception()))

        repository.deleteAssociationById(
            association1.uid,
            onSuccess = { assert(false) },
            onFailure = { assert(true) }
        )

        verify(documentReference).delete()
    }
}
