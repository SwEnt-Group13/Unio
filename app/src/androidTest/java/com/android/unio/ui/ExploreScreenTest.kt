package com.android.unio.ui

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertTextEquals
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performTextInput
import androidx.navigation.NavHostController
import com.android.unio.model.association.AssociationType
import com.android.unio.ui.explore.ExploreScreen
import com.android.unio.ui.explore.getCategoryNameWithFirstLetterUppercase
import com.android.unio.ui.explore.getFilteredAssociationsByCategoryAndAlphabeticalOrder
import com.android.unio.ui.navigation.NavigationAction
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.kotlin.mock

class ExploreScreenTest {
  private lateinit var navigationAction: NavigationAction

  @get:Rule val composeTestRule = createComposeRule()

  @Before
  fun setUp() {
    navigationAction = mock { NavHostController::class.java }
  }

  @Test
  fun allComponentsAreDisplayed() {
    composeTestRule.setContent { ExploreScreen(navigationAction) }
    composeTestRule.onNodeWithTag("exploreScreen").assertIsDisplayed()
    composeTestRule.onNodeWithTag("searchBar").assertIsDisplayed()
    composeTestRule.onNodeWithTag("exploreTitle").assertIsDisplayed()
    composeTestRule.onNodeWithTag("categoriesList").assertIsDisplayed()
  }

  @Test
  fun canTypeInSearchBar() {
    composeTestRule.setContent { ExploreScreen(navigationAction) }
    composeTestRule.onNodeWithTag("searchBarInput").performTextInput("Music")
    composeTestRule.onNodeWithTag("searchBarInput").assertTextEquals("Music")
  }

  @Test
  fun testGetFilteredAssociationsByCategory() {
    val musicAssociations =
        getFilteredAssociationsByCategoryAndAlphabeticalOrder(AssociationType.MUSIC)
    assertEquals(1, musicAssociations.size)
    assertEquals("Musical", musicAssociations[0].association.acronym)

    val festivalAssociations =
        getFilteredAssociationsByCategoryAndAlphabeticalOrder(AssociationType.FESTIVALS)
    assertEquals(4, festivalAssociations.size)
    assertEquals("Artiphys", festivalAssociations[0].association.acronym)
  }

  @Test
  fun testGetCategoryNameWithFirstLetterUppercase() {
    val musicCategory = getCategoryNameWithFirstLetterUppercase(AssociationType.MUSIC)
    assertEquals("Music", musicCategory)

    val festivalCategory = getCategoryNameWithFirstLetterUppercase(AssociationType.FESTIVALS)
    assertEquals("Festivals", festivalCategory)
  }
}
