package com.android.unio

import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navigation
import com.android.unio.model.association.AssociationViewModel
import com.android.unio.model.event.EventListViewModel
import com.android.unio.model.user.UserRepositoryFirestore
import com.android.unio.model.user.UserViewModel
import com.android.unio.ui.accountCreation.AccountDetails
import com.android.unio.ui.association.AssociationProfileScreen
import com.android.unio.ui.authentication.EmailVerificationScreen
import com.android.unio.ui.authentication.WelcomeScreen
import com.android.unio.ui.explore.ExploreScreen
import com.android.unio.ui.home.HomeScreen
import com.android.unio.ui.navigation.NavigationAction
import com.android.unio.ui.navigation.Route
import com.android.unio.ui.navigation.Screen
import com.android.unio.ui.saved.SavedScreen
import com.android.unio.ui.theme.AppTheme
import com.android.unio.ui.user.UserProfileScreen
import com.google.firebase.Firebase
import com.google.firebase.firestore.firestore

class MainActivity : ComponentActivity() {
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContent { Surface(modifier = Modifier.fillMaxSize()) { AppTheme { UnioApp() } } }
  }
}

@Composable
fun UnioApp() {
  val navController = rememberNavController()
  val navigationActions = NavigationAction(navController)

  val associationRepository = AssociationRepositoryFirestore(Firebase.firestore)
  val eventRepository = EventRepositoryFirestore(Firebase.firestore)
  val associationViewModel = AssociationViewModel(associationRepository, eventRepository)

  val userRepositoryFirestore = UserRepositoryFirestore(Firebase.firestore)
  val userViewModel = UserViewModel(userRepositoryFirestore, true)

  val context = LocalContext.current

  NavHost(navController = navController, startDestination = Route.AUTH) {
    navigation(startDestination = Screen.WELCOME, route = Route.AUTH) {
      composable(Screen.WELCOME) { WelcomeScreen(navigationActions, userRepositoryFirestore) }
      composable(Screen.EMAIL_VERIFICATION) { EmailVerificationScreen(navigationActions) }
      composable(Screen.ACCOUNT_DETAILS) { AccountDetails(navigationActions, userViewModel) }
    }
    navigation(startDestination = Screen.HOME, route = Route.HOME) {
      composable(Screen.HOME) {
        HomeScreen(navigationActions, eventViewModel, onAddEvent = {}, onEventClick = {})
      }
    }
    navigation(startDestination = Screen.EXPLORE, route = Route.EXPLORE) {
      composable(Screen.EXPLORE) { ExploreScreen(navigationActions, associationViewModel) }
      composable(Screen.ASSOCIATION_PROFILE) { navBackStackEntry ->
        // Get the association UID from the arguments
        val uid = navBackStackEntry.arguments?.getString("uid")

        // Create the AssociationProfile screen with the association UID
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
    }
  }
}
