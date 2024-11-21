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
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.work.OneTimeWorkRequest
import androidx.work.WorkManager
import androidx.work.Worker
import androidx.work.WorkerParameters
import com.android.unio.R


class NotificationReceiver() : BroadcastReceiver() {

    companion object {
        fun schedule(context: Context, eventTime: Long, code: Int) {
            //eventTime - should be in millisecond
            //code - unique code will add new job and duplicate code will replace existing job
            val manager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
            val alarmIntent = Intent(context.applicationContext, NotificationReceiver::class.java)
            //pass intent if extra data is needed
            val pendingIntent = PendingIntent.getBroadcast(
                context,
                code,
                alarmIntent,
                PendingIntent.FLAG_IMMUTABLE
            )
            manager.set(
                AlarmManager.RTC_WAKEUP,
                System.currentTimeMillis() + eventTime, pendingIntent
            )
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
        if ("android.intent.action.BOOT_COMPLETED" == intent.action) {
            context.let { MyWorker.schedule(it) }
        } else {
            //Trigger notification using NotificationManager
            val builder = NotificationCompat.Builder(context, "1234")


            val notificationManager: NotificationManager =
                context.getSystemService(NOTIFICATION_SERVICE) as NotificationManager

            var nc = notificationManager.getNotificationChannel("1234")
            if (nc == null) {
                nc = NotificationChannel("1234", "EventReminder", NotificationManager.IMPORTANCE_DEFAULT)
                nc.description = "Badge Notifications"
                nc.enableLights(true)
                nc.setShowBadge(true)
                nc.lockscreenVisibility = Notification.VISIBILITY_PUBLIC

                notificationManager.createNotificationChannel(nc)
            }
            builder.setSmallIcon(R.drawable.other_icon)
                .setContentTitle(context.getString(R.string.event_no_events_available))
                .setContentText(context.getString(R.string.home_tab_all))
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setChannelId("1234")

            notificationManager.notify(0, builder.build())


        }
    }
}

class MyWorker(context: Context, params: WorkerParameters) : Worker(context, params) {

    companion object {
        fun schedule(context: Context) {
            val workManager = WorkManager.getInstance(context)
            val request = OneTimeWorkRequest.Builder(MyWorker::class.java).build()
            workManager.enqueue(request)
        }
    }


    override fun doWork(): Result {
        // Perform your background task here like fetching notification related data (title, description, reminder time)

        NotificationReceiver.schedule(this.applicationContext, 7000, 1234)
        return Result.success()
    }
}

/*object NotificationSender {
    private const val CHANNEL_ID = "YourChannelId"
    private const val CHANNEL_NAME = "YourChannelName"
    private const val CHANNEL_DESCRIPTION = "YourChannelDescription"

    fun createNotificationChannel(context: Context) {
        val channel = NotificationChannel(
            CHANNEL_ID,
            CHANNEL_NAME,
            NotificationManager.IMPORTANCE_DEFAULT
        ).apply {
            description = CHANNEL_DESCRIPTION
        }

        val notificationManager =
            context.getSystemService(NotificationManager::class.java)
        notificationManager?.createNotificationChannel(channel)
        notificationManager?.getNotificationChannel("onch")
    }

    fun showNotification(context: Context, title: Int, message: Int) {
        val builder = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.other_icon)
            .setContentTitle(context.getString(title))
            .setContentText(context.getString(message))
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)

        val notificationManager = NotificationManagerCompat.from(context)
        //notificationManager.notify(notificationId, builder.build())
    }
}*/

