package com.android.unio.ui.association

import android.content.Context
import android.util.Log
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
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.core.net.toUri
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.android.unio.R
import com.android.unio.model.association.Association
import com.android.unio.model.association.AssociationViewModel
import com.android.unio.model.event.Event
import com.android.unio.model.event.EventCard
import com.android.unio.model.user.User
import com.android.unio.resources.ResourceManager.getString
import com.android.unio.resources.ResourceManager.init
import com.android.unio.ui.navigation.NavigationAction
import com.android.unio.ui.theme.AppTypography
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

// These variable are only here for testing purpose. They should be deleted when the screen is
// linked to the backend
private val DEBUG_MESSAGE = "<DEBUG> Not implemented yet"

private var testSnackbar: SnackbarHostState? = null
private var scope: CoroutineScope? = null

@Composable
fun AssociationProfileScreen(
    navigationAction: NavigationAction,
    associationId: String,
    associationViewModel: AssociationViewModel = viewModel(factory = AssociationViewModel.Factory)
) {
  val association = associationViewModel.findAssociationById(associationId)
  if (association == null) {
    val error = getString(R.string.association_not_found)
    Log.e("AssociationProfileScreen", error)
    AssociationProfileScaffold(
        association = null,
        navigationAction = navigationAction) { padding ->
          Column(modifier = Modifier.padding(padding)) {
            Text(
                text = error, modifier = Modifier.testTag("associationNotFound"), color = Color.Red)
          }
        }
  } else {
    AssociationProfileScaffold(
        association = association,
        navigationAction = navigationAction) { padding ->
          AssociationProfileContent(
              padding, association, associationViewModel)
        }
  }
}

/**
 * Composable element that contain the scaffold of the given association profile screen.
 * Precisely, it contains the top bar, the content given in parameter and the snackbar host used
 * on unimplemented features.
 *
 * @param association (Association) : The association to display
 * @param navigationAction (NavigationAction) : The navigation actions of the screen
 * @param content (Composable) : The content of the screen
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AssociationProfileScaffold(
    association: Association?,
    navigationAction: NavigationAction,
    content: @Composable (padding: PaddingValues) -> Unit
) {
  val context = LocalContext.current
  testSnackbar = remember { SnackbarHostState() }
  scope = rememberCoroutineScope()
  init(context)
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
            title = {
              val title: String
              if (association != null) {
                title = association.fullName
              } else {
                title = getString(R.string.association_not_found)
              }
              Text(text = title, modifier = Modifier.testTag("associationTitle"))
            },
            navigationIcon = {
              IconButton(
                  onClick = { navigationAction.goBack() },
                  modifier = Modifier.testTag("goBackButton")) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Outlined.ArrowBack,
                        contentDescription = getString(R.string.association_go_back))
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
      content = { padding -> content(padding) })
}

/**
 * Composable element that contain the content of the given association profile screen. It call all elements
 * that should be displayed on the screen, such as the header, the description, the events, (...)
 * separated by spacers.
 *
 * @param padding (PaddingValues) : The padding of the screen
 */
@Composable
fun AssociationProfileContent(
    padding: PaddingValues,
    association: Association,
    associationViewModel: AssociationViewModel
) {
  Column(
      modifier =
          Modifier.padding(padding)
              .testTag("AssociationScreen")
              .verticalScroll(rememberScrollState())) {
        AssociationHeader(association)
        Spacer(modifier = Modifier.size(22.dp))
        AssociationDescription(association)
        Spacer(modifier = Modifier.size(15.dp))
        AssociationEventTitle()
        Spacer(modifier = Modifier.size(11.dp))
        AssociationProfileEvents(association, associationViewModel)
        Spacer(modifier = Modifier.size(11.dp))
        UsersCard(association.members.list.collectAsState().value)
        Spacer(modifier = Modifier.size(61.dp))
        AssociationRecruitment(association)
      }
}

@Composable
fun AssociationRecruitment(association: Association) {
  Text(
      text = getString(R.string.association_join) + " ${association.fullName} ?",
      style = AppTypography.headlineMedium,
      modifier = Modifier.padding(horizontal = 20.dp).testTag("AssociationRecruitmentTitle"))
  Spacer(modifier = Modifier.size(13.dp))
  Text(
      text = getString(R.string.association_help_us),
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
fun UsersCard(userList: List<User>) {
  Text(
      getString(R.string.association_contact_members),
      style = AppTypography.headlineMedium,
      modifier = Modifier.padding(horizontal = 20.dp).testTag("AssociationContactMembersTitle"))
  Spacer(modifier = Modifier.size(4.dp))
  userList.forEach { user ->
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
        Icon(
            Icons.Filled.Person,
            contentDescription = "user's profile picture",
            Modifier.size(36.dp))
        Text(text = user.firstName + " " + user.lastName, style = AppTypography.headlineSmall)
      }
    }
  }
}

@Composable
fun AssociationProfileEvents(association: Association, associationViewModel: AssociationViewModel) {
  var events = emptyList<Event>()
  associationViewModel.getEventsForAssociation(association) { fetchedEvents ->
    events = fetchedEvents
  }
  Column(
      modifier = Modifier.padding(horizontal = 28.dp),
      horizontalAlignment = Alignment.CenterHorizontally) {
        events.forEach { event ->
          Box(modifier = Modifier.testTag("AssociationEventCard${event.uid}")) {
            EventCard(
                event =
                    Event(
                        organisers = event.organisers,
                        taggedAssociations = event.taggedAssociations)) {}
          }
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
              Text(getString(R.string.association_see_more))
            }
      }
}

@Composable
fun AssociationEventTitle() {
  Text(
      getString(R.string.association_upcoming_events),
      modifier = Modifier.padding(horizontal = 20.dp).testTag("AssociationEventTitle"),
      style = AppTypography.headlineMedium)
}

@Composable
fun AssociationDescription(association: Association) {
  Text(
      association.description,
      style = AppTypography.bodyMedium,
      modifier = Modifier.padding(horizontal = 24.dp).testTag("AssociationDescription"))
}

@Composable
fun AssociationHeader(association: Association) {
  Row {
    Box(modifier = Modifier.padding(horizontal = 24.dp).testTag("AssociationImageHeader")) {
      AsyncImage(
          model = association.image.toUri(),
          contentDescription = "Association image of " + association.name,
          modifier = Modifier.size(124.dp))
    }
    Column {
      Text(
          "xxx " + getString(R.string.association_follower),
          style = AppTypography.headlineSmall,
          modifier = Modifier.padding(bottom = 5.dp).testTag("AssociationHeaderFollowers"))
      Text(
          "${association.members.list.collectAsState().value.size} " +
              getString(R.string.association_member),
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
            Text(getString(R.string.association_follow))
          }
    }
  }
}
