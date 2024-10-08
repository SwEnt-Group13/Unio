package com.android.unio.ui.navigation

import BookmarkIcon
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.Place
import androidx.compose.material.icons.outlined.Search
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController

class NavigationAction(private val navController: NavHostController) {

    fun navigateTo(route: String) {
        navController.navigate(route)
    }

    fun navigateTo(tld: TopLevelDestination) {
        navController.navigate(tld.route) {
            popUpTo(navController.graph.findStartDestination().id)
        }
    }

    fun goBack() {
        navController.popBackStack()
    }

    fun getCurrentRoute(): String {
        return navController.currentDestination?.route ?: ""
    }

}

val LIST_TOP_LEVEL_DESTINATION = listOf(
    TopLevelDestinations.HOME,
    TopLevelDestinations.SAVED,
    TopLevelDestinations.EXPLORE,
    TopLevelDestinations.MY_PROFILE,
    TopLevelDestinations.MAP
)

data class TopLevelDestination(val route: String, val icon: ImageVector, val textId: String) {}

object TopLevelDestinations {
    val HOME = TopLevelDestination(route = Route.HOME, icon = Icons.Outlined.Home, textId = "Home")
    val SAVED = TopLevelDestination(route = Route.SAVED, icon = BookmarkIcon, textId = "Saved")
    val EXPLORE = TopLevelDestination(route = Route.EXPLORE, icon = Icons.Outlined.Search, textId = "Explore")
    val MY_PROFILE = TopLevelDestination(route = Route.MY_PROFILE, icon = Icons.Outlined.Person, textId = "MyProfile")
    val MAP = TopLevelDestination(route = Route.MAP, icon = Icons.Outlined.Place, textId = "Map")
}

object Route {
    const val HOME = "Overview"
    const val MAP = "Map"
    const val AUTH = "Auth"
    const val EXPLORE = "Explore"
    const val SAVED = "Saved"
    const val MY_PROFILE = "MyProfile"
}

object Screen {
    const val AUTH = "Auth Screen"
    const val HOME = "Home Screen"
    const val MAP = "Map Screen"
    const val EXPLORE = "Explore Screen"
    const val Saved = "Saved Screen"
    const val MY_PROFILE = "MyProfile Screen"
}