package com.android.unio.components.image

import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.core.net.toUri
import com.android.unio.TearDown
import com.android.unio.ui.image.AsyncImageWrapper
import org.junit.Rule
import org.junit.Test

class AsyncImageWrapperTest : TearDown() {

  @get:Rule val composeTestRule = createComposeRule()

  private val imgUrl =
      "https://m.media-amazon.com/images/S/pv-target-images/4be23d776550ebae78e63f21bec3515d3347ac4f44a3fb81e6633cf7a116761e.jpg"

  private fun setAsyncImageWrapper() {
    composeTestRule.setContent {
      AsyncImageWrapper(
          imageUri = imgUrl.toUri(), contentDescription = "", modifier = Modifier.testTag("IMAGE"))
    }
  }

  @Test
  fun checkImageDisplays() {
    setAsyncImageWrapper()
    composeTestRule.onNodeWithTag("IMAGE").assertIsDisplayed()
  }
}
