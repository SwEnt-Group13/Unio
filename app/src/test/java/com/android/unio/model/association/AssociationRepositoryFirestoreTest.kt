package com.android.unio.model.association

import android.os.Looper
import com.android.unio.mocks.association.MockAssociation
import com.android.unio.model.event.Event
import com.android.unio.model.firestore.FirestorePaths.ASSOCIATION_PATH
import com.android.unio.model.firestore.FirestorePaths.USER_PATH
import com.android.unio.model.firestore.emptyFirestoreReferenceList
import com.android.unio.model.user.User
import com.google.android.gms.tasks.OnSuccessListener
import com.google.android.gms.tasks.Task
import com.google.android.gms.tasks.Tasks
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuth.AuthStateListener
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.auth
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.MetadataChanges
import com.google.firebase.firestore.QueryDocumentSnapshot
import com.google.firebase.firestore.QuerySnapshot
import com.google.firebase.firestore.firestore
import emptyFirestoreReferenceElement
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
import org.mockito.MockitoAnnotations
import org.robolectric.RobolectricTestRunner
import org.robolectric.Shadows.shadowOf

@RunWith(RobolectricTestRunner::class)
class AssociationRepositoryFirestoreTest {
  private lateinit var db: FirebaseFirestore

  @MockK private lateinit var associationCollectionReference: CollectionReference

  @MockK private lateinit var userCollectionReference: CollectionReference

  @MockK private lateinit var querySnapshot: QuerySnapshot

  @MockK private lateinit var queryDocumentSnapshot1: QueryDocumentSnapshot

  @MockK private lateinit var queryDocumentSnapshot2: QueryDocumentSnapshot

  @MockK private lateinit var documentReference: DocumentReference

  @MockK private lateinit var querySnapshotTask: Task<QuerySnapshot>

  @MockK private lateinit var documentSnapshotTask: Task<DocumentSnapshot>

  @MockK private lateinit var auth: FirebaseAuth

  @MockK private lateinit var firebaseUser: FirebaseUser

  private lateinit var repository: AssociationRepositoryFirestore

  private lateinit var association1: Association
  private lateinit var association2: Association
  private lateinit var map1: Map<String, Any>
  private lateinit var map2: Map<String, Any>

  @Before
  fun setUp() {
    MockitoAnnotations.openMocks(this)
    MockKAnnotations.init(this)

    db = mockk()
    mockkStatic(FirebaseFirestore::class)
    every { Firebase.firestore } returns db
    every { db.collection(ASSOCIATION_PATH) } returns associationCollectionReference
    every { db.collection(USER_PATH) } returns userCollectionReference

    mockkStatic(FirebaseAuth::class)
    every { FirebaseAuth.getInstance() } returns auth
    every { Firebase.auth } returns auth
    every { auth.addAuthStateListener(any<AuthStateListener>()) } answers
        { call ->
          if (auth.currentUser != null) {
            val listener = call.invocation.args[0] as AuthStateListener
            listener.onAuthStateChanged(auth)
          }
        }

    association1 =
        MockAssociation.createMockAssociation(
            category = AssociationCategory.SCIENCE_TECH,
            members = listOf(Member(User.emptyFirestoreReferenceElement(), Role.ADMIN)))
    association2 =
        MockAssociation.createMockAssociation(category = AssociationCategory.SCIENCE_TECH)

    // When getting the collection, return the task
    every { associationCollectionReference.get() } returns (querySnapshotTask)
    every { associationCollectionReference.document(eq(association1.uid)) } returns
        (documentReference)

    every { documentReference.get() } returns documentSnapshotTask
    every { documentReference.set(any()) } returns Tasks.forResult(null)
    every { documentReference.addSnapshotListener(any<MetadataChanges>(), any()) } returns mockk()

    // When the query snapshot is iterated, return the two query document snapshots
    every { querySnapshot.iterator() } returns
        mutableListOf(queryDocumentSnapshot1, queryDocumentSnapshot2).iterator()

    // When the task is successful, return the query snapshot
    every { querySnapshotTask.addOnSuccessListener(any()) } answers
        { call ->
          val callback = call.invocation.args[0] as OnSuccessListener<QuerySnapshot>
          callback.onSuccess(querySnapshot)
          querySnapshotTask
        }
    every { querySnapshotTask.addOnFailureListener(any()) } answers { querySnapshotTask }

    every { documentReference.addSnapshotListener(any()) } returns mockk()

    every { documentSnapshotTask.addOnSuccessListener(any()) } answers
        { call ->
          val callback = call.invocation.args[0] as OnSuccessListener<DocumentSnapshot>
          callback.onSuccess(queryDocumentSnapshot1)
          documentSnapshotTask
        }

    // Set up mock data maps
    map1 =
        mapOf(
            "uid" to association1.uid,
            "url" to association1.url,
            "name" to association1.name,
            "fullName" to association1.fullName,
            "category" to association1.category.name,
            "description" to association1.description,
            "members" to
                mapOf(
                    "1" to "Guest",
                    "2" to "Guest"), // the serialization process does not allow us to simply put
            // association1.members
            "roles" to
                mapOf(
                    "Guest" to
                        mapOf("displayName" to "Guest", "permissions" to listOf("Full rights")),
                    "Administrator" to
                        mapOf(
                            "displayName" to "Administrator",
                            "permissions" to listOf("Full rights"))),
            "followersCount" to association1.followersCount,
            "image" to association1.image,
            "events" to association1.events.uids,
            "principalEmailAddress" to association1.principalEmailAddress)

    map2 =
        mapOf(
            "uid" to association2.uid,
            "url" to association2.url,
            "name" to association2.name,
            "fullName" to association2.fullName,
            "category" to association2.category.name,
            "description" to association2.description,
            "members" to mapOf("1" to "Guest", "2" to "Guest"),
            "roles" to
                mapOf(
                    "Guest" to
                        mapOf("displayName" to "Guest", "permissions" to listOf("Full rights")),
                    "Administrator" to
                        mapOf(
                            "displayName" to "Administrator",
                            "permissions" to listOf("Full rights"))),
            "followersCount" to association2.followersCount,
            "image" to association2.image,
            "events" to association2.events.uids,
            "principalEmailAddress" to association2.principalEmailAddress)

    every { queryDocumentSnapshot1.data } returns (map1)

    repository = AssociationRepositoryFirestore(db)
  }

  @Test
  fun testInitUserAuthenticated() {
    every { auth.currentUser } returns (firebaseUser)
    var onSuccessCalled = false
    val onSuccess = { onSuccessCalled = true }

    repository.init(onSuccess)

    verify { auth.addAuthStateListener(any()) }

    shadowOf(Looper.getMainLooper()).idle()

    assertTrue(onSuccessCalled)
  }

  @Test
  fun testInitUserNotAuthenticated() {
    every { auth.currentUser } returns (null)
    var onSuccessCalled = false
    val onSuccess = { onSuccessCalled = true }

    repository.init(onSuccess)

    verify { auth.addAuthStateListener(any()) }

    shadowOf(Looper.getMainLooper()).idle()

    assertFalse(onSuccessCalled)
  }

  @Test
  fun testGetAssociations() {
    every { queryDocumentSnapshot2.data } returns (map2)
    var success = false

    repository.getAssociations(
        onSuccess = { associations ->
          assertEquals(2, associations.size)
          assertEquals(association1.uid, associations[0].uid)
          assertEquals(association1.name, associations[0].name)
          assertEquals(association1.fullName, associations[0].fullName)
          assertEquals(association1.description, associations[0].description)
          assertEquals(
              association1.members.map { it.uid }.toSet(),
              associations[0].members.map { it.uid }.toSet())
          assertEquals(
              association1.roles.map { it.uid }.toSet(),
              associations[0].roles.map { it.uid }.toSet())

          assertEquals(association2.uid, associations[1].uid)
          assertEquals(association2.name, associations[1].name)
          assertEquals(association2.fullName, associations[1].fullName)
          assertEquals(association2.description, associations[1].description)
          assertEquals(
              association2.roles.map { it.uid }.toSet(),
              associations[1].roles.map { it.uid }.toSet())
          assertEquals(association2.members.map { it.uid }, associations[1].members.map { it.uid })
          success = true
        },
        onFailure = { assert(false) })
    assert(success)
  }

  @Test
  fun testGetAssociationsWithMissingFields() {
    // Only set the ID for the second association, leaving the other fields as null
    every { queryDocumentSnapshot2.data } returns (mapOf("uid" to association2.uid))
    var success = false

    repository.getAssociations(
        onSuccess = { associations ->
          val emptyAssociation =
              Association(
                  uid = association2.uid,
                  url = "",
                  name = "",
                  fullName = "",
                  category = AssociationCategory.ARTS,
                  description = "",
                  members = listOf(Member(User.emptyFirestoreReferenceElement(), Role.GUEST)),
                  roles = listOf(Role.GUEST),
                  followersCount = 0,
                  image = "",
                  events = Event.emptyFirestoreReferenceList(),
                  principalEmailAddress = "")

          assertEquals(2, associations.size)

          assertEquals(association1.uid, associations[0].uid)
          assertEquals(association1.name, associations[0].name)
          assertEquals(association1.fullName, associations[0].fullName)
          assertEquals(association1.description, associations[0].description)

          assertEquals(emptyAssociation.uid, associations[1].uid)
          assertEquals("", associations[1].name)
          assertEquals("", associations[1].fullName)
          assertEquals("", associations[1].description)
          success = true
        },
        onFailure = { assert(false) })
    assert(success)
  }

  @Test
  fun testGetAssociationWithId() {
    every { queryDocumentSnapshot1.exists() } returns (true)
    var success = false
    repository.getAssociationWithId(
        association1.uid,
        onSuccess = { association ->
          assertEquals(association1.uid, association.uid)
          assertEquals(association1.name, association.name)
          assertEquals(association1.fullName, association.fullName)
          assertEquals(association1.description, association.description)
          success = true
        },
        onFailure = { exception -> assert(false) })
    assert(success)
  }

  @Test
  fun testGetAssociationsByCategory() {
    every { queryDocumentSnapshot2.data } returns (map2)
    every { associationCollectionReference.whereEqualTo(eq("category"), any()) } returns
        (associationCollectionReference)
    var success = false
    repository.getAssociationsByCategory(
        AssociationCategory.SCIENCE_TECH,
        onSuccess = { associations ->
          for (asso in associations) {
            assertEquals(asso.category, AssociationCategory.SCIENCE_TECH)
          }
          success = true
        },
        onFailure = { assert(false) })
    assert(success)
  }

  @Test
  fun testAddAssociationSuccess() {
    every { documentReference.set(map1) } returns (Tasks.forResult(null))

    repository.saveAssociation(
        association1, onSuccess = { assert(true) }, onFailure = { assert(false) })

    verify { documentReference.set(map1) }
  }

  @Test
  fun testAddAssociationFailure() {
    every { documentReference.set(any()) } returns (Tasks.forException(Exception()))

    repository.saveAssociation(
        association1, onSuccess = { assert(false) }, onFailure = { assert(true) })

    verify { documentReference.set(map1) }
  }

  @Test
  fun testUpdateAssociationSuccess() {
    every { documentReference.set(any()) } returns (Tasks.forResult(null))

    repository.saveAssociation(
        association1, onSuccess = { assert(true) }, onFailure = { assert(false) })

    verify { documentReference.set(map1) }
  }

  @Test
  fun testUpdateAssociationFailure() {
    every { documentReference.set(any()) } returns (Tasks.forException(Exception()))

    repository.saveAssociation(
        association1, onSuccess = { assert(false) }, onFailure = { assert(true) })

    verify { documentReference.set(map1) }
  }

  @Test
  fun testDeleteAssociationByIdSuccess() {
    every { documentReference.delete() } returns (Tasks.forResult(null))

    repository.deleteAssociationById(
        association1.uid, onSuccess = { assert(true) }, onFailure = { assert(false) })

    verify { documentReference.delete() }
  }

  @Test
  fun testDeleteAssociationByIdFailure() {
    every { documentReference.delete() } returns (Tasks.forException(Exception()))

    repository.deleteAssociationById(
        association1.uid, onSuccess = { assert(false) }, onFailure = { assert(true) })

    verify { documentReference.delete() }
  }
}
