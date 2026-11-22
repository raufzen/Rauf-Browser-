package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Apps
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.MonitorHeart
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.example.api.FeatureGroup
import com.example.ui.theme.ThemeMode

private data class PaletteAction(
    val id: String,
    val title: String,
    val subtitle: String,
    val icon: ImageVector,
    val keywords: String,
    val onRun: () -> Unit
)

@Composable
fun CommandPaletteDialog(
    onDismiss: () -> Unit,
    onOpenAi: () -> Unit,
    onOpenDownloaderHub: () -> Unit,
    onOpenToolHub: (FeatureGroup) -> Unit,
    onOpenUtilities: () -> Unit,
    onOpenApiStatus: () -> Unit,
    onOpenSettings: () -> Unit,
    onOpenIncognito: () -> Unit,
    onSelectTheme: (ThemeMode) -> Unit
) {
    var query by remember { mutableStateOf("") }

    val allActions = remember {
        buildList {
            add(PaletteAction("ai", "Rauf.ai (ChatGPT · GPT-6)", "Switch GPT-6, GPT-6 Extra, Claude, Gemini & more", Icons.Default.AutoAwesome, "ai ask chat gpt gpt6 rauf.ai gemini claude llm", onOpenAi))
            add(PaletteAction("dl", "Downloader Hub", "Auto-detect YouTube, IG, TikTok, FB, X, Spotify & more", Icons.Default.Download, "download video mp4 mp3 ytdl igdl ttdl", onOpenDownloaderHub))
            add(PaletteAction("utils", "Offline Utilities", "Calculator, Notes, To-Do, Reminders, Password, Hash, Base64, JSON, Units", Icons.Default.Build, "offline calculator notes todo reminder password hash base64 json unit", onOpenUtilities))
            add(PaletteAction("status", "API Status & Priority", "Test all endpoints, view latency & reorder providers", Icons.Default.MonitorHeart, "status health ping latency provider api", onOpenApiStatus))
            add(PaletteAction("incog", "New Incognito Tab", "Private Chromium WebView session", Icons.Default.VisibilityOff, "incognito private tab browser", onOpenIncognito))
            add(PaletteAction("settings", "Settings, Themes & API Keys", "AMOLED Black, AMOLED White, Liquid Glass, White, Black, System & backup", Icons.Default.Settings, "settings theme keys backup export import", onOpenSettings))

            FeatureGroup.entries.filter { it != FeatureGroup.AI_CHAT }.forEach { feat ->
                add(
                    PaletteAction(
                        id = "feat_${feat.key}",
                        title = feat.label,
                        subtitle = "${feat.category} • ${feat.description}",
                        icon = Icons.Default.Apps,
                        keywords = "${feat.key} ${feat.label} ${feat.category} ${feat.description}",
                        onRun = { onOpenToolHub(feat) }
                    )
                )
            }

            ThemeMode.entries.forEach { theme ->
                add(
                    PaletteAction(
                        id = "theme_${theme.name}",
                        title = "Theme: ${theme.displayName}",
                        subtitle = "Switch app theme immediately",
                        icon = Icons.Default.Palette,
                        keywords = "theme color dark light ${theme.displayName}",
                        onRun = { onSelectTheme(theme) }
                    )
                )
            }
        }
    }

    val filtered = remember(query, allActions) {
        val q = query.trim().lowercase()
        if (q.isEmpty()) allActions.take(18)
        else allActions.filter {
            it.title.lowercase().contains(q) ||
                it.subtitle.lowercase().contains(q) ||
                it.keywords.lowercase().contains(q)
        }.take(24)
    }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(24.dp),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 8.dp,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("Command Palette", style = MaterialTheme.typography.titleMedium)
                Text(
                    "Jump to any tool, AI alias, utility, or theme (Ctrl+K)",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(Modifier.height(10.dp))
                OutlinedTextField(
                    value = query,
                    onValueChange = { query = it },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                    placeholder = { Text("Type a command, tool, or theme...") },
                    singleLine = true,
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("command_palette_input")
                )
                Spacer(Modifier.height(10.dp))
                LazyColumn(
                    modifier = Modifier.heightIn(max = 360.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    items(filtered, key = { it.id }) { action ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(14.dp))
                                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f))
                                .clickable {
                                    onDismiss()
                                    action.onRun()
                                }
                                .padding(horizontal = 12.dp, vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = action.icon,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(Modifier.width(12.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(action.title, style = MaterialTheme.typography.titleSmall)
                                Text(
                                    action.subtitle,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
