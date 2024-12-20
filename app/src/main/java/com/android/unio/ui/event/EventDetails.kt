package com.android.unio.ui.event

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.util.Log
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.lazy.staggeredgrid.LazyVerticalStaggeredGrid
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridCells
import androidx.compose.foundation.lazy.staggeredgrid.itemsIndexed
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PagerState
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.outlined.LocationOn
import androidx.compose.material.icons.outlined.MoreVert
import androidx.compose.material.icons.rounded.Favorite
import androidx.compose.material.icons.rounded.FavoriteBorder
import androidx.compose.material.pullrefresh.PullRefreshIndicator
import androidx.compose.material.pullrefresh.pullRefresh
import androidx.compose.material.pullrefresh.rememberPullRefreshState
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.core.net.toUri
import com.android.unio.R
import com.android.unio.model.association.Association
import com.android.unio.model.event.Event
import com.android.unio.model.event.EventUserPicture
import com.android.unio.model.event.EventUtils.formatTimestamp
import com.android.unio.model.event.EventViewModel
import com.android.unio.model.map.MapViewModel
import com.android.unio.model.notification.NotificationType
import com.android.unio.model.notification.NotificationWorker
import com.android.unio.model.notification.UnioNotification
import com.android.unio.model.preferences.AppPreferences
import com.android.unio.model.strings.FormatStrings.DAY_MONTH_FORMAT
import com.android.unio.model.strings.FormatStrings.HOUR_MINUTE_FORMAT
import com.android.unio.model.strings.NotificationStrings.EVENT_REMINDER_CHANNEL_ID
import com.android.unio.model.strings.test_tags.event.EventDetailsTestTags
import com.android.unio.model.user.User
import com.android.unio.model.user.UserViewModel
import com.android.unio.model.utils.NetworkUtils
import com.android.unio.ui.components.NotificationSender
import com.android.unio.ui.event.overlay.PictureOverlay
import com.android.unio.ui.image.AsyncImageWrapper
import com.android.unio.ui.navigation.NavigationAction
import com.android.unio.ui.navigation.Screen
import com.android.unio.ui.navigation.SmoothTopBarNavigationMenu
import com.android.unio.ui.theme.AppTypography
import com.android.unio.ui.utils.ToastUtils
import com.google.firebase.Timestamp
import java.text.SimpleDateFormat
import java.util.Locale
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import me.zhanghai.compose.preference.LocalPreferenceFlow

private const val DEBUG_MESSAGE = "<DEBUG> Not implemented yet"
private val DEBUG_LAMBDA: () -> Unit = {
  scope!!.launch {
    testSnackbar!!.showSnackbar(message = DEBUG_MESSAGE, duration = SnackbarDuration.Short)
  }
}

private val ASSOCIATION_ICON_SIZE = 24.dp

private var testSnackbar: SnackbarHostState? = null
private var scope: CoroutineScope? = null

/**
 * A screen that displays the details of an event. This screen is filled with EventScreenScaffold.
 *
 * @param navigationAction The navigation action to use.
 * @param eventViewModel The [EventViewModel] to use.
 * @param userViewModel The [UserViewModel] to use.
 * @param mapViewModel The [MapViewModel] to use.
 */
@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@Composable
fun EventScreen(
    navigationAction: NavigationAction,
    eventViewModel: EventViewModel,
    userViewModel: UserViewModel,
    mapViewModel: MapViewModel,
) {

  val event by eventViewModel.selectedEvent.collectAsState()

  val user by userViewModel.user.collectAsState()

  if (event == null || user == null) {
    Log.e("EventScreen", "Event or user is null")
    println(event == null)
    println(user == null)
    Toast.makeText(LocalContext.current, "An error occurred.", Toast.LENGTH_SHORT).show()
    return
  }
  val organisers by event!!.organisers.list.collectAsState()

  EventScreenScaffold(
      navigationAction, mapViewModel, event!!, organisers, eventViewModel, userViewModel)
}

/**
 * A scaffold for the event screen.
 *
 * @param navigationAction The navigation action to use.
 * @param mapViewModel The [MapViewModel] to use.
 * @param event The event to display.
 * @param organisers The list of associations organizing the event.
 * @param eventViewModel The [EventViewModel] to use.
 * @param userViewModel The [UserViewModel] to use.
 */
@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterialApi::class)
@Composable
fun EventScreenScaffold(
    navigationAction: NavigationAction,
    mapViewModel: MapViewModel,
    event: Event,
    organisers: List<Association>,
    eventViewModel: EventViewModel,
    userViewModel: UserViewModel
) {

  val tabList = listOf("Description", "Event pictures")
  val pagerState = rememberPagerState(initialPage = 0) { tabList.size }
  val context = LocalContext.current
  var showSheet by remember { mutableStateOf(false) }
  val user by userViewModel.user.collectAsState()

  val refreshState by eventViewModel.refreshState
  val pullRefreshState =
      rememberPullRefreshState(
          refreshing = refreshState, onRefresh = { eventViewModel.refreshEvent() })

  var showNotificationDialog by remember { mutableStateOf(false) }
  testSnackbar = remember { SnackbarHostState() }
  scope = rememberCoroutineScope()
  Scaffold(
      floatingActionButton = {
        if (pagerState.currentPage == 1) {
          EventDetailsPicturePicker(event, eventViewModel, user!!) // Asserted non null above
        }
      },
      modifier = Modifier.testTag(EventDetailsTestTags.SCREEN),
      snackbarHost = {
        SnackbarHost(
            hostState = testSnackbar!!,
            modifier = Modifier.testTag(EventDetailsTestTags.SNACKBAR_HOST),
            snackbar = { _ ->
              Snackbar {
                TextButton(
                    onClick = { testSnackbar!!.currentSnackbarData?.dismiss() },
                    modifier = Modifier.testTag(EventDetailsTestTags.SNACKBAR_ACTION_BUTTON)) {
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
                  modifier = Modifier.testTag(EventDetailsTestTags.GO_BACK_BUTTON)) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Outlined.ArrowBack,
                        contentDescription = context.getString(R.string.association_go_back))
                  }
            },
            actions = {
              EventSaveButton(event, eventViewModel, userViewModel)
              IconButton(
                  modifier = Modifier.testTag(EventDetailsTestTags.SHARE_BUTTON),
                  onClick = { showSheet = true }) {
                    Icon(
                        Icons.Outlined.MoreVert,
                        contentDescription =
                            context.getString(R.string.event_more_button_description))
                  }
            })
      }) { padding ->
        Box(
            modifier =
                Modifier.padding(padding)
                    .pullRefresh(pullRefreshState)
                    .fillMaxHeight()
                    .verticalScroll(rememberScrollState())) {
              EventScreenContent(
                  navigationAction,
                  mapViewModel,
                  eventViewModel,
                  event,
                  user!!,
                  organisers,
                  pagerState,
                  tabList)
            }
      }

  NotificationSender(
      dialogTitle = context.getString(R.string.event_send_notification),
      notificationType = NotificationType.EVENT_SAVERS,
      topic = event.uid,
      notificationContent = { mapOf("title" to event.title, "body" to it) },
      showNotificationDialog = showNotificationDialog,
      onClose = { showNotificationDialog = false })

  EventDetailsBottomSheet(showSheet, { showNotificationDialog = true }) { showSheet = false }

  Box {
    PullRefreshIndicator(
        refreshing = refreshState,
        state = pullRefreshState,
        modifier = Modifier.align(Alignment.TopCenter))
  }
}

/**
 * The content of the event screen.
 *
 * @param navigationAction The navigation action to use.
 * @param mapViewModel The [MapViewModel] to use.
 * @param event The event to display.
 * @param organisers The list of associations organizing the event.
 * @param pagerState The PagerState of the Horizontal menu
 */
@Composable
fun EventScreenContent(
    navigationAction: NavigationAction,
    mapViewModel: MapViewModel,
    eventViewModel: EventViewModel,
    event: Event,
    user: User,
    organisers: List<Association>,
    pagerState: PagerState,
    tabList: List<String>
) {
  val context = LocalContext.current
  Column(modifier = Modifier.testTag(EventDetailsTestTags.DETAILS_PAGE).fillMaxSize()) {
    Column(modifier = Modifier.height(200.dp)) {
      AsyncImageWrapper(
          imageUri = event.image.toUri(),
          contentDescription = context.getString(R.string.event_image_description),
          modifier = Modifier.fillMaxWidth().testTag(EventDetailsTestTags.DETAILS_IMAGE),
          contentScale = ContentScale.Crop,
          placeholderResourceId = R.drawable.placeholder_pictures)
    }

    EventInformationCard(event, organisers, context)

    SmoothTopBarNavigationMenu(tabList, pagerState)
    HorizontalPager(
        state = pagerState,
        modifier =
            Modifier.fillMaxSize()
                .padding(9.dp)
                .testTag(EventDetailsTestTags.EVENT_DETAILS_PAGER)) { page ->
          EventDetailsBody(
              navigationAction, mapViewModel, eventViewModel, event, user, context, page)
        }
  }
}

/**
 * A card that displays the information of the event.
 *
 * @param event The event to display.
 * @param organisers The list of associations organizing the event.
 * @param context The context to use.
 */
@Composable
fun EventInformationCard(event: Event, organisers: List<Association>, context: Context) {
  Column(
      modifier =
          Modifier.testTag(EventDetailsTestTags.DETAILS_INFORMATION_CARD)
              .background(MaterialTheme.colorScheme.primary)
              .padding(12.dp)
              .fillMaxWidth(),
      horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            event.title,
            modifier =
                Modifier.testTag(EventDetailsTestTags.TITLE).align(Alignment.CenterHorizontally),
            style = AppTypography.displayMedium,
            color = MaterialTheme.colorScheme.onPrimary)

        Row(modifier = Modifier.align(Alignment.Start)) {
          for (i in organisers.indices) {
            Row(
                modifier =
                    Modifier.testTag("${EventDetailsTestTags.ORGANIZING_ASSOCIATION}$i")
                        .padding(end = 6.dp),
                horizontalArrangement = Arrangement.Center) {
                  AsyncImageWrapper(
                      imageUri = organisers[i].image.toUri(),
                      contentDescription =
                          context.getString(R.string.event_association_icon_description),
                      modifier =
                          Modifier.size(ASSOCIATION_ICON_SIZE)
                              .clip(RoundedCornerShape(5.dp))
                              .align(Alignment.CenterVertically)
                              .testTag("${EventDetailsTestTags.ASSOCIATION_LOGO}$i"),
                      placeholderResourceId = R.drawable.adec,
                      filterQuality = FilterQuality.None,
                      contentScale = ContentScale.Crop)

                  Text(
                      organisers[i].name,
                      modifier =
                          Modifier.testTag("${EventDetailsTestTags.ASSOCIATION_NAME}$i")
                              .padding(start = 3.dp),
                      style = AppTypography.bodySmall,
                      color = MaterialTheme.colorScheme.onPrimary)
                }
          }
        }
        EventDate(event)
      }
}

/**
 * A row that displays the date of the event.
 *
 * @param event The event to display.
 */
@Composable
fun EventDate(event: Event) {
  Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
    val formattedStartDateDay = remember {
      formatTimestamp(event.startDate, SimpleDateFormat(DAY_MONTH_FORMAT, Locale.getDefault()))
    }
    val formattedEndDateDay = remember {
      formatTimestamp(event.endDate, SimpleDateFormat(DAY_MONTH_FORMAT, Locale.getDefault()))
    }
    val formattedStartDateHour = remember {
      formatTimestamp(event.startDate, SimpleDateFormat(HOUR_MINUTE_FORMAT, Locale.getDefault()))
    }
    val formattedEndDateHour = remember {
      formatTimestamp(event.endDate, SimpleDateFormat(HOUR_MINUTE_FORMAT, Locale.getDefault()))
    }
    if (formattedStartDateDay == formattedEndDateDay) {
      // event starts and ends on the same day
      Text(
          "$formattedStartDateHour - $formattedEndDateHour",
          modifier = Modifier.testTag(EventDetailsTestTags.HOUR),
          color = MaterialTheme.colorScheme.onPrimary)

      Text(
          formattedStartDateDay,
          modifier = Modifier.testTag(EventDetailsTestTags.START_DATE),
          color = MaterialTheme.colorScheme.onPrimary)
    } else {
      Text(
          "$formattedStartDateDay - $formattedStartDateHour",
          modifier = Modifier.testTag(EventDetailsTestTags.START_DATE),
          color = MaterialTheme.colorScheme.onPrimary)

      Text(
          "$formattedEndDateDay - $formattedEndDateHour",
          modifier = Modifier.testTag(EventDetailsTestTags.END_DATE),
          color = MaterialTheme.colorScheme.onPrimary)
    }
  }
}

/**
 * The body of the event details.
 *
 * @param navigationAction The navigation action to use.
 * @param mapViewModel The [MapViewModel] to use.
 * @param event The event to display.
 * @param context The context to use.
 * @param page: The index of the current page of the horizontal menu.
 */
@Composable
fun EventDetailsBody(
    navigationAction: NavigationAction,
    mapViewModel: MapViewModel,
    eventViewModel: EventViewModel,
    event: Event,
    user: User,
    context: Context,
    page: Int
) {
  if (page == 0) {
    EventDetailsDescriptionTab(navigationAction, mapViewModel, event, context)
  } else if (page == 1) {
    EventDetailsPicturesTab(event, user, context, eventViewModel)
  }
}

/**
 * The second page of the EventDetails horizontal scroll menu.
 *
 * @param event The [Event] to display.
 * @param user The logged in [User]
 * @param context The local [Context]
 */
@Composable
fun EventDetailsPicturesTab(
    event: Event,
    user: User,
    context: Context,
    eventViewModel: EventViewModel
) {

  val eventPictures by event.eventPictures.list.collectAsState()
  var showFullScreen by remember { mutableStateOf(false) }
  var selectedPictureUri by remember { mutableStateOf(Uri.EMPTY) }
  val pagerState = rememberPagerState { eventPictures.size }
  val scope = rememberCoroutineScope()

  if (event.startDate.seconds > Timestamp.now().seconds) {
    Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
      Text(
          context.getString(R.string.event_pictures_before_start_date),
          modifier = Modifier.testTag(EventDetailsTestTags.EVENT_NOT_STARTED_TEXT))
    }
  } else if (eventPictures.isEmpty()) {
    Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
      Text(
          context.getString(R.string.event_no_user_pictures),
          modifier = Modifier.testTag(EventDetailsTestTags.EVENT_NO_PICTURES_TEXT))
    }
  } else {
    LazyVerticalStaggeredGrid(
        userScrollEnabled = false,
        columns = StaggeredGridCells.Adaptive(100.dp),
        modifier =
            Modifier.fillMaxWidth()
                .heightIn(max = Short.MAX_VALUE.toInt().dp)
                .testTag(EventDetailsTestTags.GALLERY_GRID)) {
          itemsIndexed(
              eventPictures.sortedWith(
                  compareBy<EventUserPicture> { it.uid }
                      .thenByDescending { it.likes.uids.size })) { index, item ->
                println(item.likes.uids.size)
                AsyncImageWrapper(
                    item.image.toUri(),
                    contentDescription =
                        context.getString(R.string.event_details_user_picture_content_description),
                    filterQuality = FilterQuality.Medium,
                    placeholderResourceId = 0,
                    contentScale = ContentScale.Fit,
                    modifier =
                        Modifier.padding(3.dp)
                            .clip(RoundedCornerShape(10))
                            .testTag(EventDetailsTestTags.USER_EVENT_PICTURE + item.uid)
                            .clickable {
                              if (eventPictures[index].author.element.value == null) {
                                eventPictures[index].author.fetch()
                              }
                              scope.launch { pagerState.scrollToPage(index) }
                              showFullScreen = true
                            })
              }
        }
    if (showFullScreen) {
      PictureOverlay(
          onDismiss = {
            selectedPictureUri = Uri.EMPTY
            showFullScreen = false
          },
          pagerState,
          eventPictures,
          eventViewModel,
          user)
    }
  }
}
/**
 * The first page of the EventDetails horizontal scroll menu.
 *
 * @param navigationAction The navigation action to use.
 * @param mapViewModel The [MapViewModel] to use.
 * @param event The event to display.
 * @param context The context to use.
 */
@Composable
fun EventDetailsDescriptionTab(
    navigationAction: NavigationAction,
    mapViewModel: MapViewModel,
    event: Event,
    context: Context
) {
  Column(
      modifier =
          Modifier.testTag(EventDetailsTestTags.DETAILS_BODY).fillMaxHeight().padding(top = 30.dp),
      verticalArrangement = Arrangement.spacedBy(30.dp)) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
          val priceStr =
              if (event.price > 0) {
                "${context.getString(R.string.event_price)}${event.price}.-"
              } else
                  "${context.getString(R.string.event_price)} ${context.getString(R.string.event_price_free)}"

          Text(
              priceStr,
              modifier = Modifier.testTag(EventDetailsTestTags.PRICE_TEXT),
              style = AppTypography.bodyLarge,
              color = MaterialTheme.colorScheme.onPrimaryContainer)

          val placesStr =
              if (event.maxNumberOfPlaces >= 0) {
                "${event.numberOfSaved}/${event.maxNumberOfPlaces}${context.getString(R.string.event_places_remaining)}"
              } else ""
          Text(
              placesStr,
              modifier = Modifier.testTag(EventDetailsTestTags.PLACES_REMAINING_TEXT),
              style = AppTypography.bodyLarge,
              color = MaterialTheme.colorScheme.onPrimaryContainer)
        }

        Text(
            event.description,
            modifier = Modifier.testTag(EventDetailsTestTags.DESCRIPTION).padding(6.dp),
            style = AppTypography.bodyMedium)
        OutlinedButton(
            onClick = {
              mapViewModel.setHighlightedEvent(event.uid, event.location)
              navigationAction.navigateTo(Screen.MAP)
            },
            modifier =
                Modifier.testTag(EventDetailsTestTags.MAP_BUTTON)
                    .align(Alignment.CenterHorizontally)
                    .wrapContentSize(),
            shape = CircleShape) {
              Text(
                  event.location.name,
                  modifier =
                      Modifier.testTag(EventDetailsTestTags.LOCATION_ADDRESS).padding(end = 5.dp))
              Icon(
                  Icons.Outlined.LocationOn,
                  contentDescription =
                      context.getString(R.string.event_location_button_description),
              )
            }
      }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EventDetailsBottomSheet(
    showSheet: Boolean,
    onOpenNotificationDialog: () -> Unit,
    onClose: () -> Unit
) {
  val sheetState = rememberModalBottomSheetState()
  val scope = rememberCoroutineScope()

  val context = LocalContext.current

  if (showSheet) {
    ModalBottomSheet(
        modifier = Modifier.testTag(EventDetailsTestTags.BOTTOM_SHEET),
        sheetState = sheetState,
        onDismissRequest = onClose,
        properties = ModalBottomSheetProperties(shouldDismissOnBackPress = true),
    ) {
      Column {
        TextButton(
            modifier = Modifier.fillMaxWidth().testTag(EventDetailsTestTags.SEND_NOTIFICATION),
            onClick = {
              scope.launch {
                sheetState.hide()
                onClose()
                onOpenNotificationDialog()
              }
            }) {
              Text(context.getString(R.string.event_send_notification))
            }
      }
    }
  }
}

/**
 * Button that allows the user to save/unsave an event
 *
 * @param event the selected [Event].
 * @param eventViewModel The [EventViewModel].
 * @param userViewModel The [UserViewModel].
 */
@Composable
fun EventSaveButton(event: Event, eventViewModel: EventViewModel, userViewModel: UserViewModel) {
  val context = LocalContext.current

  val user by userViewModel.user.collectAsState()
  val events by eventViewModel.events.collectAsState()

  if (user == null) {
    Log.e("EventCard", "User is null")
    return
  }

  var isSaved by remember { mutableStateOf(user!!.savedEvents.contains(event.uid)) }

  var notificationPermissionsEnabled by remember { mutableStateOf(false) }
  when (PackageManager.PERMISSION_GRANTED) {
    ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) -> {
      notificationPermissionsEnabled = true
    }
  }

  val preferences by LocalPreferenceFlow.current.collectAsState()
  val notificationSettingEnabled =
      preferences
          .asMap()
          .getOrDefault(AppPreferences.NOTIFICATIONS, AppPreferences.Notification.default)
          as Boolean

  val scheduleReminderNotification = {
    if (notificationSettingEnabled) {
      NotificationWorker.schedule(
          context,
          UnioNotification(
              title = event.title,
              message = context.getString(R.string.notification_event_reminder),
              icon = R.drawable.other_icon,
              channelId = EVENT_REMINDER_CHANNEL_ID,
              channelName = EVENT_REMINDER_CHANNEL_ID,
              notificationId = event.uid.hashCode(),
              // Schedule a notification a few hours before the event's startDate
              timeMillis = (event.startDate.seconds - 2 * SECONDS_IN_AN_HOUR) * SECONDS_IN_AN_HOUR))
    }
  }

  val permissionLauncher =
      rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { permission ->
        when {
          permission -> {
            notificationPermissionsEnabled = true
            scheduleReminderNotification()
          }
          else -> {
            notificationPermissionsEnabled = false
            Log.e("EventCard", "Notification permission is not granted.")
          }
        }
      }
  var enableButton by remember { mutableStateOf(true) }
  val isConnected = NetworkUtils.checkInternetConnection(context)
  val onClickSaveButton = {
    enableButton = false
    if (isConnected) {
      eventViewModel.updateSave(events.first { it.uid == event.uid }, user!!, isSaved) {
        userViewModel.refreshUser()
        if (isSaved) {
          if (notificationPermissionsEnabled) {
            NotificationWorker.unschedule(context, event.uid.hashCode())
          }
        } else {
          if (!notificationPermissionsEnabled) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
              // this permission requires api 33
              // We should check how to make notifications work with lower api versions
              permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
          } else {
            scheduleReminderNotification()
          }
        }
        isSaved = !isSaved
        enableButton = true
      }
    } else {
      ToastUtils.showToast(context, context.getString(R.string.no_internet_connection))
    }
  }

  IconButton(
      modifier =
          Modifier.size(28.dp)
              .clip(RoundedCornerShape(14.dp))
              .background(MaterialTheme.colorScheme.inversePrimary)
              .padding(4.dp)
              .testTag(EventDetailsTestTags.SAVE_BUTTON),
      onClick = { onClickSaveButton() },
      enabled = enableButton) {
        Icon(
            imageVector = if (isSaved) Icons.Rounded.Favorite else Icons.Rounded.FavoriteBorder,
            contentDescription =
                if (isSaved) context.getString(R.string.event_card_content_description_saved_event)
                else context.getString(R.string.event_card_content_description_not_saved_event),
            tint = if (isSaved) Color.Red else Color.White)
      }
}
