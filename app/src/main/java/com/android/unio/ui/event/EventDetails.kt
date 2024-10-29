package com.android.unio.ui.event

import android.annotation.SuppressLint
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.outlined.BookmarkBorder
import androidx.compose.material.icons.outlined.Share
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
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.lifecycle.viewmodel.compose.viewModel
import com.android.unio.R
import com.android.unio.model.event.EventListViewModel
import com.android.unio.model.user.UserViewModel
import com.android.unio.ui.association.AssociationProfileContent
import com.android.unio.ui.navigation.NavigationAction
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

private val DEBUG_MESSAGE = "<DEBUG> Not implemented yet"

private var testSnackbar: SnackbarHostState? = null
private var scope: CoroutineScope? = null

@OptIn(ExperimentalMaterial3Api::class)
@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@Composable
fun EventScreen(
    navigationAction: NavigationAction,
    eventId: String,
    eventListViewModel: EventListViewModel = viewModel(factory = EventListViewModel.Factory),
    userViewModel: UserViewModel
) {

    val event = eventListViewModel.findEventById(eventId)
    val context = LocalContext.current
    testSnackbar = remember { SnackbarHostState() }
    scope = rememberCoroutineScope()
    Scaffold(
        snackbarHost = {
            SnackbarHost(
                hostState = testSnackbar!!,
                modifier = Modifier.testTag("eventSnackbarHost"),
                snackbar = { data ->
                    Snackbar {
                        TextButton(
                            onClick = { testSnackbar!!.currentSnackbarData?.dismiss() },
                            modifier = Modifier.testTag("snackbarActionButton")
                        ) {
                            Text(text = DEBUG_MESSAGE)
                        }
                    }
                })
        },
        topBar = {
            TopAppBar(
                title = { Text("<Event Name>", modifier = Modifier.testTag("EventDetailsTitle")) },
                navigationIcon = {
                    IconButton(
                        onClick = { navigationAction.goBack() },
                        modifier = Modifier.testTag("goBackButton")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Outlined.ArrowBack,
                            contentDescription = context.getString(R.string.association_go_back)
                        )
                    }
                },
                actions = {
                    IconButton(modifier = Modifier.testTag("eventFollowButton"), onClick = {
                        scope!!.launch {
                            testSnackbar!!.showSnackbar(
                                message = DEBUG_MESSAGE,
                                duration = SnackbarDuration.Short
                            )
                        }
                    }) {
                        Icon(
                            Icons.Outlined.BookmarkBorder,
                            contentDescription = "Icon for sharing event"
                        )
                    }
                    IconButton(
                        modifier = Modifier.testTag("eventShareButton"),
                        onClick = {
                            scope!!.launch {
                                testSnackbar!!.showSnackbar(
                                    message = DEBUG_MESSAGE,
                                    duration = SnackbarDuration.Short
                                )
                            }
                        }) {
                        Icon(
                            Icons.Outlined.Share,
                            contentDescription = "Icon for sharing event"
                        )
                    }
                })
        },
        content = { padding -> AssociationProfileContent(padding, context, navigationAction) })

    Scaffold(modifier = Modifier.testTag("EventScreen")) { Text("Event screen") }
}
