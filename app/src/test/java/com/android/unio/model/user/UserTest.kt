package com.android.unio.model.user

import com.android.unio.mocks.user.MockUser
import com.google.firebase.Firebase
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.firestore
import io.mockk.clearAllMocks
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.unmockkAll
import junit.framework.TestCase.assertEquals
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.MockitoAnnotations
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
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
        MockUser.createMockUser(
            uid = "1",
            email = "john@example.com",
            firstName = "John",
            lastName = "Doe",
            biography = "An example user",
            interests = listOf(Interest.SPORTS, Interest.MUSIC),
            socials =
                listOf(
                    UserSocial(Social.INSTAGRAM, "Insta"),
                    UserSocial(Social.WEBSITE, "example.com")),
            profilePicture = "https://www.example.com/image")
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
  fun testCheckNewUser() {
    val userEmptyFirstName = MockUser.createMockUser(firstName = "")

    val userEmptyLastName = MockUser.createMockUser(lastName = "")

    val userEmptyNameAndLastName = MockUser.createMockUser(firstName = "", lastName = "")
    val expectedErrors1 = mutableSetOf(AccountDetailsError.EMPTY_FIRST_NAME)
    val expectedErrors2 = mutableSetOf(AccountDetailsError.EMPTY_LAST_NAME)
    val expectedErrors3 =
        mutableSetOf(AccountDetailsError.EMPTY_FIRST_NAME, AccountDetailsError.EMPTY_LAST_NAME)

    assertEquals(expectedErrors1, checkNewUser(userEmptyFirstName))
    assertEquals(expectedErrors2, checkNewUser(userEmptyLastName))
    assertEquals(expectedErrors3, checkNewUser(userEmptyNameAndLastName))
  }

  @Test
  fun testCheckSocialContent() {
    var userSocialEmptyContent = UserSocial(Social.INSTAGRAM, "")
    assertEquals(UserSocialError.EMPTY_FIELD, checkSocialContent(userSocialEmptyContent))

    val userSocialBlankContent = UserSocial(Social.X, "    ")
    assertEquals(UserSocialError.EMPTY_FIELD, checkSocialContent(userSocialBlankContent))

    val userSocialWrongNumber =
        listOf(
            UserSocial(Social.WHATSAPP, "123456789"),
            UserSocial(Social.WHATSAPP, "12345678901234567890"))

    userSocialWrongNumber.forEach {
      assertEquals(UserSocialError.INVALID_PHONE_NUMBER, checkSocialContent(it))
    }

    val listWrongUserSocialWebsiteURL =
        listOf(
            UserSocial(Social.WEBSITE, "http://example.com"),
            UserSocial(Social.WEBSITE, "example.com"),
            UserSocial(Social.WEBSITE, "www.example.com"))
    listWrongUserSocialWebsiteURL.forEach {
      assertEquals(UserSocialError.INVALID_WEBSITE, checkSocialContent(it))
    }

    val userSocialCorrectUsername = UserSocial(Social.INSTAGRAM, "username")
    assertEquals(UserSocialError.NONE, checkSocialContent(userSocialCorrectUsername))

    val userSocialCorrectNumbers =
        listOf(
            UserSocial(Social.WHATSAPP, "41000000000"), UserSocial(Social.WHATSAPP, "33000000000"))

    userSocialCorrectNumbers.forEach { assertEquals(UserSocialError.NONE, checkSocialContent(it)) }

    val userSocialCorrectWebsite = UserSocial(Social.WEBSITE, "https://example.com")
    assertEquals(UserSocialError.NONE, checkSocialContent(userSocialCorrectWebsite))
  }

  @Test
  fun checkNewImageUri() {
    val emptyString = ""
    assertEquals(ImageUriType.EMPTY, checkImageUri(emptyString))

    val localUri = "content://mySuperLocalImage"
    assertEquals(ImageUriType.LOCAL, checkImageUri(localUri))

    val remoteUri = "https://firebasestorage.googleapis.com/blablabla"
    assertEquals(ImageUriType.REMOTE, checkImageUri(remoteUri))
  }

  @After
  fun tearDown() {
    // Clean up
    unmockkAll()
    clearAllMocks()
  }
}
