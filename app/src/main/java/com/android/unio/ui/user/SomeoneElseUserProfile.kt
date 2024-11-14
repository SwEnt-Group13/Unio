package com.android.unio.ui.user

import android.annotation.SuppressLint
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import com.android.unio.R
import com.android.unio.model.strings.test_tags.SomeoneElseUserProfileTestTags

@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@Composable
fun SomeoneElseUserProfileScreen() {
  val context = LocalContext.current
  Scaffold(modifier = Modifier.testTag(SomeoneElseUserProfileTestTags.SCREEN)) {
    Text(context.getString(R.string.someone_else_user_profile_screen_title))
  }
}
