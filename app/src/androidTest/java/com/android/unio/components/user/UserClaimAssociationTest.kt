package com.android.unio.components.user

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.navigation.NavHostController
import com.android.unio.TearDown
import com.android.unio.model.association.AssociationRepositoryFirestore
import com.android.unio.model.association.AssociationViewModel
import com.android.unio.model.event.EventRepositoryFirestore
import com.android.unio.model.follow.ConcurrentAssociationUserRepositoryFirestore
import com.android.unio.model.image.ImageRepositoryFirebaseStorage
import com.android.unio.model.search.SearchRepository
import com.android.unio.model.search.SearchViewModel
import com.android.unio.model.strings.test_tags.user.UserClaimAssociationTestTags
import com.android.unio.ui.navigation.NavigationAction
import com.android.unio.ui.user.UserClaimAssociationScreen
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import io.mockk.MockKAnnotations
import io.mockk.every
import io.mockk.impl.annotations.MockK
import org.junit.Before
import org.junit.Rule
import org.junit.Test

@HiltAndroidTest
class UserClaimAssociationTest : TearDown() {

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

    composeTestRule.onNodeWithTag(UserClaimAssociationTestTags.SCREEN).assertIsDisplayed()
    composeTestRule.onNodeWithTag(UserClaimAssociationTestTags.BACK_BUTTON).assertIsDisplayed()
  }

  @Test
  fun testGoBackButtonNavigatesBack() {
    composeTestRule.setContent {
      UserClaimAssociationScreen(associationViewModel, navigationAction, searchViewModel)
    }

    composeTestRule.onNodeWithTag(UserClaimAssociationTestTags.BACK_BUTTON).performClick()
  }
}
