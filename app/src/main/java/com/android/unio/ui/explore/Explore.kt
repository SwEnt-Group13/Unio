package com.android.unio.ui.explore

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SearchBar
import androidx.compose.material3.SearchBarDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.android.unio.R
import com.android.unio.model.association.Association
import com.android.unio.model.association.MockAssociation
import com.android.unio.model.association.MockAssociationType
import com.android.unio.model.association.mockAssociations
import com.android.unio.ui.navigation.BottomNavigationMenu
import com.android.unio.ui.navigation.LIST_TOP_LEVEL_DESTINATION
import com.android.unio.ui.navigation.NavigationAction
import com.android.unio.ui.navigation.Route
import com.android.unio.ui.navigation.Screen
import com.android.unio.ui.theme.AppTypography

@Composable
fun ExploreScreen(navigationAction: NavigationAction) {

  Scaffold(
      bottomBar = {
        BottomNavigationMenu(
            { navigationAction.navigateTo(it.route) }, LIST_TOP_LEVEL_DESTINATION, Route.EXPLORE)
      },
      modifier = Modifier.testTag("exploreScreen"),
      content = { padding -> ExploreScreenContent(padding, navigationAction) })
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExploreScreenContent(padding: PaddingValues, navigationAction: NavigationAction) {
  val searchQuery = remember { mutableStateOf("") }
  Column(modifier = Modifier.padding(padding)) {
    Text(
        text = "Explore our Associations",
        /** Will go in the string.xml */
        style = AppTypography.headlineLarge,
        modifier =
            Modifier.padding(vertical = 16.dp)
                .align(Alignment.CenterHorizontally)
                .testTag("exploreTitle"))

    SearchBar(
        inputField = {
          SearchBarDefaults.InputField(
              modifier = Modifier.testTag("searchBarInput"),
              query = searchQuery.value,
              onQueryChange = { searchQuery.value = it },
              onSearch = { /* Handle search here */},
              expanded = false,
              onExpandedChange = { /* Handle expanded state change here */},
              placeholder = { Text(text = "Search", style = AppTypography.bodyLarge) },
              trailingIcon = { Icon(Icons.Default.Search, contentDescription = "Search icon") },
          )
        },
        expanded = false,
        onExpandedChange = { /* Also handle expanded state change here */},
        modifier = Modifier.padding(horizontal = 26.dp, vertical = 8.dp).testTag("searchBar"),
        content = {},
    )

    LazyColumn(
        modifier = Modifier.fillMaxSize().testTag("categoriesList"),
        contentPadding = PaddingValues(vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
      MockAssociationType.entries.forEach { category ->
        val filteredAssociations = getFilteredAssociationsByCategoryAndAlphabeticalOrder(category)

        if (filteredAssociations.isNotEmpty()) {
          item {
            Text(
                text = getCategoryNameWithFirstLetterUppercase(category),
                style = AppTypography.headlineSmall,
                modifier = Modifier.padding(horizontal = 16.dp))

            // Horizontal scrollable list of associations
            LazyRow(
                modifier = Modifier.fillMaxSize().padding(vertical = 16.dp),
                contentPadding = PaddingValues(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(32.dp, Alignment.Start),
                verticalAlignment = Alignment.CenterVertically) {
                  items(filteredAssociations.size) { index ->
                    AssociationItem(filteredAssociations[index].association, navigationAction)
                  }
                }
          }
        }
      }
    }
  }
}

@Composable
fun AssociationItem(association: Association, navigationAction: NavigationAction) {
  Column(
      modifier =
          Modifier.clickable {
            navigationAction.navigateTo(
                Screen.ASSOCIATION_PROFILE +
                    "/{uid}".replace(oldValue = "{uid}", newValue = association.uid))
          }) {
        /**
         * AdEC image is used as the placeholder. Will need to add the actual image later, when the
         * actual view model is used.
         */
        Image(
            painter = painterResource(id = R.drawable.adec),
            contentDescription = "image description",
            modifier = Modifier.size(124.dp))

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = association.acronym,
            style = AppTypography.bodyMedium,
            modifier = Modifier.fillMaxWidth().align(Alignment.CenterHorizontally))
      }
}

/** Returns a list of associations filtered by the given category. */
fun getFilteredAssociationsByCategoryAndAlphabeticalOrder(
    category: MockAssociationType
): List<MockAssociation> {
  return mockAssociations().filter { it.type == category }.sortedBy { it.association.acronym }
}

/** Returns the name of the category with the first letter in uppercase. */
fun getCategoryNameWithFirstLetterUppercase(category: MockAssociationType): String {
  return category.name.lowercase().replaceFirstChar { it.uppercase() }
}
