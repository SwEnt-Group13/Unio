package com.android.unio

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navigation
import com.android.unio.ui.authentication.LoginScreen
import com.android.unio.ui.authentication.WelcomeScreen
import com.android.unio.ui.explore.ExploreScreen
import com.android.unio.ui.home.HomeScreen
import com.android.unio.ui.navigation.NavigationAction
import com.android.unio.ui.navigation.Route
import com.android.unio.ui.navigation.Screen
import com.android.unio.ui.saved.SavedScreen
import com.android.unio.ui.user.UserProfileScreen

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent { Surface(modifier = Modifier.fillMaxSize()) { UnioApp() } }
    }
}

@Composable
fun UnioApp() {
    val navController = rememberNavController()
    val navigationActions = NavigationAction(navController)

    // start destination should be Route.AUTH, but for now, we let it be Route.HOME for testing
    // purposes
    NavHost(navController = navController, startDestination = Route.AUTH) {
        navigation(startDestination = Screen.WELCOME, route = Route.AUTH) {
            composable(Screen.WELCOME) { WelcomeScreen(navigationActions) }
            composable(Screen.AUTH) { LoginScreen(navigationActions) }
        }
        navigation(startDestination = Screen.HOME, route = Route.HOME) {
            composable(Screen.HOME) { HomeScreen(navigationActions) }
        }
        navigation(startDestination = Screen.EXPLORE, route = Route.EXPLORE) {
            composable(Screen.EXPLORE) { ExploreScreen(navigationActions) }
        }
        navigation(startDestination = Screen.SAVED, route = Route.SAVED) {
            composable(Screen.SAVED) { SavedScreen(navigationActions) }
        }
        navigation(startDestination = Screen.MY_PROFILE, route = Route.MY_PROFILE) {
            composable(Screen.MY_PROFILE) { UserProfileScreen(navigationActions) }
        }
    }
}
