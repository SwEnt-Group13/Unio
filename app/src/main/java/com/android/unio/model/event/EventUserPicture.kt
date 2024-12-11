package com.android.unio.model.event

import com.android.unio.model.firestore.ReferenceElement
import com.android.unio.model.firestore.UniquelyIdentifiable
import com.android.unio.model.user.User

/**
 * Event user picture data class.
 *
 * @param uid Event user picture id.
 * @param image Event user picture image.
 * @param author author of the picture.
 * @param likes number of people who liked the picture.
 */
data class EventUserPicture(
    override val uid: String,
    val image: String,
    val author: ReferenceElement<User>,
    val likes: Int
) : UniquelyIdentifiable {
  companion object
}
