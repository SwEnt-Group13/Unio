package com.android.unio.ui.explore

import android.content.Context
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.core.net.toUri
import com.android.unio.R
import com.android.unio.model.association.Association
import com.android.unio.model.association.AssociationCategory
import com.android.unio.model.association.AssociationViewModel
import com.android.unio.model.search.SearchViewModel
import com.android.unio.model.strings.test_tags.explore.ExploreContentTestTags
import com.android.unio.model.strings.test_tags.explore.ExploreTestTags
import com.android.unio.ui.association.AssociationSearchBar
import com.android.unio.ui.image.AsyncImageWrapper
import com.android.unio.ui.navigation.BottomNavigationMenu
import com.android.unio.ui.navigation.LIST_TOP_LEVEL_DESTINATION
import com.android.unio.ui.navigation.NavigationAction
import com.android.unio.ui.navigation.Route
import com.android.unio.ui.navigation.Screen
import com.android.unio.ui.theme.AppTypography

/**
 * The Explore screen displays a list of associations grouped by category. This simply displays the
 * ExploreScreenContent and the BottomNavigationBar with a scaffold.
 *
 * @param navigationAction The navigation action to use when an association is clicked.
 * @param associationViewModel The [AssociationViewModel] to use.
 * @param searchViewModel The [SearchViewModel] to use.
 */
@Composable
fun ExploreScreen(
    navigationAction: NavigationAction,
    associationViewModel: AssociationViewModel,
    searchViewModel: SearchViewModel
) {

  Scaffold(
      bottomBar = {
        BottomNavigationMenu(
            { navigationAction.navigateTo(it.route) }, LIST_TOP_LEVEL_DESTINATION, Route.EXPLORE)
      },
      modifier = Modifier.testTag(ExploreTestTags.EXPLORE_SCAFFOLD_TITLE),
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
@Composable
fun ExploreScreenContent(
    padding: PaddingValues,
    navigationAction: NavigationAction,
    associationViewModel: AssociationViewModel,
    searchViewModel: SearchViewModel
) {
  val associationsByCategory by associationViewModel.associationsByCategory.collectAsState()
  val context = LocalContext.current
  var shouldCloseExpandable by rememberSaveable { mutableStateOf(false) }

  Column(
      modifier =
          Modifier.padding(padding).fillMaxWidth().pointerInput(shouldCloseExpandable) {
            detectTapGestures {
              // Collapse the component when clicking outside
              shouldCloseExpandable = true
            }
          },
      horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = context.getString(R.string.explore_content_screen_title),
            style = AppTypography.headlineLarge,
            modifier =
                Modifier.padding(top = 16.dp, bottom = 8.dp)
                    .align(Alignment.CenterHorizontally)
                    .testTag(ExploreContentTestTags.TITLE_TEXT))

        AssociationSearchBar(
            searchViewModel = searchViewModel,
            onAssociationSelected = { association ->
              associationViewModel.selectAssociation(association.uid)
              navigationAction.navigateTo(Screen.ASSOCIATION_PROFILE)
            },
            shouldCloseExpandable,
            onOutsideClickHandled = { shouldCloseExpandable = false })

        LazyColumn(
            modifier = Modifier.fillMaxSize().testTag(ExploreContentTestTags.CATEGORIES_LIST),
            contentPadding = PaddingValues(vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
          getSortedEntriesAssociationsByCategory(context, associationsByCategory).forEach {
              (category, associations) ->
            val alphabeticalAssociations = getFilteredAssociationsByAlphabeticalOrder(associations)

            if (alphabeticalAssociations.isNotEmpty()) {
              item {
                Text(
                    text = context.getString(category.displayNameId),
                    style = AppTypography.headlineSmall,
                    modifier =
                        Modifier.padding(horizontal = 16.dp)
                            .testTag(ExploreContentTestTags.CATEGORY_NAME + category.name))

                // Horizontal scrollable list of associations
                LazyRow(
                    modifier =
                        Modifier.fillMaxSize()
                            .padding(vertical = 16.dp)
                            .testTag(ExploreContentTestTags.ASSOCIATION_ROW + category.name),
                    contentPadding = PaddingValues(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(32.dp, Alignment.Start),
                    verticalAlignment = Alignment.CenterVertically) {
                      items(alphabeticalAssociations.size) { index ->
                        AssociationItem(alphabeticalAssociations[index]) {
                          associationViewModel.selectAssociation(
                              alphabeticalAssociations[index].uid)
                          navigationAction.navigateTo(Screen.ASSOCIATION_PROFILE)
                        }
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
 * @param onClick The action to perform when the item is clicked.
 */
@Composable
fun AssociationItem(association: Association, onClick: () -> Unit) {
  val context = LocalContext.current
  Column(
      modifier =
          Modifier.clickable(onClick = onClick)
              .testTag(ExploreContentTestTags.ASSOCIATION_ITEM + association.name)) {
        /**
         * AdEC image is used as the placeholder. Will need to add real logos to Firebase Storage.
         */
        AsyncImageWrapper(
            imageUri = association.image.toUri(),
            contentDescription = context.getString(R.string.explore_content_description_image),
            modifier = Modifier.size(124.dp).clip(RoundedCornerShape(12.dp)),
            placeholderResourceId = R.drawable.association_logo_placeholder,
            contentScale = ContentScale.Crop)

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = association.name,
            style = AppTypography.bodyMedium,
            modifier =
                Modifier.fillMaxWidth()
                    .align(Alignment.CenterHorizontally)
                    .testTag(ExploreContentTestTags.ASSOCIATION_NAME_TEXT + association.name))
      }
}

/**
 * Returns a list of associations sorted by alphabetical order. This method should probably be
 * removed as it does not sufficiently abstract the sorting logic.
 *
 * @param associations The list of associations to sort.
 * @return The sorted list of associations.
 */
fun getFilteredAssociationsByAlphabeticalOrder(associations: List<Association>): List<Association> {
  return associations.sortedBy { it.name }
}

/**
 * Returns the entries of the association map sorted by the key's display name.
 *
 * @param context The context to use for string resources.
 * @param associationsByCategory The map of associations by category.
 */
fun getSortedEntriesAssociationsByCategory(
    context: Context,
    associationsByCategory: Map<AssociationCategory, List<Association>>
): List<Map.Entry<AssociationCategory, List<Association>>> {
  return associationsByCategory.entries.sortedBy { context.getString(it.key.displayNameId) }
}
