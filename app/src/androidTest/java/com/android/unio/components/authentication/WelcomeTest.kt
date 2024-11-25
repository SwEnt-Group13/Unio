package com.android.unio.components.authentication

import androidx.compose.ui.test.assertHasClickAction
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performTextInput
import com.android.unio.TearDown
import com.android.unio.mocks.user.MockUser
import com.android.unio.model.strings.test_tags.WelcomeTestTags
import com.android.unio.model.user.User
import com.android.unio.model.user.UserRepositoryFirestore
import com.android.unio.model.user.UserViewModel
import com.android.unio.ui.authentication.WelcomeScreen
import io.mockk.MockKAnnotations
import io.mockk.every
import io.mockk.impl.annotations.MockK
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class WelcomeTest : TearDown() {

  @get:Rule val composeTestRule = createComposeRule()

  val user = MockUser.createMockUser()

  private lateinit var userViewModel: UserViewModel
  @MockK private lateinit var userRepository: UserRepositoryFirestore

  @Before
  fun setUp() {
    MockKAnnotations.init(this)

    // Call first callback when init is called
    every { userRepository.init(any()) } answers { firstArg<() -> Unit>().invoke() }
    every { userRepository.getUserWithId(any(), any(), any()) } answers
        {
          val onSuccess = args[1] as (User) -> Unit
          onSuccess(user)
        }

    userViewModel = UserViewModel(userRepository, false)
  }

  @Test
  fun testWelcomeIsDisplayed() {
    composeTestRule.setContent { WelcomeScreen(userViewModel) }
    composeTestRule.onNodeWithTag(WelcomeTestTags.EMAIL).assertIsDisplayed()
    composeTestRule.onNodeWithTag(WelcomeTestTags.PASSWORD).assertIsDisplayed()
    composeTestRule.onNodeWithTag(WelcomeTestTags.BUTTON).assertIsDisplayed()
    composeTestRule.onNodeWithTag(WelcomeTestTags.BUTTON).assertHasClickAction()
    composeTestRule.onNodeWithTag(WelcomeTestTags.BUTTON).assertIsNotEnabled()
  }

  @Test
  fun testButtonEnables() {
    composeTestRule.setContent { WelcomeScreen(userViewModel) }
    composeTestRule.onNodeWithTag(WelcomeTestTags.BUTTON).assertIsNotEnabled()

    composeTestRule.onNodeWithTag(WelcomeTestTags.EMAIL).performTextInput("john.doe@epfl.ch")
    composeTestRule.onNodeWithTag(WelcomeTestTags.PASSWORD).performTextInput("123456")

    composeTestRule.onNodeWithTag(WelcomeTestTags.BUTTON).assertIsEnabled()
  }
}