package com.android.unio.ui.notifications

import android.app.NotificationManager
import android.content.Context
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.test.platform.app.InstrumentationRegistry
import com.android.unio.model.event.EventRepositoryFirestore
import com.android.unio.model.event.EventViewModel
import com.android.unio.model.image.ImageRepositoryFirebaseStorage
import com.android.unio.model.notification.NotificationWorker
import com.android.unio.model.search.SearchRepository
import com.android.unio.model.search.SearchViewModel
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

class NotificationTest {
  @get:Rule val composeTestRule = createComposeRule()

  @MockK private lateinit var navigationAction: NavigationAction

  private lateinit var userViewModel: UserViewModel

  // Mock event repository to provide test data.
  @MockK private lateinit var eventRepository: EventRepositoryFirestore
  @MockK(relaxed = true) private lateinit var searchRepository: SearchRepository
  @MockK private lateinit var imageRepository: ImageRepositoryFirebaseStorage
  private lateinit var eventViewModel: EventViewModel
  private lateinit var searchViewModel: SearchViewModel
  private lateinit var context: Context
  private val mockNotificationValues =
      mapOf(
          0 to "id",
          "my notification title" to "title",
          "super duper event, come it will be nice :)" to "description",
          "1234" to "channelId",
          5.0 to "timeMillis", // short time so test can run fast
          0 to "icon")

  @Before
  fun setUp() {
    MockKAnnotations.init(this)
    context = InstrumentationRegistry.getInstrumentation().targetContext
    searchViewModel = spyk(SearchViewModel(searchRepository))
    eventViewModel = EventViewModel(eventRepository, imageRepository)

    every { eventRepository.init(any()) } just runs
    composeTestRule.setContent {
      HomeScreen(navigationAction, eventViewModel, userViewModel, searchViewModel)
    }
  }

  @Test
  fun notificationTest() {
    NotificationWorker.schedule(
        context,
        mockNotificationValues["title"]!!,
        mockNotificationValues["description"]!!,
        mockNotificationValues["icon"]!!.toInt(),
        mockNotificationValues["channelId"]!!,
        mockNotificationValues["id"]!!.toInt(),
        mockNotificationValues["timeMillis"]!!.toLong())
    Thread.sleep(5500)
    val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
    with(manager.activeNotifications.first()) {
      assertEquals(mockNotificationValues["id"]!!.toInt(), this.id)
      assertEquals(
          mockNotificationValues["title"]!!.toInt(),
          this.notification.extras.getString("android.title"))
      assertEquals(
          mockNotificationValues["id"]!!.toInt(),
          this.notification.extras.getString("android.text"))
    }
  }

  @After
  fun tearDown() {
    val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
    manager.cancelAll()
  }
}
