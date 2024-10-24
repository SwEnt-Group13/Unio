package com.android.unio.model.user

import com.android.unio.model.association.Association
import com.android.unio.model.firestore.emptyFirestoreReferenceList
import com.google.firebase.Firebase
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
  @Mock private lateinit var collectionReference: CollectionReference

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
            Association.emptyFirestoreReferenceList(),
            listOf(Interest.SPORTS, Interest.MUSIC),
            listOf(
                UserSocial(Social.INSTAGRAM, "Insta"), UserSocial(Social.WEBSITE, "example.com")),
            "https://www.example.com/image",
            true)
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

    @Test
    fun testCheckSocialContent(){
        var userSocialEmptyContent = UserSocial(Social.INSTAGRAM, "")
        assertEquals(1, checkSocialContent(userSocialEmptyContent))

        val userSocialBlankContent = UserSocial(Social.X, "    ")
        assertEquals(1, checkSocialContent(userSocialBlankContent))

        val userSocialWrongNumber = UserSocial(Social.WHATSAPP, "123456789")
        assertEquals(2, checkSocialContent(userSocialWrongNumber))

        val listWrongUserSocialWebsiteURL =
            listOf( UserSocial(Social.WEBSITE, "http://example.com"),
                UserSocial(Social.WEBSITE, "example.com"),
                UserSocial(Social.WEBSITE, "www.example.com"))
        listWrongUserSocialWebsiteURL.forEach{
            assertEquals(3, checkSocialContent(it))
        }

        val userSocialCorrectUsername = UserSocial(Social.INSTAGRAM, "username")
        assertEquals(0, checkSocialContent(userSocialCorrectUsername))

        val userSocialCorrectNumbers = listOf(
            UserSocial(Social.WHATSAPP, "41000000000"),
            UserSocial(Social.WHATSAPP, "33000000000")
        )

        userSocialCorrectNumbers.forEach{
            assertEquals(0, checkSocialContent(it))
        }

        val userSocialCorrectWebsite = UserSocial(Social.WEBSITE, "https://example.com")
        assertEquals(0, checkSocialContent(userSocialCorrectWebsite))
    }
}
