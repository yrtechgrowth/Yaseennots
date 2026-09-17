package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.UUID

@Entity(tableName = "notebooks")
data class Notebook(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    val name: String,
    val color: Long = 0xFF2DBD6C,
    val icon: String = "folder", // folder, book, star, work, personal, idea, code, heart
    val createdAt: Long = System.currentTimeMillis()
)
