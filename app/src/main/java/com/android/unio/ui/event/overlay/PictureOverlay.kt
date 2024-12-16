package com.android.unio.ui.event.overlay

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PagerState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.rounded.FavoriteBorder
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
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
import com.android.unio.model.strings.test_tags.event.EventDetailsTestTags
import com.android.unio.model.strings.test_tags.event.EventDetailsTestTags.PICTURE_FULL_SCREEN
import com.android.unio.ui.image.AsyncImageWrapper
import kotlinx.coroutines.launch

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
    eventPictures: List<EventUserPicture>
) {
  val scope = rememberCoroutineScope()
  val context = LocalContext.current
  val iconSize = 40.dp
  val onClickArrow: (Boolean) -> Unit = { isRight: Boolean ->
    if (isRight && pagerState.currentPage < eventPictures.size - 1) {
      scope.launch { pagerState.animateScrollToPage(pagerState.currentPage + 1) }
    } else if (!isRight && pagerState.currentPage > 0) {
      scope.launch { pagerState.animateScrollToPage(pagerState.currentPage - 1) }
    }
  }
  val isLiked = 1
  Dialog(
      onDismissRequest = onDismiss,
      properties =
          DialogProperties(
              dismissOnClickOutside = true,
              dismissOnBackPress = true,
              usePlatformDefaultWidth = false)) {
        Box(
            modifier = Modifier.testTag(PICTURE_FULL_SCREEN).fillMaxHeight(0.5f),
            contentAlignment = Alignment.Center) {
              HorizontalPager(pagerState, pageSpacing = 40.dp, beyondViewportPageCount = 1) { page
                ->
                AsyncImageWrapper(
                    imageUri = eventPictures[page].image.toUri(),
                    contentDescription =
                        context.getString(
                            R.string.event_details_content_description_full_screen_picture),
                    filterQuality = FilterQuality.High,
                    placeholderResourceId = 0,
                    contentScale = ContentScale.Fit,
                    modifier = Modifier.padding(start = 55.dp, end = 55.dp, bottom = 55.dp))
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
                        modifier = Modifier.size(iconSize))
                  }

              IconButton(
                  onClick = { onClickArrow(true) },
                  modifier =
                      Modifier.align(Alignment.CenterEnd)
                          .testTag(EventDetailsTestTags.EVENT_PICTURES_ARROW_RIGHT),
                  colors =
                      IconButtonDefaults.iconButtonColors(
                          contentColor = MaterialTheme.colorScheme.onPrimary,
                          containerColor = MaterialTheme.colorScheme.primary)) {
                    Icon(
                        Icons.AutoMirrored.Filled.ArrowForward,
                        context.getString(R.string.event_details_content_description_arrow_right),
                        modifier = Modifier.size(iconSize))
                  }

              Row(
                  modifier =
                      Modifier.testTag("pictureInteractionRow").align(Alignment.BottomCenter),
                  horizontalArrangement = Arrangement.SpaceAround) {
                    IconButton(
                        onClick = {},
                        modifier = Modifier,
                        colors =
                            IconButtonDefaults.iconButtonColors(
                                contentColor = MaterialTheme.colorScheme.onPrimary)) {
                          Icon(
                              imageVector = Icons.Rounded.FavoriteBorder,
                              "like button",
                              modifier = Modifier.size(iconSize))
                        }
                  }
            }
      }
}
