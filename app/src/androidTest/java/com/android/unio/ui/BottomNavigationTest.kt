package com.android.unio.ui

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.navigation.NavHostController
import com.android.unio.model.event.EventRepository
import com.android.unio.model.event.EventViewModel
import com.android.unio.model.search.SearchRepository
import com.android.unio.model.search.SearchViewModel
import com.android.unio.model.user.UserRepository
import com.android.unio.model.user.UserRepositoryFirestore
import com.android.unio.model.user.UserViewModel
import com.android.unio.ui.home.HomeScreen
import com.android.unio.ui.navigation.NavigationAction
import io.mockk.MockKAnnotations
import io.mockk.impl.annotations.MockK
import io.mockk.spyk
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.kotlin.mock

class BottomNavigationTest {

  private lateinit var navHostController: NavHostController
  private lateinit var navigationAction: NavigationAction

  private lateinit var eventRepository: EventRepository
  private lateinit var eventViewModel: EventViewModel

  private lateinit var userRepository: UserRepository
  private lateinit var userViewModel: UserViewModel

  @get:Rule val composeTestRule = createComposeRule()

  private lateinit var searchViewModel: SearchViewModel
  @MockK(relaxed = true) private lateinit var searchRepository: SearchRepository

  @Before
  fun setUp() {
    MockKAnnotations.init(this)
    eventRepository = mock { EventRepository::class.java }
    eventViewModel = EventViewModel(eventRepository)

    userRepository = mock { UserRepositoryFirestore::class.java }
    userViewModel = UserViewModel(userRepository, false)

    navHostController = mock { NavHostController::class.java }
    navigationAction = NavigationAction(navHostController)

    searchViewModel = spyk(SearchViewModel(searchRepository))

    composeTestRule.setContent {
      HomeScreen(navigationAction, eventViewModel, userViewModel, searchViewModel)
    }
  }

  @Test
  fun testBottomNavigationMenuDisplayed() {
    composeTestRule.onNodeWithTag("bottomNavigationMenu").assertIsDisplayed()
  }
}
