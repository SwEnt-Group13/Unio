package com.android.unio.model.user

interface UserRepository {
  fun init(onSuccess: () -> Unit)

  fun getUsers(onSuccess: (List<User>) -> Unit, onFailure: (Exception) -> Unit)

  fun getUserWithId(id: String, onSuccess: (User) -> Unit, onFailure: (Exception) -> Unit)

    fun updateUser(user: User, onSuccess: () -> Unit, onFailure: (Exception) -> Unit)
}
