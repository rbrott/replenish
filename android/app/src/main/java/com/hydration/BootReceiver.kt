package com.hydration

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

const val NOTIFICATION_INTERVAL_MS = 5 * 60 * 1000L

class BootReceiver : BroadcastReceiver() {

    private var alarmMgr: AlarmManager? = null
    private lateinit var alarmIntent: PendingIntent

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == "android.intent.action.BOOT_COMPLETED") {
            alarmMgr = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
            alarmIntent = Intent(context, NotificationService::class.java).let {
                PendingIntent.getBroadcast(context, 0, it, 0)
            }

            alarmMgr?.setInexactRepeating(
                AlarmManager.ELAPSED_REALTIME_WAKEUP,
                System.currentTimeMillis(),
                NOTIFICATION_INTERVAL_MS,
                alarmIntent
            )
        }
    }
}