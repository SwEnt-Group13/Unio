package com.android.unio.model.user

import com.android.unio.model.association.Association
import com.android.unio.model.firestore.FirestorePaths.ASSOCIATION_PATH
import com.android.unio.model.firestore.FirestorePaths.USER_PATH
import com.google.android.gms.tasks.OnSuccessListener
import com.google.android.gms.tasks.Task
import com.google.firebase.Firebase
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.QueryDocumentSnapshot
import com.google.firebase.firestore.QuerySnapshot
import com.google.firebase.firestore.firestore
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkStatic
import junit.framework.TestCase.assertEquals
import org.junit.Before
import org.junit.Test
import org.mockito.Mock
import org.mockito.Mockito.`when`
import org.mockito.MockitoAnnotations
import org.mockito.kotlin.any
import org.mockito.kotlin.eq

class UserRepositoryFirestoreTest {
  private lateinit var db: FirebaseFirestore
  @Mock private lateinit var userCollectionReference: CollectionReference
  @Mock private lateinit var associationCollectionReference: CollectionReference
  @Mock private lateinit var querySnapshot: QuerySnapshot
  @Mock private lateinit var queryDocumentSnapshot1: QueryDocumentSnapshot
  @Mock private lateinit var map1: Map<String, Any>
  @Mock private lateinit var queryDocumentSnapshot2: QueryDocumentSnapshot
  @Mock private lateinit var map2: Map<String, Any>
  @Mock private lateinit var documentReference: DocumentReference
  @Mock private lateinit var querySnapshotTask: Task<QuerySnapshot>
  @Mock private lateinit var documentSnapshotTask: Task<DocumentSnapshot>

  private lateinit var repository: UserRepositoryFirestore

  private lateinit var user1: User
  private lateinit var user2: User

  @Before
  fun setUp() {
    MockitoAnnotations.openMocks(this)

    db = mockk()
    mockkStatic(FirebaseFirestore::class)
    every { Firebase.firestore } returns db

    // When getting the collection, return the task
    every { db.collection(USER_PATH) } returns userCollectionReference
    every { db.collection(ASSOCIATION_PATH) } returns associationCollectionReference

    user1 =
        User(
            uid = "1",
            email = "example1@abcd.com",
            name = "Example 1",
            followingAssociations = Association.emptyFirestoreReferenceList())

    user2 =
        User(
            uid = "2",
            email = "example2@abcd.com",
            name = "Example 2",
            followingAssociations = Association.emptyFirestoreReferenceList())

    `when`(userCollectionReference.get()).thenReturn(querySnapshotTask)
    `when`(userCollectionReference.document(eq(user1.uid))).thenReturn(documentReference)
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

    `when`(queryDocumentSnapshot1.data).thenReturn(map1)
    `when`(queryDocumentSnapshot2.data).thenReturn(map2)

    `when`(map1.get("uid")).thenReturn(user1.uid)
    `when`(map1.get("name")).thenReturn(user1.name)
    `when`(map1.get("email")).thenReturn(user1.email)
    `when`(map1.get("followingAssociations"))
        .thenReturn(user1.followingAssociations.list.value.map { it.uid })

    // Only set the uid field for user2
    `when`(map2.get("uid")).thenReturn(user2.uid)

    repository = UserRepositoryFirestore(db)
  }

  @Test
  fun testGetUsers() {
    `when`(map2.get("name")).thenReturn(user2.name)
    `when`(map2.get("email")).thenReturn(user2.email)
    `when`(map2.get("followingAssociations"))
        .thenReturn(user2.followingAssociations.list.value.map { it.uid })

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
    // No specific fields are set for user2

    repository.getUsers(
        onSuccess = { users ->
          val emptyUser =
              User(
                  uid = user2.uid,
                  email = "",
                  name = "",
                  followingAssociations = Association.emptyFirestoreReferenceList())
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
