package com.example.notifications

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.widget.Toast
import androidx.core.app.NotificationManagerCompat
import com.example.data.AppDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class NotificationActionReceiver : BroadcastReceiver() {

    companion object {
        const val ACTION_MARK_COMPLETE = "com.example.yaseen.ACTION_MARK_COMPLETE"
        const val ACTION_SNOOZE = "com.example.yaseen.ACTION_SNOOZE"

        const val EXTRA_TASK_ID = "extra_task_id"
        const val EXTRA_NOTIFICATION_ID = "extra_notification_id"
        const val EXTRA_SNOOZE_MINUTES = "extra_snooze_minutes"
    }

    override fun onReceive(context: Context, intent: Intent) {
        val notificationId = intent.getIntExtra(EXTRA_NOTIFICATION_ID, -1)
        if (notificationId != -1) {
            NotificationManagerCompat.from(context).cancel(notificationId)
        }

        when (intent.action) {
            ACTION_MARK_COMPLETE -> {
                val taskId = intent.getStringExtra(EXTRA_TASK_ID) ?: return
                CoroutineScope(Dispatchers.IO).launch {
                    val db = AppDatabase.getDatabase(context)
                    db.appDao().completeTaskDirectly(taskId)
                    withContext(Dispatchers.Main) {
                        Toast.makeText(context, "Task marked complete ✓", Toast.LENGTH_SHORT).show()
                    }
                }
            }

            ACTION_SNOOZE -> {
                val itemId = intent.getStringExtra(YaseenAlarmScheduler.EXTRA_ITEM_ID) ?: return
                val itemType = intent.getStringExtra(YaseenAlarmScheduler.EXTRA_ITEM_TYPE) ?: "TASK"
                val title = intent.getStringExtra(YaseenAlarmScheduler.EXTRA_TITLE) ?: "Reminder"
                val message = intent.getStringExtra(YaseenAlarmScheduler.EXTRA_MESSAGE) ?: ""
                val snoozeMinutes = intent.getIntExtra(EXTRA_SNOOZE_MINUTES, 10)

                val snoozeTrigger = System.currentTimeMillis() + (snoozeMinutes * 60 * 1000L)
                YaseenAlarmScheduler.scheduleAlarm(
                    context = context,
                    itemId = itemId,
                    itemType = itemType,
                    title = title,
                    message = message,
                    triggerAtMillis = snoozeTrigger
                )

                CoroutineScope(Dispatchers.IO).launch {
                    val db = AppDatabase.getDatabase(context)
                    db.appDao().updateNotificationRecordStatus(itemId, "SNOOZED")
                    withContext(Dispatchers.Main) {
                        Toast.makeText(context, "Snoozed for $snoozeMinutes minutes", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }
    }
}
