package com.android.unio.ui.map

import androidx.compose.ui.test.assertHasClickAction
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.android.unio.model.event.EventRepository
import com.android.unio.model.event.EventViewModel
import com.android.unio.model.image.ImageRepository
import com.android.unio.ui.navigation.NavigationAction
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.Mockito.mock
import org.mockito.MockitoAnnotations
import org.mockito.kotlin.verify

@RunWith(AndroidJUnit4::class)
class MapScreenTest {

  @get:Rule val composeTestRule = createComposeRule()

  @Mock private lateinit var eventRepository: EventRepository
  @Mock private lateinit var imageRepository: ImageRepository
  private lateinit var navigationAction: NavigationAction
  private lateinit var eventViewModel: EventViewModel

  @Before
  fun setUp() {
    MockitoAnnotations.openMocks(this)

    navigationAction = mock(NavigationAction::class.java)
    eventViewModel = EventViewModel(eventRepository, imageRepository)
  }

  @Test
  fun mapScreenComponentsAreDisplayed() {
    composeTestRule.setContent { MapScreen(navigationAction, eventViewModel) }

    composeTestRule.onNodeWithTag("MapScreen").assertIsDisplayed()
    composeTestRule.onNodeWithTag("MapTitle").assertIsDisplayed()
    composeTestRule.onNodeWithTag("goBackButton").assertHasClickAction()
  }

  @Test
  fun mapScreenBackButtonNavigatesBack() {
    composeTestRule.setContent { MapScreen(navigationAction, eventViewModel) }

    composeTestRule.onNodeWithTag("goBackButton").performClick()
    verify(navigationAction).goBack()
  }
}
