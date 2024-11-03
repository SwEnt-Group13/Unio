package com.android.unio.ui.event

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import com.android.unio.mocks.event.MockEvent
import com.android.unio.mocks.map.MockLocation
import com.android.unio.mocks.user.MockUser
import com.android.unio.model.association.Association
import com.android.unio.model.event.Event
import com.android.unio.model.event.EventType
import com.android.unio.model.firestore.emptyFirestoreReferenceList
import com.android.unio.model.firestore.firestoreReferenceListWith
import com.android.unio.model.map.Location
import com.android.unio.model.user.Interest
import com.android.unio.model.user.Social
import com.android.unio.model.user.User
import com.android.unio.model.user.UserRepository
import com.android.unio.model.user.UserRepositoryFirestore
import com.android.unio.model.user.UserSocial
import com.android.unio.model.user.UserViewModel
import com.google.firebase.Firebase
import com.google.firebase.Timestamp
import com.google.firebase.firestore.firestore
import java.util.*
import org.junit.Rule
import org.junit.Test

class EventCardTest {

  @get:Rule val composeTestRule = createComposeRule()

  private val sampleEvent = MockEvent.createMockEvent(uid = "sample_event_123", location = MockLocation.createMockLocation(name="Sample Location"), date= Timestamp(Date(2024 - 1900, 6, 20)), catchyDescription = "This is a catchy description.")

  private val userViewModel = UserViewModel(UserRepositoryFirestore(Firebase.firestore), false)

  @Test
  fun testEventCardElementsExist() {
    composeTestRule.setContent { EventCard(event = sampleEvent, userViewModel = userViewModel) }

    composeTestRule
        .onNodeWithTag("event_EventTitle", useUnmergedTree = true)
        .assertExists()
        .assertTextEquals("Sample Event")

    composeTestRule
        .onNodeWithTag("event_EventMainType", useUnmergedTree = true)
        .assertExists()
        .assertTextEquals(EventType.TRIP.text)

    composeTestRule
        .onNodeWithTag("event_EventLocation", useUnmergedTree = true)
        .assertExists()
        .assertTextEquals("Sample Location")

    composeTestRule
        .onNodeWithTag("event_EventDate", useUnmergedTree = true)
        .assertExists()
        .assertTextEquals("20/07")

    composeTestRule
        .onNodeWithTag("event_EventTime", useUnmergedTree = true)
        .assertExists()
        .assertTextEquals("00:00")

    composeTestRule
        .onNodeWithTag("event_EventCatchyDescription", useUnmergedTree = true)
        .assertExists()
        .assertTextEquals("This is a catchy description.")
  }

  @Test
  fun testImageFallbackDisplayed() {
    composeTestRule.setContent { EventCard(event = sampleEvent, userViewModel = userViewModel) }

    // Check if the fallback image is displayed when no image is provided
    composeTestRule
        .onNodeWithTag("event_EventImage", useUnmergedTree = true)
        .assertExists() // Fallback image exists when no image is provided
  }
}

class MockUserRepository : UserRepository {
  private val mockUsers = listOf(MockUser.createMockUser(uid="1"), MockUser.createMockUser(uid="2"))
      /*listOf(
          User(
              uid = "1",
              email = "john.doe@example.com",
              firstName = "John",
              lastName = "Doe",
              biography = "Just a regular guy.",
              followedAssociations = Association.emptyFirestoreReferenceList(),
              joinedAssociations = Association.emptyFirestoreReferenceList(),
              savedEvents = Event.emptyFirestoreReferenceList(),
              interests = listOf(Interest.SPORTS, Interest.MUSIC),
              socials =
                  listOf(
                      UserSocial(Social.INSTAGRAM, "john_doe"),
                      UserSocial(Social.WHATSAPP, "john.doe.whatsapp")),
              profilePicture = "https://example.com/profile_picture1.jpg"),
          User(
              uid = "2",
              email = "jane.smith@example.com",
              firstName = "Jane",
              lastName = "Smith",
              biography = "Lover of arts and technology.",
              followedAssociations = Association.emptyFirestoreReferenceList(),
              joinedAssociations = Association.emptyFirestoreReferenceList(),
              savedEvents = Event.emptyFirestoreReferenceList(),
              interests = listOf(Interest.ART, Interest.TECHNOLOGY),
              socials =
                  listOf(
                      UserSocial(Social.TELEGRAM, "jane_smith"),
                      UserSocial(Social.TELEGRAM, "jane.smith.telegram")),
              profilePicture = "https://example.com/profile_picture2.jpg"))*/

  override fun init(onSuccess: () -> Unit) {
    onSuccess()
  }

  override fun getUsers(onSuccess: (List<User>) -> Unit, onFailure: (Exception) -> Unit) {
    onSuccess(mockUsers)
  }

  override fun getUserWithId(
      id: String,
      onSuccess: (User) -> Unit,
      onFailure: (Exception) -> Unit
  ) {
    val user = mockUsers.find { it.uid == id }
    if (user != null) {
      onSuccess(user)
    } else {
      onFailure(Exception("User not found"))
    }
  }

  override fun updateUser(user: User, onSuccess: () -> Unit, onFailure: (Exception) -> Unit) {}
}
