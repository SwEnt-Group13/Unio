package com.android.unio.model.user

import android.net.Uri
import androidx.core.net.toUri
import com.android.unio.R
import com.android.unio.model.association.Association
import com.android.unio.model.association.Role
import com.android.unio.model.event.Event
import com.android.unio.model.firestore.ReferenceList
import com.android.unio.model.firestore.UniquelyIdentifiable

/** @param title: The title is a pointer to the string resource. */
enum class Interest(val title: Int) {
  SPORTS(R.string.interest_sports),
  MUSIC(R.string.interest_music),
  ART(R.string.interest_art),
  TECHNOLOGY(R.string.interest_technology),
  SCIENCE(R.string.interest_science),
  LITERATURE(R.string.interest_literature),
  TRAVEL(R.string.interest_travel),
  FOOD(R.string.interest_food),
  GAMING(R.string.interest_gaming),
  FESTIVALS(R.string.interest_festivals),
  APEROS(R.string.interest_apero),
  NETWORKING(R.string.interest_networking),
  CULTURE(R.string.interest_culture)
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
 * @param savedEvents The events that the user has saved.
 * @param followedAssociations The associations that the user is following.
 * @param joinedAssociations The associations that the user is member of.
 * @param interests The interests of the user.
 * @param socials The socials of the user.
 * @param profilePicture The URL to the profile picture in Firebase storage.
 */
data class User(
    override val uid: String,
    val email: String,
    val firstName: String,
    val lastName: String,
    val biography: String,
    val savedEvents: ReferenceList<Event>,
    val followedAssociations: ReferenceList<Association>,
    val joinedAssociations: ReferenceList<Association>,
    val interests: List<Interest>,
    val socials: List<UserSocial>,
    val profilePicture: String,
) : UniquelyIdentifiable {
  companion object
}

/**
 * @param errorMessage: The error message is a pointer to the string resource. This enables us to
 *   have error messages in different languages.
 */
enum class UserSocialError(val errorMessage: Int) {
  EMPTY_FIELD(R.string.social_overlay_empty_field),
  INVALID_PHONE_NUMBER(R.string.social_overlay_invalid_phone_number),
  INVALID_WEBSITE(R.string.social_overlay_invalid_website),
  NONE(-1)
}

/**
 * @param errorMessage: The error message is a pointer to the string resource, just like for
 *   UserSocialError.
 */
enum class AccountDetailsError(val errorMessage: Int) {
  EMPTY_FIRST_NAME(R.string.account_details_first_name_error),
  EMPTY_LAST_NAME(R.string.account_details_last_name_error)
}

/**
 * EMPTY means that the user hasn't chosen a profile picture REMOTE means that the uri is stored in
 * firebase as a URL and therefore the user has not changed the profile picture LOCAL means that teh
 * uri is a local one and the user has chosen a new profile picture.
 */
enum class ImageUriType {
  EMPTY,
  REMOTE,
  LOCAL,
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

/**
 * Checks if the uri is empty, local or remote.
 *
 * @param uri The uri to check.
 * @return EMPTY if the uri is empty, LOCAL if the uri is local, REMOTE if the uri is remote.
 */
fun checkImageUri(uri: String): ImageUriType {
  if (uri.toUri() == Uri.EMPTY) {
    return ImageUriType.EMPTY
  }
  val localRegex = Regex("^content://.+")

  return if (localRegex.matches(uri)) {
    ImageUriType.LOCAL
  } else {
    ImageUriType.REMOTE
  }
}

/**
 * Checks the social content for errors.
 *
 * @param userSocial The user social to check.
 * @return [UserSocialError.NONE] if no error is found, [UserSocialError.EMPTY_FIELD] if the content
 *   is empty, [UserSocialError.INVALID_PHONE_NUMBER] if the phone number is invalid,
 *   [UserSocialError.INVALID_WEBSITE] if the website is invalid.
 */
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

/**
 * @param social The social to get the placeholder text for.
 * @return The placeholder text for the given social.
 */
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

fun getUserRoleInAssociation(association: Association, userUid: String): Role? {

  val roleOfMember =
      association.roles.find {
        it.uid == association.members.find { it.user.uid == userUid }?.roleUid
      }
  return roleOfMember
}
