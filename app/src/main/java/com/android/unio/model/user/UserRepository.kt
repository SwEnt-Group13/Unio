package com.android.unio.model.user

import com.android.unio.model.association.Association
import com.android.unio.model.event.Event
import com.android.unio.model.firestore.MockReferenceList

interface UserRepository {
  fun init(onSuccess: () -> Unit)

  fun getUsers(onSuccess: (List<User>) -> Unit, onFailure: (Exception) -> Unit)

  fun getUserWithId(id: String, onSuccess: (User) -> Unit, onFailure: (Exception) -> Unit)

  fun listenToSavedEvents(userUid: String, onSavedEventsChanged: (List<String>) -> Unit)

  fun saveEvent(userUid: String, eventUid: String, onSuccess: () -> Unit, onFailure: (Exception) -> Unit)

  fun unsaveEvent(userUid: String, eventUid: String, onSuccess: () -> Unit, onFailure: (Exception) -> Unit)

  fun isEventSaved(userUid: String, eventUid: String, onResult: (Boolean) -> Unit)
}

