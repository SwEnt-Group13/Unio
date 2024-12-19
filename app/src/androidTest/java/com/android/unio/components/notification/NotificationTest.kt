package com.android.unio.components.notification

import android.app.NotificationManager
import android.content.Context
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.rule.GrantPermissionRule
import com.android.unio.R
import com.android.unio.TearDown
import com.android.unio.model.association.AssociationRepositoryFirestore
import com.android.unio.model.event.EventRepositoryFirestore
import com.android.unio.model.event.EventUserPictureRepositoryFirestore
import com.android.unio.model.event.EventViewModel
import com.android.unio.model.image.ImageRepositoryFirebaseStorage
import com.android.unio.model.notification.NotificationWorker
import com.android.unio.model.notification.UnioNotification
import com.android.unio.model.search.SearchRepository
import com.android.unio.model.search.SearchViewModel
import com.android.unio.model.usecase.SaveUseCaseFirestore
import com.android.unio.model.usecase.UserDeletionUseCaseFirestore
import com.android.unio.model.user.UserRepositoryFirestore
import com.android.unio.model.user.UserViewModel
import com.android.unio.ui.home.HomeScreen
import com.android.unio.ui.navigation.NavigationAction
import io.mockk.MockKAnnotations
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.just
import io.mockk.runs
import io.mockk.spyk
import junit.framework.TestCase.assertEquals
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class NotificationTest : TearDown() {
    @get:Rule val composeTestRule = createComposeRule()

    @get:Rule
    val permissionRule = GrantPermissionRule.grant(android.Manifest.permission.POST_NOTIFICATIONS)

    @MockK private lateinit var navigationAction: NavigationAction

    private lateinit var userViewModel: UserViewModel

    // Mock event repository to provide test data.
    @MockK private lateinit var eventRepository: EventRepositoryFirestore

    @MockK(relaxed = true) private lateinit var searchRepository: SearchRepository

    @MockK private lateinit var imageRepository: ImageRepositoryFirebaseStorage
    @MockK private lateinit var associationRepository: AssociationRepositoryFirestore
    @MockK private lateinit var userRepository: UserRepositoryFirestore
    @MockK private lateinit var userDeletionRepository: UserDeletionUseCaseFirestore
    @MockK
    private lateinit var eventUserPictureRepositoryFirestore: EventUserPictureRepositoryFirestore
    @MockK private lateinit var concurrentEventUserRepositoryFirestore: SaveUseCaseFirestore
    private lateinit var eventViewModel: EventViewModel
    private lateinit var searchViewModel: SearchViewModel
    private lateinit var context: Context
    private val timerNotif: Long = 5000 // short time so test doesn't take too long
    private val mockNotification =
        UnioNotification(
            "my notification title",
            "super duper event, come it will be nice :)",
            R.drawable.other_icon,
            "1234",
            "anonymous",
            0,
            System.currentTimeMillis() + timerNotif)

    @Before
    fun setUp() {
        MockKAnnotations.init(this)
        every { eventRepository.init(any()) } just runs
        every { userRepository.init(any()) } just runs
        context = InstrumentationRegistry.getInstrumentation().targetContext
        searchViewModel = spyk(SearchViewModel(searchRepository))
        eventViewModel =
            EventViewModel(
                eventRepository,
                imageRepository,
                associationRepository,
                eventUserPictureRepositoryFirestore,
                concurrentEventUserRepositoryFirestore)
        userViewModel = spyk(UserViewModel(userRepository, imageRepository, userDeletionRepository))
    }

    @Test
    fun notificationIsSentTest() {
        composeTestRule.setContent {
            HomeScreen(navigationAction, eventViewModel, userViewModel, searchViewModel)
        }
        NotificationWorker.schedule(context, mockNotification)

        Thread.sleep(timerNotif + 500)
        val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        with(manager.activeNotifications.first()) {
            assertEquals(mockNotification.notificationId, this.id)
            assertEquals(mockNotification.title, this.notification.extras.getString("android.title"))
            assertEquals(mockNotification.message, this.notification.extras.getString("android.text"))
            assertEquals(mockNotification.icon, this.notification.smallIcon.resId)
        }
        manager.cancelAll()
    }

    @Test
    fun notificationScheduledThenCanceled() {
        composeTestRule.setContent {
            HomeScreen(navigationAction, eventViewModel, userViewModel, searchViewModel)
        }
        NotificationWorker.schedule(context, mockNotification)
        val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        Thread.sleep(timerNotif / 2)
        NotificationWorker.unschedule(context, mockNotification.notificationId)
        Thread.sleep(timerNotif / 2 + 500)
        assert(manager.activeNotifications.isEmpty())
    }

    @After
    override fun tearDown() {
        super.tearDown()
        val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        manager.cancelAll()
    }
}