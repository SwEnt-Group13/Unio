package com.android.unio.model.user

import com.android.unio.model.association.Association
import com.android.unio.model.event.Event

interface UserDeletionRepository {

  fun init(onSuccess: () -> Unit)

  fun deleteUser(
      userId: String,
      eventToUpdate: List<Event>,
      associationToUpdate: List<Association>,
      onSuccess: () -> Unit,
      onFailure: (Exception) -> Unit
  )
}
