package com.android.unio.model.user

import androidx.test.core.app.ApplicationProvider
import com.android.unio.model.association.AssociationRepositoryFirestore
import com.android.unio.model.firestore.FirestoreReferenceList
import com.google.android.gms.tasks.OnSuccessListener
import com.google.android.gms.tasks.Task
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
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class UserRepositoryFirestoreTest {
  @Mock private lateinit var db: FirebaseFirestore
  @Mock private lateinit var collectionReference: CollectionReference
  @Mock private lateinit var querySnapshot: QuerySnapshot
  @Mock private lateinit var queryDocumentSnapshot1: QueryDocumentSnapshot
  @Mock private lateinit var queryDocumentSnapshot2: QueryDocumentSnapshot
  @Mock private lateinit var documentReference: DocumentReference
  @Mock private lateinit var querySnapshotTask: Task<QuerySnapshot>
  @Mock private lateinit var documentSnapshotTask: Task<DocumentSnapshot>

  private lateinit var repository: UserRepositoryFirestore

  private lateinit var user1: User
  private lateinit var user2: User

  @Before
  fun setUp() {
    MockitoAnnotations.openMocks(this)

    // Initialize Firebase if necessary
    if (FirebaseApp.getApps(ApplicationProvider.getApplicationContext()).isEmpty()) {
      FirebaseApp.initializeApp(ApplicationProvider.getApplicationContext())
    }

    user1 =
        User(
            uid = "1",
            email = "example1@abcd.com",
            name = "Example 1",
            followingAssociations =
                FirestoreReferenceList.empty(db, "", AssociationRepositoryFirestore::hydrate))

    user2 =
        User(
            uid = "2",
            email = "example2@abcd.com",
            name = "Example 2",
            followingAssociations =
                FirestoreReferenceList.empty(db, "", AssociationRepositoryFirestore::hydrate))

    // When getting the collection, return the task
    `when`(db.collection(eq("users"))).thenReturn(collectionReference)
    `when`(collectionReference.get()).thenReturn(querySnapshotTask)
    `when`(collectionReference.document(eq(user1.uid))).thenReturn(documentReference)
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
    `when`(queryDocumentSnapshot1.id).thenReturn(user1.uid)
    `when`(queryDocumentSnapshot1.getString("name")).thenReturn(user1.name)
    `when`(queryDocumentSnapshot1.getString("email")).thenReturn(user1.email)
    `when`(queryDocumentSnapshot1.get("followingAssociations"))
        .thenReturn(user1.followingAssociations)

    repository = UserRepositoryFirestore(db)
  }

  @Test
  fun testGetUsers() {

    `when`(queryDocumentSnapshot2.id).thenReturn(user2.uid)
    `when`(queryDocumentSnapshot2.getString("name")).thenReturn(user2.name)
    `when`(queryDocumentSnapshot2.getString("email")).thenReturn(user2.email)
    `when`(queryDocumentSnapshot2.get("followingAssociations"))
        .thenReturn(user2.followingAssociations)

    repository.getUsers(
        onSuccess = { users ->
          assertEquals(2, users.size)

          assertEquals(user1.uid, users[0].uid)
          assertEquals(user1.name, users[0].name)
          assertEquals(user1.email, users[0].email)

          assertEquals(user2.uid, users[1].uid)
          assertEquals(user2.name, users[1].name)
          assertEquals(user2.email, users[1].email)
        },
        onFailure = { exception -> assert(false) })
  }

  @Test
  fun testGetAssociationsWithMissingFields() {
    // Only set the ID for the second association, leaving the other fields as null
    `when`(queryDocumentSnapshot2.id).thenReturn(user2.uid)

    repository.getUsers(
        onSuccess = { users ->
          val emptyUser =
              User(
                  uid = user2.uid,
                  email = "",
                  name = "",
                  followingAssociations =
                      FirestoreReferenceList.empty(db, "", AssociationRepositoryFirestore::hydrate))
          assertEquals(2, users.size)

          assertEquals(user1.uid, users[0].uid)
          assertEquals(user1.name, users[0].name)
          assertEquals(user1.email, users[0].email)

          assertEquals(emptyUser.uid, users[1].uid)
          assertEquals("", users[1].name)
          assertEquals("", users[1].email)
        },
        onFailure = { exception -> assert(false) })
  }

  @Test
  fun testGetUserWithId() {
    repository.getUserWithId(
        id = user1.uid,
        onSuccess = { user ->
          assertEquals(user1.uid, user.uid)
          assertEquals(user1.name, user.name)
          assertEquals(user1.email, user.email)
        },
        onFailure = { exception -> assert(false) })
  }
}
