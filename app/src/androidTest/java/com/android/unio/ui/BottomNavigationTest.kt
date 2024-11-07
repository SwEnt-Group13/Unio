package com.android.unio.ui

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import com.android.unio.model.event.EventListViewModel
import com.android.unio.model.event.EventRepository
import com.android.unio.model.image.ImageRepositoryFirebaseStorage
import com.android.unio.model.user.UserRepository
import com.android.unio.model.user.UserRepositoryFirestore
import com.android.unio.model.user.UserViewModel
import com.android.unio.ui.home.HomeScreen
import com.android.unio.ui.navigation.NavigationAction
import io.mockk.MockKAnnotations
import io.mockk.impl.annotations.MockK
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.kotlin.mock

class BottomNavigationTest {

  @MockK private lateinit var navigationAction: NavigationAction

  private lateinit var eventRepository: EventRepository
  private lateinit var eventViewModel: EventListViewModel

  @MockK private lateinit var imageRepository: ImageRepositoryFirebaseStorage

  private lateinit var userRepository: UserRepository
  private lateinit var userViewModel: UserViewModel

  @get:Rule val composeTestRule = createComposeRule()

  @Before
  fun setUp() {
    MockKAnnotations.init(this)
    eventRepository = mock { EventRepository::class.java }
    eventViewModel = EventListViewModel(eventRepository, imageRepository)

    userRepository = mock { UserRepositoryFirestore::class.java }
    userViewModel = UserViewModel(userRepository, false)
  }

  @Test
  fun testBottomNavigationMenuDisplayed() {
    composeTestRule.setContent { HomeScreen(navigationAction, eventViewModel, userViewModel) }
    composeTestRule.onNodeWithTag("bottomNavigationMenu").assertIsDisplayed()
  }
}
