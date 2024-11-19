package com.android.unio.ui.home

import android.util.Log
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Place
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DockedSearchBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SearchBarDefaults
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Alignment.Companion.CenterHorizontally
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.android.unio.R
import com.android.unio.model.event.Event
import com.android.unio.model.event.EventViewModel
import com.android.unio.model.search.SearchViewModel
import com.android.unio.model.strings.test_tags.HomeTestTags
import com.android.unio.model.user.UserViewModel
import com.android.unio.ui.event.EventCard
import com.android.unio.ui.navigation.BottomNavigationMenu
import com.android.unio.ui.navigation.LIST_TOP_LEVEL_DESTINATION
import com.android.unio.ui.navigation.NavigationAction
import com.android.unio.ui.navigation.Route
import com.android.unio.ui.navigation.Screen
import kotlinx.coroutines.launch

@Composable
fun HomeScreen(
    navigationAction: NavigationAction,
    eventViewModel: EventViewModel,
    userViewModel: UserViewModel,
    searchViewModel: SearchViewModel
) {
  val context = LocalContext.current

  val searchResults by searchViewModel.events.collectAsState()
  val searchState by searchViewModel.status.collectAsState()

  Scaffold(
      floatingActionButton = {
        FloatingActionButton(
            onClick = { navigationAction.navigateTo(Screen.MAP) },
            modifier = Modifier.testTag(HomeTestTags.MAP_BUTTON)) {
              Icon(
                  imageVector = Icons.Filled.Place,
                  contentDescription =
                      context.getString(R.string.home_content_description_map_button))
            }
      },
      bottomBar = {
        BottomNavigationMenu(
            { navigationAction.navigateTo(it.route) }, LIST_TOP_LEVEL_DESTINATION, Route.HOME)
      },
      modifier = Modifier.testTag(HomeTestTags.SCREEN),
      content = { paddingValues ->
        TopBar(
            navigationAction,
            searchState,
            searchResults,
            userViewModel,
            searchViewModel,
            eventViewModel,
            paddingValues)
      })
}

@Composable
fun HomeContent(
    navigationAction: NavigationAction,
    searchQuery: String,
    searchState: SearchViewModel.Status,
    searchResults: List<Event>,
    userViewModel: UserViewModel,
    eventViewModel: EventViewModel,
    isOnFollowScreen: Boolean,
    paddingValues: PaddingValues
) {

  val context = LocalContext.current
  val followedAsso by userViewModel.followedAssociations.collectAsState()
  val allEvent by eventViewModel.events.collectAsState()
  val events: List<Event> =
      if (isOnFollowScreen) {
        allEvent.filter { followedAsso.any { uid -> it.organisers.contains(uid) } }
      } else {
        allEvent
      }

  // Event List
  if (searchQuery.isNotEmpty() &&
      (searchState == SearchViewModel.Status.SUCCESS ||
          searchState == SearchViewModel.Status.LOADING)) {
    if (searchResults.isEmpty()) {
      Box(
          modifier = Modifier.fillMaxSize().padding(paddingValues),
          contentAlignment = Alignment.Center) {
            Text(context.getString(R.string.explore_search_no_results))
          }
    } else {
      EventList(navigationAction, searchResults, userViewModel, eventViewModel)
    }
  } else if (events.isNotEmpty()) {
    EventList(navigationAction, events, userViewModel, eventViewModel)
  } else {
    Box(
        modifier = Modifier.fillMaxSize().padding(paddingValues),
        contentAlignment = Alignment.Center) {
          Text(
              modifier = Modifier.testTag(HomeTestTags.EMPTY_EVENT_PROMPT),
              text = context.getString(R.string.event_no_events_available))
        }
  }
}

@Composable
fun EventList(
    navigationAction: NavigationAction,
    events: List<Event>,
    userViewModel: UserViewModel,
    eventViewModel: EventViewModel
) {
  LazyColumn(
      contentPadding = PaddingValues(vertical = 8.dp),
      modifier = Modifier.fillMaxSize().padding(horizontal = 32.dp)) {
        items(events) { event -> EventCard(navigationAction, event, userViewModel, eventViewModel) }
      }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TopBar(
    navigationAction: NavigationAction,
    searchState: SearchViewModel.Status,
    searchResults: List<Event>,
    userViewModel: UserViewModel,
    searchViewModel: SearchViewModel,
    eventViewModel: EventViewModel,
    paddingValues: PaddingValues
) {
  val nbOfTabs = 2
  val context = LocalContext.current

  Column(
      horizontalAlignment = CenterHorizontally,
      modifier =
          Modifier.padding(paddingValues).padding(top = 20.dp, bottom = 50.dp).fillMaxSize()) {
        var searchQuery by remember { mutableStateOf("") }

        val list =
            listOf(
                context.getString(R.string.home_tab_all),
                context.getString(R.string.home_tab_following))
        val scope = rememberCoroutineScope()
        val pagerState = rememberPagerState(initialPage = 0) { nbOfTabs }

        val sizeList = remember { mutableStateMapOf<Int, Pair<Float, Float>>() }

        val progressFromFirstPage by remember {
          derivedStateOf { pagerState.currentPageOffsetFraction + pagerState.currentPage.dp.value }
        }
        val colorScheme = MaterialTheme.colorScheme

        DockedSearchBar(
            inputField = {
              SearchBarDefaults.InputField(
                  modifier = Modifier.testTag(HomeTestTags.SEARCH_BAR_INPUT),
                  query = searchQuery,
                  onQueryChange = {
                    // Search when query changes
                    searchQuery = it
                    searchViewModel.debouncedSearch(it, SearchViewModel.SearchType.EVENT)
                  },
                  onSearch = {},
                  expanded = false,
                  onExpandedChange = {},
                  placeholder = { Text(text = context.getString(R.string.search_placeholder)) },
                  trailingIcon = {
                    if (searchState == SearchViewModel.Status.LOADING) {
                      CircularProgressIndicator()
                    } else {
                      Icon(
                          Icons.Default.Search,
                          contentDescription =
                              context.getString(R.string.home_content_description_search_icon))
                    }
                  },
              )
            },
            expanded = false,
            onExpandedChange = {},
            modifier = Modifier.padding(horizontal = 16.dp).testTag(HomeTestTags.SEARCH_BAR)) {}

        // Tab Menu at the top of the page
        TabRow(
            selectedTabIndex = pagerState.currentPage,
            contentColor = colorScheme.primary,
            divider = {},
            indicator = {
              // method that draws the indicator bar below the tab menu
              Box(
                  modifier =
                      Modifier.fillMaxSize().drawBehind {
                        val tabWidth: Float
                        val height: Float
                        if (sizeList[0] == null) {
                          Log.e(
                              "Home Page", "The size values of tabs are null, should not happen !")
                          // hardcoded values in case sizeList[0] is null
                          tabWidth = 576.0F
                          height = 92.0F
                        } else {
                          tabWidth = sizeList[0]!!.first
                          height = sizeList[0]!!.second
                        }
                        val startOffset =
                            Offset(
                                x = progressFromFirstPage * tabWidth + tabWidth / 3,
                                y = height - 25)
                        val endOffset =
                            Offset(
                                x = progressFromFirstPage * tabWidth + tabWidth * 2 / 3,
                                y = height - 25)

                        drawLine(
                            start = startOffset,
                            end = endOffset,
                            color = colorScheme.primary,
                            strokeWidth = Stroke.DefaultMiter)
                      })
            }) {
              val tabTestTags = listOf(HomeTestTags.TAB_ALL, HomeTestTags.TAB_FOLLOWING)
              list.forEachIndexed { index, str ->
                Tab(
                    selected = index == pagerState.currentPage,
                    // animate pager if click on tab
                    onClick = { scope.launch { pagerState.animateScrollToPage(index) } },
                    modifier =
                        Modifier.testTag(tabTestTags[index]).onSizeChanged {
                          sizeList[index] = Pair(it.width.toFloat(), it.height.toFloat())
                        },
                    selectedContentColor = colorScheme.primary) {
                      Text(
                          text = str,
                          style =
                              TextStyle(
                                  fontWeight = FontWeight.Bold,
                                  fontSize = 12.sp,
                              ),
                          modifier =
                              Modifier.align(CenterHorizontally)
                                  .padding(horizontal = 32.dp, vertical = 16.dp))
                    }
              }
            }
        // Pager Menu
        HorizontalPager(
            state = pagerState, modifier = Modifier.fillMaxWidth().padding(top = 15.dp)) {
              HomeContent(
                  navigationAction,
                  searchQuery,
                  searchState,
                  searchResults,
                  userViewModel,
                  eventViewModel,
                  pagerState.currentPage == 1,
                  paddingValues)
            }
      }
}
