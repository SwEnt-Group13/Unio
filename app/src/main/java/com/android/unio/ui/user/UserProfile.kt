package com.android.unio.ui.user

import android.annotation.SuppressLint
import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.fillMaxHeight
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
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.ModalBottomSheetProperties
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SuggestionChip
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.android.unio.model.association.Association
import com.android.unio.model.association.AssociationCategory
import com.android.unio.model.firestore.MockReferenceList
import com.android.unio.model.firestore.firestoreReferenceListWith
import com.android.unio.model.user.Interest
import com.android.unio.model.user.Social
import com.android.unio.model.user.User
import com.android.unio.model.user.UserSocial
import com.android.unio.model.user.UserViewModel
import com.android.unio.ui.association.AssociationSmall
import com.android.unio.ui.navigation.BottomNavigationMenu
import com.android.unio.ui.navigation.LIST_TOP_LEVEL_DESTINATION
import com.android.unio.ui.navigation.NavigationAction
import com.android.unio.ui.navigation.Route
import com.android.unio.ui.theme.AppTypography
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterialApi::class, ExperimentalMaterial3Api::class)
@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@Composable
fun UserProfileScreen(
    navigationAction: NavigationAction,
    userViewModel: UserViewModel = viewModel(factory = UserViewModel.Factory)
) {

  val user by userViewModel.user.collectAsState()

  val refreshState by userViewModel.refreshState
  val pullRefreshState =
      rememberPullRefreshState(
          refreshing = refreshState, onRefresh = { userViewModel.refreshUser() })

  var showSheet by remember { mutableStateOf(false) }

  Scaffold(
      modifier = Modifier.testTag("UserProfileScreen"),
      topBar = {
        TopAppBar(
            title = { Text("Your Profile") },
            actions = {
              IconButton(onClick = { showSheet = true }) {
                Icon(Icons.Outlined.MoreVert, contentDescription = "More")
              }
            })
      },
      bottomBar = {
        BottomNavigationMenu(
            { navigationAction.navigateTo(it.route) }, LIST_TOP_LEVEL_DESTINATION, Route.MY_PROFILE)
      }) { padding ->
        if (user != null) {
          Box(
              modifier =
                  Modifier.padding(padding)
                      .pullRefresh(pullRefreshState)
                      .fillMaxHeight()
                      .verticalScroll(rememberScrollState())) {
                UserProfileScreenContent(user!!)
              }
        } else {
          Box(
              modifier = Modifier.fillMaxWidth().fillMaxHeight().background(Color.White),
              contentAlignment = Alignment.Center) {
                CircularProgressIndicator(modifier = Modifier.width(64.dp))
              }
        }
      }

  Box {
    PullRefreshIndicator(
        refreshing = refreshState,
        state = pullRefreshState,
        modifier = Modifier.align(Alignment.TopCenter))
  }

  UserProfileBottomSheet(showSheet) { showSheet = false }
}

@OptIn(ExperimentalLayoutApi::class, ExperimentalMaterialApi::class)
@Composable
fun UserProfileScreenContent(user: User) {

  val uriHandler = LocalUriHandler.current
  val context = LocalContext.current

  val followedAssociations by user.followedAssociations.list.collectAsState()
  val joinedAssociations by user.joinedAssociations.list.collectAsState()

  Column(
      horizontalAlignment = Alignment.CenterHorizontally,
      modifier = Modifier.fillMaxWidth().padding(top = 16.dp),
      verticalArrangement = Arrangement.spacedBy(16.dp)) {
        AsyncImage(
            model = user.profilePicture,
            contentDescription = "Profile Picture",
            modifier =
                Modifier.size(100.dp)
                    .clip(CircleShape)
                    .background(Color.Gray)
                    .testTag("UserProfilePicture"))

        // Display the user's name and biography.
        Text(
            user.firstName + " " + user.lastName,
            style = AppTypography.headlineLarge,
            modifier = Modifier.testTag("UserProfileName"))
        Text(
            user.biography,
            style = AppTypography.bodyMedium,
            modifier = Modifier.testTag("UserProfileBiography"))

        // Display the user's socials.
        FlowRow(
            modifier = Modifier.fillMaxWidth(0.8f), horizontalArrangement = Arrangement.Center) {
              user.socials
                  .filter { it.social != Social.OTHER }
                  .forEach { userSocial ->
                    IconButton(
                        onClick = { uriHandler.openUri(userSocial.content) },
                        modifier = Modifier.testTag("UserProfileSocialButton")) {
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
            modifier = Modifier.fillMaxWidth(0.8f),
            horizontalArrangement =
                Arrangement.spacedBy(
                    space = 8.dp,
                    alignment = Alignment.CenterHorizontally,
                ),
        ) {
          user.interests.forEach { interest ->
            SuggestionChip(
                modifier = Modifier.testTag("UserProfileInterest"),
                onClick = {},
                label = { Text(interest.title, style = AppTypography.bodySmall) })
          }
        }

        Divider(modifier = Modifier.fillMaxWidth(0.8f))

        // Display the associations that the user is a member of.
        if (joinedAssociations.isNotEmpty()) {
          Text("Joined", style = AppTypography.headlineSmall)
          Column(
              modifier = Modifier.fillMaxWidth(0.8f).testTag("UserProfileJoinedAssociations"),
              verticalArrangement = Arrangement.spacedBy(8.dp),
          ) {
            joinedAssociations.map {
              AssociationSmall(it) {
                Toast.makeText(context, "Not yet implemented.", Toast.LENGTH_SHORT).show()
              }
            }
          }
        }

        Divider(modifier = Modifier.fillMaxWidth(0.8f))

        // Display the associations that the user is following.
        if (followedAssociations.isNotEmpty()) {
          Text("Following", style = AppTypography.headlineSmall)
          Column(
              modifier = Modifier.fillMaxWidth(0.8f).testTag("UserProfileFollowedAssociations"),
              verticalArrangement = Arrangement.spacedBy(8.dp),
          ) {
            followedAssociations.map {
              AssociationSmall(it) {
                Toast.makeText(context, "Not yet implemented.", Toast.LENGTH_SHORT).show()
              }
            }
          }
        }
      }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UserProfileBottomSheet(showSheet: Boolean, onClose: () -> Unit) {

  val sheetState = rememberModalBottomSheetState()
  val scope = rememberCoroutineScope()

  val context = LocalContext.current

  if (showSheet) {
    ModalBottomSheet(
        modifier = Modifier.testTag("UserProfileBottomSheet"),
        sheetState = sheetState,
        onDismissRequest = onClose,
        properties = ModalBottomSheetProperties(shouldDismissOnBackPress = true),
    ) {
      Column(modifier = Modifier.padding(start = 16.dp)) {
        TextButton(
            modifier = Modifier.fillMaxWidth(),
            onClick = {
              Toast.makeText(context, "Not yet implemented.", Toast.LENGTH_SHORT).show()
            }) {
              Text("Edit Profile")
            }
        TextButton(
            modifier = Modifier.fillMaxWidth(),
            onClick = {
              Toast.makeText(context, "Not yet implemented.", Toast.LENGTH_SHORT).show()
            }) {
              Text("Settings")
            }
        TextButton(
            modifier = Modifier.fillMaxWidth(),
            onClick = {
              Firebase.auth.signOut()
              scope.launch {
                sheetState.hide()
                onClose()
              }
            }) {
              Text("Sign Out")
            }
      }
    }
  }
}

val association1 =
    Association(
        uid = "1234",
        url = "https://www.epfl.ch",
        name = "EPFL",
        fullName = "École Polytechnique Fédérale de Lausanne",
        category = AssociationCategory.EPFL_BODIES,
        description = "EPFL is a research institute and university in Lausanne, Switzerland.",
        members = User.firestoreReferenceListWith(listOf("1234")),
        image = "https://www.epfl.ch/profile.jpg")

val user =
    User(
        uid = "1234",
        email = "john.doe@gmail.com",
        firstName = "John",
        lastName = "Doe",
        biography = "Hey I'm John.",
        followedAssociations = MockReferenceList(listOf(association1, association1)),
        joinedAssociations = MockReferenceList(listOf(association1, association1)),
        interests = listOf(Interest.GAMING, Interest.MUSIC),
        socials =
            listOf(
                UserSocial(Social.INSTAGRAM, "https://www.instagram.com/john.doe"),
                UserSocial(Social.SNAPCHAT, "https://www.snapchat.com/johndoe"),
                UserSocial(Social.TELEGRAM, "https://t.me/johndoe"),
                UserSocial(Social.WHATSAPP, "https://wa.me/johndoe"),
                UserSocial(Social.DISCORD, "https://discord.gg/johndoe"),
                UserSocial(Social.LINKEDIN, "https://www.linkedin.com/in/johndoe"),
                UserSocial(Social.WEBSITE, "https://john.doe.com")),
        profilePicture = "https://john.doe.com/profile.jpg",
        hasProvidedAccountDetails = true)

@Composable
@Preview
fun UserProfileScreenContentPreview() {
  UserProfileScreenContent(user)
}
