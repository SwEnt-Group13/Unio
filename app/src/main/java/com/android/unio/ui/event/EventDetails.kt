package com.android.unio.ui.event

import android.annotation.SuppressLint
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.DirectionsWalk
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.outlined.BookmarkBorder
import androidx.compose.material.icons.outlined.LocationOn
import androidx.compose.material.icons.outlined.Share
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.android.unio.R
import com.android.unio.model.association.Association
import com.android.unio.model.association.AssociationCategory
import com.android.unio.model.event.EventListViewModel
import com.android.unio.model.firestore.firestoreReferenceListWith
import com.android.unio.model.user.User
import com.android.unio.model.user.UserViewModel
import com.android.unio.ui.navigation.NavigationAction
import com.android.unio.ui.theme.AppTypography
import com.android.unio.ui.theme.inversePrimaryLight
import com.android.unio.ui.theme.onPrimaryLight
import com.android.unio.ui.theme.primaryLight
import com.android.unio.ui.theme.secondaryLight
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

private const val DEBUG_MESSAGE = "<DEBUG> Not implemented yet"
private val DEBUG_LAMBDA: () -> Unit = {
  scope!!.launch {
    testSnackbar!!.showSnackbar(message = DEBUG_MESSAGE, duration = SnackbarDuration.Short)
  }
}

private val PLACEHOLDER_IMAGE_URL =
    "https://sidebarsydney.com.au/wp-content/themes/yootheme/cache/6d/Project_X_1920x1080-6d0c5833.jpeg"
private val ASSOCIATION_ICON_SIZE = 24.dp

private var testSnackbar: SnackbarHostState? = null
private var scope: CoroutineScope? = null

@OptIn(ExperimentalMaterial3Api::class)
@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@Composable
fun EventScreen(
    navigationAction: NavigationAction,
    eventId: String,
    eventListViewModel: EventListViewModel,
    userViewModel: UserViewModel // will be used later to show whether the event is saved
) {

  // mock associations before linking to backend
  val associations =
      listOf(
          Association(
              uid = "1",
              url = "https://www.acm.org/",
              name = "ACM",
              fullName = "Association for Computing Machinery",
              category = AssociationCategory.SCIENCE_TECH,
              description =
                  "ACM is the world's largest educational and scientific computing society.",
              members = User.firestoreReferenceListWith(listOf("1", "2")),
              image = "https://www.example.com/image.jpg",
              followersCount = 0),
          Association(
              uid = "2",
              url = "https://www.ieee.org/",
              name = "IEEE",
              fullName = "Institute of Electrical and Electronics Engineers",
              category = AssociationCategory.SCIENCE_TECH,
              description =
                  "IEEE is the world's largest technical professional organization dedicated to advancing technology for the benefit of humanity.",
              members = User.firestoreReferenceListWith(listOf("3", "4")),
              image = "https://www.example.com/image.jpg",
              followersCount = 0))

  val context = LocalContext.current
  testSnackbar = remember { SnackbarHostState() }
  scope = rememberCoroutineScope()
  Scaffold(
      modifier = Modifier.testTag("EventScreen"),
      snackbarHost = {
        SnackbarHost(
            hostState = testSnackbar!!,
            modifier = Modifier.testTag("eventSnackbarHost"),
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
            title = {},
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
              IconButton(modifier = Modifier.testTag("eventSaveButton"), onClick = DEBUG_LAMBDA) {
                Icon(
                    Icons.Outlined.BookmarkBorder,
                    contentDescription = context.getString(R.string.event_save_button_description))
              }
              IconButton(modifier = Modifier.testTag("eventShareButton"), onClick = DEBUG_LAMBDA) {
                Icon(
                    Icons.Outlined.Share,
                    contentDescription = context.getString(R.string.event_share_button_description))
              }
            })
      },
      content = { padding ->
        // Mock image before linking to backend
        Column(
            modifier = Modifier.testTag("eventDetailsPage").verticalScroll(rememberScrollState())) {
              AsyncImage(
                  PLACEHOLDER_IMAGE_URL,
                  "Event image",
                  placeholder = painterResource(R.drawable.weskic),
                  modifier = Modifier.fillMaxWidth().testTag("eventDetailsImage"))

              Column(
                  modifier =
                      Modifier.testTag("eventDetailsInformationCard")
                          .background(primaryLight)
                          .align(Alignment.CenterHorizontally)
                          .padding(12.dp)
                          .fillMaxWidth()) {
                    Text(
                        "<Event Name>",
                        modifier =
                            Modifier.testTag("eventTitle").align(Alignment.CenterHorizontally),
                        style = AppTypography.headlineLarge,
                        color = onPrimaryLight)

                    Row(modifier = Modifier.align(Alignment.Start)) {
                      for (i in associations.indices) {
                        Row(
                            modifier =
                                Modifier.testTag("eventOrganisingAssociation$i")
                                    .padding(end = 6.dp),
                            horizontalArrangement = Arrangement.Center) {
                              Image(
                                  painter =
                                      painterResource(
                                          id =
                                              R.drawable
                                                  .clic), // replace with actual association image
                                  contentDescription =
                                      context.getString(
                                          R.string.event_association_icon_description),
                                  contentScale = ContentScale.Crop,
                                  modifier =
                                      Modifier.size(ASSOCIATION_ICON_SIZE)
                                          .clip(CircleShape)
                                          .align(Alignment.CenterVertically)
                                          .testTag("associationLogo$i"))
                              Text(
                                  "<Association name>",
                                  modifier =
                                      Modifier.testTag("associationName$i").padding(start = 3.dp),
                                  style = AppTypography.bodySmall,
                                  color = onPrimaryLight)
                            }
                      }
                    }
                    Row {
                      Text(
                          "<Start Hour>",
                          modifier = Modifier.testTag("eventStartHour").weight(1f),
                          color = onPrimaryLight)
                      Text(
                          "<Event Date>",
                          modifier = Modifier.testTag("eventDate"),
                          color = onPrimaryLight)
                    }
                  }
              Column(modifier = Modifier.testTag("eventDetailsBody").padding(9.dp)) {
                Text(
                    "X places remaining",
                    modifier = Modifier.testTag("placesRemainingText"),
                    style = AppTypography.bodyLarge,
                    color = secondaryLight)
                Text(
                    "Lorem ipsum dolor sit amet, consectetur adipiscing elit, sed do eiusmod tempor incididunt ut labore et dolore magna aliqua. Ut enim ad minim veniam, quis nostrud exercitation ullamco laboris nisi ut aliquip ex ea commodo consequat. Duis aute irure dolor in reprehenderit in voluptate velit esse cillum dolore eu fugiat nulla pariatur. Excepteur sint occaecat cupidatat non proident, sunt in culpa qui officia deserunt mollit anim id est laborum.",
                    modifier = Modifier.testTag("eventDescription").padding(6.dp),
                    style = AppTypography.bodyMedium)
                Spacer(modifier = Modifier.weight(1f))

                Row(
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.align(Alignment.CenterHorizontally)) {
                      Text(
                          "<Event Location>",
                          modifier = Modifier.testTag("eventLocation").padding(end = 5.dp))
                      Button(
                          onClick = DEBUG_LAMBDA,
                          modifier = Modifier.testTag("mapButton").size(48.dp),
                          shape = CircleShape,
                          colors =
                              ButtonDefaults.buttonColors(
                                  containerColor = inversePrimaryLight,
                                  contentColor = primaryLight),
                          contentPadding = PaddingValues(0.dp)) {
                            Icon(
                                Icons.Outlined.LocationOn,
                                contentDescription =
                                    context.getString(R.string.event_location_button_description),
                            )
                          }
                    }
                Spacer(modifier = Modifier.height(12.dp))
                Button(
                    onClick = DEBUG_LAMBDA,
                    modifier =
                        Modifier.testTag("signUpButton")
                            .align(Alignment.CenterHorizontally)
                            .wrapContentWidth()
                            .height(56.dp),
                    shape = CircleShape,
                    colors =
                        ButtonDefaults.buttonColors(
                            containerColor = inversePrimaryLight, contentColor = primaryLight)) {
                      Icon(
                          Icons.AutoMirrored.Filled.DirectionsWalk,
                          contentDescription =
                              context.getString(R.string.event_signup_button_description),
                      )
                      Text(context.getString(R.string.event_sign_up))
                    }
                Spacer(modifier = Modifier.height(16.dp))
              }
            }
      })
}
