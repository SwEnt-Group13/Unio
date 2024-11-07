package com.android.unio.e2e

import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import androidx.test.core.app.ApplicationProvider
import androidx.test.filters.LargeTest
import com.android.unio.MainActivity
import com.google.firebase.Firebase
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.auth
import com.google.firebase.firestore.firestore
import org.junit.Before
import org.junit.Rule
import org.junit.Test

@LargeTest
class EndToEndTest {
  @get:Rule val composeTestRule = createAndroidComposeRule<MainActivity>()

  @Before
  fun setUp() {
    Firebase.firestore.useEmulator("10.0.2.2", 8080)
    Firebase.auth.useEmulator("10.0.2.2", 9099)

    if (FirebaseApp.getApps(ApplicationProvider.getApplicationContext()).isEmpty()) {
      FirebaseApp.initializeApp(ApplicationProvider.getApplicationContext())
    }
  }

  @Test
  fun test() {
    composeTestRule.onNodeWithTag("WelcomeEmail").performTextInput(EMAIL)
    composeTestRule.onNodeWithTag("WelcomePassword").performTextInput(PWD)

    composeTestRule.onNodeWithTag("WelcomeButton").performClick()
  }

  companion object {
    const val EMAIL = "alexeithornber@gmail.com"
    const val PWD = "123456"
  }
}