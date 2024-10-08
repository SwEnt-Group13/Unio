package com.android.unio.ui.authentication

import android.annotation.SuppressLint
import androidx.compose.material.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import com.android.unio.ui.navigation.BottomNavigationMenu

@SuppressLint("UnusedMaterialScaffoldPaddingParameter")
@Composable
fun LoginScreen() {
  Scaffold(content = { Text("Login screen") },
    modifier = Modifier.testTag("LoginScreen"))
}
