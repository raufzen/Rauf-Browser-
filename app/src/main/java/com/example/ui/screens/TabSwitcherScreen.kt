package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Computer
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material.icons.filled.Public
import androidx.compose.material.icons.filled.Restore
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.core.UrlResolver
import com.example.engine.BrowserTab
import com.example.engine.ClosedTabRecord
import com.example.ui.components.FaviconBadge
import com.example.ui.components.GlassCard
import com.example.ui.components.SubScreenTopBar
import com.example.ui.theme.DarkPurple
import com.example.ui.theme.ElectricPurple
import com.example.ui.theme.IncognitoAccent
import com.example.ui.theme.LocalRaufGlassPalette

@Composable
fun TabSwitcherScreen(
    tabs: List<BrowserTab>,
    activeTabId: String,
    closedTabs: List<ClosedTabRecord>,
    onBack: () -> Unit,
    onSelectTab: (String) -> Unit,
    onCloseTab: (String) -> Unit,
    onCloseAllTabs: (Boolean) -> Unit,
    onNewTab: (Boolean) -> Unit,
    onRestoreClosedTab: () -> Unit
) {
    BackHandler(onBack = onBack)

    val activeIsIncognito = tabs.firstOrNull { it.id == activeTabId }?.isIncognito ?: false
    var showingIncognito by remember { mutableStateOf(activeIsIncognito) }

    val normalTabs = tabs.filter { !it.isIncognito }
    val privateTabs = tabs.filter { it.isIncognito }
    val visibleTabs = if (showingIncognito) privateTabs else normalTabs
    val glass = LocalRaufGlassPalette.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        SubScreenTopBar(
            title = "Tabs",
            subtitle = "${normalTabs.size} standard • ${privateTabs.size} private",
            onBack = onBack,
            actions = {
                if (visibleTabs.isNotEmpty()) {
                    IconButton(
                        onClick = { onCloseAllTabs(showingIncognito) },
                        modifier = Modifier.testTag("close_all_tabs_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.DeleteSweep,
                            contentDescription = "Close all tabs",
                            tint = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }
        )

        // Normal vs Incognito segmented bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 10.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            TabSegmentPill(
                label = "Standard (${normalTabs.size})",
                icon = Icons.Default.Public,
                selected = !showingIncognito,
                accent = ElectricPurple,
                onClick = { showingIncognito = false },
                modifier = Modifier
                    .weight(1f)
                    .testTag("segment_standard_tabs")
            )
            TabSegmentPill(
                label = "Incognito (${privateTabs.size})",
                icon = Icons.Default.VisibilityOff,
                selected = showingIncognito,
                accent = IncognitoAccent,
                onClick = { showingIncognito = true },
                modifier = Modifier
                    .weight(1f)
                    .testTag("segment_incognito_tabs")
            )
        }

        // Restore closed tab banner
        if (!showingIncognito && closedTabs.isNotEmpty()) {
            GlassCard(
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 4.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 14.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Restore,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(Modifier.width(8.dp))
                        Text(
                            text = "Closed: ${closedTabs.first().title}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurface,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                    TextButton(
                        onClick = onRestoreClosedTab,
                        modifier = Modifier.testTag("restore_closed_tab_button")
                    ) {
                        Text("Reopen")
                    }
                }
            }
        }

        // Tab preview grid
        if (visibleTabs.isEmpty()) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(32.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        imageVector = if (showingIncognito) Icons.Default.VisibilityOff else Icons.Default.Public,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(48.dp)
                    )
                    Spacer(Modifier.height(12.dp))
                    Text(
                        text = if (showingIncognito) "No open Incognito tabs" else "No standard tabs open",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(Modifier.height(6.dp))
                    Text(
                        text = if (showingIncognito) {
                            "Private tabs stay isolated from local history and clear session data when closed."
                        } else {
                            "Tap New Tab below to start browsing."
                        },
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        } else {
            LazyVerticalGrid(
                columns = GridCells.Adaptive(minSize = 158.dp),
                modifier = Modifier.weight(1f),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(visibleTabs, key = { it.id }) { tab ->
                    TabPreviewCard(
                        tab = tab,
                        isActive = tab.id == activeTabId,
                        onSelect = { onSelectTab(tab.id) },
                        onClose = { onCloseTab(tab.id) }
                    )
                }
            }
        }

        // Bottom Dock
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(glass.glassFillStrong)
                .navigationBarsPadding()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            OutlinedButton(
                onClick = { onNewTab(true) },
                modifier = Modifier
                    .weight(1f)
                    .height(48.dp)
                    .testTag("new_incognito_tab_button")
            ) {
                Icon(Icons.Default.VisibilityOff, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(6.dp))
                Text("Private")
            }

            Button(
                onClick = { onNewTab(false) },
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                modifier = Modifier
                    .weight(1.3f)
                    .height(48.dp)
                    .testTag("new_standard_tab_button")
            ) {
                Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(6.dp))
                Text("New Tab")
            }
        }
    }
}

@Composable
private fun TabSegmentPill(
    label: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    selected: Boolean,
    accent: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .height(44.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(
                if (selected) accent.copy(alpha = 0.24f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)
            )
            .border(
                width = if (selected) 1.5.dp else 1.dp,
                color = if (selected) accent else MaterialTheme.colorScheme.outline.copy(alpha = 0.5f),
                shape = RoundedCornerShape(16.dp)
            )
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = if (selected) accent else MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(17.dp)
        )
        Spacer(Modifier.width(6.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.labelLarge,
            color = if (selected) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant,
            maxLines = 1
        )
    }
}

@Composable
private fun TabPreviewCard(
    tab: BrowserTab,
    isActive: Boolean,
    onSelect: () -> Unit,
    onClose: () -> Unit
) {
    GlassCard(
        shape = RoundedCornerShape(20.dp),
        accentHighlight = isActive,
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onSelect)
            .testTag("tab_card_${tab.id}")
    ) {
        Column {
            // Tab Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 10.dp, end = 4.dp, top = 6.dp, bottom = 6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                FaviconBadge(
                    title = tab.title,
                    favicon = tab.favicon,
                    accentColor = if (tab.isIncognito) IncognitoAccent else ElectricPurple,
                    size = 26.dp
                )
                Spacer(Modifier.width(8.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = tab.title,
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = if (tab.isHome) "Homepage" else UrlResolver.extractHost(tab.url),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                IconButton(
                    onClick = onClose,
                    modifier = Modifier.size(34.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Close tab ${tab.title}",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }

            // Tab Live Preview Thumbnail
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(0.85f)
                    .padding(horizontal = 8.dp, vertical = 6.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(
                        Brush.verticalGradient(
                            colors = if (tab.isIncognito) {
                                listOf(Color(0xFF1F1238), Color(0xFF0A0614))
                            } else {
                                listOf(DarkPurple.copy(alpha = 0.55f), MaterialTheme.colorScheme.surfaceVariant)
                            }
                        )
                    ),
                contentAlignment = Alignment.Center
            ) {
                if (tab.previewBitmap != null && !tab.isHome) {
                    Image(
                        bitmap = tab.previewBitmap.asImageBitmap(),
                        contentDescription = "Preview of ${tab.title}",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                } else {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = if (tab.isIncognito) Icons.Default.VisibilityOff else Icons.Default.Public,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.7f),
                            modifier = Modifier.size(32.dp)
                        )
                        Spacer(Modifier.height(6.dp))
                        Text(
                            text = if (tab.isHome) "RAUF HOME" else UrlResolver.extractHost(tab.url),
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                if (tab.isDesktopMode) {
                    Row(
                        modifier = Modifier
                            .align(Alignment.BottomEnd)
                            .padding(6.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color(0xCC120A24))
                            .padding(horizontal = 6.dp, vertical = 3.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Computer,
                            contentDescription = "Desktop mode",
                            tint = ElectricPurple,
                            modifier = Modifier.size(12.dp)
                        )
                        Spacer(Modifier.width(3.dp))
                        Text(
                            text = "Desktop",
                            style = MaterialTheme.typography.labelSmall,
                            color = Color.White
                        )
                    }
                }
            }
        }
    }
}
