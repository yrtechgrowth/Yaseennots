package com.example.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Database(
    entities = [
        Note::class,
        Notebook::class,
        TaskItem::class,
        NoteVersion::class,
        ScheduleItem::class,
        ChatMessage::class,
        NotificationRecord::class
    ],
    version = 5,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun appDao(): AppDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        val MIGRATION_3_4 = object : Migration(3, 4) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("CREATE INDEX IF NOT EXISTS `index_notes_isDeleted_isPinned_updatedAt` ON `notes` (`isDeleted`, `isPinned`, `updatedAt`)")
                db.execSQL("CREATE INDEX IF NOT EXISTS `index_notes_notebookId` ON `notes` (`notebookId`)")
                db.execSQL("CREATE INDEX IF NOT EXISTS `index_notes_updatedAt` ON `notes` (`updatedAt`)")
                db.execSQL("CREATE INDEX IF NOT EXISTS `index_tasks_isCompleted_dueDate` ON `tasks` (`isCompleted`, `dueDate`)")
                db.execSQL("CREATE INDEX IF NOT EXISTS `index_tasks_isCompleted_priority` ON `tasks` (`isCompleted`, `priority`)")
                db.execSQL("CREATE INDEX IF NOT EXISTS `index_tasks_dueDate` ON `tasks` (`dueDate`)")
                db.execSQL("CREATE INDEX IF NOT EXISTS `index_schedule_items_dateString_startTime` ON `schedule_items` (`dateString`, `startTime`)")
                db.execSQL("CREATE INDEX IF NOT EXISTS `index_schedule_items_isCancelled_startTime` ON `schedule_items` (`isCancelled`, `startTime`)")
                db.execSQL("CREATE INDEX IF NOT EXISTS `index_chat_messages_timestamp` ON `chat_messages` (`timestamp`)")
            }
        }

        val MIGRATION_4_5 = object : Migration(4, 5) {
            override fun migrate(db: SupportSQLiteDatabase) {
                // Notes safe additions
                db.execSQL("ALTER TABLE `notes` ADD COLUMN `isFavorite` INTEGER NOT NULL DEFAULT 0")
                db.execSQL("ALTER TABLE `notes` ADD COLUMN `isArchived` INTEGER NOT NULL DEFAULT 0")
                db.execSQL("ALTER TABLE `notes` ADD COLUMN `isDraft` INTEGER NOT NULL DEFAULT 0")
                db.execSQL("ALTER TABLE `notes` ADD COLUMN `audioPath` TEXT")

                // Tasks safe additions
                db.execSQL("ALTER TABLE `tasks` ADD COLUMN `notes` TEXT NOT NULL DEFAULT ''")
                db.execSQL("ALTER TABLE `tasks` ADD COLUMN `category` TEXT NOT NULL DEFAULT 'General'")
                db.execSQL("ALTER TABLE `tasks` ADD COLUMN `tags` TEXT NOT NULL DEFAULT ''")
                db.execSQL("ALTER TABLE `tasks` ADD COLUMN `dueTime` TEXT")
                db.execSQL("ALTER TABLE `tasks` ADD COLUMN `isAllDay` INTEGER NOT NULL DEFAULT 1")
                db.execSQL("ALTER TABLE `tasks` ADD COLUMN `updatedAt` INTEGER NOT NULL DEFAULT 0")
                db.execSQL("ALTER TABLE `tasks` ADD COLUMN `reminderMinutesBefore` INTEGER")
                db.execSQL("ALTER TABLE `tasks` ADD COLUMN `recurrenceRule` TEXT NOT NULL DEFAULT 'NONE'")
                db.execSQL("ALTER TABLE `tasks` ADD COLUMN `recurrenceEndDate` INTEGER")
                db.execSQL("ALTER TABLE `tasks` ADD COLUMN `subtasksJson` TEXT NOT NULL DEFAULT '[]'")
                db.execSQL("ALTER TABLE `tasks` ADD COLUMN `linkedNoteId` TEXT")
                db.execSQL("ALTER TABLE `tasks` ADD COLUMN `linkedScheduleId` TEXT")
                db.execSQL("ALTER TABLE `tasks` ADD COLUMN `attachmentUri` TEXT")

                // Schedule items safe additions
                db.execSQL("ALTER TABLE `schedule_items` ADD COLUMN `recurrenceEndDate` INTEGER")
                db.execSQL("ALTER TABLE `schedule_items` ADD COLUMN `notes` TEXT NOT NULL DEFAULT ''")

                // Notification records table
                db.execSQL("CREATE TABLE IF NOT EXISTS `notification_records` (`id` TEXT NOT NULL PRIMARY KEY, `itemId` TEXT NOT NULL, `itemType` TEXT NOT NULL, `title` TEXT NOT NULL, `message` TEXT NOT NULL, `triggerTime` INTEGER NOT NULL, `status` TEXT NOT NULL, `createdAt` INTEGER NOT NULL)")
                db.execSQL("CREATE INDEX IF NOT EXISTS `index_notification_records_triggerTime` ON `notification_records` (`triggerTime`)")
                db.execSQL("CREATE INDEX IF NOT EXISTS `index_notification_records_status` ON `notification_records` (`status`)")
                db.execSQL("CREATE INDEX IF NOT EXISTS `index_notes_isDraft` ON `notes` (`isDraft`)")
                db.execSQL("CREATE INDEX IF NOT EXISTS `index_notes_isFavorite` ON `notes` (`isFavorite`)")
                db.execSQL("CREATE INDEX IF NOT EXISTS `index_notes_isArchived` ON `notes` (`isArchived`)")
            }
        }

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "yaseen_app.db"
                )
                .addMigrations(MIGRATION_3_4, MIGRATION_4_5)
                .addCallback(object : Callback() {
                    override fun onCreate(db: SupportSQLiteDatabase) {
                        super.onCreate(db)
                        INSTANCE?.let { database ->
                            CoroutineScope(Dispatchers.IO).launch {
                                populateInitialData(database.appDao())
                            }
                        }
                    }
                }).build()
                INSTANCE = instance
                instance
            }
        }

        private suspend fun populateInitialData(dao: AppDao) {
            val personalNotebook = Notebook(
                id = "nb-personal",
                name = "Personal",
                color = 0xFF2DBD6C,
                icon = "heart"
            )
            val workNotebook = Notebook(
                id = "nb-work",
                name = "Work & Ideas",
                color = 0xFF0288D1,
                icon = "work"
            )
            val studyNotebook = Notebook(
                id = "nb-study",
                name = "Study & Reading",
                color = 0xFF7C3AED,
                icon = "book"
            )

            dao.insertNotebook(personalNotebook)
            dao.insertNotebook(workNotebook)
            dao.insertNotebook(studyNotebook)

            // Welcome Note
            dao.insertNote(
                Note(
                    id = "note-welcome",
                    title = "Welcome to Yaseen Suite 🚀",
                    content = "Yaseen is your unified productivity hub:\n\n📝 Yaseen Notes: Rich notes, color themes, notebooks, drawing canvas & version history.\n✅ Yaseen Tasks: Categorized to-dos with priorities and deadlines.\n📅 Yaseen Schedule: Daily timetable and event schedule.\n🤖 YaRVerse Chatbot: AI assistant running with your choice of Gemini, OpenAI, or OpenRouter keys configured in Settings.",
                    description = "Overview of Yaseen suite features",
                    tags = "Welcome,Guide,Yaseen",
                    color = 0xFFE8F5E9,
                    isPinned = true,
                    notebookId = personalNotebook.id
                )
            )

            dao.insertNote(
                Note(
                    id = "note-meeting-template",
                    title = "Product Strategy & Architecture",
                    content = "Agenda:\n1. Roadmap for Yaseen suite\n2. Offline-first Room database sync\n3. YaRVerse AI chatbot integration\n\nDecisions:\n- Allow user to customize app name and icon theme in Settings.\n- Support multi-provider AI (Gemini, OpenAI, OpenRouter).",
                    description = "Meeting notes and strategy decisions",
                    tags = "Work,Planning",
                    color = 0xFFE3F2FD,
                    isPinned = false,
                    notebookId = workNotebook.id
                )
            )

            // Initial Tasks
            dao.insertTask(
                TaskItem(
                    id = "task-1",
                    title = "Configure YaRVerse API key in Settings",
                    description = "Choose Gemini, OpenAI, or OpenRouter and enter your key in Settings",
                    dueDate = System.currentTimeMillis() + 86400000L,
                    isCompleted = false,
                    priority = 2
                )
            )
            dao.insertTask(
                TaskItem(
                    id = "task-2",
                    title = "Plan week in Yaseen Schedule",
                    description = "Add calendar blocks for focused work and meetings",
                    dueDate = System.currentTimeMillis() + 172800000L,
                    isCompleted = false,
                    priority = 1
                )
            )

            // Initial Schedule Items
            val todayStr = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
            val baseTime = System.currentTimeMillis()
            dao.insertScheduleItem(
                ScheduleItem(
                    id = "sch-1",
                    title = "Morning Planning & Review",
                    description = "Check pending tasks in Yaseen Tasks and organize daily priorities",
                    startTime = baseTime,
                    endTime = baseTime + 3600000L,
                    dateString = todayStr,
                    category = "Routine",
                    color = 0xFF10B981,
                    location = "Workstation"
                )
            )
            dao.insertScheduleItem(
                ScheduleItem(
                    id = "sch-2",
                    title = "Deep Work Session",
                    description = "Focus on project core features and documentation",
                    startTime = baseTime + 7200000L,
                    endTime = baseTime + 14400000L,
                    dateString = todayStr,
                    category = "Work",
                    color = 0xFF3B82F6,
                    location = "Office / Studio"
                )
            )

            // Welcome Chat Message
            dao.insertChatMessage(
                ChatMessage(
                    id = "msg-welcome",
                    sender = "yarverse",
                    text = "Hello! I am YaRVerse, your personal AI assistant within the Yaseen suite. You can configure your API key (Gemini, OpenAI, or OpenRouter) in Settings to start chatting, brainstorming, summarizing your notes, and planning your schedule!",
                    timestamp = System.currentTimeMillis(),
                    provider = "System"
                )
            )
        }
    }
}
