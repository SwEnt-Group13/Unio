package com.android.unio.model.usecase

import com.android.unio.model.event.Event
import com.android.unio.model.user.User

interface SaveUseCase {

  fun init(onSuccess: () -> Unit)

  fun updateSave(user: User, event: Event, onSuccess: () -> Unit, onFailure: (Exception) -> Unit)
}
