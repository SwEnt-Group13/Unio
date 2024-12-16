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
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PagerState
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.outlined.MoreVert
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
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Alignment.Companion.CenterHorizontally
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.net.toUri
import com.android.unio.R
import com.android.unio.model.association.Association
import com.android.unio.model.association.AssociationViewModel
import com.android.unio.model.association.Member
import com.android.unio.model.association.PermissionType
import com.android.unio.model.event.Event
import com.android.unio.model.event.EventViewModel
import com.android.unio.model.notification.NotificationType
import com.android.unio.model.search.SearchViewModel
import com.android.unio.model.strings.test_tags.association.AssociationProfileTestTags
import com.android.unio.model.user.User
import com.android.unio.model.user.UserViewModel
import com.android.unio.model.utils.NetworkUtils
import com.android.unio.ui.components.EventSearchBar
import com.android.unio.ui.components.NotificationSender
import com.android.unio.ui.components.RoleBadge
import com.android.unio.ui.event.EventCard
import com.android.unio.ui.image.AsyncImageWrapper
import com.android.unio.ui.navigation.NavigationAction
import com.android.unio.ui.navigation.Screen
import com.android.unio.ui.navigation.SmoothTopBarNavigationMenu
import com.android.unio.ui.theme.AppTypography
import com.android.unio.ui.utils.ToastUtils
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch

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
    searchViewModel: SearchViewModel,
    userViewModel: UserViewModel,
    eventViewModel: EventViewModel
) {
  val association by associationViewModel.selectedAssociation.collectAsState()
  val context = LocalContext.current

  if (association == null) {
    Log.e("AssociationProfileScreen", "Association not found.")
    Toast.makeText(context, context.getString(R.string.association_toast_error), Toast.LENGTH_SHORT)
        .show()
  } else {

    AssociationProfileScaffold(
        navigationAction = navigationAction,
        userViewModel = userViewModel,
        eventViewModel = eventViewModel,
        associationViewModel = associationViewModel,
        searchViewModel = searchViewModel,
        onEdit = {
          associationViewModel.selectAssociation(association!!.uid)
          navigationAction.navigateTo(Screen.SAVE_ASSOCIATION)
        })
  }
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
    searchViewModel: SearchViewModel,
    onEdit: () -> Unit
) {
  val associationState by associationViewModel.selectedAssociation.collectAsState()
  val association = associationState!!

  var showSheet by remember { mutableStateOf(false) }
  val context = LocalContext.current

  Scaffold(
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
                    modifier = Modifier.testTag(AssociationProfileTestTags.MORE_BUTTON),
                    onClick = { showSheet = true }) {
                      Icon(
                          Icons.Outlined.MoreVert,
                          contentDescription = context.getString(R.string.association_more))
                    }
              }
            })
      },
      content = { padding ->
        val user by userViewModel.user.collectAsState()
        val userRole = association.members.find { it.uid == user!!.uid }?.role
        val userPermissions = userRole?.permissions

        Box(modifier = Modifier.fillMaxSize().padding(padding)) {
          if (userPermissions?.hasAnyPermission() == true) {
            val userRoleColor = Color(userRole.color)

            // Horizontal red strip
            Box(
                modifier =
                    Modifier.fillMaxWidth()
                        .background(userRoleColor)
                        .height(50.dp)
                        .align(Alignment.TopCenter)) {
                  Text(
                      context.getString(R.string.association_profile_your_role_text) +
                          " " +
                          userRole.displayName,
                      color = Color.White,
                      modifier = Modifier.align(Alignment.Center))
                }

            // Main content with vertical red lines and pager
            Box(
                modifier =
                    Modifier.fillMaxSize().padding(top = 50.dp) // Ensure space for the red strip
                ) {
                  Row(
                      modifier =
                          Modifier.fillMaxSize()
                              .padding(horizontal = 0.dp) // Space for vertical lines
                      ) {
                        // Left red line
                        Box(
                            modifier =
                                Modifier.width(2.dp).fillMaxHeight().background(userRoleColor))

                        // Main content (Conditional based on permission)
                        if (userPermissions.hasPermission(PermissionType.BETTER_OVERVIEW) &&
                            !(userPermissions.hasPermission(PermissionType.FULL_RIGHTS)) &&
                            userPermissions.getGrantedPermissions().size == 1) {
                          // Default content without HorizontalPager
                          Box(modifier = Modifier.weight(1f).padding(horizontal = 8.dp)) {
                            AssociationProfileContent(
                                navigationAction = navigationAction,
                                userViewModel = userViewModel,
                                eventViewModel = eventViewModel,
                                associationViewModel = associationViewModel)
                          }
                        } else {
                          // Main content with HorizontalPager
                          Column(modifier = Modifier.weight(1f).padding(horizontal = 8.dp)) {
                            val nbOfTabs = 2
                            val pagerState = rememberPagerState(initialPage = 0) { nbOfTabs }

                            // Tab Menu
                            val tabList =
                                listOf(
                                    context.getString(R.string.association_tab_overview),
                                    context.getString(R.string.association_tab_actions))
                            SmoothTopBarNavigationMenu(tabList, pagerState)

                            // Pager Content
                            HorizontalPager(
                                state = pagerState, modifier = Modifier.fillMaxSize()) { page ->
                                  when (page) {
                                    0 ->
                                        AssociationProfileContent(
                                            navigationAction = navigationAction,
                                            userViewModel = userViewModel,
                                            eventViewModel = eventViewModel,
                                            associationViewModel = associationViewModel)
                                    1 ->
                                        AssociationProfileActionsContent(
                                            navigationAction = navigationAction,
                                            userViewModel = userViewModel,
                                            eventViewModel = eventViewModel,
                                            associationViewModel = associationViewModel,
                                            searchViewModel = searchViewModel)
                                  }
                                }
                          }
                        }

                        // Right red line (This will always be displayed)
                        Box(
                            modifier =
                                Modifier.width(2.dp).fillMaxHeight().background(userRoleColor))
                      }
                }
          } else {
            // Default content without permissions
            AssociationProfileContent(
                navigationAction = navigationAction,
                userViewModel = userViewModel,
                eventViewModel = eventViewModel,
                associationViewModel = associationViewModel)
          }
        }
      })

  var showNotificationDialog by remember { mutableStateOf(false) }

  NotificationSender(
      context.getString(R.string.association_broadcast_message),
      NotificationType.ASSOCIATION_FOLLOWERS,
      association.uid,
      { mapOf("title" to association.name, "body" to it) },
      showNotificationDialog,
      { showNotificationDialog = false })

  AssociationProfileBottomSheet(
      showSheet,
      onClose = { showSheet = false },
      onEdit = onEdit,
      onOpenNotificationDialog = { showNotificationDialog = true })
}

/**
 * Composable element that contain the bottom sheet of the given association profile screen.
 *
 * @param showSheet [Boolean] : The state of the bottom sheet
 * @param onClose [() -> Unit] : The action to close the bottom sheet
 * @param onEdit [() -> Unit] : The action to edit the association
 * @param onOpenNotificationDialog [() -> Unit] : The action to open the notification dialog
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AssociationProfileBottomSheet(
    showSheet: Boolean,
    onClose: () -> Unit,
    onEdit: () -> Unit,
    onOpenNotificationDialog: () -> Unit
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
      Column {
        TextButton(
            modifier =
                Modifier.fillMaxWidth().testTag(AssociationProfileTestTags.BOTTOM_SHEET_EDIT),
            onClick = {
              onClose()
              onEdit()
            }) {
              Text(context.getString(R.string.association_edit))
            }
        TextButton(
            modifier =
                Modifier.fillMaxWidth()
                    .testTag(AssociationProfileTestTags.BOTTOM_SHEET_NOTIFICATION),
            onClick = {
              onClose()
              onOpenNotificationDialog()
            }) {
              Text(context.getString(R.string.association_broadcast_message))
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
      }
}

/**
 * Composable element that contain the actions of the given association. It call all
 * elements that should be displayed on the screen, such as the header, the description, the
 * events...
 *
 * @param navigationAction [NavigationAction] : The navigation actions of the screen
 * @param userViewModel [UserViewModel] : The user view model
 * @param eventViewModel [EventViewModel] : The event view model
 * @param associationViewModel [AssociationViewModel] : The association view model
 */
@Composable
private fun AssociationProfileActionsContent(
    navigationAction: NavigationAction,
    userViewModel: UserViewModel,
    eventViewModel: EventViewModel,
    associationViewModel: AssociationViewModel,
    searchViewModel: SearchViewModel
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
        AssociationActionsHeader(association!!, isFollowed, enableButton, onFollow, onClickSaveButton = {associationViewModel.selectAssociation(association!!.uid);navigationAction.navigateTo(Screen.SAVE_ASSOCIATION)})
        AssociationActionsEvents(navigationAction, association!!, userViewModel, eventViewModel, searchViewModel = searchViewModel)
        AssociationDescription(association!!)
        AssociationMembers(associationViewModel, association!!.members, onMemberClick)
    }
}

/**
 * Component that displays the users that are in the association that can be contacted. It displays
 * the title of the section and then displays the different users in the association.
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
                    Text("$firstName $lastName")

                    // Role Badge
                    RoleBadge(member.role)
                  }
                }
              }
        }
      }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun AssociationActionsMembers(
    associationViewModel: AssociationViewModel,
    members: List<Member>,
    onMemberClick: (User) -> Unit
) {
    val context = LocalContext.current

    if (members.isEmpty()) {
        return
    }

    Text(
        text = "Members",
        style = AppTypography.headlineMedium,
        modifier = Modifier.testTag("NEW TEST TAGGGG"))
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
                        Text("$firstName $lastName")

                        // Role Badge
                        RoleBadge(member.role)
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
  val user by userViewModel.user.collectAsState()

  if (user == null) {
    return
  }

  // Check if the user is a member of the association
  val isMember = association.members.any { it.uid == user!!.uid }

  // Retrieve the member's permissions if they are part of the association
  val userPermissions = association.members.find { it.uid == user!!.uid }?.role?.permissions

  // Check if the user has the "ADD_EVENTS" permission using the Permissions class
  val hasAddEventsPermission =
      userPermissions?.hasPermission(PermissionType.ADD_EDIT_EVENTS) == true
  if (events.isNotEmpty()) {
    Text(
        context.getString(R.string.association_upcoming_events),
        modifier = Modifier.testTag(AssociationProfileTestTags.EVENT_TITLE),
        style = AppTypography.headlineMedium)
    events.sortedBy { it.startDate }
    val first = events.first()
    Column(horizontalAlignment = Alignment.CenterHorizontally) {

      // See more clicked, display all events
      if (isSeeMoreClicked) {
        events.forEach { event ->
          AssociationEventCard(navigationAction, event, userViewModel, eventViewModel, shouldBeEditable = false)
        }
        // Display the first event only
      } else {
        AssociationEventCard(navigationAction, first, userViewModel, eventViewModel, shouldBeEditable = false)
      }
    }
    // Display the see more button if there are more than one event
    if (events.size > 1) {
      AssociationProfileSeeMoreButton(
          { isSeeMoreClicked = false }, { isSeeMoreClicked = true }, isSeeMoreClicked)
    }
  }
  // Show the "Add Event" button only if the user is a member and has the "ADD_EVENTS" permission
  if (isMember && hasAddEventsPermission) {
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


@Composable
private fun AssociationActionsEvents(
    navigationAction: NavigationAction,
    association: Association,
    userViewModel: UserViewModel,
    eventViewModel: EventViewModel,
    searchViewModel: SearchViewModel // Add this parameter for event searching
) {
    val context = LocalContext.current
    val isConnected = NetworkUtils.checkInternetConnection(context)

    val events by association.events.list.collectAsState()
    val user by userViewModel.user.collectAsState()
    val eventResults by searchViewModel.events.collectAsState() // Observing search results
    val searchStatus by searchViewModel.status.collectAsState()

    if (user == null) {
        return
    }

    val isMember = association.members.any { it.uid == user!!.uid }
    val userPermissions = association.members.find { it.uid == user!!.uid }?.role?.permissions
    val hasAddEventsPermission =
        userPermissions?.hasPermission(PermissionType.ADD_EDIT_EVENTS) == true

    Text(
        text = "Events",
        modifier = Modifier.testTag(AssociationProfileTestTags.EVENT_TITLE),
        style = AppTypography.headlineLarge
    )

    // Add Event Button
    if (isMember && hasAddEventsPermission) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 0.dp),
            horizontalArrangement = Arrangement.Center
        ) {
            Button(
                onClick = {
                    if (isConnected) {
                        navigationAction.navigateTo(Screen.EVENT_CREATION)
                    } else {
                        ToastUtils.showToast(context, context.getString(R.string.no_internet_connection))
                    }
                },
                modifier = Modifier.testTag(AssociationProfileTestTags.ADD_EVENT_BUTTON),
                contentPadding = ButtonDefaults.ButtonWithIconContentPadding
            ) {
                Icon(
                    Icons.Filled.Add,
                    contentDescription = context.getString(R.string.association_profile_add_event_button),
                    modifier = Modifier.size(ButtonDefaults.IconSize)
                )
                Spacer(Modifier.size(ButtonDefaults.IconSpacing))
                Text(context.getString(R.string.association_profile_add_event_button))
            }
        }
    }

    // Remember the pager state and the list of sorted events
    val sortedEvents = events.sortedBy { it.startDate }
    val nbOfTabs = sortedEvents.size
    val pagerState = rememberPagerState(initialPage = 0) { nbOfTabs }
    val coroutineScope = rememberCoroutineScope() // For handling coroutine launches

    // Add Event SearchBar on top of the slide bar
    EventSearchBar(
        searchViewModel = searchViewModel,
        onEventSelected = { selectedEvent ->
            // Scroll the HorizontalPager to the selected event
            val targetPage = sortedEvents.indexOfFirst { it.uid == selectedEvent.uid }
            if (targetPage >= 0) {
                coroutineScope.launch {
                    pagerState.animateScrollToPage(targetPage)
                }
            }
        },
        shouldCloseExpandable = false,
        onOutsideClickHandled = {}
    )

    // Slide Bar Section
    if (events.isNotEmpty()) {

        Column(horizontalAlignment = CenterHorizontally) {

            if (sortedEvents.size > 1) {
                Text(
                    text = "Slide to see all the events",
                    modifier = Modifier.testTag("ADDTESTTAGGG").padding(bottom = 4.dp),
                    style = AppTypography.bodySmall
                )
                ProgressBarBetweenElements(
                    tabList = sortedEvents.map { it.title ?: "Event" },
                    pagerState = pagerState
                )
            }

            HorizontalPager(
                state = pagerState,
                modifier = Modifier.fillMaxWidth(),
                contentPadding = PaddingValues(horizontal = 16.dp),
                pageSpacing = 16.dp
            ) { page ->
                val event = sortedEvents[page]
                AssociationEventCard(
                    navigationAction,
                    event,
                    userViewModel,
                    eventViewModel,
                    shouldBeEditable = hasAddEventsPermission
                )
            }
        }
    }
}




@Composable
fun ProgressBarBetweenElements(tabList: List<String>, pagerState: PagerState) {
    val defaultTabWidth = 576.0F
    val defaultTabHeight = 92.0F

    val scope = rememberCoroutineScope()
    val colorScheme = MaterialTheme.colorScheme
    val sizeList = remember { mutableStateMapOf<Int, Pair<Float, Float>>() }
    val progressFromFirstPage by remember {
        derivedStateOf { pagerState.currentPageOffsetFraction + pagerState.currentPage.dp.value }
    }

    TabRow(
        selectedTabIndex = pagerState.currentPage,
        contentColor = colorScheme.primary,
        divider = {},
        indicator = {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .drawBehind {
                        val totalWidth = sizeList.values.map { it.first }.sum()
                        val height: Float

                        if (sizeList.isEmpty()) {
                            Log.e("Home Page", "The size values of tabs are null, should not happen !")
                            height = defaultTabHeight
                        } else {
                            height = sizeList[0]?.second ?: defaultTabHeight
                        }

                        // Draw the outer rounded rectangle encompassing the full sliding bar area
                        val outerRectangleYStart = height - 45
                        val outerRectangleYEnd = height - 5

                        // Draw the inner rounded rectangle (the sliding line area)
                        val tabWidth = sizeList[0]?.first ?: defaultTabWidth
                        val rectangleStartX = progressFromFirstPage * tabWidth + tabWidth / 4
                        val rectangleEndX = progressFromFirstPage * tabWidth + tabWidth * 3 / 4
                        val rectangleYStart = height - 35
                        val rectangleYEnd = height - 15

                        drawRoundRect(
                            color = colorScheme.primary.copy(alpha = 0.1f),
                            topLeft = Offset(x = tabWidth / 4, y = outerRectangleYStart),
                            size = Size(width = tabWidth * 7 / 2, height = outerRectangleYEnd - outerRectangleYStart), // 2 * (7/2 = 1 + 3 / 4)
                            cornerRadius = CornerRadius(x = 16.dp.toPx(), y = 16.dp.toPx())
                        )

                        drawRoundRect(
                            color = colorScheme.primary.copy(alpha = 0.2f),
                            topLeft = Offset(x = rectangleStartX, y = rectangleYStart),
                            size = Size(width = rectangleEndX - rectangleStartX, height = rectangleYEnd - rectangleYStart),
                            cornerRadius = CornerRadius(x = 12.dp.toPx(), y = 12.dp.toPx())
                        )

                        // Draw the sliding line inside the inner rectangle
                        val lineStartOffset = Offset(x = progressFromFirstPage * tabWidth + tabWidth / 3, y = height - 25)
                        val lineEndOffset = Offset(x = progressFromFirstPage * tabWidth + tabWidth * 2 / 3, y = height - 25)

                        drawLine(
                            start = lineStartOffset,
                            end = lineEndOffset,
                            color = colorScheme.primary,
                            strokeWidth = Stroke.DefaultMiter
                        )
                    }
            )
        }
    ) {
        tabList.forEachIndexed { index, str ->
            Tab(
                selected = index == pagerState.currentPage,
                onClick = { scope.launch { pagerState.animateScrollToPage(index) } },
                modifier = Modifier.onSizeChanged {
                    sizeList[index] = Pair(it.width.toFloat(), it.height.toFloat())
                },
                selectedContentColor = colorScheme.primary
            ) {
                Spacer(
                    modifier = Modifier
                        .height(20.dp) // Minimal size to retain dimensions without visible content
                )
            }
        }
    }
}






@Composable
fun AssociationProfileSeeMoreButton(
    onSeeMore: () -> Unit,
    onSeeLess: () -> Unit,
    isSeeMoreClicked: Boolean
) {
  val context = LocalContext.current
  if (isSeeMoreClicked) {
    OutlinedButton(
        onClick = { onSeeMore() },
        modifier = Modifier.testTag(AssociationProfileTestTags.SEE_MORE_BUTTON)) {
          Icon(
              Icons.AutoMirrored.Filled.ArrowBack,
              contentDescription = context.getString(R.string.association_see_less))
          Spacer(Modifier.width(2.dp))
          Text(context.getString(R.string.association_see_less))
        }
  } else {
    OutlinedButton(
        onClick = { onSeeLess() },
        modifier = Modifier.testTag(AssociationProfileTestTags.SEE_MORE_BUTTON)) {
          Icon(
              Icons.AutoMirrored.Filled.ArrowForward,
              contentDescription = context.getString(R.string.association_see_more))
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
private fun AssociationEventCard(
    navigationAction: NavigationAction,
    event: Event,
    userViewModel: UserViewModel,
    eventViewModel: EventViewModel,
    shouldBeEditable: Boolean
) {
  Box(modifier = Modifier.testTag(AssociationProfileTestTags.EVENT_CARD + event.uid)) {
    EventCard(
        navigationAction = navigationAction,
        event = event,
        userViewModel = userViewModel,
        eventViewModel = eventViewModel,
        shouldBeEditable = shouldBeEditable)
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

/**
 * Component that display the header of the association profile screen. It display the image of the
 * association, the number of followers and the number of members. It also display a button to
 * follow the association.
 */
@Composable
private fun AssociationActionsHeader(
    association: Association,
    isFollowed: Boolean,
    enableButton: Boolean,
    onFollow: () -> Unit,
    onClickSaveButton: () -> Unit
) {
    val context = LocalContext.current


    Text(
        text = "General Actions",
        modifier = Modifier.testTag(AssociationProfileTestTags.EVENT_TITLE),
        style = AppTypography.headlineLarge
    )

    var showNotificationDialog by remember { mutableStateOf(false) }

    NotificationSender(
        context.getString(R.string.association_broadcast_message),
        NotificationType.ASSOCIATION_FOLLOWERS,
        association.uid,
        { mapOf("title" to association.name, "body" to it) },
        showNotificationDialog,
        { showNotificationDialog = false })

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 0.dp), // Optional padding around the row
        horizontalArrangement = Arrangement.Center, // Centers the content horizontally
        verticalAlignment = Alignment.CenterVertically // Aligns the text and icon vertically
    ) {
        Box(
            modifier = Modifier.widthIn(max = 300.dp) // Constrain the max width of the text
        ) {
            Text(
                text = "Broadcast a message to all members of the association",
                style = AppTypography.bodyMedium, // Use appropriate typography style
                modifier = Modifier.padding(end = 8.dp) // Add space between the text and the icon
            )
        }
        IconButton(
            onClick = {
                showNotificationDialog = true
            },
            modifier = Modifier.testTag("BROADCAST_ICON_BUTTON").size(24.dp)
        ) {
            Icon(
                Icons.AutoMirrored.Filled.Send,
                contentDescription = "CONTENT BROADCASTBUTTON",
                tint = MaterialTheme.colorScheme.primary // Optional: style the icon with a color
            )
        }
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 0.dp), // Optional padding around the row
        horizontalArrangement = Arrangement.Center, // Centers the content horizontally
        verticalAlignment = Alignment.CenterVertically // Aligns the text and icon vertically
    ) {
        Box(
            modifier = Modifier.widthIn(max = 300.dp) // Constrain the max width of the text
        ) {
            Text(
                text = "Edit Association",
                style = AppTypography.bodyMedium, // Use appropriate typography style
                modifier = Modifier.padding(end = 8.dp) // Add space between the text and the icon
            )
        }
        IconButton(
            onClick = {
                onClickSaveButton()
            },
            modifier = Modifier.testTag("BROADCAST_ICON_BUTTON").size(24.dp)
        ) {
            Icon(
                Icons.Filled.Edit,
                contentDescription = "CONTENT BROADCASTBUTTON",
                tint = MaterialTheme.colorScheme.primary // Optional: style the icon with a color
            )
        }
    }
}
