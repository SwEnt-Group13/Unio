package com.android.unio.components.settings

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import com.android.unio.TearDown
import com.android.unio.mocks.user.MockUser
import com.android.unio.model.authentication.AuthViewModel
import com.android.unio.model.image.ImageRepository
import com.android.unio.model.preferences.AppPreferences
import com.android.unio.model.strings.test_tags.SettingsTestTags
import com.android.unio.model.user.UserRepositoryFirestore
import com.android.unio.model.user.UserViewModel
import com.android.unio.ui.navigation.NavigationAction
import com.android.unio.ui.settings.SettingsScreen
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth
import io.mockk.MockKAnnotations
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.just
import io.mockk.mockkStatic
import io.mockk.runs
import kotlin.reflect.full.memberProperties
import me.zhanghai.compose.preference.ProvidePreferenceLocals
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class SettingsTest : TearDown() {
  @MockK private lateinit var navigationAction: NavigationAction
  @MockK private lateinit var userRepository: UserRepositoryFirestore
  @MockK private lateinit var imageRepository: ImageRepository

  private lateinit var authViewModel: AuthViewModel
  private lateinit var userViewModel: UserViewModel

  @MockK private lateinit var firebaseAuth: FirebaseAuth

  @get:Rule val composeTestRule = createComposeRule()

  @Before
  fun setUp() {
    MockKAnnotations.init(this)

    val user = MockUser.createMockUser()

    mockkStatic(FirebaseAuth::class)
    every { Firebase.auth } returns firebaseAuth
    every { firebaseAuth.addAuthStateListener(any()) } just runs
    every { firebaseAuth.removeAuthStateListener(any()) } just runs
    every { userRepository.updateUser(eq(user), any(), any()) } answers
        {
          val onSuccess = args[1] as () -> Unit
          onSuccess()
        }

    authViewModel = AuthViewModel(firebaseAuth, userRepository)
    userViewModel = UserViewModel(userRepository, imageRepository)

    userViewModel.addUser(user, {})
  }

  @Test
  fun testEverythingIsDisplayed() {
    composeTestRule.setContent {
      ProvidePreferenceLocals { SettingsScreen(navigationAction, authViewModel, userViewModel) }
    }

    composeTestRule.onNodeWithTag(SettingsTestTags.SCREEN).assertIsDisplayed()
    composeTestRule.onNodeWithTag(SettingsTestTags.CONTAINER).assertIsDisplayed()

    // Iterate through the values of AppPreferences and thus check that each setting exists
    AppPreferences::class.memberProperties.forEach { key ->
      composeTestRule.onNodeWithTag(key.call() as String).assertExists()
    }
  }
}
