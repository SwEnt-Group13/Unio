package com.android.unio.ui.authentication

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import com.android.unio.R
import com.android.unio.ui.navigation.NavigationAction
import com.android.unio.ui.navigation.Screen

@Composable
fun WelcomeScreen(navigationAction: NavigationAction) {
    Scaffold(modifier = Modifier.fillMaxSize()) {
        Column(modifier = Modifier.padding(it)) {
            Image(
                //Placeholder for the Unio logo
                painter = painterResource(id = R.drawable.ic_launcher_foreground),
                contentDescription = "Unio logo"
            )
            Text("The world’s largest campus life platform !")
            Button(
                onClick = { navigationAction.navigateTo(Screen.AUTH) }
            ) {
                Text("Login")
            }
            Button(
                onClick = { navigationAction.navigateTo(Screen.HOME) }
            ) {
                Text("<Debug> Skip to Home")
            }
        }
    }
}