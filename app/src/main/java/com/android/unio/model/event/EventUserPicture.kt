package com.android.unio.model.event

import com.android.unio.model.firestore.UniquelyIdentifiable
import com.android.unio.model.user.User

data class EventUserPicture(
    override val uid: String,
    val image: String,
    val author: User,
    val likes: Int
) : UniquelyIdentifiable {
  companion object
}
