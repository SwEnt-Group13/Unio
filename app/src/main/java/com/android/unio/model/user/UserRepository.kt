package com.android.unio.model.user

interface UserRepository {
  fun init(onSuccess: () -> Unit)

  fun getUsers(onSuccess: (List<User>) -> Unit, onFailure: (Exception) -> Unit)

  fun getUserWithId(userId: String, onSuccess: (User) -> Unit, onFailure: (Exception) -> Unit)

  fun saveEvent(
      userId: String,
      eventId: String,
      onSuccess: () -> Unit,
      onFailure: (Exception) -> Unit
  )
}
