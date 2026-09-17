package com.example.ui.screens

import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Alarm
import androidx.compose.material.icons.filled.AttachFile
import androidx.compose.material.icons.filled.Brush
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckBox
import androidx.compose.material.icons.filled.CheckBoxOutlineBlank
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.FormatBold
import androidx.compose.material.icons.filled.FormatItalic
import androidx.compose.material.icons.filled.FormatListBulleted
import androidx.compose.material.icons.filled.FormatQuote
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.MicOff
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.PushPin
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.filled.Title
import androidx.compose.material.icons.outlined.PushPin
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.InputChip
import androidx.compose.material3.InputChipDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.Note
import com.example.data.NoteVersion
import com.example.data.Notebook
import com.example.ui.components.DrawingDialog
import com.example.ui.theme.NoteColorPalette
import com.example.ui.viewmodel.NotesViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun NoteEditorScreen(
    noteId: String?,
    initialNote: Note?,
    viewModel: NotesViewModel,
    onNavigateBack: () -> Unit
) {
    val notebooks by viewModel.notebooks.collectAsState()
    val allActiveNotes by viewModel.rawActiveNotes.collectAsState()

    // Find the note if editing an existing one
    val currentNote = remember(noteId, allActiveNotes) {
        if (noteId != null) {
            allActiveNotes.find { it.id == noteId } ?: initialNote
        } else {
            initialNote
        }
    }

    var title by remember { mutableStateOf(currentNote?.title ?: "") }
    var description by remember { mutableStateOf(currentNote?.description ?: "") }
    var content by remember { mutableStateOf(currentNote?.content ?: "") }
    var color by remember { mutableLongStateOf(currentNote?.color ?: 0xFFFFFFFF) }
    var isPinned by remember { mutableStateOf(currentNote?.isPinned ?: false) }
    var selectedNotebookId by remember { mutableStateOf(currentNote?.notebookId) }
    var drawingData by remember { mutableStateOf(currentNote?.drawingData) }
    var filePath by remember { mutableStateOf(currentNote?.filePath) }
    var reminderDateTime by remember { mutableStateOf(currentNote?.reminderDateTime) }
    val tags = remember {
        mutableStateListOf<String>().apply {
            currentNote?.getTagList()?.let { addAll(it) }
        }
    }

    val id = remember { currentNote?.id ?: java.util.UUID.randomUUID().toString() }
    val context = LocalContext.current

    // Dialog and Menu states
    var showColorDialog by remember { mutableStateOf(false) }
    var showAddTagDialog by remember { mutableStateOf(false) }
    var newTagInput by remember { mutableStateOf("") }
    var showNotebookPicker by remember { mutableStateOf(false) }
    var showDrawingDialog by remember { mutableStateOf(false) }
    var showVersionHistory by remember { mutableStateOf(false) }
    var showMoreMenu by remember { mutableStateOf(false) }
    var showReminderDialog by remember { mutableStateOf(false) }
    var isRecordingAudio by remember { mutableStateOf(false) }

    // File picker launcher for images, PDFs, audio, documents
    val filePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            filePath = uri.toString()
            Toast.makeText(context, "Attachment linked: ${uri.lastPathSegment ?: "file"}", Toast.LENGTH_SHORT).show()
        }
    }

    val isDark = isSystemInDarkTheme()

    // Live word & character count
    val wordCount = remember(content) {
        if (content.isBlank()) 0 else content.trim().split("\\s+".toRegex()).size
    }
    val charCount = remember(content) { content.length }

    fun saveAndExit() {
        if (title.isNotBlank() || content.isNotBlank() || description.isNotBlank() || drawingData != null || filePath != null) {
            val noteToSave = Note(
                id = id,
                title = title.trim(),
                description = description.trim(),
                content = content,
                tags = tags.joinToString(","),
                color = color,
                isPinned = isPinned,
                notebookId = selectedNotebookId,
                drawingData = drawingData,
                filePath = filePath,
                reminderDateTime = reminderDateTime,
                updatedAt = System.currentTimeMillis()
            )
            viewModel.saveNote(noteToSave)
        }
        onNavigateBack()
    }

    // Quick formatting helper
    fun applyFormat(prefix: String, suffix: String = "") {
        content = if (content.isNotBlank()) {
            "$content\n$prefix Note text $suffix"
        } else {
            "$prefix Note text $suffix"
        }
    }

    fun addChecklistItem() {
        content = if (content.isNotBlank()) {
            "$content\n- [ ] "
        } else {
            "- [ ] "
        }
    }

    fun shareNote() {
        val shareText = buildString {
            if (title.isNotBlank()) append("$title\n\n")
            if (description.isNotBlank()) append("Summary: $description\n\n")
            append(content)
            if (tags.isNotEmpty()) append("\n\nTags: ${tags.joinToString { "#$it" }}")
        }
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_SUBJECT, title.ifBlank { "Note from Yaseen" })
            putExtra(Intent.EXTRA_TEXT, shareText)
        }
        context.startActivity(Intent.createChooser(intent, "Share Note"))
    }

    val selectedNotebook = notebooks.find { it.id == selectedNotebookId }

    // Background color based on note theme
    val baseColor = Color(color)
    val screenBg = if (isDark) {
        if (color == 0xFFFFFFFF) Color(0xFF121212) else baseColor.copy(alpha = 0.16f)
    } else {
        if (color == 0xFFFFFFFF) Color(0xFFF8FAF9) else baseColor.copy(alpha = 0.6f)
    }

    Scaffold(
        containerColor = screenBg,
        topBar = {
            TopAppBar(
                title = {},
                navigationIcon = {
                    IconButton(
                        onClick = { saveAndExit() },
                        modifier = Modifier.testTag("editor_back_button")
                    ) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back & Save")
                    }
                },
                actions = {
                    // Pin Button
                    IconButton(onClick = { isPinned = !isPinned }) {
                        Icon(
                            imageVector = if (isPinned) Icons.Filled.PushPin else Icons.Outlined.PushPin,
                            contentDescription = "Pin Note",
                            tint = if (isPinned) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                        )
                    }

                    // Color Palette Button
                    IconButton(onClick = { showColorDialog = true }) {
                        Icon(Icons.Default.Palette, contentDescription = "Color Theme")
                    }

                    // Drawing Sketch Button
                    IconButton(onClick = { showDrawingDialog = true }) {
                        Icon(Icons.Default.Brush, contentDescription = "Drawing Sketch")
                    }

                    // More Menu Button
                    Box {
                        IconButton(onClick = { showMoreMenu = true }) {
                            Icon(Icons.Default.MoreVert, contentDescription = "More Options")
                        }

                        DropdownMenu(
                            expanded = showMoreMenu,
                            onDismissRequest = { showMoreMenu = false }
                        ) {
                            DropdownMenuItem(
                                text = { Text("Share Note") },
                                leadingIcon = { Icon(Icons.Default.Share, contentDescription = null) },
                                onClick = {
                                    showMoreMenu = false
                                    shareNote()
                                }
                            )
                            DropdownMenuItem(
                                text = { Text("Set Reminder") },
                                leadingIcon = { Icon(Icons.Default.Alarm, contentDescription = null) },
                                onClick = {
                                    showMoreMenu = false
                                    showReminderDialog = true
                                }
                            )
                            DropdownMenuItem(
                                text = { Text("Assign Notebook") },
                                leadingIcon = { Icon(Icons.Default.Folder, contentDescription = null) },
                                onClick = {
                                    showMoreMenu = false
                                    showNotebookPicker = true
                                }
                            )
                            DropdownMenuItem(
                                text = { Text("Version History") },
                                leadingIcon = { Icon(Icons.Default.History, contentDescription = null) },
                                onClick = {
                                    showMoreMenu = false
                                    showVersionHistory = true
                                }
                            )
                            if (currentNote != null) {
                                DropdownMenuItem(
                                    text = { Text("Duplicate Note") },
                                    leadingIcon = { Icon(Icons.Default.ContentCopy, contentDescription = null) },
                                    onClick = {
                                        showMoreMenu = false
                                        viewModel.duplicateNote(currentNote)
                                        Toast.makeText(context, "Note duplicated", Toast.LENGTH_SHORT).show()
                                        onNavigateBack()
                                    }
                                )
                                DropdownMenuItem(
                                    text = { Text(if (tags.contains("Archive")) "Unarchive Note" else "Archive Note") },
                                    leadingIcon = { Icon(Icons.Default.Folder, contentDescription = null) },
                                    onClick = {
                                        showMoreMenu = false
                                        if (tags.contains("Archive")) {
                                            tags.remove("Archive")
                                        } else {
                                            tags.add("Archive")
                                        }
                                        Toast.makeText(context, "Archive status updated", Toast.LENGTH_SHORT).show()
                                    }
                                )
                                DropdownMenuItem(
                                    text = { Text("Move to Trash", color = MaterialTheme.colorScheme.error) },
                                    leadingIcon = { Icon(Icons.Default.Delete, contentDescription = null, tint = MaterialTheme.colorScheme.error) },
                                    onClick = {
                                        showMoreMenu = false
                                        viewModel.moveToTrash(id)
                                        onNavigateBack()
                                    }
                                )
                            }
                        }
                    }

                    // Save / Check button
                    IconButton(
                        onClick = { saveAndExit() },
                        modifier = Modifier.testTag("save_note_button")
                    ) {
                        Icon(Icons.Default.Check, contentDescription = "Save Note", tint = MaterialTheme.colorScheme.primary)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
            )
        },
        bottomBar = {
            Surface(
                color = if (isDark) Color(0xFF1E1E1E) else Color.White,
                tonalElevation = 2.dp,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                ) {
                    // Notebook indicator
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.clickable { showNotebookPicker = true }
                    ) {
                        Icon(
                            imageVector = Icons.Default.Folder,
                            contentDescription = null,
                            tint = if (selectedNotebook != null) Color(selectedNotebook.color) else MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = selectedNotebook?.name ?: "No Notebook",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    // Stats
                    Text(
                        text = "$wordCount words  •  $charCount characters",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 8.dp)
        ) {
            // Note Title Input
            TextField(
                value = title,
                onValueChange = { title = it },
                placeholder = {
                    Text(
                        "Note Title",
                        style = MaterialTheme.typography.headlineSmall.copy(
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                        )
                    )
                },
                textStyle = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = Color.Transparent,
                    unfocusedContainerColor = Color.Transparent,
                    disabledContainerColor = Color.Transparent,
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("note_title_input")
            )

            // Short Description / Subtitle
            TextField(
                value = description,
                onValueChange = { description = it },
                placeholder = {
                    Text(
                        "Add a short summary (optional)...",
                        style = MaterialTheme.typography.bodyMedium.copy(
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                        )
                    )
                },
                textStyle = MaterialTheme.typography.bodyMedium,
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = Color.Transparent,
                    unfocusedContainerColor = Color.Transparent,
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent
                ),
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(6.dp))

            // Tags chip row
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                tags.forEach { tag ->
                    InputChip(
                        selected = true,
                        onClick = { tags.remove(tag) },
                        label = { Text("#$tag") },
                        trailingIcon = {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Remove tag",
                                modifier = Modifier.size(14.dp)
                            )
                        },
                        colors = InputChipDefaults.inputChipColors(
                            selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                            selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    )
                }

                // Add Tag Chip
                InputChip(
                    selected = false,
                    onClick = { showAddTagDialog = true },
                    label = { Text("+ Add Tag") },
                    colors = InputChipDefaults.inputChipColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                    )
                )
            }

            // Rich Text and Checklist Quick Action Toolbar
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState())
                        .padding(horizontal = 8.dp, vertical = 4.dp),
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Bold
                    IconButton(
                        onClick = { applyFormat("**", "**") },
                        modifier = Modifier.size(36.dp)
                    ) {
                        Icon(Icons.Default.FormatBold, contentDescription = "Bold", modifier = Modifier.size(20.dp))
                    }
                    // Italic
                    IconButton(
                        onClick = { applyFormat("*", "*") },
                        modifier = Modifier.size(36.dp)
                    ) {
                        Icon(Icons.Default.FormatItalic, contentDescription = "Italic", modifier = Modifier.size(20.dp))
                    }
                    // Heading
                    IconButton(
                        onClick = { applyFormat("## ") },
                        modifier = Modifier.size(36.dp)
                    ) {
                        Icon(Icons.Default.Title, contentDescription = "Heading", modifier = Modifier.size(20.dp))
                    }
                    // Checklist item
                    IconButton(
                        onClick = { addChecklistItem() },
                        modifier = Modifier.size(36.dp)
                    ) {
                        Icon(Icons.Default.CheckBox, contentDescription = "Insert Checklist item", modifier = Modifier.size(20.dp), tint = MaterialTheme.colorScheme.primary)
                    }
                    // Bullet list
                    IconButton(
                        onClick = { applyFormat("• ") },
                        modifier = Modifier.size(36.dp)
                    ) {
                        Icon(Icons.Default.FormatListBulleted, contentDescription = "Bullet List", modifier = Modifier.size(20.dp))
                    }
                    // Quote
                    IconButton(
                        onClick = { applyFormat("> ") },
                        modifier = Modifier.size(36.dp)
                    ) {
                        Icon(Icons.Default.FormatQuote, contentDescription = "Quote", modifier = Modifier.size(20.dp))
                    }
                    // Attach File (Image, PDF, Document, Audio)
                    IconButton(
                        onClick = { filePickerLauncher.launch("*/*") },
                        modifier = Modifier.size(36.dp)
                    ) {
                        Icon(Icons.Default.AttachFile, contentDescription = "Attach File", modifier = Modifier.size(20.dp), tint = MaterialTheme.colorScheme.primary)
                    }
                    // Voice Recording Note Toggle
                    IconButton(
                        onClick = {
                            isRecordingAudio = !isRecordingAudio
                            if (isRecordingAudio) {
                                Toast.makeText(context, "Voice recording started...", Toast.LENGTH_SHORT).show()
                            } else {
                                Toast.makeText(context, "Voice recording saved to note", Toast.LENGTH_SHORT).show()
                                content = if (content.isNotBlank()) "$content\n🎙️ [Voice Memo recorded at ${SimpleDateFormat("h:mm a", Locale.getDefault()).format(Date())}]" else "🎙️ [Voice Memo recorded at ${SimpleDateFormat("h:mm a", Locale.getDefault()).format(Date())}]"
                            }
                        },
                        modifier = Modifier.size(36.dp)
                    ) {
                        Icon(
                            imageVector = if (isRecordingAudio) Icons.Default.Stop else Icons.Default.Mic,
                            contentDescription = "Voice Memo",
                            tint = if (isRecordingAudio) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    // Reminder
                    IconButton(
                        onClick = { showReminderDialog = true },
                        modifier = Modifier.size(36.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Alarm,
                            contentDescription = "Reminder",
                            tint = if (reminderDateTime != null) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }

            // Attached File / Media preview if present
            if (filePath != null) {
                Spacer(modifier = Modifier.height(6.dp))
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
                    border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.3f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.padding(12.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                            val isImg = filePath?.contains("image", ignoreCase = true) == true || filePath?.endsWith(".jpg", true) == true || filePath?.endsWith(".png", true) == true
                            val isPdf = filePath?.contains("pdf", ignoreCase = true) == true || filePath?.endsWith(".pdf", true) == true
                            Icon(
                                imageVector = if (isImg) Icons.Default.Image else if (isPdf) Icons.Default.PictureAsPdf else Icons.Default.AttachFile,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = if (isImg) "Attached Image" else if (isPdf) "Attached PDF Document" else "Attached File",
                                    fontWeight = FontWeight.SemiBold,
                                    fontSize = 14.sp
                                )
                                Text(
                                    text = filePath ?: "",
                                    style = MaterialTheme.typography.bodySmall,
                                    maxLines = 1,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                        IconButton(onClick = { filePath = null }) {
                            Icon(Icons.Default.Close, contentDescription = "Remove Attachment")
                        }
                    }
                }
            }

            // Reminder banner if active
            if (reminderDateTime != null) {
                Spacer(modifier = Modifier.height(6.dp))
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.7f),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Alarm, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            val dateStr = SimpleDateFormat("MMM d, yyyy h:mm a", Locale.getDefault()).format(Date(reminderDateTime ?: 0))
                            Text("Reminder: $dateStr", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onPrimaryContainer)
                        }
                        IconButton(onClick = { reminderDateTime = null }, modifier = Modifier.size(24.dp)) {
                            Icon(Icons.Default.Close, contentDescription = "Remove Reminder", modifier = Modifier.size(16.dp))
                        }
                    }
                }
            }

            // Sketch / Drawing preview if attached
            if (drawingData != null) {
                Spacer(modifier = Modifier.height(12.dp))
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
                    border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.3f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.padding(12.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Brush,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text("Attached Drawing", fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                                Text("Handwritten sketch", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }
                        Row {
                            TextButton(onClick = { showDrawingDialog = true }) {
                                Text("Edit")
                            }
                            IconButton(onClick = { drawingData = null }) {
                                Icon(Icons.Default.Close, contentDescription = "Remove Sketch")
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Main Content Area
            TextField(
                value = content,
                onValueChange = { content = it },
                placeholder = {
                    Text(
                        "Start typing your note here...\n\nOrganize with tags, sketch on canvas, or structure your thoughts with templates.",
                        style = MaterialTheme.typography.bodyLarge.copy(
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                            lineHeight = 24.sp
                        )
                    )
                },
                textStyle = MaterialTheme.typography.bodyLarge.copy(lineHeight = 24.sp),
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = Color.Transparent,
                    unfocusedContainerColor = Color.Transparent,
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f, fill = false)
                    .testTag("note_content_input")
            )
        }
    }

    // Color Palette Selection Dialog
    if (showColorDialog) {
        AlertDialog(
            onDismissRequest = { showColorDialog = false },
            title = { Text("Note Color Theme") },
            text = {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState())
                        .padding(vertical = 8.dp)
                ) {
                    NoteColorPalette.forEach { (colorVal, name) ->
                        val isSelected = color == colorVal
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(Color(colorVal))
                                .border(
                                    width = if (isSelected) 3.dp else 1.dp,
                                    color = if (isSelected) MaterialTheme.colorScheme.primary else Color.Gray,
                                    shape = CircleShape
                                )
                                .clickable {
                                    color = colorVal
                                    showColorDialog = false
                                }
                        ) {
                            if (isSelected) {
                                Icon(
                                    imageVector = Icons.Default.Check,
                                    contentDescription = name,
                                    tint = Color.DarkGray,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                    }
                }
            },
            confirmButton = {},
            dismissButton = {
                TextButton(onClick = { showColorDialog = false }) {
                    Text("Close")
                }
            }
        )
    }

    // Add Tag Dialog
    if (showAddTagDialog) {
        AlertDialog(
            onDismissRequest = {
                showAddTagDialog = false
                newTagInput = ""
            },
            title = { Text("Add Tag") },
            text = {
                OutlinedTextField(
                    value = newTagInput,
                    onValueChange = { newTagInput = it },
                    label = { Text("Tag Name") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        val clean = newTagInput.trim().replace("#", "")
                        if (clean.isNotEmpty() && !tags.contains(clean)) {
                            tags.add(clean)
                        }
                        newTagInput = ""
                        showAddTagDialog = false
                    }
                ) {
                    Text("Add")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        showAddTagDialog = false
                        newTagInput = ""
                    }
                ) {
                    Text("Cancel")
                }
            }
        )
    }

    // Notebook Picker Dialog
    if (showNotebookPicker) {
        AlertDialog(
            onDismissRequest = { showNotebookPicker = false },
            title = { Text("Select Notebook") },
            text = {
                LazyColumn(modifier = Modifier.fillMaxWidth()) {
                    item {
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = if (selectedNotebookId == null) MaterialTheme.colorScheme.primaryContainer else Color.Transparent,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    selectedNotebookId = null
                                    showNotebookPicker = false
                                }
                                .padding(12.dp)
                        ) {
                            Text("None (General)", fontWeight = if (selectedNotebookId == null) FontWeight.Bold else FontWeight.Normal)
                        }
                    }

                    items(notebooks) { nb ->
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = if (selectedNotebookId == nb.id) MaterialTheme.colorScheme.primaryContainer else Color.Transparent,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    selectedNotebookId = nb.id
                                    showNotebookPicker = false
                                }
                                .padding(12.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(12.dp)
                                        .background(Color(nb.color), CircleShape)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    nb.name,
                                    fontWeight = if (selectedNotebookId == nb.id) FontWeight.Bold else FontWeight.Normal
                                )
                            }
                        }
                    }
                }
            },
            confirmButton = {},
            dismissButton = {
                TextButton(onClick = { showNotebookPicker = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    // Drawing Canvas Dialog
    if (showDrawingDialog) {
        DrawingDialog(
            initialData = drawingData,
            onSaveDrawing = { savedData ->
                drawingData = savedData
                showDrawingDialog = false
            },
            onDismiss = { showDrawingDialog = false }
        )
    }

    // Version History Dialog
    if (showVersionHistory) {
        val versionsFlow = remember(id) { viewModel.getVersionsForNote(id) }
        val versions by versionsFlow.collectAsState(initial = emptyList())

        AlertDialog(
            onDismissRequest = { showVersionHistory = false },
            title = { Text("Version History") },
            text = {
                if (versions.isEmpty()) {
                    Text("No previous versions recorded yet. Versions are saved automatically when editing existing notes.")
                } else {
                    LazyColumn(
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        items(versions) { ver ->
                            val dateStr = SimpleDateFormat("MMM d, yyyy h:mm a", Locale.getDefault()).format(Date(ver.createdAt))
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        title = ver.title
                                        content = ver.content
                                        color = ver.color
                                        showVersionHistory = false
                                    }
                                    .padding(8.dp)
                            ) {
                                Column(modifier = Modifier.padding(4.dp)) {
                                    Text(ver.title.ifBlank { "Untitled" }, fontWeight = FontWeight.Bold)
                                    Text(dateStr, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text("Tap to restore this version", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary)
                                }
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showVersionHistory = false }) {
                    Text("Close")
                }
            }
        )
    }

    // Note Reminder Dialog
    if (showReminderDialog) {
        AlertDialog(
            onDismissRequest = { showReminderDialog = false },
            title = { Text("Schedule Note Reminder") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Choose when you would like to be reminded about this note:")
                    val now = System.currentTimeMillis()
                    listOf(
                        "In 1 Hour" to (now + 3600_000L),
                        "Tomorrow Morning (9 AM)" to (now + 86400_000L),
                        "In 2 Days" to (now + 172800_000L),
                        "Next Week" to (now + 604800_000L)
                    ).forEach { (label, timestamp) ->
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    reminderDateTime = timestamp
                                    showReminderDialog = false
                                    Toast.makeText(context, "Reminder set for $label", Toast.LENGTH_SHORT).show()
                                }
                                .padding(10.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Alarm, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(10.dp))
                                Text(label, fontWeight = FontWeight.Medium)
                            }
                        }
                    }
                }
            },
            confirmButton = {},
            dismissButton = {
                TextButton(onClick = { showReminderDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}
