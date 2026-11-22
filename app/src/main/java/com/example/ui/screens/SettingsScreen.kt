package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Backup
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.DeleteForever
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.MonitorHeart
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.OpenInBrowser
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Policy
import androidx.compose.material.icons.filled.PrivacyTip
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Slider
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.api.AuthKeySlot
import com.example.core.SearchEngine
import com.example.data.local.BrowserSettings
import com.example.data.local.HomepageWallpaper
import com.example.ui.components.GlassCard
import com.example.ui.components.SubScreenTopBar
import com.example.ui.theme.ACCENT_COLOR_PRESETS
import com.example.ui.theme.DangerCrimson
import com.example.ui.theme.SecureGreen
import com.example.ui.theme.ThemeMode
import kotlin.math.roundToInt

private val SUPPORTED_LANGUAGES = listOf(
    "en" to "English",
    "ur" to "Urdu (اردو)",
    "ar" to "Arabic (العربية)",
    "es" to "Español",
    "fr" to "Français",
    "de" to "Deutsch",
    "hi" to "Hindi (हिन्दी)",
    "tr" to "Türkçe"
)

@Composable
fun SettingsScreen(
    settings: BrowserSettings,
    isDesktopMode: Boolean = false,
    onToggleDesktopMode: () -> Unit = {},
    onBack: () -> Unit,
    onUpdateSettings: ((BrowserSettings) -> BrowserSettings) -> Unit,
    onSetApiKey: (String, String) -> Unit,
    onOpenApiStatus: () -> Unit,
    onExportBackup: () -> Unit,
    onImportBackup: () -> Unit,
    onClearCache: () -> Unit,
    onClearCookies: () -> Unit,
    onClearAllData: () -> Unit,
    onOpenDownloads: () -> Unit,
    onOpenDefaultBrowserSettings: () -> Unit,
    onRequestNotificationPermission: () -> Unit,
    onOpenPrivacyPolicy: () -> Unit,
    onOpenAbout: () -> Unit
) {
    BackHandler(onBack = onBack)

    var showCustomHomeDialog by remember { mutableStateOf(false) }
    var showClearAllConfirm by remember { mutableStateOf(false) }
    var showApiKeysDialog by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .navigationBarsPadding()
            .imePadding()
    ) {
        SubScreenTopBar(
            title = "Settings",
            subtitle = "Themes • Rauf.ai • Privacy • Backup",
            onBack = onBack
        )

        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
            contentAlignment = Alignment.TopCenter
        ) {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .widthIn(max = 680.dp),
                contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 14.dp, bottom = 28.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
            // 1. Themes & Appearance (AMOLED Black, AMOLED White, Liquid Glass, White, Black + System)
            item {
                SettingsSectionCard(title = "1. Themes & Appearance", icon = Icons.Default.Palette) {
                    Text(
                        text = "Browser Theme",
                        style = MaterialTheme.typography.titleSmall
                    )
                    Spacer(Modifier.height(10.dp))
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        ThemeMode.entries.forEach { mode ->
                            val selected = settings.themeMode == mode
                            val (swatchBg, swatchAccent) = when (mode) {
                                ThemeMode.AMOLED_BLACK -> Color(0xFF000000) to Color(0xFF5E8BFF)
                                ThemeMode.AMOLED_WHITE -> Color(0xFFFFFFFF) to Color(0xFF2563EB)
                                ThemeMode.LIQUID_GLASS -> Color(0xFF1D1538) to Color(0xFFC084FC)
                                ThemeMode.WHITE -> Color(0xFFF3F4F6) to Color(0xFF3969EF)
                                ThemeMode.BLACK -> Color(0xFF16181D) to Color(0xFF6B93FF)
                                ThemeMode.SYSTEM -> Color(0xFF3B4252) to Color(0xFFE5E9F0)
                            }
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .heightIn(min = 58.dp)
                                    .clip(RoundedCornerShape(14.dp))
                                    .background(
                                        if (selected) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.65f)
                                        else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f)
                                    )
                                    .border(
                                        width = if (selected) 1.25.dp else 0.75.dp,
                                        color = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant,
                                        shape = RoundedCornerShape(14.dp)
                                    )
                                    .clickable { onUpdateSettings { it.copy(themeMode = mode) } }
                                    .padding(horizontal = 14.dp, vertical = 10.dp)
                                    .testTag("theme_mode_${mode.name.lowercase()}"),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(26.dp)
                                        .clip(CircleShape)
                                        .background(swatchBg)
                                        .border(1.25.dp, MaterialTheme.colorScheme.outline, CircleShape),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(9.dp)
                                            .clip(CircleShape)
                                            .background(swatchAccent)
                                    )
                                }
                                Spacer(Modifier.width(12.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = mode.displayName,
                                        style = MaterialTheme.typography.titleSmall,
                                        color = MaterialTheme.colorScheme.onSurface,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                    Text(
                                        text = mode.subtitle,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                                if (selected) {
                                    Spacer(Modifier.width(8.dp))
                                    Icon(
                                        Icons.Default.Check,
                                        contentDescription = "Selected",
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            }
                        }
                    }

                    HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp))
                    Text("Accent Color Picker", style = MaterialTheme.typography.titleSmall)
                    Spacer(Modifier.height(8.dp))
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        items(ACCENT_COLOR_PRESETS) { (name, hex) ->
                            val selected = settings.accentColorHex == hex
                            FilterChip(
                                selected = selected,
                                onClick = { onUpdateSettings { it.copy(accentColorHex = hex) } },
                                leadingIcon = {
                                    Box(
                                        modifier = Modifier
                                            .size(14.dp)
                                            .clip(CircleShape)
                                            .background(if (hex == 0L) MaterialTheme.colorScheme.primary else Color(hex))
                                    )
                                },
                                label = { Text(name) }
                            )
                        }
                    }

                    HorizontalDivider(modifier = Modifier.padding(vertical = 10.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("UI Font Size", style = MaterialTheme.typography.titleSmall)
                        Text(
                            "${(settings.fontScale * 100).roundToInt()}%",
                            style = MaterialTheme.typography.labelLarge,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                    Slider(
                        value = settings.fontScale,
                        onValueChange = { v -> onUpdateSettings { it.copy(fontScale = (v * 20f).roundToInt() / 20f) } },
                        valueRange = 0.85f..1.25f
                    )

                    SettingsSwitchRow(
                        title = "Reduced motion",
                        subtitle = "Disables typing animation & heavy visual transitions",
                        checked = settings.reducedMotion,
                        onCheckedChange = { v -> onUpdateSettings { it.copy(reducedMotion = v) } }
                    )
                }
            }

            // 2. API Keys, API Status & Single-File JSON Backup
            item {
                SettingsSectionCard(title = "2. API Providers, Keys & Backup", icon = Icons.Default.Key) {
                    SettingsActionRow(
                        title = "API Status & Provider Priority",
                        subtitle = "Test all endpoints, view latency dots, toggle & reorder failover",
                        leadingIcon = Icons.Default.MonitorHeart,
                        onClick = onOpenApiStatus
                    )
                    HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))
                    SettingsActionRow(
                        title = "Manage Local API Keys",
                        subtitle = "${settings.apiKeys.size} key(s) saved locally (Groq, OpenWeather, NewsAPI, Shizo, PrinceTech, etc.)",
                        leadingIcon = Icons.Default.Key,
                        onClick = { showApiKeysDialog = true }
                    )
                    HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedButton(
                            onClick = onExportBackup,
                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 10.dp),
                            modifier = Modifier
                                .weight(1f)
                                .heightIn(min = 48.dp)
                                .testTag("settings_export_json")
                        ) {
                            Icon(Icons.Default.Backup, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(Modifier.width(6.dp))
                            Text("Export JSON", maxLines = 1, overflow = TextOverflow.Ellipsis)
                        }
                        OutlinedButton(
                            onClick = onImportBackup,
                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 10.dp),
                            modifier = Modifier
                                .weight(1f)
                                .heightIn(min = 48.dp)
                                .testTag("settings_import_json")
                        ) {
                            Text("Import JSON", maxLines = 1, overflow = TextOverflow.Ellipsis)
                        }
                    }
                }
            }

            // 3. General Browser Settings
            item {
                SettingsSectionCard(title = "3. General Browser Settings", icon = Icons.Default.Tune) {
                    SettingsSwitchRow(
                        title = "Desktop site (current tab)",
                        subtitle = "Fits full desktop web pages cleanly to your Android screen",
                        checked = isDesktopMode,
                        onCheckedChange = { onToggleDesktopMode() }
                    )
                    HorizontalDivider(modifier = Modifier.padding(vertical = 6.dp))
                    SettingsSwitchRow(
                        title = "Restore tabs on startup",
                        subtitle = "Keep tabs ready between sessions",
                        checked = settings.restoreTabsOnStartup,
                        onCheckedChange = { checked -> onUpdateSettings { it.copy(restoreTabsOnStartup = checked) } }
                    )
                    HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Webpage text scale", style = MaterialTheme.typography.titleSmall)
                        Text(
                            "${settings.textZoomPercent}%",
                            style = MaterialTheme.typography.labelLarge,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                    Slider(
                        value = settings.textZoomPercent.toFloat(),
                        onValueChange = { value ->
                            val stepped = (value / 5f).roundToInt() * 5
                            onUpdateSettings { it.copy(textZoomPercent = stepped) }
                        },
                        valueRange = 75f..150f
                    )
                }
            }

            // 4. Search Engine
            item {
                SettingsSectionCard(title = "4. Search Engine", icon = Icons.Default.Search) {
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        items(SearchEngine.entries) { engine ->
                            FilterChip(
                                selected = settings.searchEngine == engine,
                                onClick = { onUpdateSettings { it.copy(searchEngine = engine) } },
                                label = { Text(engine.displayName) }
                            )
                        }
                    }
                }
            }

            // 5. Homepage Settings
            item {
                SettingsSectionCard(title = "5. Homepage Settings", icon = Icons.Default.Home) {
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        items(HomepageWallpaper.entries) { wp ->
                            FilterChip(
                                selected = settings.wallpaper == wp,
                                onClick = { onUpdateSettings { it.copy(wallpaper = wp) } },
                                label = { Text(wp.displayName) }
                            )
                        }
                    }
                    HorizontalDivider(modifier = Modifier.padding(vertical = 10.dp))
                    SettingsActionRow(
                        title = "Custom Homepage URL",
                        subtitle = settings.customHomepageUrl.ifBlank { "Rauf Browser clean start page" },
                        onClick = { showCustomHomeDialog = true }
                    )
                    SettingsSwitchRow("Show current date", checked = settings.showDateOnHome) { v -> onUpdateSettings { it.copy(showDateOnHome = v) } }
                    SettingsSwitchRow("Show quick-access shortcuts", checked = settings.showQuickShortcuts) { v -> onUpdateSettings { it.copy(showQuickShortcuts = v) } }
                    SettingsSwitchRow("Show frequently visited sites", checked = settings.showFrequentSites) { v -> onUpdateSettings { it.copy(showFrequentSites = v) } }
                    SettingsSwitchRow("Show recently visited sites", checked = settings.showRecentSites) { v -> onUpdateSettings { it.copy(showRecentSites = v) } }
                    SettingsSwitchRow("Show bookmarks on homepage", checked = settings.showBookmarksOnHome) { v -> onUpdateSettings { it.copy(showBookmarksOnHome = v) } }
                }
            }

            // 6. Language
            item {
                SettingsSectionCard(title = "6. Language", icon = Icons.Default.Language) {
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        items(SUPPORTED_LANGUAGES) { (code, label) ->
                            FilterChip(
                                selected = settings.languageCode == code,
                                onClick = { onUpdateSettings { it.copy(languageCode = code) } },
                                label = { Text(label) }
                            )
                        }
                    }
                }
            }

            // 7. Privacy
            item {
                SettingsSectionCard(title = "7. Privacy", icon = Icons.Default.PrivacyTip) {
                    SettingsSwitchRow("Send Do Not Track & GPC headers", "Requests websites not to track cross-site browsing", settings.doNotTrack) { v -> onUpdateSettings { it.copy(doNotTrack = v) } }
                    SettingsSwitchRow("Accept cookies", "Required for signing in on most websites", settings.acceptCookies) { v -> onUpdateSettings { it.copy(acceptCookies = v) } }
                    SettingsSwitchRow("Allow third-party cookies", "Blocked by default to limit cross-site tracking", settings.thirdPartyCookies) { v -> onUpdateSettings { it.copy(thirdPartyCookies = v) } }
                    HorizontalDivider(modifier = Modifier.padding(vertical = 10.dp))
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedButton(
                            onClick = onClearCache,
                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 10.dp),
                            modifier = Modifier
                                .weight(1f)
                                .heightIn(min = 48.dp)
                                .testTag("clear_cache_button")
                        ) {
                            Text("Clear Cache", maxLines = 1, overflow = TextOverflow.Ellipsis)
                        }
                        OutlinedButton(
                            onClick = onClearCookies,
                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 10.dp),
                            modifier = Modifier
                                .weight(1f)
                                .heightIn(min = 48.dp)
                                .testTag("clear_cookies_button")
                        ) {
                            Text("Clear Cookies", maxLines = 1, overflow = TextOverflow.Ellipsis)
                        }
                    }
                    Spacer(Modifier.height(8.dp))
                    OutlinedButton(
                        onClick = { showClearAllConfirm = true },
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(min = 48.dp)
                            .testTag("clear_all_data_button")
                    ) {
                        Icon(Icons.Default.DeleteForever, contentDescription = null, tint = DangerCrimson, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(6.dp))
                        Text("Clear All Browsing Data", color = DangerCrimson, maxLines = 1, overflow = TextOverflow.Ellipsis)
                    }
                }
            }

            // 8. Security & Site Permissions
            item {
                SettingsSectionCard(title = "8. Security & Site Permissions", icon = Icons.Default.Security) {
                    Text(
                        text = "Always Enforced: SSL certificate errors are strictly blocked (never bypassed) and insecure mixed content is disabled.",
                        style = MaterialTheme.typography.bodySmall,
                        color = SecureGreen
                    )
                    Spacer(Modifier.height(8.dp))
                    SettingsSwitchRow("HTTPS-First upgrade", "Upgrades HTTP links to encrypted HTTPS", settings.httpsOnlyMode) { v -> onUpdateSettings { it.copy(httpsOnlyMode = v) } }
                    SettingsSwitchRow("Enable JavaScript", "Allow interactive web applications", settings.javaScriptEnabled) { v -> onUpdateSettings { it.copy(javaScriptEnabled = v) } }
                    SettingsSwitchRow("Block pop-ups", "Prevents unsolicited windows", settings.blockPopups) { v -> onUpdateSettings { it.copy(blockPopups = v) } }
                    SettingsSwitchRow("Safe external app link guard", "Ask before opening mailto:, tel:, or external apps", settings.safeExternalLinks) { v -> onUpdateSettings { it.copy(safeExternalLinks = v) } }
                    HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
                    SettingsSwitchRow("Allow site location requests", checked = settings.allowLocation) { v -> onUpdateSettings { it.copy(allowLocation = v) } }
                    SettingsSwitchRow("Allow site camera requests", checked = settings.allowCamera) { v -> onUpdateSettings { it.copy(allowCamera = v) } }
                    SettingsSwitchRow("Allow site microphone requests", checked = settings.allowMicrophone) { v -> onUpdateSettings { it.copy(allowMicrophone = v) } }
                }
            }

            // 9. Downloads, Notifications & Default Browser
            item {
                SettingsSectionCard(title = "9. Downloads, Alerts & Default Browser", icon = Icons.Default.Download) {
                    SettingsActionRow("Open Download Manager", "Saved via Android DownloadManager", onClick = onOpenDownloads)
                    SettingsSwitchRow(
                        title = "Download progress & completion alerts",
                        subtitle = "Uses Android system notifications",
                        checked = settings.notificationsEnabled,
                        onCheckedChange = { enabled ->
                            onUpdateSettings { it.copy(notificationsEnabled = enabled) }
                            if (enabled) onRequestNotificationPermission()
                        }
                    )
                    SettingsActionRow(
                        title = "Set Rauf Browser as Default",
                        subtitle = "Open Android Default Apps settings",
                        leadingIcon = Icons.Default.OpenInBrowser,
                        onClick = onOpenDefaultBrowserSettings
                    )
                }
            }

            // 10. About & Privacy Policy
            item {
                SettingsSectionCard(title = "10. About & Privacy Policy", icon = Icons.Default.Info) {
                    SettingsActionRow("Privacy Policy", "Local-first data & Incognito disclosures", Icons.Default.Policy, onOpenPrivacyPolicy)
                    HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))
                    SettingsActionRow("About Rauf Browser", "Developer: RAUF • Chromium WebView & Rauf.ai", Icons.Default.Info, onOpenAbout)
                }
            }
            }
        }
    }

    if (showApiKeysDialog) {
        val slots = remember { AuthKeySlot.entries.filter { it != AuthKeySlot.NONE } }
        val drafts = remember(settings.apiKeys) {
            mutableStateMapOf<String, String>().apply {
                slots.forEach { put(it.keyName, settings.apiKeys[it.keyName].orEmpty()) }
            }
        }
        AlertDialog(
            onDismissRequest = { showApiKeysDialog = false },
            title = { Text("Local API Keys") },
            text = {
                LazyColumn(
                    modifier = Modifier.heightIn(max = 380.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(slots, key = { it.keyName }) { slot ->
                        OutlinedTextField(
                            value = drafts[slot.keyName].orEmpty(),
                            onValueChange = { drafts[slot.keyName] = it },
                            label = { Text(slot.label) },
                            placeholder = { Text(slot.placeholder) },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    slots.forEach { slot ->
                        onSetApiKey(slot.keyName, drafts[slot.keyName].orEmpty())
                    }
                    showApiKeysDialog = false
                }) { Text("Save Keys") }
            },
            dismissButton = {
                TextButton(onClick = { showApiKeysDialog = false }) { Text("Cancel") }
            }
        )
    }

    if (showCustomHomeDialog) {
        var homeUrl by remember { mutableStateOf(settings.customHomepageUrl) }
        AlertDialog(
            onDismissRequest = { showCustomHomeDialog = false },
            title = { Text("Custom Homepage URL") },
            text = {
                OutlinedTextField(
                    value = homeUrl,
                    onValueChange = { homeUrl = it },
                    label = { Text("URL (leave blank for Rauf Start Page)") },
                    placeholder = { Text("https://www.google.com") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    onUpdateSettings { it.copy(customHomepageUrl = homeUrl.trim()) }
                    showCustomHomeDialog = false
                }) { Text("Save") }
            },
            dismissButton = {
                TextButton(onClick = { showCustomHomeDialog = false }) { Text("Cancel") }
            }
        )
    }

    if (showClearAllConfirm) {
        AlertDialog(
            onDismissRequest = { showClearAllConfirm = false },
            title = { Text("Clear All Browsing Data?") },
            text = { Text("This permanently deletes local browsing history, cookies, cache, and site storage. Bookmarks and downloaded files are kept.") },
            confirmButton = {
                TextButton(onClick = {
                    onClearAllData()
                    showClearAllConfirm = false
                }) { Text("Clear Everything", color = DangerCrimson) }
            },
            dismissButton = {
                TextButton(onClick = { showClearAllConfirm = false }) { Text("Cancel") }
            }
        )
    }
}

@Composable
private fun SettingsSectionCard(
    title: String,
    icon: ImageVector,
    content: @Composable () -> Unit
) {
    GlassCard(shape = RoundedCornerShape(22.dp), modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(20.dp))
                Spacer(Modifier.width(10.dp))
                Text(title, style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onSurface)
            }
            Spacer(Modifier.height(12.dp))
            content()
        }
    }
}

@Composable
private fun SettingsSwitchRow(
    title: String,
    subtitle: String? = null,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 52.dp)
            .clip(RoundedCornerShape(12.dp))
            .clickable { onCheckedChange(!checked) }
            .padding(vertical = 6.dp, horizontal = 2.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(text = title, style = MaterialTheme.typography.titleSmall)
            if (!subtitle.isNullOrBlank()) {
                Text(text = subtitle, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
        Spacer(Modifier.width(12.dp))
        Switch(checked = checked, onCheckedChange = onCheckedChange)
    }
}

@Composable
private fun SettingsActionRow(
    title: String,
    subtitle: String,
    leadingIcon: ImageVector? = null,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 52.dp)
            .clip(RoundedCornerShape(12.dp))
            .clickable(onClick = onClick)
            .padding(vertical = 10.dp, horizontal = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (leadingIcon != null) {
            Icon(leadingIcon, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(20.dp))
            Spacer(Modifier.width(12.dp))
        }
        Column(modifier = Modifier.weight(1f)) {
            Text(text = title, style = MaterialTheme.typography.titleSmall, maxLines = 1, overflow = TextOverflow.Ellipsis)
            Text(text = subtitle, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 2, overflow = TextOverflow.Ellipsis)
        }
        Spacer(Modifier.width(6.dp))
        Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}
