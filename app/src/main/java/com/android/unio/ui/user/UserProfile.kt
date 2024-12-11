package com.android.unio.ui.user

import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.MoreVert
import androidx.compose.material.pullrefresh.PullRefreshIndicator
import androidx.compose.material.pullrefresh.pullRefresh
import androidx.compose.material.pullrefresh.rememberPullRefreshState
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.ModalBottomSheetProperties
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SuggestionChip
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.FilterQuality
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.core.net.toUri
import com.android.unio.R
import com.android.unio.model.association.AssociationViewModel
import com.android.unio.model.firestore.unregisterAllSnapshotListeners
import com.android.unio.model.strings.test_tags.user.UserProfileTestTags
import com.android.unio.model.user.User
import com.android.unio.model.user.UserViewModel
import com.android.unio.ui.association.AssociationSmall
import com.android.unio.ui.image.AsyncImageWrapper
import com.android.unio.ui.navigation.BottomNavigationMenu
import com.android.unio.ui.navigation.LIST_TOP_LEVEL_DESTINATION
import com.android.unio.ui.navigation.NavigationAction
import com.android.unio.ui.navigation.Route
import com.android.unio.ui.navigation.Screen
import com.android.unio.ui.theme.AppTypography
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import kotlinx.coroutines.launch

/**
 * Composable function that displays the user profile screen. This composable displays the content
 * of the UserProfileScreenScaffold.
 *
 * @param userViewModel The [UserViewModel] that provides the user data.
 * @param associationViewModel The [AssociationViewModel] that provides the association data.
 * @param navigationAction The [NavigationAction] that handles navigation.
 */
@Composable
fun UserProfileScreen(
    userViewModel: UserViewModel,
    associationViewModel: AssociationViewModel,
    navigationAction: NavigationAction
) {

  val context = LocalContext.current
  val user by userViewModel.user.collectAsState()

  if (user == null) {
    Log.e("UserProfileScreen", "User is null.")
    Toast.makeText(
            LocalContext.current,
            context.getString(R.string.user_profile_toast_error),
            Toast.LENGTH_SHORT)
        .show()
    return
  }

  val refreshState by userViewModel.refreshState

  UserProfileScreenScaffold(
      user!!,
      navigationAction,
      refreshState,
      onRefresh = { userViewModel.refreshUser() },
      onAssociationClick = {
        associationViewModel.selectAssociation(it)
        navigationAction.navigateTo(Screen.ASSOCIATION_PROFILE)
      })
}

/**
 * Composable function that displays the user profile screen scaffold. This composable displays the
 * top bar, bottom bar, and the content of the UserProfileScreenContent. It also allows the user to
 * refresh the user data by pulling down the screen.
 *
 * @param user The [User] object that contains the user data.
 * @param navigationAction The [NavigationAction] that handles navigation.
 * @param refreshState The state of the refresh.
 * @param onRefresh The function that refreshes the user data.
 * @param onAssociationClick The function that navigates to the association profile screen.
 */
@OptIn(ExperimentalMaterialApi::class, ExperimentalMaterial3Api::class)
@Composable
fun UserProfileScreenScaffold(
    user: User,
    navigationAction: NavigationAction,
    refreshState: Boolean,
    onRefresh: () -> Unit,
    onAssociationClick: (String) -> Unit
) {
  val context = LocalContext.current
  val pullRefreshState = rememberPullRefreshState(refreshing = refreshState, onRefresh = onRefresh)

  var showSheet by remember { mutableStateOf(false) }

  Scaffold(
      modifier = Modifier.testTag(UserProfileTestTags.SCREEN),
      topBar = {
        TopAppBar(
            title = { Text(context.getString(R.string.user_profile_your_profile_text)) },
            actions = {
              IconButton(
                  onClick = { showSheet = true },
                  modifier = Modifier.testTag(UserProfileTestTags.SETTINGS)) {
                    Icon(
                        Icons.Outlined.MoreVert,
                        contentDescription =
                            context.getString(R.string.user_profile_content_description_more))
                  }
            })
      },
      bottomBar = {
        BottomNavigationMenu(
            { navigationAction.navigateTo(it.route) }, LIST_TOP_LEVEL_DESTINATION, Route.MY_PROFILE)
      }) { padding ->
        if (refreshState) {
          Box(
              modifier = Modifier.fillMaxSize().padding(padding),
              contentAlignment = Alignment.Center) {
                CircularProgressIndicator(modifier = Modifier.width(64.dp))
              }
        } else {
          Box(
              modifier =
                  Modifier.padding(padding)
                      .pullRefresh(pullRefreshState)
                      .fillMaxHeight()
                      .verticalScroll(rememberScrollState())) {
                UserProfileScreenContent(
                    user,
                    onAssociationClick,
                    onClaimAssociationClick = {
                      navigationAction.navigateTo(Screen.CLAIM_ASSOCIATION_RIGHTS)
                    })
              }
        }
      }

  Box {
    PullRefreshIndicator(
        refreshing = refreshState,
        state = pullRefreshState,
        modifier = Modifier.align(Alignment.TopCenter))
  }

  UserProfileBottomSheet(showSheet, navigationAction) { showSheet = false }
}

/**
 * Composable function that displays the content of the user profile screen.
 *
 * @param user The [User] object that contains the user data.
 * @param onAssociationClick The function that navigates to the association profile screen.
 * @param onClaimAssociationClick The function that navigates to the claim association rights
 *   screen.
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun UserProfileScreenContent(
    user: User,
    onAssociationClick: (String) -> Unit,
    onClaimAssociationClick: () -> Unit
) {

  val context = LocalContext.current

  val uriHandler = LocalUriHandler.current

  val followedAssociations by user.followedAssociations.list.collectAsState()
  val joinedAssociations by user.joinedAssociations.list.collectAsState()

  Surface(
      modifier = Modifier.fillMaxWidth(),
  ) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.fillMaxWidth(0.7f).padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)) {
          Box(modifier = Modifier.size(100.dp).clip(CircleShape)) {
            AsyncImageWrapper(
                imageUri = user.profilePicture.toUri(),
                contentDescription =
                    context.getString(R.string.user_profile_content_description_pfp),
                contentScale = ContentScale.Crop,
                modifier =
                    Modifier.background(Color.Gray).testTag(UserProfileTestTags.PROFILE_PICTURE),
                filterQuality = FilterQuality.Medium)
          }

          // Display the user's name and biography.
          Text(
              user.firstName + " " + user.lastName,
              style = AppTypography.headlineLarge,
              modifier = Modifier.testTag(UserProfileTestTags.NAME))
          Text(
              user.biography,
              style = AppTypography.bodyMedium,
              textAlign = TextAlign.Center,
              modifier = Modifier.testTag(UserProfileTestTags.BIOGRAPHY))

          // Display the user's socials.
          FlowRow(horizontalArrangement = Arrangement.Center) {
            user.socials.forEach { userSocial ->
              IconButton(
                  onClick = { uriHandler.openUri(userSocial.getFullUrl()) },
                  modifier =
                      Modifier.testTag(
                          UserProfileTestTags.SOCIAL_BUTTON + userSocial.social.title)) {
                    Image(
                        modifier = Modifier.size(32.dp).wrapContentSize(),
                        painter = painterResource(userSocial.social.icon),
                        contentDescription = userSocial.social.title,
                        contentScale = ContentScale.Fit)
                  }
            }
          }

          // Display the user's interests in Chips.
          FlowRow(
              horizontalArrangement =
                  Arrangement.spacedBy(
                      space = 8.dp,
                      alignment = Alignment.CenterHorizontally,
                  ),
          ) {
            user.interests.forEach { interest ->
              SuggestionChip(
                  modifier = Modifier.testTag(UserProfileTestTags.INTEREST_CHIP + interest.name),
                  onClick = {},
                  label = {
                    Text(context.getString(interest.title), style = AppTypography.bodySmall)
                  })
            }
          }

          // Display the associations that the user is a member of.
          if (joinedAssociations.isNotEmpty()) {
            HorizontalDivider()

            Text(
                context.getString(R.string.user_profile_association_joined),
                style = AppTypography.headlineSmall)
            Column(
                modifier = Modifier.fillMaxWidth().testTag(UserProfileTestTags.JOINED_ASSOCIATIONS),
                verticalArrangement = Arrangement.spacedBy(4.dp),
            ) {
              joinedAssociations.map {
                AssociationSmall(it, user.uid) { onAssociationClick(it.uid) }
              }
            }
          }

          // Display the associations that the user is following.
          if (followedAssociations.isNotEmpty()) {
            HorizontalDivider()

            Text(
                context.getString(R.string.user_profile_following_associations_text),
                style = AppTypography.headlineSmall)
            Column(
                modifier = Modifier.testTag(UserProfileTestTags.FOLLOWED_ASSOCIATIONS),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
              followedAssociations.map {
                AssociationSmall(it, user.uid) { onAssociationClick(it.uid) }
              }
            }
          }
        }
  }
}

/**
 * Composable function that displays the user profile bottom sheet that display extra options for
 * the user.
 *
 * @param showSheet A boolean that indicates whether the bottom sheet should be displayed.
 * @param navigationAction The [NavigationAction] that handles navigation.
 * @param onClose The function that closes the bottom sheet.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UserProfileBottomSheet(
    showSheet: Boolean,
    navigationAction: NavigationAction,
    onClose: () -> Unit
) {

  val sheetState = rememberModalBottomSheetState()
  val scope = rememberCoroutineScope()

  val context = LocalContext.current

  if (showSheet) {
    ModalBottomSheet(
        modifier = Modifier.testTag(UserProfileTestTags.BOTTOM_SHEET),
        sheetState = sheetState,
        onDismissRequest = onClose,
        properties = ModalBottomSheetProperties(shouldDismissOnBackPress = true),
    ) {
      Column {
        TextButton(
            modifier = Modifier.fillMaxWidth().testTag(UserProfileTestTags.EDITION),
            onClick = {
              scope.launch {
                sheetState.hide()
                onClose()
                navigationAction.navigateTo(Screen.EDIT_PROFILE)
              }
            }) {
              Text(context.getString(R.string.user_profile_bottom_sheet_edit))
            }
        TextButton(
            modifier = Modifier.fillMaxWidth().testTag(UserProfileTestTags.USER_SETTINGS),
            onClick = {
              scope.launch {
                sheetState.hide()
                onClose()
                navigationAction.navigateTo(Screen.SETTINGS)
              }
            }) {
              Text(context.getString(R.string.user_profile_bottom_sheet_settings))
            }
          TextButton(
              modifier = Modifier.fillMaxWidth().testTag(UserProfileTestTags.CLAIM_ASSOCIATION),
              onClick = {
                  scope.launch {
                      sheetState.hide()
                      onClose()
                      navigationAction.navigateTo(Screen.CLAIM_ASSOCIATION_PRESIDENTIAL_RIGHTS)
                  }
              }) {
              Text(context.getString(R.string.user_claim_association_claim_president_rights))
          }
        TextButton(
            modifier = Modifier.fillMaxWidth().testTag(UserProfileTestTags.SIGN_OUT),
            onClick = {
              unregisterAllSnapshotListeners()
              Firebase.auth.signOut()
              scope.launch {
                sheetState.hide()
                onClose()
              }
            }) {
              Text(context.getString(R.string.user_profile_bottom_sheet_sign_out))
            }
      }
    }
  }
}
