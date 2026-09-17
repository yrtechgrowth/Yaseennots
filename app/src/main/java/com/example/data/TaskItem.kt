package com.example.data

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import java.util.UUID

data class Subtask(
    val id: String = UUID.randomUUID().toString(),
    val title: String,
    val isCompleted: Boolean = false
)

@Entity(
    tableName = "tasks",
    indices = [
        Index(value = ["isCompleted", "dueDate"]),
        Index(value = ["isCompleted", "priority"]),
        Index(value = ["dueDate"])
    ]
)
data class TaskItem(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    val title: String,
    val description: String = "",
    val notes: String = "",
    val category: String = "General",
    val tags: String = "",
    val dueDate: Long = System.currentTimeMillis(),
    val dueTime: String? = null, // e.g. "14:30"
    val isAllDay: Boolean = true,
    val isCompleted: Boolean = false,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis(),
    val completedAt: Long? = null,
    val reminderDateTime: Long? = null,
    val reminderMinutesBefore: Int? = null, // 0 = At due time, 5, 10, 15, 30, 60, 1440
    val priority: Int = 1, // 0: Low, 1: Medium, 2: High, 3: Urgent
    val recurrenceRule: String = "NONE", // NONE, DAILY, WEEKDAYS, WEEKLY, MONTHLY, YEARLY, CUSTOM
    val recurrenceEndDate: Long? = null,
    val subtasksJson: String = "[]",
    val linkedNoteId: String? = null,
    val linkedScheduleId: String? = null,
    val attachmentUri: String? = null
) {
    fun getSubtasks(): List<Subtask> {
        if (subtasksJson.isBlank() || subtasksJson == "[]") return emptyList()
        return try {
            val jsonArray = org.json.JSONArray(subtasksJson)
            val list = mutableListOf<Subtask>()
            for (i in 0 until jsonArray.length()) {
                val obj = jsonArray.getJSONObject(i)
                list.add(
                    Subtask(
                        id = obj.optString("id", UUID.randomUUID().toString()),
                        title = obj.optString("title", ""),
                        isCompleted = obj.optBoolean("isCompleted", false)
                    )
                )
            }
            list
        } catch (_: Exception) {
            emptyList()
        }
    }

    fun withUpdatedSubtasks(subtasks: List<Subtask>): TaskItem {
        val jsonArray = org.json.JSONArray()
        for (st in subtasks) {
            val obj = org.json.JSONObject().apply {
                put("id", st.id)
                put("title", st.title)
                put("isCompleted", st.isCompleted)
            }
            jsonArray.put(obj)
        }
        return copy(subtasksJson = jsonArray.toString(), updatedAt = System.currentTimeMillis())
    }

    fun getPriorityLabel(): String {
        return when (priority) {
            0 -> "Low"
            2 -> "High"
            3 -> "Urgent"
            else -> "Medium"
        }
    }
}
