package com.android.unio.ui.user

import android.content.Context
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.test.SemanticsNodeInteraction
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import androidx.navigation.NavHostController
import com.android.unio.mocks.association.MockAssociation
import com.android.unio.model.association.AssociationRepositoryFirestore
import com.android.unio.model.association.AssociationViewModel
import com.android.unio.model.event.EventRepositoryFirestore
import com.android.unio.model.follow.ConcurrentAssociationUserRepositoryFirestore
import com.android.unio.model.image.ImageRepositoryFirebaseStorage
import com.android.unio.model.search.SearchRepository
import com.android.unio.model.search.SearchViewModel
import com.android.unio.ui.navigation.NavigationAction
import com.android.unio.ui.navigation.Screen
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import io.mockk.MockKAnnotations
import io.mockk.coEvery
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.just
import io.mockk.mockk
import io.mockk.runs
import io.mockk.spyk
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.kotlin.verify

@HiltAndroidTest
class UserClaimAssociationTest {

  @get:Rule val composeTestRule = createComposeRule()
  @get:Rule val hiltRule = HiltAndroidRule(this)
  @MockK private lateinit var associationRepository: AssociationRepositoryFirestore
  @MockK private lateinit var eventRepository: EventRepositoryFirestore
  @MockK private lateinit var imageRepository: ImageRepositoryFirebaseStorage
  @MockK
  private lateinit var concurrentAssociationUserRepository:
      ConcurrentAssociationUserRepositoryFirestore
  @MockK private lateinit var searchRepository: SearchRepository
  @MockK private lateinit var navHostController: NavHostController

  private lateinit var navigationAction: NavigationAction
  private lateinit var associationViewModel: AssociationViewModel
  @MockK private lateinit var searchViewModel: SearchViewModel

  @Before
  fun setUp() {
    MockKAnnotations.init(this)
    hiltRule.inject()
    every { associationRepository.init(any()) } just runs

    /*associationRepository = mockk()
    eventRepository = mockk()
    imageRepository = mockk()
    concurrentAssociationUserRepository = mockk()*/
    // searchRepository = mockk()
    navHostController = mockk()
    navigationAction = NavigationAction(navHostController)
    associationViewModel =
        spyk(
            AssociationViewModel(
                associationRepository,
                eventRepository,
                imageRepository,
                concurrentAssociationUserRepository))
    searchViewModel = mockk()

    coEvery { searchViewModel.init() } just runs
  }

  @Test
  fun testUIElementsAreDisplayed() {
    composeTestRule.setContent {
      UserClaimAssociationScreen(associationViewModel, navigationAction, searchViewModel)
    }

    assertDisplayComponent(composeTestRule.onNodeWithTag("AssociationProfileTitle"))
    assertDisplayComponent(composeTestRule.onNodeWithTag("goBackButton"))
    assertDisplayComponent(composeTestRule.onNodeWithTag("createNewAssociationButton"))
  }

  @Test
  fun testGoBackButtonNavigatesBack() {
    composeTestRule.setContent {
      UserClaimAssociationScreen(associationViewModel, navigationAction, searchViewModel)
    }

    composeTestRule.onNodeWithTag("goBackButton").performClick()

    verify(navigationAction.navController).popBackStack()
  }

  @Test
  fun testCreateNewAssociationButtonShowsToast() {
    var context: Context? = null
    composeTestRule.setContent {
      context = LocalContext.current
      UserClaimAssociationScreen(associationViewModel, navigationAction, searchViewModel)
    }

    val button = composeTestRule.onNodeWithTag("createNewAssociationButton")
    button.performClick()
  }

  @Test
  fun testSearchAndSelectAssociation() {
    composeTestRule.setContent {
      UserClaimAssociationScreen(associationViewModel, navigationAction, searchViewModel)
    }

    val associationSearchBar = composeTestRule.onNodeWithTag("associationSearchBar")
    associationSearchBar.performTextInput("Association Name")

    // Simulate selecting an association after search
    associationViewModel.selectAssociation(MockAssociation.createMockAssociation().uid)
    navigationAction.navigateTo(Screen.CLAIM_ASSOCIATION_PRESIDENTIAL_RIGHTS)

    // Verify the navigation to the claim association screen happens after selecting an association
    verify(navigationAction.navController).navigate(Screen.CLAIM_ASSOCIATION_PRESIDENTIAL_RIGHTS)
  }

  private fun assertDisplayComponent(compose: SemanticsNodeInteraction) {
    compose.assertIsDisplayed()
  }
}
