package com.android.unio.ui.association

import android.annotation.SuppressLint
import android.content.Context
import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.outlined.Share
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.android.unio.R
import com.android.unio.model.association.AssociationViewModel
import com.android.unio.model.event.Event
import com.android.unio.model.event.EventCard
import com.android.unio.model.firestore.MockReferenceList
import com.android.unio.ui.navigation.NavigationAction
import com.android.unio.ui.theme.AppTypography
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

// These variable are only here for testing purpose. They should be deleted when the screen is
// linked to the backend
private val DEBUG_MESSAGE = "<DEBUG> Not implemented yet"

private var testSnackbar: SnackbarHostState? = null
private var scope: CoroutineScope? = null

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AssociationProfileScreen(
    navigationAction: NavigationAction,
    associationId: String,
    associationViewModel: AssociationViewModel = viewModel(factory = AssociationViewModel.Factory)
) {
  val association =
      associationViewModel.findAssociationById(associationId)
          ?: run {
            Log.e("AssociationProfile", "Association not found")
            return AssociationProfileScaffold(
                title = "<Association Profile>", navigationAction = navigationAction) { padding ->
                  Column(modifier = Modifier.padding(padding)) {
                    Text(
                        text = "Association not found. Shouldn't happen.",
                        modifier = Modifier.testTag("associationNotFound"),
                        color = Color.Red)
                  }
                }
          }

  AssociationProfileScaffold(
      title = "<Association Profile>", navigationAction = navigationAction) { padding ->
        Column(modifier = Modifier.padding(padding)) {
          Text(
              "Association name: ${association.name}",
              style = AppTypography.bodyMedium,
              modifier = Modifier.testTag("associationName"))
        }
      }
}

/**
 * The scaffold for the Association Profile screen.
 *
 * @param title The title of the screen.
 * @param navigationAction The navigation action to use when the back button is clicked.
 * @param content The content of the screen.
 */
@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter", "RememberReturnType")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AssociationProfileScaffold(
    title: String,
    navigationAction: NavigationAction,
    content: @Composable (padding: PaddingValues) -> Unit
) {
  val context = LocalContext.current
  testSnackbar = remember { SnackbarHostState() }
  scope = rememberCoroutineScope()
  Scaffold(
      snackbarHost = {
        SnackbarHost(
            hostState = testSnackbar!!,
            modifier = Modifier.testTag("associationSnackbarHost"),
            snackbar = { data ->
              Snackbar {
                TextButton(
                    onClick = { testSnackbar!!.currentSnackbarData?.dismiss() },
                    modifier = Modifier.testTag("snackbarActionButton")) {
                      Text(text = DEBUG_MESSAGE)
                    }
              }
            })
      },
      topBar = {
        TopAppBar(
            title = { Text(title, modifier = Modifier.testTag("AssociationProfileTitle")) },
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
              IconButton(
                  modifier = Modifier.testTag("associationShareButton"),
                  onClick = {
                    scope!!.launch {
                      testSnackbar!!.showSnackbar(
                          message = DEBUG_MESSAGE, duration = SnackbarDuration.Short)
                    }
                  }) {
                    Icon(Icons.Outlined.Share, contentDescription = "Icon for sharing association")
                  }
            })
      },
      content = { padding -> AssociationProfileContent(padding, context) })
}

@Composable
fun AssociationProfileContent(padding: PaddingValues, context: Context) {
  Column(
      modifier =
          Modifier.padding(padding)
              .testTag("AssociationScreen")
              .verticalScroll(rememberScrollState())) {
        AssociationHeader(context)
        Spacer(modifier = Modifier.size(22.dp))
        AssociationDescription(context)
        Spacer(modifier = Modifier.size(15.dp))
        AssociationEventTitle(context)
        Spacer(modifier = Modifier.size(11.dp))
        AssociationProfileEvents(context)
        Spacer(modifier = Modifier.size(11.dp))
        UserCard(context)
        Spacer(modifier = Modifier.size(61.dp))
        AssociationRecruitment(context)
      }
}

@Composable
fun AssociationRecruitment(context: Context) {
  Text(
      text = context.getString(R.string.association_join) + " <Association> ?",
      style = AppTypography.headlineMedium,
      modifier = Modifier.padding(horizontal = 20.dp).testTag("AssociationRecruitmentTitle"))
  Spacer(modifier = Modifier.size(13.dp))
  Text(
      text = context.getString(R.string.association_help_us),
      style = AppTypography.bodySmall,
      modifier = Modifier.padding(horizontal = 23.dp).testTag("AssociationRecruitmentDescription"))
  Spacer(modifier = Modifier.size(18.dp))
  Row(modifier = Modifier.padding(horizontal = 24.dp).testTag("AssociationRecruitmentRoles")) {
    OutlinedButton(
        modifier = Modifier.testTag("AssociationDesignerRoles"),
        onClick = {
          scope!!.launch {
            testSnackbar!!.showSnackbar(message = DEBUG_MESSAGE, duration = SnackbarDuration.Short)
          }
        },
        enabled = true) {
          Icon(Icons.Filled.Add, contentDescription = "Add icon")
          Spacer(Modifier.width(2.dp))
          Text("<Graphic Designer>")
        }
    Spacer(modifier = Modifier.width(10.dp))
    OutlinedButton(
        modifier = Modifier.testTag("AssociationTreasurerRoles"),
        onClick = {
          scope!!.launch {
            testSnackbar!!.showSnackbar(message = DEBUG_MESSAGE, duration = SnackbarDuration.Short)
          }
        },
        enabled = true) {
          Icon(Icons.Filled.Add, contentDescription = "Add icon")
          Spacer(Modifier.width(2.dp))
          Text("<Treasurer>")
        }
  }
}

@Composable
fun UserCard(context: Context) {
  Text(
      context.getString(R.string.association_contact_members),
      style = AppTypography.headlineMedium,
      modifier = Modifier.padding(horizontal = 20.dp).testTag("AssociationContactMembersTitle"))
  Spacer(modifier = Modifier.size(4.dp))
  Box(
      modifier =
          Modifier.testTag("AssociationContactMembersCard")
              .padding(horizontal = 23.dp)
              .width(366.dp)
              .height(40.dp)
              .background(Color.LightGray, RoundedCornerShape(12.dp))
              .padding(vertical = 2.dp, horizontal = 3.dp)
              .clickable {
                scope!!.launch {
                  testSnackbar!!.showSnackbar(
                      message = DEBUG_MESSAGE, duration = SnackbarDuration.Short)
                }
              },
  ) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(115.dp, Alignment.Start),
        verticalAlignment = Alignment.CenterVertically,
    ) {
      Icon(Icons.Filled.Person, contentDescription = "user's profile picture", Modifier.size(36.dp))
      Text(text = "<Casey Rue>", style = AppTypography.headlineSmall)
    }
  }
}

@Composable
fun AssociationProfileEvents(context: Context) {
  Column(
      modifier = Modifier.padding(horizontal = 28.dp),
      horizontalAlignment = Alignment.CenterHorizontally) {
        Box(modifier = Modifier.testTag("AssociationEventCard")) {
          EventCard(
              event =
                  Event(
                      organisers = MockReferenceList(),
                      taggedAssociations = MockReferenceList())) {}
        }
        Spacer(modifier = Modifier.size(11.dp))
        OutlinedButton(
            onClick = {
              scope!!.launch {
                testSnackbar!!.showSnackbar(
                    message = DEBUG_MESSAGE, duration = SnackbarDuration.Short)
              }
            },
            modifier = Modifier.padding(horizontal = 28.dp).testTag("AssociationSeeMoreButton")) {
              Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = "See more")
              Spacer(Modifier.width(2.dp))
              Text(context.getString(R.string.association_see_more))
            }
      }
}

@Composable
fun AssociationEventTitle(context: Context) {

  Text(
      context.getString(R.string.association_upcoming_events),
      modifier = Modifier.padding(horizontal = 20.dp).testTag("AssociationEventTitle"),
      style = AppTypography.headlineMedium)
}

@Composable
fun AssociationDescription(context: Context) {
  Text(
      context.getString(R.string.debug_lorem_ipsum),
      style = AppTypography.bodyMedium,
      modifier = Modifier.padding(horizontal = 24.dp).testTag("AssociationDescription"))
}

@Composable
fun AssociationHeader(context: Context) {
  Row {
    Box(modifier = Modifier.padding(horizontal = 24.dp).testTag("AssociationImageHeader")) {
      Image(
          painter = painterResource(id = R.drawable.adec),
          contentDescription = "placeholder",
          modifier = Modifier.size(124.dp))
    }
    Column {
      Text(
          "xxx " + context.getString(R.string.association_follower),
          style = AppTypography.headlineSmall,
          modifier = Modifier.padding(bottom = 5.dp).testTag("AssociationHeaderFollowers"))
      Text(
          "yyy " + context.getString(R.string.association_member),
          style = AppTypography.headlineSmall,
          modifier = Modifier.padding(bottom = 14.dp).testTag("AssociationHeaderMembers"))
      Button(
          onClick = {
            scope!!.launch {
              testSnackbar!!.showSnackbar(
                  message = DEBUG_MESSAGE, duration = SnackbarDuration.Short)
            }
          },
          modifier = Modifier.testTag("AssociationFollowButton")) {
            Icon(Icons.Filled.Add, contentDescription = "Follow icon")
            Spacer(Modifier.width(2.dp))
            Text(context.getString(R.string.association_follow))
          }
    }
  }
}
