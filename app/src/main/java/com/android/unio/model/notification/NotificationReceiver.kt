package com.android.unio.model.notification

import android.app.AlarmManager
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Context.NOTIFICATION_SERVICE
import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.os.toPersistableBundle
import androidx.work.Data
import androidx.work.OneTimeWorkRequest
import androidx.work.WorkManager
import androidx.work.Worker
import androidx.work.WorkerParameters
import com.android.unio.MainActivity

class NotificationReceiver() : BroadcastReceiver() {

  companion object {
    fun schedule(context: Context, data: Data) {
      // code - unique code will add new job and duplicate code will replace existing job
      val manager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
      val bundle = Bundle(data.keyValueMap.toPersistableBundle())
      val alarmIntent =
          Intent(context.applicationContext, NotificationReceiver::class.java).putExtras(bundle)
      // pass intent if extra data is needed
      val pendingIntent =
          PendingIntent.getBroadcast(
              context,
              1234,
              alarmIntent,
              PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
      val timeMillis = data.getLong("timeMillis", 1000)
      manager.set(AlarmManager.RTC_WAKEUP, timeMillis, pendingIntent)
    }
  }

  override fun onReceive(context: Context?, intent: Intent?) {
    if (intent == null) {
      Log.w("NotificationReceiver", "Received a null Intent")
      return
    }
    if (context == null) {
      Log.w("NotificationReceiver", "Received a null Context")
      return
    }
    val data = intent.extras ?: return
    val title = data.getString("title") ?: "Unio"
    val message = data.getString("message") ?: "An event will occur soon"
    val icon = data.getInt("icon")
    val channelId = data.getString("channelId") ?: "0"
    val notificationId = data.getInt("notificationId")
    val timeMillis = data.getLong("timeMillis")

    if ("android.intent.action.BOOT_COMPLETED" == intent.action) {
      context.let {
        NotificationWorker.schedule(it, title, message, icon, channelId, notificationId, timeMillis)
      }
    } else {
      // Trigger notification using NotificationManager
      val builder = NotificationCompat.Builder(context, channelId)

      var notificationManager: NotificationManager =
          context.getSystemService(NOTIFICATION_SERVICE) as NotificationManager

      var nc = notificationManager.getNotificationChannel(channelId)
      if (nc == null) {
        nc = NotificationChannel(channelId, "EventReminder", NotificationManager.IMPORTANCE_HIGH)
        nc.enableLights(true)
        nc.setShowBadge(true)
        nc.lockscreenVisibility = Notification.VISIBILITY_PUBLIC

        notificationManager.createNotificationChannel(nc)
      }
      // For now, clicking on the notification makes the app start at MainActivity
      val activityIntent = Intent(context.applicationContext, MainActivity::class.java)
      val pendingIntent =
          PendingIntent.getActivity(
              context.applicationContext,
              0,
              activityIntent,
              PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
      builder
          .setSmallIcon(icon)
          .setContentTitle(title)
          .setContentText(message)
          .setPriority(NotificationCompat.PRIORITY_MAX)
          .setChannelId(channelId)
          .setContentIntent(pendingIntent)

      notificationManager = context.getSystemService(NOTIFICATION_SERVICE) as NotificationManager
      notificationManager.notify(0, builder.build())
    }
  }
}

class NotificationWorker(context: Context, params: WorkerParameters) : Worker(context, params) {

  companion object {
    fun schedule(
        context: Context,
        title: String,
        message: String,
        icon: Int,
        channelId: String,
        notificationId: Int,
        timeMillis: Long
    ) {
      val workManager = WorkManager.getInstance(context)
      val data =
          Data.Builder()
              .putAll(
                  mapOf(
                      "title" to title,
                      "message" to message,
                      "icon" to icon,
                      "channelId" to channelId,
                      "notificationId" to notificationId,
                      "timeMillis" to timeMillis))
      val request =
          OneTimeWorkRequest.Builder(NotificationWorker::class.java)
              .setInputData(data.build())
              .build()
      workManager.enqueue(request)
    }
  }

  override fun doWork(): Result {
    // Perform your background task here like fetching notification related data (title,
    // description, reminder time)

    NotificationReceiver.schedule(this.applicationContext, inputData)
    return Result.success()
  }
}
