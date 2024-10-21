package com.android.unio.ui.user

import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertTextEquals
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onNodeWithTag
import androidx.test.espresso.Espresso
import com.android.unio.model.association.Association
import com.android.unio.model.association.AssociationCategory
import com.android.unio.model.firestore.MockReferenceList
import com.android.unio.model.firestore.firestoreReferenceListWith
import com.android.unio.model.user.Interest
import com.android.unio.model.user.Social
import com.android.unio.model.user.User
import com.android.unio.model.user.UserSocial
import junit.framework.TestCase.assertEquals
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class UserProfileTest {
  @get:Rule val composeTestRule = createComposeRule()

  private val association =
      Association(
          uid = "1234",
          url = "https://www.epfl.ch",
          name = "EPFL",
          fullName = "École Polytechnique Fédérale de Lausanne",
          category = AssociationCategory.EPFL_BODIES,
          description = "EPFL is a research institute and university in Lausanne, Switzerland.",
          members = User.firestoreReferenceListWith(listOf("1234")),
          image = "https://www.epfl.ch/profile.jpg")

  private val user =
      User(
          uid = "1",
          email = "1@gmail.com",
          firstName = "userFirst",
          lastName = "userLast",
          biography = "An example user",
          followedAssociations = MockReferenceList(listOf(association1, association1)),
          joinedAssociations = MockReferenceList(listOf(association1, association1)),
          interests = listOf(Interest.SPORTS, Interest.MUSIC),
          socials =
              listOf(
                  UserSocial(Social.INSTAGRAM, "Instagram"),
                  UserSocial(Social.WEBSITE, "example.com")),
          profilePicture = "https://www.example.com/image",
          hasProvidedAccountDetails = true)

  @Before fun setUp() {}

  @Test
  fun testEverythingIsDisplayed() {
    composeTestRule.setContent { UserProfileScreenContent(user) }

    composeTestRule.onNodeWithTag("UserProfilePicture").assertIsDisplayed()

    composeTestRule.onNodeWithTag("UserProfileName").assertIsDisplayed()
    composeTestRule
        .onNodeWithTag("UserProfileName")
        .assertTextEquals("${user.firstName} ${user.lastName}")

    composeTestRule.onNodeWithTag("UserProfileBiography").assertIsDisplayed()
    composeTestRule.onNodeWithTag("UserProfileBiography").assertTextEquals(user.biography)

    composeTestRule
        .onAllNodesWithTag("UserProfileSocialButton")
        .assertCountEquals(user.socials.size)
    composeTestRule.onAllNodesWithTag("UserProfileInterest").assertCountEquals(user.interests.size)

    composeTestRule.onNodeWithTag("UserProfileJoinedAssociations").assertIsDisplayed()
    composeTestRule.onNodeWithTag("UserProfileFollowedAssociations").assertIsDisplayed()
  }

  @Test
  fun testBottomSheet() {
    var called = false

    composeTestRule.setContent { UserProfileBottomSheet(true) { called = true } }

    composeTestRule.onNodeWithTag("UserProfileBottomSheet").assertIsDisplayed()

    // Press the android back button to close the bottom sheet
    Espresso.pressBack()

    assertEquals(true, called)
  }
}
