package com.android.unio.ui.event.overlay

import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PagerState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.rounded.Favorite
import androidx.compose.material.icons.rounded.FavoriteBorder
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
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
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.core.net.toUri
import com.android.unio.R
import com.android.unio.model.event.EventUserPicture
import com.android.unio.model.event.EventViewModel
import com.android.unio.model.strings.test_tags.event.EventDetailsTestTags
import com.android.unio.model.strings.test_tags.event.EventDetailsTestTags.PICTURE_FULL_SCREEN
import com.android.unio.model.user.User
import com.android.unio.ui.image.AsyncImageWrapper
import com.android.unio.ui.theme.AppTypography
import com.android.unio.ui.user.UserDeletePrompt
import kotlinx.coroutines.launch

private val ASSOCIATION_ICON_SIZE = 32.dp
private val PADDING_VALUE = 55.dp
/**
 * A dialog that allows users to view event pictures in full screen.
 *
 * @param onDismiss Callback when the dialog is dismissed.
 * @param pagerState the [PagerState] of the pager.
 * @param eventPictures The list of [EventUserPicture] to display.
 */
@Composable
fun PictureOverlay(
    onDismiss: () -> Unit,
    pagerState: PagerState,
    eventPictures: List<EventUserPicture>,
    eventViewModel: EventViewModel,
    user: User
) {

  val scope = rememberCoroutineScope()
  val context = LocalContext.current
  val arrowSize = 40.dp
  var enableButton by remember { mutableStateOf(true) }
  val event by eventViewModel.selectedEvent.collectAsState()

  if (event == null) {
    Log.e("PictureOverlay", "Event is null")
    Toast.makeText(LocalContext.current, "An error occurred.", Toast.LENGTH_SHORT).show()
    return
  }

  val onClickArrow: (Boolean) -> Unit = { isRight: Boolean ->
    if (isRight && pagerState.currentPage < eventPictures.size - 1) {
      val newPageIndex = pagerState.currentPage + 1
      eventPictures[newPageIndex].author.fetch()
      scope.launch { pagerState.animateScrollToPage(newPageIndex) }
    } else if (!isRight && pagerState.currentPage > 0) {
      val newPageIndex = pagerState.currentPage - 1
      eventPictures[newPageIndex].author.fetch()
      scope.launch { pagerState.animateScrollToPage(newPageIndex) }
    }
  }

  val author by eventPictures[pagerState.currentPage].author.element.collectAsState()
  var showDeletePicturePrompt by remember { mutableStateOf(false) }
  val onDeletePicture: () -> Unit = {
    showDeletePicturePrompt = false
    onDismiss()
    scope.launch {
      eventViewModel.deleteEventUserPicture(
          eventPictures[pagerState.currentPage].uid, event!!, {}, {})
    }
  }
  var isLiked by
      remember(pagerState.currentPage) {
        mutableStateOf(eventPictures[pagerState.currentPage].likes.contains(user.uid))
      }

  var nbOfLikes by
      remember(pagerState.currentPage) {
        mutableIntStateOf(eventPictures[pagerState.currentPage].likes.uids.size)
      }

  val onClickLike = {
    enableButton = false
    val picture = eventPictures[pagerState.currentPage]
    if (isLiked) {
      picture.likes.remove(user.uid)
      nbOfLikes -= 1
    } else {
      picture.likes.add(user.uid)
      nbOfLikes += 1
    }
    isLiked = !isLiked
    eventViewModel.updateEventUserPictureWithoutImage(
        event = event!!, picture = picture, { enableButton = true }, {})
  }
  Dialog(
      onDismissRequest = onDismiss,
      properties =
          DialogProperties(
              dismissOnClickOutside = true,
              dismissOnBackPress = true,
              usePlatformDefaultWidth = false)) {
        Box(
            modifier = Modifier.testTag(PICTURE_FULL_SCREEN).fillMaxHeight(0.5f).fillMaxWidth(),
            contentAlignment = Alignment.Center) {
              HorizontalPager(
                  pagerState,
                  pageSpacing = 40.dp,
                  beyondViewportPageCount = 1,
                  modifier = Modifier.align(Alignment.Center)) { page ->
                    AsyncImageWrapper(
                        imageUri = eventPictures[page].image.toUri(),
                        contentDescription =
                            context.getString(
                                R.string.event_details_content_description_full_screen_picture),
                        filterQuality = FilterQuality.High,
                        placeholderResourceId = 0,
                        contentScale = ContentScale.Fit,
                        modifier =
                            Modifier.padding(
                                    start = PADDING_VALUE,
                                    end = PADDING_VALUE,
                                    bottom = PADDING_VALUE)
                                .align(Alignment.Center)
                                .fillMaxWidth())
                  }

              if (author?.uid == user.uid) {
                IconButton(
                    onClick = { showDeletePicturePrompt = true },
                    modifier =
                        Modifier.align(Alignment.TopEnd)
                            .testTag("")
                            .padding(end = PADDING_VALUE + 15.dp),
                    colors =
                        IconButtonDefaults.iconButtonColors(
                            contentColor = MaterialTheme.colorScheme.onPrimary,
                            containerColor = Color(120, 30, 24))) {
                      Icon(
                          imageVector = Icons.Default.Delete,
                          contentDescription = "",
                          modifier = Modifier.size(30.dp))
                    }
              }
              IconButton(
                  onClick = { onClickArrow(false) },
                  modifier =
                      Modifier.align(Alignment.CenterStart)
                          .testTag(EventDetailsTestTags.EVENT_PICTURES_ARROW_LEFT),
                  colors =
                      IconButtonDefaults.iconButtonColors(
                          contentColor = MaterialTheme.colorScheme.onPrimary)) {
                    Icon(
                        Icons.AutoMirrored.Filled.ArrowBack,
                        context.getString(R.string.event_details_content_description_arrow_left),
                        modifier = Modifier.size(arrowSize))
                  }

              IconButton(
                  onClick = { onClickArrow(true) },
                  modifier =
                      Modifier.align(Alignment.CenterEnd)
                          .testTag(EventDetailsTestTags.EVENT_PICTURES_ARROW_RIGHT),
                  colors =
                      IconButtonDefaults.iconButtonColors(
                          contentColor = MaterialTheme.colorScheme.onPrimary)) {
                    Icon(
                        Icons.AutoMirrored.Filled.ArrowForward,
                        context.getString(R.string.event_details_content_description_arrow_right),
                        modifier = Modifier.size(arrowSize))
                  }

              Row(
                  modifier =
                      Modifier.testTag(EventDetailsTestTags.INTERACTION_ROW)
                          .align(Alignment.BottomCenter)
                          .fillMaxWidth()
                          .padding(horizontal = PADDING_VALUE),
                  horizontalArrangement = Arrangement.SpaceBetween,
                  verticalAlignment = Alignment.CenterVertically) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                      IconButton(
                          onClick = onClickLike,
                          modifier = Modifier,
                          colors =
                              IconButtonDefaults.iconButtonColors(
                                  contentColor = MaterialTheme.colorScheme.onPrimary)) {
                            Icon(
                                imageVector =
                                    if (isLiked) Icons.Rounded.Favorite
                                    else Icons.Rounded.FavoriteBorder,
                                context.getString(
                                    R.string.event_details_content_description_like_picture),
                                modifier = Modifier.size(arrowSize),
                                tint = if (isLiked) Color.Red else Color.White)
                          }

                      Text("$nbOfLikes", color = Color.White)
                    }
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier =
                            Modifier.testTag(EventDetailsTestTags.EVENT_PICTURES_AUTHOR_INFO)) {
                          author?.profilePicture?.toUri()?.let {
                            AsyncImageWrapper(
                                imageUri = it,
                                contentDescription =
                                    context.getString(
                                        R.string.event_details_content_description_author_icon),
                                modifier =
                                    Modifier.size(ASSOCIATION_ICON_SIZE)
                                        .clip(RoundedCornerShape(5.dp))
                                        .align(Alignment.CenterVertically)
                                        .testTag(EventDetailsTestTags.EVENT_PICTURES_AUTHOR_ICON),
                                placeholderResourceId = R.drawable.adec,
                                filterQuality = FilterQuality.None,
                                contentScale = ContentScale.Crop)
                          }

                          Text(
                              "${author?.firstName} ${author?.lastName}",
                              modifier =
                                  Modifier.testTag(EventDetailsTestTags.EVENT_PICTURES_AUTHOR_NAME)
                                      .padding(start = 5.dp),
                              style = AppTypography.bodyMedium,
                              color = MaterialTheme.colorScheme.onPrimary)
                        }
                  }
            }
      }
  if (showDeletePicturePrompt) {
    UserDeletePrompt(
        onDismiss = { showDeletePicturePrompt = false }, onConfirmDelete = { onDeletePicture() })
  }
}
