package com.example.ui.theme

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Article
import androidx.compose.material.icons.filled.Assistant
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.CheckBox
import androidx.compose.material.icons.filled.CheckCircleOutline
import androidx.compose.material.icons.filled.CollectionsBookmark
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.EditNote
import androidx.compose.material.icons.filled.EventNote
import androidx.compose.material.icons.filled.FactCheck
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.FolderOpen
import androidx.compose.material.icons.filled.FolderSpecial
import androidx.compose.material.icons.filled.FormatListNumbered
import androidx.compose.material.icons.filled.HistoryEdu
import androidx.compose.material.icons.filled.Hub
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.SmartToy
import androidx.compose.material.icons.filled.SnippetFolder
import androidx.compose.material.icons.filled.Source
import androidx.compose.material.icons.filled.TaskAlt
import androidx.compose.material.icons.filled.Terminal
import androidx.compose.material.icons.filled.Today
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector

data class IconPack(
    val name: String,
    val description: String,
    val primaryColor: Color,
    val notesIcon: ImageVector,
    val tasksIcon: ImageVector,
    val scheduleIcon: ImageVector,
    val chatbotIcon: ImageVector,
    val filesIcon: ImageVector = Icons.Default.FolderOpen,
    val notebooksIcon: ImageVector,
    val settingsIcon: ImageVector
)

object IconThemeHelper {
    val emeraldPack = IconPack(
        name = "Emerald",
        description = "Refined emerald gradient aesthetic",
        primaryColor = Color(0xFF10B981),
        notesIcon = Icons.Default.EditNote,
        tasksIcon = Icons.Default.TaskAlt,
        scheduleIcon = Icons.Default.CalendarMonth,
        chatbotIcon = Icons.Default.AutoAwesome,
        notebooksIcon = Icons.Default.Folder,
        settingsIcon = Icons.Default.Settings
    )

    val neonCyanPack = IconPack(
        name = "Neon Cyan",
        description = "Futuristic cyber style",
        primaryColor = Color(0xFF06B6D4),
        notesIcon = Icons.Default.Terminal,
        tasksIcon = Icons.Default.CheckCircleOutline,
        scheduleIcon = Icons.Default.Schedule,
        chatbotIcon = Icons.Default.SmartToy,
        notebooksIcon = Icons.Default.FolderSpecial,
        settingsIcon = Icons.Default.Settings
    )

    val sunsetGoldPack = IconPack(
        name = "Sunset Gold",
        description = "Warm energetic amber palette",
        primaryColor = Color(0xFFF59E0B),
        notesIcon = Icons.Default.Bookmark,
        tasksIcon = Icons.Default.FormatListNumbered,
        scheduleIcon = Icons.Default.Today,
        chatbotIcon = Icons.Default.Psychology,
        notebooksIcon = Icons.Default.CollectionsBookmark,
        settingsIcon = Icons.Default.Settings
    )

    val royalPurplePack = IconPack(
        name = "Royal Purple",
        description = "Creative and elegant indigo-violet",
        primaryColor = Color(0xFF8B5CF6),
        notesIcon = Icons.Default.HistoryEdu,
        tasksIcon = Icons.Default.FactCheck,
        scheduleIcon = Icons.Default.EventNote,
        chatbotIcon = Icons.Default.Hub,
        notebooksIcon = Icons.Default.SnippetFolder,
        settingsIcon = Icons.Default.Settings
    )

    val minimalSlatePack = IconPack(
        name = "Minimal Slate",
        description = "Clean, focused, modern monochrome",
        primaryColor = Color(0xFF64748B),
        notesIcon = Icons.Default.Article,
        tasksIcon = Icons.Default.CheckBox,
        scheduleIcon = Icons.Default.DateRange,
        chatbotIcon = Icons.Default.Assistant,
        notebooksIcon = Icons.Default.Source,
        settingsIcon = Icons.Default.Settings
    )

    val crimsonRubyPack = IconPack(
        name = "Crimson Ruby",
        description = "Vibrant, bold crimson and rose energy",
        primaryColor = Color(0xFFE11D48),
        notesIcon = Icons.Default.EditNote,
        tasksIcon = Icons.Default.TaskAlt,
        scheduleIcon = Icons.Default.CalendarMonth,
        chatbotIcon = Icons.Default.AutoAwesome,
        notebooksIcon = Icons.Default.Folder,
        settingsIcon = Icons.Default.Settings
    )

    val oceanBluePack = IconPack(
        name = "Ocean Blue",
        description = "Deep nautical sapphire & calming azure",
        primaryColor = Color(0xFF2563EB),
        notesIcon = Icons.Default.Article,
        tasksIcon = Icons.Default.CheckCircleOutline,
        scheduleIcon = Icons.Default.Schedule,
        chatbotIcon = Icons.Default.SmartToy,
        notebooksIcon = Icons.Default.FolderSpecial,
        settingsIcon = Icons.Default.Settings
    )

    val availablePacks = listOf(
        emeraldPack,
        neonCyanPack,
        sunsetGoldPack,
        royalPurplePack,
        minimalSlatePack,
        crimsonRubyPack,
        oceanBluePack
    )

    fun getPack(name: String): IconPack {
        return availablePacks.firstOrNull { it.name.equals(name, ignoreCase = true) } ?: emeraldPack
    }
}
