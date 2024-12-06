package com.android.unio.model.notification

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.util.Log
import androidx.core.app.NotificationCompat
import com.android.unio.R
import com.android.unio.model.strings.NotificationStrings.EVENT_BROADCAST_CHANNEL_ID
import com.android.unio.model.strings.NotificationStrings.EVENT_BROADCAST_NOTIFICATION_ID
import com.google.firebase.Firebase
import com.google.firebase.functions.functions
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage

enum class NotificationType(val requiredFields: List<String>) {
  EVENT_MESSAGE(listOf("title", "body")),
}

fun broadcastMessage(
    type: NotificationType,
    topic: String,
    payload: Map<String, Any>,
    onSuccess: () -> Unit,
    onFailure: () -> Unit
) {
  // check for required fields in data according to type
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
  override fun onMessageReceived(message: RemoteMessage) {
    println("Message received: ${message.data}")

    if (!message.data.containsKey("type")) {
      Log.e("CloudMessaging", "onMessageReceived: missing type field")
      return
    }

    val type = NotificationType.valueOf(message.data["type"]!!)

    var builder = NotificationCompat.Builder(this, EVENT_BROADCAST_CHANNEL_ID)
      .setSmallIcon(R.drawable.other_icon)
      .setPriority(NotificationCompat.PRIORITY_DEFAULT)

    when (type) {
      NotificationType.EVENT_MESSAGE -> {
        if (!message.data.containsKey("title") || !message.data.containsKey("body")) {
          Log.e("CloudMessaging", "onMessageReceived: missing title or body field")
          return
        }

        builder = builder
          .setContentTitle(message.data["title"])
          .setContentText(message.data["body"])
      }
    }

    val notificationManager: NotificationManager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager

    var notificationChannel = notificationManager.getNotificationChannel(EVENT_BROADCAST_CHANNEL_ID)
    if (notificationChannel == null) {
      notificationChannel = NotificationChannel(EVENT_BROADCAST_CHANNEL_ID, EVENT_BROADCAST_CHANNEL_ID, NotificationManager.IMPORTANCE_HIGH)
      notificationChannel.enableLights(true)
      notificationChannel.setShowBadge(true)
      notificationChannel.lockscreenVisibility = Notification.VISIBILITY_PUBLIC

      notificationManager.createNotificationChannel(notificationChannel)
    }

    notificationManager.notify(EVENT_BROADCAST_NOTIFICATION_ID, builder.build())
  }
}
