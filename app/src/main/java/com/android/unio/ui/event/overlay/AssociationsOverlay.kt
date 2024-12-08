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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.wear.compose.material.Text
import com.android.unio.R
import com.android.unio.model.association.Association
import com.android.unio.model.search.SearchViewModel
import com.android.unio.model.strings.test_tags.EventCreationOverlayTestTags
import com.android.unio.ui.theme.AppTypography

/**
 * A dialog that allows users to select associations.
 *
 * @param onDismiss Callback when the dialog is dismissed.
 * @param onSave Callback when the changes are saved.
 * @param associations The list of pairs of [Association] and their selected state.
 * @param searchViewModel The [SearchViewModel] to use.
 * @param headerText The text to display as the header.
 * @param bodyText The text to display as the body.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AssociationsOverlay(
    onDismiss: () -> Unit,
    onSave: (List<Pair<Association, MutableState<Boolean>>>) -> Unit,
    associations: List<Pair<Association, MutableState<Boolean>>>,
    searchViewModel: SearchViewModel,
    headerText: String,
    bodyText: String
) {
  val context = LocalContext.current
  val copiedAssociations = associations.toList().map { it.first to mutableStateOf(it.second.value) }
  val searchResults by searchViewModel.associations.collectAsState()
  val searchState by searchViewModel.status.collectAsState()
  var searchQuery by remember { mutableStateOf("") }

  // The text in this composable has had its color manually set as the color scheme does not apply
  // for some reason
  Dialog(onDismissRequest = onDismiss) {
    Card(
        elevation = CardDefaults.cardElevation(8.dp),
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier.testTag(EventCreationOverlayTestTags.SCREEN)) {
          Column(
              modifier =
                  Modifier.fillMaxWidth()
                      .padding(15.dp)
                      .sizeIn(minHeight = 450.dp, maxHeight = 450.dp),
              horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    headerText,
                    color = MaterialTheme.colorScheme.onSurface,
                    style = AppTypography.headlineSmall,
                    modifier = Modifier.testTag(EventCreationOverlayTestTags.TITLE))
                Text(
                    bodyText,
                    color = MaterialTheme.colorScheme.onSurface,
                    style = AppTypography.bodyMedium,
                    modifier = Modifier.testTag(EventCreationOverlayTestTags.BODY))
                SearchBar(
                    inputField = {
                      SearchBarDefaults.InputField(
                          modifier =
                              Modifier.testTag(EventCreationOverlayTestTags.SEARCH_BAR_INPUT),
                          expanded = false,
                          onExpandedChange = {},
                          query = searchQuery,
                          onQueryChange = {
                            searchQuery = it
                            searchViewModel.debouncedSearch(
                                it, SearchViewModel.SearchType.ASSOCIATION)
                          },
                          onSearch = {},
                          placeholder = {
                            Text(
                                text = context.getString(R.string.search_placeholder),
                                color = MaterialTheme.colorScheme.onSurface,
                            )
                          },
                          trailingIcon = {
                            if (searchState == SearchViewModel.Status.LOADING) {
                              CircularProgressIndicator()
                            } else {
                              Icon(
                                  Icons.Default.Search,
                                  contentDescription =
                                      context.getString(
                                          R.string.home_content_description_search_icon))
                            }
                          },
                      )
                    },
                    expanded = false,
                    onExpandedChange = {},
                ) {}

                Surface(
                    modifier =
                        Modifier.sizeIn(minHeight = 250.dp, maxHeight = 250.dp)
                            .testTag(EventCreationOverlayTestTags.ASSOCIATION_LIST),
                    color = Color.Transparent) {
                      if (searchQuery.isEmpty()) {
                        AssociationsList(copiedAssociations)
                      } else {
                        if (searchState != SearchViewModel.Status.LOADING &&
                            searchResults.isEmpty()) {
                          Text(
                              context.getString(R.string.associations_overlay_search_no_results),
                              color = MaterialTheme.colorScheme.onSurfaceVariant,
                          )
                        } else {
                          AssociationsList(
                              copiedAssociations.filter {
                                it.first.uid in
                                    searchResults.map { searchResult -> searchResult.uid }
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
                          modifier =
                              Modifier.padding(5.dp).testTag(EventCreationOverlayTestTags.CANCEL)) {
                            Text(
                                context.getString(R.string.overlay_cancel),
                                color = MaterialTheme.colorScheme.primary,
                            )
                          }
                      Button(
                          onClick = { onSave(copiedAssociations) },
                          modifier =
                              Modifier.padding(5.dp).testTag(EventCreationOverlayTestTags.SAVE)) {
                            Text(
                                context.getString(R.string.overlay_save),
                                color = MaterialTheme.colorScheme.onPrimary,
                            )
                          }
                    }
              }
        }
  }
}

/**
 * A list of associations with checkboxes.
 *
 * @param associations The list of pairs of [Association] and their selected state.
 */
@Composable
fun AssociationsList(
    associations: List<Pair<Association, MutableState<Boolean>>>,
) {
  LazyColumn(modifier = Modifier.sizeIn(maxHeight = 280.dp), horizontalAlignment = Alignment.End) {
    items(associations) { (copiedCoauthors, selected) ->
      Row(
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically,
          modifier = Modifier.padding(5.dp).fillMaxWidth()) {
            Text(
                copiedCoauthors.name,
                color = MaterialTheme.colorScheme.onSurface,
            )
            Checkbox(checked = selected.value, onCheckedChange = { selected.value = it })
          }
    }
  }
}
