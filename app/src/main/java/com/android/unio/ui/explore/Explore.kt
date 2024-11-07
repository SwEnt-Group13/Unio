package com.android.unio.ui.explore

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.DockedSearchBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.ListItem
import androidx.compose.material3.Scaffold
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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.android.unio.R
import com.android.unio.model.association.Association
import com.android.unio.model.association.AssociationCategory
import com.android.unio.model.association.AssociationViewModel
import com.android.unio.model.search.SearchViewModel
import com.android.unio.ui.navigation.BottomNavigationMenu
import com.android.unio.ui.navigation.LIST_TOP_LEVEL_DESTINATION
import com.android.unio.ui.navigation.NavigationAction
import com.android.unio.ui.navigation.Route
import com.android.unio.ui.navigation.Screen
import com.android.unio.ui.theme.AppTypography

@Composable
fun ExploreScreen(
    navigationAction: NavigationAction,
    associationViewModel: AssociationViewModel = viewModel(factory = AssociationViewModel.Factory),
    searchViewModel: SearchViewModel
) {

  Scaffold(
      bottomBar = {
        BottomNavigationMenu(
            { navigationAction.navigateTo(it.route) }, LIST_TOP_LEVEL_DESTINATION, Route.EXPLORE)
      },
      modifier = Modifier.testTag("exploreScreen"),
      content = { padding ->
        ExploreScreenContent(padding, navigationAction, associationViewModel, searchViewModel)
      })
}

/**
 * The content of the Explore screen. It displays a list of associations grouped by category.
 *
 * @param padding The padding values to apply to the content.
 * @param navigationAction The navigation action to use when an association is clicked.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExploreScreenContent(
    padding: PaddingValues,
    navigationAction: NavigationAction,
    associationViewModel: AssociationViewModel,
    searchViewModel: SearchViewModel
) {
  val associationsByCategory by associationViewModel.associationsByCategory.collectAsState()
  val searchQuery = remember { mutableStateOf("") }
  var expanded by rememberSaveable { mutableStateOf(false) }
  val assocationResults by searchViewModel.associations.collectAsState()
  val searchState by searchViewModel.status.collectAsState()
  val context = LocalContext.current

  Column(
      modifier = Modifier.padding(padding).fillMaxWidth(),
      horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = "Explore our Associations",
            /** Will go in the string.xml */
            style = AppTypography.headlineLarge,
            modifier =
                Modifier.padding(top = 16.dp, bottom = 8.dp)
                    .align(Alignment.CenterHorizontally)
                    .testTag("exploreTitle"))

        DockedSearchBar(
            inputField = {
              SearchBarDefaults.InputField(
                  modifier = Modifier.testTag("searchBarInput"),
                  query = searchQuery.value,
                  onQueryChange = {
                    searchQuery.value = it
                    searchViewModel.debouncedSearch(it)
                  },
                  onSearch = {},
                  expanded = expanded,
                  onExpandedChange = { expanded = it },
                  placeholder = {
                    Text(
                        text = context.getString(R.string.explore_search_placeholder),
                        style = AppTypography.bodyLarge,
                        modifier = Modifier.testTag("searchPlaceHolder"))
                  },
                  trailingIcon = {
                    Icon(
                        Icons.Default.Search,
                        contentDescription = "Search icon",
                        modifier = Modifier.testTag("searchTrailingIcon"))
                  },
              )
            },
            expanded = expanded,
            onExpandedChange = { expanded = it },
            modifier = Modifier.padding(horizontal = 16.dp).testTag("searchBar")) {
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
                  if (assocationResults.isEmpty()) {
                    Box(
                        modifier = Modifier.fillMaxWidth().padding(16.dp),
                        contentAlignment = Alignment.Center) {
                          Text(context.getString(R.string.explore_search_no_results))
                        }
                  } else {
                    Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                      assocationResults.forEach { association ->
                        ListItem(
                            modifier =
                                Modifier.clickable {
                                  expanded = false
                                  navigationAction.navigateTo(
                                      Screen.withParams(
                                          Screen.ASSOCIATION_PROFILE, association.uid))
                                },
                            headlineContent = { Text(association.name) },
                        )
                      }
                    }
                  }
                }
              }
            }

        LazyColumn(
            modifier = Modifier.fillMaxSize().testTag("categoriesList"),
            contentPadding = PaddingValues(vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
          getSortedEntriesAssociationsByCategory(associationsByCategory).forEach {
              (category, associations) ->
            val alphabeticalAssociations = getFilteredAssociationsByAlphabeticalOrder(associations)

            if (alphabeticalAssociations.isNotEmpty()) {
              item {
                Text(
                    text = category.displayName,
                    style = AppTypography.headlineSmall,
                    modifier =
                        Modifier.padding(horizontal = 16.dp)
                            .testTag("category_${category.displayName}"))

                // Horizontal scrollable list of associations
                LazyRow(
                    modifier =
                        Modifier.fillMaxSize()
                            .padding(vertical = 16.dp)
                            .testTag("associationRow_${category.displayName}"),
                    contentPadding = PaddingValues(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(32.dp, Alignment.Start),
                    verticalAlignment = Alignment.CenterVertically) {
                      items(alphabeticalAssociations.size) { index ->
                        AssociationItem(alphabeticalAssociations[index], navigationAction)
                      }
                    }
              }
            }
          }
        }
      }
}

/**
 * A single item in the horizontal list of associations. When clicked, it navigates to the
 * association profile.
 *
 * @param association The association to display.
 * @param navigationAction The navigation action to use when the item is clicked.
 */
@Composable
fun AssociationItem(association: Association, navigationAction: NavigationAction) {
  Column(
      modifier =
          Modifier.clickable {
                navigationAction.navigateTo(
                    Screen.withParams(Screen.ASSOCIATION_PROFILE, association.uid))
              }
              .testTag("associationItem_${association.name}")) {
        /**
         * AdEC image is used as the placeholder. Will need to add the actual image later, when the
         * actual view model is used.
         */
        Image(
            painter = painterResource(id = R.drawable.adec),
            contentDescription = "image description",
            modifier = Modifier.size(124.dp))

        /**
         * AsyncImage( model = association.image.toUri(), contentDescription = "Translated
         * description of what the image contains", modifier =
         * Modifier.size(124.dp).testTag("associationImage"), contentScale = ContentScale.Crop //
         * crop the image to fit )
         */
        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = association.name,
            style = AppTypography.bodyMedium,
            modifier =
                Modifier.fillMaxWidth()
                    .align(Alignment.CenterHorizontally)
                    .testTag("associationName_${association.name}"))
      }
}

/** Returns a list of associations sorted by alphabetical order. */
fun getFilteredAssociationsByAlphabeticalOrder(associations: List<Association>): List<Association> {
  return associations.sortedBy { it.name }
}

/** Returns the entries of the association map sorted by the key's display name. */
fun getSortedEntriesAssociationsByCategory(
    associationsByCategory: Map<AssociationCategory, List<Association>>
): List<Map.Entry<AssociationCategory, List<Association>>> {
  return associationsByCategory.entries.sortedBy { it.key.displayName }
}
