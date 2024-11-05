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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.unit.dp
import androidx.core.net.toUri
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.android.unio.R
import com.android.unio.model.association.Association
import com.android.unio.model.association.AssociationViewModel
import com.android.unio.model.event.Event
import com.android.unio.model.user.User
import com.android.unio.model.user.UserViewModel
import com.android.unio.ui.event.EventCard
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
    associationViewModel: AssociationViewModel = viewModel(factory = AssociationViewModel.Factory),
    userViewModel: UserViewModel
) {
  val context = LocalContext.current
  val association = associationViewModel.findAssociationById(associationId)
  if (association == null) {
    val error = context.getString(R.string.association_not_found)
    Log.e("AssociationProfileScreen", error)
    AssociationProfileScaffold(association = null, navigationAction = navigationAction) { padding ->
      Column(modifier = Modifier.padding(padding)) {
        Text(text = error, modifier = Modifier.testTag("associationNotFound"), color = Color.Red)
      }
    }
  } else {
    AssociationProfileScaffold(association = association, navigationAction = navigationAction) {
        padding ->
      AssociationProfileContent(navigationAction,padding, association, associationViewModel, userViewModel, context)
    }
  }
}

/**
 * Composable element that contain the scaffold of the given association profile screen. Precisely,
 * it contains the top bar, the content given in parameter and the snackbar host used on
 * unimplemented features.
 *
 * @param association (Association) : The association to display
 * @param navigationAction (NavigationAction) : The navigation actions of the screen
 * @param content (Composable) : The content of the screen
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AssociationProfileScaffold(
    association: Association?,
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
            title = {
              val title: String
              if (association != null) {
                title = association.name
              } else {
                title = context.getString(R.string.association_not_found)
              }
              Text(text = title, modifier = Modifier.testTag("AssociationProfileTitle"))
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
 * Composable element that contain the content of the given association profile screen. It call all
 * elements that should be displayed on the screen, such as the header, the description, the events,
 * (...) separated by spacers.
 *
 * @param padding (PaddingValues) : The padding of the screen
 * @param association (Association) : The association to display
 * @param associationViewModel (AssociationViewModel) : The associations view model
 * @param context (Context) : The context of the screen
 */
@Composable
private fun AssociationProfileContent(
    navigationAction: NavigationAction,
    padding: PaddingValues,
    association: Association,
    associationViewModel: AssociationViewModel,
    userViewModel: UserViewModel,
    context: Context
) {
  Column(
      modifier =
          Modifier.padding(padding)
              .testTag("AssociationScreen")
              .verticalScroll(rememberScrollState())) {
        AssociationHeader(association, context)
        Spacer(modifier = Modifier.size(22.dp))
        AssociationDescription(association)
        Spacer(modifier = Modifier.size(15.dp))
        AssociationEventTitle(context)
        Spacer(modifier = Modifier.size(11.dp))
        AssociationProfileEvents(
            navigationAction, association, associationViewModel, userViewModel = userViewModel, context)
        Spacer(modifier = Modifier.size(11.dp))
        UsersCard(association.members.list.collectAsState().value, context)
        Spacer(modifier = Modifier.size(61.dp))
        AssociationRecruitment(association, context)
      }
}

/**
 * Composable element that contain the recruitment part of the association profile screen. It
 * display the recruitment title, the recruitment description, the roles that are needed and the
 * users that are already in the association.
 *
 * !!! This element is only a placeholder and should be replaced by the real recruitment system when
 * implemented !!!
 *
 * @param association (Association) : The association currently displayed
 * @param context (Context) : The context of the screen
 */
@Composable
private fun AssociationRecruitment(association: Association, context: Context) {
  Text(
      text = context.getString(R.string.association_join) + " ${association.name} ?",
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

/**
 * Component that display the users that are in the association that can be contacted. It display
 * the title of the section and then display the different users in the association.
 *
 * @param userList (List<User>) : The list of users in the association that can be contacted
 * @param context (Context) : The context of the screen
 */
@Composable
private fun UsersCard(userList: List<User>, context: Context) {
  Text(
      context.getString(R.string.association_contact_members),
      style = AppTypography.headlineMedium,
      modifier = Modifier.padding(horizontal = 20.dp).testTag("AssociationContactMembersTitle"))
  Spacer(modifier = Modifier.size(4.dp))
  userList.forEach { user ->
    Box(
        modifier =
            Modifier.padding(horizontal = 23.dp)
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
        AsyncImage(
            user.profilePicture.toUri(),
            contentDescription = "user's profile picture",
            Modifier.size(36.dp))
        Text(text = user.firstName + " " + user.lastName, style = AppTypography.headlineSmall)
      }
    }
  }
}

/**
 * Component that display all the events of the association in a card format, like in the home
 * screen.
 *
 * @param association (Association) : The association currently displayed
 * @param associationViewModel (AssociationViewModel) : The associations view model
 * @param context (Context) : The context of the screen
 */
@Composable
private fun AssociationProfileEvents(
    navigationAction: NavigationAction,
    association: Association,
    associationViewModel: AssociationViewModel,
    userViewModel: UserViewModel,
    context: Context
) {
  var isSeeMoreClicked by remember { mutableStateOf(false) }
  var events = emptyList<Event>()
  associationViewModel.getEventsForAssociation(association) { fetchedEvents ->
    events = fetchedEvents
  }
  if (events.isEmpty()) {
    Text(
        text = context.getString(R.string.association_no_event),
        style = AppTypography.bodySmall,
        fontStyle = FontStyle.Italic,
        modifier = Modifier.padding(horizontal = 20.dp).testTag("AssociationNoEvent"))
  } else {
    events.sortedBy { it.date }
    val first = events.first()
    Column(
        modifier = Modifier.padding(horizontal = 28.dp),
        horizontalAlignment = Alignment.CenterHorizontally) {
          if (isSeeMoreClicked) {
            events.forEach { event -> AssociationEventCard(navigationAction, event, userViewModel) }
          } else {
            AssociationEventCard(navigationAction, first, userViewModel)
          }
        }
    Spacer(modifier = Modifier.size(11.dp))
    OutlinedButton(
        onClick = { isSeeMoreClicked = true },
        modifier = Modifier.padding(horizontal = 28.dp).testTag("AssociationSeeMoreButton")) {
          Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = "See more")
          Spacer(Modifier.width(2.dp))
          Text(context.getString(R.string.association_see_more))
        }
  }
}

/**
 * Component that display only one event in a card format, like in the home screen.
 *
 * @param event (Event) : The event to display
 */
@Composable
private fun AssociationEventCard(navigationAction: NavigationAction, event: Event,userViewModel: UserViewModel) {
  Box(modifier = Modifier.testTag("AssociationEventCard-${event.uid}")) {
    EventCard(
        navigationAction = navigationAction,
        event = Event(organisers = event.organisers, taggedAssociations = event.taggedAssociations),
        userViewModel = userViewModel)
  }
}

/**
 * Component that introduce the upcoming events of the association.
 *
 * @param context (Context) : The context of the screen
 */
@Composable
private fun AssociationEventTitle(context: Context) {
  Text(
      context.getString(R.string.association_upcoming_events),
      modifier = Modifier.padding(horizontal = 20.dp).testTag("AssociationEventTitle"),
      style = AppTypography.headlineMedium)
}

/**
 * Component that display the description of the association.
 *
 * @param association (Association) : The association currently displayed
 */
@Composable
private fun AssociationDescription(association: Association) {
  Text(
      association.description,
      style = AppTypography.bodyMedium,
      modifier = Modifier.padding(horizontal = 24.dp).testTag("AssociationDescription"))
}

/**
 * Component that display the header of the association profile screen. It display the image of the
 * association, the number of followers and the number of members. It also display a button to
 * follow the association.
 *
 * !!! The follow button do not have any action and should be implemented when follow feature is
 * implemented !!!
 *
 * @param association (Association) : The association currently displayed
 * @param context (Context) : The context of the screen
 */
@Composable
private fun AssociationHeader(association: Association, context: Context) {
  Row {
    Box(modifier = Modifier.padding(horizontal = 24.dp).testTag("AssociationImageHeader")) {
      AsyncImage(
          model = association.image.toUri(),
          contentDescription = "Association image of " + association.name,
          modifier = Modifier.size(124.dp))
    }
    Column {
      Text(
          "${association.followersCount} " + context.getString(R.string.association_follower),
          style = AppTypography.headlineSmall,
          modifier = Modifier.padding(bottom = 5.dp).testTag("AssociationHeaderFollowers"))
      Text(
          "${association.members.list.collectAsState().value.size} " +
              context.getString(R.string.association_member),
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
