package com.android.unio.ui.authentication

import android.annotation.SuppressLint
import androidx.compose.material.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable

@SuppressLint("UnusedMaterialScaffoldPaddingParameter")
@Composable
fun LoginScreen() {
    Scaffold (content = { Text("Login screen") })
}