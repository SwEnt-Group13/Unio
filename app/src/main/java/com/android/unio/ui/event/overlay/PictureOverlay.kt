package com.android.unio.ui.event.overlay

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PagerState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.core.net.toUri
import com.android.unio.model.event.EventUserPicture
import com.android.unio.ui.image.AsyncImageWrapper
import kotlinx.coroutines.launch

@Composable
fun PictureOverlay(
    onDismiss: () -> Unit,
    pagerState: PagerState,
    eventPictures: List<EventUserPicture>
) {
  val scope = rememberCoroutineScope()
  val iconSize = 40.dp
  val onClickArrow = { isRight: Boolean ->
    if (isRight && pagerState.currentPage < eventPictures.size - 1) {
      scope.launch { pagerState.animateScrollToPage(pagerState.currentPage + 1) }
    } else if (!isRight) {
      if (pagerState.currentPage > 0) {
        scope.launch { pagerState.animateScrollToPage(pagerState.currentPage - 1) }
      } else {}
    } else {}
  }
  Dialog(
      onDismissRequest = onDismiss,
      properties =
          DialogProperties(
              dismissOnClickOutside = true,
              dismissOnBackPress = true,
              usePlatformDefaultWidth = false)) {
        Box {
          HorizontalPager(pagerState, pageSpacing = 40.dp, beyondViewportPageCount = 1) { page ->
            AsyncImageWrapper(
                imageUri = eventPictures[page].image.toUri(),
                contentDescription = "",
                filterQuality = FilterQuality.High,
                placeholderResourceId = 0,
                contentScale = ContentScale.Inside,
                modifier = Modifier)
          }
          IconButton(
              onClick = { onClickArrow(false) },
              modifier = Modifier.align(Alignment.CenterStart),
              colors =
                  IconButtonDefaults.iconButtonColors(
                      contentColor = MaterialTheme.colorScheme.onPrimary,
                      containerColor = MaterialTheme.colorScheme.primary)) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, "", modifier = Modifier.size(iconSize))
              }

          IconButton(
              onClick = { onClickArrow(true) },
              modifier = Modifier.align(Alignment.CenterEnd),
              colors =
                  IconButtonDefaults.iconButtonColors(
                      contentColor = MaterialTheme.colorScheme.onPrimary,
                      containerColor = MaterialTheme.colorScheme.primary)) {
                Icon(Icons.AutoMirrored.Filled.ArrowForward, "", modifier = Modifier.size(iconSize))
              }
        }
      }
}
