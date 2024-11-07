package com.android.unio.ui.association

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.navigation.NavHostController
import com.android.unio.mocks.association.MockAssociation
import com.android.unio.model.association.Association
import com.android.unio.model.association.AssociationRepository
import com.android.unio.model.association.AssociationViewModel
import com.android.unio.ui.navigation.NavigationAction
import com.android.unio.ui.navigation.Screen
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.FirebaseFirestore
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.Mock
import org.mockito.Mockito
import org.mockito.MockitoAnnotations
import org.mockito.kotlin.any
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify

class EditAssociationTest {

  private lateinit var navHostController: NavHostController
  private lateinit var navigationAction: NavigationAction
  @Mock private lateinit var collectionReference: CollectionReference
  @Mock private lateinit var db: FirebaseFirestore
  @Mock private lateinit var associationRepository: AssociationRepository
  private lateinit var associationViewModel: AssociationViewModel

  private lateinit var associations: List<Association>

  @get:Rule val composeTestRule = createComposeRule()

  @Before
  fun setUp() {
    MockitoAnnotations.openMocks(this)

    associations =
        listOf(
            MockAssociation.createMockAssociation(uid = "1"),
            MockAssociation.createMockAssociation(uid = "2"))

    Mockito.`when`(db.collection(Mockito.anyString())).thenReturn(collectionReference)
    Mockito
        . // Correct way to mock methods that take argument matchers
        `when`(associationRepository.getAssociations(any(), any()))
        .thenAnswer { invocation ->
          val onSuccess: (List<Association>) -> Unit =
              invocation.arguments[0] as (List<Association>) -> Unit
          onSuccess(associations) // Simulating success callback
        }

    navHostController = mock()
    navigationAction = NavigationAction(navHostController)

    associationViewModel = AssociationViewModel(associationRepository, mock())
    associationViewModel.getAssociations()
  }

  @Test
  fun testEditAssociationScreenDisplaysCorrectly() {
    composeTestRule.setContent {
      EditAssociationScreen(
          associationId = "1",
          associationViewModel = associationViewModel,
          imageRepository = mock(),
          navigationAction = navigationAction)
    }

    composeTestRule.waitForIdle()

    assertDisplayComponentInScroll(composeTestRule.onNodeWithTag("EditAssociationScreen"))
    assertDisplayComponentInScroll(composeTestRule.onNodeWithTag("associationName"))
    assertDisplayComponentInScroll(composeTestRule.onNodeWithTag("associationFullName"))
    assertDisplayComponentInScroll(composeTestRule.onNodeWithTag("associationDescription"))
    assertDisplayComponentInScroll(composeTestRule.onNodeWithTag("associationImage"))
    assertDisplayComponentInScroll(composeTestRule.onNodeWithTag("associationUrl"))
  }

  private fun assertDisplayComponentInScroll(compose: SemanticsNodeInteraction) {
    if (compose.isNotDisplayed()) {
      compose.performScrollTo()
    }
    compose.assertIsDisplayed()
  }

  @Test
  fun testSaveAssociation() {
    composeTestRule.setContent {
      EditAssociationScreen(
          associationId = "1",
          associationViewModel = associationViewModel,
          imageRepository = mock(),
          navigationAction = navigationAction)
    }

    // Verify that the "Save" button is visible
    assertDisplayComponentInScroll(composeTestRule.onNodeWithTag("saveButton"))
    composeTestRule.onNodeWithTag("saveButton").performClick()

    // Verify that the navigation action to the AssociationProfile screen is called
    verify(navHostController).navigate(Screen.withParams(Screen.ASSOCIATION_PROFILE, "1"))
  }

  @Test
  fun testCancelButton() {
    composeTestRule.setContent {
      EditAssociationScreen(
          associationId = "1",
          associationViewModel = associationViewModel,
          imageRepository = mock(),
          navigationAction = navigationAction)
    }

    // Verify that the "Cancel" button is visible and performs the navigation correctly
    assertDisplayComponentInScroll(composeTestRule.onNodeWithTag("cancelButton"))
    composeTestRule.onNodeWithTag("cancelButton").performClick()

    // Verify that navigation to the AssociationProfile screen happens without saving
    verify(navHostController).navigate(Screen.withParams(Screen.ASSOCIATION_PROFILE, "1"))
  }

  @Test
  fun testErrorMessageWhenAssociationNotFound() {
    composeTestRule.setContent {
      EditAssociationScreen(
          associationId = "nonexistent_id",
          associationViewModel = associationViewModel,
          imageRepository = mock(),
          navigationAction = navigationAction)
    }

    assertDisplayComponentInScroll(composeTestRule.onNodeWithTag("associationNotFound"))
  }

  @Test
  fun testFieldsArePopulatedWithExistingData() {
    composeTestRule.setContent {
      EditAssociationScreen(
          associationId = "1",
          associationViewModel = associationViewModel,
          imageRepository = mock(),
          navigationAction = navigationAction)
    }

    // Verify that the fields are populated with the association data
    composeTestRule.onNodeWithTag("associationName").assert(hasText("Test Association"))
    composeTestRule.onNodeWithTag("associationFullName").assert(hasText("Test Full Name"))
    composeTestRule.onNodeWithTag("associationDescription").assert(hasText("Description of Test"))
  }

  @Test
  fun testSaveAssociationFailure() {
    composeTestRule.setContent {
      EditAssociationScreen(
          associationId = "1",
          associationViewModel = associationViewModel,
          imageRepository = mock(),
          navigationAction = navigationAction)
    }

    // Simulate failure during save
    Mockito.`when`(
            associationViewModel.saveAssociation(
                Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any()))
        .thenAnswer { invocation ->
          val onFailure = invocation.arguments[3] as (Throwable) -> Unit
          onFailure(Throwable("Save failed"))
        }

    // Trigger save
    composeTestRule.onNodeWithTag("saveButton").performClick()

    // Verify that a Toast with the "save failed" message appears
    composeTestRule.onNodeWithText("Save failed").assertIsDisplayed()
  }
}
