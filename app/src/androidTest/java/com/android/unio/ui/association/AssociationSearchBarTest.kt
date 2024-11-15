package com.android.unio.ui.association

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.navigation.NavHostController
import com.android.unio.mocks.association.MockAssociation
import com.android.unio.model.search.SearchRepository
import com.android.unio.model.search.SearchViewModel
import com.android.unio.model.strings.test_tags.ExploreContentTestTags
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
class AssociationSearchBarTest {

  @get:Rule val composeTestRule = createComposeRule()
  @get:Rule val hiltRule = HiltAndroidRule(this)

  private lateinit var searchViewModel: SearchViewModel
  @MockK private lateinit var navHostController: NavHostController

  @MockK private lateinit var navigationAction: NavigationAction

  @MockK private lateinit var searchRepository: SearchRepository

  @Before
  fun setUp() {
    MockKAnnotations.init(this, relaxed = true)
    hiltRule.inject()

    every { navigationAction.navigateTo(any<String>()) } returns Unit

    searchViewModel = SearchViewModel(searchRepository)

    navigationAction = NavigationAction(navHostController)
  }

  @Test
  fun testSearchBarIsDisplayed() {
    composeTestRule.setContent { AssociationSearchBar(searchViewModel, onAssociationSelected = {}) }

    // Verify the search input field is displayed

    // Verify the placeholder text is displayed
    composeTestRule.onNodeWithText("Search for associations").assertIsDisplayed()
  }

  @Test
  fun testSearchQueryUpdatesOnInput() {
    composeTestRule.setContent { AssociationSearchBar(searchViewModel, onAssociationSelected = {}) }

    // Enter text into the search input field
    composeTestRule.onNodeWithTag("SEARCH_BAR_INPUT").performTextInput("Student Club")

    // Verify that the search query has been updated
    composeTestRule.onNodeWithTag("SEARCH_BAR_INPUT").assertTextEquals("Student Club")
  }

  @Test
  fun testSearchButtonClick() {
    composeTestRule.setContent { AssociationSearchBar(searchViewModel, onAssociationSelected = {}) }

    // Verify the search icon is displayed
    composeTestRule.onNodeWithTag("SEARCH_TRAILING_ICON").assertIsDisplayed()

    // Simulate clicking the search button (icon)
    composeTestRule.onNodeWithTag("SEARCH_TRAILING_ICON").performClick()

    // Ensure the search action is triggered (you can assert if needed, like checking some state
    // change)
    // You might want to mock a function or verify that the corresponding search function gets
    // triggered
  }

  @Test
  fun testErrorStateDisplaysMessage() {
    // Mock error state
    composeTestRule.setContent { AssociationSearchBar(searchViewModel, onAssociationSelected = {}) }

    // Verify the error message is displayed
    composeTestRule.onNodeWithText("Error occurred during search").assertIsDisplayed()
  }

  @Test
  fun testNoResultsDisplaysMessage() {

    composeTestRule.setContent { AssociationSearchBar(searchViewModel, onAssociationSelected = {}) }

    // Verify the no results message is displayed
    composeTestRule.onNodeWithText("No results found").assertIsDisplayed()
  }

  @Test
  fun allComponentsAreDisplayed() {
    composeTestRule.setContent { AssociationSearchBar(searchViewModel, onAssociationSelected = {}) }
    // composeTestRule.onNodeWithTag(ExploreTestTags.EXPLORE_SCAFFOLD_TITLE).assertIsDisplayed()
    composeTestRule
        .onNodeWithTag(ExploreContentTestTags.SEARCH_BAR_PLACEHOLDER, true)
        .assertIsDisplayed()
    composeTestRule
        .onNodeWithTag(ExploreContentTestTags.SEARCH_TRAILING_ICON, true)
        .assertIsDisplayed()
    composeTestRule.onNodeWithTag(ExploreContentTestTags.SEARCH_BAR).assertIsDisplayed()
    composeTestRule.onNodeWithTag(ExploreContentTestTags.TITLE_TEXT).assertIsDisplayed()
    composeTestRule.onNodeWithTag(ExploreContentTestTags.CATEGORIES_LIST).assertExists()
  }

  @Test
  fun testAssociationItemClick() {
    // Mock successful search with some associations
    val testAssociations =
        listOf(
            MockAssociation.createMockAssociation(name = "Student Club 1", uid = "1"),
            MockAssociation.createMockAssociation(name = "Student Club 2", uid = "2"))

    composeTestRule.setContent { AssociationSearchBar(searchViewModel, onAssociationSelected = {}) }

    // Verify the list of associations are displayed
    composeTestRule.onNodeWithText("Student Club 1").assertIsDisplayed()
    composeTestRule.onNodeWithText("Student Club 2").assertIsDisplayed()

    // Simulate selecting the first association
    composeTestRule.onNodeWithText("Student Club 1").performClick()

    // Ensure that the correct association is selected
    // Here you can check the selected association or mock the onAssociationSelected callback to
    // verify
  }
}
