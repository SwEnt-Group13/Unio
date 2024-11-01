package com.android.unio.ui.event

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
import com.android.unio.model.event.Event
import com.android.unio.model.event.EventListViewModel
import com.android.unio.model.event.EventRepository
import com.android.unio.model.firestore.firestoreReferenceListWith
import com.android.unio.model.user.User
import com.android.unio.model.user.UserRepository
import com.android.unio.model.user.UserViewModel
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

class EventDetailsTest {
    private lateinit var navHostController: NavHostController
    private lateinit var navigationAction: NavigationAction
    @Mock private lateinit var collectionReference: CollectionReference
    @Mock private lateinit var db: FirebaseFirestore
    @Mock private lateinit var eventRepository: EventRepository
    @Mock private lateinit var userRepository: UserRepository
    private lateinit var eventListViewModel: EventListViewModel
    private lateinit var userViewModel: UserViewModel

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

        navHostController = mock { NavHostController::class.java }
        navigationAction = NavigationAction(navHostController)

        eventListViewModel = EventListViewModel(eventRepository)
        userViewModel = UserViewModel(userRepository, true)
    }

    private fun setEventScreen() {
        composeTestRule.setContent {
            EventScreen(navigationAction, "1", eventListViewModel, userViewModel)
        }
    }
    @Test
    fun testEventDetailsDisplayComponent() {
        setEventScreen()
        composeTestRule.waitForIdle()

        assertDisplayComponentInScroll(composeTestRule.onNodeWithTag("EventScreen"))
        assertDisplayComponentInScroll(composeTestRule.onNodeWithTag("goBackButton"))


        assertDisplayComponentInScroll(composeTestRule.onNodeWithTag("eventSaveButton"))
        assertDisplayComponentInScroll(composeTestRule.onNodeWithTag("eventShareButton"))
        assertDisplayComponentInScroll(composeTestRule.onNodeWithTag("eventDetailsPage"))
        assertDisplayComponentInScroll(composeTestRule.onNodeWithTag("eventDetailsImage"))
        assertDisplayComponentInScroll(composeTestRule.onNodeWithTag("eventDetailsInformationCard"))
        assertDisplayComponentInScroll(composeTestRule.onNodeWithTag("eventTitle"))
        assertDisplayComponentInScroll(composeTestRule.onNodeWithTag("eventOrganisingAssociation0"))
        assertDisplayComponentInScroll(composeTestRule.onNodeWithTag("eventOrganisingAssociation1"))
        assertDisplayComponentInScroll(composeTestRule.onNodeWithTag("associationLogo0"))
        assertDisplayComponentInScroll(composeTestRule.onNodeWithTag("associationName0"))
        assertDisplayComponentInScroll(composeTestRule.onNodeWithTag("associationLogo1"))
        assertDisplayComponentInScroll(composeTestRule.onNodeWithTag("associationName1"))
        assertDisplayComponentInScroll(composeTestRule.onNodeWithTag("eventStartHour"))
        assertDisplayComponentInScroll(composeTestRule.onNodeWithTag("eventDate"))
        assertDisplayComponentInScroll(
            composeTestRule.onNodeWithTag("eventDetailsBody"))
        assertDisplayComponentInScroll(composeTestRule.onNodeWithTag("placesRemainingText"))
        assertDisplayComponentInScroll(composeTestRule.onNodeWithTag("eventDescription"))
        assertDisplayComponentInScroll(composeTestRule.onNodeWithTag("eventLocation"))
        assertDisplayComponentInScroll(composeTestRule.onNodeWithTag("mapButton"))
        assertDisplayComponentInScroll(composeTestRule.onNodeWithTag("signUpButton"))
    }

    private fun assertDisplayComponentInScroll(compose: SemanticsNodeInteraction) {
        if (compose.isNotDisplayed()) {
            compose.performScrollTo()
        }
        compose.assertIsDisplayed()
    }

    @Test
    fun testButtonBehavior() {
        setEventScreen()
        // Share button
        composeTestRule.onNodeWithTag("eventShareButton").performClick()
        assertSnackBarIsDisplayed()

        // Save button
        composeTestRule.onNodeWithTag("eventSaveButton").performClick()
        assertSnackBarIsDisplayed()

        // Location button
        composeTestRule.onNodeWithTag("mapButton").performClick()
        assertSnackBarIsDisplayed()

        // Sign-up button
        composeTestRule.onNodeWithTag("signUpButton").performClick()
        assertSnackBarIsDisplayed()
    }

    private fun assertSnackBarIsDisplayed() {
        composeTestRule.onNodeWithTag("eventSnackbarHost").assertIsDisplayed()
        composeTestRule.onNodeWithTag("snackbarActionButton").performClick()
        composeTestRule.onNodeWithTag("eventSnackbarHost").assertIsNotDisplayed()
    }

    @Test
    fun testGoBackButton() {
        setEventScreen()
        composeTestRule.onNodeWithTag("goBackButton").performClick()
        verify(navHostController).popBackStack()
    }
}