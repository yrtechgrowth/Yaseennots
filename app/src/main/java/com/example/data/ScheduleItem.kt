package com.example.data

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import java.util.UUID

@Entity(
    tableName = "schedule_items",
    indices = [
        Index(value = ["dateString", "startTime"]),
        Index(value = ["isCancelled", "startTime"])
    ]
)
data class ScheduleItem(
    @PrimaryKey
    val id: String = UUID.randomUUID().toString(),
    val title: String,
    val description: String = "",
    val startTime: Long,
    val endTime: Long,
    val dateString: String, // format: "yyyy-MM-dd"
    val category: String = "Work",
    val color: Long = 0xFF10B981,
    val location: String = "",
    val isCompleted: Boolean = false,
    val isCancelled: Boolean = false,
    val isAllDay: Boolean = false,
    val recurrence: String = "NONE", // "NONE", "DAILY", "WEEKDAYS", "WEEKLY", "MONTHLY", "YEARLY", "CUSTOM"
    val recurrenceEndDate: Long? = null,
    val priority: Int = 1, // 0: Low, 1: Normal, 2: High, 3: Urgent
    val reminderMinutes: Int = 15,
    val snoozeUntil: Long? = null,
    val linkedNoteId: String? = null,
    val linkedTaskId: String? = null,
    val attachmentUri: String? = null,
    val notes: String = "",
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
) {
    /**
     * Detects if this active event conflicts/overlaps with another non-cancelled event.
     */
    fun conflictsWith(other: ScheduleItem): Boolean {
        if (id == other.id || isCancelled || other.isCancelled) return false
        if (dateString != other.dateString) return false
        if (isAllDay || other.isAllDay) return false
        // Overlap condition: start < other.end AND other.start < end
        return startTime < other.endTime && other.startTime < endTime
    }

    fun getPriorityName(): String {
        return when (priority) {
            0 -> "Low"
            2 -> "High"
            3 -> "Urgent"
            else -> "Normal"
        }
    }
}
