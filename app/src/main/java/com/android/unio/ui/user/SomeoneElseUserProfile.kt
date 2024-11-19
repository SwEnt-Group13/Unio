package com.android.unio.ui.user

import android.util.Log
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import com.android.unio.model.user.UserViewModel
import com.android.unio.ui.navigation.NavigationAction

@Composable
fun SomeoneElseUserProfileScreen(navigationAction: NavigationAction, userViewModel: UserViewModel) {
  val user by userViewModel.selectedSomeoneElseUser.collectAsState()
  if (user == null) {
    Log.e("SomeoneElseUserProfile", "No user selected")
  } else {
    UserProfileScreenScaffold(user!!, navigationAction, false) {
      userViewModel.refreshSomeoneElseUser()
    }
  }
}
