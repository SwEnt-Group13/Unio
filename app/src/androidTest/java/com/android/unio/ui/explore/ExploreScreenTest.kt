package com.android.unio.ui.explore

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertTextEquals
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performTextInput
import com.android.unio.model.association.Association
import com.android.unio.model.association.AssociationCategory
import com.android.unio.model.association.AssociationRepository
import com.android.unio.model.association.AssociationViewModel
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
  @Mock private lateinit var db: FirebaseFirestore
  @Mock private lateinit var collectionReference: CollectionReference
  @Mock private lateinit var associationRepository: AssociationRepository
  private lateinit var associationViewModel: AssociationViewModel

  @get:Rule val composeTestRule = createComposeRule()

  private lateinit var associations: List<Association>

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
                fullName = "Music club",
                category = AssociationCategory.ARTS,
                description = "Musical is the world's largest music society.",
                members =
                    FirestoreReferenceList.empty(
                        db.collection(USER_PATH), UserRepositoryFirestore.Companion::hydrate)),
            //            Association(
            //                uid = "3",
            //                url = "",
            //                name = "OChe",
            //                fullName = "Orchestre de chambre des étudiant-e-s de Lausanne",
            //                category = AssociationCategory.ARTS,
            //                description = "Orchestre de chambre.",
            //                members =
            //                    FirestoreReferenceList.empty(
            //                        db.collection(USER_PATH),
            // UserRepositoryFirestore.Companion::hydrate)),
            //            Association(
            //                uid = "4",
            //                url = "",
            //                name = "AGEPoly",
            //                fullName = "Student’s general association of the EPFL",
            //                category = AssociationCategory.EPFL_BODIES,
            //                description = "Student’s general association.",
            //                members =
            //                    FirestoreReferenceList.empty(
            //                        db.collection(USER_PATH),
            // UserRepositoryFirestore.Companion::hydrate))
        )

    associationViewModel = AssociationViewModel(associationRepository)
  }

  @Test
  fun allComponentsAreDisplayed() {
    composeTestRule.setContent { ExploreScreen(navigationAction, associationViewModel) }
    composeTestRule.onNodeWithTag("exploreScreen").assertIsDisplayed()
    composeTestRule.onNodeWithTag("searchBar").assertIsDisplayed()
    composeTestRule.onNodeWithTag("exploreTitle").assertIsDisplayed()
    composeTestRule.onNodeWithTag("categoriesList").assertIsDisplayed()
  }

  @Test
  fun canTypeInSearchBar() {
    composeTestRule.setContent { ExploreScreen(navigationAction, associationViewModel) }
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
    // Not true if all 4 associations are used.
    assertEquals(AssociationCategory.SCIENCE_TECH, sortedByCategoryAssociations[1].key)
    // Two asserts below true only if all 4 associations are used.
    //    assertEquals(AssociationCategory.EPFL_BODIES, sortedByCategoryAssociations[1].key)
    //    assertEquals(AssociationCategory.SCIENCE_TECH, sortedByCategoryAssociations[2].key)
  }

  @Test
  fun associationsAreDisplayed() {
    `when`(associationRepository.getAssociations(any(), any())).thenAnswer { invocation ->
      val onSuccess = invocation.arguments[0] as (List<Association>) -> Unit
      onSuccess(associations)
    }

    associationViewModel.getAssociations()
    composeTestRule.setContent { ExploreScreen(navigationAction, associationViewModel) }

    val sortedByCategoryAssociations =
        getSortedEntriesAssociationsByCategory(associations.groupBy { it.category })

    sortedByCategoryAssociations.forEach { (category, associations) ->
      composeTestRule.onNodeWithTag("category_${category.displayName}").assertIsDisplayed()
      composeTestRule.onNodeWithTag("associationRow_${category.displayName}")
      associations.forEach { composeTestRule.onNodeWithTag("associationName_${it.name}") }
    }
  }
}
