package com.android.unio.ui

import android.content.Context
import android.location.Location
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.test.rule.GrantPermissionRule
import com.android.unio.mocks.association.MockAssociation
import com.android.unio.mocks.event.MockEvent
import com.android.unio.mocks.user.MockUser
import com.android.unio.model.association.AssociationViewModel
import com.android.unio.model.event.Event
import com.android.unio.model.event.EventRepositoryFirestore
import com.android.unio.model.event.EventViewModel
import com.android.unio.model.follow.ConcurrentAssociationUserRepositoryFirestore
import com.android.unio.model.image.ImageRepositoryFirebaseStorage
import com.android.unio.model.map.MapViewModel
import com.android.unio.model.search.SearchRepository
import com.android.unio.model.search.SearchViewModel
import com.android.unio.model.strings.test_tags.AccountDetailsTestTags
import com.android.unio.model.strings.test_tags.AssociationProfileTestTags
import com.android.unio.model.strings.test_tags.EmailVerificationTestTags
import com.android.unio.model.strings.test_tags.EventCreationTestTags
import com.android.unio.model.strings.test_tags.EventDetailsTestTags
import com.android.unio.model.strings.test_tags.ExploreContentTestTags
import com.android.unio.model.strings.test_tags.ExploreTestTags
import com.android.unio.model.strings.test_tags.HomeTestTags
import com.android.unio.model.strings.test_tags.MapTestTags
import com.android.unio.model.strings.test_tags.SavedTestTags
import com.android.unio.model.strings.test_tags.SettingsTestTags
import com.android.unio.model.strings.test_tags.SomeoneElseUserProfileTestTags
import com.android.unio.model.strings.test_tags.UserProfileTestTags
import com.android.unio.model.strings.test_tags.WelcomeTestTags
import com.android.unio.model.user.User
import com.android.unio.model.user.UserRepositoryFirestore
import com.android.unio.model.user.UserViewModel
import com.android.unio.ui.association.AssociationProfileScaffold
import com.android.unio.ui.authentication.AccountDetailsScreen
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
import com.android.unio.ui.user.UserProfileScreenScaffold
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.tasks.OnSuccessListener
import com.google.android.gms.tasks.Task
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth
import com.google.firebase.auth.internal.zzac
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import io.mockk.MockKAnnotations
import io.mockk.clearAllMocks
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.spyk
import io.mockk.unmockkAll
import me.zhanghai.compose.preference.ProvidePreferenceLocals
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.Mockito.mock
import org.mockito.Mockito.`when`
import org.mockito.kotlin.any

@HiltAndroidTest
class ScreenDisplayingTest {
  val user = MockUser.createMockUser(uid = "1")
  val events = listOf(MockEvent.createMockEvent())

  @MockK private lateinit var navigationAction: NavigationAction

  @MockK private lateinit var userRepository: UserRepositoryFirestore
  private lateinit var userViewModel: UserViewModel

  private lateinit var associationViewModel: AssociationViewModel

  @MockK private lateinit var eventRepository: EventRepositoryFirestore
  private lateinit var eventViewModel: EventViewModel

  // Mocking the mapViewModel and its dependencies
  private lateinit var locationTask: Task<Location>
  private lateinit var context: Context
  private lateinit var mapViewModel: MapViewModel
  private lateinit var fusedLocationProviderClient: FusedLocationProviderClient
  private val location =
      Location("mockProvider").apply {
        latitude = 46.518831258
        longitude = 6.559331096
      }

  @MockK private lateinit var imageRepositoryFirestore: ImageRepositoryFirebaseStorage

  @MockK private lateinit var firebaseAuth: FirebaseAuth

  // This is the implementation of the abstract method getUid() from FirebaseUser.
  // Because it is impossible to mock abstract method, this is the only way to mock it.
  @MockK private lateinit var mockFirebaseUser: zzac

  @get:Rule val composeTestRule = createComposeRule()

  @get:Rule
  val permissionRule =
      GrantPermissionRule.grant(
          android.Manifest.permission.ACCESS_FINE_LOCATION,
          android.Manifest.permission.ACCESS_COARSE_LOCATION)

  @get:Rule val hiltRule = HiltAndroidRule(this)

  private lateinit var searchViewModel: SearchViewModel
  @MockK(relaxed = true) private lateinit var searchRepository: SearchRepository

  @Before
  fun setUp() {
    MockKAnnotations.init(this, relaxed = true)
    searchViewModel = spyk(SearchViewModel(searchRepository))

    hiltRule.inject()

    associationViewModel =
        spyk(
            AssociationViewModel(
                mock(),
                mockk<EventRepositoryFirestore>(),
                imageRepositoryFirestore,
                mockk<ConcurrentAssociationUserRepositoryFirestore>()))

    every { eventRepository.getEvents(any(), any()) } answers
        {
          val onSuccess = args[0] as (List<Event>) -> Unit
          onSuccess(events)
        }
    eventViewModel = EventViewModel(eventRepository, imageRepositoryFirestore)
    eventViewModel.loadEvents()
    eventViewModel.selectEvent(events.first().uid)

    // Mocking the mapViewModel and its dependencies
    fusedLocationProviderClient = mock()
    locationTask = mock()
    context = mock()
    `when`(fusedLocationProviderClient.lastLocation).thenReturn(locationTask)
    `when`(locationTask.addOnSuccessListener(any())).thenAnswer {
      (it.arguments[0] as OnSuccessListener<Location?>).onSuccess(location)
      locationTask
    }
    mapViewModel =
        spyk(MapViewModel(fusedLocationProviderClient)) {
          every { hasLocationPermissions(any()) } returns true
        }
    mapViewModel = MapViewModel(fusedLocationProviderClient)
    mapViewModel.fetchUserLocation(context)

    every { userRepository.getUserWithId(any(), any(), any()) } answers
        {
          val onSuccess = args[1] as (User) -> Unit
          onSuccess(user)
        }
    userViewModel = UserViewModel(userRepository)
    userViewModel.getUserByUid("1", false)

    searchViewModel = spyk(SearchViewModel(searchRepository))
    val associations = MockAssociation.createAllMockAssociations(size = 2)

    every { associationViewModel.findAssociationById(any()) } returns associations.first()
    every { associationViewModel.getEventsForAssociation(any(), any()) } answers
        {
          val onSuccess = args[1] as (List<Event>) -> Unit
          onSuccess(emptyList())
        }

    // Mocking the Firebase.auth object and its behaviour
    mockkStatic(FirebaseAuth::class)
    every { Firebase.auth } returns firebaseAuth
    every { firebaseAuth.currentUser } returns mockFirebaseUser
    associationViewModel.selectAssociation(associations.first().uid)
  }

  @Test
  fun testWelcomeDisplayed() {
    composeTestRule.setContent { WelcomeScreen(userViewModel) }
    composeTestRule.onNodeWithTag(WelcomeTestTags.SCREEN).assertIsDisplayed()
  }

  @Test
  fun testEmailVerificationDisplayed() {
    composeTestRule.setContent { EmailVerificationScreen(navigationAction, userViewModel) }
    composeTestRule.onNodeWithTag(EmailVerificationTestTags.SCREEN).assertIsDisplayed()
  }

  @Test
  fun testAccountDetailsDisplayed() {
    composeTestRule.setContent {
      AccountDetailsScreen(navigationAction, userViewModel, imageRepositoryFirestore)
    }
    composeTestRule.onNodeWithTag(AccountDetailsTestTags.ACCOUNT_DETAILS).assertIsDisplayed()
  }

  @Test
  fun testHomeDisplayed() {
    composeTestRule.setContent {
      HomeScreen(navigationAction, eventViewModel, userViewModel, searchViewModel)
    }
    composeTestRule.onNodeWithTag(HomeTestTags.SCREEN).assertIsDisplayed()
    composeTestRule.onNodeWithTag(HomeTestTags.SEARCH_BAR).assertIsDisplayed()
    composeTestRule.onNodeWithTag(HomeTestTags.SEARCH_BAR_INPUT).assertIsDisplayed()
  }

  @Test
  fun testExploreDisplayed() {
    composeTestRule.setContent {
      ExploreScreen(navigationAction, associationViewModel, searchViewModel)
    }
    composeTestRule.onNodeWithTag(ExploreTestTags.EXPLORE_SCAFFOLD_TITLE).assertIsDisplayed()
    composeTestRule.onNodeWithTag(ExploreContentTestTags.SEARCH_BAR).assertIsDisplayed()
  }

  @Test
  fun testMapDisplayed() {
    composeTestRule.setContent {
      MapScreen(navigationAction, eventViewModel, userViewModel, mapViewModel)
    }
    composeTestRule.onNodeWithTag(MapTestTags.SCREEN).assertIsDisplayed()
  }

  @Test
  fun testEventDisplayed() {
    composeTestRule.setContent {
      EventScreen(
          navigationAction = navigationAction,
          eventViewModel = eventViewModel,
          userViewModel = userViewModel,
          mapViewModel = mapViewModel)
    }
    composeTestRule.onNodeWithTag(EventDetailsTestTags.SCREEN).assertIsDisplayed()
  }

  @Test
  fun testEventCreationDisplayed() {
    composeTestRule.setContent {
      EventCreationScreen(navigationAction, searchViewModel, associationViewModel)
    }
    composeTestRule.onNodeWithTag(EventCreationTestTags.SCREEN).assertIsDisplayed()
  }

  @Test
  fun testAssociationProfileDisplayed() {
    composeTestRule.setContent {
      AssociationProfileScaffold(
          navigationAction, userViewModel, eventViewModel, associationViewModel) {}
    }
    composeTestRule.onNodeWithTag(AssociationProfileTestTags.SCREEN).assertIsDisplayed()
  }

  @Test
  fun testSavedDisplayed() {
    composeTestRule.setContent { SavedScreen(navigationAction, eventViewModel, userViewModel) }
    composeTestRule.onNodeWithTag(SavedTestTags.SCREEN).assertIsDisplayed()
  }

  @Test
  fun testSettingsDisplayed() {
    composeTestRule.setContent { ProvidePreferenceLocals { SettingsScreen(navigationAction) } }
    composeTestRule.onNodeWithTag(SettingsTestTags.SCREEN).assertIsDisplayed()
  }

  @Test
  fun testUserProfileDisplayed() {
    composeTestRule.setContent {
      UserProfileScreenScaffold(MockUser.createMockUser(), navigationAction, false, {}, {})
    }
    composeTestRule.onNodeWithTag(UserProfileTestTags.SCREEN).assertIsDisplayed()
  }

  @Test
  fun testSomeoneElseUserProfileDisplayed() {
    composeTestRule.setContent {
      userViewModel.setSomeoneElseUser(user)
      SomeoneElseUserProfileScreen(navigationAction, userViewModel, associationViewModel)
    }
    composeTestRule.onNodeWithTag(SomeoneElseUserProfileTestTags.SCREEN).assertIsDisplayed()
  }

  @After
  fun tearDown() {
    clearAllMocks()
    unmockkAll()
  }
}
