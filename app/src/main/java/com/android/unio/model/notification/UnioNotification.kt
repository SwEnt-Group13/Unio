package com.android.unio.model.notification

import com.android.unio.R

data class UnioNotification(
    val title: String = "",
    val message: String = "",
    val icon: Int = R.drawable.other_icon,
    var channelId: String = "1234",
    val channelName: String = "",
    val notificationId: Int = 0,
    val timeMillis: Long
)
