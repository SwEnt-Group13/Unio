package com.android.unio.ui.user

import android.annotation.SuppressLint
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag

@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@Composable
fun UserProfileScreen() {
  Scaffold (modifier = Modifier.testTag("UserProfileScreen")){ Text("User profile screen") }
}
