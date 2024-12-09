package com.android.unio.ui.association

import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.outlined.Share
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.ModalBottomSheetProperties
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.core.net.toUri
import com.android.unio.R
import com.android.unio.model.association.Association
import com.android.unio.model.association.AssociationViewModel
import com.android.unio.model.association.Member
import com.android.unio.model.event.Event
import com.android.unio.model.event.EventViewModel
import com.android.unio.model.strings.test_tags.association.AssociationProfileTestTags
import com.android.unio.model.user.User
import com.android.unio.model.user.UserViewModel
import com.android.unio.model.utils.NetworkUtils
import com.android.unio.ui.event.EventCard
import com.android.unio.ui.image.AsyncImageWrapper
import com.android.unio.ui.navigation.NavigationAction
import com.android.unio.ui.navigation.Screen
import com.android.unio.ui.theme.AppTypography
import com.android.unio.ui.utils.ToastUtils
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

// These variable are only here for testing purpose. They should be deleted when the screen is
// linked to the backend
private const val DEBUG_MESSAGE = "<DEBUG> Not implemented yet"

private var testSnackbar: SnackbarHostState? = null
private var scope: CoroutineScope? = null

/**
 * Composable element that contain the association profile screen. It display the association.
 *
 * @param navigationAction [NavigationAction] : The navigation actions of the screen
 * @param associationViewModel [AssociationViewModel] : The association view model
 * @param userViewModel [UserViewModel] : The user view model
 * @param eventViewModel [EventViewModel] : The event view model
 */
@Composable
fun AssociationProfileScreen(
    navigationAction: NavigationAction,
    associationViewModel: AssociationViewModel,
    userViewModel: UserViewModel,
    eventViewModel: EventViewModel
) {
  val association by associationViewModel.selectedAssociation.collectAsState()
  val context = LocalContext.current

  if (association == null) {
    Log.e("AssociationProfileScreen", "Association not found.")
    Toast.makeText(context, context.getString(R.string.association_toast_error), Toast.LENGTH_SHORT)
        .show()
    return
  }

  AssociationProfileScaffold(
      navigationAction = navigationAction,
      userViewModel = userViewModel,
      eventViewModel = eventViewModel,
      associationViewModel = associationViewModel,
      onEdit = {
        associationViewModel.selectAssociation(association!!.uid)
        navigationAction.navigateTo(Screen.EDIT_ASSOCIATION)
      })
}

/**
 * Composable element that contain the scaffold of the given association profile screen. Precisely,
 * it contains the top bar, the content given in parameter and the snackbar host used on
 * unimplemented features.
 *
 * @param navigationAction [NavigationAction] : The navigation actions of the screen
 * @param userViewModel [UserViewModel] : The user view model
 * @param eventViewModel [EventViewModel] : The event view model
 * @param associationViewModel [AssociationViewModel] : The association view model
 * @param onEdit [() -> Unit] : The action to edit the association
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AssociationProfileScaffold(
    navigationAction: NavigationAction,
    userViewModel: UserViewModel,
    eventViewModel: EventViewModel,
    associationViewModel: AssociationViewModel,
    onEdit: () -> Unit
) {
  val associationState by associationViewModel.selectedAssociation.collectAsState()
  val association = associationState!!

  var showSheet by remember { mutableStateOf(false) }

  val context = LocalContext.current
  testSnackbar = remember { SnackbarHostState() }
  scope = rememberCoroutineScope()
  Scaffold(
      snackbarHost = {
        SnackbarHost(
            hostState = testSnackbar!!,
            modifier = Modifier.testTag(AssociationProfileTestTags.SNACKBAR_HOST),
            snackbar = {
              Snackbar {
                TextButton(
                    onClick = { testSnackbar!!.currentSnackbarData?.dismiss() },
                    modifier =
                        Modifier.testTag(AssociationProfileTestTags.SNACKBAR_ACTION_BUTTON)) {
                      Text(text = DEBUG_MESSAGE)
                    }
              }
            })
      },
      topBar = {
        TopAppBar(
            title = {
              Text(
                  text = association.name,
                  modifier = Modifier.testTag(AssociationProfileTestTags.TITLE))
            },
            navigationIcon = {
              IconButton(
                  onClick = { navigationAction.goBack() },
                  modifier = Modifier.testTag(AssociationProfileTestTags.GO_BACK_BUTTON)) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Outlined.ArrowBack,
                        contentDescription = context.getString(R.string.association_go_back))
                  }
            },
            actions = {
              Row {
                IconButton(
                    modifier = Modifier.testTag(AssociationProfileTestTags.SHARE_BUTTON),
                    onClick = {
                      scope!!.launch {
                        testSnackbar!!.showSnackbar(
                            message = DEBUG_MESSAGE, duration = SnackbarDuration.Short)
                      }
                    }) {
                      Icon(
                          Icons.Outlined.Share,
                          contentDescription = context.getString(R.string.association_share))
                    }
              }
            })
      },
      content = { padding ->
        Surface(
            modifier = Modifier.padding(padding),
        ) {
          AssociationProfileContent(
              navigationAction, userViewModel, eventViewModel, associationViewModel)
        }
      })

  AssociationProfileBottomSheet(
      association, showSheet, onClose = { showSheet = false }, onEdit = onEdit)
}

/**
 * Composable element that contain the bottom sheet of the given association profile screen.
 *
 * @param association [Association] : The association to display
 * @param showSheet [Boolean] : The state of the bottom sheet
 * @param onClose [() -> Unit] : The action to close the bottom sheet
 * @param onEdit [() -> Unit] : The action to edit the association
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AssociationProfileBottomSheet(
    association: Association,
    showSheet: Boolean,
    onClose: () -> Unit,
    onEdit: () -> Unit
) {
  val sheetState = rememberModalBottomSheetState()

  val context = LocalContext.current

  if (showSheet) {
    ModalBottomSheet(
        modifier = Modifier.testTag(AssociationProfileTestTags.BOTTOM_SHEET),
        sheetState = sheetState,
        onDismissRequest = onClose,
        properties = ModalBottomSheetProperties(shouldDismissOnBackPress = true),
    ) {
      Column(modifier = Modifier) {
        Text(
            association.uid,
            color = MaterialTheme.colorScheme.inversePrimary,
            style = MaterialTheme.typography.bodySmall,
            modifier = Modifier.align(Alignment.CenterHorizontally))

        TextButton(modifier = Modifier.fillMaxWidth(), onClick = onEdit) {
          Text(context.getString(R.string.association_edit))
        }
      }
    }
  }
}

/**
 * Composable element that contain the content of the given association profile screen. It call all
 * elements that should be displayed on the screen, such as the header, the description, the
 * events...
 *
 * @param navigationAction [NavigationAction] : The navigation actions of the screen
 * @param userViewModel [UserViewModel] : The user view model
 * @param eventViewModel [EventViewModel] : The event view model
 * @param associationViewModel [AssociationViewModel] : The association view model
 */
@Composable
private fun AssociationProfileContent(
    navigationAction: NavigationAction,
    userViewModel: UserViewModel,
    eventViewModel: EventViewModel,
    associationViewModel: AssociationViewModel
) {
  val context = LocalContext.current
  val association by associationViewModel.selectedAssociation.collectAsState()
  val user by userViewModel.user.collectAsState()

  if (association == null || user == null) {
    Log.e("AssociationProfileContent", "Association or user not found.")
    return
  }

  var isFollowed by remember {
    mutableStateOf(user!!.followedAssociations.contains(association!!.uid))
  }
  var enableButton by remember { mutableStateOf(true) }
  val isConnected = NetworkUtils.checkInternetConnection(context)

  val onFollow = {
    if (isConnected) {
      enableButton = false
      associationViewModel.updateFollow(association!!, user!!, isFollowed) {
        userViewModel.refreshUser()
        enableButton = true
      }
      isFollowed = !isFollowed
    } else {
      ToastUtils.showToast(context, context.getString(R.string.no_internet_connection))
    }
  }

  val onMemberClick = { member: User ->
    userViewModel.setSomeoneElseUser(member)
    navigationAction.navigateTo(Screen.SOMEONE_ELSE_PROFILE)
  }

  // Add spacedBy to the horizontalArrangement
  Column(
      modifier =
          Modifier.testTag(AssociationProfileTestTags.SCREEN)
              .verticalScroll(rememberScrollState())
              .fillMaxWidth()
              .padding(24.dp),
      verticalArrangement = Arrangement.spacedBy(16.dp)) {
        AssociationHeader(association!!, isFollowed, enableButton, onFollow)
        AssociationDescription(association!!)
        AssociationEvents(navigationAction, association!!, userViewModel, eventViewModel)
        AssociationMembers(associationViewModel, association!!.members, onMemberClick)
        AssociationRecruitment(association!!)
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
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun AssociationRecruitment(association: Association) {
  val context = LocalContext.current

  Text(
      text = context.getString(R.string.association_join) + " ${association.name} ?",
      style = AppTypography.headlineMedium,
      modifier = Modifier.testTag(AssociationProfileTestTags.RECRUITMENT_TITLE))
  Text(
      text = context.getString(R.string.association_help_us),
      style = AppTypography.bodySmall,
      modifier = Modifier.testTag(AssociationProfileTestTags.RECRUITMENT_DESCRIPTION))
  FlowRow(
      modifier = Modifier.testTag(AssociationProfileTestTags.RECRUITMENT_ROLES),
      horizontalArrangement = Arrangement.spacedBy(8.dp),
  ) {
    OutlinedButton(
        modifier = Modifier.testTag(AssociationProfileTestTags.DESIGNER_ROLES),
        onClick = {
          scope!!.launch {
            testSnackbar!!.showSnackbar(message = DEBUG_MESSAGE, duration = SnackbarDuration.Short)
          }
        },
        enabled = true) {
          Icon(
              Icons.Filled.Add,
              contentDescription = context.getString(R.string.association_recruitment))
          Spacer(Modifier.width(2.dp))
          Text("Graphic Designer")
        }
    OutlinedButton(
        modifier = Modifier.testTag(AssociationProfileTestTags.TREASURER_ROLES),
        onClick = {
          scope!!.launch {
            testSnackbar!!.showSnackbar(message = DEBUG_MESSAGE, duration = SnackbarDuration.Short)
          }
        },
        enabled = true) {
          Icon(
              Icons.Filled.Add,
              contentDescription = context.getString(R.string.association_recruitment))
          Spacer(Modifier.width(2.dp))
          Text("Treasurer")
        }
  }
}

/**
 * Component that display the users that are in the association that can be contacted. It display
 * the title of the section and then display the different users in the association.
 *
 * @param members (List<User>) : The list of users in the association that can be contacted
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun AssociationMembers(
    associationViewModel: AssociationViewModel,
    members: List<Member>,
    onMemberClick: (User) -> Unit
) {
  val context = LocalContext.current

  if (members.isEmpty()) {
    return
  }

  Text(
      context.getString(R.string.association_contact_members),
      style = AppTypography.headlineMedium,
      modifier = Modifier.testTag(AssociationProfileTestTags.CONTACT_MEMBERS_TITLE))
  FlowRow(
      modifier = Modifier.fillMaxWidth(),
      horizontalArrangement = Arrangement.spacedBy(16.dp, Alignment.CenterHorizontally),
      verticalArrangement = Arrangement.spacedBy(16.dp)) {
        members.forEach { member ->
          val user = associationViewModel.getUserFromMember(member).collectAsState()
          Column(
              modifier =
                  Modifier.background(
                          MaterialTheme.colorScheme.secondaryContainer, RoundedCornerShape(8.dp))
                      .clickable { user.value?.let { onMemberClick(it) } }
                      .padding(16.dp),
              verticalArrangement = Arrangement.spacedBy(8.dp),
              horizontalAlignment = Alignment.CenterHorizontally) {
                Box(
                    modifier =
                        Modifier.clip(CircleShape)
                            .size(75.dp)
                            .background(MaterialTheme.colorScheme.surfaceDim)) {
                      user.value?.profilePicture?.toUri()?.let {
                        AsyncImageWrapper(
                            imageUri = it,
                            contentDescription =
                                context.getString(
                                    R.string.association_contact_member_profile_picture),
                            modifier = Modifier.fillMaxWidth(),
                            contentScale = ContentScale.Crop)
                      }
                    }
                user.value?.firstName?.let {
                  val firstName = it
                  user.value?.lastName?.let {
                    val lastName = it
                    Text("${firstName} ${lastName}")
                  }
                }
              }
        }
      }
}

/**
 * Component that display all the events of the association in a card format, like in the home
 * screen.
 *
 * @param navigationAction (NavigationAction) : The navigation actions of the screen
 * @param association (Association) : The association currently displayed
 * @param userViewModel (UserViewModel) : The user view model
 * @param eventViewModel (EventViewModel) : The event view model
 */
@Composable
private fun AssociationEvents(
    navigationAction: NavigationAction,
    association: Association,
    userViewModel: UserViewModel,
    eventViewModel: EventViewModel
) {
  val context = LocalContext.current
  val isConnected = NetworkUtils.checkInternetConnection(context)

  var isSeeMoreClicked by remember { mutableStateOf(false) }

  val events by association.events.list.collectAsState()

  // To be changed when we have a functional admin system
  var isAdmin by remember { mutableStateOf(true) }

  if (events.isNotEmpty()) {
    Text(
        context.getString(R.string.association_upcoming_events),
        modifier = Modifier.testTag(AssociationProfileTestTags.EVENT_TITLE),
        style = AppTypography.headlineMedium)
    events.sortedBy { it.startDate }
    val first = events.first()
    Column(modifier = Modifier, horizontalAlignment = Alignment.CenterHorizontally) {
      if (isSeeMoreClicked) {
        events.forEach { event ->
          AssociationEventCard(navigationAction, event, userViewModel, eventViewModel)
        }
      } else {
        AssociationEventCard(navigationAction, first, userViewModel, eventViewModel)
      }
    }
    if (events.size > 1) {
      OutlinedButton(
          onClick = { isSeeMoreClicked = true },
          modifier = Modifier.testTag(AssociationProfileTestTags.SEE_MORE_BUTTON)) {
            Icon(
                Icons.AutoMirrored.Filled.ArrowForward,
                contentDescription = context.getString(R.string.association_see_more))
            Spacer(Modifier.width(2.dp))
            Text(context.getString(R.string.association_see_more))
          }
    }
  }
  if (isAdmin) {
    Button(
        onClick = {
          if (isConnected) {
            navigationAction.navigateTo(Screen.EVENT_CREATION)
          } else {
            ToastUtils.showToast(context, context.getString(R.string.no_internet_connection))
          }
        },
        modifier = Modifier.testTag(AssociationProfileTestTags.ADD_EVENT_BUTTON),
        contentPadding = ButtonDefaults.ButtonWithIconContentPadding) {
          Icon(
              Icons.Filled.Add,
              contentDescription = context.getString(R.string.association_profile_add_event_button),
              modifier = Modifier.size(ButtonDefaults.IconSize))
          Spacer(Modifier.size(ButtonDefaults.IconSpacing))
          Text(context.getString(R.string.association_profile_add_event_button))
        }
  }
}

/**
 * Component that display only one event in a card format, like in the home screen.
 *
 * @param event (Event) : The event to display
 */
@Composable
private fun AssociationEventCard(
    navigationAction: NavigationAction,
    event: Event,
    userViewModel: UserViewModel,
    eventViewModel: EventViewModel
) {
  Box(modifier = Modifier.testTag(AssociationProfileTestTags.EVENT_CARD + event.uid)) {
    EventCard(
        navigationAction = navigationAction,
        event = event,
        userViewModel = userViewModel,
        eventViewModel = eventViewModel,
        true)
  }
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
      modifier = Modifier.testTag(AssociationProfileTestTags.DESCRIPTION))
}

/**
 * Component that display the header of the association profile screen. It display the image of the
 * association, the number of followers and the number of members. It also display a button to
 * follow the association.
 */
@Composable
private fun AssociationHeader(
    association: Association,
    isFollowed: Boolean,
    enableButton: Boolean,
    onFollow: () -> Unit,
) {
  val context = LocalContext.current
  Row(
      horizontalArrangement = Arrangement.spacedBy(16.dp),
  ) {
    Box(modifier = Modifier.testTag(AssociationProfileTestTags.IMAGE_HEADER)) {
      AsyncImageWrapper(
          imageUri = association.image.toUri(),
          contentDescription =
              context.getString(R.string.association_content_description_association_image) +
                  association.name,
          modifier = Modifier.size(124.dp),
          placeholderResourceId = R.drawable.adec,
          contentScale = ContentScale.Crop)
    }
    Column {
      Text(
          "${association.followersCount} " + context.getString(R.string.association_follower),
          style = AppTypography.headlineSmall,
          modifier =
              Modifier.padding(bottom = 5.dp).testTag(AssociationProfileTestTags.HEADER_FOLLOWERS))
      Text(
          "${association.members.size} " + context.getString(R.string.association_member),
          style = AppTypography.headlineSmall,
          modifier =
              Modifier.padding(bottom = 14.dp).testTag(AssociationProfileTestTags.HEADER_MEMBERS))

      if (isFollowed) {
        OutlinedButton(
            enabled = enableButton,
            onClick = onFollow,
            modifier = Modifier.testTag(AssociationProfileTestTags.FOLLOW_BUTTON)) {
              Text(context.getString(R.string.association_unfollow))
            }
      } else {
        Button(
            enabled = enableButton,
            onClick = onFollow,
            modifier = Modifier.testTag(AssociationProfileTestTags.FOLLOW_BUTTON)) {
              Icon(
                  Icons.Filled.Add,
                  contentDescription =
                      context.getString(R.string.association_content_description_follow_icon))
              Spacer(Modifier.width(2.dp))
              Text(context.getString(R.string.association_follow))
            }
      }
    }
  }
}
