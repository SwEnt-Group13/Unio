package com.android.unio.ui.association

import android.annotation.SuppressLint
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag

@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@Composable
fun AssociationScreen() {
  Scaffold(modifier = Modifier.testTag("AssociationScreen")) { Text("Association screen") }
}
