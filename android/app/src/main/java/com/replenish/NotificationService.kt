package com.replenish

import android.app.*
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.IBinder
import android.os.SystemClock
import android.support.v4.app.NotificationCompat
import android.support.v4.app.NotificationManagerCompat

const val CHANNEL_ID = "HydrationReminders"
const val NOTIFICATION_INTERVAL_MS = 15000L // 5 * 60 * 1000L

class NotificationService : Service() {
    companion object {
        fun setAlarm(context: Context) {
            val alarmMgr = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
            val alarmIntent = Intent(context, NotificationService::class.java).let {
                PendingIntent.getService(context, 0, it, 0)
            }

            alarmMgr.setInexactRepeating(
                AlarmManager.ELAPSED_REALTIME_WAKEUP,
                SystemClock.elapsedRealtime(),
                NOTIFICATION_INTERVAL_MS,
                alarmIntent
            )
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        // send a notification if necessary
        val apiClient = StdLibClient.createClient()
        // TODO
        if (apiClient.getDehydrationLevel() < 10.0) {
            val notifIntent = Intent(this, MainActivity::class.java)
            val pendingIntent: PendingIntent = PendingIntent.getActivity(this, 0, notifIntent, 0)

            createNotificationChannel()

            val notification = NotificationCompat.Builder(this, CHANNEL_ID)
                .setSmallIcon(R.drawable.water)
                .setContentTitle("Drink Up!")
                .setContentText("Based on your recent activity, we recommend you drink a glass of water to rehydrate.")
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setContentIntent(pendingIntent)
                .build()

            with(NotificationManagerCompat.from(this)) {
                notify(0, notification)
            }
        }

        return super.onStartCommand(intent, flags, startId)
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    private fun createNotificationChannel() {
        // Create the NotificationChannel, but only on API 26+ because
        // the NotificationChannel class is new and not in the support library
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = "Hydration reminders"
            val descriptionText = "Hydration reminders based on recent activity."
            val importance = NotificationManager.IMPORTANCE_DEFAULT
            val channel = NotificationChannel(CHANNEL_ID, name, importance).apply {
                description = descriptionText
            }
            // Register the channel with the system
            val notificationManager: NotificationManager =
                getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }
}