package com.android.unio.ui.authentication

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import com.android.unio.ui.navigation.NavigationAction

@Composable
fun AccountDetails(navigationAction: NavigationAction) {
  Scaffold(modifier = Modifier.testTag("AccountDetails")) { padding ->
    Surface(modifier = Modifier.padding(padding)) { Text("Account details when creating account") }
  }
}
