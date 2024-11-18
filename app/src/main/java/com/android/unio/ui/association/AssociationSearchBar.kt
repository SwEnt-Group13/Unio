package com.android.unio.ui.association

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import com.android.unio.R
import com.android.unio.model.association.Association
import com.android.unio.model.search.SearchViewModel
import com.android.unio.model.strings.test_tags.ExploreContentTestTags
import com.android.unio.ui.theme.AppTypography

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AssociationSearchBar(
    searchViewModel: SearchViewModel,
    onAssociationSelected: (Association) -> Unit,
    modifier: Modifier = Modifier
) {
  var searchQuery by remember { mutableStateOf("") }
  var expanded by rememberSaveable { mutableStateOf(false) }
  val associationResults by searchViewModel.associations.collectAsState()
  val searchState by searchViewModel.status.collectAsState()
  val context = LocalContext.current

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
            expanded = expanded,
            onExpandedChange = { expanded = it },
            placeholder = {
              Text(
                  text = context.getString(R.string.explore_search_placeholder),
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
      expanded = expanded,
      onExpandedChange = { expanded = it },
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
                                expanded = false
                                onAssociationSelected(association)
                              }
                              .testTag(
                                  ExploreContentTestTags.ASSOCIATION_EXPLORE_RESULT +
                                      association.name),
                      headlineContent = { Text(association.name) },
                  )
                }
              }
            }
          }
        }
      }
}
