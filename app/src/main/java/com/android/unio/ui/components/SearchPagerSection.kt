package com.android.unio.ui.components

import android.util.Log
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.android.unio.R
import com.android.unio.ui.theme.AppTypography
import kotlinx.coroutines.launch

/**
 * A generalized composable for displaying a search bar with a sliding pager.
 *
 * @param T The type of the entity (e.g., Event, Member, Association).
 * @param items The list of items to display in the pager.
 * @param searchViewModel The view model handling the search logic.
 * @param onItemSelected Callback when an item is selected from search results.
 * @param cardContent A composable lambda that defines how to render each pager content.
 * @param title The title displayed above the section.
 * @param searchBar Composable function for rendering the search bar.
 */
@Composable
fun <T> SearchPagerSection(
    items: List<T>,
    cardContent: @Composable (T) -> Unit,
    pagerState: PagerState
) {
  val context = LocalContext.current
  Column(
      horizontalAlignment = Alignment.CenterHorizontally,
      modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)) {

        // Sliding Progress Bar (if more than one item exists)
        if (items.size > 1) {
          // Search Bar Composable
          // Box(modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp)) { searchBar() }

          Text(
              text = context.getString(R.string.search_pager_section_slide),
              style = AppTypography.bodySmall,
              modifier = Modifier.padding(vertical = 8.dp))
          ProgressBarBetweenElements(tabList = items.map { it.toString() }, pagerState = pagerState)
        }

        // Horizontal Pager
        HorizontalPager(
            state = pagerState,
            contentPadding = PaddingValues(horizontal = 16.dp),
            pageSpacing = 16.dp) { page ->
              val item = items[page]
              Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                cardContent(item)
              }
            }
      }
}

/**
 * A composable that renders a progress bar between elements in a tab layout, based on the state of
 * the pager. The progress bar visually represents the current position and movement between tabs.
 *
 * @param tabList The list of tab labels, corresponding to the pager's items.
 * @param pagerState The state of the pager, providing the current page and offset information.
 */
@Composable
fun ProgressBarBetweenElements(tabList: List<String>, pagerState: PagerState) {
  val defaultTabWidth = 576.0F
  val defaultTabHeight = 92.0F

  val scope = rememberCoroutineScope()
  val colorScheme = MaterialTheme.colorScheme
  val sizeList = remember { mutableStateMapOf<Int, Pair<Float, Float>>() }
  val progressFromFirstPage by remember {
    derivedStateOf { pagerState.currentPageOffsetFraction + pagerState.currentPage.dp.value }
  }

  TabRow(
      selectedTabIndex = pagerState.currentPage,
      contentColor = colorScheme.primary,
      divider = {},
      indicator = {
        Box(
            modifier =
                Modifier.fillMaxSize().drawBehind {
                  val totalWidth = sizeList.values.map { it.first }.sum()
                  val height: Float

                  if (sizeList.isEmpty()) {
                    Log.e("Home Page", "The size values of tabs are null, should not happen !")
                    height = defaultTabHeight
                  } else {
                    height = sizeList[0]?.second ?: defaultTabHeight
                  }

                  val outerRectangleYStart = height - 45
                  val outerRectangleYEnd = height - 5

                  val tabWidth = sizeList[0]?.first ?: defaultTabWidth
                  val rectangleStartX = progressFromFirstPage * tabWidth + tabWidth / 4
                  val rectangleEndX = progressFromFirstPage * tabWidth + tabWidth * 3 / 4
                  val rectangleYStart = height - 35
                  val rectangleYEnd = height - 15

                  drawRoundRect(
                      color = colorScheme.primary.copy(alpha = 0.1f),
                      topLeft = Offset(x = tabWidth / 4, y = outerRectangleYStart),
                      size =
                          Size(
                              width = tabWidth * 7 / 2,
                              height =
                                  outerRectangleYEnd -
                                      outerRectangleYStart), // 2 * (7/2 = 1 + 3 / 4)
                      cornerRadius = CornerRadius(x = 16.dp.toPx(), y = 16.dp.toPx()))

                  drawRoundRect(
                      color = colorScheme.primary.copy(alpha = 0.2f),
                      topLeft = Offset(x = rectangleStartX, y = rectangleYStart),
                      size =
                          Size(
                              width = rectangleEndX - rectangleStartX,
                              height = rectangleYEnd - rectangleYStart),
                      cornerRadius = CornerRadius(x = 12.dp.toPx(), y = 12.dp.toPx()))

                  val lineStartOffset =
                      Offset(x = progressFromFirstPage * tabWidth + tabWidth / 3, y = height - 25)
                  val lineEndOffset =
                      Offset(
                          x = progressFromFirstPage * tabWidth + tabWidth * 2 / 3, y = height - 25)

                  drawLine(
                      start = lineStartOffset,
                      end = lineEndOffset,
                      color = colorScheme.primary,
                      strokeWidth = Stroke.DefaultMiter)
                })
      }) {
        tabList.forEachIndexed { index, str ->
          Tab(
              selected = index == pagerState.currentPage,
              onClick = { scope.launch { pagerState.animateScrollToPage(index) } },
              modifier =
                  Modifier.onSizeChanged {
                    sizeList[index] = Pair(it.width.toFloat(), it.height.toFloat())
                  },
              selectedContentColor = colorScheme.primary) {
                Spacer(modifier = Modifier.height(20.dp))
              }
        }
      }
}
