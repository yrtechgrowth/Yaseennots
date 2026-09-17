# Yaseen — Productivity & AI Suite for Android

[![Platform](https://img.shields.io/badge/Platform-Android-green.svg)](https://android.com)
[![Kotlin](https://img.shields.io/badge/Kotlin-2.2.10-blue.svg)](https://kotlinlang.org)
[![Jetpack Compose](https://img.shields.io/badge/UI-Jetpack%20Compose%20M3-purple.svg)](https://developer.android.com/jetpack/compose)
[![Database](https://img.shields.io/badge/Database-Room%202.6-orange.svg)](https://developer.android.com/training/data-storage/room)
[![License](https://img.shields.io/badge/License-Apache%202.0-lightgrey.svg)](LICENSE)

**Yaseen** is a modern, unified Android productivity suite and AI workspace designed with Material Design 3 and Jetpack Compose. Built with local-first offline Room persistence and multi-provider AI capability, Yaseen combines **Notes**, **Tasks**, **Smart Scheduling**, and **YaRVerse AI Chatbot** into an integrated experience.

---

## 📑 Table of Contents

- [Overview](#overview)
- [Key Features](#key-features)
  - [1. Yaseen Notes](#1-yaseen-notes)
  - [2. Yaseen Tasks](#2-yaseen-tasks)
  - [3. Yaseen Schedule & Time Blocking](#3-yaseen-schedule--time-blocking)
  - [4. Yaseen Files — Full Local File Manager](#4-yaseen-files--full-local-file-manager)
  - [5. YaRVerse AI Chatbot](#5-yarverse-ai-chatbot)
  - [6. Notebooks & Organization](#6-notebooks--organization)
  - [7. Trash & Version History](#7-trash--version-history)
  - [8. Productivity Statistics & Insights](#8-productivity-statistics--insights)
  - [9. Backup & Restore](#9-backup--restore)
  - [10. Personalization & Security](#10-personalization--security)
- [Architecture & Tech Stack](#architecture--tech-stack)
- [Project Directory Structure](#project-directory-structure)
- [Prerequisites & Requirements](#prerequisites--requirements)
- [Installation & Build Instructions](#installation--build-instructions)
  - [Clone the Repository](#step-1-clone-the-repository)
  - [Open in Android Studio](#step-2-open-in-android-studio)
  - [Build and Run](#step-3-build-and-run)
  - [Running Unit & Robolectric Tests](#step-4-running-unit--robolectric-tests)
- [AI Provider Configuration](#ai-provider-configuration)
  - [Google Gemini Setup](#google-gemini-setup)
  - [OpenAI Setup](#openai-setup)
  - [OpenRouter Setup](#openrouter-setup)
- [Detailed Module Documentation](#detailed-module-documentation)
- [Screenshots & UI Showcase](#screenshots--ui-showcase)
- [Troubleshooting & FAQs](#troubleshooting--faqs)
- [License](#license)

---

## 🌟 Overview

Yaseen is built from the ground up using modern Android architectural principles:
- **Offline-First Room SQLite Database**: All notes, tasks, events, notebooks, and chat conversations are saved securely on your device with instant access.
- **Material Design 3**: Dynamic color harmonies, fluid animations, staggered/grid card layouts, and adaptive navigation drawer and bottom bar.
- **YaRVerse AI**: Integrated conversational assistant capable of chatting via **Google Gemini**, **OpenAI**, or **OpenRouter** to brainstorm, summarize notes, and schedule routines.
- **Privacy & Security**: Built-in 4-digit PIN security lock with full JSON data portability (export and import backups anytime).

---

## 🚀 Key Features

### 1. Yaseen Notes
- **Rich Note Editor**: Markdown/HTML title and content support, rich text templates (Meeting Minutes, Brainstorming, Daily Journal, Weekly Review, Coding Snippets, Project Roadmap).
- **Freehand Drawing Canvas**: Draw notes, sketches, diagrams, or signatures with customizable brush colors and stroke widths.
- **Color Coding & Organization**: Choose from 8 vibrant pastel note card backgrounds with custom visual tags and notebook categorization.
- **Pinning & Sorting**: Pin vital notes to top; sort by Modified Date, Created Date, or Alphabetical (A-Z, Z-A).
- **Staggered vs. List View**: Seamlessly toggle between responsive masonry staggered grid layout and compact linear list cards.
- **Version History**: Automatically snapshots note revisions on edit, allowing users to inspect or roll back to any prior version.

### 2. Yaseen Tasks
- **To-Do Management**: Quick inline task creator with interactive checkboxes.
- **Smart Filtering**: Tab filters for **All**, **Today**, **Upcoming**, and **Completed** tasks.
- **Priority Tiers**: Color-coded priorities (**High**, **Medium**, **Low**).
- **Due Dates & Times**: Timestamp tracking with overdue indicators.
- **Task Analytics**: Real-time counter of total, active, and completed tasks.

### 3. Yaseen Schedule & Time Blocking
- **Calendar & Timeline View**: Day-by-day date picker with schedule overview.
- **Intelligent Conflict Detection**: Automatically alerts you when two active non-cancelled events overlap in time.
- **Event Categorization**: Color-coded tags for *Work*, *Personal*, *Study*, *Health*, *Finance*, and *Meeting*.
- **Urgency Levels**: Priority flags (Low, Normal, High, Urgent).
- **Recurrence Support**: Flexible recurring intervals (Daily, Weekly, Monthly).
- **Cross-Module Linking**: Link events directly to relevant Notes or Tasks.

### 4. Yaseen Files — Full Local File Manager
- **Dynamic Modular Branding**: Seamlessly adapts its label to the user's custom application branding (e.g. `Yaseen Files` or `My Notes Files`) while maintaining consistent system navigation under module ID `files`.
- **Comprehensive Storage Navigation**:
  - Internal app-isolated storage and cache.
  - Device shared storage categories: **Documents**, **Downloads**, **Pictures**, **Videos**, **Music**, **Audio**, and **Archives**.
  - Direct access to **Notes Attachments**, **Tasks Attachments**, and **Schedule Attachments**.
  - Modern scoped-storage and runtime permissions compliance.
- **File Management & Operations**:
  - Full CRUD: Create files/folders, copy, move, rename, delete with confirmation.
  - Multi-select batch actions (batch copy, batch move, batch zip, batch delete).
  - ZIP compression and in-place archive extraction.
  - Interactive breadcrumb navigation, search bar with real-time filtering, and Grid / List view toggling.
  - Sort by Name, Date, Size, or Type (ascending / descending).
- **Native In-App Viewers & Editors**:
  - **Image Viewer**: Fullscreen pinch-to-zoom, pan, and 90° rotation controls.
  - **Video Player**: Native hardware-accelerated playback with play/pause, scrub slider, forward/backward seek, and playback speed adjustment (0.5x – 2.0x).
  - **Audio Player**: Clean media player with play/pause, position scrubbing, duration readout, and loop controls.
  - **Text & Code Editor**: Integrated text and code viewer/editor with line counting, file saving, and word statistics.
  - **Metadata & Info Sheet**: Inspect file path, MIME type, file size, permissions, MD5 checksum, and last modified date.
- **AI File Intelligence**:
  - Contextual AI actions powered by the active YaRVerse LLM engine (Gemini, OpenAI, OpenRouter):
    - *Summarize*: Distill large text documents and logs.
    - *Extract Tasks*: Automatically find action items in documents and create Yaseen Tasks.
    - *Explain Code*: Decode scripts, config files, and code snippets into plain English.
    - *Keywords & Tags*: Generate indexing keywords and descriptive metadata.
    - *Translate*: Instant translation into target languages.
- **Deep Suite Integration**:
  - Attach any local file directly to an existing or new Yaseen Note, Task, or Schedule event with preview chips.
- **Storage Analyzer & Optimization**:
  - Category breakdown visualization (Images, Video, Audio, Documents, Archives, Other).
  - Storage consumption percentage bars and free space indicators.
  - Duplicate file finder using size & MD5 hash heuristics.
  - Large files list (> 25MB) with one-tap inspection.
- **Private Encrypted Vault**:
  - Secure local hidden folder protected by a dedicated 4-digit PIN lock.
  - Keep sensitive files, contracts, photos, and backups safe from prying eyes.

### 5. YaRVerse AI Chatbot
- **Multi-Engine Intelligence**: Connect to:
  - **Google Gemini** (e.g., `gemini-3.5-flash`, `gemini-1.5-pro`, `gemini-1.5-flash`)
  - **OpenAI** (e.g., `gpt-4o-mini`, `gpt-4o`, `gpt-3.5-turbo`)
  - **OpenRouter** (e.g., `openrouter/auto`, Claude 3.5 Sonnet, DeepSeek, Llama 3)
- **Context-Aware Dialogue**: Maintains recent conversation context for coherent multi-turn discussions.
- **Customizable System Prompt**: Adjust the personality, tone, and directives of YaRVerse to suit coding, writing, or scheduling.
- **Conversation Management**: Clear chat history anytime or copy responses with a single tap.

### 6. Notebooks & Organization
- Create custom notebooks with custom accent colors and icons.
- Filter notes by specific notebook directly from the home search bar.
- Note counts per notebook displayed automatically.

### 7. Trash & Version History
- **Two-Stage Deletion**: Deleted notes move to the Trash bin first to prevent accidental loss.
- **Restore or Wipe**: Restore notes to your active list or permanently delete them.
- **Empty Trash**: One-click purge of all soft-deleted items.
- **Audit Trail**: View timestamps and previous content states in Note Version history.

### 8. Productivity Statistics & Insights
- Comprehensive visual analytics dashboard:
  - Total notes created, pinned notes, and total word count.
  - Task completion rate progress bar and pending counts.
  - Active scheduled items and conflict monitors.
  - Estimated database disk storage usage.

### 9. Backup & Restore
- **Complete JSON Export**: Export your entire database (notes, tasks, schedule items, notebooks, settings) to a human-readable JSON file.
- **Safe Import & Restore**: Restore previous backups or transfer your workspace between devices seamlessly.

### 10. Personalization & Security
- **Dynamic Icon & Color Themes**: 7 tailored color palettes:
  - *Emerald* (Default crisp teal/emerald)
  - *Neon Cyan* (Vibrant cyberpunk cyan)
  - *Sunset Gold* (Warm amber/golden hour)
  - *Royal Purple* (Deep amethyst violet)
  - *Minimal Slate* (Monochromatic modern gray)
  - *Crimson Ruby* (Bold scarlet red)
  - *Ocean Blue* (Serene sapphire blue)
- **Dark Mode Support**: Seamless manual and system-level Dark / Light mode switching.
- **Custom App & Module Naming**: Re-label the app and module titles (e.g., personalize "Yaseen Notes" to your own name or workflow).
- **4-Digit PIN Lock**: Optional passcode verification on app launch to safeguard sensitive journal entries and plans.

---

## 🏗️ Architecture & Tech Stack

```
                     ┌───────────────────────────────┐
                     │          MainActivity         │
                     └───────────────┬───────────────┘
                                     │
                     ┌───────────────▼───────────────┐
                     │        MainAppScaffold        │
                     │ (Navigation Drawer + Bottom)  │
                     └───────────────┬───────────────┘
                                     │
         ┌───────────────┬───────────┴───┬───────────────┬───────────────┐
         ▼               ▼               ▼               ▼               ▼
   [NotesScreen]   [TasksScreen]  [ScheduleScreen] [ChatbotScreen] [SettingsScreen]
         │               │               │               │               │
         └───────────────┼───────────────┼───────────────┴───────────────┘
                         ▼               ▼
                 ┌───────────────────────────────┐
                 │         NotesViewModel        │
                 └───────────────┬───────────────┘
                                 │
                 ┌───────────────┴───────────────┐
                 ▼                               ▼
       ┌──────────────────┐            ┌───────────────────┐
       │   Room Database  │            │  ChatApiService   │
       │ (AppDao / SQLite)│            │(OkHttp / REST API)│
       └──────────────────┘            └─────────┬─────────┘
                                                 │
                                 ┌───────────────┼───────────────┐
                                 ▼               ▼               ▼
                              [Gemini]        [OpenAI]     [OpenRouter]
```

- **Architecture**: Model-View-ViewModel (MVVM) with unidirectional data flow (UDF).
- **UI Framework**: Jetpack Compose with Material Design 3 (`androidx.compose.material3`).
- **Language**: Kotlin 2.2.10.
- **Database / Local Storage**: Room 2.6.1 + KSP (Kotlin Symbol Processing).
- **Networking**: OkHttp 4.12.0 + Moshi JSON parsing.
- **Asynchronous Execution**: Kotlin Coroutines & `StateFlow` / `SharedFlow`.
- **Target SDK**: 36 | **Compile SDK**: 36 | **Minimum SDK**: 24 (Android 7.0+).

---

## 📁 Project Directory Structure

```
├── app/
│   ├── build.gradle.kts                      # Module Gradle build configuration
│   ├── proguard-rules.pro                    # ProGuard / R8 rules
│   └── src/
│       ├── main/
│       │   ├── AndroidManifest.xml           # Permissions and application manifest
│       │   ├── java/com/example/
│       │   │   ├── MainActivity.kt           # Main entry activity & theme container
│       │   │   ├── data/
│       │   │   │   ├── Note.kt               # Note Room entity
│       │   │   │   ├── Notebook.kt           # Notebook Room entity
│       │   │   │   ├── NoteVersion.kt        # Note version history entity
│       │   │   │   ├── TaskItem.kt           # Task Room entity
│       │   │   │   ├── ScheduleItem.kt       # Event entity with conflict detection
│       │   │   │   ├── ChatMessage.kt        # Chat message entity
│       │   │   │   ├── AppDao.kt             # Unified Room DAO interface
│       │   │   │   ├── AppDatabase.kt        # Room database class (v3 migration)
│       │   │   │   ├── AppSettings.kt        # Settings model & SharedPreferences
│       │   │   │   ├── ChatApiService.kt     # Multi-provider REST client (Gemini/OpenAI/OpenRouter)
│       │   │   │   └── files/                # File manager models & repository
│       │   │   │       ├── FileModels.kt     # FileItem, StorageCategory, SortOption
│       │   │   │       └── FileManagerRepository.kt # Scoped-storage & CRUD repository
│       │   │   └── ui/
│       │   │       ├── MainAppScaffold.kt    # Root scaffold with drawer, bottom bar, & dynamic naming
│       │   │       ├── components/
│       │   │       │   ├── DrawingDialog.kt  # Freehand canvas sketch dialog
│       │   │       │   ├── NoteCard.kt       # Note card composable
│       │   │       │   ├── TaskCard.kt       # Interactive task item card
│       │   │       │   └── SearchAndFilterBar.kt
│       │   │       ├── screens/
│       │   │       │   ├── NotesListScreen.kt# Notes feed & filter UI
│       │   │       │   ├── NoteEditorScreen.kt# Full-featured note composer
│       │   │       │   ├── TasksScreen.kt    # To-do task dashboard
│       │   │       │   ├── ScheduleScreen.kt # Calendar & schedule coordinator
│       │   │       │   ├── files/            # Full-featured File Manager module
│       │   │       │   │   ├── FilesScreen.kt# Primary File Manager Screen
│       │   │       │   │   ├── FileComponents.kt# File card/list items, breadcrumbs
│       │   │       │   │   ├── ImageViewerScreen.kt# Pinch-to-zoom & rotation
│       │   │       │   │   ├── VideoPlayerScreen.kt# Playback & speed controls
│       │   │       │   │   ├── AudioPlayerScreen.kt# Audio seek & playback
│       │   │       │   │   ├── TextEditorScreen.kt # In-app text/code editor
│       │   │       │   │   ├── StorageAnalyzerScreen.kt# Categories & duplicates
│       │   │       │   │   ├── AiFileActionDialog.kt# AI summarize, tasks, explain
│       │   │       │   │   ├── AttachToYaseenDialog.kt# Attach files to Notes/Tasks
│       │   │       │   │   └── PrivateVaultPinDialog.kt# 4-digit PIN private vault
│       │   │       │   ├── ChatbotScreen.kt  # YaRVerse AI chat window
│       │   │       │   ├── NotebooksScreen.kt# Notebook manager
│       │   │       │   ├── TrashScreen.kt    # Soft-deleted recycle bin
│       │   │       │   ├── StatisticsScreen.kt# Productivity metrics & stats
│       │   │       │   ├── BackupRestoreScreen.kt# JSON backup & restore
│       │   │       │   ├── SettingsScreen.kt # Configuration, module names & API keys
│       │   │       │   └── TemplatePickerSheet.kt# Quick template chooser
│       │   │       ├── theme/
│       │   │       │   ├── Color.kt          # Color definitions
│       │   │       │   ├── Theme.kt          # M3 MaterialTheme config
│       │   │       │   ├── Type.kt           # Typography scale
│       │   │       │   └── IconThemeHelper.kt# 7 custom icon pack helper with Files icons
│       │   │       └── viewmodel/
│       │   │           ├── NotesViewModel.kt # Unified app StateFlow ViewModel
│       │   │           └── FilesViewModel.kt # File Manager & Media Player ViewModel
│       │   └── res/                          # Vector drawables, mipmaps, strings, themes
│       └── test/                             # Unit tests & Robolectric test suite
├── gradle/
│   └── libs.versions.toml                    # Version Catalog
├── build.gradle.kts                          # Root build script
├── settings.gradle.kts                       # Project settings
├── metadata.json                             # AI Studio metadata
└── README.md                                 # This documentation file
```

---

## 📋 Prerequisites & Requirements

Before building Yaseen, ensure your local development environment meets the following specifications:

| Requirement | Recommended Version |
|---|---|
| **Operating System** | macOS, Linux, or Windows 10/11 |
| **Android Studio** | Android Studio Ladybug (2024.2.1+) or newer |
| **JDK** | Java Development Kit 17 or 21 (Temurin / OpenJDK) |
| **Android SDK** | API Level 36 (Minimum API 24 supported) |
| **Gradle** | 8.x / 9.x (managed via Gradle wrapper) |

---

## 🛠️ Installation & Build Instructions

### Step 1: Clone the Repository

Clone the project repository to your local workstation:

```bash
git clone https://github.com/whoisyaseen59/Yaseennots.git
cd Yaseennots
```

### Step 2: Open in Android Studio

1. Launch **Android Studio**.
2. Select **File > Open...** (or click **Open** from the welcome screen).
3. Browse to the cloned directory and select the root folder.
4. Allow Android Studio to complete Gradle sync and download required dependencies.

### Step 3: Build and Run

- **Via Android Studio**:
  1. Connect a physical Android device (with USB Debugging enabled) or start an Android Virtual Device (AVD) running Android 7.0 (API 24) or higher.
  2. Select the `app` run configuration in the toolbar.
  3. Click the green **Run (▶)** button or press `Shift + F10`.

- **Via Terminal (Gradle)**:
  To build the debug APK directly:
  ```bash
  ./gradlew assembleDebug
  # Or on Windows:
  # gradlew.bat assembleDebug
  ```
  The resulting APK will be generated at:
  ```
  app/build/outputs/apk/debug/app-debug.apk
  ```

  To install the debug APK onto a connected device via ADB:
  ```bash
  adb install -r app/build/outputs/apk/debug/app-debug.apk
  ```

### Step 4: Running Unit & Robolectric Tests

Execute the comprehensive test suite directly from your terminal:

```bash
./gradlew testDebugUnitTest
```

---

## 🤖 AI Provider Configuration

The YaRVerse AI assistant supports three AI providers. You can easily configure your preferred provider and API key directly inside the app:

1. Open the app and open the **Navigation Drawer** (hamburger icon on top left).
2. Tap **Settings**.
3. Scroll to the **AI Provider & YaRVerse Setup** section.
4. Select your preferred provider from the dropdown:

### Google Gemini Setup
1. Obtain an API key from [Google AI Studio](https://aistudio.google.com/).
2. In Yaseen Settings, select **Google Gemini** as the AI Provider.
3. Paste your key into the **Gemini API Key** field.
4. Select or type your desired model (default: `gemini-3.5-flash`).

### OpenAI Setup
1. Obtain an API key from [OpenAI Platform](https://platform.openai.com/api-keys).
2. In Yaseen Settings, select **OpenAI** as the AI Provider.
3. Paste your key into the **OpenAI API Key** field.
4. Select or type your desired model (default: `gpt-4o-mini`).

### OpenRouter Setup
1. Obtain an API key from [OpenRouter](https://openrouter.ai/keys).
2. In Yaseen Settings, select **OpenRouter** as the AI Provider.
3. Paste your key into the **OpenRouter API Key** field.
4. Select or type your desired model (default: `openrouter/auto`).

> 💡 **Tip**: You can also customize the **System Prompt** in Settings to personalize how YaRVerse formats, styles, or focuses its answers.

---

## 📖 Detailed Module Documentation

### 📝 Notes Module
- **Creating a Note**: Tap the Floating Action Button `+` on the Notes tab.
- **Templates**: In the note editor, tap the **Template** icon to insert pre-built templates for meetings, daily journal, code reviews, or sprint roadmaps.
- **Drawing Canvas**: Tap the **Palette / Pen** icon to open the drawing board. Sketch annotations, doodles, or formulas, and hit **Save Drawing** to embed it directly in your note.
- **Tags**: Enter comma-separated tags (e.g. `work, ideas, urgent`) in the tag field for instant filtering.
- **Version History**: Tap the clock icon in the editor toolbar to view and roll back to past edits of your note.

### ✅ Tasks Module
- **Adding a Task**: Enter a task title in the quick task field and tap **Add**, or use the bottom action sheet to set a due date and priority level (**High**, **Medium**, **Low**).
- **Managing Completion**: Tap the circular checkbox on any task card to mark it finished.
- **Filtering**: Use the filter chips at the top to toggle between All, Today, Upcoming, and Completed views.

### 📅 Schedule Module
- **Event Scheduling**: Tap `+` in the Schedule view. Specify the title, date, start time, end time, location, category, and recurrence.
- **Conflict Warning**: If an event overlaps with an existing appointment on the same date, a red conflict warning banner appears with details of the overlapping event.

### 📁 Files Module (Local Storage & Media Manager)
- **Navigation & Browsing**:
  - Switch between top category chips (**All Files**, **Documents**, **Pictures**, **Videos**, **Audio**, **Archives**, **Attachments**).
  - Tap any directory to navigate deeper with interactive breadcrumbs to step back directly to any parent directory.
  - Search files instantly by name via the header search bar.
  - Toggle between compact list view and 2-column grid view.
- **Operations & Management**:
  - Single tap on a file opens its native in-app viewer (Image, Video, Audio, Text Editor, or Details).
  - Tap the three-dot action menu on any file/folder for **Attach**, **Share**, **Rename**, **Compress (ZIP)**, **Extract**, **Move to Private Vault**, or **Delete**.
  - Long-press or enter multi-select mode to perform batch copy, batch move, batch zip, or batch deletion.
  - Use the FAB `+` to create a new folder, create a new text file, or import external files.
- **In-App Media Viewers**:
  - **Photos/Images**: Pinch to zoom in/out, double-tap zoom, drag/pan, and rotate in 90-degree steps.
  - **Videos**: Dedicated player supporting pause/play, seek bar, time indicator, and playback rate toggles (0.5x, 1x, 1.25x, 1.5x, 2x).
  - **Audio**: Clean audio playback player with track title, elapsed time, total duration, seek slider, and looping.
  - **Text & Code Editor**: Edit files in-place with line counter, word count, character stats, and direct save to disk.
- **AI File Intelligence**:
  - Select **AI Actions** from any text or document menu.
  - Choose from *Summarize*, *Extract Tasks*, *Explain Code*, *Generate Tags*, or *Translate*.
  - View results in Markdown and copy or convert directly into Yaseen Tasks with a single tap.
- **Private Vault**:
  - Tap the Vault icon on the Files toolbar. Enter your secure 4-digit PIN (default `0000` until customized) to access hidden files and records.
- **Storage Analytics**:
  - Tap the Pie Chart icon on the Files toolbar to inspect local disk distribution, find duplicates, and inspect large files (>25MB).

### 🤖 YaRVerse Chatbot
- **Interactive Chat**: Type any query into the prompt input bar. YaRVerse can summarize notes, write task action plans, generate study guides, or answer general knowledge questions.
- **Multi-Turn Memory**: YaRVerse remembers the ongoing conversation thread to provide contextual answers.

### 💾 Backup & Restore
- **Export**: Navigate to **Backup & Restore** from the drawer menu, and tap **Export Complete Backup**. The entire database is formatted into a portable JSON structure.
- **Import**: Paste or load previously exported JSON data to restore your workspace.

---

## 🎨 Personalization & Customization

Yaseen provides unprecedented customization options without needing code modifications:
- **7 Icon & Accent Themes**: Choose between *Emerald*, *Neon Cyan*, *Sunset Gold*, *Royal Purple*, *Minimal Slate*, *Crimson Ruby*, or *Ocean Blue*.
- **Custom Branding**: Modify the application display name and module names (e.g. rename "Yaseen Notes" to "Personal Notes") directly inside **Settings > Customization**.
- **Layout Selection**: Toggle between Staggered Grid (Pinterest-style) or Linear Card layout for notes.
- **Security PIN**: Enable PIN Lock to prevent unauthorized access upon app launch.

---

## ❓ Troubleshooting & FAQs

**Q: Where is my data stored?**  
A: All notes, tasks, notebooks, and schedule events are stored locally on your device in a secure SQLite database via Android Room. No data is sent to external servers unless you interact with the YaRVerse AI chatbot (which sends your prompt to your selected AI provider).

**Q: Is an internet connection required?**  
A: No! Yaseen is fully functional offline. An internet connection is only needed when communicating with the YaRVerse AI chatbot.

**Q: How do I backup my notes?**  
A: Go to the side navigation drawer > **Backup & Restore** > **Export Complete Backup**. You can copy the generated JSON and store it securely in cloud storage or a text file.

---

## 📄 License

```
Copyright 2026 Yaseen Contributors

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
```
