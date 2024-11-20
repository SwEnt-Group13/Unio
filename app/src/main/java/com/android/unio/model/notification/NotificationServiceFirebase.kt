package com.android.unio.model.notification

import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage

class NotificationServiceFirebase : FirebaseMessagingService() {


    override fun onNewToken(token: String) {

    }


    override fun onMessageReceived(remoteMessage: RemoteMessage) {

    }
}