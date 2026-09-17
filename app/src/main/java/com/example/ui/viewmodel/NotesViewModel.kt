package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.AppDatabase
import com.example.data.AppSettings
import com.example.data.ChatApiService
import com.example.data.ChatMessage
import com.example.data.Note
import com.example.data.NoteVersion
import com.example.data.Notebook
import com.example.data.ScheduleItem
import com.example.data.SettingsManager
import com.example.data.TaskItem
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import org.json.JSONArray
import org.json.JSONObject
import java.util.UUID

enum class SortOption(val displayName: String) {
    UPDATED_DESC("Recently Modified"),
    UPDATED_ASC("Oldest Modified"),
    CREATED_DESC("Newest Created"),
    CREATED_ASC("Oldest Created"),
    TITLE_ASC("Title (A-Z)"),
    TITLE_DESC("Title (Z-A)")
}

enum class TaskFilter(val displayName: String) {
    ALL("All"),
    TODAY("Today"),
    UPCOMING("Upcoming"),
    COMPLETED("Completed")
}

class NotesViewModel(application: Application) : AndroidViewModel(application) {
    private val dao = AppDatabase.getDatabase(application).appDao()

    val rawActiveNotes = dao.getAllActiveNotes().stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000),
        emptyList()
    )

    val trashNotes = dao.getTrashNotes().stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000),
        emptyList()
    )

    val notebooks = dao.getAllNotebooks().stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000),
        emptyList()
    )

    val tasks = dao.getAllTasks().stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000),
        emptyList()
    )

    val scheduleItems = dao.getAllScheduleItems().stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000),
        emptyList()
    )

    val chatMessages = dao.getAllChatMessages().stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000),
        emptyList()
    )

    private val settingsManager = SettingsManager(application)
    private val _settings = MutableStateFlow(settingsManager.loadSettings())
    val settings: StateFlow<AppSettings> = _settings.asStateFlow()

    private val chatApiService = ChatApiService()

    private val _isChatSending = MutableStateFlow(false)
    val isChatSending: StateFlow<Boolean> = _isChatSending.asStateFlow()

    private val _chatErrorMessage = MutableStateFlow<String?>(null)
    val chatErrorMessage: StateFlow<String?> = _chatErrorMessage.asStateFlow()

    // UI Filter & Sort States
    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _selectedNotebookId = MutableStateFlow<String?>(null)
    val selectedNotebookId: StateFlow<String?> = _selectedNotebookId.asStateFlow()

    private val _selectedColorFilter = MutableStateFlow<Long?>(null)
    val selectedColorFilter: StateFlow<Long?> = _selectedColorFilter.asStateFlow()

    private val _selectedTagFilter = MutableStateFlow<String?>(null)
    val selectedTagFilter: StateFlow<String?> = _selectedTagFilter.asStateFlow()

    private val _showOnlyPinned = MutableStateFlow(false)
    val showOnlyPinned: StateFlow<Boolean> = _showOnlyPinned.asStateFlow()

    private val _currentSort = MutableStateFlow(SortOption.UPDATED_DESC)
    val currentSort: StateFlow<SortOption> = _currentSort.asStateFlow()

    // Selection mode state
    private val _isSelectionMode = MutableStateFlow(false)
    val isSelectionMode: StateFlow<Boolean> = _isSelectionMode.asStateFlow()

    private val _selectedNoteIds = MutableStateFlow<Set<String>>(emptySet())
    val selectedNoteIds: StateFlow<Set<String>> = _selectedNoteIds.asStateFlow()

    // Task filter state
    private val _currentTaskFilter = MutableStateFlow(TaskFilter.ALL)
    val currentTaskFilter: StateFlow<TaskFilter> = _currentTaskFilter.asStateFlow()

    data class FilterCriteria(
        val query: String,
        val notebookId: String?,
        val color: Long?,
        val tag: String?,
        val onlyPinned: Boolean,
        val sort: SortOption
    )

    private val filterCriteria1 = combine(
        searchQuery,
        selectedNotebookId,
        selectedColorFilter
    ) { query, notebookId, color ->
        Triple(query, notebookId, color)
    }

    private val filterCriteria2 = combine(
        selectedTagFilter,
        showOnlyPinned,
        currentSort
    ) { tag, onlyPinned, sort ->
        Triple(tag, onlyPinned, sort)
    }

    private val allFilterCriteria = combine(
        filterCriteria1,
        filterCriteria2
    ) { c1, c2 ->
        FilterCriteria(
            query = c1.first,
            notebookId = c1.second,
            color = c1.third,
            tag = c2.first,
            onlyPinned = c2.second,
            sort = c2.third
        )
    }

    // Filtered and Sorted Notes
    val filteredNotes: StateFlow<List<Note>> = combine(
        rawActiveNotes,
        allFilterCriteria
    ) { notes, criteria ->
        var list = notes

        if (criteria.notebookId != null) {
            list = list.filter { it.notebookId == criteria.notebookId }
        }
        if (criteria.color != null) {
            list = list.filter { it.color == criteria.color }
        }
        if (criteria.tag != null) {
            list = list.filter { it.getTagList().any { t -> t.equals(criteria.tag, ignoreCase = true) } }
        }
        if (criteria.onlyPinned) {
            list = list.filter { it.isPinned }
        }
        if (criteria.query.isNotBlank()) {
            val q = criteria.query.trim().lowercase()
            list = list.filter {
                it.title.lowercase().contains(q) ||
                it.content.lowercase().contains(q) ||
                it.description.lowercase().contains(q) ||
                it.tags.lowercase().contains(q)
            }
        }

        // Apply Sorting
        when (criteria.sort) {
            SortOption.UPDATED_DESC -> list.sortedWith(compareByDescending<Note> { it.isPinned }.thenByDescending { it.updatedAt })
            SortOption.UPDATED_ASC -> list.sortedWith(compareByDescending<Note> { it.isPinned }.thenBy { it.updatedAt })
            SortOption.CREATED_DESC -> list.sortedWith(compareByDescending<Note> { it.isPinned }.thenByDescending { it.createdAt })
            SortOption.CREATED_ASC -> list.sortedWith(compareByDescending<Note> { it.isPinned }.thenBy { it.createdAt })
            SortOption.TITLE_ASC -> list.sortedWith(compareByDescending<Note> { it.isPinned }.thenBy { it.title.lowercase() })
            SortOption.TITLE_DESC -> list.sortedWith(compareByDescending<Note> { it.isPinned }.thenByDescending { it.title.lowercase() })
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Filtered Tasks
    val filteredTasks: StateFlow<List<TaskItem>> = combine(tasks, currentTaskFilter) { allTasks, filter ->
        val now = System.currentTimeMillis()
        val startOfDay = now - (now % 86400000L)
        val endOfDay = startOfDay + 86400000L

        when (filter) {
            TaskFilter.ALL -> allTasks
            TaskFilter.TODAY -> allTasks.filter { !it.isCompleted && it.dueDate in startOfDay..endOfDay }
            TaskFilter.UPCOMING -> allTasks.filter { !it.isCompleted && it.dueDate > endOfDay }
            TaskFilter.COMPLETED -> allTasks.filter { it.isCompleted }
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Filter controls
    fun setSearchQuery(query: String) { _searchQuery.value = query }
    fun selectNotebook(id: String?) { _selectedNotebookId.value = id }
    fun setColorFilter(color: Long?) { _selectedColorFilter.value = color }
    fun setTagFilter(tag: String?) { _selectedTagFilter.value = tag }
    fun toggleOnlyPinned() { _showOnlyPinned.value = !_showOnlyPinned.value }
    fun setSort(sort: SortOption) { _currentSort.value = sort }
    fun setTaskFilter(filter: TaskFilter) { _currentTaskFilter.value = filter }

    fun clearAllFilters() {
        _searchQuery.value = ""
        _selectedNotebookId.value = null
        _selectedColorFilter.value = null
        _selectedTagFilter.value = null
        _showOnlyPinned.value = false
    }

    // Selection mode operations
    fun toggleSelectionMode() {
        val newMode = !_isSelectionMode.value
        _isSelectionMode.value = newMode
        if (!newMode) {
            _selectedNoteIds.value = emptySet()
        }
    }

    fun toggleNoteSelection(id: String) {
        val current = _selectedNoteIds.value.toMutableSet()
        if (current.contains(id)) {
            current.remove(id)
        } else {
            current.add(id)
        }
        _selectedNoteIds.value = current
        if (current.isEmpty()) {
            _isSelectionMode.value = false
        }
    }

    fun selectAllNotes() {
        val allIds = filteredNotes.value.map { it.id }.toSet()
        _selectedNoteIds.value = allIds
        _isSelectionMode.value = true
    }

    fun clearSelection() {
        _selectedNoteIds.value = emptySet()
        _isSelectionMode.value = false
    }

    fun bulkPin(pin: Boolean) {
        viewModelScope.launch {
            val ids = _selectedNoteIds.value
            ids.forEach { id ->
                dao.setNotePinned(id, pin)
            }
            clearSelection()
        }
    }

    fun bulkColor(color: Long) {
        viewModelScope.launch {
            val ids = _selectedNoteIds.value
            ids.forEach { id ->
                dao.setNoteColor(id, color)
            }
            clearSelection()
        }
    }

    fun bulkMoveToTrash() {
        viewModelScope.launch {
            val ids = _selectedNoteIds.value
            val now = System.currentTimeMillis()
            ids.forEach { id ->
                dao.softDeleteNote(id, now)
            }
            clearSelection()
        }
    }

    // Note operations
    fun saveNote(note: Note) {
        viewModelScope.launch {
            // Save version snapshot if note already existed and content changed
            val existing = dao.getNoteByIdSync(note.id)
            if (existing != null && (existing.title != note.title || existing.content != note.content)) {
                dao.insertNoteVersion(
                    NoteVersion(
                        noteId = existing.id,
                        title = existing.title,
                        content = existing.content,
                        color = existing.color,
                        createdAt = existing.updatedAt
                    )
                )
            }
            dao.insertNote(note.copy(updatedAt = System.currentTimeMillis()))
        }
    }

    fun togglePin(note: Note) {
        viewModelScope.launch {
            dao.setNotePinned(note.id, !note.isPinned)
        }
    }

    fun duplicateNote(note: Note) {
        viewModelScope.launch {
            val duplicate = note.copy(
                id = UUID.randomUUID().toString(),
                title = if (note.title.isNotBlank()) "${note.title} (Copy)" else "Copy Note",
                createdAt = System.currentTimeMillis(),
                updatedAt = System.currentTimeMillis(),
                isPinned = false
            )
            dao.insertNote(duplicate)
        }
    }

    fun toggleArchiveNote(note: Note) {
        viewModelScope.launch {
            val currentTags = note.getTagList().toMutableList()
            if (currentTags.any { it.equals("Archive", ignoreCase = true) }) {
                currentTags.removeAll { it.equals("Archive", ignoreCase = true) }
            } else {
                currentTags.add("Archive")
            }
            dao.updateNote(note.copy(tags = currentTags.joinToString(","), updatedAt = System.currentTimeMillis()))
        }
    }

    fun moveToTrash(noteId: String) {
        viewModelScope.launch {
            dao.softDeleteNote(noteId, System.currentTimeMillis())
        }
    }

    fun restoreNote(noteId: String) {
        viewModelScope.launch {
            dao.restoreNote(noteId)
        }
    }

    fun deletePermanently(noteId: String) {
        viewModelScope.launch {
            dao.deleteNotePermanently(noteId)
        }
    }

    fun emptyTrash() {
        viewModelScope.launch {
            dao.emptyTrash()
        }
    }

    fun getVersionsForNote(noteId: String) = dao.getVersionsForNote(noteId)

    // Notebook operations
    fun addOrUpdateNotebook(notebook: Notebook) {
        viewModelScope.launch {
            dao.insertNotebook(notebook)
        }
    }

    fun deleteNotebook(notebookId: String) {
        viewModelScope.launch {
            dao.removeNotebookFromNotes(notebookId)
            dao.deleteNotebook(notebookId)
            if (_selectedNotebookId.value == notebookId) {
                _selectedNotebookId.value = null
            }
        }
    }

    // Task operations
    fun addOrUpdateTask(task: TaskItem) {
        viewModelScope.launch {
            dao.insertTask(task)
        }
    }

    fun toggleTaskComplete(task: TaskItem) {
        viewModelScope.launch {
            val completed = !task.isCompleted
            val completedAt = if (completed) System.currentTimeMillis() else null
            dao.updateTask(task.copy(isCompleted = completed, completedAt = completedAt))
        }
    }

    fun deleteTask(taskId: String) {
        viewModelScope.launch {
            dao.deleteTask(taskId)
        }
    }

    // Export & Backup
    fun exportNotesAsJson(): String {
        val root = JSONObject()
        val notesArray = JSONArray()
        rawActiveNotes.value.forEach { note ->
            val noteObj = JSONObject().apply {
                put("id", note.id)
                put("title", note.title)
                put("content", note.content)
                put("description", note.description)
                put("tags", note.tags)
                put("color", note.color)
                put("isPinned", note.isPinned)
                put("notebookId", note.notebookId ?: "")
                put("createdAt", note.createdAt)
                put("updatedAt", note.updatedAt)
            }
            notesArray.put(noteObj)
        }
        val notebooksArray = JSONArray()
        notebooks.value.forEach { nb ->
            val nbObj = JSONObject().apply {
                put("id", nb.id)
                put("name", nb.name)
                put("color", nb.color)
                put("icon", nb.icon)
            }
            notebooksArray.put(nbObj)
        }
        root.put("appName", "Yaseen Notes")
        root.put("version", "1.1.0")
        root.put("exportTime", System.currentTimeMillis())
        root.put("notes", notesArray)
        root.put("notebooks", notebooksArray)
        return root.toString(2)
    }

    fun importNotesFromJson(jsonString: String): Result<Int> {
        return try {
            val root = JSONObject(jsonString)
            val notesArray = root.optJSONArray("notes") ?: JSONArray()
            val importedCount = notesArray.length()
            viewModelScope.launch {
                for (i in 0 until notesArray.length()) {
                    val obj = notesArray.getJSONObject(i)
                    val note = Note(
                        id = obj.optString("id", UUID.randomUUID().toString()),
                        title = obj.optString("title", "Imported Note"),
                        content = obj.optString("content", ""),
                        description = obj.optString("description", ""),
                        tags = obj.optString("tags", ""),
                        color = obj.optLong("color", 0xFFFFFFFF),
                        isPinned = obj.optBoolean("isPinned", false),
                        notebookId = obj.optString("notebookId").takeIf { it.isNotBlank() },
                        createdAt = obj.optLong("createdAt", System.currentTimeMillis()),
                        updatedAt = obj.optLong("updatedAt", System.currentTimeMillis())
                    )
                    dao.insertNote(note)
                }
            }
            Result.success(importedCount)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // --- Settings Actions ---
    fun updateSettings(newSettings: AppSettings) {
        _settings.value = newSettings
        settingsManager.saveSettings(newSettings)
    }

    // --- Schedule Actions ---
    fun addScheduleItem(
        title: String,
        description: String = "",
        startTime: Long,
        endTime: Long,
        dateString: String,
        category: String = "Work",
        color: Long = 0xFF10B981,
        location: String = ""
    ) {
        viewModelScope.launch {
            val item = ScheduleItem(
                title = title,
                description = description,
                startTime = startTime,
                endTime = endTime,
                dateString = dateString,
                category = category,
                color = color,
                location = location
            )
            dao.insertScheduleItem(item)
        }
    }

    fun updateScheduleItem(item: ScheduleItem) {
        viewModelScope.launch {
            dao.updateScheduleItem(item.copy(updatedAt = System.currentTimeMillis()))
        }
    }

    fun deleteScheduleItem(id: String) {
        viewModelScope.launch {
            dao.deleteScheduleItem(id)
        }
    }

    fun toggleScheduleCompleted(item: ScheduleItem) {
        viewModelScope.launch {
            dao.updateScheduleItem(
                item.copy(
                    isCompleted = !item.isCompleted,
                    updatedAt = System.currentTimeMillis()
                )
            )
        }
    }

    // --- Chatbot (YaRVerse) Actions ---
    fun sendChatMessage(userText: String) {
        if (userText.isBlank()) return
        val currentSettings = _settings.value
        val userMsg = ChatMessage(
            sender = "user",
            text = userText.trim(),
            provider = currentSettings.aiProvider
        )

        viewModelScope.launch {
            dao.insertChatMessage(userMsg)
            _isChatSending.value = true
            _chatErrorMessage.value = null

            val apiKey = currentSettings.getActiveApiKey()
            if (apiKey.isBlank()) {
                val providerName = when (currentSettings.aiProvider) {
                    "GEMINI" -> "Google Gemini"
                    "OPENAI" -> "OpenAI"
                    "OPENROUTER" -> "OpenRouter"
                    else -> currentSettings.aiProvider
                }
                val warningMsg = ChatMessage(
                    sender = "yarverse",
                    text = "⚠️ $providerName API key is required.\n\nPlease open Settings and enter your API key for $providerName to activate YaRVerse responses.\n\nTip: You can switch between Gemini, OpenAI, and OpenRouter in Settings at any time!",
                    provider = currentSettings.aiProvider,
                    isError = true
                )
                dao.insertChatMessage(warningMsg)
                _isChatSending.value = false
                return@launch
            }

            // Build contextual intelligence: user's note count, tasks summary, schedule overview
            val notesCount = rawActiveNotes.value.size
            val pendingTasks = tasks.value.filter { !it.isCompleted }
            val scheduleToday = scheduleItems.value

            val contextAugmentation = buildString {
                append("\n[Context: The user is using Yaseen suite. They currently have $notesCount notes, ${pendingTasks.size} pending tasks")
                if (pendingTasks.isNotEmpty()) {
                    append(" (Top tasks: ")
                    append(pendingTasks.take(3).joinToString { it.title })
                    append(")")
                }
                if (scheduleToday.isNotEmpty()) {
                    append(". Today's schedule events: ")
                    append(scheduleToday.take(3).joinToString { it.title })
                }
                append("]")
            }

            val finalSystemPrompt = currentSettings.systemPrompt + contextAugmentation

            val result = chatApiService.sendMessage(
                provider = currentSettings.aiProvider,
                apiKey = apiKey,
                model = currentSettings.getActiveModel(),
                systemPrompt = finalSystemPrompt,
                history = chatMessages.value,
                userMessage = userText.trim()
            )

            result.fold(
                onSuccess = { reply ->
                    val botMsg = ChatMessage(
                        sender = "yarverse",
                        text = reply,
                        provider = currentSettings.aiProvider
                    )
                    dao.insertChatMessage(botMsg)
                },
                onFailure = { error ->
                    _chatErrorMessage.value = error.localizedMessage
                    val errorMsg = ChatMessage(
                        sender = "yarverse",
                        text = "Error communicating with ${currentSettings.aiProvider}: ${error.localizedMessage ?: "Unknown error"}\n\nPlease verify your API key in Settings.",
                        provider = currentSettings.aiProvider,
                        isError = true
                    )
                    dao.insertChatMessage(errorMsg)
                }
            )

            _isChatSending.value = false
        }
    }

    fun clearChatHistory() {
        viewModelScope.launch {
            dao.clearChatMessages()
        }
    }

    fun saveChatAsNote(content: String, title: String = "Insight from YaRVerse") {
        viewModelScope.launch {
            val newNote = Note(
                title = title,
                content = content,
                description = "Saved from YaRVerse AI Chatbot",
                tags = "AI,YaRVerse",
                color = 0xFFE0F2FE
            )
            dao.insertNote(newNote)
        }
    }
}
