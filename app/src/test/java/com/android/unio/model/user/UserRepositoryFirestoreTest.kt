package com.android.unio.model.user

import com.android.unio.model.association.Association
import com.android.unio.model.firestore.FirestorePaths.ASSOCIATION_PATH
import com.android.unio.model.firestore.FirestorePaths.USER_PATH
import com.android.unio.model.firestore.emptyFirestoreReferenceList
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
            firstName = "Example 1",
            lastName = "Last name 1",
            biography = "An example user",
            followedAssociations = Association.emptyFirestoreReferenceList(),
            joinedAssociations = Association.emptyFirestoreReferenceList(),
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
            interests = listOf(Interest.FESTIVALS, Interest.GAMING),
            socials =
                listOf(
                    UserSocial(Social.SNAPCHAT, "Snap"),
                    UserSocial(Social.WEBSITE, "example2.com")),
            profilePicture = "https://www.example.com/image2")

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
    `when`(map1.get("email")).thenReturn(user1.email)
    `when`(map1.get("firstName")).thenReturn(user1.firstName)
    `when`(map1.get("lastName")).thenReturn(user1.lastName)
    `when`(map1.get("biography")).thenReturn(user1.biography)
    `when`(map1.get("followedAssociations"))
        .thenReturn(user1.followedAssociations.list.value.map { it.uid })
    `when`(map1.get("joinedAssociations"))
        .thenReturn(user1.joinedAssociations.list.value.map { it.uid })
    `when`(map1.get("interests")).thenReturn(user1.interests.map { it.name })
    `when`(map1.get("socials"))
        .thenReturn(
            user1.socials.map { mapOf("social" to it.social.name, "content" to it.content) })
    `when`(map1.get("profilePicture")).thenReturn(user1.profilePicture)

    // Only set the uid field for user2
    `when`(map2.get("uid")).thenReturn(user2.uid)

    repository = UserRepositoryFirestore(db)
  }

  @Test
  fun testGetUsers() {
    `when`(map2.get("email")).thenReturn(user2.email)
    `when`(map2.get("firstName")).thenReturn(user2.firstName)
    `when`(map2.get("lastName")).thenReturn(user2.lastName)
    `when`(map2.get("biography")).thenReturn(user2.biography)
    `when`(map2.get("followedAssociations"))
        .thenReturn(user2.followedAssociations.list.value.map { it.uid })
    `when`(map2.get("joinedAssociations"))
        .thenReturn(user2.joinedAssociations.list.value.map { it.uid })
    `when`(map2.get("interests")).thenReturn(user2.interests.map { it.name })
    `when`(map2.get("socials"))
        .thenReturn(
            user2.socials.map { mapOf("social" to it.social.name, "content" to it.content) })
    `when`(map2.get("profilePicture")).thenReturn(user2.profilePicture)

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
                  firstName = "",
                  lastName = "",
                  biography = "",
                  followedAssociations = Association.emptyFirestoreReferenceList(),
                  joinedAssociations = Association.emptyFirestoreReferenceList(),
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
        },
        onFailure = { exception -> assert(false) })
  }

  @Test
  fun testGetUserWithId() {
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
        },
        onFailure = { exception -> assert(false) })
  }
}
