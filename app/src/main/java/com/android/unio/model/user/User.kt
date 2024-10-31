package com.android.unio.model.user

import com.android.unio.R
import com.android.unio.model.association.Association
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

enum class Social(val title: String, val icon: Int, val url: String) {
  FACEBOOK("Facebook", R.drawable.facebook_icon, "facebook.com/"),
  X("X", R.drawable.x_icon, "x.com/"),
  INSTAGRAM("Instagram", R.drawable.instagram_icon, "instagram.com/"),
  SNAPCHAT("Snapchat", R.drawable.snapchat_icon, "snapchat.com/add/"),
  TELEGRAM("Telegram", R.drawable.telegram_icon, "t.me/"),
  WHATSAPP("WhatsApp", R.drawable.whatsapp_icon, "wa.me/"),
  WEBSITE("Website", R.drawable.website_icon, "")
}

data class UserSocial(val social: Social, val content: String) {
  fun getFullUrl(): String {
    return "https://" + social.url + content
  }
}

/**
 * @param uid The unique identifier of the user.
 * @param email The email of the user.
 * @param firstName The first name of the user.
 * @param lastName The last name of the user.
 * @param biography The biography of the user.
 * @param followedAssociations The associations that the user is following.
 * @param joinedAssociations The associations that the user is member of.
 * @param interests The interests of the user.
 * @param socials The socials of the user.
 * @param profilePicture The URL to the profile picture in Firebase storage.
 * @param hasProvidedAccountDetails Whether the user has provided account details.
 */
data class User(
    val uid: String,
    val email: String,
    val firstName: String,
    val lastName: String,
    val biography: String,
    val followedAssociations: ReferenceList<Association>,
    val joinedAssociations: ReferenceList<Association>,
    val interests: List<Interest>,
    val socials: List<UserSocial>,
    val profilePicture: String
) {
  companion object
}

enum class UserSocialError(val errorMessage: String) {
  EMPTY_FIELD("The input is empty or blank"),
  INVALID_PHONE_NUMBER("The phone number has wrong format"),
  INVALID_WEBSITE("The website is not encoded with https"),
  NONE("")
}

enum class AccountDetailsError(val errorMessage: String) {
  EMPTY_FIRST_NAME("Please fill in your first name"),
  EMPTY_LAST_NAME("Please fill in your last name"),
  NONE("")
}
// Helper methods
/**
 * @return NONE: no problem is found
 * @return EMPTY_FIELD: the website is not encoded with https
 * @return INVALID_PHONE_NUMBER: the input is empty or blank
 * @return INVALID_WEBSITE: the phone number has wrong format
 */
fun checkNewUser(user: User): MutableSet<AccountDetailsError> {
  val errors = mutableSetOf<AccountDetailsError>()

  if (user.firstName.isEmpty() || user.firstName.isBlank()) {
    errors.add(AccountDetailsError.EMPTY_FIRST_NAME)
  }
  if (user.lastName.isEmpty() || user.lastName.isBlank()) {
    errors.add(AccountDetailsError.EMPTY_LAST_NAME)
  }
  return errors
}

fun checkSocialContent(userSocial: UserSocial): UserSocialError {

  if (userSocial.content.isEmpty() || userSocial.content.isBlank()) {
    return UserSocialError.EMPTY_FIELD
  }

  when (userSocial.social) {
    Social.WHATSAPP -> {
      val numberRegex = Regex("^[0-9]{10,12}$")
      if (numberRegex.matches(userSocial.content)) {
        return UserSocialError.NONE
      }
      return UserSocialError.INVALID_PHONE_NUMBER
    }
    Social.WEBSITE -> {
      val regex = Regex("^https://.*$")
      if (regex.matches(userSocial.content)) {
        return UserSocialError.NONE
      }
      return UserSocialError.INVALID_WEBSITE
    }
    else -> {
      return UserSocialError.NONE
    }
  }
}

fun getPlaceHolderText(social: Social): String {
  return when (social) {
    Social.FACEBOOK,
    Social.X,
    Social.INSTAGRAM,
    Social.SNAPCHAT,
    Social.TELEGRAM -> "username"
    Social.WHATSAPP -> "41XXXXXXXXX"
    Social.WEBSITE -> "https://www.mywebsite.com"
  }
}
