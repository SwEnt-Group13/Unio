package com.android.unio.ui.association

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.navigation.NavHostController
import com.android.unio.model.association.Association
import com.android.unio.model.association.AssociationRepository
import com.android.unio.model.association.AssociationViewModel
import com.android.unio.model.firestore.FirestorePaths.USER_PATH
import com.android.unio.model.firestore.FirestoreReferenceList
import com.android.unio.model.user.UserRepositoryFirestore
import com.android.unio.ui.navigation.NavigationAction
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.FirebaseFirestore
import junit.framework.TestCase.assertEquals
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.Mock
import org.mockito.Mockito.mock
import org.mockito.Mockito.`when`
import org.mockito.MockitoAnnotations
import org.mockito.kotlin.any
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify

class AssociationProfileTest {
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

    `when`(db.collection(any())).thenReturn(collectionReference)

    associations = listOf(Association(
      uid = "1",
      acronym = "ACM",
      fullName = "Association for Computing Machinery",
      description = "ACM is the world's largest educational and scientific computing society.",
      members = FirestoreReferenceList.empty(
        db.collection(USER_PATH),
        UserRepositoryFirestore::hydrate)
    ))

    navHostController = mock { NavHostController::class.java }
    navigationAction = NavigationAction(navHostController)

    associationViewModel = AssociationViewModel(associationRepository)
  }

  @Test
  fun testAssociationProfileDisplayedBadId() {
    composeTestRule.setContent { AssociationProfile(navigationAction, "") }

    composeTestRule.onNodeWithTag("AssociationProfileTitle").assertIsDisplayed()

    composeTestRule.onNodeWithTag("goBackButton").assertIsDisplayed()

    composeTestRule.onNodeWithTag("AssociationProfileScreen").assertIsDisplayed()

    composeTestRule.onNodeWithTag("associationNotFound").assertIsDisplayed()
  }

  @Test
  fun testGoBackButton() {
    composeTestRule.setContent { AssociationProfile(navigationAction, "") }

    composeTestRule.onNodeWithTag("goBackButton").performClick()

    verify(navHostController).popBackStack()
  }

  @Test
  fun testAssociationProfileGoodId() {
    `when`(associationRepository.getAssociations(any(), any())).thenAnswer { invocation ->
      val onSuccess = invocation.arguments[0] as (List<Association>) -> Unit
      onSuccess(associations)
    }

    associationViewModel.getAssociations()

    composeTestRule.runOnIdle {
      assertEquals(associations, associationViewModel.associations.value)
    }

    composeTestRule.setContent {
      AssociationProfile(navigationAction, "1", associationViewModel)
    }

    composeTestRule.onNodeWithTag("AssociationProfileTitle").assertIsDisplayed()
    composeTestRule.onNodeWithTag("goBackButton").assertIsDisplayed()
    composeTestRule.onNodeWithTag("AssociationProfileScreen").assertIsDisplayed()
    composeTestRule.onNodeWithTag("associationAcronym").assertIsDisplayed()
  }

}
