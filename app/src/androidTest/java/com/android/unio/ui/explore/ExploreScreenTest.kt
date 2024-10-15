package com.android.unio.ui.explore

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertTextEquals
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performTextInput
import com.android.unio.model.association.Association
import com.android.unio.model.association.AssociationCategory
import com.android.unio.model.firestore.FirestorePaths.USER_PATH
import com.android.unio.model.firestore.FirestoreReferenceList
import com.android.unio.model.firestore.transform.hydrate
import com.android.unio.model.user.UserRepositoryFirestore
import com.android.unio.ui.navigation.NavigationAction
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.FirebaseFirestore
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.Mock
import org.mockito.Mockito.mock
import org.mockito.Mockito.`when`
import org.mockito.MockitoAnnotations
import org.mockito.kotlin.any

class ExploreScreenTest {
  private lateinit var navigationAction: NavigationAction
  private lateinit var associations: List<Association>
  @Mock private lateinit var db: FirebaseFirestore
  @Mock private lateinit var collectionReference: CollectionReference

  @get:Rule val composeTestRule = createComposeRule()

  @Before
  fun setUp() {
    MockitoAnnotations.openMocks(this)
    navigationAction = mock(NavigationAction::class.java)

    // Mock the navigation action to do nothing
    `when`(navigationAction.navigateTo(any<String>())).then {}

    `when`(db.collection(any())).thenReturn(collectionReference)
    associations =
        listOf(
            Association(
                uid = "1",
                url = "",
                name = "ACM",
                fullName = "Association for Computing Machinery",
                category = AssociationCategory.SCIENCE_TECH,
                description =
                    "ACM is the world's largest educational and scientific computing society.",
                members =
                    FirestoreReferenceList.empty(
                        db.collection(USER_PATH), UserRepositoryFirestore.Companion::hydrate)),
            Association(
                uid = "2",
                url = "",
                name = "Musical",
                fullName = "-",
                category = AssociationCategory.ARTS,
                description = "Musical is the world's largest music society.",
                members =
                    FirestoreReferenceList.empty(
                        db.collection(USER_PATH), UserRepositoryFirestore.Companion::hydrate)))
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
  fun testGetFilteredAssociationsByAlphabeticalOrder() {
    val result = getFilteredAssociationsByAlphabeticalOrder(associations)
    assertEquals(associations[0].name, result[0].name)
    assertEquals(associations[1].name, result[1].name)
  }

    @Test
    fun testGetFilteredAssociationsByCategory() {
        val associationsByCategory = associations.groupBy { it.category }
        val sortedByCategoryAssociations = getSortedEntriesAssociationsByCategory(associationsByCategory)

        assertEquals(AssociationCategory.ARTS, sortedByCategoryAssociations[0].key)
        assertEquals(AssociationCategory.SCIENCE_TECH, sortedByCategoryAssociations[1].key)
    }
}
