package com.example.ui.screens.files

import android.Manifest
import android.os.Build
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Archive
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Checklist
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.ContentCut
import androidx.compose.material.icons.filled.ContentPaste
import androidx.compose.material.icons.filled.CreateNewFolder
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DriveFileMove
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.FolderOpen
import androidx.compose.material.icons.filled.GridView
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.NoteAdd
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.SelectAll
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Sort
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material.icons.filled.ViewList
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ElevatedButton
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.data.FileCategory
import com.example.data.FileSortOption
import com.example.data.FileSortOrder
import com.example.data.LocalFileItem
import com.example.data.Note
import com.example.data.ViewMode
import com.example.ui.viewmodel.FilesViewModel
import com.example.ui.viewmodel.NotesViewModel
import kotlinx.coroutines.launch
import java.io.File
import java.util.UUID

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FilesScreen(
    notesViewModel: NotesViewModel,
    filesViewModel: FilesViewModel = viewModel(),
    onOpenDrawer: () -> Unit,
    onNavigateToNoteEditor: (String?) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    // State collections
    val appSettings by notesViewModel.settings.collectAsState()
    val notesList by notesViewModel.rawActiveNotes.collectAsState()
    val tasksList by notesViewModel.tasks.collectAsState()
    val schedulesList by notesViewModel.scheduleItems.collectAsState()

    val currentDirectory by filesViewModel.currentDirectory.collectAsState()
    val directoryHistory by filesViewModel.directoryHistory.collectAsState()
    val files by filesViewModel.files.collectAsState()
    val isLoading by filesViewModel.isLoading.collectAsState()
    val errorMessage by filesViewModel.errorMessage.collectAsState()
    val successMessage by filesViewModel.successMessage.collectAsState()

    val selectedCategory by filesViewModel.selectedCategory.collectAsState()
    val searchQuery by filesViewModel.searchQuery.collectAsState()
    val sortOption by filesViewModel.sortOption.collectAsState()
    val sortOrder by filesViewModel.sortOrder.collectAsState()
    val viewMode by filesViewModel.viewMode.collectAsState()
    val showHiddenFiles by filesViewModel.showHiddenFiles.collectAsState()

    val isSelectionMode by filesViewModel.isSelectionMode.collectAsState()
    val selectedItemIds by filesViewModel.selectedItemIds.collectAsState()
    val clipboard by filesViewModel.clipboard.collectAsState()

    val activeViewerItem by filesViewModel.activeViewerItem.collectAsState()
    val mediaPlaylist by filesViewModel.mediaPlaylist.collectAsState()
    val mediaIndex by filesViewModel.mediaIndex.collectAsState()

    val editingFile by filesViewModel.editingFile.collectAsState()
    val editingText by filesViewModel.editingText.collectAsState()

    val storageBreakdown by filesViewModel.storageBreakdown.collectAsState()
    val duplicateGroups by filesViewModel.duplicateGroups.collectAsState()
    val cleanupSuggestions by filesViewModel.cleanupSuggestions.collectAsState()
    val largestFiles by filesViewModel.largestFiles.collectAsState()
    val isAnalyzingStorage by filesViewModel.isAnalyzing.collectAsState()

    val isVaultUnlocked by filesViewModel.isVaultUnlocked.collectAsState()
    val isAiLoading by filesViewModel.isAiLoading.collectAsState()
    val aiResult by filesViewModel.aiResult.collectAsState()

    // Dialog & UI Visibility states
    var isSearchExpanded by remember { mutableStateOf(false) }
    var isSortMenuExpanded by remember { mutableStateOf(false) }
    var isOptionsMenuExpanded by remember { mutableStateOf(false) }

    var showNewFolderDialog by remember { mutableStateOf(false) }
    var showNewFileDialog by remember { mutableStateOf(false) }
    var showZipDialog by remember { mutableStateOf(false) }
    var renamingItem by remember { mutableStateOf<LocalFileItem?>(null) }
    var detailsItem by remember { mutableStateOf<LocalFileItem?>(null) }
    var attachingItem by remember { mutableStateOf<LocalFileItem?>(null) }
    var aiActionItem by remember { mutableStateOf<LocalFileItem?>(null) }
    var showVaultPinDialog by remember { mutableStateOf(false) }
    var showStorageAnalyzerScreen by remember { mutableStateOf(false) }
    var deleteConfirmItem by remember { mutableStateOf<LocalFileItem?>(null) }
    var showDeleteSelectionConfirm by remember { mutableStateOf(false) }

    // Snackbar notifications
    LaunchedEffect(errorMessage) {
        errorMessage?.let {
            snackbarHostState.showSnackbar(it)
            filesViewModel.clearMessages()
        }
    }
    LaunchedEffect(successMessage) {
        successMessage?.let {
            snackbarHostState.showSnackbar(it)
            filesViewModel.clearMessages()
        }
    }

    // Modern permission launcher
    val permissionsLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { grants ->
        filesViewModel.refreshCurrentDirectory()
    }

    LaunchedEffect(Unit) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            permissionsLauncher.launch(
                arrayOf(
                    Manifest.permission.READ_MEDIA_IMAGES,
                    Manifest.permission.READ_MEDIA_VIDEO,
                    Manifest.permission.READ_MEDIA_AUDIO
                )
            )
        } else {
            permissionsLauncher.launch(
                arrayOf(
                    Manifest.permission.READ_EXTERNAL_STORAGE,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE
                )
            )
        }
    }

    // Filter files by search query
    val displayFiles = remember(files, searchQuery) {
        if (searchQuery.isBlank()) {
            files
        } else {
            files.filter { it.name.contains(searchQuery.trim(), ignoreCase = true) }
        }
    }

    // Handle Back Press hierarchy
    BackHandler(
        enabled = activeViewerItem != null ||
                editingFile != null ||
                showStorageAnalyzerScreen ||
                isSelectionMode ||
                directoryHistory.size > 1
    ) {
        when {
            activeViewerItem != null -> filesViewModel.closeViewer()
            editingFile != null -> filesViewModel.closeTextEditor()
            showStorageAnalyzerScreen -> showStorageAnalyzerScreen = false
            isSelectionMode -> filesViewModel.clearSelection()
            else -> filesViewModel.navigateUp()
        }
    }

    // --- Sub-Screen Overlays (Viewer / Editor / Analyzer) ---

    if (showStorageAnalyzerScreen) {
        StorageAnalyzerScreen(
            breakdown = storageBreakdown,
            duplicateGroups = duplicateGroups,
            cleanupSuggestions = cleanupSuggestions,
            largestFiles = largestFiles,
            isAnalyzing = isAnalyzingStorage,
            onRefresh = { filesViewModel.refreshStorageAnalytics() },
            onNavigateBack = { showStorageAnalyzerScreen = false },
            onDeleteFiles = { filesViewModel.deleteDuplicateFiles(it) },
            onOpenFile = {
                showStorageAnalyzerScreen = false
                filesViewModel.openViewer(it)
            }
        )
        return
    }

    editingFile?.let { item ->
        TextEditorScreen(
            item = item,
            initialContent = editingText,
            onSave = { filesViewModel.saveEditingFile(it) },
            onClose = { filesViewModel.closeTextEditor() },
            onShare = { filesViewModel.shareFile(it) },
            onAiAction = { aiActionItem = item }
        )
        return
    }

    activeViewerItem?.let { item ->
        when (item.category) {
            FileCategory.IMAGES -> {
                ImageViewerScreen(
                    item = item,
                    playlist = mediaPlaylist,
                    currentIndex = mediaIndex,
                    onNavigateBack = { filesViewModel.closeViewer() },
                    onNext = { filesViewModel.nextMedia() },
                    onPrevious = { filesViewModel.previousMedia() },
                    onToggleFavorite = { filesViewModel.toggleFavorite(it) },
                    onShare = { filesViewModel.shareFile(it) },
                    onDelete = {
                        filesViewModel.deleteSingleFile(it)
                        filesViewModel.closeViewer()
                    },
                    onShowDetails = { detailsItem = it }
                )
                return
            }
            FileCategory.VIDEOS -> {
                VideoPlayerScreen(
                    item = item,
                    playlist = mediaPlaylist,
                    currentIndex = mediaIndex,
                    onNavigateBack = { filesViewModel.closeViewer() },
                    onNext = { filesViewModel.nextMedia() },
                    onPrevious = { filesViewModel.previousMedia() },
                    onShare = { filesViewModel.shareFile(it) },
                    onShowDetails = { detailsItem = it }
                )
                return
            }
            FileCategory.AUDIO -> {
                AudioPlayerScreen(
                    item = item,
                    playlist = mediaPlaylist,
                    currentIndex = mediaIndex,
                    onNavigateBack = { filesViewModel.closeViewer() },
                    onNext = { filesViewModel.nextMedia() },
                    onPrevious = { filesViewModel.previousMedia() },
                    onShare = { filesViewModel.shareFile(it) }
                )
                return
            }
            else -> {
                // Documents / Archives / APKs - open with system viewer
                filesViewModel.openWith(item)
                filesViewModel.closeViewer()
            }
        }
    }

    // --- Main Files Browser Screen ---

    Scaffold(
        modifier = modifier.fillMaxSize(),
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = {
                    if (isSearchExpanded) {
                        OutlinedTextField(
                            value = searchQuery,
                            onValueChange = { filesViewModel.setSearchQuery(it) },
                            placeholder = { Text("Search files & folders...") },
                            singleLine = true,
                            trailingIcon = {
                                IconButton(onClick = {
                                    filesViewModel.setSearchQuery("")
                                    isSearchExpanded = false
                                }) {
                                    Icon(Icons.Default.Close, contentDescription = "Close search")
                                }
                            },
                            modifier = Modifier.fillMaxWidth()
                        )
                    } else {
                        Column {
                            Text(
                                text = appSettings.dynamicFilesTitle,
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Text(
                                text = "${displayFiles.size} items",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onOpenDrawer) {
                        Icon(Icons.Default.Menu, contentDescription = "Open Drawer")
                    }
                },
                actions = {
                    if (!isSearchExpanded) {
                        IconButton(onClick = { isSearchExpanded = true }) {
                            Icon(Icons.Default.Search, contentDescription = "Search")
                        }

                        // View Mode Toggle (Grid / List)
                        IconButton(onClick = { filesViewModel.toggleViewMode() }) {
                            Icon(
                                imageVector = if (viewMode == ViewMode.GRID) Icons.Default.ViewList else Icons.Default.GridView,
                                contentDescription = "Switch View Mode"
                            )
                        }

                        // Sort Menu
                        Box {
                            IconButton(onClick = { isSortMenuExpanded = true }) {
                                Icon(Icons.Default.Sort, contentDescription = "Sort")
                            }

                            DropdownMenu(
                                expanded = isSortMenuExpanded,
                                onDismissRequest = { isSortMenuExpanded = false }
                            ) {
                                DropdownMenuItem(
                                    text = { Text("Sort by Name") },
                                    trailingIcon = {
                                        if (sortOption == FileSortOption.NAME) {
                                            Icon(
                                                imageVector = if (sortOrder == FileSortOrder.ASCENDING) Icons.Default.ArrowUpward else Icons.Default.ArrowDownward,
                                                contentDescription = null,
                                                modifier = Modifier.size(16.dp)
                                            )
                                        }
                                    },
                                    onClick = {
                                        if (sortOption == FileSortOption.NAME) filesViewModel.toggleSortOrder()
                                        else filesViewModel.setSortOption(FileSortOption.NAME)
                                        isSortMenuExpanded = false
                                    }
                                )
                                DropdownMenuItem(
                                    text = { Text("Sort by Date") },
                                    trailingIcon = {
                                        if (sortOption == FileSortOption.DATE) {
                                            Icon(
                                                imageVector = if (sortOrder == FileSortOrder.ASCENDING) Icons.Default.ArrowUpward else Icons.Default.ArrowDownward,
                                                contentDescription = null,
                                                modifier = Modifier.size(16.dp)
                                            )
                                        }
                                    },
                                    onClick = {
                                        if (sortOption == FileSortOption.DATE) filesViewModel.toggleSortOrder()
                                        else filesViewModel.setSortOption(FileSortOption.DATE)
                                        isSortMenuExpanded = false
                                    }
                                )
                                DropdownMenuItem(
                                    text = { Text("Sort by Size") },
                                    trailingIcon = {
                                        if (sortOption == FileSortOption.SIZE) {
                                            Icon(
                                                imageVector = if (sortOrder == FileSortOrder.ASCENDING) Icons.Default.ArrowUpward else Icons.Default.ArrowDownward,
                                                contentDescription = null,
                                                modifier = Modifier.size(16.dp)
                                            )
                                        }
                                    },
                                    onClick = {
                                        if (sortOption == FileSortOption.SIZE) filesViewModel.toggleSortOrder()
                                        else filesViewModel.setSortOption(FileSortOption.SIZE)
                                        isSortMenuExpanded = false
                                    }
                                )
                                DropdownMenuItem(
                                    text = { Text("Sort by Type") },
                                    trailingIcon = {
                                        if (sortOption == FileSortOption.TYPE) {
                                            Icon(
                                                imageVector = if (sortOrder == FileSortOrder.ASCENDING) Icons.Default.ArrowUpward else Icons.Default.ArrowDownward,
                                                contentDescription = null,
                                                modifier = Modifier.size(16.dp)
                                            )
                                        }
                                    },
                                    onClick = {
                                        if (sortOption == FileSortOption.TYPE) filesViewModel.toggleSortOrder()
                                        else filesViewModel.setSortOption(FileSortOption.TYPE)
                                        isSortMenuExpanded = false
                                    }
                                )
                            }
                        }

                        // Multi-select toggle
                        IconButton(onClick = { filesViewModel.toggleSelectionMode() }) {
                            Icon(
                                imageVector = Icons.Default.Checklist,
                                contentDescription = "Select items",
                                tint = if (isSelectionMode) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                            )
                        }

                        // Overflow Options Menu
                        Box {
                            IconButton(onClick = { isOptionsMenuExpanded = true }) {
                                Icon(Icons.Default.MoreVert, contentDescription = "More")
                            }

                            DropdownMenu(
                                expanded = isOptionsMenuExpanded,
                                onDismissRequest = { isOptionsMenuExpanded = false }
                            ) {
                                DropdownMenuItem(
                                    text = { Text("New Folder") },
                                    leadingIcon = { Icon(Icons.Default.CreateNewFolder, contentDescription = null) },
                                    onClick = {
                                        isOptionsMenuExpanded = false
                                        showNewFolderDialog = true
                                    }
                                )
                                DropdownMenuItem(
                                    text = { Text("New Text File") },
                                    leadingIcon = { Icon(Icons.Default.NoteAdd, contentDescription = null) },
                                    onClick = {
                                        isOptionsMenuExpanded = false
                                        showNewFileDialog = true
                                    }
                                )
                                DropdownMenuItem(
                                    text = { Text("Storage Analyzer") },
                                    leadingIcon = { Icon(Icons.Default.Storage, contentDescription = null) },
                                    onClick = {
                                        isOptionsMenuExpanded = false
                                        filesViewModel.refreshStorageAnalytics()
                                        showStorageAnalyzerScreen = true
                                    }
                                )
                                DropdownMenuItem(
                                    text = { Text("Private Safe") },
                                    leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null, tint = Color(0xFFE11D48)) },
                                    onClick = {
                                        isOptionsMenuExpanded = false
                                        if (isVaultUnlocked) {
                                            filesViewModel.selectCategory(FileCategory.PRIVATE_VAULT)
                                        } else {
                                            showVaultPinDialog = true
                                        }
                                    }
                                )
                                DropdownMenuItem(
                                    text = { Text(if (showHiddenFiles) "Hide Hidden Files" else "Show Hidden Files") },
                                    leadingIcon = {
                                        Icon(
                                            if (showHiddenFiles) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                            contentDescription = null
                                        )
                                    },
                                    onClick = {
                                        isOptionsMenuExpanded = false
                                        filesViewModel.toggleShowHidden()
                                    }
                                )
                                HorizontalDivider()
                                DropdownMenuItem(
                                    text = { Text("Refresh") },
                                    leadingIcon = { Icon(Icons.Default.Refresh, contentDescription = null) },
                                    onClick = {
                                        isOptionsMenuExpanded = false
                                        filesViewModel.refreshCurrentDirectory()
                                    }
                                )
                            }
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surfaceContainer
                )
            )
        },
        floatingActionButton = {
            if (!isSelectionMode) {
                FloatingActionButton(
                    onClick = { showNewFolderDialog = true },
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary
                ) {
                    Icon(Icons.Default.Add, contentDescription = "Create Folder")
                }
            }
        },
        bottomBar = {
            // Multi-Selection Action Bar
            AnimatedVisibility(
                visible = isSelectionMode && selectedItemIds.isNotEmpty(),
                enter = slideInVertically(initialOffsetY = { it }),
                exit = slideOutVertically(targetOffsetY = { it })
            ) {
                Surface(
                    color = MaterialTheme.colorScheme.surfaceContainerHigh,
                    shadowElevation = 8.dp,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 12.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            IconButton(onClick = { filesViewModel.clearSelection() }) {
                                Icon(Icons.Default.Close, contentDescription = "Clear")
                            }
                            Text(
                                text = "${selectedItemIds.size} selected",
                                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold)
                            )
                        }

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            IconButton(onClick = { filesViewModel.selectAll() }) {
                                Icon(Icons.Default.SelectAll, contentDescription = "Select All")
                            }
                            IconButton(onClick = {
                                val selected = filesViewModel.getSelectedFiles()
                                if (selected.size == 1) {
                                    filesViewModel.shareFile(selected.first())
                                } else if (selected.isNotEmpty()) {
                                    // ZIP and share
                                    showZipDialog = true
                                }
                            }) {
                                Icon(Icons.Default.Share, contentDescription = "Share")
                            }
                            IconButton(onClick = { showZipDialog = true }) {
                                Icon(Icons.Default.Archive, contentDescription = "Compress to ZIP")
                            }
                            IconButton(onClick = { filesViewModel.cutSelection() }) {
                                Icon(Icons.Default.ContentCut, contentDescription = "Cut")
                            }
                            IconButton(onClick = { filesViewModel.copySelection() }) {
                                Icon(Icons.Default.ContentCopy, contentDescription = "Copy")
                            }
                            IconButton(onClick = { showDeleteSelectionConfirm = true }) {
                                Icon(
                                    imageVector = Icons.Default.Delete,
                                    contentDescription = "Delete",
                                    tint = MaterialTheme.colorScheme.error
                                )
                            }
                        }
                    }
                }
            }

            // Clipboard Paste Bar
            AnimatedVisibility(
                visible = clipboard != null && !isSelectionMode,
                enter = slideInVertically(initialOffsetY = { it }),
                exit = slideOutVertically(targetOffsetY = { it })
            ) {
                clipboard?.let { clip ->
                    Surface(
                        color = MaterialTheme.colorScheme.primaryContainer,
                        shadowElevation = 6.dp,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = if (clip.isCut) Icons.Default.DriveFileMove else Icons.Default.ContentPaste,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "${clip.items.size} items ready to ${if (clip.isCut) "move" else "paste"}",
                                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                                    color = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                            }

                            Row {
                                TextButton(onClick = { filesViewModel.clearClipboard() }) {
                                    Text("Cancel")
                                }
                                Button(onClick = { filesViewModel.pasteClipboard() }) {
                                    Text("Paste Here")
                                }
                            }
                        }
                    }
                }
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Storage Overview Card (collapsible when in search mode)
            if (!isSearchExpanded && selectedCategory == FileCategory.ALL) {
                StorageOverviewCard(
                    breakdown = storageBreakdown,
                    onOpenAnalyzer = {
                        filesViewModel.refreshStorageAnalytics()
                        showStorageAnalyzerScreen = true
                    }
                )
            }

            // Category Filter Pills
            CategoryFilterRow(
                selectedCategory = selectedCategory,
                onSelectCategory = { cat ->
                    if (cat == FileCategory.PRIVATE_VAULT && !isVaultUnlocked) {
                        showVaultPinDialog = true
                    } else if (cat == FileCategory.STORAGE_ANALYZER) {
                        filesViewModel.refreshStorageAnalytics()
                        showStorageAnalyzerScreen = true
                    } else {
                        filesViewModel.selectCategory(cat)
                    }
                }
            )

            // Breadcrumbs Navigation Bar (only in folder hierarchy view)
            if (selectedCategory == FileCategory.ALL) {
                BreadcrumbsBar(
                    history = directoryHistory,
                    onCrumbClick = { filesViewModel.navigateToBreadcrumb(it) }
                )
            }

            // Main Files List / Grid
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .weight(1f)
            ) {
                if (isLoading) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }
                } else if (displayFiles.isEmpty()) {
                    // Empty State
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(32.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.FolderOpen,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f),
                            modifier = Modifier.size(72.dp)
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = if (searchQuery.isNotEmpty()) "No files match '$searchQuery'" else "This folder is empty",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Create folders, text documents, or import files.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(20.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            OutlinedButton(onClick = { showNewFolderDialog = true }) {
                                Icon(Icons.Default.CreateNewFolder, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("New Folder")
                            }
                            Button(onClick = { showNewFileDialog = true }) {
                                Icon(Icons.Default.NoteAdd, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("New File")
                            }
                        }
                    }
                } else if (viewMode == ViewMode.GRID) {
                    LazyVerticalGrid(
                        columns = GridCells.Adaptive(minSize = 110.dp),
                        contentPadding = PaddingValues(8.dp),
                        modifier = Modifier.fillMaxSize()
                    ) {
                        items(displayFiles, key = { it.id }) { item ->
                            val isSelected = selectedItemIds.contains(item.id)
                            FileGridItem(
                                item = item,
                                isSelected = isSelected,
                                isSelectionMode = isSelectionMode,
                                onClick = {
                                    if (item.isDirectory) {
                                        filesViewModel.navigateToDirectory(item.file)
                                    } else {
                                        filesViewModel.openViewer(item)
                                    }
                                },
                                onLongClick = {
                                    if (!isSelectionMode) {
                                        filesViewModel.toggleSelectItem(item.id)
                                    }
                                },
                                onSelectToggle = {
                                    filesViewModel.toggleSelectItem(item.id)
                                },
                                onToggleFavorite = {
                                    filesViewModel.toggleFavorite(item)
                                }
                            )
                        }
                    }
                } else {
                    LazyColumn(
                        contentPadding = PaddingValues(vertical = 4.dp),
                        modifier = Modifier.fillMaxSize()
                    ) {
                        items(displayFiles, key = { it.id }) { item ->
                            val isSelected = selectedItemIds.contains(item.id)
                            FileListItem(
                                item = item,
                                isSelected = isSelected,
                                isSelectionMode = isSelectionMode,
                                onClick = {
                                    if (item.isDirectory) {
                                        filesViewModel.navigateToDirectory(item.file)
                                    } else {
                                        filesViewModel.openViewer(item)
                                    }
                                },
                                onLongClick = {
                                    if (!isSelectionMode) {
                                        filesViewModel.toggleSelectItem(item.id)
                                    }
                                },
                                onSelectToggle = {
                                    filesViewModel.toggleSelectItem(item.id)
                                },
                                onToggleFavorite = {
                                    filesViewModel.toggleFavorite(item)
                                },
                                onShare = {
                                    filesViewModel.shareFile(item)
                                },
                                onRename = {
                                    renamingItem = item
                                },
                                onDuplicate = {
                                    filesViewModel.duplicateFile(item)
                                },
                                onDelete = {
                                    deleteConfirmItem = item
                                },
                                onShowDetails = {
                                    detailsItem = item
                                },
                                onAiAction = {
                                    aiActionItem = item
                                },
                                onAttachToYaseen = {
                                    attachingItem = item
                                },
                                onMoveToVault = {
                                    filesViewModel.moveToPrivateVault(item)
                                }
                            )
                        }
                    }
                }
            }
        }
    }

    // --- Dialogs ---

    if (showNewFolderDialog) {
        SimpleInputDialog(
            title = "New Folder",
            placeholder = "Folder name",
            confirmButtonText = "Create",
            onConfirm = {
                filesViewModel.createFolder(it)
                showNewFolderDialog = false
            },
            onDismiss = { showNewFolderDialog = false }
        )
    }

    if (showNewFileDialog) {
        SimpleInputDialog(
            title = "New Text File",
            placeholder = "File name (e.g. notes.txt)",
            confirmButtonText = "Create",
            onConfirm = {
                filesViewModel.createTextFile(it)
                showNewFileDialog = false
            },
            onDismiss = { showNewFileDialog = false }
        )
    }

    renamingItem?.let { item ->
        SimpleInputDialog(
            title = "Rename",
            initialValue = item.name,
            placeholder = "New name",
            confirmButtonText = "Rename",
            onConfirm = {
                filesViewModel.renameFile(item, it)
                renamingItem = null
            },
            onDismiss = { renamingItem = null }
        )
    }

    if (showZipDialog) {
        SimpleInputDialog(
            title = "Create ZIP Archive",
            initialValue = "Archive_${System.currentTimeMillis() % 10000}.zip",
            placeholder = "Archive name",
            confirmButtonText = "Compress",
            onConfirm = {
                filesViewModel.zipSelectedFiles(it)
                showZipDialog = false
            },
            onDismiss = { showZipDialog = false }
        )
    }

    detailsItem?.let { item ->
        FileDetailsDialog(
            item = item,
            onDismiss = { detailsItem = null },
            onShare = { filesViewModel.shareFile(item) },
            onOpenWith = { filesViewModel.openWith(item) }
        )
    }

    attachingItem?.let { item ->
        AttachToYaseenDialog(
            fileItem = item,
            notes = notesList,
            tasks = tasksList,
            schedules = schedulesList,
            onAttachToNote = { note ->
                val updatedNote = note.copy(filePath = item.path)
                notesViewModel.saveNote(updatedNote)
                attachingItem = null
            },
            onAttachToTask = { task ->
                val desc = if (task.description.isBlank()) "Attachment: ${item.path}" else "${task.description}\nAttachment: ${item.path}"
                notesViewModel.addOrUpdateTask(task.copy(description = desc))
                attachingItem = null
            },
            onAttachToSchedule = { event ->
                val desc = if (event.description.isBlank()) "Attachment: ${item.path}" else "${event.description}\nAttachment: ${item.path}"
                notesViewModel.updateScheduleItem(event.copy(description = desc))
                attachingItem = null
            },
            onCreateNewNoteWithFile = {
                val newNote = Note(
                    title = item.file.nameWithoutExtension.ifEmpty { "New Note" },
                    content = if (item.isTextEditable) {
                        try { item.file.readText() } catch (_: Exception) { "" }
                    } else "",
                    filePath = item.path
                )
                notesViewModel.saveNote(newNote)
                attachingItem = null
                onNavigateToNoteEditor(newNote.id)
            },
            onDismiss = { attachingItem = null }
        )
    }

    aiActionItem?.let { item ->
        AiFileActionDialog(
            item = item,
            isLoading = isAiLoading,
            aiResult = aiResult,
            onRunAction = { action ->
                filesViewModel.runAiAction(item, action) {}
            },
            onSaveAsNote = { title, content ->
                val newNote = Note(
                    title = title,
                    content = content,
                    filePath = item.path
                )
                notesViewModel.saveNote(newNote)
                filesViewModel.clearAiResult()
                aiActionItem = null
            },
            onApplySuggestedName = { newName ->
                filesViewModel.renameFile(item, newName)
                aiActionItem = null
            },
            onDismiss = {
                filesViewModel.clearAiResult()
                aiActionItem = null
            }
        )
    }

    if (showVaultPinDialog) {
        PrivateVaultPinDialog(
            onUnlock = { pin ->
                val ok = filesViewModel.unlockVault(pin)
                if (ok) {
                    showVaultPinDialog = false
                }
                ok
            },
            onDismiss = { showVaultPinDialog = false }
        )
    }

    deleteConfirmItem?.let { item ->
        AlertDialog(
            onDismissRequest = { deleteConfirmItem = null },
            title = { Text("Delete File") },
            text = { Text("Are you sure you want to permanently delete '${item.name}'?") },
            confirmButton = {
                Button(
                    onClick = {
                        filesViewModel.deleteSingleFile(item)
                        deleteConfirmItem = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) {
                    Text("Delete")
                }
            },
            dismissButton = {
                TextButton(onClick = { deleteConfirmItem = null }) {
                    Text("Cancel")
                }
            }
        )
    }

    if (showDeleteSelectionConfirm) {
        AlertDialog(
            onDismissRequest = { showDeleteSelectionConfirm = false },
            title = { Text("Delete Selected Items") },
            text = { Text("Are you sure you want to permanently delete ${selectedItemIds.size} selected items?") },
            confirmButton = {
                Button(
                    onClick = {
                        filesViewModel.deleteSelectedFiles()
                        showDeleteSelectionConfirm = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) {
                    Text("Delete All")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteSelectionConfirm = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}
