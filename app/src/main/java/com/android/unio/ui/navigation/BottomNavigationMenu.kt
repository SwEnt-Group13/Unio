package com.android.unio.ui.navigation

import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag

@Composable
fun BottomNavigationMenu(
    onSelection: (TopLevelDestination) -> Unit,
    tabList: List<TopLevelDestination>,
    selectedItem: String
) {
  NavigationBar(modifier = Modifier.testTag("bottomNavigationMenu")) {
    tabList.map { tld ->
      NavigationBarItem(
          modifier = Modifier.testTag(tld.textId),
          label = { Text(tld.route) },
          icon = { Icon(tld.icon, tld.textId) },
          selected = selectedItem == tld.route,
          onClick = { onSelection(tld) })
    }
  }
}
