package com.example.data

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import java.util.UUID

@Entity(
    tableName = "chat_messages",
    indices = [
        Index(value = ["timestamp"])
    ]
)
data class ChatMessage(
    @PrimaryKey
    val id: String = UUID.randomUUID().toString(),
    val sender: String, // "user" or "yarverse"
    val text: String,
    val timestamp: Long = System.currentTimeMillis(),
    val provider: String = "Gemini",
    val isError: Boolean = false
)
