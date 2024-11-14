package com.android.unio.ui.user

import android.widget.Toast
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.outlined.MoreVert
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import com.android.unio.R
import com.android.unio.model.association.AssociationViewModel
import com.android.unio.model.search.SearchViewModel
import com.android.unio.ui.association.AssociationSearchBar
import com.android.unio.ui.navigation.NavigationAction
import com.android.unio.ui.navigation.Screen
import com.android.unio.ui.theme.AppTypography

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UserClaimAssociationScreen(
    associationViewModel: AssociationViewModel,
    navigationAction: NavigationAction,
    searchViewModel: SearchViewModel
) {
  val context = LocalContext.current
  Scaffold(
      topBar = {
        TopAppBar(
            title = {
              Text(text = "Go Back", modifier = Modifier.testTag("AssociationProfileTitle"))
            },
            navigationIcon = {
              IconButton(
                  onClick = { navigationAction.goBack() },
                  modifier = Modifier.testTag("goBackButton")) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Outlined.ArrowBack,
                        contentDescription = context.getString(R.string.association_go_back))
                  }
            },
            actions = {
              Row {
                IconButton(onClick = {}) {
                  Icon(
                      Icons.Outlined.MoreVert,
                      contentDescription = context.getString(R.string.association_see_more))
                }
              }
            })
      },
      content = { padding ->
        Surface(
            modifier = Modifier.padding(padding),
        ) {
          Column(modifier = Modifier.padding(16.dp)) {
            Text("You can either", style = AppTypography.headlineSmall)
            Spacer(modifier = Modifier.height(6.dp))

            Text("Create a new association", style = AppTypography.bodySmall)
            Spacer(modifier = Modifier.height(6.dp))

            Button(
                onClick = {
                  Toast.makeText(context, "Not implemented yet", Toast.LENGTH_SHORT).show()
                },
                modifier = Modifier.testTag("createNewAssociationButton")) {
                  Text("Create association")
                }
            Spacer(modifier = Modifier.height(6.dp))

            Text(
                "Claim president rights for an existing association",
                style = AppTypography.bodySmall)
            Spacer(modifier = Modifier.height(6.dp))

            AssociationSearchBar(
                searchViewModel = searchViewModel,
                onAssociationSelected = { association ->
                  associationViewModel.selectAssociation(association.uid)
                  navigationAction.navigateTo(Screen.CLAIM_ASSOCIATION_PRESIDENTIAL_RIGHTS)
                },
                modifier = Modifier)
          }
        }
      })
}
