package com.android.unio.ui.event

import android.annotation.SuppressLint
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import com.android.unio.R
import com.android.unio.model.strings.test_tags.EventCreationTestTags

@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@Composable
fun EventCreationScreen() {
  val context = LocalContext.current
  Scaffold(modifier = Modifier.testTag(EventCreationTestTags.SCREEN)) {
    Text(context.getString(R.string.event_creation_screen))
  }
}
