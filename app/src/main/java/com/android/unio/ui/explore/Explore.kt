package com.android.unio.ui.explore

import androidx.compose.foundation.background
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.android.unio.model.association.Association
import com.android.unio.model.association.AssociationType
import com.android.unio.model.association.MockAssociation
import com.android.unio.model.association.mockAssociations
import com.android.unio.ui.navigation.BottomNavigationMenu
import com.android.unio.ui.navigation.LIST_TOP_LEVEL_DESTINATION
import com.android.unio.ui.navigation.NavigationAction
import com.android.unio.ui.navigation.TopLevelDestinations

@Composable
fun ExploreScreen(navigationAction: NavigationAction) {

  Scaffold(
      bottomBar = {
        BottomNavigationMenu(
            { navigationAction.navigateTo(it.route) },
            LIST_TOP_LEVEL_DESTINATION,
            TopLevelDestinations.EXPLORE.route)
      },
      modifier = Modifier.testTag("exploreScreen"),
      content = { padding -> ExploreScreenContent(padding) })
}

@Composable
fun ExploreScreenContent(padding: PaddingValues) {
  // This is a placeholder for the actual content of the Explore screen
  Column(modifier = Modifier.fillMaxSize().padding(padding)) {
    OutlinedTextField(
        value = "",
        onValueChange = {},
        modifier = Modifier.fillMaxWidth().padding(8.dp).testTag("searchBar"),
        placeholder = { Text("Search") },
        trailingIcon = { Icon(Icons.Default.Search, contentDescription = "Search icon") })

    LazyColumn(
        modifier = Modifier.fillMaxSize(), verticalArrangement = Arrangement.spacedBy(16.dp)) {
          AssociationType.entries.forEach { category ->
            val filteredAssociations =
                getFilteredAssociationsByCategoryAndAlphabeticalOrder(category)

            if (filteredAssociations.isNotEmpty()) {
              item {
                Text(
                    text = getCategoryNameWithFirstLetterUppercase(category),
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(horizontal = 16.dp).testTag("categoryTitle"))

                // Horizontal scrollable list of associations
                LazyRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                      items(filteredAssociations.size) { index ->
                        AssociationItem(filteredAssociations[index].association)
                      }
                    }
              }
            }
          }
        }
  }
}

@Composable
fun AssociationItem(association: Association) {
  Column(
      modifier = Modifier.padding(16.dp).width(80.dp)
      // Interaction (to see detailed screen about an association) can be defined here,
      // with the .clickable modifier
      ) {
        /**
         * Placeholder for the image later on when we find a way to get and store them, for now just
         * a gray box
         */
        Box(
            modifier =
                Modifier.size(64.dp).background(Color.LightGray, shape = RoundedCornerShape(8.dp)))

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = association.acronym,
            fontWeight = FontWeight.Normal,
            modifier = Modifier.fillMaxWidth(),
            maxLines = 1)
      }
}

/** Returns a list of associations filtered by the given category. */
fun getFilteredAssociationsByCategoryAndAlphabeticalOrder(
    category: AssociationType
): List<MockAssociation> {
  return mockAssociations.filter { it.type == category }.sortedBy { it.association.acronym }
}

/** Returns the name of the category with the first letter in uppercase. */
fun getCategoryNameWithFirstLetterUppercase(category: AssociationType): String {
  return category.name.lowercase().replaceFirstChar { it.uppercase() }
}
