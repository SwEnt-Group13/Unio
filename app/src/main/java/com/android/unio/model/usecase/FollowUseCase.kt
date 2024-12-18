package com.android.unio.model.usecase

import com.android.unio.model.association.Association
import com.android.unio.model.user.User

interface FollowUseCase {

  fun init(onSuccess: () -> Unit)

  fun updateFollow(
      user: User,
      association: Association,
      onSuccess: () -> Unit,
      onFailure: (Exception) -> Unit
  )
}
