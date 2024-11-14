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
   * Connects Firebase to the local emulators
   */
  fun useEmulators() {
    Firebase.firestore.useEmulator("10.0.2.2", 8080)
    Firebase.auth.useEmulator("10.0.2.2", 9099)
  }

  /*
   * Verify that the local Firebase emulator is running.
   *
   * @throws Exception if the emulator is not running
   */
  fun verifyEmulatorsAreRunning() {
    val client = OkHttpClient()
    val request = Request.Builder().url(Firestore.ROOT).build()

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
   * Delete all users in the Firebase Authentication emulator
   */
  fun flushAuthenticationUsers() {
    val client = OkHttpClient()

    val request = Request.Builder().url(Auth.ACCOUNTS_URL).delete().build()

    client.newCall(request).execute()
  }

  /*
   * Delete all documents in the Firestore emulator
   */
  fun flushFirestoreDatabase() {
    val client = OkHttpClient()

    val request = Request.Builder().url(Firestore.DATABASE_URL).delete().build()

    client.newCall(request).execute()
  }

  /* Constant URLs used by the local emulator */
  object Firestore {
    const val HOST = "10.0.2.2"
    const val PORT = 8080
    const val ROOT = "http://$HOST:$PORT"
    const val DATABASE_URL = "$ROOT/emulator/v1/projects/unio-1b8ee/databases/(default)/documents"
  }
  object Auth {
    const val HOST = "10.0.2.2"
    const val PORT = 9099
    const val ROOT = "http://$HOST:$PORT"
    const val OOB_URL = "$ROOT/emulator/v1/projects/unio-1b8ee/oobCodes"
    const val ACCOUNTS_URL = "$ROOT/emulator/v1/projects/unio-1b8ee/accounts"
  }
}
