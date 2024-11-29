package com.android.unio.components.map

import android.content.Context
import android.location.Location
import androidx.compose.ui.test.assertHasClickAction
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.navigation.NavHostController
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.rule.GrantPermissionRule
import com.android.unio.TearDown
import com.android.unio.mocks.user.MockUser
import com.android.unio.model.association.AssociationRepositoryFirestore
import com.android.unio.model.event.EventRepositoryFirestore
import com.android.unio.model.event.EventViewModel
import com.android.unio.model.image.ImageRepositoryFirebaseStorage
import com.android.unio.model.map.MapViewModel
import com.android.unio.model.strings.test_tags.MapTestTags
import com.android.unio.model.user.User
import com.android.unio.model.user.UserRepositoryFirestore
import com.android.unio.model.user.UserViewModel
import com.android.unio.ui.map.MapScreen
import com.android.unio.ui.navigation.NavigationAction
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.tasks.OnSuccessListener
import com.google.android.gms.tasks.Task
import io.mockk.MockKAnnotations
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.spyk
import io.mockk.verify
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mockito.mock
import org.mockito.Mockito.`when`
import org.mockito.kotlin.any

@RunWith(AndroidJUnit4::class)
class MapScreenTest : TearDown() {
  @get:Rule val composeTestRule = createComposeRule()

  @get:Rule
  val permissionRule =
      GrantPermissionRule.grant(
          android.Manifest.permission.ACCESS_FINE_LOCATION,
          android.Manifest.permission.ACCESS_COARSE_LOCATION)

  private val user = MockUser.createMockUser()

  @MockK private lateinit var eventRepository: EventRepositoryFirestore
  @MockK private lateinit var imageRepository: ImageRepositoryFirebaseStorage
  @MockK private lateinit var userRepository: UserRepositoryFirestore
  @MockK private lateinit var navHostController: NavHostController
  @MockK private lateinit var associationRepositoryFirestore: AssociationRepositoryFirestore

  private lateinit var locationTask: Task<Location>
  private lateinit var context: Context
  private lateinit var mapViewModel: MapViewModel
  private lateinit var fusedLocationProviderClient: FusedLocationProviderClient
  private val location =
      Location("mockProvider").apply {
        latitude = 46.518831258
        longitude = 6.559331096
      }

  private lateinit var navigationAction: NavigationAction
  private lateinit var eventViewModel: EventViewModel
  private lateinit var userViewModel: UserViewModel

  @Before
  fun setUp() {
    MockKAnnotations.init(this, relaxed = true)

    navigationAction = NavigationAction(navHostController)

    every { eventRepository.init(any()) } answers {}
    eventViewModel =
        EventViewModel(eventRepository, imageRepository, associationRepositoryFirestore)

    every { userRepository.init(any()) } returns Unit
    every { userRepository.getUserWithId("123", any(), any()) } answers
        {
          val onSuccess = it.invocation.args[1] as (User) -> Unit
          onSuccess(user)
        }
    userViewModel = UserViewModel(userRepository, false)
    userViewModel.getUserByUid("123")

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

    composeTestRule.setContent {
      MapScreen(
          navigationAction = navigationAction,
          eventViewModel = eventViewModel,
          userViewModel = userViewModel,
          mapViewModel = mapViewModel)
    }
  }

  @Test
  fun mapScreenComponentsAreDisplayed() {
    composeTestRule.onNodeWithTag(MapTestTags.SCREEN).assertIsDisplayed()
    composeTestRule.onNodeWithTag(MapTestTags.TITLE).assertIsDisplayed()
    composeTestRule.onNodeWithTag(MapTestTags.GO_BACK_BUTTON).assertIsDisplayed()
    composeTestRule.onNodeWithTag(MapTestTags.GO_BACK_BUTTON).assertHasClickAction()
  }

  @Test
  fun mapScreenBackButtonNavigatesBack() {
    composeTestRule.onNodeWithTag(MapTestTags.GO_BACK_BUTTON).assertHasClickAction()
    composeTestRule.onNodeWithTag(MapTestTags.GO_BACK_BUTTON).performClick()
    verify { navigationAction.goBack() }
  }

  @Test
  fun centerOnUserFabCentersMap() {
    composeTestRule.onNodeWithTag(MapTestTags.CENTER_ON_USER_FAB).assertIsDisplayed()
    composeTestRule.onNodeWithTag(MapTestTags.CENTER_ON_USER_FAB).assertHasClickAction()
    composeTestRule.onNodeWithTag(MapTestTags.CENTER_ON_USER_FAB).performClick()

    assert(mapViewModel.userLocation.value != null)
    assert(mapViewModel.userLocation.value!!.latitude == location.latitude)
    assert(mapViewModel.userLocation.value!!.longitude == location.longitude)
  }
}
