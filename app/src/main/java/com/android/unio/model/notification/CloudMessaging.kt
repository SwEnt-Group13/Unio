package com.android.unio.model.notification

import android.util.Log
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
  }
}
