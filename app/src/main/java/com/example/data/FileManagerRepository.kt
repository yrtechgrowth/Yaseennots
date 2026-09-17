package com.example.data

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.os.StatFs
import android.webkit.MimeTypeMap
import androidx.core.content.FileProvider
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.security.MessageDigest
import java.util.Locale
import java.util.zip.ZipEntry
import java.util.zip.ZipInputStream
import java.util.zip.ZipOutputStream

class FileManagerRepository(private val context: Context) {

    private val prefs: SharedPreferences =
        context.getSharedPreferences("yaseen_file_preferences", Context.MODE_PRIVATE)

    // Base App Managed Storage Directory
    val appFilesDir: File by lazy {
        val dir = File(context.filesDir, "yaseen_files")
        if (!dir.exists()) dir.mkdirs()
        ensureDefaultFolders(dir)
        dir
    }

    val privateVaultDir: File by lazy {
        val dir = File(context.filesDir, "yaseen_private_vault")
        if (!dir.exists()) dir.mkdirs()
        dir
    }

    private fun ensureDefaultFolders(base: File) {
        val defaultFolders = listOf(
            "Documents",
            "Downloads",
            "Pictures",
            "Videos",
            "Audio",
            "Archives",
            "Notes Attachments",
            "Tasks Attachments",
            "Schedule Attachments"
        )
        for (name in defaultFolders) {
            val folder = File(base, name)
            if (!folder.exists()) folder.mkdirs()
        }

        // Add a sample welcome guide document if empty
        val welcomeDoc = File(File(base, "Documents"), "Welcome_to_Yaseen_Files.md")
        if (!welcomeDoc.exists()) {
            welcomeDoc.writeText(
                """# Welcome to Yaseen Files
                
Yaseen Files is a powerful, local-first file manager fully integrated into your Yaseen productivity suite.

### Key Capabilities:
- **Local Storage Management**: Browse and manage your local documents, pictures, videos, audio, and archives.
- **Productivity Ecosystem**: Directly attach files to Yaseen Notes, Tasks, and Schedule events.
- **Built-in Media Players**: Preview high-resolution photos, stream video clips with timeline controls, and listen to audio recordings.
- **Lightweight Text Editor**: Edit `.txt`, `.md`, `.json`, `.csv`, `.log`, and source code files directly on your device.
- **Storage Analyzer & Cleaner**: Visualize your device storage footprint and safely find duplicate files.
- **YaRVerse AI Integration**: Generate instant document summaries, extract actionable task checklists, and draft notes from any text file.
- **Private Safe**: Securely store confidential documents protected by your app PIN.
""".trimIndent()
            )
        }
    }

    // Accessible root directories
    fun getRootLocations(): List<LocalFileItem> {
        val list = mutableListOf<LocalFileItem>()

        // 1. App-Managed Storage
        list.add(fileToLocalFileItem(appFilesDir, customName = "Yaseen Storage"))

        // 2. Device Downloads if accessible
        try {
            val downloadsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
            if (downloadsDir != null && downloadsDir.exists() && downloadsDir.canRead()) {
                list.add(fileToLocalFileItem(downloadsDir, customName = "Downloads"))
            }
        } catch (_: Exception) {}

        // 3. Device Documents if accessible
        try {
            val docsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS)
            if (docsDir != null && docsDir.exists() && docsDir.canRead()) {
                list.add(fileToLocalFileItem(docsDir, customName = "Documents"))
            }
        } catch (_: Exception) {}

        // 4. Device Pictures if accessible
        try {
            val picsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES)
            if (picsDir != null && picsDir.exists() && picsDir.canRead()) {
                list.add(fileToLocalFileItem(picsDir, customName = "Pictures"))
            }
        } catch (_: Exception) {}

        // 5. Device Movies / Videos if accessible
        try {
            val moviesDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MOVIES)
            if (moviesDir != null && moviesDir.exists() && moviesDir.canRead()) {
                list.add(fileToLocalFileItem(moviesDir, customName = "Videos"))
            }
        } catch (_: Exception) {}

        // 6. Device Music if accessible
        try {
            val musicDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MUSIC)
            if (musicDir != null && musicDir.exists() && musicDir.canRead()) {
                list.add(fileToLocalFileItem(musicDir, customName = "Music"))
            }
        } catch (_: Exception) {}

        // 7. External App Files if present
        try {
            val extApp = context.getExternalFilesDir(null)
            if (extApp != null && extApp.exists() && extApp.canRead() && extApp != appFilesDir) {
                list.add(fileToLocalFileItem(extApp, customName = "External App Files"))
            }
        } catch (_: Exception) {}

        return list
    }

    suspend fun listDirectory(
        directory: File,
        showHidden: Boolean = false,
        categoryFilter: FileCategory = FileCategory.ALL
    ): List<LocalFileItem> = withContext(Dispatchers.IO) {
        if (!directory.exists() || !directory.isDirectory) return@withContext emptyList()

        val files = directory.listFiles() ?: return@withContext emptyList()
        val favorites = getFavoritesSet()
        val result = mutableListOf<LocalFileItem>()

        for (file in files) {
            if (!showHidden && file.name.startsWith(".")) continue

            val item = fileToLocalFileItem(file, isFavorite = favorites.contains(file.absolutePath))
            if (categoryFilter == FileCategory.ALL || item.isDirectory || item.category == categoryFilter) {
                result.add(item)
            }
        }
        result
    }

    @Volatile
    private var cachedIndexedFiles: List<LocalFileItem>? = null
    @Volatile
    private var lastIndexTimestamp: Long = 0L
    private val CACHE_TTL_MS = 60_000L

    fun invalidateIndexCache() {
        cachedIndexedFiles = null
        lastIndexTimestamp = 0L
    }

    suspend fun getIndexedFiles(forceRefresh: Boolean = false): List<LocalFileItem> = withContext(Dispatchers.IO) {
        val cached = cachedIndexedFiles
        val now = System.currentTimeMillis()
        if (!forceRefresh && cached != null && (now - lastIndexTimestamp) < CACHE_TTL_MS) {
            return@withContext cached
        }
        val scanned = scanAllAccessibleFiles()
        cachedIndexedFiles = scanned
        lastIndexTimestamp = now
        scanned
    }

    suspend fun listCategoryFiles(category: FileCategory, forceRefresh: Boolean = false): List<LocalFileItem> = withContext(Dispatchers.IO) {
        val allScanned = getIndexedFiles(forceRefresh)
        val favorites = getFavoritesSet()

        when (category) {
            FileCategory.ALL -> allScanned
            FileCategory.FAVORITES -> allScanned.filter { favorites.contains(it.path) }
            FileCategory.RECENTS -> {
                val recentPaths = getRecentPaths()
                val map = allScanned.associateBy { it.path }
                recentPaths.mapNotNull { map[it] }.ifEmpty {
                    allScanned.sortedByDescending { it.lastModified }.take(30)
                }
            }
            FileCategory.PRIVATE_VAULT -> {
                listDirectory(privateVaultDir, showHidden = true).map { it.copy(isPrivate = true) }
            }
            FileCategory.ATTACHMENTS -> {
                allScanned.filter {
                    it.path.contains("Notes Attachments") ||
                            it.path.contains("Tasks Attachments") ||
                            it.path.contains("Schedule Attachments")
                }
            }
            else -> allScanned.filter { it.category == category }
        }
    }

    private suspend fun scanAllAccessibleFiles(): List<LocalFileItem> = withContext(Dispatchers.IO) {
        val list = mutableListOf<LocalFileItem>()
        val favorites = getFavoritesSet()

        fun walk(dir: File, depth: Int = 0) {
            if (depth > 6) return
            val children = dir.listFiles() ?: return
            for (f in children) {
                if (f.name.startsWith(".") || f.name == "yaseen_private_vault") continue
                if (f.isDirectory) {
                    walk(f, depth + 1)
                } else {
                    list.add(fileToLocalFileItem(f, isFavorite = favorites.contains(f.absolutePath)))
                }
            }
        }

        walk(appFilesDir)

        val standardDirs = listOf(
            Environment.DIRECTORY_DOWNLOADS,
            Environment.DIRECTORY_DOCUMENTS,
            Environment.DIRECTORY_PICTURES,
            Environment.DIRECTORY_MOVIES,
            Environment.DIRECTORY_MUSIC
        )
        for (std in standardDirs) {
            try {
                val d = Environment.getExternalStoragePublicDirectory(std)
                if (d != null && d.exists() && d.canRead()) {
                    walk(d, 0)
                }
            } catch (_: Exception) {}
        }

        list.distinctBy { it.path }
    }

    fun fileToLocalFileItem(
        file: File,
        customName: String? = null,
        isFavorite: Boolean = false,
        isPrivate: Boolean = false
    ): LocalFileItem {
        val isDir = file.isDirectory
        val ext = if (isDir) "" else file.extension.lowercase(Locale.ROOT)
        val category = determineCategory(ext, isDir)
        val mime = getMimeTypeFromExtension(ext)
        val count = if (isDir) (file.listFiles()?.size ?: 0) else 0

        return LocalFileItem(
            id = file.absolutePath,
            file = file,
            name = customName ?: file.name,
            path = file.absolutePath,
            size = if (isDir) 0L else file.length(),
            lastModified = file.lastModified(),
            isDirectory = isDir,
            extension = ext,
            category = category,
            mimeType = mime,
            childCount = count,
            isFavorite = isFavorite,
            isPrivate = isPrivate
        )
    }

    private fun determineCategory(ext: String, isDirectory: Boolean): FileCategory {
        if (isDirectory) return FileCategory.ALL
        return when (ext) {
            "jpg", "jpeg", "png", "webp", "gif", "bmp", "heic", "heif", "svg" -> FileCategory.IMAGES
            "mp4", "mkv", "mov", "avi", "webm", "3gp", "flv", "m4v" -> FileCategory.VIDEOS
            "mp3", "wav", "m4a", "aac", "ogg", "flac", "opus", "wma" -> FileCategory.AUDIO
            "pdf", "txt", "doc", "docx", "xls", "xlsx", "ppt", "pptx", "csv", "md", "json", "xml", "log", "html", "kt", "java", "py" -> FileCategory.DOCUMENTS
            "zip", "rar", "7z", "tar", "gz", "bz2" -> FileCategory.ARCHIVES
            else -> FileCategory.ALL
        }
    }

    fun getMimeTypeFromExtension(extension: String): String {
        if (extension.isBlank()) return "*/*"
        return MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension) ?: when (extension) {
            "md" -> "text/markdown"
            "json" -> "application/json"
            "log" -> "text/plain"
            "csv" -> "text/csv"
            "kt" -> "text/x-kotlin"
            else -> "*/*"
        }
    }

    // --- CRUD File Operations ---

    suspend fun createFolder(parentDir: File, folderName: String): Result<File> = withContext(Dispatchers.IO) {
        try {
            val safeName = sanitizeFilename(folderName)
            val newFolder = File(parentDir, safeName)
            if (newFolder.exists()) {
                return@withContext Result.failure(Exception("Folder '$safeName' already exists"))
            }
            if (newFolder.mkdirs()) {
                invalidateIndexCache()
                Result.success(newFolder)
            } else {
                Result.failure(Exception("Failed to create folder '$safeName'"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun createTextFile(parentDir: File, fileName: String, initialContent: String = ""): Result<File> = withContext(Dispatchers.IO) {
        try {
            var safeName = sanitizeFilename(fileName)
            if (!safeName.contains(".")) {
                safeName += ".txt"
            }
            val newFile = File(parentDir, safeName)
            if (newFile.exists()) {
                return@withContext Result.failure(Exception("File '$safeName' already exists"))
            }
            newFile.writeText(initialContent)
            recordRecent(newFile.absolutePath)
            invalidateIndexCache()
            Result.success(newFile)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun readText(file: File): Result<String> = withContext(Dispatchers.IO) {
        try {
            recordRecent(file.absolutePath)
            Result.success(file.readText())
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun writeText(file: File, content: String): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            file.writeText(content)
            recordRecent(file.absolutePath)
            invalidateIndexCache()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun rename(file: File, newName: String): Result<File> = withContext(Dispatchers.IO) {
        try {
            val safeName = sanitizeFilename(newName)
            val target = File(file.parentFile, safeName)
            if (target.exists()) {
                return@withContext Result.failure(Exception("A file or folder named '$safeName' already exists"))
            }
            if (file.renameTo(target)) {
                // Update favorite if present
                if (isFavorite(file.absolutePath)) {
                    removeFavorite(file.absolutePath)
                    addFavorite(target.absolutePath)
                }
                invalidateIndexCache()
                Result.success(target)
            } else {
                Result.failure(Exception("Could not rename file"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun delete(file: File): Result<Boolean> = withContext(Dispatchers.IO) {
        try {
            removeFavorite(file.absolutePath)
            val success = if (file.isDirectory) file.deleteRecursively() else file.delete()
            if (success) {
                invalidateIndexCache()
                Result.success(true)
            } else {
                Result.failure(Exception("Failed to delete ${file.name}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun copy(source: File, targetDir: File): Result<File> = withContext(Dispatchers.IO) {
        try {
            val destination = getUniqueFile(targetDir, source.name)
            if (source.isDirectory) {
                source.copyRecursively(destination, overwrite = false)
            } else {
                source.copyTo(destination, overwrite = false)
            }
            invalidateIndexCache()
            Result.success(destination)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun move(source: File, targetDir: File): Result<File> = withContext(Dispatchers.IO) {
        try {
            val destination = getUniqueFile(targetDir, source.name)
            val moved = source.renameTo(destination)
            if (moved) {
                if (isFavorite(source.absolutePath)) {
                    removeFavorite(source.absolutePath)
                    addFavorite(destination.absolutePath)
                }
                invalidateIndexCache()
                Result.success(destination)
            } else {
                // Fallback copy + delete
                val copyResult = copy(source, targetDir)
                if (copyResult.isSuccess) {
                    source.deleteRecursively()
                    invalidateIndexCache()
                    Result.success(copyResult.getOrThrow())
                } else {
                    Result.failure(Exception("Could not move file"))
                }
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun duplicate(file: File): Result<File> = withContext(Dispatchers.IO) {
        try {
            val parent = file.parentFile ?: return@withContext Result.failure(Exception("Invalid directory"))
            val baseName = file.nameWithoutExtension
            val ext = if (file.extension.isNotEmpty()) ".${file.extension}" else ""
            val newName = "${baseName}_copy$ext"
            val target = getUniqueFile(parent, newName)
            if (file.isDirectory) {
                file.copyRecursively(target)
            } else {
                file.copyTo(target)
            }
            Result.success(target)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun zipFiles(sources: List<File>, zipFile: File): Result<File> = withContext(Dispatchers.IO) {
        try {
            ZipOutputStream(FileOutputStream(zipFile)).use { zos ->
                for (source in sources) {
                    addToZip(source, "", zos)
                }
            }
            Result.success(zipFile)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private fun addToZip(file: File, parentPath: String, zos: ZipOutputStream) {
        val entryName = if (parentPath.isEmpty()) file.name else "$parentPath/${file.name}"
        if (file.isDirectory) {
            val children = file.listFiles() ?: return
            for (child in children) {
                addToZip(child, entryName, zos)
            }
        } else {
            FileInputStream(file).use { fis ->
                zos.putNextEntry(ZipEntry(entryName))
                fis.copyTo(zos)
                zos.closeEntry()
            }
        }
    }

    suspend fun unzipArchive(zipFile: File, targetDir: File): Result<File> = withContext(Dispatchers.IO) {
        try {
            val folderName = zipFile.nameWithoutExtension
            val outputFolder = getUniqueFile(targetDir, folderName)
            outputFolder.mkdirs()

            ZipInputStream(FileInputStream(zipFile)).use { zis ->
                var entry = zis.nextEntry
                while (entry != null) {
                    val destFile = File(outputFolder, entry.name)
                    // Security: prevent zip-slip vulnerability
                    val canonicalDest = destFile.canonicalPath
                    if (!canonicalDest.startsWith(outputFolder.canonicalPath)) {
                        throw SecurityException("Zip entry is attempting to traverse outside target directory")
                    }

                    if (entry.isDirectory) {
                        destFile.mkdirs()
                    } else {
                        destFile.parentFile?.mkdirs()
                        FileOutputStream(destFile).use { fos ->
                            zis.copyTo(fos)
                        }
                    }
                    zis.closeEntry()
                    entry = zis.nextEntry
                }
            }
            Result.success(outputFolder)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // --- Private Safe / Vault ---

    suspend fun moveToPrivateVault(file: File): Result<File> = withContext(Dispatchers.IO) {
        try {
            val dest = getUniqueFile(privateVaultDir, file.name)
            val success = file.renameTo(dest)
            if (success) {
                removeFavorite(file.absolutePath)
                Result.success(dest)
            } else {
                val copied = file.copyTo(dest)
                file.delete()
                Result.success(copied)
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun restoreFromPrivateVault(file: File, targetDir: File = File(appFilesDir, "Documents")): Result<File> = withContext(Dispatchers.IO) {
        try {
            val dest = getUniqueFile(targetDir, file.name)
            val success = file.renameTo(dest)
            if (success) {
                Result.success(dest)
            } else {
                val copied = file.copyTo(dest)
                file.delete()
                Result.success(copied)
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // --- Storage Metrics & Duplicate Analyzer ---

    suspend fun getStorageBreakdown(): StorageBreakdown = withContext(Dispatchers.IO) {
        var total = 0L
        var free = 0L
        try {
            val stat = StatFs(Environment.getDataDirectory().path)
            total = stat.blockSizeLong * stat.blockCountLong
            free = stat.blockSizeLong * stat.availableBlocksLong
        } catch (_: Exception) {
            total = 64L * 1024 * 1024 * 1024
            free = 32L * 1024 * 1024 * 1024
        }
        val used = (total - free).coerceAtLeast(0L)

        val scanned = scanAllAccessibleFiles()
        var imgBytes = 0L
        var vidBytes = 0L
        var audBytes = 0L
        var docBytes = 0L
        var arcBytes = 0L
        var othBytes = 0L

        var imgCount = 0
        var vidCount = 0
        var audCount = 0
        var docCount = 0
        var arcCount = 0

        for (item in scanned) {
            when (item.category) {
                FileCategory.IMAGES -> { imgBytes += item.size; imgCount++ }
                FileCategory.VIDEOS -> { vidBytes += item.size; vidCount++ }
                FileCategory.AUDIO -> { audBytes += item.size; audCount++ }
                FileCategory.DOCUMENTS -> { docBytes += item.size; docCount++ }
                FileCategory.ARCHIVES -> { arcBytes += item.size; arcCount++ }
                else -> othBytes += item.size
            }
        }

        StorageBreakdown(
            totalBytes = total,
            usedBytes = used,
            freeBytes = free,
            imagesBytes = imgBytes,
            videosBytes = vidBytes,
            audioBytes = audBytes,
            documentsBytes = docBytes,
            archivesBytes = arcBytes,
            otherBytes = othBytes,
            imagesCount = imgCount,
            videosCount = vidCount,
            audioCount = audCount,
            documentsCount = docCount,
            archivesCount = arcCount
        )
    }

    suspend fun getLargestFiles(limit: Int = 20): List<LocalFileItem> = withContext(Dispatchers.IO) {
        val scanned = scanAllAccessibleFiles()
        scanned.sortedByDescending { it.size }.take(limit)
    }

    suspend fun findDuplicates(): List<DuplicateGroup> = withContext(Dispatchers.IO) {
        val scanned = scanAllAccessibleFiles().filter { it.size > 1024 } // Skip tiny files (<1KB)
        // 1. Group by exact file size
        val sizeBuckets = scanned.groupBy { it.size }.filter { it.value.size > 1 }

        val duplicates = mutableListOf<DuplicateGroup>()
        for ((size, files) in sizeBuckets) {
            // 2. Hash files that share same size
            val hashBuckets = files.groupBy { calculateMD5(it.file) }.filter { it.value.size > 1 }
            for ((hash, dupeFiles) in hashBuckets) {
                if (hash.isNotEmpty()) {
                    duplicates.add(DuplicateGroup(hash = hash, size = size, files = dupeFiles))
                }
            }
        }
        duplicates.sortedByDescending { it.size * (it.files.size - 1) }
    }

    suspend fun getCleanupSuggestions(): List<StorageCleanupItem> = withContext(Dispatchers.IO) {
        val suggestions = mutableListOf<StorageCleanupItem>()
        val scanned = scanAllAccessibleFiles()

        // 1. Large Files (> 20MB)
        val largeFiles = scanned.filter { it.size > 20 * 1024 * 1024 }.sortedByDescending { it.size }
        if (largeFiles.isNotEmpty()) {
            val totalSize = largeFiles.sumOf { it.size }
            suggestions.add(
                StorageCleanupItem(
                    title = "Large Files (${largeFiles.size})",
                    description = "Files exceeding 20 MB that consume significant storage.",
                    files = largeFiles,
                    totalSize = totalSize
                )
            )
        }

        // 2. Duplicates
        val dupes = findDuplicates()
        if (dupes.isNotEmpty()) {
            val dupeFilesToDelete = dupes.flatMap { it.files.drop(1) }
            val recoverableSize = dupeFilesToDelete.sumOf { it.size }
            suggestions.add(
                StorageCleanupItem(
                    title = "Duplicate Files (${dupeFilesToDelete.size})",
                    description = "Exact copies of files found in multiple folders.",
                    files = dupeFilesToDelete,
                    totalSize = recoverableSize
                )
            )
        }

        // 3. Old Downloads (> 60 days)
        val sixtyDaysAgo = System.currentTimeMillis() - (60L * 24 * 60 * 60 * 1000)
        val oldDownloads = scanned.filter {
            (it.path.contains("Download", ignoreCase = true) || it.category == FileCategory.ARCHIVES) &&
                    it.lastModified < sixtyDaysAgo
        }
        if (oldDownloads.isNotEmpty()) {
            val totalSize = oldDownloads.sumOf { it.size }
            suggestions.add(
                StorageCleanupItem(
                    title = "Old Downloads & Archives (${oldDownloads.size})",
                    description = "Downloaded files and archives older than 60 days.",
                    files = oldDownloads,
                    totalSize = totalSize
                )
            )
        }

        suggestions
    }

    fun calculateMD5(file: File): String {
        return try {
            val digest = MessageDigest.getInstance("MD5")
            FileInputStream(file).use { fis ->
                val buffer = ByteArray(8192)
                var bytesRead: Int
                var count = 0
                while (fis.read(buffer).also { bytesRead = it } != -1 && count < 64) {
                    digest.update(buffer, 0, bytesRead)
                    count++ // Read up to first 512KB for fast responsive matching
                }
            }
            digest.digest().joinToString("") { "%02x".format(it) }
        } catch (_: Exception) {
            ""
        }
    }

    // --- Favorites & Recents ---

    fun isFavorite(path: String): Boolean {
        return getFavoritesSet().contains(path)
    }

    fun addFavorite(path: String) {
        val current = getFavoritesSet().toMutableSet()
        current.add(path)
        prefs.edit().putStringSet("favorite_files", current).apply()
    }

    fun removeFavorite(path: String) {
        val current = getFavoritesSet().toMutableSet()
        current.remove(path)
        prefs.edit().putStringSet("favorite_files", current).apply()
    }

    fun toggleFavorite(path: String): Boolean {
        val fav = isFavorite(path)
        if (fav) removeFavorite(path) else addFavorite(path)
        return !fav
    }

    private fun getFavoritesSet(): Set<String> {
        return prefs.getStringSet("favorite_files", emptySet()) ?: emptySet()
    }

    fun recordRecent(path: String) {
        val list = getRecentPaths().toMutableList()
        list.remove(path)
        list.add(0, path)
        val trimmed = list.take(50)
        prefs.edit().putString("recent_files_ordered", trimmed.joinToString("|||")).apply()
    }

    private fun getRecentPaths(): List<String> {
        val raw = prefs.getString("recent_files_ordered", "") ?: ""
        if (raw.isBlank()) return emptyList()
        return raw.split("|||").filter { it.isNotBlank() }
    }

    // --- Yaseen Attachments Integration ---

    suspend fun saveAttachment(sourceFile: File, targetModule: String): Result<File> = withContext(Dispatchers.IO) {
        val targetFolderName = when (targetModule.uppercase(Locale.ROOT)) {
            "NOTE" -> "Notes Attachments"
            "TASK" -> "Tasks Attachments"
            "SCHEDULE" -> "Schedule Attachments"
            else -> "Notes Attachments"
        }
        val folder = File(appFilesDir, targetFolderName)
        if (!folder.exists()) folder.mkdirs()
        copy(sourceFile, folder)
    }

    // --- Android Intents: Share & Open With ---

    fun getFileUri(file: File): Uri {
        return FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            file
        )
    }

    fun createShareIntent(file: File): Intent {
        val uri = getFileUri(file)
        val mime = getMimeTypeFromExtension(file.extension.lowercase(Locale.ROOT))
        return Intent(Intent.ACTION_SEND).apply {
            type = mime
            putExtra(Intent.EXTRA_STREAM, uri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
    }

    fun createOpenWithIntent(file: File): Intent {
        val uri = getFileUri(file)
        val mime = getMimeTypeFromExtension(file.extension.lowercase(Locale.ROOT))
        return Intent(Intent.ACTION_VIEW).apply {
            setDataAndType(uri, mime)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
    }

    private fun getUniqueFile(dir: File, fileName: String): File {
        var file = File(dir, fileName)
        if (!file.exists()) return file

        val nameWithoutExt = file.nameWithoutExtension
        val ext = if (file.extension.isNotEmpty()) ".${file.extension}" else ""
        var counter = 1
        while (file.exists()) {
            file = File(dir, "${nameWithoutExt}_$counter$ext")
            counter++
        }
        return file
    }

    private fun sanitizeFilename(name: String): String {
        return name.replace(Regex("[/\\\\?%*:|\"<>]"), "_").trim()
    }
}
