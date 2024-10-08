package com.android.unio.ui.navigation

import androidx.compose.foundation.layout.Row
import androidx.compose.material.BottomNavigation
import androidx.compose.material.BottomNavigationDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag

@Composable
fun BottomNavigationMenu() {

  BottomNavigation(
      windowInsets = BottomNavigationDefaults.windowInsets,
      modifier = Modifier.testTag("BottomNavigationMenu")) {
        Row {}
      }
}
