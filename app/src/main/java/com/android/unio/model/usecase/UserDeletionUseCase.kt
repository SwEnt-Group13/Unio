package com.android.unio.model.usecase

import com.android.unio.model.association.Association
import com.android.unio.model.event.Event

interface UserDeletionUseCase {

  fun init(onSuccess: () -> Unit)

  fun deleteUser(
      userId: String,
      eventToUpdate: List<Event>,
      associationToUpdate: List<Association>,
      onSuccess: () -> Unit,
      onFailure: (Exception) -> Unit
  )
}
