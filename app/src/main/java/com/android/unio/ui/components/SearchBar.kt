package com.android.unio.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.DockedSearchBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.ListItem
import androidx.compose.material3.SearchBarDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import com.android.unio.R
import com.android.unio.model.association.Association
import com.android.unio.model.association.Member
import com.android.unio.model.event.Event
import com.android.unio.model.search.SearchViewModel
import com.android.unio.model.strings.test_tags.explore.ExploreContentTestTags
import com.android.unio.ui.theme.AppTypography

/**
 * A general search bar composable that can be configured for different use cases.
 *
 * @param searchQuery The current search query.
 * @param onQueryChange Callback invoked when the search query changes.
 * @param results The list of search results to display.
 * @param onResultClick Callback invoked when a search result is clicked.
 * @param searchState The current search status.
 * @param shouldCloseExpandable Whether the search bar should close the expandable when expanded.
 * @param onOutsideClickHandled Callback invoked when an outside click is handled.
 * @param resultContent A composable to define how each result is displayed.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun <T> SearchBar(
    searchQuery: String,
    onQueryChange: (String) -> Unit,
    results: List<T>,
    onResultClick: (T) -> Unit,
    searchState: SearchViewModel.Status,
    shouldCloseExpandable: Boolean,
    onOutsideClickHandled: () -> Unit,
    resultContent: @Composable (T) -> Unit
) {
  var isExpanded by rememberSaveable { mutableStateOf(false) }
  val context = LocalContext.current

  if (shouldCloseExpandable && isExpanded) {
    isExpanded = false
    onOutsideClickHandled()
  }

  DockedSearchBar(
      inputField = {
        SearchBarDefaults.InputField(
            modifier = Modifier.testTag("SEARCH_BAR_INPUT"),
            query = searchQuery,
            onQueryChange = { onQueryChange(it) },
            onSearch = {},
            expanded = isExpanded,
            onExpandedChange = { isExpanded = it },
            placeholder = {
              Text(
                  text = context.getString(R.string.search_placeholder),
                  style = AppTypography.bodyLarge,
                  modifier = Modifier.testTag("SEARCH_BAR_PLACEHOLDER"))
            },
            trailingIcon = {
              Icon(
                  Icons.Default.Search,
                  contentDescription =
                      context.getString(R.string.explore_content_description_search_icon),
                  modifier = Modifier.testTag("SEARCH_TRAILING_ICON"))
            },
        )
      },
      expanded = isExpanded,
      onExpandedChange = { isExpanded = it },
      modifier = Modifier.padding(horizontal = 16.dp).testTag("SEARCH_BAR")) {
        when (searchState) {
          SearchViewModel.Status.ERROR -> {
            Box(
                modifier = Modifier.fillMaxWidth().padding(16.dp),
                contentAlignment = Alignment.Center) {
                  Text(context.getString(R.string.explore_search_error_message))
                }
          }
          SearchViewModel.Status.LOADING -> {
            Box(
                modifier = Modifier.fillMaxWidth().padding(16.dp),
                contentAlignment = Alignment.Center) {
                  LinearProgressIndicator()
                }
          }
          SearchViewModel.Status.IDLE -> {}
          SearchViewModel.Status.SUCCESS -> {
            if (results.isEmpty()) {
              Box(
                  modifier = Modifier.fillMaxWidth().padding(16.dp),
                  contentAlignment = Alignment.Center) {
                    Text(context.getString(R.string.explore_search_no_results))
                  }
            } else {
              Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                results.forEach { result ->
                  ListItem(
                      modifier =
                          Modifier.clickable {
                            isExpanded = false
                            onResultClick(result)
                          },
                      headlineContent = { resultContent(result) })
                }
              }
            }
          }
        }
      }
}

/**
 * A search bar that allows users to search for associations. The last 2 parameters are used to
 * handle the expandable state of the search bar. For an example of how to use this, see Explore.kt
 *
 * @param searchViewModel [SearchViewModel] that provides the search results.
 * @param onAssociationSelected Callback when an association is selected.
 * @param shouldCloseExpandable Whether the search bar should close the expandable when it is
 *   expanded.
 * @param onOutsideClickHandled Callback when the outside click is handled.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AssociationSearchBar(
    searchViewModel: SearchViewModel,
    onAssociationSelected: (Association) -> Unit,
    shouldCloseExpandable: Boolean,
    onOutsideClickHandled: () -> Unit
) {
  var searchQuery by remember { mutableStateOf("") }
  var isExpanded by rememberSaveable { mutableStateOf(false) }
  val associationResults by searchViewModel.associations.collectAsState()
  val searchState by searchViewModel.status.collectAsState()
  val context = LocalContext.current

  if (shouldCloseExpandable && isExpanded) {
    isExpanded = false
    onOutsideClickHandled()
  }

  DockedSearchBar(
      inputField = {
        SearchBarDefaults.InputField(
            modifier = Modifier.testTag(ExploreContentTestTags.SEARCH_BAR_INPUT),
            query = searchQuery,
            onQueryChange = {
              searchQuery = it
              searchViewModel.debouncedSearch(it, SearchViewModel.SearchType.ASSOCIATION)
            },
            onSearch = {},
            expanded = isExpanded,
            onExpandedChange = { isExpanded = it },
            placeholder = {
              Text(
                  text = context.getString(R.string.search_placeholder),
                  style = AppTypography.bodyLarge,
                  modifier = Modifier.testTag(ExploreContentTestTags.SEARCH_BAR_PLACEHOLDER))
            },
            trailingIcon = {
              Icon(
                  Icons.Default.Search,
                  contentDescription =
                      context.getString(R.string.explore_content_description_search_icon),
                  modifier = Modifier.testTag(ExploreContentTestTags.SEARCH_TRAILING_ICON))
            },
        )
      },
      expanded = isExpanded,
      onExpandedChange = { isExpanded = it },
      modifier = Modifier.padding(horizontal = 16.dp).testTag(ExploreContentTestTags.SEARCH_BAR)) {
        when (searchState) {
          SearchViewModel.Status.ERROR -> {
            Box(
                modifier = Modifier.fillMaxWidth().padding(16.dp),
                contentAlignment = Alignment.Center) {
                  Text(context.getString(R.string.explore_search_error_message))
                }
          }
          SearchViewModel.Status.LOADING -> {
            Box(
                modifier = Modifier.fillMaxWidth().padding(16.dp),
                contentAlignment = Alignment.Center) {
                  LinearProgressIndicator()
                }
          }
          SearchViewModel.Status.IDLE -> {}
          SearchViewModel.Status.SUCCESS -> {
            if (associationResults.isEmpty()) {
              Box(
                  modifier = Modifier.fillMaxWidth().padding(16.dp),
                  contentAlignment = Alignment.Center) {
                    Text(context.getString(R.string.explore_search_no_results))
                  }
            } else {
              Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                associationResults.forEach { association ->
                  ListItem(
                      modifier =
                          Modifier.clickable {
                                isExpanded = false
                                onAssociationSelected(association)
                              }
                              .testTag(
                                  ExploreContentTestTags.ASSOCIATION_EXPLORE_RESULT +
                                      association.name),
                      headlineContent = { Text(association.name) })
                }
              }
            }
          }
        }
      }
}