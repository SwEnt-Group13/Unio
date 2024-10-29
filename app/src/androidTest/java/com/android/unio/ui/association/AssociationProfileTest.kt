package com.android.unio.ui.association

import androidx.compose.ui.test.SemanticsNodeInteraction
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsNotDisplayed
import androidx.compose.ui.test.isNotDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.navigation.NavHostController
import com.android.unio.model.association.Association
import com.android.unio.model.association.AssociationCategory
import com.android.unio.model.association.AssociationRepository
import com.android.unio.model.association.AssociationViewModel
import com.android.unio.model.event.Event
import com.android.unio.model.event.EventRepository
import com.android.unio.model.firestore.firestoreReferenceListWith
import com.android.unio.model.user.User
import com.android.unio.ui.navigation.NavigationAction
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.FirebaseFirestore
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
  @Mock private lateinit var eventRepository: EventRepository
  private lateinit var associationViewModel: AssociationViewModel

  private lateinit var associations: List<Association>
  private lateinit var events: List<Event>

  @get:Rule val composeTestRule = createComposeRule()

  @Before
  fun setUp() {
    MockitoAnnotations.openMocks(this)

    associations =
        listOf(
            Association(
                uid = "1",
                url = "this is an url",
                name = "ACM",
                fullName = "Association for Computing Machinery",
                category = AssociationCategory.SCIENCE_TECH,
                description =
                    "ACM is the world's largest educational and scientific computing society.",
                members = User.firestoreReferenceListWith(listOf("1", "2", "3")),
                followersCount = 321,
                image = "https://www.example.com/image.jpg"),
            Association(
                uid = "2",
                url = "this is an url",
                name = "IEEE",
                fullName = "Institute of Electrical and Electronics Engineers",
                category = AssociationCategory.SCIENCE_TECH,
                description =
                    "IEEE is the world's largest technical professional organization dedicated to advancing technology for the benefit of humanity.",
                members = User.firestoreReferenceListWith(listOf("4", "5", "6")),
                followersCount = 654,
                image = "https://www.example.com/image.jpg"))

    events =
        listOf(
            Event(
                uid = "a",
                title = "Event A",
                organisers = Association.firestoreReferenceListWith(listOf("1")),
                taggedAssociations = Association.firestoreReferenceListWith(listOf("1")),
                image = "",
                description = "Description of event A",
                catchyDescription = "Catchy description of event A",
                price = 0.0,
            ),
            Event(
                uid = "b",
                title = "Event B",
                organisers = Association.firestoreReferenceListWith(listOf("1")),
                taggedAssociations = Association.firestoreReferenceListWith(listOf("1")),
                image = "",
                description = "Description of event B",
                catchyDescription = "Catchy description of event B",
                price = 0.0,
            ))

    `when`(db.collection(any())).thenReturn(collectionReference)
    `when`(associationRepository.getAssociations(any(), any())).thenAnswer { invocation ->
      val onSuccess = invocation.arguments[0] as (List<Association>) -> Unit
      onSuccess(this.associations)
    }
    `when`(eventRepository.getEventsOfAssociation(any(), any(), any())).thenAnswer { invocation ->
      val onSuccess = invocation.arguments[1] as (List<Event>) -> Unit
      onSuccess(events)
    }

    navHostController = mock { NavHostController::class.java }
    navigationAction = NavigationAction(navHostController)

    associationViewModel = AssociationViewModel(associationRepository, eventRepository)
    associationViewModel.getAssociations()
  }

  @Test
  fun testAssociationProfileDisplayComponent() {
    composeTestRule.setContent {
      AssociationProfileScreen(navigationAction, "1", associationViewModel)
    }
    composeTestRule.waitForIdle()

    assertDisplayComponentInScroll(composeTestRule.onNodeWithTag("AssociationScreen"))
    assertDisplayComponentInScroll(composeTestRule.onNodeWithTag("goBackButton"))

    assertDisplayComponentInScroll(composeTestRule.onNodeWithTag("AssociationImageHeader"))
    assertDisplayComponentInScroll(composeTestRule.onNodeWithTag("AssociationProfileTitle"))
    assertDisplayComponentInScroll(composeTestRule.onNodeWithTag("associationShareButton"))
    assertDisplayComponentInScroll(composeTestRule.onNodeWithTag("AssociationHeaderFollowers"))
    assertDisplayComponentInScroll(composeTestRule.onNodeWithTag("AssociationHeaderMembers"))
    assertDisplayComponentInScroll(composeTestRule.onNodeWithTag("AssociationFollowButton"))
    assertDisplayComponentInScroll(composeTestRule.onNodeWithTag("AssociationDescription"))
    assertDisplayComponentInScroll(composeTestRule.onNodeWithTag("AssociationEventTitle"))
    assertDisplayComponentInScroll(composeTestRule.onNodeWithTag("AssociationEventCard-a"))
    assertDisplayComponentInScroll(composeTestRule.onNodeWithTag("AssociationSeeMoreButton"))
    assertDisplayComponentInScroll(composeTestRule.onNodeWithTag("AssociationContactMembersTitle"))
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
      AssociationProfileScreen(navigationAction, "1", associationViewModel)
    }
    // Share button
    assertDisplayComponentInScroll(composeTestRule.onNodeWithTag("associationShareButton"))
    composeTestRule.onNodeWithTag("associationShareButton").performClick()
    assertSnackBarIsDisplayed()

    // Follow button
    assertDisplayComponentInScroll(composeTestRule.onNodeWithTag("AssociationFollowButton"))
    composeTestRule.onNodeWithTag("AssociationFollowButton").performClick()
    assertSnackBarIsDisplayed()

    // See more button
    assertDisplayComponentInScroll(composeTestRule.onNodeWithTag("AssociationSeeMoreButton"))
    composeTestRule.onNodeWithTag("AssociationSeeMoreButton").performClick()
    assertDisplayComponentInScroll(composeTestRule.onNodeWithTag("AssociationEventCard-b"))

    // Roles buttons
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
    composeTestRule.setContent {
      AssociationProfileScreen(navigationAction, "1", associationViewModel)
    }

    assertDisplayComponentInScroll(composeTestRule.onNodeWithTag("AssociationProfileTitle"))
    assertDisplayComponentInScroll(composeTestRule.onNodeWithText(this.associations.first().fullName))
  }

  @Test
  fun testAssociationProfileBadId() {
    composeTestRule.setContent {
      AssociationProfileScreen(navigationAction, "IDONOTEXIST", associationViewModel)
    }

    assertDisplayComponentInScroll(composeTestRule.onNodeWithTag("associationNotFound"))
  }
}
