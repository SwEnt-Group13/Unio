package com.android.unio.ui.notifications

import android.app.NotificationManager
import android.content.Context
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.rule.GrantPermissionRule
import com.android.unio.R
import com.android.unio.TearDown
import com.android.unio.model.event.EventRepositoryFirestore
import com.android.unio.model.event.EventViewModel
import com.android.unio.model.image.ImageRepositoryFirebaseStorage
import com.android.unio.model.notification.NotificationWorker
import com.android.unio.model.search.SearchRepository
import com.android.unio.model.search.SearchViewModel
import com.android.unio.model.user.UserRepositoryFirestore
import com.android.unio.model.user.UserViewModel
import com.android.unio.ui.home.HomeScreen
import com.android.unio.ui.navigation.NavigationAction
import com.google.firebase.Timestamp
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
  @MockK private lateinit var userRepository: UserRepositoryFirestore
  private lateinit var eventViewModel: EventViewModel
  private lateinit var searchViewModel: SearchViewModel
  private lateinit var context: Context
  private val mockNotificationValues =
      mapOf(
          "id" to 0,
          "title" to "my notification title",
          "description" to "super duper event, come it will be nice :)",
          "channelId" to "1234",
          "timeMillis" to 5.toLong(), // short time so test can run fast
          "icon" to R.drawable.other_icon)

  private val timerNotif: Long = 5000

  @Before
  fun setUp() {
    MockKAnnotations.init(this)
    every { eventRepository.init(any()) } just runs
    context = InstrumentationRegistry.getInstrumentation().targetContext
    searchViewModel = spyk(SearchViewModel(searchRepository))
    eventViewModel = EventViewModel(eventRepository, imageRepository)
    userViewModel = spyk(UserViewModel(userRepository))

    composeTestRule.setContent {
      HomeScreen(navigationAction, eventViewModel, userViewModel, searchViewModel)
    }
  }

  @Test
  fun notificationIsSentTest() {
    val startTimer = Timestamp.now()
    NotificationWorker.schedule(
        context,
        mockNotificationValues["title"]!! as String,
        mockNotificationValues["description"]!! as String,
        mockNotificationValues["icon"]!! as Int,
        mockNotificationValues["channelId"]!! as String,
        mockNotificationValues["id"]!! as Int,
        mockNotificationValues["timeMillis"]!! as Long)
    Thread.sleep(timerNotif + 500)
    val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
    with(manager.activeNotifications.first()) {
      assertEquals(mockNotificationValues["id"]!!, this.id)
      assertEquals(
          mockNotificationValues["title"]!!, this.notification.extras.getString("android.title"))
      assertEquals(
          mockNotificationValues["description"]!!,
          this.notification.extras.getString("android.text"))

      assertEquals(mockNotificationValues["icon"]!!, this.notification.smallIcon.resId)
    }
  }

  @After
  override fun tearDown() {
    super.tearDown()
    val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
    manager.cancelAll()
  }
}
