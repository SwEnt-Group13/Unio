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
import com.android.unio.model.strings.NotificationStrings.NOTIFICATION_SCHEDULER_TYPE_CANCEL
import com.android.unio.model.strings.NotificationStrings.NOTIFICATION_SCHEDULER_TYPE_CREATE

class NotificationScheduler() : BroadcastReceiver() {

  companion object {
    /**
     * Creates a PendingIntent for the notification.
     *
     * @param context The application context.
     * @param data The data containing notification details.
     * @return The created PendingIntent.
     */
    private fun createIntent(context: Context, data: Data): PendingIntent {

      val notificationId = data.getInt("notificationId", 0)
      val bundle = Bundle(data.keyValueMap.toPersistableBundle())
      val alarmIntent =
          Intent(context.applicationContext, NotificationScheduler::class.java).putExtras(bundle)
      // pass intent if extra data is needed
      val pendingIntent =
          PendingIntent.getBroadcast(
              context,
              notificationId,
              alarmIntent,
              PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)

      return pendingIntent
    }
    /**
     * Schedules a notification using AlarmManager.
     *
     * @param context The application context.
     * @param data The data containing notification details including timeMillis.
     */
    fun schedule(context: Context, data: Data) {
      val manager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
      // pass intent if extra data is needed
      val pendingIntent = createIntent(context, data)
      val timeMillis = data.getLong("timeMillis", System.currentTimeMillis() + 10000)
      println(timeMillis)
      manager.set(AlarmManager.RTC_WAKEUP, timeMillis, pendingIntent)
      Log.d("NotificationScheduler", "Notification scheduled")
    }
    /**
     * Unschedules a previously scheduled notification using AlarmManager.
     *
     * @param context The application context.
     * @param data The data containing notification details.
     */
    fun unschedule(context: Context, data: Data) {
      val manager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

      val pendingIntent = createIntent(context, data)
      manager.cancel(pendingIntent)
      Log.d("NotificationScheduler", "Notification canceled")
    }
  }
  /**
   * Receives the broadcast and triggers the notification or handles BOOT_COMPLETED action.
   *
   * @param context The application context.
   * @param intent The received broadcast intent.
   */
  override fun onReceive(context: Context?, intent: Intent?) {
    if (intent == null) {
      Log.w("NotificationScheduler", "Received a null Intent")
      return
    }
    if (context == null) {
      Log.w("NotificationScheduler", "Received a null Context")
      return
    }
    val data = intent.extras ?: return
    val title = data.getString("title") ?: "Unio"
    val message = data.getString("message") ?: "An event will occur soon"
    val icon = data.getInt("icon")
    val channelId = data.getString("channelId") ?: "0"
    val channelName = data.getString("channelName") ?: "0"
    val notificationId = data.getInt("notificationId")
    val timeMillis = data.getLong("timeMillis")

    if ("android.intent.action.BOOT_COMPLETED" ==
        intent.action) { // allows notification to persist after a phone reboot
      context.let {
        NotificationWorker.schedule(
            it,
            UnioNotification(
                title, message, icon, channelId, channelName, notificationId, timeMillis))
      }
    } else {
      // Trigger notification using NotificationManager
      val builder = NotificationCompat.Builder(context, channelId)

      var notificationManager: NotificationManager =
          context.getSystemService(NOTIFICATION_SERVICE) as NotificationManager

      var nc = notificationManager.getNotificationChannel(channelId)
      if (nc == null) {
        nc = NotificationChannel(channelId, channelName, NotificationManager.IMPORTANCE_HIGH)
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
              notificationId,
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
      notificationManager.notify(notificationId, builder.build())
      NotificationWorker.notificationMap.remove(notificationId)
    }
  }
}
/**
 * NotificationWorker handles background work for scheduling and unscheduling notifications using
 * WorkManager.
 *
 * @param context The application context.
 * @param params The Worker parameters.
 */
class NotificationWorker(context: Context, params: WorkerParameters) : Worker(context, params) {

  companion object {
    val notificationMap: MutableMap<Int, UnioNotification> = mutableMapOf()
    /**
     * Schedules a notification using WorkManager.
     *
     * @param context The application context.
     * @param notification The notification details.
     */
    fun schedule(context: Context, notification: UnioNotification) {

      val workManager = WorkManager.getInstance(context)
      val data =
          Data.Builder()
              .putAll(
                  mapOf(
                      "title" to notification.title,
                      "message" to notification.message,
                      "icon" to notification.icon,
                      "channelId" to notification.channelId,
                      "notificationId" to notification.notificationId,
                      "timeMillis" to notification.timeMillis,
                      "type" to NOTIFICATION_SCHEDULER_TYPE_CREATE))
      val request =
          OneTimeWorkRequest.Builder(NotificationWorker::class.java)
              .setInputData(data.build())
              .build()
      workManager.enqueue(request)
      notificationMap[notification.notificationId] = notification
    }

    /**
     * Unschedules a previously scheduled notification using WorkManager.
     *
     * @param context The application context.
     * @param notificationId The ID of the notification to be unscheduled.
     */
    fun unschedule(context: Context, notificationId: Int) {
      val notification = notificationMap[notificationId]
      if (notification == null) {
        Log.w("NotificationWorker", "There is no notification with this id")
        return
      }
      val workManager = WorkManager.getInstance(context)
      val data =
          Data.Builder()
              .putAll(
                  mapOf(
                      "title" to notification.title,
                      "message" to notification.message,
                      "icon" to notification.icon,
                      "channelId" to notification.channelId,
                      "notificationId" to notification.notificationId,
                      "timeMillis" to notification.timeMillis,
                      "type" to NOTIFICATION_SCHEDULER_TYPE_CANCEL))
      val request =
          OneTimeWorkRequest.Builder(NotificationWorker::class.java)
              .setInputData(data.build())
              .build()
      workManager.enqueue(request)

      notificationMap.remove(notification.notificationId)
    }
  }

  /**
   * Performs the background work to either schedule or unschedule the notification based on the
   * input data.
   *
   * @return The Result of the work execution.
   */
  override fun doWork(): Result {
    if (inputData.getString("type") == NOTIFICATION_SCHEDULER_TYPE_CANCEL) {
      NotificationScheduler.unschedule(this.applicationContext, inputData)
    } else if (inputData.getString("type") == NOTIFICATION_SCHEDULER_TYPE_CREATE) {
      NotificationScheduler.schedule(this.applicationContext, inputData)
    } else {
      return Result.failure()
    }

    return Result.success()
  }
}
