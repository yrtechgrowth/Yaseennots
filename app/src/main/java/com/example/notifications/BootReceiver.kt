package com.example.notifications

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.example.data.AppDatabase
import com.example.data.SettingsManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class BootReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val action = intent.action
        if (action == Intent.ACTION_BOOT_COMPLETED ||
            action == Intent.ACTION_TIMEZONE_CHANGED ||
            action == Intent.ACTION_TIME_SET
        ) {
            val settings = SettingsManager(context).loadSettings()
            if (!settings.notificationsEnabled) return

            NotificationHelper.createNotificationChannels(context)

            CoroutineScope(Dispatchers.IO).launch {
                try {
                    val db = AppDatabase.getDatabase(context)
                    val dao = db.appDao()
                    val now = System.currentTimeMillis()

                    // Reschedule active tasks
                    if (settings.taskRemindersEnabled) {
                        val tasks = dao.getPendingTasksSync()
                        for (task in tasks) {
                            val trigger = task.reminderDateTime ?: if (task.dueDate > now) task.dueDate else null
                            if (trigger != null && trigger > now) {
                                YaseenAlarmScheduler.scheduleAlarm(
                                    context = context,
                                    itemId = task.id,
                                    itemType = "TASK",
                                    title = "Task: ${task.title}",
                                    message = if (task.description.isNotBlank()) task.description else "Task is due",
                                    triggerAtMillis = trigger
                                )
                            }
                        }
                    }

                    // Reschedule active schedule items
                    if (settings.scheduleRemindersEnabled) {
                        val scheduleItems = dao.getActiveScheduleItemsSync()
                        for (item in scheduleItems) {
                            val reminderOffsetMs = item.reminderMinutes * 60 * 1000L
                            val trigger = item.startTime - reminderOffsetMs
                            if (trigger > now) {
                                YaseenAlarmScheduler.scheduleAlarm(
                                    context = context,
                                    itemId = item.id,
                                    itemType = "SCHEDULE",
                                    title = "Upcoming Event: ${item.title}",
                                    message = "Starts soon at ${item.location.ifBlank { "scheduled time" }}",
                                    triggerAtMillis = trigger
                                )
                            }
                        }
                    }
                } catch (_: Exception) {}
            }
        }
    }
}
