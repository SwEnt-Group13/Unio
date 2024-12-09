package com.android.unio.ui.saved

import android.annotation.SuppressLint
import android.util.Log
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Place
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment.Companion.CenterHorizontally
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
import com.android.unio.ui.navigation.Screen
import com.android.unio.ui.theme.AppTypography
import java.util.Calendar

@SuppressLint("UnrememberedMutableState")
@Composable
fun SavedScreen(
    navigationAction: NavigationAction,
    eventViewModel: EventViewModel,
    userViewModel: UserViewModel
) {
  val user by userViewModel.user.collectAsState()

  if (user == null) {
    Log.e("SavedScreen", "User is null")
    return
  }

  val allEvents by eventViewModel.events.collectAsState()
  val savedEvents by derivedStateOf {
    allEvents.filter { user!!.savedEvents.contains(it.uid) }.sortedBy { it.startDate }
  }

  val context = LocalContext.current

  val today = remember { Calendar.getInstance().get(Calendar.DAY_OF_YEAR) }
  val savedEventsToday by derivedStateOf {
    savedEvents.partition {
      val calendar = Calendar.getInstance()
      calendar.timeInMillis = it.startDate.toDate().time
      calendar.get(Calendar.DAY_OF_YEAR) == today
    }
  }

  Scaffold(
      modifier = Modifier.testTag(SavedTestTags.SCREEN),
      floatingActionButton = {
        FloatingActionButton(
            onClick = { navigationAction.navigateTo(Screen.MAP) },
            modifier = Modifier.testTag(SavedTestTags.FAB),
        ) {
          Icon(
              imageVector = Icons.Filled.Place,
              contentDescription = context.getString(R.string.home_content_description_map_button))
        }
      },
      bottomBar = {
        BottomNavigationMenu(
            { navigationAction.navigateTo(it.route) }, LIST_TOP_LEVEL_DESTINATION, Route.SAVED)
      }) {
        Column(
            modifier = Modifier.fillMaxSize().padding(it).padding(vertical = 16.dp),
            horizontalAlignment = CenterHorizontally) {
              Text(
                  context.getString(R.string.saved_screen_title),
                  style = AppTypography.headlineLarge,
                  modifier = Modifier.testTag(SavedTestTags.TITLE))

              LazyColumn(
                  contentPadding = PaddingValues(vertical = 8.dp),
                  horizontalAlignment = CenterHorizontally,
                  modifier = Modifier.fillMaxSize().padding(horizontal = 32.dp),
              ) {
                if (savedEventsToday.first.isEmpty() && savedEventsToday.second.isEmpty()) {
                  item {
                    Text(
                        context.getString(R.string.saved_screen_no_events),
                        modifier = Modifier.padding(top = 16.dp).testTag(SavedTestTags.NO_EVENTS),
                    )
                  }
                }
                if (savedEventsToday.first.isNotEmpty()) {
                  item {
                    Text(
                        context.getString(R.string.saved_screen_today),
                        modifier =
                            Modifier.padding(top = 16.dp, bottom = 8.dp)
                                .testTag(SavedTestTags.TODAY),
                        style = AppTypography.headlineSmall)
                  }
                  item { HorizontalDivider() }

                  items(savedEventsToday.first) { event ->
                    EventCard(navigationAction, event, userViewModel, eventViewModel)
                  }
                }

                if (savedEventsToday.second.isNotEmpty()) {
                  item {
                    Text(
                        context.getString(R.string.saved_screen_upcoming),
                        modifier =
                            Modifier.padding(top = 16.dp, bottom = 8.dp)
                                .testTag(SavedTestTags.UPCOMING),
                        style = AppTypography.headlineSmall)
                  }
                  item { HorizontalDivider() }

                  items(savedEventsToday.second) { event ->
                    EventCard(navigationAction, event, userViewModel, eventViewModel)
                  }
                }
              }
            }
      }
}
