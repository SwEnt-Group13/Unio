package com.android.unio.model.user

import com.android.unio.model.association.AssociationRepositoryFirestore
import com.android.unio.model.firestore.FirestorePaths.ASSOCIATION_PATH
import com.android.unio.model.firestore.FirestoreReferenceList
import com.android.unio.model.firestore.transform.hydrate
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
  @Mock private lateinit var collectionReference: CollectionReference

  @Before
  fun setUp() {
    MockitoAnnotations.openMocks(this)

    `when`(db.collection(any())).thenReturn(collectionReference)
  }

  @Test
  fun testUser() {
    val user =
        User(
            "1",
            "john@example.com",
            "John",
            "Doe",
            "An example user",
            FirestoreReferenceList.empty(
                db.collection(ASSOCIATION_PATH), AssociationRepositoryFirestore::hydrate),
            listOf(Interest.SPORTS, Interest.MUSIC),
            listOf(
                UserSocial(Social.INSTAGRAM, "Insta"), UserSocial(Social.WEBSITE, "example.com")),
            "https://www.example.com/image")
    assertEquals("1", user.uid)
    assertEquals("john@example.com", user.email)
    assertEquals("John", user.firstName)
    assertEquals("Doe", user.lastName)
    assertEquals("An example user", user.biography)
    assertEquals(listOf(Interest.SPORTS, Interest.MUSIC), user.interests)
    assertEquals(
        listOf(UserSocial(Social.INSTAGRAM, "Insta"), UserSocial(Social.WEBSITE, "example.com")),
        user.socials)
    assertEquals("https://www.example.com/image", user.profilePicture)
  }
}
