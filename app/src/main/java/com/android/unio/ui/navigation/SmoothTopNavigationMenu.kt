package com.android.unio.ui.navigation

import android.util.Log
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.pager.PagerState
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment.Companion.CenterHorizontally
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.android.unio.model.strings.test_tags.home.HomeTestTags
import kotlinx.coroutines.launch

@Composable
fun SmoothTopBarNavigationMenu(tabList: List<String>, pagerState: PagerState) {

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
        // method that draws the indicator bar below the tab menu
        Box(
            modifier =
                Modifier.fillMaxSize().drawBehind {
                  val tabWidth: Float
                  val height: Float
                  if (sizeList[0] == null) {
                    Log.e("Home Page", "The size values of tabs are null, should not happen !")
                    // hardcoded values in case sizeList[0] is null
                    tabWidth = 576.0F
                    height = 92.0F
                  } else {
                    tabWidth = sizeList[0]!!.first
                    height = sizeList[0]!!.second
                  }
                  val startOffset =
                      Offset(x = progressFromFirstPage * tabWidth + tabWidth / 3, y = height - 25)
                  val endOffset =
                      Offset(
                          x = progressFromFirstPage * tabWidth + tabWidth * 2 / 3, y = height - 25)

                  drawLine(
                      start = startOffset,
                      end = endOffset,
                      color = colorScheme.primary,
                      strokeWidth = Stroke.DefaultMiter)
                })
      }) {
        val tabTestTags = listOf(HomeTestTags.TAB_ALL, HomeTestTags.TAB_FOLLOWING)
        tabList.forEachIndexed { index, str ->
          Tab(
              selected = index == pagerState.currentPage,
              // animate pager if click on tab
              onClick = { scope.launch { pagerState.animateScrollToPage(index) } },
              modifier =
                  Modifier.testTag(tabTestTags[index]).onSizeChanged {
                    sizeList[index] = Pair(it.width.toFloat(), it.height.toFloat())
                  },
              selectedContentColor = colorScheme.primary) {
                Text(
                    text = str,
                    style =
                        TextStyle(
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp,
                        ),
                    modifier =
                        Modifier.align(CenterHorizontally)
                            .padding(horizontal = 32.dp, vertical = 16.dp))
              }
        }
      }
}
