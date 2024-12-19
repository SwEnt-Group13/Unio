package com.android.unio.components.notification

import androidx.compose.ui.test.assertHasClickAction
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import com.android.unio.TearDown
import com.android.unio.model.notification.NotificationType
import com.android.unio.model.notification.broadcastMessage
import com.android.unio.model.strings.test_tags.NotificationSenderTestTags
import com.android.unio.ui.components.NotificationSender
import io.mockk.MockKAnnotations
import io.mockk.every
import io.mockk.mockkStatic
import io.mockk.verify
import kotlin.reflect.jvm.javaMethod
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class NotificationSenderTest : TearDown() {
    @get:Rule val composeTestRule = createComposeRule()

    @Before
    fun setUp() {
        MockKAnnotations.init(this)

        mockkStatic(::broadcastMessage.javaMethod!!.declaringClass.kotlin)
    }

    @Test
    fun testEverythingIsDisplayed() {
        composeTestRule.setContent {
            NotificationSender(
                dialogTitle = "Test",
                notificationType = NotificationType.EVENT_SAVERS,
                topic = "Test",
                notificationContent = { mapOf("title" to it) },
                showNotificationDialog = true,
                onClose = {})
        }

        composeTestRule.onNodeWithTag(NotificationSenderTestTags.CARD).assertExists()
        composeTestRule.onNodeWithTag(NotificationSenderTestTags.MESSAGE_FIELD).assertExists()
        composeTestRule.onNodeWithTag(NotificationSenderTestTags.TITLE).assertExists()
        composeTestRule.onNodeWithTag(NotificationSenderTestTags.SEND_BUTTON).assertExists()
        composeTestRule.onNodeWithTag(NotificationSenderTestTags.SEND_BUTTON).assertHasClickAction()
    }

    @Test
    fun testSendSuccess() {
        val topic = "Topic"
        val message = "Message"
        val payload = mapOf("title" to message)

        every { broadcastMessage(any(), any(), any(), any(), any()) } answers
                {
                    (args[3] as () -> Unit)()
                    (args[4] as () -> Unit)()
                }

        composeTestRule.setContent {
            NotificationSender(
                dialogTitle = "Test",
                notificationType = NotificationType.EVENT_SAVERS,
                topic = topic,
                notificationContent = { mapOf("title" to it) },
                showNotificationDialog = true,
                onClose = {})
        }

        composeTestRule
            .onNodeWithTag(NotificationSenderTestTags.MESSAGE_FIELD)
            .performTextInput(message)
        composeTestRule.onNodeWithTag(NotificationSenderTestTags.SEND_BUTTON).performClick()

        // Verify that the broadcastMessage function was called
        verify { broadcastMessage(NotificationType.EVENT_SAVERS, topic, payload, any(), any()) }
    }

    @Test
    fun testSendEmptyMessage() {
        val topic = "Topic"
        val message = ""

        composeTestRule.setContent {
            NotificationSender(
                dialogTitle = "Test",
                notificationType = NotificationType.EVENT_SAVERS,
                topic = topic,
                notificationContent = { mapOf("title" to it) },
                showNotificationDialog = true,
                onClose = {})
        }

        composeTestRule
            .onNodeWithTag(NotificationSenderTestTags.MESSAGE_FIELD)
            .performTextInput(message)
        composeTestRule.onNodeWithTag(NotificationSenderTestTags.SEND_BUTTON).performClick()

        // Verify that the broadcastMessage function was not called
        verify(exactly = 0) { broadcastMessage(any(), any(), any(), any(), any()) }
    }
}