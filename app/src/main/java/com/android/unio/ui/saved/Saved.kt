package com.android.unio.ui.saved

import android.annotation.SuppressLint
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import com.android.unio.R
import com.android.unio.model.event.EventViewModel
import com.android.unio.model.strings.test_tags.SavedTestTags
import com.android.unio.model.user.UserViewModel
import com.android.unio.ui.event.EventCard
import com.android.unio.ui.navigation.BottomNavigationMenu
import com.android.unio.ui.navigation.LIST_TOP_LEVEL_DESTINATION
import com.android.unio.ui.navigation.NavigationAction
import com.android.unio.ui.navigation.Route
import java.util.Calendar

@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@Composable
fun SavedScreen(
    navigationAction: NavigationAction,
    eventViewModel: EventViewModel,
    userViewModel: UserViewModel
) {
  val allEvents by eventViewModel.events.collectAsState()
  val savedEvents = allEvents.filter { userViewModel.isEventSavedForCurrentUser(it.uid) }
  val context = LocalContext.current

  val today = Calendar.getInstance().get(Calendar.DAY_OF_YEAR)

  Scaffold(
      content = {
          Text(context.getString(R.string.saved_screen_title))
          LazyColumn(
              contentPadding = PaddingValues(vertical = 8.dp),
              modifier = Modifier.fillMaxSize().padding(horizontal = 32.dp)) {
              items(savedEvents) { EventCard(navigationAction, it, userViewModel, eventViewModel) }
          }
                },
      modifier = Modifier.testTag(SavedTestTags.SCREEN),
      bottomBar = {
        BottomNavigationMenu(
            { navigationAction.navigateTo(it.route) }, LIST_TOP_LEVEL_DESTINATION, Route.SAVED)
      })
}
