package com.android.unio.ui.authentication

import androidx.compose.ui.test.assertHasClickAction
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performTextInput
import com.android.unio.model.strings.test_tags.WelcomeTestTags
import com.android.unio.model.user.UserRepositoryFirestore
import com.android.unio.model.user.UserViewModel
import io.mockk.MockKAnnotations
import io.mockk.clearAllMocks
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.unmockkAll
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class WelcomeTest {

  @get:Rule val composeTestRule = createComposeRule()

  private lateinit var userViewModel: UserViewModel
  @MockK private lateinit var userRepository: UserRepositoryFirestore

  @Before
  fun setUp() {
    MockKAnnotations.init(this, relaxed = true)

    // Call first callback when init is called
    every { userRepository.init(any()) } answers { firstArg<() -> Unit>().invoke() }

    userViewModel = UserViewModel(userRepository)
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

  @After
  fun tearDown() {
    clearAllMocks()
    unmockkAll()
  }
}
