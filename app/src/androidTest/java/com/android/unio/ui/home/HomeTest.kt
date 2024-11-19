package com.android.unio.ui.home

import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.test.assertHasClickAction
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsNotDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.unit.ExperimentalUnitApi
import com.android.unio.R
import com.android.unio.mocks.association.MockAssociation
import com.android.unio.mocks.event.MockEvent
import com.android.unio.mocks.user.MockUser
import com.android.unio.model.event.Event
import com.android.unio.model.event.EventRepositoryFirestore
import com.android.unio.model.event.EventRepositoryMock
import com.android.unio.model.event.EventViewModel
import com.android.unio.model.hilt.module.FirebaseModule
import com.android.unio.model.image.ImageRepositoryFirebaseStorage
import com.android.unio.model.search.SearchRepository
import com.android.unio.model.search.SearchViewModel
import com.android.unio.model.strings.test_tags.HomeTestTags
import com.android.unio.model.user.UserRepositoryFirestore
import com.android.unio.model.user.UserViewModel
import com.android.unio.ui.assertDisplayComponentInScroll
import com.android.unio.ui.navigation.NavigationAction
import com.android.unio.ui.navigation.Screen
import com.android.unio.ui.navigation.TopLevelDestination
import com.google.firebase.firestore.FirebaseFirestore
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.testing.BindValue
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import dagger.hilt.android.testing.UninstallModules
import dagger.hilt.components.SingletonComponent
import io.mockk.MockKAnnotations
import io.mockk.clearAllMocks
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.just
import io.mockk.mockk
import io.mockk.runs
import io.mockk.spyk
import io.mockk.unmockkAll
import io.mockk.verify
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.runBlockingTest
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test

/**
 * Test class for the HomeScreen Composable. This class contains unit tests to validate the behavior
 * of the Event List UI.
 */
@HiltAndroidTest
@ExperimentalUnitApi
@UninstallModules(FirebaseModule::class)
class HomeTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @get:Rule
    val hiltRule = HiltAndroidRule(this)

    private lateinit var userViewModel: UserViewModel

    // Mock event repository to provide test data.
    @MockK
    private lateinit var eventRepository: EventRepositoryFirestore

    @MockK
    private lateinit var userRepository: UserRepositoryFirestore

    @MockK
    private lateinit var navigationAction: NavigationAction

    @MockK
    private lateinit var imageRepository: ImageRepositoryFirebaseStorage

    private lateinit var eventViewModel: EventViewModel
    private lateinit var searchViewModel: SearchViewModel

    @MockK(relaxed = true)
    private lateinit var searchRepository: SearchRepository

    private lateinit var eventList: List<Event>
    private lateinit var eventListFollowed: List<Event>

    @Before
    fun setUp() {
        MockKAnnotations.init(this)
        hiltRule.inject()
        searchViewModel = spyk(SearchViewModel(searchRepository))
        every { navigationAction.navigateTo(any(TopLevelDestination::class)) } returns Unit
        every { navigationAction.navigateTo(any(String::class)) } returns Unit

        every { userRepository.init(any()) } just runs
        every { eventRepository.init(any()) } just runs
        userViewModel = spyk(UserViewModel(userRepository))
        val user = MockUser.createMockUser()
        val asso = MockAssociation.createMockAssociation()

        every { userRepository.updateUser(user, any(), any()) } answers
                {
                    val onSuccess = args[1] as () -> Unit
                    onSuccess()
                }
        userViewModel.addUser(user, {})
        every { userRepository.init(any()) } just runs
        every { eventRepository.getEvents(any(), any()) } answers {
            val onSuccess = args[0] as (List<Event>) -> Unit
            onSuccess(eventList)
        }
        eventViewModel = EventViewModel(eventRepository, imageRepository)

        val field = userViewModel.javaClass.getDeclaredMethod("setFollowedAssociations", List::class.java)
        field.isAccessible = true
        field.invoke(userViewModel, listOf(asso.uid))

        eventList =
            listOf(
                MockEvent.createMockEvent(organisers = listOf(asso)),
                MockEvent.createMockEvent(title = "I am different")
            )

        val eventField = eventViewModel.javaClass.getDeclaredMethod("setEvents", List::class.java)
        eventField.isAccessible = true
        eventField.invoke(eventViewModel, eventList)

        assert(eventViewModel.events.value.isNotEmpty())

        eventListFollowed =
            asso.let { eventList.filter { event -> event.organisers.contains(it.uid) } }
    }

    /**
     * Tests the UI when the event list is empty. Asserts that the appropriate message is displayed
     * when there are no events available.
     */
    @Test
    fun testEmptyEventList() {
        var text = ""
        composeTestRule.setContent {
            val context = LocalContext.current
            text = context.getString(R.string.event_no_events_available)
            val field = eventViewModel.javaClass.getDeclaredMethod("setEvents", List::class.java)
            field.isAccessible = true
            field.invoke(eventViewModel, emptyList<Event>())
            HomeScreen(navigationAction, eventViewModel, userViewModel, searchViewModel)
        }
        composeTestRule.onNodeWithTag(HomeTestTags.EMPTY_EVENT_PROMPT).assertExists()
        composeTestRule.onNodeWithText(text).assertExists()
    }

    @Test
    fun testEventListAll() {
        composeTestRule.setContent {
            HomeScreen(navigationAction, eventViewModel, userViewModel, searchViewModel)
        }
        composeTestRule.onNodeWithTag(HomeTestTags.TAB_ALL).assertIsDisplayed()
        composeTestRule.onNodeWithTag(HomeTestTags.TAB_ALL).performClick()

        eventList.forEach { event ->
            assertDisplayComponentInScroll(composeTestRule.onNodeWithText(event.title))
        }
    }


    /**
     * Test the UI of the following screen. Asserts that the 'Following' tab is displayed and that the
     * list of events displayed is the same as the list of events followed by the user.
     */
    @Test
    fun testEventListFollowed() {
        composeTestRule.setContent {
            HomeScreen(navigationAction, eventViewModel, userViewModel, searchViewModel)
        }
        composeTestRule.onNodeWithTag(HomeTestTags.TAB_FOLLOWING).assertIsDisplayed()
        composeTestRule.onNodeWithTag(HomeTestTags.TAB_FOLLOWING).performClick()

        eventListFollowed.forEach { event ->
            assertDisplayComponentInScroll(composeTestRule.onNodeWithText(event.title))
        }
        val theNegative = eventList.filter { !eventListFollowed.contains(it) }
        theNegative.forEach { event ->
            composeTestRule.onNodeWithText(event.title).assertIsNotDisplayed()
        }
    }

    /**
     * Tests the functionality of the Map button. Verifies that clicking the button triggers the
     * expected action.
     */
    @Test
    fun testMapButton() {
        composeTestRule.setContent {
            val eventViewModel = EventViewModel(eventRepository, imageRepository)
            HomeScreen(navigationAction, eventViewModel, userViewModel, searchViewModel)
        }
        composeTestRule.onNodeWithTag(HomeTestTags.MAP_BUTTON).assertExists()
        composeTestRule.onNodeWithTag(HomeTestTags.MAP_BUTTON).assertHasClickAction()

        composeTestRule.onNodeWithTag(HomeTestTags.MAP_BUTTON).performClick()
        verify { navigationAction.navigateTo(Screen.MAP) }
    }

    /**
     * Tests the sequence of clicking on the 'Following' tab and then on the 'Map' button to ensure
     * that both actions trigger their respective animations and behaviors.
     */
    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun testClickFollowingAndAdd() = runBlockingTest {
        composeTestRule.setContent {
            val eventViewModel = EventViewModel(eventRepository, imageRepository)
            HomeScreen(navigationAction, eventViewModel, userViewModel, searchViewModel)
        }

        composeTestRule.onNodeWithTag(HomeTestTags.TAB_FOLLOWING).assertExists()
        composeTestRule.onNodeWithTag(HomeTestTags.TAB_FOLLOWING).performClick()

        composeTestRule.onNodeWithTag(HomeTestTags.MAP_BUTTON).assertExists()
        composeTestRule.onNodeWithTag(HomeTestTags.MAP_BUTTON).performClick()

        verify { navigationAction.navigateTo(Screen.MAP) }
    }

    @After
    fun tearDown() {
        clearAllMocks()
        unmockkAll()
    }

    @Module
    @InstallIn(SingletonComponent::class)
    object FirebaseTestModdule{
        @Provides
        fun provideFirestore(): FirebaseFirestore = mockk()
    }
}
