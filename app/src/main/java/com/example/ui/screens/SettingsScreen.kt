package com.example.ui.screens

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.ColorLens
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.SmartToy
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material.icons.filled.Title
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.AppSettings
import com.example.ui.theme.IconThemeHelper
import com.example.ui.viewmodel.NotesViewModel

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun SettingsScreen(
    viewModel: NotesViewModel,
    onOpenDrawer: () -> Unit,
    onNavigateToBackup: () -> Unit
) {
    val settings by viewModel.settings.collectAsState()
    val isDark = isSystemInDarkTheme()
    val context = LocalContext.current
    val focusManager = LocalFocusManager.current

    val currentPack = IconThemeHelper.getPack(settings.iconTheme)

    // Local form state
    var appNameInput by remember(settings.appName) { mutableStateOf(settings.appName) }
    var notesModuleNameInput by remember(settings.notesModuleName) { mutableStateOf(settings.notesModuleName) }
    var tasksModuleNameInput by remember(settings.tasksModuleName) { mutableStateOf(settings.tasksModuleName) }
    var scheduleModuleNameInput by remember(settings.scheduleModuleName) { mutableStateOf(settings.scheduleModuleName) }
    var filesModuleNameInput by remember(settings.filesModuleName) { mutableStateOf(settings.filesModuleName) }
    var chatbotModuleNameInput by remember(settings.chatbotModuleName) { mutableStateOf(settings.chatbotModuleName) }

    var selectedTheme by remember(settings.iconTheme) { mutableStateOf(settings.iconTheme) }
    var selectedProvider by remember(settings.aiProvider) { mutableStateOf(settings.aiProvider) }

    var geminiKeyInput by remember(settings.geminiApiKey) { mutableStateOf(settings.geminiApiKey) }
    var openAiKeyInput by remember(settings.openAiApiKey) { mutableStateOf(settings.openAiApiKey) }
    var openRouterKeyInput by remember(settings.openRouterApiKey) { mutableStateOf(settings.openRouterApiKey) }

    var geminiModelInput by remember(settings.geminiModel) { mutableStateOf(settings.geminiModel) }
    var openAiModelInput by remember(settings.openAiModel) { mutableStateOf(settings.openAiModel) }
    var openRouterModelInput by remember(settings.openRouterModel) { mutableStateOf(settings.openRouterModel) }

    var systemPromptInput by remember(settings.systemPrompt) { mutableStateOf(settings.systemPrompt) }
    var defaultTabInput by remember(settings.defaultTab) { mutableStateOf(settings.defaultTab) }
    var confirmDeleteInput by remember(settings.confirmDelete) { mutableStateOf(settings.confirmDelete) }

    var securityLockEnabled by remember(settings.securityLockEnabled) { mutableStateOf(settings.securityLockEnabled) }
    var securityPinCode by remember(settings.securityPinCode) { mutableStateOf(settings.securityPinCode) }
    var notificationsSound by remember(settings.notificationsSound) { mutableStateOf(settings.notificationsSound) }
    var notificationsVibrate by remember(settings.notificationsVibrate) { mutableStateOf(settings.notificationsVibrate) }

    var showKeyVisibility by remember { mutableStateOf(false) }

    fun saveAllChanges() {
        val updated = settings.copy(
            appName = appNameInput.trim().ifBlank { "Yaseen" },
            notesModuleName = notesModuleNameInput.trim().ifBlank { "Yaseen Notes" },
            tasksModuleName = tasksModuleNameInput.trim().ifBlank { "Yaseen Tasks" },
            scheduleModuleName = scheduleModuleNameInput.trim().ifBlank { "Yaseen Schedule" },
            filesModuleName = filesModuleNameInput.trim().ifBlank { "${appNameInput.trim().ifBlank { "Yaseen" }} Files" },
            chatbotModuleName = chatbotModuleNameInput.trim().ifBlank { "YaRVerse" },
            iconTheme = selectedTheme,
            aiProvider = selectedProvider,
            geminiApiKey = geminiKeyInput.trim(),
            openAiApiKey = openAiKeyInput.trim(),
            openRouterApiKey = openRouterKeyInput.trim(),
            geminiModel = geminiModelInput.trim().ifBlank { "gemini-3.5-flash" },
            openAiModel = openAiModelInput.trim().ifBlank { "gpt-4o-mini" },
            openRouterModel = openRouterModelInput.trim().ifBlank { "openrouter/auto" },
            systemPrompt = systemPromptInput.trim(),
            defaultTab = defaultTabInput,
            confirmDelete = confirmDeleteInput,
            securityLockEnabled = securityLockEnabled,
            securityPinCode = securityPinCode.trim(),
            notificationsSound = notificationsSound,
            notificationsVibrate = notificationsVibrate
        )
        viewModel.updateSettings(updated)
        Toast.makeText(context, "Settings saved successfully", Toast.LENGTH_SHORT).show()
        focusManager.clearFocus()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = currentPack.primaryColor,
                            modifier = Modifier.size(34.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = currentPack.settingsIcon,
                                    contentDescription = "Settings Logo",
                                    tint = Color.White,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = "Settings & Controls",
                            style = MaterialTheme.typography.titleLarge.copy(
                                fontWeight = FontWeight.Bold,
                                fontSize = 20.sp
                            )
                        )
                    }
                },
                navigationIcon = {
                    IconButton(
                        onClick = onOpenDrawer,
                        modifier = Modifier.testTag("settings_menu_button")
                    ) {
                        Icon(Icons.Default.Menu, contentDescription = "Open Drawer")
                    }
                },
                actions = {
                    IconButton(onClick = { saveAllChanges() }) {
                        Icon(Icons.Default.Save, contentDescription = "Save Settings", tint = currentPack.primaryColor)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = if (isDark) Color(0xFF121212) else Color(0xFFF8FAF9)
                )
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            // Section 1: App Identity (App Name Change-able)
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = if (isDark) Color(0xFF1E1E1E) else Color.White),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Title, contentDescription = null, tint = currentPack.primaryColor)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("App Name & Branding", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))
                    }
                    Text(
                        "Change the app display title across the entire suite (Notes, Tasks, Schedule, Drawer).",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedTextField(
                        value = appNameInput,
                        onValueChange = { appNameInput = it },
                        label = { Text("App Name") },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                        keyboardActions = KeyboardActions(onDone = { saveAllChanges() }),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = currentPack.primaryColor
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("app_name_input")
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    // Preset tags
                    Text("Quick Presets:", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        listOf("Yaseen", "Yaseen Notes", "Yaseen Suite", "Yaseen Pro").forEach { preset ->
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = if (appNameInput == preset) currentPack.primaryColor.copy(alpha = 0.2f) else MaterialTheme.colorScheme.surfaceVariant,
                                modifier = Modifier.clickable {
                                    appNameInput = preset
                                    saveAllChanges()
                                }
                            ) {
                                Text(
                                    text = preset,
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontWeight = if (appNameInput == preset) FontWeight.Bold else FontWeight.Normal,
                                        color = if (appNameInput == preset) currentPack.primaryColor else MaterialTheme.colorScheme.onSurfaceVariant
                                    ),
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))
                    HorizontalDivider()
                    Spacer(modifier = Modifier.height(14.dp))

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Folder, contentDescription = null, tint = currentPack.primaryColor, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Module Names", style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold))
                    }
                    Text(
                        "Rename individual sections to match your workflow or preferred terminology.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedTextField(
                        value = notesModuleNameInput,
                        onValueChange = { notesModuleNameInput = it },
                        label = { Text("Notes Module Name") },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = currentPack.primaryColor),
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedTextField(
                        value = tasksModuleNameInput,
                        onValueChange = { tasksModuleNameInput = it },
                        label = { Text("Tasks Module Name") },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = currentPack.primaryColor),
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedTextField(
                        value = scheduleModuleNameInput,
                        onValueChange = { scheduleModuleNameInput = it },
                        label = { Text("Schedule Module Name") },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = currentPack.primaryColor),
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedTextField(
                        value = filesModuleNameInput,
                        onValueChange = { filesModuleNameInput = it },
                        label = { Text("Files Module Name") },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = currentPack.primaryColor),
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedTextField(
                        value = chatbotModuleNameInput,
                        onValueChange = { chatbotModuleNameInput = it },
                        label = { Text("AI Assistant / Chatbot Name") },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = currentPack.primaryColor),
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }

            // Section 2: All Icons Change-able (Icon Theme Packs)
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = if (isDark) Color(0xFF1E1E1E) else Color.White),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Palette, contentDescription = null, tint = currentPack.primaryColor)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("App Icons & Theme Pack", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))
                    }
                    Text(
                        "Change all icons across navigation tabs, drawer items, and section badges.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    IconThemeHelper.availablePacks.forEach { pack ->
                        val isPackSelected = selectedTheme == pack.name
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = if (isPackSelected) pack.primaryColor.copy(alpha = 0.12f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                            border = if (isPackSelected) androidx.compose.foundation.BorderStroke(1.5.dp, pack.primaryColor) else null,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp)
                                .clickable {
                                    selectedTheme = pack.name
                                    saveAllChanges()
                                }
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween,
                                modifier = Modifier.padding(12.dp)
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Box(
                                        modifier = Modifier
                                            .size(14.dp)
                                            .background(pack.primaryColor, CircleShape)
                                    )
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Column {
                                        Text(pack.name, style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold))
                                        Text(pack.description, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    }
                                }

                                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    Icon(pack.notesIcon, contentDescription = null, tint = pack.primaryColor, modifier = Modifier.size(18.dp))
                                    Icon(pack.tasksIcon, contentDescription = null, tint = pack.primaryColor, modifier = Modifier.size(18.dp))
                                    Icon(pack.scheduleIcon, contentDescription = null, tint = pack.primaryColor, modifier = Modifier.size(18.dp))
                                    Icon(pack.chatbotIcon, contentDescription = null, tint = pack.primaryColor, modifier = Modifier.size(18.dp))
                                    if (isPackSelected) {
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Icon(Icons.Default.Check, contentDescription = "Active", tint = pack.primaryColor, modifier = Modifier.size(18.dp))
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // Section 3: YaRVerse Chatbot API Key & Multi-Provider Options
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = if (isDark) Color(0xFF1E1E1E) else Color.White),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.SmartToy, contentDescription = null, tint = currentPack.primaryColor)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Chatbot: YaRVerse API Configuration", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))
                    }
                    Text(
                        "YaRVerse runs using your choice of API provider. Choose a provider below and enter your API key.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // Provider Toggle Tabs
                    Text("Select AI Provider:", style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold))
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        listOf("GEMINI" to "Google Gemini", "OPENAI" to "OpenAI", "OPENROUTER" to "OpenRouter").forEach { (code, label) ->
                            val isChosen = selectedProvider == code
                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = if (isChosen) currentPack.primaryColor else MaterialTheme.colorScheme.surfaceVariant,
                                modifier = Modifier
                                    .weight(1f)
                                    .clickable {
                                        selectedProvider = code
                                        saveAllChanges()
                                    }
                            ) {
                                Box(
                                    contentAlignment = Alignment.Center,
                                    modifier = Modifier.padding(vertical = 10.dp, horizontal = 4.dp)
                                ) {
                                    Text(
                                        text = label,
                                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                        color = if (isChosen) Color.White else MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // API Key Input for selected provider
                    val activeKeyInput = when (selectedProvider) {
                        "GEMINI" -> geminiKeyInput
                        "OPENAI" -> openAiKeyInput
                        "OPENROUTER" -> openRouterKeyInput
                        else -> geminiKeyInput
                    }

                    OutlinedTextField(
                        value = activeKeyInput,
                        onValueChange = { newVal ->
                            when (selectedProvider) {
                                "GEMINI" -> geminiKeyInput = newVal
                                "OPENAI" -> openAiKeyInput = newVal
                                "OPENROUTER" -> openRouterKeyInput = newVal
                            }
                        },
                        label = { Text("$selectedProvider API Key *") },
                        placeholder = { Text("Enter your $selectedProvider key...") },
                        singleLine = true,
                        visualTransformation = if (showKeyVisibility) VisualTransformation.None else PasswordVisualTransformation(),
                        trailingIcon = {
                            IconButton(onClick = { showKeyVisibility = !showKeyVisibility }) {
                                Icon(
                                    imageVector = if (showKeyVisibility) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                    contentDescription = "Toggle Key Visibility"
                                )
                            }
                        },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = currentPack.primaryColor
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("api_key_input")
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    // Model Selection for Selected Provider
                    Text("Model Selection:", style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold))
                    Spacer(modifier = Modifier.height(6.dp))

                    when (selectedProvider) {
                        "GEMINI" -> {
                            val geminiModels = listOf("gemini-3.5-flash", "gemini-3.1-pro-preview")
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                geminiModels.forEach { m ->
                                    val isSel = geminiModelInput == m
                                    Surface(
                                        shape = RoundedCornerShape(8.dp),
                                        color = if (isSel) currentPack.primaryColor.copy(alpha = 0.2f) else MaterialTheme.colorScheme.surfaceVariant,
                                        modifier = Modifier.clickable { geminiModelInput = m }
                                    ) {
                                        Text(
                                            text = m,
                                            style = MaterialTheme.typography.labelSmall.copy(
                                                color = if (isSel) currentPack.primaryColor else MaterialTheme.colorScheme.onSurfaceVariant,
                                                fontWeight = if (isSel) FontWeight.Bold else FontWeight.Normal
                                            ),
                                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                                        )
                                    }
                                }
                            }
                        }
                        "OPENAI" -> {
                            val openAiModels = listOf("gpt-4o-mini", "gpt-4o")
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                openAiModels.forEach { m ->
                                    val isSel = openAiModelInput == m
                                    Surface(
                                        shape = RoundedCornerShape(8.dp),
                                        color = if (isSel) currentPack.primaryColor.copy(alpha = 0.2f) else MaterialTheme.colorScheme.surfaceVariant,
                                        modifier = Modifier.clickable { openAiModelInput = m }
                                    ) {
                                        Text(
                                            text = m,
                                            style = MaterialTheme.typography.labelSmall.copy(
                                                color = if (isSel) currentPack.primaryColor else MaterialTheme.colorScheme.onSurfaceVariant,
                                                fontWeight = if (isSel) FontWeight.Bold else FontWeight.Normal
                                            ),
                                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                                        )
                                    }
                                }
                            }
                        }
                        "OPENROUTER" -> {
                            val openRouterModels = listOf("openrouter/auto", "deepseek/deepseek-chat", "meta-llama/llama-3.3-70b-instruct")
                            FlowRow(
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                openRouterModels.forEach { m ->
                                    val isSel = openRouterModelInput == m
                                    Surface(
                                        shape = RoundedCornerShape(8.dp),
                                        color = if (isSel) currentPack.primaryColor.copy(alpha = 0.2f) else MaterialTheme.colorScheme.surfaceVariant,
                                        modifier = Modifier.clickable { openRouterModelInput = m }
                                    ) {
                                        Text(
                                            text = m,
                                            style = MaterialTheme.typography.labelSmall.copy(
                                                color = if (isSel) currentPack.primaryColor else MaterialTheme.colorScheme.onSurfaceVariant,
                                                fontWeight = if (isSel) FontWeight.Bold else FontWeight.Normal
                                            ),
                                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // System Prompt Editor
                    OutlinedTextField(
                        value = systemPromptInput,
                        onValueChange = { systemPromptInput = it },
                        label = { Text("YaRVerse Persona & System Prompt") },
                        maxLines = 3,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }

            // Section 4: More Controls & Preferences
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = if (isDark) Color(0xFF1E1E1E) else Color.White),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Tune, contentDescription = null, tint = currentPack.primaryColor)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("App Preferences & Controls", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Default Tab Selector
                    Text("Default Launch Tab:", style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold))
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        listOf(
                            "NOTES" to "Notes",
                            "TASKS" to "Tasks",
                            "SCHEDULE" to "Schedule",
                            "FILES" to "Files",
                            "CHATBOT" to "YaRVerse"
                        ).forEach { (tabCode, tabName) ->
                            val isTabChosen = defaultTabInput == tabCode
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = if (isTabChosen) currentPack.primaryColor else MaterialTheme.colorScheme.surfaceVariant,
                                modifier = Modifier
                                    .weight(1f)
                                    .clickable {
                                        defaultTabInput = tabCode
                                        saveAllChanges()
                                    }
                            ) {
                                Box(
                                    contentAlignment = Alignment.Center,
                                    modifier = Modifier.padding(vertical = 8.dp)
                                ) {
                                    Text(
                                        text = tabName,
                                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                        color = if (isTabChosen) Color.White else MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Confirm Delete Switch
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Confirm on Deleting", style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold))
                            Text("Show prompt before emptying trash or removing items", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        Switch(
                            checked = confirmDeleteInput,
                            onCheckedChange = {
                                confirmDeleteInput = it
                                saveAllChanges()
                            }
                        )
                    }
                }
            }

            // Section 5: Security & App Lock
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = if (isDark) Color(0xFF1E1E1E) else Color.White),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Security, contentDescription = null, tint = currentPack.primaryColor)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Security & Privacy Lock", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))
                    }
                    Text(
                        "Protect your private notes, tasks, and schedule with local passcode protection.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Enable App Lock", style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold))
                            Text("Require PIN to open app or view locked notes", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        Switch(
                            checked = securityLockEnabled,
                            onCheckedChange = {
                                securityLockEnabled = it
                                saveAllChanges()
                            }
                        )
                    }

                    if (securityLockEnabled) {
                        Spacer(modifier = Modifier.height(10.dp))
                        OutlinedTextField(
                            value = securityPinCode,
                            onValueChange = { if (it.length <= 6) securityPinCode = it },
                            label = { Text("Security PIN (4–6 digits)") },
                            singleLine = true,
                            visualTransformation = PasswordVisualTransformation(),
                            colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = currentPack.primaryColor),
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            }

            // Section 6: Notification Preferences
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = if (isDark) Color(0xFF1E1E1E) else Color.White),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Notifications, contentDescription = null, tint = currentPack.primaryColor)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Reminders & Notifications", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))
                    }
                    Text(
                        "Control alerts for schedule events, task deadlines, and note reminders.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Sound Alerts", style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold))
                            Text("Play notification tone for reminders", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        Switch(
                            checked = notificationsSound,
                            onCheckedChange = {
                                notificationsSound = it
                                saveAllChanges()
                            }
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Vibration", style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold))
                            Text("Vibrate device on alert trigger", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        Switch(
                            checked = notificationsVibrate,
                            onCheckedChange = {
                                notificationsVibrate = it
                                saveAllChanges()
                            }
                        )
                    }
                }
            }

            // Section 7: Storage & Trash Management
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = if (isDark) Color(0xFF1E1E1E) else Color.White),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Storage, contentDescription = null, tint = currentPack.primaryColor)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Local Storage & Maintenance", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))
                    }
                    Text(
                        "100% offline, on-device SQLite database. You have complete ownership of all data.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedButton(
                        onClick = {
                            viewModel.emptyTrash()
                            Toast.makeText(context, "Trash emptied completely", Toast.LENGTH_SHORT).show()
                        },
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Default.DeleteSweep, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Empty Trash Now")
                    }
                }
            }

            // Save Settings Button
            Button(
                onClick = { saveAllChanges() },
                colors = ButtonDefaults.buttonColors(containerColor = currentPack.primaryColor),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
                    .testTag("save_settings_button")
            ) {
                Icon(Icons.Default.Save, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Save All Settings & Preferences", fontWeight = FontWeight.Bold)
            }

            // Backup & Restore Navigation
            OutlinedButton(
                onClick = onNavigateToBackup,
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
            ) {
                Text("Export & Backup Database (JSON)")
            }
        }
    }
}
