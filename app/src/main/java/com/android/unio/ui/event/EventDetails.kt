package com.android.unio.ui.event

import android.annotation.SuppressLint
import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
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
import androidx.compose.material.icons.outlined.LocationOn
import androidx.compose.material.icons.outlined.Share
import androidx.compose.material.icons.rounded.Favorite
import androidx.compose.material.icons.rounded.FavoriteBorder
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.core.net.toUri
import coil.compose.AsyncImage
import com.android.unio.R
import com.android.unio.model.event.Event
import com.android.unio.model.event.EventUtils.formatTimestamp
import com.android.unio.model.event.EventViewModel
import com.android.unio.model.user.UserViewModel
import com.android.unio.ui.navigation.NavigationAction
import com.android.unio.ui.theme.AppTypography
import java.text.SimpleDateFormat
import java.util.Locale
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

private const val DEBUG_MESSAGE = "<DEBUG> Not implemented yet"
private val DEBUG_LAMBDA: () -> Unit = {
  scope!!.launch {
    testSnackbar!!.showSnackbar(message = DEBUG_MESSAGE, duration = SnackbarDuration.Short)
  }
}

private val ASSOCIATION_ICON_SIZE = 24.dp

private var testSnackbar: SnackbarHostState? = null
private var scope: CoroutineScope? = null

@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@Composable
fun EventScreen(
    navigationAction: NavigationAction,
    eventViewModel: EventViewModel,
    userViewModel: UserViewModel
) {

  val event by eventViewModel.selectedEvent.collectAsState()
  val user by userViewModel.user.collectAsState()

  if (event == null || user == null) {
    Log.e("EventScreen", "Event or user is null")
    Toast.makeText(LocalContext.current, "An error occurred.", Toast.LENGTH_SHORT).show()
    return
  }

  var isSaved by remember { mutableStateOf(user!!.savedEvents.contains(event!!.uid)) }

  val onClickSaveButton = {
    if (isSaved) {
      userViewModel.unSaveEventForCurrentUser(event!!.uid) { isSaved = false }
    } else {
      userViewModel.saveEventForCurrentUser(event!!.uid) { isSaved = true }
    }
    userViewModel.updateUserDebounced(user!!)
  }

  EventScreenScaffold(navigationAction, event!!, isSaved, onClickSaveButton)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EventScreenScaffold(
    navigationAction: NavigationAction,
    event: Event,
    isSaved: Boolean,
    onClickSaveButton: () -> Unit
) {
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
              IconButton(
                  modifier = Modifier.testTag("eventSaveButton"), onClick = onClickSaveButton) {
                    Icon(
                        imageVector =
                            if (isSaved) Icons.Rounded.Favorite else Icons.Rounded.FavoriteBorder,
                        contentDescription = if (isSaved) "Saved" else "Not saved",
                        tint =
                            if (isSaved) MaterialTheme.colorScheme.error
                            else LocalContentColor.current)
                  }
              IconButton(modifier = Modifier.testTag("eventShareButton"), onClick = DEBUG_LAMBDA) {
                Icon(
                    Icons.Outlined.Share,
                    contentDescription = context.getString(R.string.event_share_button_description))
              }
            })
      },
      content = { padding -> EventScreenContent(event, padding) })
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun EventScreenContent(event: Event, padding: PaddingValues) {
  val context = LocalContext.current
  Column(
      modifier =
          Modifier.testTag("eventDetailsPage")
              .verticalScroll(rememberScrollState())
              .padding(padding)) {
        AsyncImage(
            event.image.toUri(),
            context.getString(R.string.event_image_description),
            placeholder = painterResource(R.drawable.no_picture_found),
            modifier = Modifier.fillMaxSize().testTag("eventDetailsImage"))

        Column(
            modifier =
                Modifier.testTag("eventDetailsInformationCard")
                    .background(MaterialTheme.colorScheme.primary)
                    .align(Alignment.CenterHorizontally)
                    .padding(12.dp)
                    .fillMaxWidth()) {
              Text(
                  event.title,
                  modifier = Modifier.testTag("eventTitle").align(Alignment.CenterHorizontally),
                  style = AppTypography.headlineLarge,
                  color = MaterialTheme.colorScheme.onPrimary)

              Row(modifier = Modifier.align(Alignment.Start)) {
                val associations by event.organisers.list.collectAsState()
                for (i in associations.indices) {
                  Row(
                      modifier =
                          Modifier.testTag("eventOrganisingAssociation$i").padding(end = 6.dp),
                      horizontalArrangement = Arrangement.Center) {
                        AsyncImage(
                            associations[i].image.toUri(),
                            context.getString(R.string.event_association_icon_description),
                            placeholder = painterResource(R.drawable.weskic),
                            modifier =
                                Modifier.size(ASSOCIATION_ICON_SIZE)
                                    .clip(CircleShape)
                                    .align(Alignment.CenterVertically)
                                    .testTag("associationLogo$i"),
                            contentScale = ContentScale.Crop,
                        )

                        Text(
                            associations[i].name,
                            modifier = Modifier.testTag("associationName$i").padding(start = 3.dp),
                            style = AppTypography.bodySmall,
                            color = MaterialTheme.colorScheme.onPrimary)
                      }
                }
              }
              Row {
                Text(
                    formatTimestamp(event.date, SimpleDateFormat("HH:mm", Locale.getDefault())),
                    modifier = Modifier.testTag("eventStartHour").weight(1f),
                    color = MaterialTheme.colorScheme.onPrimary)
                Text(
                    formatTimestamp(event.date, SimpleDateFormat("dd/MM", Locale.getDefault())),
                    modifier = Modifier.testTag("eventDate"),
                    color = MaterialTheme.colorScheme.onPrimary)
              }
            }
        Column(modifier = Modifier.testTag("eventDetailsBody").padding(9.dp)) {
          Text(
              "X places remaining",
              modifier = Modifier.testTag("placesRemainingText"),
              style = AppTypography.bodyLarge,
              color = MaterialTheme.colorScheme.secondary)
          Text(
              event.description,
              modifier = Modifier.testTag("eventDescription").padding(6.dp),
              style = AppTypography.bodyMedium)

          Spacer(modifier = Modifier.height(10.dp))
          Column(modifier = Modifier.fillMaxSize(), verticalArrangement = Arrangement.Bottom) {
            FlowRow(
                horizontalArrangement = Arrangement.Center,
                verticalArrangement = Arrangement.Center,
                modifier = Modifier.align(Alignment.CenterHorizontally).wrapContentWidth()) {
                  Text(
                      event.location.name,
                      modifier = Modifier.testTag("eventLocation").padding(end = 5.dp))
                  Button(
                      onClick = DEBUG_LAMBDA,
                      modifier = Modifier.testTag("mapButton").size(48.dp),
                      shape = CircleShape,
                      colors =
                          ButtonDefaults.buttonColors(
                              containerColor = MaterialTheme.colorScheme.inversePrimary,
                              contentColor = MaterialTheme.colorScheme.primary),
                      contentPadding = PaddingValues(0.dp)) {
                        Icon(
                            Icons.Outlined.LocationOn,
                            contentDescription =
                                context.getString(R.string.event_location_button_description),
                        )
                      }
                }
            Spacer(modifier = Modifier.height(20.dp))
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
                        containerColor = MaterialTheme.colorScheme.inversePrimary,
                        contentColor = MaterialTheme.colorScheme.primary)) {
                  Icon(
                      Icons.AutoMirrored.Filled.DirectionsWalk,
                      contentDescription =
                          context.getString(R.string.event_signup_button_description),
                  )
                  Text(context.getString(R.string.event_sign_up))
                }
          }
        }
      }
}
