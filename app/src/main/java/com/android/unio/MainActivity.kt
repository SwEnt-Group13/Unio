package com.android.unio

import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navigation
import com.android.unio.model.association.AssociationRepositoryFirestore
import com.android.unio.model.association.AssociationViewModel
import com.android.unio.model.event.EventListViewModel
import com.android.unio.model.event.EventRepositoryFirestore
import com.android.unio.model.search.SearchRepository
import com.android.unio.model.search.SearchViewModel
import com.android.unio.model.user.UserRepositoryFirestore
import com.android.unio.model.user.UserViewModel
import com.android.unio.ui.accountCreation.AccountDetails
import com.android.unio.ui.association.AssociationProfileScreen
import com.android.unio.ui.authentication.EmailVerificationScreen
import com.android.unio.ui.authentication.WelcomeScreen
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
import com.android.unio.ui.user.UserProfileScreen
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import com.google.firebase.firestore.firestore
import me.zhanghai.compose.preference.ProvidePreferenceLocals

class MainActivity : ComponentActivity() {
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContent {
      Surface(modifier = Modifier.fillMaxSize()) {
        ProvidePreferenceLocals { AppTheme { UnioApp() } }
      }
    }
  }
}

@Composable
fun UnioApp() {
  val navController = rememberNavController()
  val navigationActions = NavigationAction(navController)
  val db = Firebase.firestore
  val context = LocalContext.current

  val associationRepository = remember { AssociationRepositoryFirestore(db) }
  val eventRepository = remember { EventRepositoryFirestore(db) }
  val userRepositoryFirestore = remember { UserRepositoryFirestore(db) }
  val searchRepository = remember {
    SearchRepository(context, associationRepository, eventRepository)
  }

  val associationViewModel = remember {
    AssociationViewModel(associationRepository, eventRepository)
  }
  val eventListViewModel = remember { EventListViewModel(eventRepository) }
  val userViewModel = remember { UserViewModel(userRepositoryFirestore, true) }
  val searchViewModel = remember { SearchViewModel(searchRepository) }

  // Redirect user based on authentication state
  Firebase.auth.addAuthStateListener { auth ->
    val user = auth.currentUser
    if (user != null) {
      if (user.isEmailVerified) {
        userRepositoryFirestore.getUserWithId(
            user.uid,
            {
              if (it.firstName.isNotEmpty()) {
                navigationActions.navigateTo(Screen.HOME)
              } else {
                navigationActions.navigateTo(Screen.ACCOUNT_DETAILS)
              }
            },
            {
              Log.e("UnioApp", "Error fetching account details: $it")
              Toast.makeText(context, "Error fetching account details.", Toast.LENGTH_SHORT).show()
            })
      } else {
        navigationActions.navigateTo(Screen.EMAIL_VERIFICATION)
      }
    } else {
      navigationActions.navigateTo(Route.AUTH)
    }
  }

  NavHost(navController = navController, startDestination = Route.AUTH) {
    navigation(startDestination = Screen.WELCOME, route = Route.AUTH) {
      composable(Screen.WELCOME) { WelcomeScreen(navigationActions) }
      composable(Screen.EMAIL_VERIFICATION) { EmailVerificationScreen(navigationActions) }
      composable(Screen.ACCOUNT_DETAILS) { AccountDetails(navigationActions, userViewModel) }
    }
    navigation(startDestination = Screen.HOME, route = Route.HOME) {
      composable(Screen.HOME) { HomeScreen(navigationActions, eventListViewModel, onAddEvent = {}) }
      composable(Screen.EVENT_DETAILS) { navBackStackEntry ->
        // Get the event UID from the arguments
        val uid = navBackStackEntry.arguments?.getString("uid")
        // Create the Event screen with the event UID
        uid?.let {
          EventScreen(
              navigationAction = navigationActions,
              eventId = it,
              eventListViewModel,
              userViewModel = userViewModel)
        }
        /*uid?.let { EventScreen(navigationActions, it, userViewModel = userViewModel) }
        ?: run {
          Log.e("EventDetails", "Event UID is null")
          Toast.makeText(context, "Event UID is null", Toast.LENGTH_SHORT).show()
        }*/
      }
      composable(Screen.MAP) { MapScreen(navigationActions, eventListViewModel) }
    }
    navigation(startDestination = Screen.EXPLORE, route = Route.EXPLORE) {
      composable(Screen.EXPLORE) {
        ExploreScreen(navigationActions, associationViewModel, searchViewModel)
      }
      composable(Screen.ASSOCIATION_PROFILE) { navBackStackEntry ->
        // Get the association UID from the arguments
        val uid = navBackStackEntry.arguments?.getString("uid")

        // Create the AssociationProfile screen with the association UID
        uid?.let {
          AssociationProfileScreen(navigationAction = navigationActions, associationId = it)
        }
        uid?.let { AssociationProfileScreen(navigationActions, it, associationViewModel) }
            ?: run {
              Log.e("AssociationProfile", "Association UID is null")
              Toast.makeText(context, "Association UID is null", Toast.LENGTH_SHORT).show()
            }
      }
    }
    navigation(startDestination = Screen.SAVED, route = Route.SAVED) {
      composable(Screen.SAVED) { SavedScreen(navigationActions) }
    }
    navigation(startDestination = Screen.MY_PROFILE, route = Route.MY_PROFILE) {
      composable(Screen.MY_PROFILE) { UserProfileScreen(navigationActions, userViewModel) }
      composable(Screen.SETTINGS) { SettingsScreen(navigationActions) }
    }
  }
}
