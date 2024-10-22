package com.android.unio.ui.authentication

import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import com.android.unio.model.user.Interest
import junit.framework.TestCase.assertEquals
import kotlinx.coroutines.flow.MutableStateFlow
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class OverlayCardTest {
  val interests = Interest.entries.map { it to mutableStateOf(false) }.toMutableList()

  @get:Rule val composeTestRule = createComposeRule()

  @Before
  fun setUp() {
    composeTestRule.setContent { InterestOverlay({}, {}, interests) }
  }

  @Test
  fun testEverythingIsDisplayed() {
    composeTestRule.onNodeWithTag("InterestOverlayTitle").assertIsDisplayed()
    composeTestRule.onNodeWithTag("InterestOverlaySubtitle").assertIsDisplayed()
    composeTestRule.onNodeWithTag("InterestOverlaySaveButton").assertIsDisplayed()

    interests.forEachIndexed { index, pair ->
      composeTestRule
          .onNodeWithTag("InterestOverlayText: ${pair.first.name}", useUnmergedTree = true)
          .assertExists()
      composeTestRule.onNodeWithTag("InterestOverlayCheckbox: ${pair.first.name}").assertExists()

      if (index != interests.size - 1) {
        composeTestRule.onNodeWithTag("InterestOverlayDivider: $index").assertExists()
      }
    }
  }

  @Test
  fun testWhenCheckBoxCheckedInterestStateChanges() {
    interests.forEachIndexed() { index, pair ->
      composeTestRule
          .onNodeWithTag("InterestOverlayClickableRow: $index")
          .performScrollTo()
          .performClick()
      assertEquals(true, pair.second.value)
    }
  }
}
