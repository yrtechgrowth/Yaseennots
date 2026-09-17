package com.example.notifications

import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.example.MainActivity
import com.example.data.AppDatabase
import com.example.data.NotificationRecord
import com.example.data.SettingsManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class AlarmReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val settings = SettingsManager(context).loadSettings()
        if (!settings.notificationsEnabled) return

        val itemId = intent.getStringExtra(YaseenAlarmScheduler.EXTRA_ITEM_ID) ?: return
        val itemType = intent.getStringExtra(YaseenAlarmScheduler.EXTRA_ITEM_TYPE) ?: "TASK"
        val title = intent.getStringExtra(YaseenAlarmScheduler.EXTRA_TITLE) ?: "Reminder"
        val message = intent.getStringExtra(YaseenAlarmScheduler.EXTRA_MESSAGE) ?: ""

        // Check specific channel toggle in settings
        when (itemType) {
            "TASK" -> if (!settings.taskRemindersEnabled) return
            "SCHEDULE" -> if (!settings.scheduleRemindersEnabled) return
            "NOTE" -> if (!settings.noteRemindersEnabled) return
            "DRAFT" -> if (!settings.draftRemindersEnabled) return
        }

        NotificationHelper.createNotificationChannels(context)

        val channelId = when (itemType) {
            "SCHEDULE" -> NotificationHelper.CHANNEL_SCHEDULE
            "NOTE" -> NotificationHelper.CHANNEL_NOTES
            "DRAFT" -> NotificationHelper.CHANNEL_DRAFTS
            else -> NotificationHelper.CHANNEL_TASKS
        }

        val notificationId = YaseenAlarmScheduler.generateRequestCode(itemId, itemType)

        // Main click intent: open MainActivity deep link
        val openIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            putExtra("initialNavTarget", when (itemType) {
                "SCHEDULE" -> "schedule"
                "NOTE", "DRAFT" -> "notes"
                else -> "tasks"
            })
            putExtra("targetItemId", itemId)
        }
        val contentPendingIntent = PendingIntent.getActivity(
            context,
            notificationId,
            openIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val builder = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(android.R.drawable.ic_lock_idle_alarm)
            .setContentTitle(title)
            .setContentText(message)
            .setStyle(NotificationCompat.BigTextStyle().bigText(message))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .setContentIntent(contentPendingIntent)

        if (settings.notificationsVibrate) {
            builder.setVibrate(longArrayOf(0, 300, 200, 300))
        }

        // Action Buttons
        if (itemType == "TASK") {
            // "Mark Complete" action
            val completeIntent = Intent(context, NotificationActionReceiver::class.java).apply {
                action = NotificationActionReceiver.ACTION_MARK_COMPLETE
                putExtra(NotificationActionReceiver.EXTRA_TASK_ID, itemId)
                putExtra(NotificationActionReceiver.EXTRA_NOTIFICATION_ID, notificationId)
            }
            val completePendingIntent = PendingIntent.getBroadcast(
                context,
                notificationId + 1,
                completeIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            builder.addAction(android.R.drawable.checkbox_on_background, "Mark Complete", completePendingIntent)
        }

        // "Snooze 10m" action
        val snoozeIntent = Intent(context, NotificationActionReceiver::class.java).apply {
            action = NotificationActionReceiver.ACTION_SNOOZE
            putExtra(YaseenAlarmScheduler.EXTRA_ITEM_ID, itemId)
            putExtra(YaseenAlarmScheduler.EXTRA_ITEM_TYPE, itemType)
            putExtra(YaseenAlarmScheduler.EXTRA_TITLE, title)
            putExtra(YaseenAlarmScheduler.EXTRA_MESSAGE, message)
            putExtra(NotificationActionReceiver.EXTRA_NOTIFICATION_ID, notificationId)
            putExtra(NotificationActionReceiver.EXTRA_SNOOZE_MINUTES, settings.defaultSnoozeMinutes)
        }
        val snoozePendingIntent = PendingIntent.getBroadcast(
            context,
            notificationId + 2,
            snoozeIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        builder.addAction(android.R.drawable.ic_menu_recent_history, "Snooze ${settings.defaultSnoozeMinutes}m", snoozePendingIntent)

        try {
            NotificationManagerCompat.from(context).notify(notificationId, builder.build())
        } catch (_: SecurityException) {
            // Catch if POST_NOTIFICATIONS is not granted
        }

        // Record in database for the in-app Notification Center
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val db = AppDatabase.getDatabase(context)
                db.appDao().insertNotificationRecord(
                    NotificationRecord(
                        itemId = itemId,
                        itemType = itemType,
                        title = title,
                        message = message,
                        triggerTime = System.currentTimeMillis(),
                        status = "UPCOMING"
                    )
                )
            } catch (_: Exception) {}
        }
    }
}
