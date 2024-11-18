package com.android.unio.ui.event.overlay

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.OutlinedButton
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.SearchBar
import androidx.compose.material3.SearchBarDefaults
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.wear.compose.material.Text
import com.android.unio.R
import com.android.unio.model.association.Association
import com.android.unio.model.search.SearchViewModel
import com.android.unio.ui.theme.AppTypography

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CoauthorsOverlay(
    onDismiss: () -> Unit,
    onSave: (List<Pair<Association, MutableState<Boolean>>>) -> Unit,
    coauthors: List<Pair<Association, MutableState<Boolean>>>,
    searchViewModel: SearchViewModel,
) {
  val context = LocalContext.current
  val copiedCoauthors = coauthors.toList().map { it.first to mutableStateOf(it.second.value) }
  val searchResults by searchViewModel.associations.collectAsState()
  val searchState by searchViewModel.status.collectAsState()
  var searchQuery by remember { mutableStateOf("") }

  Dialog(onDismissRequest = onDismiss) {
    Card(elevation = CardDefaults.cardElevation(8.dp), shape = RoundedCornerShape(16.dp)) {
      Column(
          modifier =
              Modifier.fillMaxWidth()
                  .padding(15.dp)
                  .sizeIn(minHeight = 450.dp, maxHeight = 450.dp)) {
            Text("Coauthors", style = AppTypography.headlineSmall)
            Text("Select coauthors to add to your event", style = AppTypography.bodyMedium)
            SearchBar(
                inputField = {
                  SearchBarDefaults.InputField(
                      expanded = false,
                      onExpandedChange = {},
                      query = searchQuery,
                      onQueryChange = {
                        searchQuery = it
                        searchViewModel.debouncedSearch(it, SearchViewModel.SearchType.ASSOCIATION)
                      },
                      onSearch = {},
                      placeholder = {
                        // TODO change this string
                        Text(text = context.getString(R.string.explore_search_placeholder))
                      },
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
            ) {}

            // Need to show
            // If no query is entered, show all coauthors
            // If a query is entered show the search results
            // If no search results are found, show a message
            // If the search is loading, show a loading indicator
            Surface(
                modifier = Modifier.sizeIn(minHeight = 250.dp, maxHeight = 250.dp),
                color = Color.Transparent) {
                  if (searchQuery.isEmpty()) {
                    AssociationsList(copiedCoauthors)
                  } else {
                    if (searchState != SearchViewModel.Status.LOADING && searchResults.isEmpty()) {
                      Text("No results found")
                    } else {
                      AssociationsList(
                          copiedCoauthors.filter {
                            it.first.uid in searchResults.map { searchResult -> searchResult.uid }
                          })
                    }
                  }
                }

            Row(
                modifier = Modifier.fillMaxWidth().padding(8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Absolute.Right) {
                  OutlinedButton(
                      shape = RoundedCornerShape(16.dp),
                      onClick = onDismiss,
                      modifier = Modifier.padding(5.dp)) // TODO add test tag
                  {
                        Text("ADD TEXT")
                      }
                  Button(
                      onClick = { onSave(copiedCoauthors) },
                      modifier = Modifier.padding(5.dp)) // TODO add test tag
                  {
                        Text("ADD TEXT")
                      }
                }
          }
    }
  }
}

@Composable
fun AssociationsList(
    associations: List<Pair<Association, MutableState<Boolean>>>,
) {
  LazyColumn(modifier = Modifier.sizeIn(maxHeight = 280.dp)) {
    items(associations) { (copiedCoauthors, selected) ->
      Row(
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically) {
            Text(copiedCoauthors.name)
            Checkbox(checked = selected.value, onCheckedChange = { selected.value = it })
          }
    }
  }
}
