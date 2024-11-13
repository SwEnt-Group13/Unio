package com.android.unio.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.Search
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import com.android.unio.model.strings.test_tags.BottomNavBarTestTags
import com.android.unio.resources.BookmarkIcon

open class NavigationAction(val navController: NavHostController) {

  /**
   * Navigate to the specified screen.
   *
   * @param screen The screen to navigate to
   */
  open fun navigateTo(screen: String) {
    if (getCurrentRoute() == screen) {
      return
    }
    navController.navigate(screen)
  }

  /**
   * Navigate to the specified [TopLevelDestination]
   *
   * @param tld Main destination to navigate to, clearing the back stack when navigating to a new
   *   one.
   */
  open fun navigateTo(tld: TopLevelDestination) {
    navController.navigate(tld.route) {
      popUpTo(navController.graph.findStartDestination().id) {
        saveState = true
        inclusive = true
      }

      // To avoid having multiples copies of the same destination if we reselct the same item
      launchSingleTop = true

      if (tld.route != Route.AUTH) {
        restoreState = true
      }
    }
  }

  /** Navigate back to the previous screen. */
  open fun goBack() {
    navController.popBackStack()
  }

  /**
   * Navigate to the specified [TopLevelDestination] while allowing to pop up the stack
   *
   * @param screen Main destination to navigate to, clearing the back stack when navigating to a new
   *   one.
   * @param screenPopUpTo Destination to pop up to
   */
  open fun navigateTo(screen: String, screenPopUpTo: String) {
    navController.navigate(screen) {
      popUpTo(screenPopUpTo) {
        saveState = true
        inclusive = true
      }

      // To avoid having multiples copies of the same destination if we reselect the same item
      launchSingleTop = true

      if (screen != Route.AUTH) {
        restoreState = true
      }
    }
  }

  /**
   * Get the current route.
   *
   * @return The current route
   */
  open fun getCurrentRoute(): String {
    return navController.currentDestination?.route ?: ""
  }
}

val LIST_TOP_LEVEL_DESTINATION =
    listOf(
        TopLevelDestinations.HOME,
        TopLevelDestinations.SAVED,
        TopLevelDestinations.EXPLORE,
        TopLevelDestinations.MY_PROFILE)

data class TopLevelDestination(val route: String, val icon: ImageVector, val textId: String)

object TopLevelDestinations {
  val HOME =
      TopLevelDestination(
          route = Route.HOME, icon = Icons.Outlined.Home, textId = BottomNavBarTestTags.HOME)
  val SAVED =
      TopLevelDestination(
          route = Route.SAVED, icon = BookmarkIcon, textId = BottomNavBarTestTags.SAVED)
  val EXPLORE =
      TopLevelDestination(
          route = Route.EXPLORE,
          icon = Icons.Outlined.Search,
          textId = BottomNavBarTestTags.EXPLORE)
  val MY_PROFILE =
      TopLevelDestination(
          route = Route.MY_PROFILE,
          icon = Icons.Outlined.Person,
          textId = BottomNavBarTestTags.MY_PROFILE)
}

object Route {
  const val HOME = "Home"
  const val AUTH = "Auth"
  const val EXPLORE = "Explore"
  const val SAVED = "Saved"
  const val MY_PROFILE = "MyProfile"
}

object Screen {
  const val WELCOME = "Welcome Screen"
  const val EMAIL_VERIFICATION = "Email Verification Screen"
  const val ACCOUNT_DETAILS = "Account Details Screen"
  const val HOME = "Home Screen"
  const val MAP = "Map Screen"
  const val EXPLORE = "Explore Screen"
  const val SAVED = "Saved Screen"
  const val MY_PROFILE = "MyProfile Screen"
  const val SETTINGS = "Settings"
  const val ASSOCIATION_PROFILE = "Association Profile Screen"
  const val EDIT_ASSOCIATION = "Edit Assocation Screen"
  const val EVENT_DETAILS = "Event Details Screen"
  const val EVENT_CREATION = "Event Creation Screen"
  const val EDIT_EVENT = "Edit Event Screen"

  /**
   * Replace the placeholders in the screen with the provided parameters.
   *
   * @param screen The screen with placeholders
   * @param params The parameters to replace the placeholders with
   */
  fun withParams(screen: String, vararg params: String): String {
    return params.fold(screen) { acc, param -> acc.replaceFirst(Regex("\\{[^}]*\\}"), param) }
  }
}
