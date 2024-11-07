package com.android.unio.ui

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import com.android.unio.mocks.association.MockAssociation
import com.android.unio.model.association.AssociationViewModel
import com.android.unio.model.event.Event
import com.android.unio.model.event.EventRepositoryFirestore
import com.android.unio.model.event.EventViewModel
import com.android.unio.model.image.ImageRepositoryFirebaseStorage
import com.android.unio.model.search.SearchRepository
import com.android.unio.model.search.SearchViewModel
import com.android.unio.model.user.UserViewModel
import com.android.unio.ui.association.AssociationProfileScreen
import com.android.unio.ui.authentication.AccountDetails
import com.android.unio.ui.authentication.EmailVerificationScreen
import com.android.unio.ui.authentication.WelcomeScreen
import com.android.unio.ui.event.EventCreationScreen
import com.android.unio.ui.event.EventScreen
import com.android.unio.ui.explore.ExploreScreen
import com.android.unio.ui.home.HomeScreen
import com.android.unio.ui.map.MapScreen
import com.android.unio.ui.navigation.NavigationAction
import com.android.unio.ui.saved.SavedScreen
import com.android.unio.ui.settings.SettingsScreen
import com.android.unio.ui.user.SomeoneElseUserProfileScreen
import com.android.unio.ui.user.UserProfileScreen
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth
import com.google.firebase.auth.internal.zzac
import io.mockk.MockKAnnotations
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.spyk
import me.zhanghai.compose.preference.ProvidePreferenceLocals
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.kotlin.mock

class ScreenDisplayingTest {

  private lateinit var navigationAction: NavigationAction
  private lateinit var userViewModel: UserViewModel

  @MockK private lateinit var searchViewModel: SearchViewModel
  @MockK private lateinit var eventRepository: EventRepositoryFirestore
  private lateinit var eventViewModel: EventViewModel
  @MockK private lateinit var associationViewModel: AssociationViewModel
  @MockK private lateinit var imageRepositoryFirestore: ImageRepositoryFirebaseStorage

  @MockK private lateinit var firebaseAuth: FirebaseAuth

  // This is the implementation of the abstract method getUid() from FirebaseUser.
  // Because it is impossible to mock abstract method, this is the only way to mock it.
  @MockK private lateinit var mockFirebaseUser: zzac

  @get:Rule val composeTestRule = createComposeRule()

  @Before
  fun setUp() {
    MockKAnnotations.init(this, relaxed = true)

    navigationAction = mock { NavHostController::class.java }
    userViewModel = mockk()
    associationViewModel = mockk()
    eventRepository = mockk()

    every { eventRepository.init(any()) } answers {}
    eventViewModel = EventViewModel(eventRepository)

    every { eventRepository.getEvents(any(), any()) } answers
        {
          val onSuccess = args[1] as (List<Event>) -> Unit
          onSuccess(emptyList())
        }

    val associations = MockAssociation.createAllMockAssociations(size = 2)
    searchViewModel = spyk(SearchViewModel(searchRepository))

    every { associationViewModel.findAssociationById(any()) } returns associations.first()
    every { associationViewModel.getEventsForAssociation(any(), any()) } answers
        {
          val onSuccess = args[1] as (List<Event>) -> Unit
          onSuccess(emptyList())
        }

    // Mocking the Firebase.auth object and it's behaviour
    mockkStatic(FirebaseAuth::class)
    every { Firebase.auth } returns firebaseAuth
    every { firebaseAuth.currentUser } returns mockFirebaseUser
  }

  @Test
  fun testWelcomeDisplayed() {
    composeTestRule.setContent { WelcomeScreen() }
    composeTestRule.onNodeWithTag("WelcomeScreen").assertIsDisplayed()
  }

  @Test
  fun testEmailVerificationDisplayed() {
    composeTestRule.setContent { EmailVerificationScreen(navigationAction) }
    composeTestRule.onNodeWithTag("EmailVerificationScreen").assertIsDisplayed()
  }

  @Test
  fun testAccountDetailsDisplayed() {
    composeTestRule.setContent {
      AccountDetails(navigationAction, userViewModel, imageRepositoryFirestore)
    }
    composeTestRule.onNodeWithTag("AccountDetails").assertIsDisplayed()
  }

  @Test
  fun testHomeDisplayed() {
    composeTestRule.setContent { HomeScreen(navigationAction, eventViewModel) }
    composeTestRule.onNodeWithTag("HomeScreen").assertIsDisplayed()
  }

  @Test
  fun testExploreDisplayed() {
    composeTestRule.setContent {
      ExploreScreen(navigationAction, searchViewModel = searchViewModel)
    }
    composeTestRule.onNodeWithTag("exploreScreen").assertIsDisplayed()
    composeTestRule.onNodeWithTag("searchBar").assertIsDisplayed()
  }

  @Test
  fun testMapDisplayed() {
    composeTestRule.setContent { MapScreen(navigationAction, eventViewModel) }
    composeTestRule.onNodeWithTag("MapScreen").assertIsDisplayed()
  }

  @Test
  fun testEventDisplayed() {
    composeTestRule.setContent {
      EventScreen(
          navigationAction = navigationAction,
          eventViewModel = eventViewModel,
          userViewModel = userViewModel)
    }
    composeTestRule.onNodeWithTag("EventScreen").assertIsDisplayed()
  }

  @Test
  fun testEventCreationDisplayed() {
    composeTestRule.setContent { EventCreationScreen() }
    composeTestRule.onNodeWithTag("EventCreationScreen").assertIsDisplayed()
  }

  @Test
  fun testAssociationProfileDisplayed() {
    composeTestRule.setContent {
      AssociationProfileScreen(
          navigationAction,
          "1",
          associationViewModel,
          userViewModel = viewModel(factory = UserViewModel.Factory))
    }
    composeTestRule.onNodeWithTag("AssociationScreen").assertIsDisplayed()
  }

  @Test
  fun testSavedDisplayed() {
    composeTestRule.setContent { SavedScreen(navigationAction) }
    composeTestRule.onNodeWithTag("SavedScreen").assertIsDisplayed()
  }

  @Test
  fun testSettingsDisplayed() {
    composeTestRule.setContent { ProvidePreferenceLocals { SettingsScreen(navigationAction) } }
    composeTestRule.onNodeWithTag("SettingsScreen").assertIsDisplayed()
  }

  @Test
  fun testUserProfileDisplayed() {
    composeTestRule.setContent { UserProfileScreen(navigationAction) }
    composeTestRule.onNodeWithTag("UserProfileScreen").assertIsDisplayed()
  }

  @Test
  fun testSomeoneElseUserProfileDisplayed() {
    composeTestRule.setContent { SomeoneElseUserProfileScreen() }
    composeTestRule.onNodeWithTag("SomeoneElseUserProfileScreen").assertIsDisplayed()
  }
}
