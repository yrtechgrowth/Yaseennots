package com.example.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Book
import androidx.compose.material.icons.filled.Checklist
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.School
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.Note
import java.util.UUID

data class NoteTemplate(
    val title: String,
    val description: String,
    val content: String,
    val icon: ImageVector,
    val color: Long,
    val tags: String
)

val BuiltInTemplates = listOf(
    NoteTemplate(
        title = "Meeting Notes",
        description = "Format for team meetings, agendas, and action items",
        content = "## Meeting Details\nDate: \nAttendees: \n\n## Agenda\n1. \n2. \n3. \n\n## Discussion Points\n• \n• \n\n## Action Items & Next Steps\n- [ ] \n- [ ] ",
        icon = Icons.Default.Groups,
        color = 0xFFE3F2FD,
        tags = "Meeting,Work"
    ),
    NoteTemplate(
        title = "Daily Journal",
        description = "Reflect on your day, gratitude, and intentions",
        content = "## Morning Reflections\n• Top 3 intentions for today:\n  1. \n  2. \n  3. \n\n• What am I grateful for today?\n  - \n\n## Evening Review\n• Highlights of the day:\n  - \n• Lessons learned:\n  - ",
        icon = Icons.Default.Book,
        color = 0xFFFFF3E0,
        tags = "Journal,Personal"
    ),
    NoteTemplate(
        title = "Project Planner",
        description = "Scope out projects, milestones, and deliverables",
        content = "## Project Overview\nObjective: \nDeadline: \n\n## Key Milestones\n1. Phase 1: Research & Discovery\n2. Phase 2: Design & Prototyping\n3. Phase 3: Development & Testing\n4. Phase 4: Launch\n\n## Resources & Links\n• \n\n## Risks & Mitigations\n• ",
        icon = Icons.Default.Description,
        color = 0xFFE8F5E9,
        tags = "Project,Planning"
    ),
    NoteTemplate(
        title = "Lecture & Study Notes",
        description = "Structured Cornell-style note taking for classes",
        content = "Subject: \nTopic: \nDate: \n\n## Main Notes\n• Key concept 1: \n• Key concept 2: \n\n## Summary\n",
        icon = Icons.Default.School,
        color = 0xFFF3E5F5,
        tags = "Study,Lecture"
    ),
    NoteTemplate(
        title = "Quick Idea",
        description = "Capture spontaneous brainstorms and inspirations",
        content = "💡 Core Idea:\n\nWhy it matters:\n\nInitial steps to validate:\n- \n- ",
        icon = Icons.Default.Lightbulb,
        color = 0xFFFFF9C4,
        tags = "Ideas,Creative"
    )
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TemplatePickerSheet(
    onSelectTemplate: (Note) -> Unit,
    onDismiss: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState()

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 8.dp)
        ) {
            Text(
                text = "Choose a Template",
                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
            )
            Text(
                text = "Kickstart your note with structured formatting",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(16.dp))

            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                items(BuiltInTemplates) { template ->
                    Surface(
                        shape = RoundedCornerShape(14.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                val note = Note(
                                    id = UUID.randomUUID().toString(),
                                    title = template.title,
                                    description = template.description,
                                    content = template.content,
                                    color = template.color,
                                    tags = template.tags
                                )
                                onSelectTemplate(note)
                            }
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(14.dp)
                        ) {
                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = Color(template.color),
                                modifier = Modifier.size(44.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        imageVector = template.icon,
                                        contentDescription = template.title,
                                        tint = Color(0xFF1E293B),
                                        modifier = Modifier.size(24.dp)
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.width(14.dp))

                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = template.title,
                                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold)
                                )
                                Text(
                                    text = template.description,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            }
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}
