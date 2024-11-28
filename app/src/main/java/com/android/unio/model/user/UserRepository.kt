package com.android.unio.model.user

import com.google.firebase.firestore.DocumentReference

interface UserRepository {
  fun init(onSuccess: () -> Unit)

  fun getUserRef(uid: String): DocumentReference

  fun getUsers(onSuccess: (List<User>) -> Unit, onFailure: (Exception) -> Unit)

  fun getUserWithId(id: String, onSuccess: (User) -> Unit, onFailure: (Exception) -> Unit)

  fun updateUser(user: User, onSuccess: () -> Unit, onFailure: (Exception) -> Unit)

  fun deleteUser(userId: String, onSuccess: () -> Unit, onFailure: (Exception) -> Unit)
}
