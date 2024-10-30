package com.android.unio.ui.authentication

import androidx.compose.ui.test.assertHasClickAction
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performTextInput
import com.android.unio.model.user.UserRepositoryFirestore
import com.android.unio.ui.navigation.NavigationAction
import io.mockk.MockKAnnotations
import io.mockk.impl.annotations.MockK
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.kotlin.mock

class WelcomeTest {

  private lateinit var navigationAction: NavigationAction
  @MockK private lateinit var userRepositoryFirestore: UserRepositoryFirestore

  @get:Rule val composeTestRule = createComposeRule()

  @Before
  fun setUp() {
    MockKAnnotations.init(this, relaxed = true)
    navigationAction = mock { NavigationAction::class.java }
    composeTestRule.setContent { WelcomeScreen(navigationAction, userRepositoryFirestore) }
  }

  @Test
  fun testWelcomeIsDisplayed() {
    composeTestRule.onNodeWithTag("WelcomeEmail").assertIsDisplayed()
    composeTestRule.onNodeWithTag("WelcomePassword").assertIsDisplayed()
    composeTestRule.onNodeWithTag("WelcomePassword").assertIsDisplayed()
    composeTestRule.onNodeWithTag("WelcomeButton").assertIsDisplayed()
    composeTestRule.onNodeWithTag("WelcomeButton").assertHasClickAction()
    composeTestRule.onNodeWithTag("WelcomeButton").assertIsNotEnabled()
  }

  @Test
  fun testButtonEnables() {
    composeTestRule.onNodeWithTag("WelcomeButton").assertIsNotEnabled()

    composeTestRule.onNodeWithTag("WelcomeEmail").performTextInput("john.doe@epfl.ch")
    composeTestRule.onNodeWithTag("WelcomePassword").performTextInput("123456")

    composeTestRule.onNodeWithTag("WelcomeButton").assertIsEnabled()
  }
}
