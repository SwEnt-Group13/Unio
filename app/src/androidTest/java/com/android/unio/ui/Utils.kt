package com.android.unio.ui

import androidx.compose.ui.test.junit4.ComposeContentTestRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.test.performTextInput
import com.android.unio.model.strings.test_tags.SocialsOverlayTestTags
import com.android.unio.ui.authentication.UserAccountCreationTest.Companion.FIRESTORE_URL
import com.android.unio.ui.authentication.UserAccountCreationTest.Companion.FLUSH_AUTH_URL
import com.android.unio.ui.authentication.UserAccountCreationTest.Companion.FLUSH_FIRESTORE_URL
import okhttp3.OkHttpClient
import okhttp3.Request

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
 * This tests verifies that your local Firebase emulator is running before running tests that use it
 */
fun verifyEmulatorsAreRunning() {
  try {
    val client = OkHttpClient()
    val request = Request.Builder().url(FIRESTORE_URL).build()

    val response = client.newCall(request).execute()
    val data = response.body?.string()
    assert(data!!.contains("Ok")) { "Your emulators don't seem to be running correctly" }
  } catch (e: Exception) {
    assert(false) { "Start your emulators before running the end to end test" }
  }
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
