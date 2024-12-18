package com.android.unio.components

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import com.android.unio.TearDown
import com.android.unio.model.association.AssociationRepositoryFirestore
import com.android.unio.model.event.EventRepository
import com.android.unio.model.event.EventUserPictureRepositoryFirestore
import com.android.unio.model.event.EventViewModel
import com.android.unio.model.image.ImageRepositoryFirebaseStorage
import com.android.unio.model.search.SearchRepository
import com.android.unio.model.search.SearchViewModel
import com.android.unio.model.strings.test_tags.navigation.NavigationActionTestTags
import com.android.unio.model.usecase.SaveUseCaseFirestore
import com.android.unio.model.usecase.UserDeletionUseCaseFirestore
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

class BottomNavigationTest : TearDown() {

  @MockK private lateinit var navigationAction: NavigationAction

  private lateinit var eventRepository: EventRepository
  private lateinit var eventViewModel: EventViewModel

  @MockK private lateinit var imageRepository: ImageRepositoryFirebaseStorage
  @MockK private lateinit var associationRepositoryFirestore: AssociationRepositoryFirestore
  @MockK private lateinit var userDeletionRepository: UserDeletionUseCaseFirestore
  @MockK
  private lateinit var eventUserPictureRepositoryFirestore: EventUserPictureRepositoryFirestore
  @MockK private lateinit var concurrentEventUserRepositoryFirestore: SaveUseCaseFirestore

  private lateinit var userRepository: UserRepository
  private lateinit var userViewModel: UserViewModel

  @get:Rule val composeTestRule = createComposeRule()

  private lateinit var searchViewModel: SearchViewModel
  @MockK(relaxed = true) private lateinit var searchRepository: SearchRepository

  @Before
  fun setUp() {
    MockKAnnotations.init(this)
    eventRepository = mock { EventRepository::class.java }
    eventViewModel =
        EventViewModel(
            eventRepository,
            imageRepository,
            associationRepositoryFirestore,
            eventUserPictureRepositoryFirestore,
            concurrentEventUserRepositoryFirestore)

    userRepository = mock { UserRepositoryFirestore::class.java }
    userViewModel = UserViewModel(userRepository, imageRepository, userDeletionRepository)

    searchViewModel = spyk(SearchViewModel(searchRepository))

    composeTestRule.setContent {
      HomeScreen(navigationAction, eventViewModel, userViewModel, searchViewModel)
    }
  }

  @Test
  fun testBottomNavigationMenuDisplayed() {
    composeTestRule.onNodeWithTag(NavigationActionTestTags.BOTTOM_NAV_MENU).assertIsDisplayed()
  }
}
