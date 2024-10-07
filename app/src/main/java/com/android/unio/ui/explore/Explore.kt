package com.android.unio.ui.explore

import android.annotation.SuppressLint
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import com.android.unio.ui.navigation.BottomNavigationMenu

@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@Composable
fun ExploreScreen() {
    Scaffold(
        content = { Text("Explore Screen") },
    )
}