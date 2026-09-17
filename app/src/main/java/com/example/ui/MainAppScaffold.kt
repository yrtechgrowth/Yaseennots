package com.example.ui

import androidx.activity.compose.BackHandler
import androidx.compose.animation.Crossfade
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.NavigationDrawerItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.Note
import com.example.ui.screens.BackupRestoreScreen
import com.example.ui.screens.ChatbotScreen
import com.example.ui.screens.NoteEditorScreen
import com.example.ui.screens.NotebooksScreen
import com.example.ui.screens.NotesListScreen
import com.example.ui.screens.ScheduleScreen
import com.example.ui.screens.SettingsScreen
import com.example.ui.screens.StatisticsScreen
import com.example.ui.screens.TasksScreen
import com.example.ui.screens.TrashScreen
import com.example.ui.screens.files.FilesScreen
import com.example.ui.theme.IconThemeHelper
import com.example.ui.viewmodel.NotesViewModel
import com.example.ui.widget.YaseenWidgetProvider
import kotlinx.coroutines.launch

sealed class Screen {
    object Notes : Screen()
    object Tasks : Screen()
    object Schedule : Screen()
    object Files : Screen()
    object Chatbot : Screen()
    object Notebooks : Screen()
    object Trash : Screen()
    object Statistics : Screen()
    object Backup : Screen()
    object Settings : Screen()
    data class NoteEditor(val noteId: String? = null, val initialNote: Note? = null) : Screen()
}

@Composable
fun MainAppScaffold(
    viewModel: NotesViewModel,
    isDarkTheme: Boolean,
    onToggleDarkTheme: () -> Unit,
    initialNavTarget: String? = null,
    initialAction: String? = null
) {
    val settings by viewModel.settings.collectAsState()
    val currentPack = IconThemeHelper.getPack(settings.iconTheme)

    val initialScreen = remember(initialNavTarget, initialAction) {
        if (initialAction == "ACTION_NEW_NOTE") {
            Screen.NoteEditor(null, null)
        } else {
            when (initialNavTarget) {
                "tasks" -> Screen.Tasks
                "schedule" -> Screen.Schedule
                "files" -> Screen.Files
                "chatbot" -> Screen.Chatbot
                "notes" -> Screen.Notes
                else -> when (settings.defaultTab) {
                    "TASKS" -> Screen.Tasks
                    "SCHEDULE" -> Screen.Schedule
                    "FILES" -> Screen.Files
                    "CHATBOT" -> Screen.Chatbot
                    else -> Screen.Notes
                }
            }
        }
    }

    var currentScreen by remember { mutableStateOf<Screen>(initialScreen) }
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()

    val rawNotes by viewModel.rawActiveNotes.collectAsState()
    val rawTasks by viewModel.tasks.collectAsState()
    val rawSchedule by viewModel.scheduleItems.collectAsState()
    val notebooks by viewModel.notebooks.collectAsState()
    val trashNotes by viewModel.trashNotes.collectAsState()

    val pendingTasksCount = remember(rawTasks) { rawTasks.count { !it.isCompleted } }

    val context = LocalContext.current
    // Intelligently sync lightweight stats with Home Screen Widget without database hits during widget draw
    LaunchedEffect(rawNotes.size, pendingTasksCount) {
        val preview = rawTasks.firstOrNull { !it.isCompleted }?.title?.let { "Task: $it" }
            ?: rawNotes.firstOrNull()?.title?.let { "Note: $it" }
            ?: "All caught up!"
        YaseenWidgetProvider.updateWidgetData(
            context = context,
            notesCount = rawNotes.size,
            pendingTasksCount = pendingTasksCount,
            previewMessage = preview
        )
    }

    // Intercept hardware back press when in editor or other screens
    BackHandler(enabled = currentScreen !is Screen.Notes) {
        currentScreen = Screen.Notes
    }

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet(
                modifier = Modifier.width(300.dp)
            ) {
                // Header displaying dynamic App Name and dynamic Icon
                Surface(
                    color = MaterialTheme.colorScheme.primaryContainer,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp)
                    ) {
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = currentPack.primaryColor,
                            modifier = Modifier.size(48.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = currentPack.notesIcon,
                                    contentDescription = settings.appName,
                                    tint = Color.White,
                                    modifier = Modifier.size(30.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        Text(
                            text = settings.appName,
                            style = MaterialTheme.typography.titleLarge.copy(
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        )
                        Text(
                            text = "Notes • Tasks • Schedule • YaRVerse",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Core Navigation Items
                NavigationDrawerItem(
                    label = { Text("${settings.appName} Notes") },
                    icon = { Icon(currentPack.notesIcon, contentDescription = null, tint = currentPack.primaryColor) },
                    badge = { Text(rawNotes.size.toString()) },
                    selected = currentScreen is Screen.Notes,
                    onClick = {
                        currentScreen = Screen.Notes
                        scope.launch { drawerState.close() }
                    },
                    modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)
                )

                NavigationDrawerItem(
                    label = { Text("${settings.appName} Tasks") },
                    icon = { Icon(currentPack.tasksIcon, contentDescription = null, tint = currentPack.primaryColor) },
                    badge = { if (pendingTasksCount > 0) Text(pendingTasksCount.toString()) },
                    selected = currentScreen is Screen.Tasks,
                    onClick = {
                        currentScreen = Screen.Tasks
                        scope.launch { drawerState.close() }
                    },
                    modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)
                )

                NavigationDrawerItem(
                    label = { Text("${settings.appName} Schedule") },
                    icon = { Icon(currentPack.scheduleIcon, contentDescription = null, tint = currentPack.primaryColor) },
                    badge = { Text(rawSchedule.size.toString()) },
                    selected = currentScreen is Screen.Schedule,
                    onClick = {
                        currentScreen = Screen.Schedule
                        scope.launch { drawerState.close() }
                    },
                    modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)
                )

                NavigationDrawerItem(
                    label = { Text(settings.dynamicFilesTitle) },
                    icon = { Icon(currentPack.filesIcon, contentDescription = null, tint = currentPack.primaryColor) },
                    selected = currentScreen is Screen.Files,
                    onClick = {
                        currentScreen = Screen.Files
                        scope.launch { drawerState.close() }
                    },
                    modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)
                )

                NavigationDrawerItem(
                    label = { Text("YaRVerse AI Chatbot") },
                    icon = { Icon(currentPack.chatbotIcon, contentDescription = null, tint = currentPack.primaryColor) },
                    selected = currentScreen is Screen.Chatbot,
                    onClick = {
                        currentScreen = Screen.Chatbot
                        scope.launch { drawerState.close() }
                    },
                    modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)
                )

                HorizontalDivider(modifier = Modifier.padding(vertical = 6.dp, horizontal = 16.dp))

                NavigationDrawerItem(
                    label = { Text("Notebooks") },
                    icon = { Icon(currentPack.notebooksIcon, contentDescription = null) },
                    badge = { Text(notebooks.size.toString()) },
                    selected = currentScreen is Screen.Notebooks,
                    onClick = {
                        currentScreen = Screen.Notebooks
                        scope.launch { drawerState.close() }
                    },
                    modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)
                )

                NavigationDrawerItem(
                    label = { Text("Trash") },
                    icon = { Icon(Icons.Default.DeleteSweep, contentDescription = null) },
                    badge = { if (trashNotes.isNotEmpty()) Text(trashNotes.size.toString()) },
                    selected = currentScreen is Screen.Trash,
                    onClick = {
                        currentScreen = Screen.Trash
                        scope.launch { drawerState.close() }
                    },
                    modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)
                )

                NavigationDrawerItem(
                    label = { Text("Statistics") },
                    icon = { Icon(Icons.Default.BarChart, contentDescription = null) },
                    selected = currentScreen is Screen.Statistics,
                    onClick = {
                        currentScreen = Screen.Statistics
                        scope.launch { drawerState.close() }
                    },
                    modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)
                )

                NavigationDrawerItem(
                    label = { Text("Backup & Restore") },
                    icon = { Icon(Icons.Default.Storage, contentDescription = null) },
                    selected = currentScreen is Screen.Backup,
                    onClick = {
                        currentScreen = Screen.Backup
                        scope.launch { drawerState.close() }
                    },
                    modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)
                )

                NavigationDrawerItem(
                    label = { Text("Settings & Controls") },
                    icon = { Icon(currentPack.settingsIcon, contentDescription = null, tint = currentPack.primaryColor) },
                    selected = currentScreen is Screen.Settings,
                    onClick = {
                        currentScreen = Screen.Settings
                        scope.launch { drawerState.close() }
                    },
                    modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)
                )

                Spacer(modifier = Modifier.weight(1f))

                // Dark Theme Toggle Row in Drawer Footer
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 14.dp)
                ) {
                    Icon(
                        imageVector = if (isDarkTheme) Icons.Default.DarkMode else Icons.Default.LightMode,
                        contentDescription = "Theme",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = if (isDarkTheme) "Dark Mode" else "Light Mode",
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.weight(1f)
                    )
                    Switch(
                        checked = isDarkTheme,
                        onCheckedChange = { onToggleDarkTheme() }
                    )
                }
            }
        }
    ) {
        val showBottomBar = currentScreen is Screen.Notes || currentScreen is Screen.Tasks || currentScreen is Screen.Schedule || currentScreen is Screen.Files || currentScreen is Screen.Chatbot

        Scaffold(
            bottomBar = {
                if (showBottomBar) {
                    NavigationBar(
                        containerColor = MaterialTheme.colorScheme.surface,
                        tonalElevation = 6.dp
                    ) {
                        NavigationBarItem(
                            selected = currentScreen is Screen.Notes,
                            onClick = { currentScreen = Screen.Notes },
                            icon = {
                                Icon(
                                    imageVector = currentPack.notesIcon,
                                    contentDescription = "Notes"
                                )
                            },
                            label = { Text("Notes") },
                            colors = NavigationBarItemDefaults.colors(
                                indicatorColor = currentPack.primaryColor.copy(alpha = 0.2f),
                                selectedIconColor = currentPack.primaryColor,
                                selectedTextColor = currentPack.primaryColor
                            ),
                            modifier = Modifier.testTag("nav_notes")
                        )

                        NavigationBarItem(
                            selected = currentScreen is Screen.Tasks,
                            onClick = { currentScreen = Screen.Tasks },
                            icon = {
                                Icon(
                                    imageVector = currentPack.tasksIcon,
                                    contentDescription = "Tasks"
                                )
                            },
                            label = { Text("Tasks") },
                            colors = NavigationBarItemDefaults.colors(
                                indicatorColor = currentPack.primaryColor.copy(alpha = 0.2f),
                                selectedIconColor = currentPack.primaryColor,
                                selectedTextColor = currentPack.primaryColor
                            ),
                            modifier = Modifier.testTag("nav_tasks")
                        )

                        NavigationBarItem(
                            selected = currentScreen is Screen.Schedule,
                            onClick = { currentScreen = Screen.Schedule },
                            icon = {
                                Icon(
                                    imageVector = currentPack.scheduleIcon,
                                    contentDescription = "Schedule"
                                )
                            },
                            label = { Text("Schedule") },
                            colors = NavigationBarItemDefaults.colors(
                                indicatorColor = currentPack.primaryColor.copy(alpha = 0.2f),
                                selectedIconColor = currentPack.primaryColor,
                                selectedTextColor = currentPack.primaryColor
                            ),
                            modifier = Modifier.testTag("nav_schedule")
                        )

                        NavigationBarItem(
                            selected = currentScreen is Screen.Files,
                            onClick = { currentScreen = Screen.Files },
                            icon = {
                                Icon(
                                    imageVector = currentPack.filesIcon,
                                    contentDescription = settings.dynamicFilesTitle
                                )
                            },
                            label = { Text("Files") },
                            colors = NavigationBarItemDefaults.colors(
                                indicatorColor = currentPack.primaryColor.copy(alpha = 0.2f),
                                selectedIconColor = currentPack.primaryColor,
                                selectedTextColor = currentPack.primaryColor
                            ),
                            modifier = Modifier.testTag("nav_files")
                        )

                        NavigationBarItem(
                            selected = currentScreen is Screen.Chatbot,
                            onClick = { currentScreen = Screen.Chatbot },
                            icon = {
                                Icon(
                                    imageVector = currentPack.chatbotIcon,
                                    contentDescription = "YaRVerse"
                                )
                            },
                            label = { Text("YaRVerse") },
                            colors = NavigationBarItemDefaults.colors(
                                indicatorColor = currentPack.primaryColor.copy(alpha = 0.2f),
                                selectedIconColor = currentPack.primaryColor,
                                selectedTextColor = currentPack.primaryColor
                            ),
                            modifier = Modifier.testTag("nav_chatbot")
                        )
                    }
                }
            }
        ) { innerPadding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {
                Crossfade(targetState = currentScreen, label = "screen_transition") { screen ->
                    when (screen) {
                        is Screen.Notes -> NotesListScreen(
                            viewModel = viewModel,
                            onOpenDrawer = { scope.launch { drawerState.open() } },
                            onNoteClick = { noteId ->
                                currentScreen = Screen.NoteEditor(noteId = noteId)
                            },
                            onCreateNewNote = {
                                currentScreen = Screen.NoteEditor()
                            },
                            onUseTemplate = { templateNote ->
                                currentScreen = Screen.NoteEditor(initialNote = templateNote)
                            }
                        )

                        is Screen.Tasks -> TasksScreen(
                            viewModel = viewModel,
                            onOpenDrawer = { scope.launch { drawerState.open() } }
                        )

                        is Screen.Schedule -> ScheduleScreen(
                            viewModel = viewModel,
                            onOpenDrawer = { scope.launch { drawerState.open() } }
                        )

                        is Screen.Files -> FilesScreen(
                            notesViewModel = viewModel,
                            onOpenDrawer = { scope.launch { drawerState.open() } },
                            onNavigateToNoteEditor = { noteId ->
                                currentScreen = Screen.NoteEditor(noteId = noteId)
                            }
                        )

                        is Screen.Chatbot -> ChatbotScreen(
                            viewModel = viewModel,
                            onOpenDrawer = { scope.launch { drawerState.open() } },
                            onNavigateToSettings = { currentScreen = Screen.Settings }
                        )

                        is Screen.Notebooks -> NotebooksScreen(
                            viewModel = viewModel,
                            onOpenDrawer = { scope.launch { drawerState.open() } },
                            onSelectNotebookToView = {
                                currentScreen = Screen.Notes
                            }
                        )

                        is Screen.Trash -> TrashScreen(
                            viewModel = viewModel,
                            onOpenDrawer = { scope.launch { drawerState.open() } }
                        )

                        is Screen.Statistics -> StatisticsScreen(
                            viewModel = viewModel,
                            onOpenDrawer = { scope.launch { drawerState.open() } }
                        )

                        is Screen.Backup -> BackupRestoreScreen(
                            viewModel = viewModel,
                            onOpenDrawer = { scope.launch { drawerState.open() } }
                        )

                        is Screen.Settings -> SettingsScreen(
                            viewModel = viewModel,
                            onOpenDrawer = { scope.launch { drawerState.open() } },
                            onNavigateToBackup = { currentScreen = Screen.Backup }
                        )

                        is Screen.NoteEditor -> NoteEditorScreen(
                            noteId = screen.noteId,
                            initialNote = screen.initialNote,
                            viewModel = viewModel,
                            onNavigateBack = {
                                currentScreen = Screen.Notes
                            }
                        )
                    }
                }
            }
        }
    }
}
