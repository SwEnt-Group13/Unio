package com.android.unio.model.user

import com.android.unio.model.association.Association
import com.android.unio.model.event.Event
import com.android.unio.model.firestore.ReferenceList

enum class Interest(val title: String) {
  SPORTS("Sports"),
  MUSIC("Music"),
  ART("Art"),
  TECHNOLOGY("Technology"),
  SCIENCE("Science"),
  LITERATURE("Literature"),
  TRAVEL("Travel"),
  FOOD("Food"),
  GAMING("Gaming"),
  FESTIVALS("Festivals")
}

enum class Social(val title: String) {
  INSTAGRAM("Instagram"),
  SNAPCHAT("Snapchat"),
  TELEGRAM("Telegram"),
  WHATSAPP("WhatsApp"),
  DISCORD("Discord"),
  LINKEDIN("LinkedIn"),
  WEBSITE("Website"),
  OTHER("Other")
}

data class UserSocial(val social: Social, val content: String)

/**
 * @param uid The unique identifier of the user.
 * @param email The email of the user.
 * @param firstName The first name of the user.
 * @param lastName The last name of the user.
 * @param biography The biography of the user.
 * @param followingAssociations The associations that the user is following.
 * @param interests The interests of the user.
 * @param socials The socials of the user.
 * @param profilePicture The URL to the profile picture in Firebase storage.
 */
data class User(
    val uid: String,
    val email: String,
    val firstName: String,
    val lastName: String,
    val biography: String,
    val followingAssociations: ReferenceList<Association>,
    val savedEvents: ReferenceList<Event>,
    val interests: List<Interest>,
    val socials: List<UserSocial>,
    val profilePicture: String
) {
  companion object
}
