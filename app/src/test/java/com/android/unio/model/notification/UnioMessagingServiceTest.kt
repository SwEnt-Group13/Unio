package com.android.unio.model.notification

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import androidx.test.platform.app.InstrumentationRegistry
import com.google.firebase.messaging.RemoteMessage
import io.mockk.MockKAnnotations
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.just
import io.mockk.mockk
import io.mockk.runs
import io.mockk.verify
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class UnioMessagingServiceTest {

    @MockK private lateinit var notificationManager: NotificationManager
    @MockK private lateinit var messagingService: UnioMessagingService
    @MockK private lateinit var notificationChannel: NotificationChannel

    @Before
    fun setup() {
        MockKAnnotations.init(this)

        every { messagingService.getSystemService(Context.NOTIFICATION_SERVICE) } returns
                notificationManager
        every { messagingService.resources } returns
                InstrumentationRegistry.getInstrumentation().context.resources
        every { messagingService.applicationInfo } returns
                InstrumentationRegistry.getInstrumentation().context.applicationInfo
        every { messagingService.packageName } returns
                InstrumentationRegistry.getInstrumentation().context.packageName

        every { notificationManager.getNotificationChannel(any()) } returns notificationChannel
        every { notificationManager.notify(any<Int>(), any()) } just runs

        // Make the messaging service run the real onMessageReceived method when it is called
        every { messagingService.onMessageReceived(any()) } answers { callOriginal() }
    }

    @Test
    fun `onMessageReceived handles notification with all required fields`() {
        // Mock the RemoteMessage
        val data =
            mapOf(
                "type" to NotificationType.EVENT_SAVERS.name,
                "title" to "Test Title",
                "body" to "Test Body")
        val remoteMessage = mockk<RemoteMessage>()
        every { remoteMessage.data } returns data

        // Call the method under test
        messagingService.onMessageReceived(remoteMessage)

        // Verify the notification was sent
        verify { notificationManager.notify(any<Int>(), any()) }
    }

    @Test
    fun `onMessageReceived logs error when type is missing`() {
        // Mock the RemoteMessage
        val data = mapOf("title" to "Test Title", "body" to "Test Body")
        val remoteMessage = mockk<RemoteMessage>()
        every { remoteMessage.data } returns data

        // Call the method under test
        messagingService.onMessageReceived(remoteMessage)

        // Verify that the notification was not sent
        verify(exactly = 0) { notificationManager.notify(any(), any()) }
    }
}