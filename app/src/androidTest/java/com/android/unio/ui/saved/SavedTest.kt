package com.android.unio.ui.saved

import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import com.android.unio.mocks.association.MockAssociation
import com.android.unio.mocks.event.MockEvent
import com.android.unio.mocks.user.MockUser
import com.android.unio.model.event.Event
import com.android.unio.model.event.EventRepositoryFirestore
import com.android.unio.model.event.EventViewModel
import com.android.unio.model.image.ImageRepositoryFirebaseStorage
import com.android.unio.model.search.SearchRepository
import com.android.unio.model.search.SearchViewModel
import com.android.unio.model.strings.test_tags.SavedTestTags
import com.android.unio.model.user.UserRepositoryFirestore
import com.android.unio.model.user.UserViewModel
import com.android.unio.ui.assertDisplayComponentInScroll
import com.android.unio.ui.navigation.NavigationAction
import com.android.unio.ui.navigation.TopLevelDestination
import com.google.firebase.Timestamp
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import io.mockk.MockKAnnotations
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.just
import io.mockk.runs
import io.mockk.spyk
import org.junit.Before
import org.junit.Rule
import org.junit.Test

@HiltAndroidTest
class SavedTest {
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

    @MockK(relaxed = true) private lateinit var searchRepository: SearchRepository

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
        every { eventRepository.getEvents(any(), any()) } answers
                {
                    val onSuccess = args[0] as (List<Event>) -> Unit
                    onSuccess(eventList)
                }
        eventViewModel = EventViewModel(eventRepository, imageRepository)

        val field =
            userViewModel.javaClass.getDeclaredMethod("setFollowedAssociations", List::class.java)
        field.isAccessible = true
        field.invoke(userViewModel, listOf(asso.uid))

        eventList =
            listOf(
                MockEvent.createMockEvent(organisers = listOf(asso)),
                MockEvent.createMockEvent(title = "I am different", startDate = Timestamp.now()))

        val eventField = eventViewModel.javaClass.getDeclaredMethod("setEvents", List::class.java)
        eventField.isAccessible = true
        eventField.invoke(eventViewModel, eventList)

        every {userViewModel.isEventSavedForCurrentUser(any())} returns true

        assert(eventViewModel.events.value.isNotEmpty())
        eventListFollowed = asso.let { eventList.filter { event -> event.organisers.contains(it.uid) } }
    }

    @Test
    fun testSavedScreenWithSavedEvents() {
        composeTestRule.setContent {
            SavedScreen(navigationAction, eventViewModel, userViewModel)
        }

        composeTestRule.waitForIdle()

        assertDisplayComponentInScroll(composeTestRule.onNodeWithTag(SavedTestTags.TITLE))
        assertDisplayComponentInScroll(composeTestRule.onNodeWithTag(SavedTestTags.FAB))
        assertDisplayComponentInScroll(composeTestRule.onNodeWithTag(SavedTestTags.TODAY))
        assertDisplayComponentInScroll(composeTestRule.onNodeWithTag(SavedTestTags.UPCOMING))
    }

    @Test
    fun testSavedScreenWithNoSavedEvents() {
        every {userViewModel.isEventSavedForCurrentUser(any())} returns false

        composeTestRule.setContent {
            SavedScreen(navigationAction, eventViewModel, userViewModel)
        }

        composeTestRule.waitForIdle()

        assertDisplayComponentInScroll(composeTestRule.onNodeWithTag(SavedTestTags.TITLE))
        assertDisplayComponentInScroll(composeTestRule.onNodeWithTag(SavedTestTags.FAB))
        assertDisplayComponentInScroll(composeTestRule.onNodeWithTag(SavedTestTags.NO_EVENTS))
    }
}