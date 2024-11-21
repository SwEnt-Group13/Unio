package com.android.unio.ui.user

import android.content.Context
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.test.SemanticsNodeInteraction
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.navigation.NavHostController
import com.android.unio.model.association.AssociationRepositoryFirestore
import com.android.unio.model.association.AssociationViewModel
import com.android.unio.model.event.EventRepositoryFirestore
import com.android.unio.model.follow.ConcurrentAssociationUserRepositoryFirestore
import com.android.unio.model.image.ImageRepositoryFirebaseStorage
import com.android.unio.model.search.SearchRepository
import com.android.unio.model.search.SearchViewModel
import com.android.unio.model.strings.test_tags.UserClaimAssociationTestTags
import com.android.unio.ui.navigation.NavigationAction
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import io.mockk.MockKAnnotations
import io.mockk.every
import io.mockk.impl.annotations.MockK
import org.junit.Before
import org.junit.Rule
import org.junit.Test

@HiltAndroidTest
class UserClaimAssociationTest {

  @get:Rule val composeTestRule = createComposeRule()
  @get:Rule val hiltRule = HiltAndroidRule(this)

  @MockK private lateinit var associationRepository: AssociationRepositoryFirestore
  @MockK private lateinit var eventRepository: EventRepositoryFirestore
  @MockK private lateinit var imageRepository: ImageRepositoryFirebaseStorage
  @MockK
  private lateinit var concurrentAssociationUserRepositoryFirestore:
      ConcurrentAssociationUserRepositoryFirestore
  @MockK private lateinit var searchRepository: SearchRepository
  @MockK private lateinit var navHostController: NavHostController

  @MockK private lateinit var navigationAction: NavigationAction
  private lateinit var associationViewModel: AssociationViewModel
  private lateinit var searchViewModel: SearchViewModel

  @Before
  fun setUp() {
    MockKAnnotations.init(this, relaxed = true)
    hiltRule.inject()

    every { navigationAction.navigateTo(any<String>()) } returns Unit

    associationViewModel =
        AssociationViewModel(
            associationRepository,
            eventRepository,
            imageRepository,
            concurrentAssociationUserRepositoryFirestore)

    searchViewModel = SearchViewModel(searchRepository)

    navigationAction = NavigationAction(navHostController)
  }

  @Test
  fun testUIElementsAreDisplayed() {
    composeTestRule.setContent {
      UserClaimAssociationScreen(associationViewModel, navigationAction, searchViewModel)
    }

    assertDisplayComponent(composeTestRule.onNodeWithTag(UserClaimAssociationTestTags.SCREEN))
    assertDisplayComponent(composeTestRule.onNodeWithTag(UserClaimAssociationTestTags.BACK_BUTTON))
    assertDisplayComponent(
        composeTestRule.onNodeWithTag(UserClaimAssociationTestTags.NEW_ASSOCIATION_BUTTON))
  }

  @Test
  fun testGoBackButtonNavigatesBack() {
    composeTestRule.setContent {
      UserClaimAssociationScreen(associationViewModel, navigationAction, searchViewModel)
    }

    composeTestRule.onNodeWithTag(UserClaimAssociationTestTags.BACK_BUTTON).performClick()
  }

  @Test
  fun testCreateNewAssociationButtonShowsToast() {
    var context: Context? = null
    composeTestRule.setContent {
      context = LocalContext.current
      UserClaimAssociationScreen(associationViewModel, navigationAction, searchViewModel)
    }

    val button = composeTestRule.onNodeWithTag(UserClaimAssociationTestTags.NEW_ASSOCIATION_BUTTON)
    button.performClick()
  }

  private fun assertDisplayComponent(compose: SemanticsNodeInteraction) {
    compose.assertIsDisplayed()
  }
}