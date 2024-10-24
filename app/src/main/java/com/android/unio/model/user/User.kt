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

enum class Social(val title: String, val icon: Int, val URL: String, val URLshort : String) {
    FACEBOOK("Facebook",R.drawable.facebook_icon , "https://www.facebook.com/", "facebook.com/"),
    X("X",R.drawable.x_icon ,"https://x.com/", "x.com/"),
  INSTAGRAM("Instagram", R.drawable.instagram_icon, "https://www.instagram.com/", "instagram.com/"),
  SNAPCHAT("Snapchat", R.drawable.snapchat_icon, "https://www.snapchat.com/add/", "snapchat.com/add/"),
  TELEGRAM("Telegram", R.drawable.telegram_icon, "https://t.me/", "t.me/"),
  WHATSAPP("WhatsApp", R.drawable.whatsapp_icon, "https://wa.me/", "wa.me/"),
  WEBSITE("Website", R.drawable.website_icon, "", "")
}

enum class PhoneNumberRegex(val number: Regex){
    SWISS(Regex("41[0-9]{9}")),
    FRENCH(Regex("33[0-9]{9}"))
}

data class UserSocial(val social: Social, val content: String)

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
    val profilePicture: String,
    val hasProvidedAccountDetails: Boolean
) {
  companion object
}

//Helper methods
/**
 * @return 0: no problem is found
 * @return 1: the input is empty or blank
 * @return 2: the phone number has wrong format
 * @return 3: the website is not encoded with https
 */
fun checkSocialURL(userSocial: UserSocial): Int{

    if(userSocial.content.isEmpty() || userSocial.content.isBlank()){
        return 1
    }

    when(userSocial.social){
        Social.WHATSAPP -> {
            PhoneNumberRegex.entries.forEach{ regexNumber ->
                if(!regexNumber.number.matches(userSocial.content)){
                    return 2
                }
                return 0
            }
        }
        Social.WEBSITE -> {
            val regex = Regex("https://")
            if(regex.matchAt(userSocial.content, 0) != null){
                return 3
            }
            return 0
        }
        else -> {
            return 0
        }
    }
    return 0
}