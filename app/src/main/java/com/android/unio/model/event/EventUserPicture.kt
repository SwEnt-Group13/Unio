package com.android.unio.model.event

import com.android.unio.model.firestore.UniquelyIdentifiable
import com.android.unio.model.user.User

data class EventUserPicture(
    override var uid: String,
    var image: String,
    val author: User,
    val likes: Int
) : UniquelyIdentifiable {
  companion object
}
