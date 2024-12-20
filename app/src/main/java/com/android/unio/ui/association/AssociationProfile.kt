package com.android.unio.ui.association

import android.annotation.SuppressLint
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
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.outlined.MoreVert
import androidx.compose.material.pullrefresh.PullRefreshIndicator
import androidx.compose.material.pullrefresh.pullRefresh
import androidx.compose.material.pullrefresh.rememberPullRefreshState
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.ModalBottomSheetProperties
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.core.net.toUri
import com.android.unio.R
import com.android.unio.model.association.Association
import com.android.unio.model.association.AssociationViewModel
import com.android.unio.model.association.Member
import com.android.unio.model.association.PermissionType
import com.android.unio.model.association.Permissions
import com.android.unio.model.association.Role
import com.android.unio.model.event.Event
import com.android.unio.model.event.EventViewModel
import com.android.unio.model.functions.addEditRoleCloudFunction
import com.android.unio.model.notification.NotificationType
import com.android.unio.model.search.SearchViewModel
import com.android.unio.model.strings.test_tags.association.AssociationProfileActionsTestTags
import com.android.unio.model.strings.test_tags.association.AssociationProfileTestTags
import com.android.unio.model.user.User
import com.android.unio.model.user.UserViewModel
import com.android.unio.model.utils.NetworkUtils
import com.android.unio.ui.components.EventSearchBar
import com.android.unio.ui.components.MemberSearchBar
import com.android.unio.ui.components.NotificationSender
import com.android.unio.ui.components.RoleBadge
import com.android.unio.ui.components.SearchPagerSection
import com.android.unio.ui.event.EventCard
import com.android.unio.ui.image.AsyncImageWrapper
import com.android.unio.ui.navigation.NavigationAction
import com.android.unio.ui.navigation.Screen
import com.android.unio.ui.navigation.SmoothTopBarNavigationMenu
import com.android.unio.ui.theme.AppTypography
import com.android.unio.ui.utils.ToastUtils
import com.github.skydoves.colorpicker.compose.HsvColorPicker
import com.github.skydoves.colorpicker.compose.rememberColorPickerController
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
@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterialApi::class)
@Composable
fun AssociationProfileScaffold(
    navigationAction: NavigationAction,
    userViewModel: UserViewModel,
    eventViewModel: EventViewModel,
    associationViewModel: AssociationViewModel,
    searchViewModel: SearchViewModel,
    onEdit: () -> Unit
) {
  val associationCollect by associationViewModel.selectedAssociation.collectAsState()

  val refreshState by associationViewModel.refreshState
  val pullRefreshState =
      rememberPullRefreshState(
          refreshing = refreshState,
          onRefresh = {
            associationViewModel.refreshAssociation()
            eventViewModel.loadEvents()
          })

  var showNotificationDialog by remember { mutableStateOf(false) }

  var showSheet by remember { mutableStateOf(false) }
  val context = LocalContext.current
  associationCollect?.let { association ->
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
          Box(modifier = Modifier.padding(0.dp).fillMaxSize()) {
            val user by userViewModel.user.collectAsState()
            val userRole =
                association.roles.find {
                  it.uid == association.members.find { it.uid == user!!.uid }?.roleUid
                }

            val userPermissions = userRole?.permissions

            Box(modifier = Modifier.fillMaxSize().padding(padding)) {
              if (userPermissions?.hasAnyPermission() == true) {
                val userRoleColor = Color(userRole.color)


                Box(
                    modifier =
                        Modifier.fillMaxWidth()
                            .background(userRoleColor)
                            .height(50.dp)
                            .align(Alignment.TopCenter)) {
                      Text(
                          context.getString(R.string.association_profile_scaffold_role_is) + " " + userRole.displayName,
                          color = Color.White,
                          modifier = Modifier.align(Alignment.Center))
                    }


                Box(
                    modifier =
                        Modifier.fillMaxSize()
                            .padding(top = 50.dp)
                    ) {
                      Row(
                          modifier =
                              Modifier.fillMaxSize()
                                  .padding(horizontal = 0.dp)
                          ) {
                            Box(
                                modifier =
                                    Modifier.width(2.dp).fillMaxHeight().background(userRoleColor))

                            // Main content (Conditional based on permission)
                            if (userPermissions.hasPermission(PermissionType.BETTER_OVERVIEW) &&
                                !(userPermissions.hasPermission(PermissionType.FULL_RIGHTS)) &&
                                userPermissions.getGrantedPermissions().size == 1) {
                              // Default content without HorizontalPager
                              Box(
                                  modifier =
                                      Modifier.weight(1f)
                                          .padding(horizontal = 8.dp)
                                          .pullRefresh(pullRefreshState)
                                          .verticalScroll(rememberScrollState())) {
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
                                val tabList = listOf(context.getString(R.string.association_profile_scaffold_overview), context.getString(R.string.association_profile_scaffold_actions))
                                SmoothTopBarNavigationMenu(tabList, pagerState)

                                // Pager Content
                                HorizontalPager(
                                    userScrollEnabled = false,
                                    state = pagerState,
                                    modifier = Modifier.fillMaxSize()) { page ->
                                      when (page) {
                                        0 ->
                                            Box(
                                                modifier =
                                                    Modifier.pullRefresh(pullRefreshState)
                                                        .verticalScroll(rememberScrollState())) {
                                                  AssociationProfileContent(
                                                      navigationAction = navigationAction,
                                                      userViewModel = userViewModel,
                                                      eventViewModel = eventViewModel,
                                                      associationViewModel = associationViewModel)
                                                }
                                        1 ->
                                            Box(
                                                modifier =
                                                    Modifier.pullRefresh(pullRefreshState)
                                                        .verticalScroll(rememberScrollState())) {
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
                            }

                            Box(
                                modifier =
                                    Modifier.width(2.dp).fillMaxHeight().background(userRoleColor))
                          }
                    }
              } else {
                // Default content without permissions
                Box(
                    modifier =
                        Modifier.pullRefresh(pullRefreshState)
                            .verticalScroll(rememberScrollState())) {
                      AssociationProfileContent(
                          navigationAction = navigationAction,
                          userViewModel = userViewModel,
                          eventViewModel = eventViewModel,
                          associationViewModel = associationViewModel)
                    }
              }
            }
          }
        })
    NotificationSender(
        context.getString(R.string.association_broadcast_message),
        NotificationType.ASSOCIATION_FOLLOWERS,
        association.uid,
        { mapOf("title" to association.name, "body" to it) },
        showNotificationDialog,
        { showNotificationDialog = false })
  }

  AssociationProfileBottomSheet(
      showSheet,
      onClose = { showSheet = false },
      onEdit = onEdit,
      onOpenNotificationDialog = { showNotificationDialog = true })

  Box {
    PullRefreshIndicator(
        refreshing = refreshState,
        state = pullRefreshState,
        modifier = Modifier.align(Alignment.TopCenter))
  }
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
fun AssociationProfileContent(
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

  Column(
      modifier = Modifier.testTag(AssociationProfileTestTags.SCREEN).fillMaxWidth().padding(24.dp),
      verticalArrangement = Arrangement.spacedBy(16.dp)) {
        AssociationHeader(association!!, isFollowed, enableButton, onFollow)
        AssociationDescription(association!!)
        AssociationEvents(navigationAction, association!!, userViewModel, eventViewModel)
        AssociationMembers(associationViewModel, association!!.members, onMemberClick)

      }
}

/**
 * Composable element that contain the actions of the given association. It call all elements that
 * should be displayed on the screen, such as the header, the description, the events...
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

  Column(
      modifier =
          Modifier.testTag(AssociationProfileActionsTestTags.SCREEN)
              .fillMaxWidth()
              .padding(24.dp),
      verticalArrangement = Arrangement.spacedBy(16.dp)) {
        AssociationActionsHeader(
            association!!,
            isFollowed,
            enableButton,
            onFollow,
            onClickSaveButton = {
              associationViewModel.selectAssociation(association!!.uid)
              navigationAction.navigateTo(Screen.SAVE_ASSOCIATION)
            })
        AssociationActionsEvents(
            navigationAction,
            association!!,
            userViewModel,
            eventViewModel,
            searchViewModel = searchViewModel)
        AssociationActionsMembers(associationViewModel, user!!.uid, onMemberClick, searchViewModel)
      }
}

/**
 * Component that displays the users that are in the association that can be contacted. It displays
 * the title of the section and then displays the different users in the association.
 *
 * @param members (List<User>) : The list of users in the association that can be contacted
 */
@SuppressLint("StateFlowValueCalledInComposition")
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
                    val association = associationViewModel.selectedAssociation.value
                    val userRole =
                        association?.roles?.find {
                          it.uid == association.members.find { it.uid == user.value?.uid }?.roleUid
                        }

                    if (userRole != null) {
                      RoleBadge(userRole)
                    }
                  }
                }
              }
        }
      }
}

/**
 * Displays a list of members of an association and allows searching for members using a search bar.
 * When a member is selected from the search, the view scrolls to the respective member's card.
 * Additionally, it allows managing the association's roles.
 *
 * @param associationViewModel The ViewModel responsible for managing the association's data.
 * @param userUid The unique identifier of the current user.
 * @param onMemberClick A lambda function to be called when a member's card is clicked, passing the selected member's data.
 * @param searchViewModel The ViewModel responsible for managing member search functionality.
 */
@Composable
private fun AssociationActionsMembers(
    associationViewModel: AssociationViewModel,
    userUid: String,
    onMemberClick: (User) -> Unit,
    searchViewModel: SearchViewModel,
) {
  val context = LocalContext.current

  val association by associationViewModel.selectedAssociation.collectAsState()
  val members = association?.members
  val pagerState = rememberPagerState(initialPage = 0) { members?.size ?: 0 }
  val coroutineScope = rememberCoroutineScope()

  val searchBar: @Composable () -> Unit = {
    association?.let {
      MemberSearchBar(
          searchViewModel = searchViewModel,
          associationUid = it.uid,
          userUid = userUid,
          onMemberSelected = { selectedMember ->
            val targetPage = members?.indexOfFirst { it.uid == selectedMember.uid }
            if (targetPage != null && targetPage >= 0) {
              coroutineScope.launch { pagerState.animateScrollToPage(targetPage) }
            }
          },
          shouldCloseExpandable = false,
          onOutsideClickHandled = {})
    }
  }

  val cardContent: @Composable (Member) -> Unit = { member ->
    val user = associationViewModel.getUserFromMember(member).collectAsState()
    Box(
        modifier = Modifier.fillMaxWidth().padding(top = 10.dp),
        contentAlignment = Alignment.Center) {
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
                user.value?.firstName?.let { firstName ->
                  user.value?.lastName?.let { lastName ->
                    Text("$firstName $lastName")

                    // Role Badge
                    val association = associationViewModel.selectedAssociation.value
                    val userRole =
                        association?.roles?.find {
                          it.uid == association.members.find { it.uid == user.value?.uid }?.roleUid
                        }

                    if (userRole != null) {
                      RoleBadge(userRole)
                    }
                  }
                }
              }
        }
  }

  if (members != null) {
    SearchPagerSection(
        items = members,
        cardContent = { cardContent(it) },
        searchBar = searchBar,
        pagerState = pagerState)

    association?.let {
      RolesManagementScreen(it.roles, associationViewModel = associationViewModel)
    }
  }
}

/**
 * Displays the list of roles for an association and allows creating, editing, and deleting roles.
 * Each role is displayed as a clickable card, and users can perform actions like editing or deleting the role.
 * A dialog is shown for creating or editing roles, and another dialog confirms deletion.
 *
 * @param roles The list of roles for the association.
 * @param associationViewModel The ViewModel responsible for managing the association's data.
 */
@Composable
fun RolesManagementScreen(roles: List<Role>, associationViewModel: AssociationViewModel) {
  var showCreateRoleDialog by remember { mutableStateOf(false) }

  Column(Modifier.fillMaxSize().padding(16.dp)) {
    Text(text = "Roles", style = MaterialTheme.typography.headlineMedium)

    Spacer(modifier = Modifier.height(16.dp))

    roles.forEach { role -> RoleCard(role, associationViewModel) }

    Spacer(modifier = Modifier.height(16.dp))

    Button(onClick = { showCreateRoleDialog = true }) { Text(text = "Create New Role") }

    // Show dialog for creating a new role
    if (showCreateRoleDialog) {
      SaveRoleDialog(
          onDismiss = { showCreateRoleDialog = false },
          onCreateRole = { newRole -> showCreateRoleDialog = false },
          associationViewModel = associationViewModel)
    }
  }
}

/**
 * Displays the card for a specific role in the association, allowing users to edit or delete it.
 * The card shows the role's display name, color, and granted permissions. Users can click the card
 * to expand it and see more information. Editing and deleting a role triggers respective dialogs.
 *
 * @param role The role to be displayed.
 * @param associationViewModel The ViewModel responsible for managing the association's data.
 */
@Composable
fun RoleCard(role: Role, associationViewModel: AssociationViewModel) {
  var expanded by remember { mutableStateOf(false) }
  var showEditDialog by remember { mutableStateOf(false) }
  var showDeleteDialog by remember { mutableStateOf(false) }

    val context = LocalContext.current

  Card(
      modifier =
          Modifier.fillMaxWidth().padding(vertical = 8.dp).clickable { expanded = !expanded },
      elevation = CardDefaults.cardElevation(4.dp)) {
        Column(Modifier.fillMaxWidth().padding(16.dp)) {
          Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Box(modifier = Modifier.size(24.dp).background(Color(role.color)))

            Spacer(modifier = Modifier.width(8.dp))

            Text(
                text = role.displayName,
                style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier.weight(1f))

            Row {
              Icon(
                  imageVector = Icons.Default.Edit,
                  contentDescription = context.getString(R.string.association_profile_role_card_edit_role),
                  modifier = Modifier.size(24.dp).clickable { showEditDialog = true }.padding(4.dp))

              Icon(
                  imageVector = Icons.Default.Delete,
                  contentDescription = context.getString(R.string.association_profile_role_card_delete_role),
                  modifier =
                      Modifier.size(24.dp).clickable { showDeleteDialog = true }.padding(4.dp))
            }
          }

          if (expanded) {
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Permissions:",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.primary)

            Spacer(modifier = Modifier.height(4.dp))

            role.permissions.getGrantedPermissions().forEach { permission ->
              Text(
                  text = "- ${permission.stringName}",
                  style = MaterialTheme.typography.bodySmall,
                  modifier = Modifier.padding(start = 16.dp))
            }
          }
        }
      }

  // Edit Role Dialog
  if (showEditDialog) {
    SaveRoleDialog(
        onDismiss = { showEditDialog = false },
        onCreateRole = { updatedRole ->
          associationViewModel.selectedAssociation.value?.let { association ->
            associationViewModel.editRoleLocally(association.uid, updatedRole)
          }
          showEditDialog = false
        },
        associationViewModel = associationViewModel,
        initialRole = role
        )
  }

  // Delete Role Confirmation Dialog
  if (showDeleteDialog) {
    AlertDialog(
        onDismissRequest = { showDeleteDialog = false },
        confirmButton = {
          Button(
              onClick = {
                associationViewModel.selectedAssociation.value?.let { association ->
                  associationViewModel.deleteRoleLocally(association.uid, role)
                }
                showDeleteDialog = false
              }) {
                Text(context.getString(R.string.association_profile_role_card_delete))
              }
        },
        dismissButton = { TextButton(onClick = { showDeleteDialog = false }) { Text(context.getString(R.string.association_profile_role_card_cancel)) } },
        title = { Text(context.getString(R.string.association_profile_role_card_delete_role)) },
        text = { Text(context.getString(R.string.association_profile_role_card_sure_delete_role) +" '${role.displayName}'?") })
  }
}

/**
 * Displays a dialog to create or edit a role within the association.
 * If an existing role is passed, the dialog will allow editing the role's details, including the display name,
 * color, and permissions. If no existing role is passed, the dialog will create a new role.
 * Once the role is saved, it triggers the appropriate actions in the [associationViewModel].
 *
 * @param onDismiss A lambda function to be called when the dialog is dismissed.
 * @param onCreateRole A lambda function to be called after the role is created or updated, passing the role.
 * @param associationViewModel The ViewModel responsible for managing the association's data.
 * @param initialRole The initial role details to prefill the dialog fields (optional, null if creating a new role).
 */
@Composable
fun SaveRoleDialog(
    onDismiss: () -> Unit,
    onCreateRole: (Role) -> Unit,
    associationViewModel: AssociationViewModel,
    initialRole: Role? = null
) {
  var displayName by remember { mutableStateOf(TextFieldValue(initialRole?.displayName ?: "")) }
  val controller = rememberColorPickerController()
  var selectedColor by remember {
    mutableStateOf(Color(initialRole?.color ?: Color.White.toArgb().toLong()))
  }
  val selectedPermissions = remember {
    mutableStateListOf<PermissionType>().apply {
      initialRole?.permissions?.getGrantedPermissions()?.let { addAll(it) }
    }
  }
  val allPermissions = PermissionType.values()
    val context = LocalContext.current

  AlertDialog(
      onDismissRequest = onDismiss,
      confirmButton = {
        Button(
            onClick = {
              val colorInt = selectedColor.toArgb().toLong()
              val saveRole =
                  Role(
                      displayName = displayName.text,
                      permissions =
                          Permissions.PermissionsBuilder()
                              .addPermissions(selectedPermissions.toList())
                              .build(),
                      color = colorInt,
                      uid = initialRole?.uid ?: displayName.text
                      )
              associationViewModel.selectedAssociation.value?.let { association ->
                addEditRoleCloudFunction(
                    saveRole,
                    association.uid,
                    onSuccess = {
                      associationViewModel.addRoleLocally(association.uid, saveRole)
                    },
                    onError = { e -> Log.e("ADD_ROLE", "ERROR: $e") },
                    isNewRole = initialRole == null)
              }
              onCreateRole(saveRole)
            }) {
              Text(if (initialRole != null) context.getString(R.string.association_profile_save_role_dialog_save) else context.getString(R.string.association_profile_save_role_dialog_create))
            }
      },
      dismissButton = { TextButton(onClick = onDismiss) { Text(context.getString(R.string.association_profile_save_role_dialog_cancel)) } },
      title = { Text(if (initialRole != null) context.getString(R.string.association_profile_save_role_dialog_edit_role) else context.getString(R.string.association_profile_save_role_dialog_create_role)) },
      text = {
        Column(Modifier.fillMaxWidth()) {
          Column(Modifier.fillMaxWidth().padding(bottom = 16.dp)) {
            Text(text = context.getString(R.string.association_profile_save_role_dialog_display_name), style = MaterialTheme.typography.labelMedium)
            BasicTextField(
                value = displayName,
                onValueChange = { displayName = it },
                modifier = Modifier.fillMaxWidth().background(Color.LightGray).padding(8.dp))
          }

          Column(Modifier.fillMaxWidth().padding(vertical = 16.dp)) {
            Text(context.getString(R.string.association_profile_save_role_dialog_role_color), style = MaterialTheme.typography.labelMedium)
            HsvColorPicker(
                modifier = Modifier.fillMaxWidth().height(200.dp).padding(10.dp),
                controller = controller,
                onColorChanged = { colorEnvelope -> selectedColor = colorEnvelope.color },
                initialColor = selectedColor)
          }

          Text(text = context.getString(R.string.association_profile_save_role_dialog_permissions), style = MaterialTheme.typography.labelMedium)
          LazyColumn(modifier = Modifier.fillMaxWidth()) {
            items(allPermissions.size) { index ->
              val permission = allPermissions[index]
              Row(
                  modifier =
                      Modifier.fillMaxWidth()
                          .clickable {
                            if (selectedPermissions.contains(permission)) {
                              selectedPermissions.remove(permission)
                            } else {
                              selectedPermissions.add(permission)
                            }
                          }
                          .padding(8.dp),
                  verticalAlignment = Alignment.CenterVertically) {
                    Checkbox(
                        checked = selectedPermissions.contains(permission),
                        onCheckedChange = {
                          if (it) {
                            selectedPermissions.add(permission)
                          } else {
                            selectedPermissions.remove(permission)
                          }
                        })
                    Text(text = permission.stringName)
                  }
            }
          }
        }
      })
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
  var isSeeMoreClicked by remember { mutableStateOf(false) }
  val events by association.events.list.collectAsState()

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
          AssociationEventCard(
              navigationAction, event, userViewModel, eventViewModel, shouldBeEditable = false)
        }
      } else {
        AssociationEventCard(
            navigationAction, first, userViewModel, eventViewModel, shouldBeEditable = false)
      }
    }

    if (events.size > 1) {
      AssociationProfileSeeMoreButton(
          { isSeeMoreClicked = false }, { isSeeMoreClicked = true }, isSeeMoreClicked)
    }
  }
}

/**
 * Displays events related to an association, along with a search bar, event creation button,
 * and event details. The event list is sorted and displayed using a paginated view.
 * Users with appropriate permissions can create new events, and a search bar allows for event searching.
 *
 * @param navigationAction The navigation actions to navigate between screens.
 * @param association The association whose events are being displayed.
 * @param userViewModel The ViewModel responsible for managing user data.
 * @param eventViewModel The ViewModel responsible for managing event data.
 * @param searchViewModel The ViewModel responsible for managing event search functionality.
 */
@Composable
private fun AssociationActionsEvents(
    navigationAction: NavigationAction,
    association: Association,
    userViewModel: UserViewModel,
    eventViewModel: EventViewModel,
    searchViewModel: SearchViewModel
) {
  val context = LocalContext.current
  val isConnected = NetworkUtils.checkInternetConnection(context)

  val events by association.events.list.collectAsState()
  val user by userViewModel.user.collectAsState()

  if (user == null) {
    return
  }

  val isMember = association.members.any { it.uid == user!!.uid }
  val userRole =
      association?.roles?.find {
        it.uid == association.members.find { it.uid == user!!.uid }?.roleUid
      }

  val userPermissions = userRole?.permissions
  val hasAddEventsPermission =
      userPermissions?.hasPermission(PermissionType.ADD_EDIT_EVENTS) == true

  // Title
  Text(
      text = context.getString(R.string.association_profile_actions_events_text),
      modifier = Modifier.testTag(AssociationProfileActionsTestTags.SMALL_EVENT_TITLE),
      style = AppTypography.headlineLarge)

  // Add Event Button
  if (isMember && hasAddEventsPermission) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 0.dp),
        horizontalArrangement = Arrangement.Center) {
          Button(
              onClick = {
                if (isConnected) {
                  navigationAction.navigateTo(Screen.EVENT_CREATION)
                } else {
                  ToastUtils.showToast(context, context.getString(R.string.no_internet_connection))
                }
              },
              modifier = Modifier.testTag(AssociationProfileActionsTestTags.ADD_EVENT_BUTTON),
              contentPadding = ButtonDefaults.ButtonWithIconContentPadding) {
                Icon(
                    Icons.Filled.Add,
                    contentDescription =
                        context.getString(R.string.association_profile_add_event_button),
                    modifier = Modifier.size(ButtonDefaults.IconSize))
                Spacer(Modifier.size(ButtonDefaults.IconSpacing))
                Text(context.getString(R.string.association_profile_add_event_button))
              }
        }
  }

  // Sorted Events
  val sortedEvents = events.sortedBy { it.startDate }
  val pagerState = rememberPagerState { sortedEvents.size }
  val coroutineScope = rememberCoroutineScope()


  if (events.isNotEmpty()) {
    SearchPagerSection(
        items = sortedEvents,
        cardContent = { event ->
          // Each event card in the HorizontalPager
          AssociationEventCard(
              navigationAction = navigationAction,
              event = event,
              userViewModel = userViewModel,
              eventViewModel = eventViewModel,
              shouldBeEditable = hasAddEventsPermission)
        },
        searchBar = {
          EventSearchBar(
              searchViewModel = searchViewModel,
              onEventSelected = { selectedEvent ->
                val targetPage = sortedEvents.indexOfFirst { it.uid == selectedEvent.uid }
                if (targetPage >= 0) {
                  coroutineScope.launch { pagerState.animateScrollToPage(targetPage) }
                }
              },
              shouldCloseExpandable = false,
              onOutsideClickHandled = {})
        },
        pagerState = pagerState)
  }
}

/**
 * Displays a button that toggles between "See More" and "See Less" states.
 * This component is used to show or hide more content, depending on whether the button has been clicked.
 *
 * @param onSeeMore A lambda function that is triggered when the "See More" button is clicked.
 * @param onSeeLess A lambda function that is triggered when the "See Less" button is clicked.
 * @param isSeeMoreClicked A boolean flag indicating whether the "See More" button is clicked or not.
 */
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
 * Displays a single event inside a card format. This component is typically used in places like the home screen
 * to show event details, and it includes an option for editing the event based on the provided permissions.
 *
 * @param navigationAction The navigation actions to navigate between screens.
 * @param event The event to be displayed in the card.
 * @param userViewModel The ViewModel responsible for managing user data.
 * @param eventViewModel The ViewModel responsible for managing event data.
 * @param shouldBeEditable A flag indicating whether the event should be editable by the user.
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
      text = context.getString(R.string.association_profile_general_actions_text),
      modifier = Modifier.testTag(AssociationProfileActionsTestTags.EVENT_TITLE),
      style = AppTypography.headlineLarge)

  var showNotificationDialog by remember { mutableStateOf(false) }

  NotificationSender(
      context.getString(R.string.association_broadcast_message),
      NotificationType.ASSOCIATION_FOLLOWERS,
      association.uid,
      { mapOf("title" to association.name, "body" to it) },
      showNotificationDialog,
      { showNotificationDialog = false })

  Row(
      modifier =
          Modifier.fillMaxWidth().padding(vertical = 0.dp),
      horizontalArrangement = Arrangement.Center,
      verticalAlignment = Alignment.CenterVertically
      ) {
        Box(
            modifier = Modifier.widthIn(max = 300.dp)
            ) {
              Text(
                  text = context.getString(R.string.association_profile_header_broadcast_text),
                  style = AppTypography.bodyMedium,
                  modifier = Modifier.padding(end = 8.dp)
                  )
            }
        IconButton(
            onClick = { showNotificationDialog = true },
            modifier = Modifier.testTag(AssociationProfileActionsTestTags.BROADCAST_ICON_BUTTON).size(24.dp)) {
              Icon(
                  Icons.AutoMirrored.Filled.Send,
                  contentDescription = context.getString(R.string.association_profile_header_broadcast_button),
                  tint = MaterialTheme.colorScheme.primary
                  )
            }
      }

  Row(
      modifier =
          Modifier.fillMaxWidth().padding(vertical = 0.dp),
      horizontalArrangement = Arrangement.Center,
      verticalAlignment = Alignment.CenterVertically
      ) {
        Box(
            modifier = Modifier.widthIn(max = 300.dp)
            ) {
              Text(
                  text = context.getString(R.string.association_profile_header_edit_text),
                  style = AppTypography.bodyMedium,
                  modifier = Modifier.padding(end = 8.dp)
                  )
            }
        IconButton(
            onClick = { onClickSaveButton() },
            modifier = Modifier.testTag(AssociationProfileActionsTestTags.EDIT_BUTTON).size(24.dp)) {
              Icon(
                  Icons.Filled.Edit,
                  contentDescription = context.getString(R.string.association_profile_header_edit_button),
                  tint = MaterialTheme.colorScheme.primary
                  )
            }
      }
}
