package com.android.unio.model.user

import android.os.Looper
import com.android.unio.model.association.Association
import com.android.unio.model.event.Event
import com.android.unio.model.firestore.FirestorePaths.ASSOCIATION_PATH
import com.android.unio.model.firestore.FirestorePaths.USER_PATH
import com.android.unio.model.firestore.emptyFirestoreReferenceList
import com.google.android.gms.tasks.OnSuccessListener
import com.google.android.gms.tasks.Task
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.QueryDocumentSnapshot
import com.google.firebase.firestore.QuerySnapshot
import com.google.firebase.firestore.firestore
import io.mockk.MockKAnnotations
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.verify
import junit.framework.TestCase.assertEquals
import junit.framework.TestCase.assertTrue
import org.junit.Assert.assertFalse
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.Shadows.shadowOf

@RunWith(RobolectricTestRunner::class)
class UserRepositoryFirestoreTest {
  private lateinit var db: FirebaseFirestore
  @MockK private lateinit var userCollectionReference: CollectionReference
  @MockK private lateinit var associationCollectionReference: CollectionReference
  @MockK private lateinit var eventCollectionReference: CollectionReference
  @MockK private lateinit var querySnapshot: QuerySnapshot
  @MockK private lateinit var queryDocumentSnapshot1: QueryDocumentSnapshot
  @MockK private lateinit var map1: Map<String, Any>
  @MockK private lateinit var queryDocumentSnapshot2: QueryDocumentSnapshot
  @MockK private lateinit var map2: Map<String, Any>
  @MockK private lateinit var documentReference: DocumentReference
  @MockK private lateinit var querySnapshotTask: Task<QuerySnapshot>
  @MockK private lateinit var documentSnapshotTask: Task<DocumentSnapshot>

  @MockK private lateinit var auth: FirebaseAuth
  @MockK private lateinit var firebaseUser: FirebaseUser

  private lateinit var repository: UserRepositoryFirestore

  private lateinit var user1: User
  private lateinit var user2: User

  @Before
  fun setUp() {
    //    MockitoAnnotations.openMocks(this)
    MockKAnnotations.init(this)

    db = mockk()
    mockkStatic(FirebaseFirestore::class)
    every { Firebase.firestore } returns db

    mockkStatic(FirebaseAuth::class)
    every { FirebaseAuth.getInstance() } returns auth

    // When getting the collection, return the task
    every { db.collection(USER_PATH) } returns userCollectionReference
    every { db.collection(ASSOCIATION_PATH) } returns associationCollectionReference

    user1 =
        User(
            uid = "1",
            email = "example1@abcd.com",
            firstName = "Example 1",
            lastName = "Last name 1",
            biography = "An example user",
            followedAssociations = Association.emptyFirestoreReferenceList(),
            joinedAssociations = Association.emptyFirestoreReferenceList(),
            savedEvents = Event.emptyFirestoreReferenceList(),
            interests = listOf(Interest.SPORTS, Interest.MUSIC),
            socials =
                listOf(
                    UserSocial(Social.INSTAGRAM, "Insta"),
                    UserSocial(Social.WEBSITE, "example.com")),
            profilePicture = "https://www.example.com/image")

    user2 =
        User(
            uid = "2",
            email = "example2@abcd.com",
            firstName = "Example 2",
            lastName = "Last name 2",
            biography = "An example user 2",
            followedAssociations = Association.emptyFirestoreReferenceList(),
            joinedAssociations = Association.emptyFirestoreReferenceList(),
            savedEvents = Event.emptyFirestoreReferenceList(),
            interests = listOf(Interest.FESTIVALS, Interest.GAMING),
            socials =
                listOf(
                    UserSocial(Social.SNAPCHAT, "Snap"),
                    UserSocial(Social.WEBSITE, "example2.com")),
            profilePicture = "https://www.example.com/image2")

    every { (userCollectionReference.get()) } returns (querySnapshotTask)
    every { (userCollectionReference.document(eq(user1.uid))) } returns (documentReference)
    every { (documentReference.get()) } returns (documentSnapshotTask)

    // When the query snapshot is iterated, return the two query document snapshots
    every { (querySnapshot.iterator()) } returns
        (mutableListOf(queryDocumentSnapshot1, queryDocumentSnapshot2).iterator())

    // When the task is successful, return the query snapshot
    every { (querySnapshotTask.addOnSuccessListener(any())) } answers
        { call ->
          val callback = call.invocation.args[0] as OnSuccessListener<QuerySnapshot>
          callback.onSuccess(querySnapshot)
          querySnapshotTask
        }
    every { querySnapshotTask.addOnFailureListener(any()) } answers { querySnapshotTask }

    every { (documentSnapshotTask.addOnSuccessListener(any())) } answers
        { call ->
          val callback = call.invocation.args[0] as OnSuccessListener<DocumentSnapshot>
          callback.onSuccess(queryDocumentSnapshot1)
          documentSnapshotTask
        }
    every { documentSnapshotTask.addOnFailureListener(any()) } answers { documentSnapshotTask }

    // When the query document snapshots are queried for specific fields, return the fields

    every { (queryDocumentSnapshot1.data) } returns (map1)
    every { (queryDocumentSnapshot2.data) } returns (map2)

    every { (map1.get("uid")) } returns (user1.uid)
    every { (map1.get("email")) } returns (user1.email)
    every { (map1.get("firstName")) } returns (user1.firstName)
    every { (map1.get("lastName")) } returns (user1.lastName)
    every { (map1.get("biography")) } returns (user1.biography)
    every { (map1.get("followedAssociations")) } returns
        (user1.followedAssociations.list.value.map { it.uid })
    every { (map1.get("joinedAssociations")) } returns
        (user1.joinedAssociations.list.value.map { it.uid })
    every { (map1.get("interests")) } returns (user1.interests.map { it.name })
    every { (map1.get("socials")) } returns
        (user1.socials.map { mapOf("social" to it.social.name, "content" to it.content) })
    every { (map1.get("profilePicture")) } returns (user1.profilePicture)

    // Only set the uid field for user2
    every { (map2.get("uid")) } returns (user2.uid)

    repository = UserRepositoryFirestore(db)
  }

  @Test
  fun testInitUserAuthenticated() {
    every { (auth.currentUser) } returns (firebaseUser)
    var onSuccessCalled = false
    val onSuccess = { onSuccessCalled = true }

    repository.init(onSuccess)

    // Capture listener and trigger it
    verify { auth.addAuthStateListener(any()) }

    shadowOf(Looper.getMainLooper()).idle()

    assertTrue(onSuccessCalled)
  }

  @Test
  fun testInitUserNotAuthenticated() {
    every { (auth.currentUser) } returns (null)
    var onSuccessCalled = false
    val onSuccess = { onSuccessCalled = true }

    repository.init(onSuccess)

    // Capture listener and trigger it
    verify { auth.addAuthStateListener(any()) }

    shadowOf(Looper.getMainLooper()).idle()

    assertFalse(onSuccessCalled)
  }

  @Test
  fun testGetUsers() {
    every { map2.get("email") } returns (user2.email)
    every { (map2.get("firstName")) } returns (user2.firstName)
    every { (map2.get("lastName")) } returns (user2.lastName)
    every { (map2.get("biography")) } returns (user2.biography)
    every { (map2.get("followedAssociations")) } returns
        (user2.followedAssociations.list.value.map { it.uid })
    every { (map2.get("joinedAssociations")) } returns
        (user2.joinedAssociations.list.value.map { it.uid })
    every { (map2.get("interests")) } returns (user2.interests.map { it.name })
    every { (map2.get("socials")) } returns
        (user2.socials.map { mapOf("social" to it.social.name, "content" to it.content) })
    every { (map2.get("profilePicture")) } returns (user2.profilePicture)

    var success = false

    repository.getUsers(
        onSuccess = { users ->
          assertEquals(2, users.size)

          assertEquals(user1.uid, users[0].uid)
          assertEquals(user1.email, users[0].email)
          assertEquals(user1.firstName, users[0].firstName)
          assertEquals(user1.lastName, users[0].lastName)
          assertEquals(user1.biography, users[0].biography)
          assertEquals(
              user1.followedAssociations.list.value.map { it.uid },
              users[0].followedAssociations.list.value.map { it.uid })
          assertEquals(
              user1.joinedAssociations.list.value.map { it.uid },
              users[0].joinedAssociations.list.value.map { it.uid })
          assertEquals(user1.interests.map { it.name }, users[0].interests.map { it.name })
          assertEquals(
              user1.socials.map { mapOf("social" to it.social.name, "content" to it.content) },
              users[0].socials.map { mapOf("social" to it.social.name, "content" to it.content) })
          assertEquals(user1.profilePicture, users[0].profilePicture)

          assertEquals(user2.uid, users[1].uid)
          assertEquals(user2.email, users[1].email)
          assertEquals(user2.firstName, users[1].firstName)
          assertEquals(user2.lastName, users[1].lastName)
          assertEquals(user2.biography, users[1].biography)
          assertEquals(
              user2.followedAssociations.list.value.map { it.uid },
              users[1].followedAssociations.list.value.map { it.uid })
          assertEquals(
              user2.joinedAssociations.list.value.map { it.uid },
              users[1].joinedAssociations.list.value.map { it.uid })
          assertEquals(user2.interests.map { it.name }, users[1].interests.map { it.name })
          assertEquals(
              user2.socials.map { mapOf("social" to it.social.name, "content" to it.content) },
              users[1].socials.map { mapOf("social" to it.social.name, "content" to it.content) })
          assertEquals(user2.profilePicture, users[1].profilePicture)
          success = true
        },
        onFailure = { exception -> assert(false) })
    assert(success)
  }

  @Test
  fun testGetAssociationsWithMissingFields() {
    // No specific fields are set for user2

    var success = false

    repository.getUsers(
        onSuccess = { users ->
          val emptyUser =
              User(
                  uid = user2.uid,
                  email = "",
                  firstName = "",
                  lastName = "",
                  biography = "",
                  followedAssociations = Association.emptyFirestoreReferenceList(),
                  joinedAssociations = Association.emptyFirestoreReferenceList(),
                  savedEvents = Event.emptyFirestoreReferenceList(),
                  interests = emptyList(),
                  socials = emptyList(),
                  profilePicture = "")
          assertEquals(2, users.size)

          assertEquals(user1.uid, users[0].uid)
          assertEquals(user1.email, users[0].email)
          assertEquals(user1.firstName, users[0].firstName)
          assertEquals(user1.lastName, users[0].lastName)
          assertEquals(user1.biography, users[0].biography)
          assertEquals(
              user1.followedAssociations.list.value.map { it.uid },
              users[0].followedAssociations.list.value.map { it.uid })
          assertEquals(
              user1.joinedAssociations.list.value.map { it.uid },
              users[0].joinedAssociations.list.value.map { it.uid })
          assertEquals(user1.interests.map { it.name }, users[0].interests.map { it.name })
          assertEquals(
              user1.socials.map { mapOf("social" to it.social.name, "content" to it.content) },
              users[0].socials.map { mapOf("social" to it.social.name, "content" to it.content) })
          assertEquals(user1.profilePicture, users[0].profilePicture)

          assertEquals(emptyUser.uid, users[1].uid)
          assertEquals("", users[1].email)
          assertEquals("", users[1].firstName)
          assertEquals("", users[1].lastName)
          assertEquals("", users[1].biography)
          assertEquals(
              emptyUser.followedAssociations.list.value.map { it.uid },
              users[1].followedAssociations.list.value.map { it.uid })
          assertEquals(
              emptyUser.joinedAssociations.list.value.map { it.uid },
              users[1].joinedAssociations.list.value.map { it.uid })
          assertEquals(emptyUser.interests.map { it.name }, users[1].interests.map { it.name })
          assertEquals(
              emptyUser.socials.map { mapOf("social" to it.social.name, "content" to it.content) },
              users[1].socials.map { mapOf("social" to it.social.name, "content" to it.content) })
          assertEquals(emptyUser.profilePicture, users[1].profilePicture)
          success = true
        },
        onFailure = { exception -> assert(false) })
    assert(success)
  }

  @Test
  fun testGetUserWithId() {
    every { (queryDocumentSnapshot1.exists()) } returns (true)
    var success = false
    repository.getUserWithId(
        id = user1.uid,
        onSuccess = { user ->
          assertEquals(user1.uid, user.uid)
          assertEquals(user1.email, user.email)
          assertEquals(user1.firstName, user.firstName)
          assertEquals(user1.lastName, user.lastName)
          assertEquals(user1.biography, user.biography)
          assertEquals(
              user1.followedAssociations.list.value.map { it.uid },
              user.followedAssociations.list.value.map { it.uid })
          assertEquals(
              user1.joinedAssociations.list.value.map { it.uid },
              user.joinedAssociations.list.value.map { it.uid })
          assertEquals(user1.interests.map { it.name }, user.interests.map { it.name })
          assertEquals(
              user1.socials.map { mapOf("social" to it.social.name, "content" to it.content) },
              user.socials.map { mapOf("social" to it.social.name, "content" to it.content) })
          assertEquals(user1.profilePicture, user.profilePicture)
          success = true
        },
        onFailure = { exception -> assert(false) })
    assert(success)
  }
}
