package com.example.ui.viewmodel

import android.app.Application
import android.content.Intent
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.AppSettings
import com.example.data.ChatApiService
import com.example.data.ChatMessage
import com.example.data.DuplicateGroup
import com.example.data.FileCategory
import com.example.data.FileClipboard
import com.example.data.FileManagerRepository
import com.example.data.FileSortOption
import com.example.data.FileSortOrder
import com.example.data.LocalFileItem
import com.example.data.SettingsManager
import com.example.data.StorageBreakdown
import com.example.data.StorageCleanupItem
import com.example.data.ViewMode
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.io.File
import java.util.Locale

class FilesViewModel(application: Application) : AndroidViewModel(application) {

    val repository = FileManagerRepository(application)
    private val settingsManager = SettingsManager(application)
    private val chatApiService = ChatApiService()

    private val _currentDirectory = MutableStateFlow<File>(repository.appFilesDir)
    val currentDirectory: StateFlow<File> = _currentDirectory.asStateFlow()

    private val _directoryHistory = MutableStateFlow<List<File>>(listOf(repository.appFilesDir))
    val directoryHistory: StateFlow<List<File>> = _directoryHistory.asStateFlow()

    private val _files = MutableStateFlow<List<LocalFileItem>>(emptyList())
    val files: StateFlow<List<LocalFileItem>> = _files.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    private val _successMessage = MutableStateFlow<String?>(null)
    val successMessage: StateFlow<String?> = _successMessage.asStateFlow()

    private val _selectedCategory = MutableStateFlow(FileCategory.ALL)
    val selectedCategory: StateFlow<FileCategory> = _selectedCategory.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _sortOption = MutableStateFlow(FileSortOption.NAME)
    val sortOption: StateFlow<FileSortOption> = _sortOption.asStateFlow()

    private val _sortOrder = MutableStateFlow(FileSortOrder.ASCENDING)
    val sortOrder: StateFlow<FileSortOrder> = _sortOrder.asStateFlow()

    private val _viewMode = MutableStateFlow(ViewMode.GRID)
    val viewMode: StateFlow<ViewMode> = _viewMode.asStateFlow()

    private val _showHiddenFiles = MutableStateFlow(false)
    val showHiddenFiles: StateFlow<Boolean> = _showHiddenFiles.asStateFlow()

    // Multi-selection state
    private val _isSelectionMode = MutableStateFlow(false)
    val isSelectionMode: StateFlow<Boolean> = _isSelectionMode.asStateFlow()

    private val _selectedItemIds = MutableStateFlow<Set<String>>(emptySet())
    val selectedItemIds: StateFlow<Set<String>> = _selectedItemIds.asStateFlow()

    // Clipboard for Cut / Copy / Paste
    private val _clipboard = MutableStateFlow<FileClipboard?>(null)
    val clipboard: StateFlow<FileClipboard?> = _clipboard.asStateFlow()

    // Active viewer / player state
    private val _activeViewerItem = MutableStateFlow<LocalFileItem?>(null)
    val activeViewerItem: StateFlow<LocalFileItem?> = _activeViewerItem.asStateFlow()

    private val _mediaPlaylist = MutableStateFlow<List<LocalFileItem>>(emptyList())
    val mediaPlaylist: StateFlow<List<LocalFileItem>> = _mediaPlaylist.asStateFlow()

    private val _mediaIndex = MutableStateFlow(0)
    val mediaIndex: StateFlow<Int> = _mediaIndex.asStateFlow()

    // Text Editor state
    private val _editingFile = MutableStateFlow<LocalFileItem?>(null)
    val editingFile: StateFlow<LocalFileItem?> = _editingFile.asStateFlow()

    private val _editingText = MutableStateFlow("")
    val editingText: StateFlow<String> = _editingText.asStateFlow()

    // Storage Analyzer state
    private val _storageBreakdown = MutableStateFlow(StorageBreakdown())
    val storageBreakdown: StateFlow<StorageBreakdown> = _storageBreakdown.asStateFlow()

    private val _duplicateGroups = MutableStateFlow<List<DuplicateGroup>>(emptyList())
    val duplicateGroups: StateFlow<List<DuplicateGroup>> = _duplicateGroups.asStateFlow()

    private val _cleanupSuggestions = MutableStateFlow<List<StorageCleanupItem>>(emptyList())
    val cleanupSuggestions: StateFlow<List<StorageCleanupItem>> = _cleanupSuggestions.asStateFlow()

    private val _largestFiles = MutableStateFlow<List<LocalFileItem>>(emptyList())
    val largestFiles: StateFlow<List<LocalFileItem>> = _largestFiles.asStateFlow()

    private val _isAnalyzing = MutableStateFlow(false)
    val isAnalyzing: StateFlow<Boolean> = _isAnalyzing.asStateFlow()

    // Private Safe / Vault PIN unlock state
    private val _isVaultUnlocked = MutableStateFlow(false)
    val isVaultUnlocked: StateFlow<Boolean> = _isVaultUnlocked.asStateFlow()

    // AI Operation state
    private val _isAiLoading = MutableStateFlow(false)
    val isAiLoading: StateFlow<Boolean> = _isAiLoading.asStateFlow()

    private val _aiResult = MutableStateFlow<String?>(null)
    val aiResult: StateFlow<String?> = _aiResult.asStateFlow()

    init {
        refreshCurrentDirectory()
        refreshStorageAnalytics()
    }

    fun clearMessages() {
        _errorMessage.value = null
        _successMessage.value = null
    }

    fun selectCategory(category: FileCategory) {
        _selectedCategory.value = category
        clearSelection()
        if (category == FileCategory.STORAGE_ANALYZER) {
            refreshStorageAnalytics()
        } else if (category == FileCategory.ALL) {
            refreshCurrentDirectory()
        } else {
            loadCategoryFiles(category)
        }
    }

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun setSortOption(option: FileSortOption) {
        _sortOption.value = option
        sortCurrentFiles()
    }

    fun toggleSortOrder() {
        _sortOrder.update {
            if (it == FileSortOrder.ASCENDING) FileSortOrder.DESCENDING else FileSortOrder.ASCENDING
        }
        sortCurrentFiles()
    }

    fun toggleViewMode() {
        _viewMode.update { if (it == ViewMode.GRID) ViewMode.LIST else ViewMode.GRID }
    }

    fun toggleShowHidden() {
        _showHiddenFiles.update { !it }
        refreshCurrentDirectory()
    }

    fun refreshCurrentDirectory() {
        val current = _currentDirectory.value
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val list = repository.listDirectory(
                    directory = current,
                    showHidden = _showHiddenFiles.value,
                    categoryFilter = _selectedCategory.value
                )
                _files.value = applySorting(list)
            } catch (e: Exception) {
                _errorMessage.value = "Failed to list files: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    private fun loadCategoryFiles(category: FileCategory) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val list = repository.listCategoryFiles(category)
                _files.value = applySorting(list)
            } catch (e: Exception) {
                _errorMessage.value = "Failed to load category: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun navigateToDirectory(directory: File) {
        if (!directory.exists() || !directory.isDirectory) return
        _currentDirectory.value = directory
        _directoryHistory.update { it + directory }
        _selectedCategory.value = FileCategory.ALL
        clearSelection()
        refreshCurrentDirectory()
    }

    fun navigateUp(): Boolean {
        val history = _directoryHistory.value
        if (history.size > 1) {
            val updated = history.dropLast(1)
            _directoryHistory.value = updated
            _currentDirectory.value = updated.last()
            clearSelection()
            refreshCurrentDirectory()
            return true
        }
        return false
    }

    fun navigateToBreadcrumb(targetIndex: Int) {
        val history = _directoryHistory.value
        if (targetIndex in history.indices) {
            val updated = history.take(targetIndex + 1)
            _directoryHistory.value = updated
            _currentDirectory.value = updated.last()
            clearSelection()
            refreshCurrentDirectory()
        }
    }

    private fun sortCurrentFiles() {
        _files.value = applySorting(_files.value)
    }

    private fun applySorting(list: List<LocalFileItem>): List<LocalFileItem> {
        val comparator = when (_sortOption.value) {
            FileSortOption.NAME -> compareBy<LocalFileItem> { it.name.lowercase(Locale.ROOT) }
            FileSortOption.DATE -> compareBy<LocalFileItem> { it.lastModified }
            FileSortOption.SIZE -> compareBy<LocalFileItem> { it.size }
            FileSortOption.TYPE -> compareBy<LocalFileItem> { it.extension }
        }

        val sorted = if (_sortOrder.value == FileSortOrder.ASCENDING) {
            list.sortedWith(comparator)
        } else {
            list.sortedWith(comparator.reversed())
        }

        // Directories always stay at top
        val dirs = sorted.filter { it.isDirectory }
        val files = sorted.filter { !it.isDirectory }
        return dirs + files
    }

    // --- Multi-Selection ---

    fun toggleSelectionMode() {
        _isSelectionMode.update { !it }
        if (!_isSelectionMode.value) {
            clearSelection()
        }
    }

    fun toggleSelectItem(id: String) {
        _selectedItemIds.update { current ->
            if (current.contains(id)) current - id else current + id
        }
        if (_selectedItemIds.value.isNotEmpty()) {
            _isSelectionMode.value = true
        }
    }

    fun selectAll() {
        _selectedItemIds.value = _files.value.map { it.id }.toSet()
        _isSelectionMode.value = true
    }

    fun clearSelection() {
        _selectedItemIds.value = emptySet()
        _isSelectionMode.value = false
    }

    fun getSelectedFiles(): List<LocalFileItem> {
        val selectedIds = _selectedItemIds.value
        return _files.value.filter { selectedIds.contains(it.id) }
    }

    // --- CRUD Operations ---

    fun createFolder(name: String) {
        viewModelScope.launch {
            val result = repository.createFolder(_currentDirectory.value, name)
            if (result.isSuccess) {
                _successMessage.value = "Created folder '$name'"
                refreshCurrentDirectory()
            } else {
                _errorMessage.value = result.exceptionOrNull()?.message ?: "Failed to create folder"
            }
        }
    }

    fun createTextFile(name: String, content: String = "") {
        viewModelScope.launch {
            val result = repository.createTextFile(_currentDirectory.value, name, content)
            if (result.isSuccess) {
                _successMessage.value = "Created file '${result.getOrNull()?.name}'"
                refreshCurrentDirectory()
            } else {
                _errorMessage.value = result.exceptionOrNull()?.message ?: "Failed to create file"
            }
        }
    }

    fun renameFile(item: LocalFileItem, newName: String) {
        viewModelScope.launch {
            val result = repository.rename(item.file, newName)
            if (result.isSuccess) {
                _successMessage.value = "Renamed to '$newName'"
                refreshCurrentDirectory()
            } else {
                _errorMessage.value = result.exceptionOrNull()?.message ?: "Failed to rename"
            }
        }
    }

    fun deleteSelectedFiles() {
        val selected = getSelectedFiles()
        if (selected.isEmpty()) return
        viewModelScope.launch {
            var successCount = 0
            for (item in selected) {
                if (repository.delete(item.file).isSuccess) {
                    successCount++
                }
            }
            _successMessage.value = "Deleted $successCount items"
            clearSelection()
            refreshCurrentDirectory()
            refreshStorageAnalytics()
        }
    }

    fun deleteSingleFile(item: LocalFileItem) {
        viewModelScope.launch {
            val result = repository.delete(item.file)
            if (result.isSuccess) {
                _successMessage.value = "Deleted '${item.name}'"
                refreshCurrentDirectory()
                refreshStorageAnalytics()
            } else {
                _errorMessage.value = result.exceptionOrNull()?.message ?: "Failed to delete"
            }
        }
    }

    fun duplicateFile(item: LocalFileItem) {
        viewModelScope.launch {
            val result = repository.duplicate(item.file)
            if (result.isSuccess) {
                _successMessage.value = "Created duplicate: ${result.getOrNull()?.name}"
                refreshCurrentDirectory()
            } else {
                _errorMessage.value = result.exceptionOrNull()?.message ?: "Failed to duplicate"
            }
        }
    }

    fun zipSelectedFiles(zipName: String) {
        val selected = getSelectedFiles()
        if (selected.isEmpty()) return
        viewModelScope.launch {
            var safeZipName = zipName.trim()
            if (!safeZipName.endsWith(".zip", ignoreCase = true)) {
                safeZipName += ".zip"
            }
            val targetZip = File(_currentDirectory.value, safeZipName)
            val result = repository.zipFiles(selected.map { it.file }, targetZip)
            if (result.isSuccess) {
                _successMessage.value = "Created archive '$safeZipName'"
                clearSelection()
                refreshCurrentDirectory()
            } else {
                _errorMessage.value = result.exceptionOrNull()?.message ?: "Failed to create ZIP"
            }
        }
    }

    fun unzipArchive(item: LocalFileItem) {
        viewModelScope.launch {
            _isLoading.value = true
            val result = repository.unzipArchive(item.file, _currentDirectory.value)
            _isLoading.value = false
            if (result.isSuccess) {
                _successMessage.value = "Extracted archive to '${result.getOrNull()?.name}'"
                refreshCurrentDirectory()
            } else {
                _errorMessage.value = result.exceptionOrNull()?.message ?: "Failed to extract ZIP"
            }
        }
    }

    fun toggleFavorite(item: LocalFileItem) {
        val isNowFav = repository.toggleFavorite(item.path)
        _files.update { current ->
            current.map { if (it.id == item.id) it.copy(isFavorite = isNowFav) else it }
        }
        _successMessage.value = if (isNowFav) "Added to Favorites" else "Removed from Favorites"
    }

    // --- Clipboard Cut / Copy / Paste ---

    fun cutSelection() {
        val selected = getSelectedFiles()
        if (selected.isNotEmpty()) {
            _clipboard.value = FileClipboard(items = selected, isCut = true)
            clearSelection()
            _successMessage.value = "${selected.size} items ready to move"
        }
    }

    fun copySelection() {
        val selected = getSelectedFiles()
        if (selected.isNotEmpty()) {
            _clipboard.value = FileClipboard(items = selected, isCut = false)
            clearSelection()
            _successMessage.value = "${selected.size} items copied to clipboard"
        }
    }

    fun pasteClipboard() {
        val clip = _clipboard.value ?: return
        val target = _currentDirectory.value
        viewModelScope.launch {
            _isLoading.value = true
            var processed = 0
            for (item in clip.items) {
                val res = if (clip.isCut) {
                    repository.move(item.file, target)
                } else {
                    repository.copy(item.file, target)
                }
                if (res.isSuccess) processed++
            }
            _isLoading.value = false
            _successMessage.value = "Pasted $processed items"
            if (clip.isCut) {
                _clipboard.value = null
            }
            refreshCurrentDirectory()
            refreshStorageAnalytics()
        }
    }

    fun clearClipboard() {
        _clipboard.value = null
    }

    // --- Media Viewer & Audio Player State ---

    fun openViewer(item: LocalFileItem) {
        repository.recordRecent(item.path)
        if (item.category == FileCategory.IMAGES) {
            val imageList = _files.value.filter { it.category == FileCategory.IMAGES }
            val index = imageList.indexOfFirst { it.id == item.id }.coerceAtLeast(0)
            _mediaPlaylist.value = imageList
            _mediaIndex.value = index
            _activeViewerItem.value = item
        } else if (item.category == FileCategory.VIDEOS) {
            val videoList = _files.value.filter { it.category == FileCategory.VIDEOS }
            val index = videoList.indexOfFirst { it.id == item.id }.coerceAtLeast(0)
            _mediaPlaylist.value = videoList
            _mediaIndex.value = index
            _activeViewerItem.value = item
        } else if (item.category == FileCategory.AUDIO) {
            val audioList = _files.value.filter { it.category == FileCategory.AUDIO }
            val index = audioList.indexOfFirst { it.id == item.id }.coerceAtLeast(0)
            _mediaPlaylist.value = audioList
            _mediaIndex.value = index
            _activeViewerItem.value = item
        } else if (item.isTextEditable) {
            openTextEditor(item)
        } else {
            // External open with
            _activeViewerItem.value = item
        }
    }

    fun closeViewer() {
        _activeViewerItem.value = null
    }

    fun nextMedia() {
        val list = _mediaPlaylist.value
        if (list.isNotEmpty()) {
            val nextIdx = (_mediaIndex.value + 1) % list.size
            _mediaIndex.value = nextIdx
            _activeViewerItem.value = list[nextIdx]
        }
    }

    fun previousMedia() {
        val list = _mediaPlaylist.value
        if (list.isNotEmpty()) {
            val prevIdx = if (_mediaIndex.value - 1 < 0) list.size - 1 else _mediaIndex.value - 1
            _mediaIndex.value = prevIdx
            _activeViewerItem.value = list[prevIdx]
        }
    }

    // --- Text Editor ---

    fun openTextEditor(item: LocalFileItem) {
        viewModelScope.launch {
            val result = repository.readText(item.file)
            if (result.isSuccess) {
                _editingFile.value = item
                _editingText.value = result.getOrNull() ?: ""
                _activeViewerItem.value = item
            } else {
                _errorMessage.value = "Cannot read text file: ${result.exceptionOrNull()?.message}"
            }
        }
    }

    fun saveEditingFile(content: String) {
        val fileItem = _editingFile.value ?: return
        viewModelScope.launch {
            val result = repository.writeText(fileItem.file, content)
            if (result.isSuccess) {
                _editingText.value = content
                _successMessage.value = "Saved '${fileItem.name}'"
                refreshCurrentDirectory()
            } else {
                _errorMessage.value = "Failed to save: ${result.exceptionOrNull()?.message}"
            }
        }
    }

    fun closeTextEditor() {
        _editingFile.value = null
        _editingText.value = ""
        if (_activeViewerItem.value?.isTextEditable == true) {
            _activeViewerItem.value = null
        }
    }

    // --- Storage Analytics ---

    fun refreshStorageAnalytics() {
        viewModelScope.launch {
            _isAnalyzing.value = true
            try {
                _storageBreakdown.value = repository.getStorageBreakdown()
                _duplicateGroups.value = repository.findDuplicates()
                _cleanupSuggestions.value = repository.getCleanupSuggestions()
                _largestFiles.value = repository.getLargestFiles(25)
            } catch (e: Exception) {
                _errorMessage.value = "Analysis failed: ${e.message}"
            } finally {
                _isAnalyzing.value = false
            }
        }
    }

    fun deleteDuplicateFiles(filesToDelete: List<LocalFileItem>) {
        viewModelScope.launch {
            var count = 0
            for (f in filesToDelete) {
                if (repository.delete(f.file).isSuccess) count++
            }
            _successMessage.value = "Cleaned up $count duplicate files"
            refreshStorageAnalytics()
            refreshCurrentDirectory()
        }
    }

    // --- Private Vault ---

    fun unlockVault(pin: String): Boolean {
        val settings = settingsManager.loadSettings()
        val correctPin = settings.securityPinCode
        return if (correctPin.isNotBlank() && pin == correctPin) {
            _isVaultUnlocked.value = true
            _selectedCategory.value = FileCategory.PRIVATE_VAULT
            loadCategoryFiles(FileCategory.PRIVATE_VAULT)
            true
        } else if (!settings.securityLockEnabled) {
            // If lock is not enabled, unlock directly
            _isVaultUnlocked.value = true
            _selectedCategory.value = FileCategory.PRIVATE_VAULT
            loadCategoryFiles(FileCategory.PRIVATE_VAULT)
            true
        } else {
            _errorMessage.value = "Incorrect PIN code"
            false
        }
    }

    fun lockVault() {
        _isVaultUnlocked.value = false
        if (_selectedCategory.value == FileCategory.PRIVATE_VAULT) {
            selectCategory(FileCategory.ALL)
        }
    }

    fun moveToPrivateVault(item: LocalFileItem) {
        viewModelScope.launch {
            val result = repository.moveToPrivateVault(item.file)
            if (result.isSuccess) {
                _successMessage.value = "Moved '${item.name}' to Private Safe"
                refreshCurrentDirectory()
            } else {
                _errorMessage.value = result.exceptionOrNull()?.message ?: "Failed to secure file"
            }
        }
    }

    fun restoreFromPrivateVault(item: LocalFileItem) {
        viewModelScope.launch {
            val result = repository.restoreFromPrivateVault(item.file)
            if (result.isSuccess) {
                _successMessage.value = "Restored '${item.name}' to Documents"
                loadCategoryFiles(FileCategory.PRIVATE_VAULT)
            } else {
                _errorMessage.value = result.exceptionOrNull()?.message ?: "Failed to restore file"
            }
        }
    }

    // --- YaRVerse AI File Intelligence ---

    fun runAiAction(item: LocalFileItem, actionType: String, onComplete: (String) -> Unit) {
        viewModelScope.launch {
            _isAiLoading.value = true
            val textContent = try {
                item.file.readText().take(4000)
            } catch (_: Exception) {
                "Filename: ${item.name}, Path: ${item.path}, Size: ${item.formattedSize}"
            }

            val prompt = when (actionType) {
                "SUMMARIZE" -> "Summarize the key information and purpose of this document clearly and concisely:\n\n$textContent"
                "EXPLAIN" -> "Explain the main ideas, arguments, or code logic in this file in simple terms:\n\n$textContent"
                "EXTRACT_TASKS" -> "Extract all action items, to-do tasks, and follow-ups from this document as a markdown checklist (- [ ] task):\n\n$textContent"
                "GENERATE_NOTE" -> "Format this document's core content into a clean, structured study/reference note with title, bullet points, and summary:\n\n$textContent"
                "SUGGEST_NAME" -> "Suggest a concise, descriptive, clean filename (with extension .${item.extension.ifEmpty { "txt" }}) for this file:\n\n$textContent"
                else -> "Analyze this document:\n\n$textContent"
            }

            val settings = settingsManager.loadSettings()
            val result = chatApiService.sendMessage(
                provider = settings.aiProvider,
                apiKey = settings.getActiveApiKey(),
                model = settings.getActiveModel(),
                systemPrompt = settings.systemPrompt,
                history = emptyList(),
                userMessage = prompt
            )

            _isAiLoading.value = false
            if (result.isSuccess) {
                val answer = result.getOrNull() ?: ""
                _aiResult.value = answer
                onComplete(answer)
            } else {
                val err = result.exceptionOrNull()?.message ?: "AI request failed"
                _errorMessage.value = err
                onComplete("Error: $err")
            }
        }
    }

    fun clearAiResult() {
        _aiResult.value = null
    }

    // --- Share / Open with Intent Helpers ---

    fun shareFile(item: LocalFileItem) {
        try {
            val intent = repository.createShareIntent(item.file)
            val chooser = Intent.createChooser(intent, "Share ${item.name}").apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            getApplication<Application>().startActivity(chooser)
        } catch (e: Exception) {
            _errorMessage.value = "Cannot share file: ${e.message}"
        }
    }

    fun openWith(item: LocalFileItem) {
        try {
            val intent = repository.createOpenWithIntent(item.file)
            val chooser = Intent.createChooser(intent, "Open with").apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            getApplication<Application>().startActivity(chooser)
        } catch (e: Exception) {
            _errorMessage.value = "No app found to open this file: ${e.message}"
        }
    }
}
