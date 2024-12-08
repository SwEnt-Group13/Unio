package com.android.unio.model.notification

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.util.Log
import androidx.core.app.NotificationCompat
import com.android.unio.R
import com.google.firebase.Firebase
import com.google.firebase.functions.functions
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage

/**
 * Enum class representing the different types of notifications that can be broadcasted.
 *
 * @param requiredFields The fields that are required for the notification type.
 */
enum class NotificationType(
    val requiredFields: List<String>,
    val displayName: String,
    val description: String
) {
  EVENT_SAVERS(
      listOf("title", "body"),
      "Saved Events",
      "Receive notifications from associations organizing events you saved."),
  ASSOCIATION_FOLLOWERS(
      listOf("title", "body"),
      "Followed Associations",
      "Receive notifications from associations you follow.")
}

/**
 * Broadcasts a message to all users subscribed to a topic. This is a client-side function that
 * calls a Firebase Cloud Function, as message broadcasting is a server-side operation.
 *
 * @param type The type of notification to broadcast.
 * @param topic The topic to broadcast the message to.
 * @param payload The data to send with the message.
 * @param onSuccess The function to call if the message is successfully sent.
 */
fun broadcastMessage(
    type: NotificationType,
    topic: String,
    payload: Map<String, String>,
    onSuccess: () -> Unit,
    onFailure: () -> Unit
) {
  // Check if the payload contains all required fields
  type.requiredFields.forEach {
    if (!payload.containsKey(it)) {
      Log.e("CloudMessaging", "broadcastMessage: missing required field $it")
      onFailure()
      return
    }
  }

  Firebase.functions
      .getHttpsCallable("broadcastMessage")
      .call(hashMapOf("type" to type.name, "topic" to topic, "payload" to payload))
      .addOnSuccessListener { onSuccess() }
      .addOnFailureListener {
        Log.e("CloudMessaging", "sendEventNotification: failure", it)
        onFailure()
      }
}

class UnioMessagingService : FirebaseMessagingService() {
  override fun onNewToken(token: String) {
    // Method is left empty because we do not target single devices
  }

  /**
   * Called when a message is received from Firebase Cloud Messaging. This method is responsible for
   * building and displaying the notification.
   *
   * @param message The message received from Firebase Cloud Messaging.
   */
  override fun onMessageReceived(message: RemoteMessage) {
    Log.i("CloudMessaging", "onMessageReceived: message ${message.data}")

    // Get the type and topic of the notification
    val typeString = message.data["type"]
    if (typeString == null) {
      Log.e("CloudMessaging", "onMessageReceived: missing type field")
      return
    }
    val notificationType = NotificationType.valueOf(typeString)

    // Check if the notification has the required fields
    notificationType.requiredFields.forEach {
      if (!message.data.containsKey(it)) {
        Log.e(
            "CloudMessaging",
            "onMessageReceived: message ${message.data} missing required field $it")
        return
      }
    }

    // Build notification
    var builder =
        NotificationCompat.Builder(this, notificationType.name)
            .setSmallIcon(R.drawable.other_icon)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)

    // Set notification content based on type
    when (notificationType) {
      NotificationType.EVENT_SAVERS,
      NotificationType.ASSOCIATION_FOLLOWERS -> {
        builder =
            builder.setContentTitle(message.data["title"]).setContentText(message.data["body"])
      }
    }

    val notification = builder.build()

    // Create notification channel if it does not exist
    val notificationManager: NotificationManager =
        getSystemService(NOTIFICATION_SERVICE) as NotificationManager

    var notificationChannel = notificationManager.getNotificationChannel(notificationType.name)
    if (notificationChannel == null) {
      notificationChannel =
          NotificationChannel(
              notificationType.name,
              notificationType.displayName,
              NotificationManager.IMPORTANCE_HIGH)
      notificationChannel.enableLights(true)
      notificationChannel.setShowBadge(true)
      notificationChannel.lockscreenVisibility = Notification.VISIBILITY_PUBLIC
      notificationChannel.description = notificationType.description

      notificationManager.createNotificationChannel(notificationChannel)
    }

    // Send notification
    notificationManager.notify(System.currentTimeMillis().toInt(), notification)
  }
}
