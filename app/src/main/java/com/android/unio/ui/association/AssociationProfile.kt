package com.android.unio.ui.association

import android.annotation.SuppressLint
import android.util.Log
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.lifecycle.viewmodel.compose.viewModel
import com.android.unio.model.association.AssociationViewModel
import com.android.unio.ui.navigation.NavigationAction
import com.android.unio.ui.theme.AppTypography

@OptIn(ExperimentalMaterial3Api::class)
@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@Composable
fun AssociationProfile(
    navigationAction: NavigationAction,
    associationId: String,
    associationViewModel: AssociationViewModel = viewModel(factory = AssociationViewModel.Factory)
) {
  val association =
      associationViewModel.findAssociationById(associationId)
          ?: run {
            Log.e("AssociationProfile", "Association not found")
            return AssociationProfileScaffold(
                title = "Association Profile", navigationAction = navigationAction) {
                  Text(
                      text = "Association not found. Shouldn't happen.",
                      modifier = Modifier.testTag("associationNotFound"),
                      color = Color.Red)
                }
          }

  AssociationProfileScaffold(title = "Association Profile", navigationAction = navigationAction) {
    Text("Association acronym: ${association.acronym}", style = AppTypography.bodyMedium, modifier = Modifier.testTag("associationAcronym"))
  }
}

@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AssociationProfileScaffold(
    title: String,
    navigationAction: NavigationAction,
    content: @Composable () -> Unit
) {
  Scaffold(
      topBar = {
        TopAppBar(
            title = { Text(title, modifier = Modifier.testTag("AssociationProfileTitle")) },
            navigationIcon = {
              IconButton(
                  onClick = { navigationAction.goBack() },
                  modifier = Modifier.testTag("goBackButton")) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Outlined.ArrowBack,
                        contentDescription = "Go back")
                  }
            })
      },
      modifier = Modifier.testTag("AssociationProfileScreen")) {
        content()
      }
}
