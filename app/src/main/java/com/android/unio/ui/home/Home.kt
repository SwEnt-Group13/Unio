package com.android.unio.ui.home

import android.annotation.SuppressLint
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import com.android.unio.ui.navigation.BottomNavigationMenu
import com.android.unio.ui.navigation.LIST_TOP_LEVEL_DESTINATION
import com.android.unio.ui.navigation.NavigationAction
import com.android.unio.ui.navigation.Route

@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@Composable
fun HomeScreen(navigationAction: NavigationAction) {
    Scaffold(content = { Text("Home screen") },
        modifier = Modifier.testTag("HomeScreen"),
        bottomBar = {
            BottomNavigationMenu(
                { navigationAction.navigateTo(Route.HOME) },
                LIST_TOP_LEVEL_DESTINATION,
                Route.HOME
            )
        })
}
