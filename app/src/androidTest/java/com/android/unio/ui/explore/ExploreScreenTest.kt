package com.android.unio.ui.explore

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertTextEquals
import androidx.compose.ui.test.isDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import com.android.unio.mocks.association.MockAssociation
import com.android.unio.model.association.Association
import com.android.unio.model.association.AssociationCategory
import com.android.unio.model.association.AssociationRepositoryFirestore
import com.android.unio.model.association.AssociationViewModel
import com.android.unio.model.event.EventRepositoryFirestore
import com.android.unio.model.image.ImageRepositoryFirebaseStorage
import com.android.unio.model.search.SearchRepository
import com.android.unio.model.search.SearchViewModel
import com.android.unio.ui.navigation.NavigationAction
import com.android.unio.ui.navigation.Screen
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import io.mockk.MockKAnnotations
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.spyk
import io.mockk.verify
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.MockitoAnnotations

@HiltAndroidTest
class ExploreScreenTest {
  @MockK private lateinit var navigationAction: NavigationAction
  @MockK private lateinit var associationRepository: AssociationRepositoryFirestore
  private lateinit var searchViewModel: SearchViewModel
  @MockK private lateinit var searchRepository: SearchRepository
  @MockK private lateinit var eventRepository: EventRepositoryFirestore
  @MockK private lateinit var imageRepository: ImageRepositoryFirebaseStorage
  private lateinit var associationViewModel: AssociationViewModel

  @get:Rule val composeTestRule = createComposeRule()
  @get:Rule val hiltRule = HiltAndroidRule(this)

  private lateinit var associations: List<Association>
  private lateinit var sortedByCategoryAssociations:
      List<Map.Entry<AssociationCategory, List<Association>>>

  @Before
  fun setUp() {
    MockitoAnnotations.openMocks(this)
    MockKAnnotations.init(this, relaxed = true)
    searchViewModel = spyk(SearchViewModel(searchRepository))

    // Mock the navigation action to do nothing
    every { navigationAction.navigateTo(any<String>()) } returns Unit

    associations =
        listOf(
            MockAssociation.createMockAssociation(
                uid = "1", name = "ACM", category = AssociationCategory.SCIENCE_TECH),
            MockAssociation.createMockAssociation(
                uid = "2", name = "Musical", category = AssociationCategory.ARTS),
        )

    every { associationRepository.init {} } returns Unit
    every { associationRepository.getAssociations(any(), any()) } answers
        {
          val onSuccess = args[0] as (List<Association>) -> Unit
          onSuccess(associations)
        }

    sortedByCategoryAssociations =
        getSortedEntriesAssociationsByCategory(associations.groupBy { it.category })

    associationViewModel =
        AssociationViewModel(associationRepository, eventRepository, imageRepository)
  }

  @Test
  fun allComponentsAreDisplayed() {
    composeTestRule.setContent {
      ExploreScreen(navigationAction, associationViewModel, searchViewModel)
    }
    composeTestRule.onNodeWithTag("exploreScreen").assertIsDisplayed()
    composeTestRule.onNodeWithTag("searchPlaceHolder", true).assertIsDisplayed()
    composeTestRule.onNodeWithTag("searchTrailingIcon", true).assertIsDisplayed()
    composeTestRule.onNodeWithTag("searchBar").assertIsDisplayed()
    composeTestRule.onNodeWithTag("exploreTitle").assertIsDisplayed()
    composeTestRule.onNodeWithTag("categoriesList").assertExists()
  }

  @Test
  fun canTypeInSearchBar() {
    composeTestRule.setContent {
      ExploreScreen(navigationAction, associationViewModel, searchViewModel)
    }
    composeTestRule.onNodeWithTag("searchBarInput").performTextInput("Music")
    composeTestRule.onNodeWithTag("searchBarInput").assertTextEquals("Music")
  }

  @Test
  fun testGetFilteredAssociationsByAlphabeticalOrder() {
    val result = getFilteredAssociationsByAlphabeticalOrder(associations)
    assertEquals(associations[0].name, result[0].name) // Still true if all 4 associations are used.
    assertEquals(associations[1].name, result[1].name) // Not true if all 4 associations are used.
    // assertEquals(associations[3].name, result[1].name)
    // assertEquals(associations[1].name, result[2].name)
    // assertEquals(associations[2].name, result[3].name)
  }

  @Test
  fun testGetFilteredAssociationsByCategory() {
    val associationsByCategory = associations.groupBy { it.category }
    val sortedByCategoryAssociations =
        getSortedEntriesAssociationsByCategory(associationsByCategory)

    assertEquals(AssociationCategory.ARTS, sortedByCategoryAssociations[0].key)
    assertEquals(AssociationCategory.SCIENCE_TECH, sortedByCategoryAssociations[1].key)
  }

  @Test
  fun associationsAreDisplayed() {
    associationViewModel.getAssociations()
    composeTestRule.setContent {
      ExploreScreen(navigationAction, associationViewModel, searchViewModel)
    }

    sortedByCategoryAssociations.forEach { (category, associations) ->
      composeTestRule.onNodeWithTag("category_${category.name}", true).isDisplayed()
      composeTestRule.onNodeWithTag("associationRow_${category.name}", true).isDisplayed()
      associations.forEach { association ->
        composeTestRule.onNodeWithTag("associationItem_${association.name}", true).isDisplayed()
      }
    }
  }

  @Test
  fun testClickOnAssociation() {
    associationViewModel.getAssociations()
    composeTestRule.setContent {
      ExploreScreen(navigationAction, associationViewModel, searchViewModel)
    }

    sortedByCategoryAssociations.forEach { (_, associations) ->
      associations.forEach {
        composeTestRule.onNodeWithTag("associationItem_${it.name}").performClick()
        verify {
          navigationAction.navigateTo(Screen.withParams(Screen.ASSOCIATION_PROFILE, it.uid))
        }
      }
    }
  }
}
