package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.UUID

@Entity(tableName = "note_versions")
data class NoteVersion(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    val noteId: String,
    val title: String,
    val content: String,
    val createdAt: Long = System.currentTimeMillis(),
    val color: Long = 0xFFFFFFFF
)
