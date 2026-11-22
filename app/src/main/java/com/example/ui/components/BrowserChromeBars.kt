package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Apps
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Computer
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Fullscreen
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Public
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.StarBorder
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material.icons.filled.WarningAmber
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.core.SearchEngine
import com.example.core.UrlResolver
import com.example.data.local.BookmarkEntity
import com.example.data.local.HistoryEntity
import com.example.engine.BrowserTab
import com.example.ui.theme.ElectricPurpleBright
import com.example.ui.theme.LocalRaufGlassPalette
import com.example.ui.theme.MonoFontFamily
import com.example.ui.theme.SecureGreen
import com.example.ui.theme.WarningAmber

@Composable
fun BrowserTopAddressBar(
    tab: BrowserTab,
    searchEngine: SearchEngine,
    isBookmarked: Boolean,
    bookmarks: List<BookmarkEntity>,
    history: List<HistoryEntity>,
    onSubmitUrl: (String) -> Unit,
    onAskSearchAi: (String) -> Unit,
    onReloadOrStop: () -> Unit,
    onToggleBookmark: () -> Unit,
    onToggleDesktop: () -> Unit,
    onOpenAiPanel: () -> Unit
) {
    val glass = LocalRaufGlassPalette.current
    val focusManager = LocalFocusManager.current
    var isFocused by remember { mutableStateOf(false) }
    var textValue by remember(tab.id) { mutableStateOf(if (tab.isHome) "" else tab.url) }

    LaunchedEffect(tab.url, tab.id, isFocused) {
        if (!isFocused) {
            textValue = if (tab.isHome) "" else tab.url
        }
    }

    val suggestions = remember(textValue, bookmarks, history, isFocused) {
        if (!isFocused || textValue.isBlank()) {
            emptyList()
        } else {
            val q = textValue.trim().lowercase()
            val bmMatches = bookmarks.filter {
                it.title.lowercase().contains(q) || it.url.lowercase().contains(q)
            }.take(3).map { Triple(it.title, it.url, true) }

            val histMatches = history.filter {
                (it.title.lowercase().contains(q) || it.url.lowercase().contains(q)) &&
                    bmMatches.none { b -> b.second == it.url }
            }.take(4).map { Triple(it.title, it.url, false) }

            bmMatches + histMatches
        }
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(glass.glassFillStrong)
            .statusBarsPadding()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 10.dp, vertical = 7.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Rounded omnibox pill fitted cleanly for all Android screen widths
            GlassCard(
                shape = RoundedCornerShape(24.dp),
                accentHighlight = isFocused || tab.isIncognito,
                modifier = Modifier
                    .weight(1f)
                    .height(46.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 11.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Security / Incognito indicator
                    Icon(
                        imageVector = when {
                            tab.isIncognito -> Icons.Default.VisibilityOff
                            tab.isHome -> Icons.Default.Search
                            tab.isSecure -> Icons.Default.Lock
                            else -> Icons.Default.WarningAmber
                        },
                        contentDescription = when {
                            tab.isIncognito -> "Incognito tab"
                            tab.isSecure -> "Secure HTTPS connection"
                            else -> "Unencrypted HTTP connection"
                        },
                        tint = when {
                            tab.isIncognito -> glass.accentGlow
                            tab.isHome -> MaterialTheme.colorScheme.onSurfaceVariant
                            tab.isSecure -> SecureGreen
                            else -> WarningAmber
                        },
                        modifier = Modifier.size(18.dp)
                    )

                    Spacer(Modifier.width(8.dp))

                    Box(
                        modifier = Modifier.weight(1f),
                        contentAlignment = Alignment.CenterStart
                    ) {
                        if (textValue.isEmpty() && !isFocused) {
                            Text(
                                text = if (tab.isIncognito) {
                                    "Search privately or type URL"
                                } else {
                                    "Search or type URL"
                                },
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }

                        BasicTextField(
                            value = if (!isFocused && !tab.isHome) UrlResolver.extractHost(tab.url) else textValue,
                            onValueChange = { textValue = it },
                            singleLine = true,
                            textStyle = MaterialTheme.typography.bodyMedium.copy(
                                color = MaterialTheme.colorScheme.onSurface,
                                fontFamily = if (!tab.isHome) MonoFontFamily else MaterialTheme.typography.bodyMedium.fontFamily
                            ),
                            cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
                            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Go),
                            keyboardActions = KeyboardActions(
                                onGo = {
                                    if (textValue.isNotBlank()) {
                                        onSubmitUrl(textValue)
                                        focusManager.clearFocus()
                                    }
                                }
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("browser_omnibox_input")
                                .onFocusChanged { state ->
                                    isFocused = state.isFocused
                                    if (state.isFocused && !tab.isHome) {
                                        textValue = tab.url
                                    }
                                }
                        )
                    }

                    if (isFocused && textValue.isNotEmpty()) {
                        IconButton(
                            onClick = { textValue = "" },
                            modifier = Modifier.size(36.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Clear address bar",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    } else if (!tab.isHome) {
                        if (tab.isDesktopMode) {
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.85f))
                                    .clickable(onClick = onToggleDesktop)
                                    .padding(horizontal = 6.dp, vertical = 4.dp)
                                    .testTag("desktop_mode_indicator"),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Computer,
                                    contentDescription = "Desktop mode active, tap for Mobile site",
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(15.dp)
                                )
                            }
                            Spacer(Modifier.width(2.dp))
                        }
                        IconButton(
                            onClick = onToggleBookmark,
                            modifier = Modifier
                                .size(36.dp)
                                .testTag("bookmark_toggle_button")
                        ) {
                            Icon(
                                imageVector = if (isBookmarked) Icons.Default.Star else Icons.Default.StarBorder,
                                contentDescription = if (isBookmarked) "Remove bookmark" else "Add bookmark",
                                tint = if (isBookmarked) ElectricPurpleBright else MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        IconButton(
                            onClick = onReloadOrStop,
                            modifier = Modifier
                                .size(36.dp)
                                .testTag("reload_stop_button")
                        ) {
                            Icon(
                                imageVector = if (tab.isLoading) Icons.Default.Close else Icons.Default.Refresh,
                                contentDescription = if (tab.isLoading) "Stop loading" else "Reload page",
                                tint = MaterialTheme.colorScheme.onSurface,
                                modifier = Modifier.size(19.dp)
                            )
                        }
                    }
                }
            }

            Spacer(Modifier.width(8.dp))

            // Clean Duck.ai-style Rauf.ai (ChatGPT) button
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f))
                    .clickable(onClick = onOpenAiPanel)
                    .testTag("open_ai_assist_button"),
                contentAlignment = Alignment.Center
            ) {
                AiModelLogo(
                    style = com.example.api.AiLogoStyle.OPENAI_GPT,
                    size = 28.dp
                )
            }
        }

        BrowserProgressBar(
            progress = tab.progress,
            isLoading = tab.isLoading,
            isIncognito = tab.isIncognito
        )

        if (!tab.isLoading) {
            HorizontalDivider(
                thickness = 0.5.dp,
                color = MaterialTheme.colorScheme.outline.copy(alpha = 0.4f)
            )
        }

        // Smart Omnibox Suggestions dropdown
        AnimatedVisibility(visible = isFocused && textValue.isNotBlank()) {
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 260.dp)
                    .background(glass.glassFillStrong)
                    .padding(horizontal = 12.dp, vertical = 6.dp)
            ) {
                item {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(14.dp))
                            .clickable {
                                val q = textValue
                                focusManager.clearFocus()
                                onSubmitUrl(q)
                            }
                            .padding(horizontal = 12.dp, vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Public,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(Modifier.width(10.dp))
                        Text(
                            text = "Go / Search: $textValue",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurface,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }

                item {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(14.dp))
                            .clickable {
                                val q = textValue
                                focusManager.clearFocus()
                                onAskSearchAi(q)
                            }
                            .padding(horizontal = 12.dp, vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.AutoAwesome,
                            contentDescription = null,
                            tint = ElectricPurpleBright,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(Modifier.width(10.dp))
                        Text(
                            text = "Ask Rauf.ai (ChatGPT): \"$textValue\"",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.primary,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }

                items(suggestions) { (title, url, isBm) ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .clickable {
                                focusManager.clearFocus()
                                onSubmitUrl(url)
                            }
                            .padding(horizontal = 12.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = if (isBm) Icons.Default.Bookmark else Icons.Default.History,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(Modifier.width(10.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = title,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurface,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Text(
                                text = url,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun BrowserBottomBar(
    tab: BrowserTab,
    tabCount: Int,
    onBack: () -> Unit,
    onForward: () -> Unit,
    onHome: () -> Unit,
    onOpenTabs: () -> Unit,
    onNewTab: () -> Unit,
    onNewIncognitoTab: () -> Unit,
    onOpenBookmarks: () -> Unit,
    onOpenHistory: () -> Unit,
    onOpenDownloads: () -> Unit,
    onOpenApiHub: () -> Unit,
    onOpenDownloaderHub: () -> Unit,
    onOpenUtilities: () -> Unit,
    onOpenApiStatus: () -> Unit,
    onOpenCommandPalette: () -> Unit,
    onToggleDesktop: () -> Unit,
    onToggleFullScreen: () -> Unit,
    onOpenFindInPage: () -> Unit,
    onSharePage: () -> Unit,
    onOpenSettings: () -> Unit
) {
    val glass = LocalRaufGlassPalette.current
    var menuExpanded by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(glass.glassFillStrong)
            .navigationBarsPadding()
    ) {
        HorizontalDivider(
            thickness = 0.5.dp,
            color = MaterialTheme.colorScheme.outline.copy(alpha = 0.45f)
        )
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 6.dp, vertical = 4.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
        IconButton(
            onClick = onBack,
            enabled = !tab.isHome || tab.canGoBack,
            modifier = Modifier
                .size(48.dp)
                .testTag("nav_back_button")
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = "Navigate back",
                tint = if (!tab.isHome || tab.canGoBack) {
                    MaterialTheme.colorScheme.onSurface
                } else {
                    MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.38f)
                }
            )
        }

        IconButton(
            onClick = onForward,
            enabled = tab.canGoForward,
            modifier = Modifier
                .size(48.dp)
                .testTag("nav_forward_button")
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                contentDescription = "Navigate forward",
                tint = if (tab.canGoForward) {
                    MaterialTheme.colorScheme.onSurface
                } else {
                    MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.38f)
                }
            )
        }

        IconButton(
            onClick = onHome,
            modifier = Modifier
                .size(48.dp)
                .testTag("nav_home_button")
        ) {
            Icon(
                imageVector = Icons.Default.Home,
                contentDescription = "Go to home screen",
                tint = if (tab.isHome) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
            )
        }

        // Tab count button
        Box(
            modifier = Modifier
                .size(48.dp)
                .clip(RoundedCornerShape(14.dp))
                .clickable(onClick = onOpenTabs)
                .testTag("nav_tabs_button"),
            contentAlignment = Alignment.Center
        ) {
            Box(
                modifier = Modifier
                    .size(26.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .border(
                        width = 1.8.dp,
                        color = if (tab.isIncognito) glass.accentGlow else MaterialTheme.colorScheme.onSurface,
                        shape = RoundedCornerShape(8.dp)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = if (tabCount > 99) "99+" else tabCount.toString(),
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = if (tab.isIncognito) glass.accentGlow else MaterialTheme.colorScheme.onSurface
                )
            }
        }

        // Browser Menu
        Box {
            IconButton(
                onClick = { menuExpanded = true },
                modifier = Modifier
                    .size(48.dp)
                    .testTag("nav_menu_button")
            ) {
                Icon(
                    imageVector = Icons.Default.MoreVert,
                    contentDescription = "Open browser menu",
                    tint = MaterialTheme.colorScheme.onSurface
                )
            }

            DropdownMenu(
                expanded = menuExpanded,
                onDismissRequest = { menuExpanded = false },
                containerColor = MaterialTheme.colorScheme.surface
            ) {
                DropdownMenuItem(
                    text = { Text("New Tab") },
                    leadingIcon = { Icon(Icons.Default.Public, contentDescription = null) },
                    onClick = { menuExpanded = false; onNewTab() }
                )
                DropdownMenuItem(
                    text = { Text("New Incognito Tab") },
                    leadingIcon = { Icon(Icons.Default.VisibilityOff, contentDescription = null) },
                    onClick = { menuExpanded = false; onNewIncognitoTab() }
                )
                HorizontalDivider()
                DropdownMenuItem(
                    text = { Text("Bookmarks") },
                    leadingIcon = { Icon(Icons.Default.Bookmark, contentDescription = null) },
                    onClick = { menuExpanded = false; onOpenBookmarks() }
                )
                DropdownMenuItem(
                    text = { Text("History") },
                    leadingIcon = { Icon(Icons.Default.History, contentDescription = null) },
                    onClick = { menuExpanded = false; onOpenHistory() }
                )
                DropdownMenuItem(
                    text = { Text("Downloads") },
                    leadingIcon = { Icon(Icons.Default.Download, contentDescription = null) },
                    onClick = { menuExpanded = false; onOpenDownloads() }
                )
                DropdownMenuItem(
                    text = { Text("Rauf Tools") },
                    leadingIcon = { Icon(Icons.Default.Apps, contentDescription = null) },
                    onClick = { menuExpanded = false; onOpenApiHub() }
                )
                DropdownMenuItem(
                    text = { Text("Downloader Hub (Auto-Detect)") },
                    leadingIcon = { Icon(Icons.Default.Download, contentDescription = null) },
                    onClick = { menuExpanded = false; onOpenDownloaderHub() }
                )
                DropdownMenuItem(
                    text = { Text("Offline Utilities") },
                    leadingIcon = { Icon(Icons.Default.Settings, contentDescription = null) },
                    onClick = { menuExpanded = false; onOpenUtilities() }
                )
                DropdownMenuItem(
                    text = { Text("API Status & Priority") },
                    leadingIcon = { Icon(Icons.Default.Shield, contentDescription = null) },
                    onClick = { menuExpanded = false; onOpenApiStatus() }
                )
                DropdownMenuItem(
                    text = { Text("Command Palette (Ctrl+K)") },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                    onClick = { menuExpanded = false; onOpenCommandPalette() }
                )
                HorizontalDivider()
                DropdownMenuItem(
                    text = { Text(if (tab.isDesktopMode) "Mobile Site" else "Desktop Site") },
                    leadingIcon = {
                        Icon(
                            Icons.Default.Computer,
                            contentDescription = null,
                            tint = if (tab.isDesktopMode) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                        )
                    },
                    onClick = { menuExpanded = false; onToggleDesktop() }
                )
                DropdownMenuItem(
                    text = { Text("Full-Screen Mode") },
                    leadingIcon = { Icon(Icons.Default.Fullscreen, contentDescription = null) },
                    onClick = { menuExpanded = false; onToggleFullScreen() }
                )
                if (!tab.isHome) {
                    DropdownMenuItem(
                        text = { Text("Find in Page") },
                        leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                        onClick = { menuExpanded = false; onOpenFindInPage() }
                    )
                    DropdownMenuItem(
                        text = { Text("Share Page") },
                        leadingIcon = { Icon(Icons.Default.Share, contentDescription = null) },
                        onClick = { menuExpanded = false; onSharePage() }
                    )
                }
                HorizontalDivider()
                DropdownMenuItem(
                    text = { Text("Settings & Privacy") },
                    leadingIcon = { Icon(Icons.Default.Settings, contentDescription = null) },
                    onClick = { menuExpanded = false; onOpenSettings() }
                )
            }
        }
        }
    }
}
