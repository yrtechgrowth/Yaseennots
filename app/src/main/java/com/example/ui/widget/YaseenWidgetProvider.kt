package com.example.ui.widget

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.widget.RemoteViews
import com.example.MainActivity
import com.example.R

/**
 * Real Android AppWidget Provider for Yaseen Home Screen Widgets.
 * Uses cached data for zero-latency RemoteViews rendering without main-thread database blocking.
 */
class YaseenWidgetProvider : AppWidgetProvider() {

    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray
    ) {
        for (appWidgetId in appWidgetIds) {
            updateAppWidget(context, appWidgetManager, appWidgetId)
        }
    }

    override fun onReceive(context: Context, intent: Intent) {
        super.onReceive(context, intent)
        if (intent.action == ACTION_REFRESH_WIDGET) {
            val appWidgetManager = AppWidgetManager.getInstance(context)
            val thisWidget = ComponentName(context, YaseenWidgetProvider::class.java)
            val appWidgetIds = appWidgetManager.getAppWidgetIds(thisWidget)
            for (id in appWidgetIds) {
                updateAppWidget(context, appWidgetManager, id)
            }
        }
    }

    companion object {
        const val ACTION_REFRESH_WIDGET = "com.example.ACTION_REFRESH_WIDGET"
        private const val PREFS_NAME = "yaseen_widget_cache"
        private const val KEY_NOTES_COUNT = "notes_count"
        private const val KEY_TASKS_COUNT = "tasks_count"
        private const val KEY_PREVIEW_TEXT = "preview_text"

        fun updateWidgetData(
            context: Context,
            notesCount: Int,
            pendingTasksCount: Int,
            previewMessage: String?
        ) {
            val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            prefs.edit()
                .putInt(KEY_NOTES_COUNT, notesCount)
                .putInt(KEY_TASKS_COUNT, pendingTasksCount)
                .putString(KEY_PREVIEW_TEXT, previewMessage ?: "$pendingTasksCount tasks pending")
                .apply()

            // Trigger broadcast refresh for all widget instances
            val intent = Intent(context, YaseenWidgetProvider::class.java).apply {
                action = ACTION_REFRESH_WIDGET
            }
            context.sendBroadcast(intent)
        }

        fun updateAppWidget(
            context: Context,
            appWidgetManager: AppWidgetManager,
            appWidgetId: Int
        ) {
            val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            val notesCount = prefs.getInt(KEY_NOTES_COUNT, 0)
            val tasksCount = prefs.getInt(KEY_TASKS_COUNT, 0)
            val previewText = prefs.getString(KEY_PREVIEW_TEXT, "Tap any action above to jump in")

            val views = RemoteViews(context.packageName, R.layout.widget_yaseen_quick)

            // Header text
            views.setTextViewText(R.id.widget_title, "Yaseen")
            views.setTextViewText(R.id.widget_stats, "$notesCount Notes • $tasksCount Tasks")
            views.setTextViewText(R.id.widget_preview_text, previewText)

            // 1. Root Click -> Open Main Activity
            val rootIntent = Intent(context, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            }
            val rootPendingIntent = PendingIntent.getActivity(
                context,
                0,
                rootIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            views.setOnClickPendingIntent(R.id.widget_root, rootPendingIntent)

            // 2. New Note Shortcut
            val noteIntent = Intent(context, MainActivity::class.java).apply {
                action = "ACTION_NEW_NOTE"
                putExtra("NAV_TARGET", "notes")
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            }
            val notePendingIntent = PendingIntent.getActivity(
                context,
                101,
                noteIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            views.setOnClickPendingIntent(R.id.btn_widget_new_note, notePendingIntent)

            // 3. New Task Shortcut
            val taskIntent = Intent(context, MainActivity::class.java).apply {
                action = "ACTION_NEW_TASK"
                putExtra("NAV_TARGET", "tasks")
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            }
            val taskPendingIntent = PendingIntent.getActivity(
                context,
                102,
                taskIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            views.setOnClickPendingIntent(R.id.btn_widget_new_task, taskPendingIntent)

            // 4. Schedule Shortcut
            val schedIntent = Intent(context, MainActivity::class.java).apply {
                action = "ACTION_SCHEDULE"
                putExtra("NAV_TARGET", "schedule")
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            }
            val schedPendingIntent = PendingIntent.getActivity(
                context,
                103,
                schedIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            views.setOnClickPendingIntent(R.id.btn_widget_schedule, schedPendingIntent)

            // 5. Files Shortcut
            val filesIntent = Intent(context, MainActivity::class.java).apply {
                action = "ACTION_FILES"
                putExtra("NAV_TARGET", "files")
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            }
            val filesPendingIntent = PendingIntent.getActivity(
                context,
                104,
                filesIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            views.setOnClickPendingIntent(R.id.btn_widget_files, filesPendingIntent)

            appWidgetManager.updateAppWidget(appWidgetId, views)
        }
    }
}
