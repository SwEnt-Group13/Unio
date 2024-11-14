package com.android.unio.ui

import androidx.compose.ui.test.SemanticsNodeInteraction
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.isNotDisplayed
import androidx.compose.ui.test.junit4.ComposeContentTestRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.test.performTextInput
import com.android.unio.model.strings.test_tags.SocialsOverlayTestTags
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import com.google.firebase.firestore.firestore
import okhttp3.OkHttpClient
import okhttp3.Request

/*
 * Scrolls to a component if it's not displayed and asserts if it is displayed
 */

fun assertDisplayComponentInScroll(compose: SemanticsNodeInteraction) {
  if (compose.isNotDisplayed()) {
    compose.performScrollTo()
  }
  compose.assertIsDisplayed()
}

/*
 * Adds a new user social to the list of user socials
 */
fun addNewUserSocial(composeTestRule: ComposeContentTestRule, username: String, platform: String) {
  composeTestRule.onNodeWithTag(SocialsOverlayTestTags.ADD_BUTTON).performScrollTo().performClick()
  composeTestRule.onNodeWithTag(SocialsOverlayTestTags.PROMPT_TEXT_FIELD).performTextInput(username)
  composeTestRule.onNodeWithTag(SocialsOverlayTestTags.PROMPT_DROP_BOX).performClick()
  composeTestRule
      .onNodeWithTag(SocialsOverlayTestTags.PROMPT_DROP_BOX_ITEM + "$platform")
      .performClick()
  composeTestRule.onNodeWithTag(SocialsOverlayTestTags.PROMPT_SAVE_BUTTON).performClick()
}

/*
 * This object contains all utility methods relating to the firebase emulator suite
 */
object EmulatorUtils {
  /*
   * Links the Firebase objects to the emulator
   */
  fun linkFirebaseToLocalEmulator() {
    Firebase.firestore.useEmulator("10.0.2.2", 8080)
    Firebase.auth.useEmulator("10.0.2.2", 9099)
  }

  /*
   * This tests verifies that your local Firebase emulator is running before running tests that use it
   */
  fun verifyEmulatorsAreRunning() {
    val client = OkHttpClient()
    val request = Request.Builder().url(FIRESTORE_URL).build()

    client
        .newCall(request)
        .enqueue(
            object : okhttp3.Callback {
              override fun onFailure(call: okhttp3.Call, e: java.io.IOException) {
                throw Exception("Firebase Emulators are not running.")
              }

              override fun onResponse(call: okhttp3.Call, response: okhttp3.Response) {
                if (response.body == null) {
                  throw Exception("Firebase Emulators are not running.")
                }
                val data = response.body!!.string()
                assert(data.contains("Ok")) { "Firebase Emulators are not running." }
              }
            })
  }

  /*
   * This method empties all users in the authentication emulator
   */
  fun flushAuthenticationClients() {
    val client = OkHttpClient()

    val request = Request.Builder().url(FLUSH_AUTH_URL).delete().build()

    client.newCall(request).execute()
  }

  /*
   * This method empties all users in the Firestore Database
   */
  fun flushFirestoreDatabase() {
    val client = OkHttpClient()

    val request = Request.Builder().url(FLUSH_FIRESTORE_URL).delete().build()

    client.newCall(request).execute()
  }

  /*
  Constant URLS used by the local emulator
   */
  const val OOB_URL = "http://10.0.2.2:9099/emulator/v1/projects/unio-1b8ee/oobCodes"
  const val FIRESTORE_URL = "http://10.0.2.2:8080"
  const val FLUSH_FIRESTORE_URL =
      "http://10.0.2.2:8080/emulator/v1/projects/unio-1b8ee/databases/(default)/documents"
  const val FLUSH_AUTH_URL = "http://10.0.2.2:9099/emulator/v1/projects/unio-1b8ee/accounts"
}
