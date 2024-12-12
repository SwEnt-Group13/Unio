package com.android.unio.ui.user

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import com.android.unio.R
import com.android.unio.model.association.AssociationViewModel
import com.android.unio.model.search.SearchViewModel
import com.android.unio.model.strings.test_tags.user.UserClaimAssociationTestTags
import com.android.unio.ui.association.AssociationSearchBar
import com.android.unio.ui.navigation.NavigationAction
import com.android.unio.ui.navigation.Screen
import com.android.unio.ui.theme.AppTypography

/**
 * Composable allows the user to search for an association and one.
 *
 * @param associationViewModel The [AssociationViewModel] for the association.
 * @param navigationAction The [NavigationAction] to navigate to different screens.
 * @param searchViewModel The [SearchViewModel] for the search.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UserClaimAssociationScreen(
    associationViewModel: AssociationViewModel,
    navigationAction: NavigationAction,
    searchViewModel: SearchViewModel
) {
  val context = LocalContext.current

  Scaffold(
      modifier = Modifier.testTag(UserClaimAssociationTestTags.SCREEN),
      topBar = {
        TopAppBar(
            title = {},
            navigationIcon = {
              IconButton(
                  onClick = { navigationAction.goBack() },
                  modifier = Modifier.testTag(UserClaimAssociationTestTags.BACK_BUTTON)) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Outlined.ArrowBack,
                        contentDescription =
                            context.getString(R.string.user_claim_association_association_go_back))
                  }
            })
      },
      content = { padding ->
        Surface(
            modifier = Modifier.padding(padding),
        ) {
          Column(
              modifier = Modifier.padding(16.dp),
              verticalArrangement = Arrangement.spacedBy(12.dp),
              horizontalAlignment = Alignment.CenterHorizontally,
          ) {
            Text(
                context.getString(R.string.user_claim_association_you_can_do_the_following),
                style = AppTypography.headlineSmall)

            Text(
                context.getString(R.string.user_claim_association_claim_president_rights),
                style = AppTypography.bodySmall)

            val focusRequester = remember { FocusRequester() }

            LaunchedEffect(Unit) { focusRequester.requestFocus() }

            Box(modifier = Modifier.focusRequester(focusRequester)) {
              AssociationSearchBar(
                  searchViewModel = searchViewModel,
                  onAssociationSelected = { association ->
                    associationViewModel.selectAssociation(association.uid)
                    navigationAction.navigateTo(Screen.CLAIM_ASSOCIATION_PRESIDENTIAL_RIGHTS)
                  },
                  false,
                  {})
            }
          }
        }
      })
}
