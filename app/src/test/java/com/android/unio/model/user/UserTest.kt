package com.android.unio.model.user

import com.android.unio.model.association.AssociationRepositoryFirestore
import com.android.unio.model.firestore.FirestorePaths.ASSOCIATION_PATH
import com.android.unio.model.firestore.FirestoreReferenceList
import com.android.unio.model.firestore.transform.hydrate
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.firestore
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkStatic
import junit.framework.TestCase.assertEquals
import org.junit.Before
import org.junit.Test
import org.mockito.Mock
import org.mockito.MockitoAnnotations

class UserTest {
  private lateinit var db: FirebaseFirestore
  @Mock private lateinit var associationCollectionReference: CollectionReference
  @Mock private lateinit var eventCollectionReference: CollectionReference

  @Before
  fun setUp() {
    MockitoAnnotations.openMocks(this)

    // Use MockK to mock Firebase.firestore calls without dependency injection
    db = mockk()
    mockkStatic(FirebaseFirestore::class)
    every { Firebase.firestore } returns db
    every { db.collection(any()) } returns collectionReference
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
            Association.emptyFirestoreReferenceList(),
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
    assertEquals(emptyList<String>(), user.savedEvents.list.value) // Validate savedEvents
  }
}
