package com.android.unio.model.user

import com.android.unio.model.association.Association
import com.android.unio.model.event.Event
import com.android.unio.model.firestore.MockReferenceList

interface UserRepository {
  fun init(onSuccess: () -> Unit)

  fun getUsers(onSuccess: (List<User>) -> Unit, onFailure: (Exception) -> Unit)

  fun getUserWithId(id: String, onSuccess: (User) -> Unit, onFailure: (Exception) -> Unit)

  fun listenToSavedEvents(userUid: String, onSavedEventsChanged: (List<String>) -> Unit)

  fun saveEvent(
      userUid: String,
      eventUid: String,
      onSuccess: () -> Unit,
      onFailure: (Exception) -> Unit
  )

  fun unsaveEvent(
      userUid: String,
      eventUid: String,
      onSuccess: () -> Unit,
      onFailure: (Exception) -> Unit
  )

  fun isEventSaved(userUid: String, eventUid: String, onResult: (Boolean) -> Unit)

  fun updateUser(user: User, onSuccess: () -> Unit, onFailure: (Exception) -> Unit)
}

class MockUserRepository : UserRepository {
  private val mockUsers =
      listOf(
          User(
              uid = "1",
              email = "john.doe@example.com",
              firstName = "John",
              lastName = "Doe",
              biography = "Just a regular guy.",
              followingAssociations = MockReferenceList<Association>(),
              savedEvents = MockReferenceList<Event>(),
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
              followingAssociations = MockReferenceList<Association>(),
              savedEvents = MockReferenceList<Event>(),
              interests = listOf(Interest.ART, Interest.TECHNOLOGY),
              socials =
                  listOf(
                      UserSocial(Social.DISCORD, "jane_smith"),
                      UserSocial(Social.TELEGRAM, "jane.smith.telegram")),
              profilePicture = "https://example.com/profile_picture2.jpg"))

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

  override fun listenToSavedEvents(userUid: String, onSavedEventsChanged: (List<String>) -> Unit) {
    onSavedEventsChanged(listOf("1", "2"))
  }

  override fun saveEvent(
      userUid: String,
      eventUid: String,
      onSuccess: () -> Unit,
      onFailure: (Exception) -> Unit
  ) {
    onSuccess()
  }

  override fun unsaveEvent(
      userUid: String,
      eventUid: String,
      onSuccess: () -> Unit,
      onFailure: (Exception) -> Unit
  ) {
    onSuccess()
  }

  override fun isEventSaved(userUid: String, eventUid: String, onResult: (Boolean) -> Unit) {
    onResult(false)
  }

  override fun updateUser(user: User, onSuccess: () -> Unit, onFailure: (Exception) -> Unit) {
    TODO("Not yet implemented")
  }
}
