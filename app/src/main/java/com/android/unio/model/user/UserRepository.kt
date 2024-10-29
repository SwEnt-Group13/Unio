package com.android.unio.model.user

import com.android.unio.model.association.Association
import com.android.unio.model.event.Event

interface UserRepository {
  fun init(onSuccess: () -> Unit)

  fun getUsers(onSuccess: (List<User>) -> Unit, onFailure: (Exception) -> Unit)

  fun getUserWithId(id: String, onSuccess: (User) -> Unit, onFailure: (Exception) -> Unit)

  fun updateUser(user: User, onSuccess: () -> Unit, onFailure: (Exception) -> Unit)
}

