package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.GppBad
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.OpenInNew
import androidx.compose.material.icons.filled.Security
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.R
import com.example.data.local.BrowserSettings
import com.example.data.local.HomepageWallpaper
import com.example.engine.ExternalLinkPrompt
import com.example.engine.FindInPageState
import com.example.engine.SitePermissionPrompt
import com.example.engine.SslAlertState
import com.example.ui.theme.DangerCrimson
import com.example.ui.theme.MonoFontFamily

@Composable
fun SslErrorInterstitial(
    alert: SslAlertState,
    onBackToSafety: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xF512050B))
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        GlassCard(
            shape = RoundedCornerShape(26.dp),
            strong = true,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(22.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.GppBad,
                    contentDescription = "SSL Security Error",
                    tint = DangerCrimson,
                    modifier = Modifier.size(44.dp)
                )
                Text(
                    text = stringResource(R.string.ssl_error_title),
                    style = MaterialTheme.typography.headlineMedium,
                    color = Color.White,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = stringResource(R.string.ssl_error_body),
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color(0xFFE2D4DC)
                )
                GlassCard(shape = RoundedCornerShape(14.dp), modifier = Modifier.fillMaxWidth()) {
                    Column(
                        modifier = Modifier.padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(
                            text = "Blocked URL: ${alert.url}",
                            style = MaterialTheme.typography.bodySmall.copy(fontFamily = MonoFontFamily),
                            color = DangerCrimson
                        )
                        Text(
                            text = "Reason: ${alert.reason}",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.White
                        )
                        Text(
                            text = "Certificate: ${alert.issuedTo} • Issuer: ${alert.issuedBy}",
                            style = MaterialTheme.typography.labelSmall,
                            color = Color(0xFFBBA8B5)
                        )
                    }
                }
                Spacer(Modifier.height(4.dp))
                Button(
                    onClick = onBackToSafety,
                    colors = ButtonDefaults.buttonColors(containerColor = DangerCrimson),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .testTag("ssl_back_to_safety_button")
                ) {
                    Text(stringResource(R.string.ssl_go_back), color = Color.White)
                }
            }
        }
    }
}

@Composable
fun ExternalLinkConfirmDialog(
    prompt: ExternalLinkPrompt,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        icon = { Icon(Icons.Default.OpenInNew, contentDescription = null) },
        title = { Text("Open External App?") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text("This link wants to leave Rauf Browser using scheme \"${prompt.scheme}:\":")
                Text(
                    text = prompt.url,
                    style = MaterialTheme.typography.bodySmall.copy(fontFamily = MonoFontFamily),
                    color = MaterialTheme.colorScheme.primary
                )
            }
        },
        confirmButton = {
            TextButton(onClick = onConfirm) { Text("Open App") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Block") }
        }
    )
}

@Composable
fun SitePermissionDialog(
    prompt: SitePermissionPrompt,
    onAllow: () -> Unit,
    onDeny: () -> Unit
) {
    val (origin, detail) = when (prompt) {
        is SitePermissionPrompt.Geolocation -> prompt.origin to "wants to access your device's precise location."
        is SitePermissionPrompt.Media -> prompt.origin to "wants to access your camera and/or microphone."
    }
    AlertDialog(
        onDismissRequest = onDeny,
        icon = { Icon(Icons.Default.Security, contentDescription = null) },
        title = { Text("Website Permission Request") },
        text = { Text("$origin $detail") },
        confirmButton = {
            TextButton(onClick = onAllow) { Text("Allow Once") }
        },
        dismissButton = {
            TextButton(onClick = onDeny) { Text("Deny") }
        }
    )
}

@Composable
fun FindInPageBar(
    state: FindInPageState,
    onQueryChange: (String) -> Unit,
    onPrev: () -> Unit,
    onNext: () -> Unit,
    onClose: () -> Unit
) {
    GlassCard(
        shape = RoundedCornerShape(18.dp),
        strong = true,
        accentHighlight = true,
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 6.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 10.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedTextField(
                value = state.query,
                onValueChange = onQueryChange,
                placeholder = { Text("Find in page...") },
                singleLine = true,
                modifier = Modifier
                    .weight(1f)
                    .testTag("find_in_page_input")
            )
            Spacer(Modifier.width(6.dp))
            Text(
                text = "${state.activeMatchIndex}/${state.totalMatches}",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            IconButton(onClick = onPrev) {
                Icon(Icons.Default.KeyboardArrowUp, contentDescription = "Previous match")
            }
            IconButton(onClick = onNext) {
                Icon(Icons.Default.KeyboardArrowDown, contentDescription = "Next match")
            }
            IconButton(onClick = onClose) {
                Icon(Icons.Default.Close, contentDescription = "Close find in page")
            }
        }
    }
}

@Composable
fun CustomizeHomepageDialog(
    settings: BrowserSettings,
    onUpdateSettings: ((BrowserSettings) -> BrowserSettings) -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Customize Homepage") },
        text = {
            Column(
                modifier = Modifier.verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text("Background Wallpaper", style = MaterialTheme.typography.labelLarge)
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(HomepageWallpaper.entries) { wp ->
                        FilterChip(
                            selected = settings.wallpaper == wp,
                            onClick = { onUpdateSettings { it.copy(wallpaper = wp) } },
                            label = { Text(wp.displayName) }
                        )
                    }
                }
                Spacer(Modifier.height(4.dp))
                ToggleLine("Current Date", settings.showDateOnHome) { v ->
                    onUpdateSettings { it.copy(showDateOnHome = v) }
                }
                ToggleLine("Quick Shortcuts", settings.showQuickShortcuts) { v ->
                    onUpdateSettings { it.copy(showQuickShortcuts = v) }
                }
                ToggleLine("Frequently Visited", settings.showFrequentSites) { v ->
                    onUpdateSettings { it.copy(showFrequentSites = v) }
                }
                ToggleLine("Recently Visited", settings.showRecentSites) { v ->
                    onUpdateSettings { it.copy(showRecentSites = v) }
                }
                ToggleLine("Bookmarks Section", settings.showBookmarksOnHome) { v ->
                    onUpdateSettings { it.copy(showBookmarksOnHome = v) }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) { Text("Done") }
        }
    )
}

@Composable
private fun ToggleLine(label: String, checked: Boolean, onChange: (Boolean) -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(label, style = MaterialTheme.typography.bodyMedium)
        Switch(checked = checked, onCheckedChange = onChange)
    }
}

@Composable
fun AddShortcutDialog(
    onConfirm: (title: String, url: String) -> Unit,
    onDismiss: () -> Unit
) {
    var title by remember { mutableStateOf("") }
    var url by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add Quick-Access Shortcut") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Shortcut Name") },
                    placeholder = { Text("e.g., BBC News") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = url,
                    onValueChange = { url = it },
                    label = { Text("Website URL") },
                    placeholder = { Text("https://www.bbc.com") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    if (url.isNotBlank()) {
                        onConfirm(title.trim(), url.trim())
                    }
                }
            ) { Text("Add") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}
