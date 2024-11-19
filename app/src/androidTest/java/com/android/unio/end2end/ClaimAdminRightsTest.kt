package com.android.unio.end2end

import android.util.Log
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.isDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import androidx.test.filters.LargeTest
import com.android.unio.MainActivity
import com.android.unio.model.strings.test_tags.BottomNavBarTestTags
import com.android.unio.model.strings.test_tags.ExploreContentTestTags
import com.android.unio.model.strings.test_tags.HomeTestTags
import com.android.unio.model.strings.test_tags.UserClaimAssociationPresidentialRightsTestTags
import com.android.unio.model.strings.test_tags.UserProfileTestTags
import com.google.firebase.Firebase
import com.google.firebase.firestore.firestore
import dagger.hilt.android.testing.HiltAndroidTest
import org.junit.Rule
import org.junit.Test

@LargeTest
@HiltAndroidTest
class ClaimAdminRightsTest : EndToEndTest() {
  @get:Rule val composeTestRule = createAndroidComposeRule<MainActivity>()

  @Test
  fun testUserClaimRightsAccess() {
    /** Create an account on the welcome screen */
    signInWithUser(composeTestRule, Admin.EMAIL, Admin.PASSWORD)

    // Wait until "HomeScreen" is displayed
    composeTestRule.waitUntil(10000) {
      composeTestRule.onNodeWithTag(HomeTestTags.SCREEN).isDisplayed()
    }

    // Wait until the bottom nav bar is displayed
    composeTestRule.waitUntil(10000) {
      composeTestRule.onNodeWithTag(BottomNavBarTestTags.MY_PROFILE).isDisplayed()
    }

    /** Navigate to the profile screen */
    composeTestRule.onNodeWithTag(BottomNavBarTestTags.MY_PROFILE).performClick()

    composeTestRule.waitUntil(10000) {
      composeTestRule.onNodeWithTag(UserProfileTestTags.SCREEN).isDisplayed()
    }

    /** Navigate to the claiming button screen */
    composeTestRule.onNodeWithTag(UserProfileTestTags.CLAIMING_BUTTON).performClick()

    composeTestRule.onNodeWithTag(ExploreContentTestTags.SEARCH_BAR).assertIsDisplayed()
    composeTestRule
        .onNodeWithTag(ExploreContentTestTags.SEARCH_BAR_INPUT)
        .performTextInput(ASSOCIATION_SEARCH_INPUT)

    // Wait for the server's response to get the association
    composeTestRule.waitUntil(10000) {
      composeTestRule
          .onNodeWithTag(
              ExploreContentTestTags.ASSOCIATION_EXPLORE_RESULT + EXPECTED_ASSOCIATION_NAME)
          .isDisplayed()
    }

    composeTestRule
        .onNodeWithTag(
            ExploreContentTestTags.ASSOCIATION_EXPLORE_RESULT + EXPECTED_ASSOCIATION_NAME)
        .performClick()

    composeTestRule.waitUntil(5000) {
      composeTestRule
          .onNodeWithTag(UserClaimAssociationPresidentialRightsTestTags.SCREEN)
          .isDisplayed()
    }

    composeTestRule
        .onNodeWithTag(UserClaimAssociationPresidentialRightsTestTags.EMAIL_ADDRESS)
        .assertIsDisplayed()
    composeTestRule
        .onNodeWithTag(UserClaimAssociationPresidentialRightsTestTags.EMAIL_ADDRESS)
        .performTextInput(PRESIDENTIAL_EMAIL_ADDRESS)

    composeTestRule
        .onNodeWithTag(UserClaimAssociationPresidentialRightsTestTags.VERIFY_EMAIL_BUTTON)
        .performClick()

    composeTestRule.waitUntil(5000) {
      composeTestRule
          .onNodeWithTag(UserClaimAssociationPresidentialRightsTestTags.CODE)
          .isDisplayed()
    }

    var finalCode = ""

    Firebase.firestore
        .collection("emailVerifications")
        .document(EXPECTED_ASSOCIATION_UID)
        .get()
        .addOnSuccessListener { document ->
          Log.d("ClaimHEYHEY", "hehe")
          if (document != null && document.exists()) {
            val code: String? = document.getString("code")
            Log.d("ClaimHEYHEY", "houhou $code") // Retrieve the "code" field as a String
            if (code != null) {
              finalCode = code
              Log.d("ClaimHEYHEY", "hehddd")
            } else {
              Log.d("ClaimHEYHEY", "error")
              throw IllegalStateException("Code field is missing in the document")
            }
          } else {
            Log.d("ClaimHEYHEY", "error2")
            throw IllegalStateException("Document does not exist")
          }
        }
        .addOnFailureListener { exception ->
          Log.d("ClaimHEYHEY", "error3")
          throw IllegalStateException("Failed to fetch verification code: ${exception.message}")
        }

    Thread.sleep(2000)

    composeTestRule
        .onNodeWithTag(UserClaimAssociationPresidentialRightsTestTags.CODE)
        .performTextInput(finalCode)
    Log.d("ClaimHEYHEY", "houhou $finalCode")

    Thread.sleep(4000)

    composeTestRule
        .onNodeWithTag(UserClaimAssociationPresidentialRightsTestTags.SUBMIT_CODE_BUTTON)
        .performClick()

    composeTestRule.waitUntil(10000) {
      composeTestRule.onNodeWithTag(UserProfileTestTags.SCREEN).isDisplayed()
    }

    signOutWithUser(composeTestRule)
  }

  private companion object {
    const val ASSOCIATION_SEARCH_INPUT = "music"
    const val EXPECTED_ASSOCIATION_NAME = "Musical"
    const val PRESIDENTIAL_EMAIL_ADDRESS = "aurelien.domenget@icloud.com"
    const val EXPECTED_ASSOCIATION_UID = "P0eaFO5qG9y9lK46x8nf"
  }
}
