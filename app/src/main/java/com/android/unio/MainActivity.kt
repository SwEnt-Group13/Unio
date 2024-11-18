package com.android.unio

import android.annotation.SuppressLint
import android.app.Application
import android.content.pm.ActivityInfo
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navigation
import com.android.unio.model.association.AssociationViewModel
import com.android.unio.model.authentication.AuthViewModel
import com.android.unio.model.event.EventViewModel
import com.android.unio.model.image.ImageRepositoryFirebaseStorage
import com.android.unio.model.map.MapViewModel
import com.android.unio.model.search.SearchViewModel
import com.android.unio.model.user.UserViewModel
import com.android.unio.ui.association.AssociationProfileScreen
import com.android.unio.ui.association.EditAssociationScreen
import com.android.unio.ui.authentication.AccountDetails
import com.android.unio.ui.authentication.EmailVerificationScreen
import com.android.unio.ui.authentication.WelcomeScreen
import com.android.unio.ui.event.EventCreationScreen
import com.android.unio.ui.event.EventScreen
import com.android.unio.ui.explore.ExploreScreen
import com.android.unio.ui.home.HomeScreen
import com.android.unio.ui.map.MapScreen
import com.android.unio.ui.navigation.NavigationAction
import com.android.unio.ui.navigation.Route
import com.android.unio.ui.navigation.Screen
import com.android.unio.ui.saved.SavedScreen
import com.android.unio.ui.settings.SettingsScreen
import com.android.unio.ui.theme.AppTheme
import com.android.unio.ui.user.UserClaimAssociationPresidentialRightsScreen
import com.android.unio.ui.user.UserClaimAssociationScreen
import com.android.unio.ui.user.UserProfileScreen
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import com.google.firebase.firestore.firestore
import com.google.firebase.functions.functions
import com.google.firebase.storage.storage
import dagger.hilt.android.AndroidEntryPoint
import dagger.hilt.android.HiltAndroidApp
import javax.inject.Inject
import me.zhanghai.compose.preference.ProvidePreferenceLocals

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

  @Inject lateinit var imageRepository: ImageRepositoryFirebaseStorage

  @SuppressLint("SourceLockedOrientationActivity")
  override fun onCreate(savedInstanceState: Bundle?) {
    requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
    super.onCreate(savedInstanceState)

    useEmulator(false) // Modify this to launch the project with Firebase Emulators

    setContent {
      Surface(modifier = Modifier.fillMaxSize()) {
        ProvidePreferenceLocals { AppTheme { UnioApp(imageRepository) } }
      }
    }
  }
}

private fun useEmulator(useEmulator: Boolean) {
  if (useEmulator) {
    Firebase.functions.useEmulator("10.0.2.2", 5001)
    Firebase.auth.useEmulator("10.0.2.2", 9099)
    Firebase.firestore.useEmulator("10.0.2.2", 8080)
    Firebase.storage.useEmulator("10.0.2.2", 9199)
  }
}

@HiltAndroidApp class UnioApplication : Application()

@Composable
fun UnioApp(imageRepository: ImageRepositoryFirebaseStorage) {
  val navController = rememberNavController()

  val navigationActions = NavigationAction(navController)

  val associationViewModel = hiltViewModel<AssociationViewModel>()
  val userViewModel = hiltViewModel<UserViewModel>()
  val searchViewModel = hiltViewModel<SearchViewModel>()
  val authViewModel = hiltViewModel<AuthViewModel>()
  val eventViewModel = hiltViewModel<EventViewModel>()
  val mapViewModel = hiltViewModel<MapViewModel>()

  // Observe the authentication state
  val authState by authViewModel.authState.collectAsState()
  var previousAuthState by rememberSaveable { mutableStateOf<String?>(null) }

  LaunchedEffect(authState) {
    authState?.let { screen ->
      // Only navigate if the screen has changed
      if (screen != previousAuthState) {
        navigationActions.navigateTo(screen)
        previousAuthState = screen
      }
    }
  }

  NavHost(navController = navController, startDestination = Route.AUTH) {
    navigation(startDestination = Screen.WELCOME, route = Route.AUTH) {
      composable(Screen.WELCOME) { WelcomeScreen(userViewModel) }
      composable(Screen.EMAIL_VERIFICATION) {
        EmailVerificationScreen(navigationActions, userViewModel)
      }
      composable(Screen.ACCOUNT_DETAILS) {
        AccountDetails(navigationActions, userViewModel, imageRepository)
      }
    }
    navigation(startDestination = Screen.HOME, route = Route.HOME) {
      composable(Screen.HOME) {
        HomeScreen(navigationActions, eventViewModel, userViewModel, searchViewModel)
      }
      composable(Screen.EVENT_DETAILS) {
        EventScreen(
            navigationAction = navigationActions,
            eventViewModel = eventViewModel,
            userViewModel = userViewModel)
      }
      composable(Screen.MAP) {
        MapScreen(navigationActions, eventViewModel, userViewModel, mapViewModel)
      }
    }
    navigation(startDestination = Screen.EXPLORE, route = Route.EXPLORE) {
      composable(Screen.EXPLORE) {
        ExploreScreen(navigationActions, associationViewModel, searchViewModel)
      }
      composable(Screen.ASSOCIATION_PROFILE) {
        AssociationProfileScreen(
            navigationActions, associationViewModel, userViewModel, eventViewModel)
      }
      composable(Screen.EDIT_ASSOCIATION) {
        EditAssociationScreen(associationViewModel, navigationActions)
      }
      composable(Screen.EVENT_CREATION) { EventCreationScreen(navigationActions) }
    }
    navigation(startDestination = Screen.SAVED, route = Route.SAVED) {
      composable(Screen.SAVED) { SavedScreen(navigationActions) }
    }
    navigation(startDestination = Screen.MY_PROFILE, route = Route.MY_PROFILE) {
      composable(Screen.MY_PROFILE) {
        UserProfileScreen(userViewModel, navigationActions, searchViewModel)
      }
      composable(Screen.SETTINGS) { SettingsScreen(navigationActions) }
      composable(Screen.CLAIM_ASSOCIATION_RIGHTS) {
        UserClaimAssociationScreen(associationViewModel, navigationActions, searchViewModel)
      }
      composable(Screen.CLAIM_ASSOCIATION_PRESIDENTIAL_RIGHTS) {
        UserClaimAssociationPresidentialRightsScreen(
            associationViewModel, navigationActions, userViewModel)
      }
    }
  }
}
