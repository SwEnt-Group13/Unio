package com.android.unio.ui.authentication

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import com.android.unio.ui.navigation.NavigationAction
import com.android.unio.ui.navigation.Screen

@Composable
fun LoginScreen(navigationAction: NavigationAction) {
  var email by remember { mutableStateOf("") }
  var password by remember { mutableStateOf("") }
  Scaffold(
      modifier = Modifier.testTag("LoginScreen").fillMaxSize(),
      content = { padding ->
        Column(
            modifier = Modifier.padding(padding).padding(vertical = 200.dp, horizontal = 50.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally) {
              OutlinedTextField(
                  modifier = Modifier.padding(16.dp),
                  label = { Text("Enter your university address") },
                  onValueChange = { email = it },
                  value = email)
              OutlinedTextField(
                  modifier = Modifier.padding(16.dp),
                  label = { Text("And your password") },
                  onValueChange = { password = it },
                  value = password)
              Button(
                  onClick = {
                    /* TODO Handle login with Tequila/Microsoft if possible */
                    navigationAction.navigateTo(Screen.ACCOUNT_CREATION)
                  }) {
                    Text("Continue")
                  }
            }
      })
}
