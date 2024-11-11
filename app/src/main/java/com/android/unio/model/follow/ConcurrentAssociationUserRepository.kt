package com.android.unio.model.follow

import com.android.unio.model.association.Association
import com.android.unio.model.user.User

interface ConcurrentAssociationUserRepository {

  fun updateFollow(
      user: User,
      association: Association,
      onSuccess: () -> Unit,
      onFailure: (Exception) -> Unit
  )
}
