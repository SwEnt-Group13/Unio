package com.android.unio.ui.explore

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertTextEquals
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onFirst
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import com.android.unio.model.association.MockAssociationType
import com.android.unio.ui.navigation.NavigationAction
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.Mockito.mock
import org.mockito.Mockito.`when`
import org.mockito.kotlin.any
import org.mockito.kotlin.verify

class ExploreScreenTest {
  private lateinit var navigationAction: NavigationAction

  @get:Rule val composeTestRule = createComposeRule()

  @Before
  fun setUp() {
    navigationAction = mock(NavigationAction::class.java)

    // Mock the navigation action to do nothing
    `when`(navigationAction.navigateTo(any<String>())).then {}
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
        getFilteredAssociationsByCategoryAndAlphabeticalOrder(MockAssociationType.MUSIC)
    assertEquals(1, musicAssociations.size)
    assertEquals("Musical", musicAssociations[0].association.acronym)

    val festivalAssociations =
        getFilteredAssociationsByCategoryAndAlphabeticalOrder(MockAssociationType.FESTIVALS)
    assertEquals(4, festivalAssociations.size)
    assertEquals("Artiphys", festivalAssociations[0].association.acronym)
  }

  @Test
  fun testGetCategoryNameWithFirstLetterUppercase() {
    val musicCategory = getCategoryNameWithFirstLetterUppercase(MockAssociationType.MUSIC)
    assertEquals("Music", musicCategory)

    val festivalCategory = getCategoryNameWithFirstLetterUppercase(MockAssociationType.FESTIVALS)
    assertEquals("Festivals", festivalCategory)
  }

  @Test
  fun testClickOnAssociation() {
    composeTestRule.setContent { ExploreScreen(navigationAction) }

    composeTestRule.onAllNodesWithTag("associationItem").onFirst().performClick()

    verify(navigationAction).navigateTo(any<String>())
  }
}
