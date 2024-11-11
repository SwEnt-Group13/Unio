package com.android.unio.ui.user

import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsNotDisplayed
import androidx.compose.ui.test.assertTextEquals
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onNodeWithTag
import com.android.unio.mocks.user.MockUser
import com.android.unio.model.strings.test_tags.UserProfileTestTags
import com.android.unio.model.user.UserRepositoryFirestore
import com.android.unio.model.user.UserViewModel
import com.android.unio.ui.navigation.NavigationAction
import io.mockk.MockKAnnotations
import io.mockk.impl.annotations.MockK
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class UserProfileTest {

  @MockK private lateinit var navigationAction: NavigationAction
  @MockK private lateinit var userRepository: UserRepositoryFirestore
  private lateinit var userViewModel: UserViewModel

  @get:Rule val composeTestRule = createComposeRule()

  private val user = MockUser.createMockUser()

  @Before
  fun setUp() {
    MockKAnnotations.init(this)

    userViewModel = UserViewModel(userRepository)
  }

  @Test
  fun testEverythingIsDisplayed() {
    composeTestRule.setContent { UserProfileScreenScaffold(user, navigationAction, false) {} }

    composeTestRule.onNodeWithTag(UserProfileTestTags.PROFILE_PICTURE).assertExists()

    composeTestRule.onNodeWithTag(UserProfileTestTags.NAME).assertExists()
    composeTestRule
        .onNodeWithTag(UserProfileTestTags.NAME)
        .assertTextEquals("${user.firstName} ${user.lastName}")

    composeTestRule.onNodeWithTag(UserProfileTestTags.BIOGRAPHY).assertExists()
    composeTestRule.onNodeWithTag(UserProfileTestTags.BIOGRAPHY).assertTextEquals(user.biography)

    composeTestRule
        .onAllNodesWithTag(UserProfileTestTags.SOCIAL_BUTTON)
        .assertCountEquals(user.socials.size)
    composeTestRule
        .onAllNodesWithTag(UserProfileTestTags.INTEREST)
        .assertCountEquals(user.interests.size)
  }

  @Test
  fun testBottomSheet() {

    composeTestRule.setContent { UserProfileBottomSheet(true, navigationAction) {} }

    composeTestRule.onNodeWithTag(UserProfileTestTags.BOTTOM_SHEET).assertIsDisplayed()
  }

  @Test
  fun testNoUser() {
    composeTestRule.setContent { UserProfileScreen(userViewModel, navigationAction) }

    composeTestRule.onNodeWithTag(UserProfileTestTags.SCREEN).assertIsNotDisplayed()
  }
}
