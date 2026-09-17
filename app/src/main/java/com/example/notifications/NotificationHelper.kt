package com.example.notifications

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build

object NotificationHelper {
    const val CHANNEL_TASKS = "yaseen_tasks_channel"
    const val CHANNEL_SCHEDULE = "yaseen_schedule_channel"
    const val CHANNEL_NOTES = "yaseen_notes_channel"
    const val CHANNEL_DRAFTS = "yaseen_drafts_channel"

    fun createNotificationChannels(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationManager =
                context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

            val tasksChannel = NotificationChannel(
                CHANNEL_TASKS,
                "Tasks & Deadlines",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Reminders for scheduled tasks and upcoming deadlines"
                enableVibration(true)
                enableLights(true)
            }

            val scheduleChannel = NotificationChannel(
                CHANNEL_SCHEDULE,
                "Schedule & Events",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Alerts for calendar events and meetings"
                enableVibration(true)
                enableLights(true)
            }

            val notesChannel = NotificationChannel(
                CHANNEL_NOTES,
                "Note Reminders",
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "Reminders for pinned and saved notes"
            }

            val draftsChannel = NotificationChannel(
                CHANNEL_DRAFTS,
                "Draft Alerts",
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "Gentle reminders to finish inactive drafts"
            }

            notificationManager.createNotificationChannels(
                listOf(tasksChannel, scheduleChannel, notesChannel, draftsChannel)
            )
        }
    }
}
