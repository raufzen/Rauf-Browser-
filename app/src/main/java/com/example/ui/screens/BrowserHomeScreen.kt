package com.example.ui.screens

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
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
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.R
import com.example.api.AiLogoStyle
import com.example.api.FeatureGroup
import com.example.core.UrlResolver
import com.example.data.local.BookmarkEntity
import com.example.data.local.BrowserSettings
import com.example.data.local.HistoryEntity
import com.example.data.local.ShortcutEntity
import com.example.ui.components.AiModelLogo
import com.example.ui.components.FaviconBadge
import com.example.ui.components.GlassCard
import com.example.ui.components.WallpaperBackground
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun BrowserHomeScreen(
    isIncognito: Boolean,
    settings: BrowserSettings,
    shortcuts: List<ShortcutEntity>,
    frequentSites: List<HistoryEntity>,
    recentHistory: List<HistoryEntity>,
    bookmarks: List<BookmarkEntity>,
    onSearchOrNavigate: (String) -> Unit,
    onOpenUrl: (String) -> Unit,
    onAskSearchAi: (String) -> Unit,
    onOpenIncognitoTab: () -> Unit,
    onOpenBookmarks: () -> Unit,
    onOpenHistory: () -> Unit,
    onOpenApiHub: () -> Unit,
    onOpenDownloaderHub: () -> Unit,
    onOpenUtilities: () -> Unit,
    onOpenApiStatus: () -> Unit,
    onOpenCommandPalette: () -> Unit,
    onOpenFeatureTool: (FeatureGroup) -> Unit,
    onOpenSettings: () -> Unit,
    onCustomizeHome: () -> Unit,
    onAddShortcutClick: () -> Unit,
    onDeleteShortcut: (Long) -> Unit
) {
    var searchQuery by remember { mutableStateOf("") }
    val formattedDate = remember {
        SimpleDateFormat("EEEE, MMM d", Locale.getDefault()).format(Date())
    }

    Box(modifier = Modifier.fillMaxSize()) {
        WallpaperBackground(
            wallpaper = settings.wallpaper,
            isIncognito = isIncognito
        )

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .widthIn(max = 640.dp)
                .align(Alignment.TopCenter),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 14.dp),
            verticalArrangement = Arrangement.spacedBy(22.dp)
        ) {
            // Clean Minimal Top Utility Row
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (settings.showDateOnHome) {
                        Text(
                            text = formattedDate,
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    } else {
                        Spacer(Modifier.width(1.dp))
                    }

                    Row(horizontalArrangement = Arrangement.spacedBy(2.dp)) {
                        if (!isIncognito) {
                            IconButton(
                                onClick = onOpenIncognitoTab,
                                modifier = Modifier
                                    .size(48.dp)
                                    .testTag("home_incognito_button")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.VisibilityOff,
                                    contentDescription = "New Private Tab",
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                        IconButton(
                            onClick = onCustomizeHome,
                            modifier = Modifier
                                .size(48.dp)
                                .testTag("home_customize_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Palette,
                                contentDescription = "Customize homepage",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        IconButton(
                            onClick = onOpenSettings,
                            modifier = Modifier
                                .size(48.dp)
                                .testTag("home_settings_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Settings,
                                contentDescription = "Settings",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                }
            }

            // DuckDuckGo-style Centered Minimal Hero Emblem & Brand
            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 18.dp, bottom = 4.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    if (isIncognito) {
                        Box(
                            modifier = Modifier
                                .size(64.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.primaryContainer),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.VisibilityOff,
                                contentDescription = "Private Mode",
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(32.dp)
                            )
                        }
                    } else {
                        Image(
                            painter = painterResource(id = R.drawable.rauf_browser_icon_fg_1795338224771),
                            contentDescription = "Rauf logo",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier
                                .size(64.dp)
                                .clip(CircleShape)
                        )
                    }

                    Spacer(Modifier.height(14.dp))

                    Text(
                        text = if (isIncognito) "Private Tab" else "Rauf",
                        style = MaterialTheme.typography.headlineLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground,
                        textAlign = TextAlign.Center
                    )

                    Spacer(Modifier.height(4.dp))

                    Text(
                        text = if (isIncognito) {
                            "No history saved • Session clears on close"
                        } else {
                            "Clean. Fast. Private."
                        },
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // DuckDuckGo-style Search + Rauf.ai Pill Bar
            item {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    GlassCard(
                        shape = RoundedCornerShape(28.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Search,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(Modifier.width(12.dp))
                            Box(modifier = Modifier.weight(1f)) {
                                if (searchQuery.isEmpty()) {
                                    Text(
                                        text = if (isIncognito) "Search privately or type URL" else "Search or type URL",
                                        style = MaterialTheme.typography.bodyLarge,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                                BasicTextField(
                                    value = searchQuery,
                                    onValueChange = { searchQuery = it },
                                    singleLine = true,
                                    textStyle = MaterialTheme.typography.bodyLarge.copy(
                                        color = MaterialTheme.colorScheme.onSurface
                                    ),
                                    cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
                                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                                    keyboardActions = KeyboardActions(
                                        onSearch = {
                                            if (searchQuery.isNotBlank()) {
                                                onSearchOrNavigate(searchQuery)
                                                searchQuery = ""
                                            }
                                        }
                                    ),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .testTag("home_search_input")
                                )
                            }
                            Spacer(Modifier.width(8.dp))

                            // Clean Duck.ai-style Rauf.ai Chip
                            Row(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(20.dp))
                                    .background(MaterialTheme.colorScheme.surfaceVariant)
                                    .clickable {
                                        val q = searchQuery.trim()
                                        onAskSearchAi(q)
                                        searchQuery = ""
                                    }
                                    .padding(horizontal = 10.dp, vertical = 6.dp)
                                    .testTag("home_ai_search_chip"),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                AiModelLogo(style = AiLogoStyle.OPENAI_GPT, size = 18.dp)
                                Spacer(Modifier.width(6.dp))
                                Text(
                                    text = "Rauf.ai",
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.SemiBold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }
                    }

                    // Minimal Quick-Tool Pills (lightweight, spacious, zero clutter)
                    if (!isIncognito) {
                        LazyRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            contentPadding = PaddingValues(horizontal = 2.dp)
                        ) {
                            val quickPills = listOf(
                                Triple("Rauf.ai (ChatGPT)", "home_rauf_ai_pill") { onAskSearchAi("") },
                                Triple("Downloader", "home_downloader_btn", onOpenDownloaderHub),
                                Triple("Tools", "home_api_hub_card", onOpenApiHub),
                                Triple("Utilities", "home_utilities_btn", onOpenUtilities),
                                Triple("API Status", "home_status_btn", onOpenApiStatus),
                                Triple("Search All", "home_palette_btn", onOpenCommandPalette)
                            )
                            items(quickPills) { (label, tag, action) ->
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(18.dp))
                                        .background(MaterialTheme.colorScheme.surface)
                                        .border(
                                            0.75.dp,
                                            MaterialTheme.colorScheme.outline.copy(alpha = 0.65f),
                                            RoundedCornerShape(18.dp)
                                        )
                                        .clickable(onClick = action)
                                        .padding(horizontal = 14.dp, vertical = 8.dp)
                                        .testTag(tag)
                                ) {
                                    Text(
                                        text = label,
                                        style = MaterialTheme.typography.labelMedium,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Incognito Honest Notice
            if (isIncognito) {
                item {
                    GlassCard(shape = RoundedCornerShape(18.dp), modifier = Modifier.fillMaxWidth()) {
                        Column(modifier = Modifier.padding(18.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Shield, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(20.dp))
                                Spacer(Modifier.width(10.dp))
                                Text(stringResource(R.string.incognito_title), style = MaterialTheme.typography.titleSmall)
                            }
                            Spacer(Modifier.height(8.dp))
                            Text(
                                text = stringResource(R.string.incognito_disclaimer),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }

            // Airy, Unboxed DuckDuckGo-style Favorites / Shortcuts Grid
            if (settings.showQuickShortcuts && !isIncognito) {
                item {
                    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                        val rows = (shortcuts + listOf(null)).chunked(4)
                        rows.forEach { rowItems ->
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceEvenly
                            ) {
                                rowItems.forEach { item ->
                                    if (item == null) {
                                        Column(
                                            modifier = Modifier
                                                .weight(1f)
                                                .clip(RoundedCornerShape(14.dp))
                                                .clickable(onClick = onAddShortcutClick)
                                                .padding(vertical = 4.dp)
                                                .testTag("add_shortcut_tile"),
                                            horizontalAlignment = Alignment.CenterHorizontally
                                        ) {
                                            Box(
                                                modifier = Modifier
                                                    .size(48.dp)
                                                    .clip(RoundedCornerShape(16.dp))
                                                    .background(MaterialTheme.colorScheme.surface)
                                                    .border(
                                                        0.75.dp,
                                                        MaterialTheme.colorScheme.outline.copy(alpha = 0.7f),
                                                        RoundedCornerShape(16.dp)
                                                    ),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Default.Add,
                                                    contentDescription = "Add shortcut",
                                                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                                                )
                                            }
                                            Spacer(Modifier.height(6.dp))
                                            Text(
                                                text = "Add",
                                                style = MaterialTheme.typography.labelSmall,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }
                                    } else {
                                        Column(
                                            modifier = Modifier
                                                .weight(1f)
                                                .clip(RoundedCornerShape(14.dp))
                                                .combinedClickable(
                                                    onClick = { onOpenUrl(item.url) },
                                                    onLongClick = { onDeleteShortcut(item.id) }
                                                )
                                                .padding(vertical = 4.dp),
                                            horizontalAlignment = Alignment.CenterHorizontally
                                        ) {
                                            FaviconBadge(
                                                title = item.title,
                                                accentColor = Color(item.accentHex),
                                                size = 48.dp
                                            )
                                            Spacer(Modifier.height(6.dp))
                                            Text(
                                                text = item.title,
                                                style = MaterialTheme.typography.labelSmall,
                                                color = MaterialTheme.colorScheme.onSurface,
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis
                                            )
                                        }
                                    }
                                }
                                repeat(4 - rowItems.size) { Spacer(Modifier.weight(1f)) }
                            }
                        }
                    }
                }
            }

            // Bookmarks (only shown lightly when non-empty)
            if (settings.showBookmarksOnHome && !isIncognito && bookmarks.isNotEmpty()) {
                item {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Bookmark, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(16.dp))
                                Spacer(Modifier.width(8.dp))
                                Text("Bookmarks", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                            TextButton(onClick = onOpenBookmarks) { Text("All") }
                        }
                        GlassCard(shape = RoundedCornerShape(18.dp), modifier = Modifier.fillMaxWidth()) {
                            Column(modifier = Modifier.padding(10.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                bookmarks.take(4).forEach { bm ->
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clip(RoundedCornerShape(12.dp))
                                            .clickable { onOpenUrl(bm.url) }
                                            .padding(horizontal = 8.dp, vertical = 8.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        FaviconBadge(title = bm.title, size = 32.dp)
                                        Spacer(Modifier.width(12.dp))
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(bm.title, style = MaterialTheme.typography.bodyMedium, maxLines = 1, overflow = TextOverflow.Ellipsis)
                                            Text(UrlResolver.extractHost(bm.url), style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 1)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // Recent History (lightweight)
            if (settings.showRecentSites && !isIncognito && recentHistory.isNotEmpty()) {
                item {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.History, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(16.dp))
                                Spacer(Modifier.width(8.dp))
                                Text("Recent", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                            TextButton(onClick = onOpenHistory) { Text("See all") }
                        }
                        GlassCard(shape = RoundedCornerShape(18.dp), modifier = Modifier.fillMaxWidth()) {
                            Column(modifier = Modifier.padding(10.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                recentHistory.take(4).forEach { entry ->
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clip(RoundedCornerShape(12.dp))
                                            .clickable { onOpenUrl(entry.url) }
                                            .padding(horizontal = 8.dp, vertical = 8.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        FaviconBadge(title = entry.title, size = 30.dp)
                                        Spacer(Modifier.width(12.dp))
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(entry.title, style = MaterialTheme.typography.bodyMedium, maxLines = 1, overflow = TextOverflow.Ellipsis)
                                            Text(UrlResolver.extractHost(entry.url), style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 1)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // Keep unused frequentSites / onOpenFeatureTool available when favorites are tapped via quick tools
            if (settings.showFrequentSites && !isIncognito && frequentSites.isNotEmpty() && shortcuts.isEmpty()) {
                item {
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        items(frequentSites.take(6), key = { "freq_${it.id}" }) { entry ->
                            GlassCard(
                                shape = RoundedCornerShape(16.dp),
                                modifier = Modifier.clickable { onOpenUrl(entry.url) }
                            ) {
                                Text(
                                    text = entry.title,
                                    style = MaterialTheme.typography.labelMedium,
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
                                    maxLines = 1
                                )
                            }
                        }
                    }
                }
            }

            item { Spacer(Modifier.height(16.dp)) }
        }
    }
}
