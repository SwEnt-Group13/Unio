package com.android.unio.ui.navigation

import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import com.android.unio.model.strings.test_tags.NavigationActionTestTags

/**
 * The Bottom Navigation Menu for the app that displays the tabs at the bottom of the screen and
 * allows the user to navigate between them.
 *
 * @param onSelection : Callback to be called when a tab is selected
 * @param tabList : List of tabs to be displayed
 * @param selectedItem : The selected tab
 */
@Composable
fun BottomNavigationMenu(
    onSelection: (TopLevelDestination) -> Unit,
    tabList: List<TopLevelDestination>,
    selectedItem: String
) {
  NavigationBar(modifier = Modifier.testTag(NavigationActionTestTags.BOTTOM_NAV_MENU)) {
    tabList.map { tld ->
      NavigationBarItem(
          modifier = Modifier.testTag(tld.textId),
          label = {
            Text(Route.toTranslatedString(context = LocalContext.current, route = tld.route))
          },
          icon = { Icon(tld.icon, tld.textId) },
          selected = selectedItem == tld.route,
          onClick = { onSelection(tld) })
    }
  }
}
