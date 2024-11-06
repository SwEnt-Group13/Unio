package com.android.unio.ui

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.lifecycle.viewmodel.compose.viewModel
import com.android.unio.mocks.association.MockAssociation
import com.android.unio.model.association.AssociationViewModel
import com.android.unio.model.event.Event
import com.android.unio.model.event.EventListViewModel
import com.android.unio.model.event.EventRepositoryFirestore
import com.android.unio.model.image.ImageRepositoryFirebaseStorage
import com.android.unio.model.search.SearchViewModel
import com.android.unio.model.user.UserRepositoryFirestore
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
import dagger.hilt.android.testing.BindValue
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
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
import org.mockito.Mockito.mock

@HiltAndroidTest
class ScreenDisplayingTest {

  @BindValue @MockK lateinit var navigationAction: NavigationAction

  private lateinit var userViewModel: UserViewModel
  @MockK private lateinit var userRepositoryFirestore: UserRepositoryFirestore

  @MockK private lateinit var searchViewModel: SearchViewModel

  private lateinit var associationViewModel: AssociationViewModel

  private lateinit var eventListViewModel: EventListViewModel
  @MockK private lateinit var imageRepositoryFirestore: ImageRepositoryFirebaseStorage

  @MockK private lateinit var firebaseAuth: FirebaseAuth

  // This is the implementation of the abstract method getUid() from FirebaseUser.
  // Because it is impossible to mock abstract method, this is the only way to mock it.
  @MockK private lateinit var mockFirebaseUser: zzac

  @get:Rule val composeTestRule = createComposeRule()

  @get:Rule val hiltRule = HiltAndroidRule(this)

  @Before
  fun setUp() {
    MockKAnnotations.init(this, relaxed = true)

    hiltRule.inject()

    associationViewModel = spyk(AssociationViewModel(mock(), mockk<EventRepositoryFirestore>(), imageRepositoryFirestore))
    eventListViewModel = spyk(EventListViewModel(mock(), mock()))
    userViewModel = spyk(UserViewModel(mock()))

    val associations = MockAssociation.createAllMockAssociations(size = 2)

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

    composeTestRule.setContent { HomeScreen(navigationAction, eventListViewModel, userViewModel) }
    composeTestRule.onNodeWithTag("HomeScreen").assertIsDisplayed()
  }

  @Test
  fun testExploreDisplayed() {
    composeTestRule.setContent { ExploreScreen(navigationAction, associationViewModel, searchViewModel) }
    composeTestRule.onNodeWithTag("exploreScreen").assertIsDisplayed()
    composeTestRule.onNodeWithTag("searchBar").assertIsDisplayed()
  }

  @Test
  fun testMapDisplayed() {
    composeTestRule.setContent { MapScreen(navigationAction, eventListViewModel) }
    composeTestRule.onNodeWithTag("MapScreen").assertIsDisplayed()
  }

  @Test
  fun testEventDisplayed() {
    composeTestRule.setContent { EventScreen() }
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
          navigationAction,"1", associationViewModel, userViewModel)
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
    composeTestRule.setContent { UserProfileScreen(navigationAction, userViewModel) }
    composeTestRule.onNodeWithTag("UserProfileScreen").assertIsDisplayed()
  }

  @Test
  fun testSomeoneElseUserProfileDisplayed() {
    composeTestRule.setContent { SomeoneElseUserProfileScreen() }
    composeTestRule.onNodeWithTag("SomeoneElseUserProfileScreen").assertIsDisplayed()
  }
}
