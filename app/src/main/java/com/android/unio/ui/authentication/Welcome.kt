package com.android.unio.ui.authentication

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.android.unio.R // Ensure this is imported
import com.android.unio.ui.navigation.NavigationAction
import com.android.unio.ui.navigation.Screen

@Composable
fun WelcomeScreen(navigationAction: NavigationAction) {
  Scaffold(modifier = Modifier.fillMaxSize().testTag("WelcomeScreen")) {
    Column(
        modifier = Modifier.padding(it).padding(vertical = 200.dp, horizontal = 100.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally) {
          // Uncomment and update when logo resource is available
          // Image(
          //     painter = painterResource(id = R.drawable.ic_launcher_foreground),
          //     contentDescription = "Unio logo",
          //     modifier = Modifier.padding(16.dp)
          // )

          Text(
              text = stringResource(id = R.string.welcome_message), // Updated
              textAlign = TextAlign.Center,
              modifier = Modifier.padding(16.dp))
          Button(
              onClick = { navigationAction.navigateTo(Screen.AUTH) },
              modifier = Modifier.testTag("LoginButton").padding(16.dp)) {
                Text(stringResource(id = R.string.welcome_login_button)) // Updated
          }
          Button(onClick = { navigationAction.navigateTo(Screen.HOME) }) {
            Text("<Debug> " + stringResource(id = R.string.welcome_skip_home_button)) // Updated
          }
        }
  }
}
