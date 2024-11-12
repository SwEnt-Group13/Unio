package com.android.unio.ui.map

import androidx.compose.ui.test.assertHasClickAction
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.navigation.NavHostController
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.android.unio.mocks.user.MockUser
import com.android.unio.model.event.EventRepositoryFirestore
import com.android.unio.model.event.EventViewModel
import com.android.unio.model.image.ImageRepositoryFirebaseStorage
import com.android.unio.model.strings.test_tags.MapTestTags
import com.android.unio.model.user.User
import com.android.unio.model.user.UserRepositoryFirestore
import com.android.unio.model.user.UserViewModel
import com.android.unio.ui.navigation.NavigationAction
import io.mockk.MockKAnnotations
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.verify
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class MapScreenTest {
  private val user = MockUser.createMockUser()

  @get:Rule val composeTestRule = createComposeRule()

  @MockK private lateinit var eventRepository: EventRepositoryFirestore
  @MockK private lateinit var imageRepository: ImageRepositoryFirebaseStorage
  @MockK private lateinit var userRepository: UserRepositoryFirestore
  @MockK private lateinit var navHostController: NavHostController
  private lateinit var navigationAction: NavigationAction
  private lateinit var eventViewModel: EventViewModel
  private lateinit var userViewModel: UserViewModel

  @Before
  fun setUp() {
    MockKAnnotations.init(this, relaxed = true)

    navigationAction = NavigationAction(navHostController)

    every { eventRepository.init(any()) } answers {}
    eventViewModel = EventViewModel(eventRepository, imageRepository)

    every { userRepository.init(any()) } returns Unit
    every { userRepository.getUserWithId("123", any(), any()) } answers
        {
          val onSuccess = it.invocation.args[1] as (User) -> Unit
          onSuccess(user)
        }
    userViewModel = UserViewModel(userRepository, false)
    userViewModel.getUserByUid("123")
  }

  @Test
  fun mapScreenComponentsAreDisplayed() {
    composeTestRule.setContent {
      MapScreen(
          navigationAction = navigationAction,
          eventViewModel = eventViewModel,
          userViewModel = userViewModel)
    }

    composeTestRule.onNodeWithTag(MapTestTags.SCREEN).assertIsDisplayed()
    composeTestRule.onNodeWithTag(MapTestTags.TITLE).assertIsDisplayed()
    composeTestRule.onNodeWithTag(MapTestTags.GO_BACK_BUTTON).assertIsDisplayed()
    composeTestRule.onNodeWithTag(MapTestTags.GO_BACK_BUTTON).assertHasClickAction()
  }

  @Test
  fun mapScreenBackButtonNavigatesBack() {
    composeTestRule.setContent {
      MapScreen(
          navigationAction = navigationAction,
          eventViewModel = eventViewModel,
          userViewModel = userViewModel)
    }

    composeTestRule.onNodeWithTag(MapTestTags.GO_BACK_BUTTON).assertHasClickAction()
    composeTestRule.onNodeWithTag(MapTestTags.GO_BACK_BUTTON).performClick()
    verify { navigationAction.goBack() }
  }
}
