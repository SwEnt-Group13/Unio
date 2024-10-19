package com.android.unio.ui.association

import androidx.compose.ui.test.SemanticsNodeInteraction
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsNotDisplayed
import androidx.compose.ui.test.isNotDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.navigation.NavHostController
import com.android.unio.model.association.Association
import com.android.unio.model.association.AssociationCategory
import com.android.unio.model.association.AssociationRepository
import com.android.unio.model.association.AssociationViewModel
import com.android.unio.model.firestore.emptyFirestoreReferenceList
import com.android.unio.model.user.User
import com.android.unio.ui.navigation.NavigationAction
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.FirebaseFirestore
import junit.framework.TestCase.assertEquals
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.Mock
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
                members = User.emptyFirestoreReferenceList(),
                image = "https://www.example.com/image.jpg"))

    navHostController = mock { NavHostController::class.java }
    navigationAction = NavigationAction(navHostController)

    associationViewModel = AssociationViewModel(associationRepository)
  }

  @Test
  fun testAssociationProfileDisplayComponent() {
    composeTestRule.setContent {
      AssociationProfileScreen(navigationAction, "", associationViewModel)
    }

    composeTestRule.onNodeWithTag("AssociationScreen").assertIsDisplayed()
    composeTestRule.onNodeWithTag("goBackButton").assertIsDisplayed()

    assertDisplayComponentInScroll(composeTestRule.onNodeWithTag("AssociationImageHeader"))
    assertDisplayComponentInScroll(composeTestRule.onNodeWithTag("AssociationProfileTitle"))
    assertDisplayComponentInScroll(composeTestRule.onNodeWithTag("associationShareButton"))
    assertDisplayComponentInScroll(composeTestRule.onNodeWithTag("AssociationHeaderFollowers"))
    assertDisplayComponentInScroll(composeTestRule.onNodeWithTag("AssociationHeaderMembers"))
    assertDisplayComponentInScroll(composeTestRule.onNodeWithTag("AssociationFollowButton"))
    assertDisplayComponentInScroll(composeTestRule.onNodeWithTag("AssociationDescription"))
    assertDisplayComponentInScroll(composeTestRule.onNodeWithTag("AssociationEventTitle"))
    assertDisplayComponentInScroll(composeTestRule.onNodeWithTag("AssociationEventCard"))
    assertDisplayComponentInScroll(composeTestRule.onNodeWithTag("AssociationSeeMoreButton"))
    assertDisplayComponentInScroll(composeTestRule.onNodeWithTag("AssociationContactMembersTitle"))
    assertDisplayComponentInScroll(composeTestRule.onNodeWithTag("AssociationContactMembersCard"))
    assertDisplayComponentInScroll(
        composeTestRule.onNodeWithTag("AssociationRecruitmentDescription"))
    assertDisplayComponentInScroll(composeTestRule.onNodeWithTag("AssociationRecruitmentRoles"))
  }

  private fun assertDisplayComponentInScroll(compose: SemanticsNodeInteraction) {
    if (compose.isNotDisplayed()) {
      compose.performScrollTo()
    }
    compose.assertIsDisplayed()
  }

  @Test
  fun testButtonBehavior() {
    composeTestRule.setContent {
      AssociationProfileScreen(navigationAction, "", associationViewModel)
    }
    assertDisplayComponentInScroll(composeTestRule.onNodeWithTag("associationShareButton"))
    composeTestRule.onNodeWithTag("associationShareButton").performClick()
    assertSnackBarIsDisplayed()

    assertDisplayComponentInScroll(composeTestRule.onNodeWithTag("AssociationFollowButton"))
    composeTestRule.onNodeWithTag("AssociationFollowButton").performClick()
    assertSnackBarIsDisplayed()

    assertDisplayComponentInScroll(composeTestRule.onNodeWithTag("AssociationSeeMoreButton"))
    composeTestRule.onNodeWithTag("AssociationSeeMoreButton").performClick()
    assertSnackBarIsDisplayed()

    assertDisplayComponentInScroll(composeTestRule.onNodeWithTag("AssociationContactMembersCard"))
    composeTestRule.onNodeWithTag("AssociationContactMembersCard").performClick()
    assertSnackBarIsDisplayed()

    assertDisplayComponentInScroll(composeTestRule.onNodeWithTag("AssociationTreasurerRoles"))
    composeTestRule.onNodeWithTag("AssociationTreasurerRoles").performClick()
    assertSnackBarIsDisplayed()

    assertDisplayComponentInScroll(composeTestRule.onNodeWithTag("AssociationDesignerRoles"))
    composeTestRule.onNodeWithTag("AssociationDesignerRoles").performClick()
    assertSnackBarIsDisplayed()
  }

  private fun assertSnackBarIsDisplayed() {
    composeTestRule.onNodeWithTag("associationSnackbarHost").assertIsDisplayed()
    composeTestRule.onNodeWithTag("snackbarActionButton").performClick()
    composeTestRule.onNodeWithTag("associationSnackbarHost").assertIsNotDisplayed()
  }

  @Test
  fun testGoBackButton() {
    composeTestRule.setContent {
      AssociationProfileScreen(navigationAction, "", associationViewModel)
    }

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
      AssociationProfileScreen(navigationAction, associations.first().uid, associationViewModel)
    }

    composeTestRule.onNodeWithTag("AssociationProfileTitle").assertIsDisplayed()
    composeTestRule.onNodeWithTag("goBackButton").assertIsDisplayed()
    composeTestRule.onNodeWithTag("AssociationScreen").assertIsDisplayed()
    // TODO uncomment when implementing the association logic
    //    composeTestRule.onNodeWithTag("associationAcronym").assertIsDisplayed()
    //    composeTestRule
    //        .onNodeWithText("Association acronym: ${associations.first().acronym}")
    //        .assertIsDisplayed()
  }
}
