package com.android.unio.components.explore

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertTextEquals
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import com.android.unio.TearDown
import com.android.unio.mocks.association.MockAssociation
import com.android.unio.model.association.Association
import com.android.unio.model.association.AssociationCategory
import com.android.unio.model.association.AssociationRepositoryFirestore
import com.android.unio.model.association.AssociationViewModel
import com.android.unio.model.event.EventRepositoryFirestore
import com.android.unio.model.follow.ConcurrentAssociationUserRepositoryFirestore
import com.android.unio.model.image.ImageRepositoryFirebaseStorage
import com.android.unio.model.search.SearchRepository
import com.android.unio.model.search.SearchViewModel
import com.android.unio.model.strings.test_tags.ExploreContentTestTags
import com.android.unio.model.strings.test_tags.ExploreTestTags
import com.android.unio.ui.explore.ExploreScreen
import com.android.unio.ui.explore.getFilteredAssociationsByAlphabeticalOrder
import com.android.unio.ui.explore.getSortedEntriesAssociationsByCategory
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

@HiltAndroidTest
class ExploreScreenTest : TearDown() {
  @MockK private lateinit var navigationAction: NavigationAction
  @MockK private lateinit var associationRepository: AssociationRepositoryFirestore
  private lateinit var searchViewModel: SearchViewModel
  @MockK private lateinit var searchRepository: SearchRepository
  @MockK
  private lateinit var concurrentAssociationUserRepositoryFirestore:
      ConcurrentAssociationUserRepositoryFirestore
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
        AssociationViewModel(
            associationRepository,
            eventRepository,
            imageRepository,
            concurrentAssociationUserRepositoryFirestore)
  }

  @Test
  fun allComponentsAreDisplayed() {
    composeTestRule.setContent {
      ExploreScreen(navigationAction, associationViewModel, searchViewModel)
    }
    composeTestRule.onNodeWithTag(ExploreTestTags.EXPLORE_SCAFFOLD_TITLE).assertIsDisplayed()
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
  fun canTypeInSearchBar() {
    composeTestRule.setContent {
      ExploreScreen(navigationAction, associationViewModel, searchViewModel)
    }
    composeTestRule.onNodeWithTag(ExploreContentTestTags.SEARCH_BAR_INPUT).performTextInput("Music")
    composeTestRule.onNodeWithTag(ExploreContentTestTags.SEARCH_BAR_INPUT).assertTextEquals("Music")
  }

  @Test
  fun testGetFilteredAssociationsByAlphabeticalOrder() {
    val result = getFilteredAssociationsByAlphabeticalOrder(associations)
    assertEquals(associations[0].name, result[0].name)
    assertEquals(associations[1].name, result[1].name)
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
      composeTestRule
          .onNodeWithTag(ExploreContentTestTags.CATEGORY_NAME + category.name, true)
          .assertIsDisplayed()
      composeTestRule
          .onNodeWithTag(ExploreContentTestTags.ASSOCIATION_ROW + category.name, true)
          .assertIsDisplayed()
      associations.forEach { association ->
        composeTestRule
            .onNodeWithTag(ExploreContentTestTags.ASSOCIATION_ITEM + association.name, true)
            .assertIsDisplayed()
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
        composeTestRule
            .onNodeWithTag(ExploreContentTestTags.ASSOCIATION_ITEM + it.name)
            .performClick()
      }
    }

    verify(atLeast = 1) { navigationAction.navigateTo(Screen.ASSOCIATION_PROFILE) }
  }
}
