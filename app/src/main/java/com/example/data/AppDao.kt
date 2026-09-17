package com.example.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface AppDao {
    // --- Notes ---
    @Query("SELECT * FROM notes WHERE isDeleted = 0 ORDER BY isPinned DESC, updatedAt DESC")
    fun getAllActiveNotes(): Flow<List<Note>>

    @Query("SELECT * FROM notes WHERE isDeleted = 1 ORDER BY deletedAt DESC")
    fun getTrashNotes(): Flow<List<Note>>

    @Query("SELECT * FROM notes WHERE id = :id LIMIT 1")
    fun getNoteById(id: String): Flow<Note?>

    @Query("SELECT * FROM notes WHERE id = :id LIMIT 1")
    suspend fun getNoteByIdSync(id: String): Note?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertNote(note: Note)

    @Update
    suspend fun updateNote(note: Note)

    @Query("UPDATE notes SET isDeleted = 1, deletedAt = :deletedAt WHERE id = :id")
    suspend fun softDeleteNote(id: String, deletedAt: Long = System.currentTimeMillis())

    @Query("UPDATE notes SET isDeleted = 0, deletedAt = NULL WHERE id = :id")
    suspend fun restoreNote(id: String)

    @Query("DELETE FROM notes WHERE id = :id")
    suspend fun deleteNotePermanently(id: String)

    @Query("DELETE FROM notes WHERE isDeleted = 1")
    suspend fun emptyTrash()

    @Query("UPDATE notes SET isPinned = :isPinned, updatedAt = :updatedAt WHERE id = :id")
    suspend fun setNotePinned(id: String, isPinned: Boolean, updatedAt: Long = System.currentTimeMillis())

    @Query("UPDATE notes SET color = :color, updatedAt = :updatedAt WHERE id = :id")
    suspend fun setNoteColor(id: String, color: Long, updatedAt: Long = System.currentTimeMillis())

    // --- Notebooks ---
    @Query("SELECT * FROM notebooks ORDER BY createdAt ASC")
    fun getAllNotebooks(): Flow<List<Notebook>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertNotebook(notebook: Notebook)

    @Update
    suspend fun updateNotebook(notebook: Notebook)

    @Query("DELETE FROM notebooks WHERE id = :id")
    suspend fun deleteNotebook(id: String)

    @Query("UPDATE notes SET notebookId = NULL WHERE notebookId = :notebookId")
    suspend fun removeNotebookFromNotes(notebookId: String)

    // --- Tasks ---
    @Query("SELECT * FROM tasks ORDER BY isCompleted ASC, dueDate ASC")
    fun getAllTasks(): Flow<List<TaskItem>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTask(task: TaskItem)

    @Update
    suspend fun updateTask(task: TaskItem)

    @Query("DELETE FROM tasks WHERE id = :id")
    suspend fun deleteTask(id: String)

    // --- Note Versions ---
    @Query("SELECT * FROM note_versions WHERE noteId = :noteId ORDER BY createdAt DESC")
    fun getVersionsForNote(noteId: String): Flow<List<NoteVersion>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertNoteVersion(version: NoteVersion)

    // --- Sync Getters for Widgets and Background Receivers ---
    @Query("SELECT * FROM notes WHERE isDeleted = 0 ORDER BY isPinned DESC, updatedAt DESC")
    suspend fun getActiveNotesSync(): List<Note>

    @Query("SELECT * FROM tasks ORDER BY isCompleted ASC, priority DESC, dueDate ASC")
    suspend fun getAllTasksSync(): List<TaskItem>

    @Query("SELECT * FROM tasks WHERE isCompleted = 0 ORDER BY priority DESC, dueDate ASC")
    suspend fun getPendingTasksSync(): List<TaskItem>

    @Query("SELECT * FROM schedule_items WHERE isCancelled = 0 ORDER BY startTime ASC")
    suspend fun getActiveScheduleItemsSync(): List<ScheduleItem>

    // --- Schedule Items ---
    @Query("SELECT * FROM schedule_items ORDER BY startTime ASC")
    fun getAllScheduleItems(): Flow<List<ScheduleItem>>

    @Query("SELECT * FROM schedule_items WHERE dateString = :dateString ORDER BY startTime ASC")
    fun getScheduleItemsByDate(dateString: String): Flow<List<ScheduleItem>>

    @Query("SELECT * FROM schedule_items WHERE title LIKE '%' || :query || '%' OR description LIKE '%' || :query || '%' OR location LIKE '%' || :query || '%' ORDER BY startTime ASC")
    fun searchScheduleItems(query: String): Flow<List<ScheduleItem>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertScheduleItem(item: ScheduleItem)

    @Update
    suspend fun updateScheduleItem(item: ScheduleItem)

    @Query("DELETE FROM schedule_items WHERE id = :id")
    suspend fun deleteScheduleItem(id: String)

    // --- Notification Records ---
    @Query("SELECT * FROM notification_records ORDER BY triggerTime DESC")
    fun getAllNotificationRecords(): Flow<List<NotificationRecord>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertNotificationRecord(record: NotificationRecord)

    @Query("UPDATE notification_records SET status = :status WHERE id = :id")
    suspend fun updateNotificationRecordStatus(id: String, status: String)

    @Query("DELETE FROM notification_records WHERE id = :id")
    suspend fun deleteNotificationRecord(id: String)

    @Query("DELETE FROM notification_records WHERE triggerTime < :beforeTimestamp")
    suspend fun clearOldNotificationRecords(beforeTimestamp: Long)

    @Query("UPDATE tasks SET isCompleted = 1, completedAt = :completedAt WHERE id = :id")
    suspend fun completeTaskDirectly(id: String, completedAt: Long = System.currentTimeMillis())

    @Query("UPDATE notes SET isDraft = 0, updatedAt = :now WHERE id = :id")
    suspend fun convertDraftToNote(id: String, now: Long = System.currentTimeMillis())

    @Query("UPDATE notes SET isFavorite = :isFav, updatedAt = :now WHERE id = :id")
    suspend fun setNoteFavorite(id: String, isFav: Boolean, now: Long = System.currentTimeMillis())

    @Query("UPDATE notes SET isArchived = :isArchived, updatedAt = :now WHERE id = :id")
    suspend fun setNoteArchived(id: String, isArchived: Boolean, now: Long = System.currentTimeMillis())

    // --- Chat Messages ---
    @Query("SELECT * FROM chat_messages ORDER BY timestamp ASC")
    fun getAllChatMessages(): Flow<List<ChatMessage>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertChatMessage(message: ChatMessage)

    @Query("DELETE FROM chat_messages")
    suspend fun clearChatMessages()
}
