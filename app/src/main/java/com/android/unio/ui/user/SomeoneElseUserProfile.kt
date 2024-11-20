package com.android.unio.ui.user

import android.annotation.SuppressLint
import android.util.Log
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import com.android.unio.R
import com.android.unio.model.strings.test_tags.SomeoneElseUserProfileTestTags
import com.android.unio.model.user.UserViewModel
import com.android.unio.ui.navigation.NavigationAction

@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SomeoneElseUserProfileScreen(navigationAction: NavigationAction, userViewModel: UserViewModel) {
  val context = LocalContext.current
  val user by userViewModel.selectedSomeoneElseUser.collectAsState()
  if (user == null) {
    Log.e("SomeoneElseUserProfile", "No user selected")
  } else {
    //    UserProfileScreenScaffold(user!!, navigationAction, false) {
    //      userViewModel.refreshSomeoneElseUser()
    //    }
    Scaffold(
        modifier = Modifier.testTag(SomeoneElseUserProfileTestTags.SCREEN),
        topBar = {
          TopAppBar(
              title = {
                Text(
                    text = user!!.firstName + " " + user!!.lastName,
                    modifier = Modifier.testTag(SomeoneElseUserProfileTestTags.NAME))
              },
              navigationIcon = {
                IconButton(
                    onClick = { navigationAction.goBack() },
                    content = {
                      Icon(
                          imageVector = Icons.AutoMirrored.Outlined.ArrowBack,
                          contentDescription = context.getString(R.string.association_go_back))
                    })
              })
        }) { padding ->
          Box(
              modifier =
                  Modifier.padding(padding).fillMaxHeight().verticalScroll(rememberScrollState())) {
                UserProfileScreenContent(navigationAction, user!!)
              }
        }
  }
}
