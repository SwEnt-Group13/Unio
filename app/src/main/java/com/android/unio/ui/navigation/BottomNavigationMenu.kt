package com.android.unio.ui.navigation

import androidx.compose.material.BottomNavigation
import androidx.compose.material.BottomNavigationDefaults
import androidx.compose.material.BottomNavigationItem
import androidx.compose.material.Icon
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag

@Composable
fun BottomNavigationMenu(
    onSelection: (TopLevelDestination) -> Unit,
    tabList: List<TopLevelDestination>,
    selectedItem: String
) {
  BottomNavigation(
      windowInsets = BottomNavigationDefaults.windowInsets,
      modifier = Modifier.testTag("bottomNavigationMenu")) {
        tabList.map { tld ->
          BottomNavigationItem(
              modifier = Modifier.testTag(tld.textId),
              label = { Text(tld.route) },
              icon = { Icon(tld.icon, tld.textId) },
              selected = selectedItem == tld.route,
              onClick = { onSelection(tld) })
        }
      }
}
