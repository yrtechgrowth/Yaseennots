package com.example.data

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import java.util.UUID

@Entity(
    tableName = "notification_records",
    indices = [
        Index(value = ["triggerTime"]),
        Index(value = ["status"])
    ]
)
data class NotificationRecord(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    val itemId: String,
    val itemType: String, // "TASK", "SCHEDULE", "NOTE", "DRAFT"
    val title: String,
    val message: String,
    val triggerTime: Long,
    val status: String = "UPCOMING", // "UPCOMING", "MISSED", "COMPLETED", "SNOOZED"
    val createdAt: Long = System.currentTimeMillis()
)
