package com.example.data

import android.net.Uri
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

enum class FileCategory(val displayName: String) {
    ALL("All Files"),
    IMAGES("Images"),
    VIDEOS("Videos"),
    AUDIO("Audio"),
    DOCUMENTS("Documents"),
    ARCHIVES("Archives"),
    DOWNLOADS("Downloads"),
    ATTACHMENTS("Attachments"),
    RECENTS("Recent"),
    FAVORITES("Favorites"),
    STORAGE_ANALYZER("Analyzer"),
    PRIVATE_VAULT("Private Safe")
}

data class LocalFileItem(
    val id: String, // Absolute path or URI string
    val file: File,
    val uri: Uri? = null,
    val name: String,
    val path: String,
    val size: Long = 0L,
    val lastModified: Long = 0L,
    val isDirectory: Boolean = false,
    val extension: String = "",
    val category: FileCategory = FileCategory.ALL,
    val mimeType: String = "*/*",
    val childCount: Int = 0,
    val isFavorite: Boolean = false,
    val isPrivate: Boolean = false
) {
    val formattedSize: String
        get() {
            if (isDirectory) {
                return if (childCount == 1) "1 item" else "$childCount items"
            }
            if (size <= 0) return "0 B"
            val units = arrayOf("B", "KB", "MB", "GB", "TB")
            val digitGroups = (Math.log10(size.toDouble()) / Math.log10(1024.0)).toInt()
            val clamped = digitGroups.coerceIn(0, units.size - 1)
            val value = size / Math.pow(1024.0, clamped.toDouble())
            return String.format(Locale.US, "%.1f %s", value, units[clamped])
        }

    val formattedDate: String
        get() {
            if (lastModified <= 0) return "Unknown"
            val sdf = SimpleDateFormat("MMM dd, yyyy HH:mm", Locale.getDefault())
            return sdf.format(Date(lastModified))
        }

    val isMedia: Boolean
        get() = category == FileCategory.IMAGES || category == FileCategory.VIDEOS || category == FileCategory.AUDIO

    val isTextEditable: Boolean
        get() {
            val ext = extension.lowercase(Locale.ROOT)
            return ext in setOf("txt", "md", "json", "xml", "csv", "log", "kt", "java", "py", "html", "css", "js", "ts", "properties", "gradle", "yaml", "yml")
        }
}

enum class FileSortOption(val label: String) {
    NAME("Name"),
    DATE("Date Modified"),
    SIZE("Size"),
    TYPE("File Type")
}

enum class FileSortOrder(val label: String) {
    ASCENDING("Ascending (A-Z, Old-New)"),
    DESCENDING("Descending (Z-A, New-Old)")
}

enum class ViewMode {
    GRID,
    LIST
}

data class FileClipboard(
    val items: List<LocalFileItem> = emptyList(),
    val isCut: Boolean = false
)

data class StorageBreakdown(
    val totalBytes: Long = 0L,
    val usedBytes: Long = 0L,
    val freeBytes: Long = 0L,
    val imagesBytes: Long = 0L,
    val videosBytes: Long = 0L,
    val audioBytes: Long = 0L,
    val documentsBytes: Long = 0L,
    val archivesBytes: Long = 0L,
    val otherBytes: Long = 0L,
    val imagesCount: Int = 0,
    val videosCount: Int = 0,
    val audioCount: Int = 0,
    val documentsCount: Int = 0,
    val archivesCount: Int = 0
) {
    val usedPercent: Float
        get() = if (totalBytes > 0) (usedBytes.toFloat() / totalBytes.toFloat()).coerceIn(0f, 1f) else 0f

    fun formatBytes(bytes: Long): String {
        if (bytes <= 0) return "0 MB"
        val units = arrayOf("B", "KB", "MB", "GB", "TB")
        val digitGroups = (Math.log10(bytes.toDouble()) / Math.log10(1024.0)).toInt()
        val clamped = digitGroups.coerceIn(0, units.size - 1)
        val value = bytes / Math.pow(1024.0, clamped.toDouble())
        return String.format(Locale.US, "%.1f %s", value, units[clamped])
    }
}

data class DuplicateGroup(
    val hash: String,
    val size: Long,
    val files: List<LocalFileItem>
)

data class StorageCleanupItem(
    val title: String,
    val description: String,
    val files: List<LocalFileItem>,
    val totalSize: Long
)
