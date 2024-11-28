package com.android.unio.end2end

import android.util.Log
import androidx.compose.ui.test.isDisplayed
import androidx.compose.ui.test.junit4.ComposeContentTestRule
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import com.android.unio.MainActivity
import com.android.unio.clearTest
import com.android.unio.model.authentication.currentAuthStateListenerCount
import com.android.unio.model.strings.test_tags.BottomNavBarTestTags
import com.android.unio.model.strings.test_tags.UserProfileTestTags
import com.android.unio.model.strings.test_tags.WelcomeTestTags
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import com.google.firebase.firestore.firestore
import com.google.firebase.functions.functions
import dagger.hilt.android.testing.HiltAndroidRule
import junit.framework.TestCase.assertEquals
import okhttp3.OkHttpClient
import okhttp3.Request
import org.junit.After
import org.junit.Before
import org.junit.Rule

open class EndToEndTest : FirebaseEmulatorFunctions {
  init {
    assertEquals(
        """There are still listeners attached to the Auth instance. Make sure to remove 
           them between tests with Firebase.auth.unregisterAllAuthStateListeners().
        """
            .trimIndent(),
        0,
        Firebase.auth.currentAuthStateListenerCount())
  }

  @get:Rule val hiltRule = HiltAndroidRule(this)
  @get:Rule val composeTestRule = createAndroidComposeRule<MainActivity>()

  @Before
  override fun setUp() {
    /** Verify that the emulators are running */
    verifyEmulatorsAreRunning()

    /** Connect Firebase to the emulators */
    useEmulators()
  }

  @After
  override fun tearDown() {
    clearTest()
  }

  override fun signInWithUser(
      composeTestRule: ComposeContentTestRule,
      email: String,
      password: String
  ) {
    composeTestRule.waitUntil(10000) {
      composeTestRule.onNodeWithTag(WelcomeTestTags.SCREEN).isDisplayed()
    }
    composeTestRule.onNodeWithTag(WelcomeTestTags.EMAIL).performTextInput(email)
    composeTestRule.onNodeWithTag(WelcomeTestTags.PASSWORD).performTextInput(password)
    composeTestRule.onNodeWithTag(WelcomeTestTags.BUTTON).performClick()
  }

  override fun signOutWithUser(composeTestRule: ComposeContentTestRule) {
    composeTestRule.waitUntil(10000) {
      composeTestRule.onNodeWithTag(BottomNavBarTestTags.MY_PROFILE).isDisplayed()
    }
    composeTestRule.onNodeWithTag(BottomNavBarTestTags.MY_PROFILE).performClick()
    composeTestRule.onNodeWithTag(UserProfileTestTags.SETTINGS).performClick()
    composeTestRule.onNodeWithTag(UserProfileTestTags.SIGN_OUT).performClick()
  }

  override fun verifyEmulatorsAreRunning() {
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
                if (response.body == null || !response.body!!.string().contains("Ok")) {
                  throw Exception("Firebase Emulators are not running.")
                }
              }
            })
  }

  override fun useEmulators() {
    try {
      Firebase.firestore.useEmulator(HOST, Firestore.PORT)
      Firebase.auth.useEmulator(HOST, Auth.PORT)
      Firebase.functions.useEmulator(HOST, Functions.PORT)
    } catch (e: IllegalStateException) {
      Log.d("EndToEndTest", "Firebase Emulators are already in use. $e")
    } finally {
      val currentHost = Firebase.firestore.firestoreSettings.host
      if (!currentHost.contains(HOST)) {
        throw Exception("Failed to connect to Firebase Emulators.")
      }
    }
  }

  override fun flushAuthenticatedUsers() {
    val client = OkHttpClient()

    val request = Request.Builder().url(Auth.ACCOUNTS_URL).delete().build()

    client.newCall(request).execute()
  }

  override fun flushFirestoreDatabase() {
    val client = OkHttpClient()

    val request = Request.Builder().url(Firestore.DATABASE_URL).delete().build()

    client.newCall(request).execute()
  }

  companion object {
    const val HOST = "10.0.2.2"
  }

  /* Constant URLs used by the local emulator */
  object Firestore {
    const val PORT = 8080
    const val ROOT = "http://$HOST:$PORT"

    const val DATABASE_URL = "$ROOT/emulator/v1/projects/unio-1b8ee/databases/(default)/documents"
  }

  object Auth {
    const val PORT = 9099
    const val ROOT = "http://$HOST:$PORT"

    const val OOB_URL = "$ROOT/emulator/v1/projects/unio-1b8ee/oobCodes"
    const val ACCOUNTS_URL = "$ROOT/emulator/v1/projects/unio-1b8ee/accounts"
  }

  object Functions {
    const val PORT = 5001
    const val ROOT = "http://$HOST:$PORT"
  }

  object UnverifiedUser {
    const val EMAIL = "example@gmail.com"
    const val PWD = "123456"

    const val FIRST_NAME = "John"
    const val LAST_NAME = "Doe"
    const val BIOGRAPHY = "I am a software engineer"
  }

  // This user's email is already verified
  object JohnDoe {
    const val EMAIL = "example1@gmail.com"
    const val PASSWORD = "helloWorld123"
  }

  // This user's email is already verified
  object AliceMurphy {
    const val EMAIL = "example2@gmail.com"
    const val PASSWORD = "password123"
  }

  object Admin { // to use only if you need specific bypass (otherwise the tests would have no
    // sense)
    const val EMAIL = "admin@admin.com"
    const val PASSWORD = "adminadmin9"
  }
}
