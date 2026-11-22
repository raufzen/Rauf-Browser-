package com.example.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.speech.tts.TextToSpeech
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
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.MonitorHeart
import androidx.compose.material.icons.filled.OpenInBrowser
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.StarBorder
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.api.FeatureGroup
import com.example.api.ProviderRegistry
import com.example.data.local.ToolHistoryEntity
import com.example.ui.components.GlassCard
import com.example.ui.components.SubScreenTopBar
import com.example.ui.theme.DangerCrimson
import com.example.ui.theme.MonoFontFamily
import com.example.ui.theme.SecureGreen
import com.example.viewmodel.ToolHubUiState
import java.util.Locale

private val HUB_CATEGORIES = listOf("All", "Favorites", "Search", "Media & Fun", "Downloaders", "AI")
private val TRANSLATE_TARGETS = listOf("en" to "English", "ur" to "Urdu", "ar" to "Arabic", "es" to "Spanish", "fr" to "French", "hi" to "Hindi", "id" to "Indonesian", "tr" to "Turkish")

@Composable
fun ApiHubScreen(
    hubState: ToolHubUiState,
    favoriteToolIds: Set<String>,
    disabledProviderIds: Set<String>,
    providerPriorityOrder: List<String>,
    toolHistory: List<ToolHistoryEntity>,
    onBack: () -> Unit,
    onSelectFeature: (FeatureGroup) -> Unit,
    onRunTool: (FeatureGroup, String, Map<String, String>, String?, Boolean) -> Unit,
    onToggleFavorite: (String) -> Unit,
    onRerunHistory: (ToolHistoryEntity) -> Unit,
    onClearHistory: () -> Unit,
    onOpenAiChat: () -> Unit,
    onOpenDownloaderHub: () -> Unit,
    onOpenUtilities: () -> Unit,
    onOpenApiStatus: () -> Unit,
    onOpenInBrowser: (String) -> Unit,
    onDownloadUrl: (String) -> Unit,
    onCopiedNotice: (String) -> Unit
) {
    BackHandler(onBack = onBack)
    val context = LocalContext.current

    var toolSearch by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf("All") }
    var queryInput by remember(hubState.selectedFeature) { mutableStateOf(hubState.lastQuery) }
    var selectedExplicitProviderId by remember(hubState.selectedFeature) { mutableStateOf<String?>(null) }
    var providerMenuExpanded by remember { mutableStateOf(false) }
    var targetLang by remember { mutableStateOf("en") }
    var showRawJson by remember { mutableStateOf(false) }

    // Android built-in Text-To-Speech for instant pronunciation / TTS
    var ttsEngine by remember { mutableStateOf<TextToSpeech?>(null) }
    DisposableEffect(context) {
        var engine: TextToSpeech? = null
        engine = TextToSpeech(context) { status ->
            if (status == TextToSpeech.SUCCESS) {
                engine?.language = Locale.getDefault()
            }
        }
        ttsEngine = engine
        onDispose {
            engine.stop()
            engine.shutdown()
        }
    }

    LaunchedEffect(hubState.lastQuery) {
        if (hubState.lastQuery.isNotBlank()) queryInput = hubState.lastQuery
    }

    val filteredFeatures = remember(toolSearch, selectedCategory, favoriteToolIds) {
        val q = toolSearch.trim().lowercase()
        FeatureGroup.entries.filter { feat ->
            val catOk = when (selectedCategory) {
                "All" -> true
                "Favorites" -> feat.key in favoriteToolIds
                else -> feat.category == selectedCategory
            }
            val searchOk = q.isEmpty() ||
                feat.label.lowercase().contains(q) ||
                feat.description.lowercase().contains(q) ||
                feat.key.lowercase().contains(q)
            catOk && searchOk
        }
    }

    val providersForSelected = remember(hubState.selectedFeature, disabledProviderIds, providerPriorityOrder) {
        ProviderRegistry.orderedProvidersFor(hubState.selectedFeature, disabledProviderIds, providerPriorityOrder)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .navigationBarsPadding()
    ) {
        SubScreenTopBar(
            title = "Rauf Tools",
            subtitle = "${ProviderRegistry.ALL_PROVIDERS.size} public API endpoints • Primary → Fallback",
            onBack = onBack,
            actions = {
                IconButton(onClick = onOpenUtilities, modifier = Modifier.testTag("hub_open_utilities")) {
                    Icon(Icons.Default.Build, contentDescription = "Offline Utilities")
                }
                IconButton(onClick = onOpenApiStatus, modifier = Modifier.testTag("hub_open_api_status")) {
                    Icon(Icons.Default.MonitorHeart, contentDescription = "API Status")
                }
            }
        )

        LazyColumn(
            modifier = Modifier.weight(1f),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Quick Search + Dedicated Hubs Shortcut Row
            item {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedTextField(
                        value = toolSearch,
                        onValueChange = { toolSearch = it },
                        leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                        placeholder = { Text("Search all tools (anime, lyrics, qr, ephoto, weather...)") },
                        singleLine = true,
                        shape = RoundedCornerShape(18.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("hub_tool_search_input")
                    )

                    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        items(HUB_CATEGORIES) { cat ->
                            FilterChip(
                                selected = selectedCategory == cat,
                                onClick = { selectedCategory = cat },
                                label = { Text(cat) }
                            )
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedButton(onClick = onOpenDownloaderHub, modifier = Modifier.weight(1f)) {
                            Text("Downloader Hub", maxLines = 1, overflow = TextOverflow.Ellipsis)
                        }
                        OutlinedButton(onClick = onOpenUtilities, modifier = Modifier.weight(1f)) {
                            Text("Offline Tools", maxLines = 1, overflow = TextOverflow.Ellipsis)
                        }
                        OutlinedButton(onClick = onOpenApiStatus, modifier = Modifier.weight(1f)) {
                            Text("API Status", maxLines = 1, overflow = TextOverflow.Ellipsis)
                        }
                    }
                }
            }

            // Tool Picker Horizontal Chips
            item {
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(filteredFeatures, key = { it.key }) { feat ->
                        val selected = feat == hubState.selectedFeature
                        val isFav = feat.key in favoriteToolIds
                        Row(
                            modifier = Modifier
                                .clip(RoundedCornerShape(16.dp))
                                .background(
                                    if (selected) MaterialTheme.colorScheme.primaryContainer
                                    else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.55f)
                                )
                                .border(
                                    width = if (selected) 1.5.dp else 0.5.dp,
                                    color = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline,
                                    shape = RoundedCornerShape(16.dp)
                                )
                                .clickable {
                                    if (feat == FeatureGroup.AI_CHAT) {
                                        onOpenAiChat()
                                    } else {
                                        onSelectFeature(feat)
                                        selectedExplicitProviderId = null
                                    }
                                }
                                .padding(start = 12.dp, end = 6.dp, top = 6.dp, bottom = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = feat.label,
                                style = MaterialTheme.typography.labelLarge,
                                color = if (selected) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurface
                            )
                            IconButton(
                                onClick = { onToggleFavorite(feat.key) },
                                modifier = Modifier.size(30.dp)
                            ) {
                                Icon(
                                    imageVector = if (isFav) Icons.Default.Star else Icons.Default.StarBorder,
                                    contentDescription = "Favorite ${feat.label}",
                                    tint = if (isFav) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                    }
                }
            }

            // Active Tool Execution Card
            item {
                val feat = hubState.selectedFeature
                GlassCard(
                    shape = RoundedCornerShape(22.dp),
                    accentHighlight = true,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(feat.label, style = MaterialTheme.typography.titleLarge)
                                Text(
                                    feat.description,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            Text(
                                text = "${providersForSelected.size} APIs",
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }

                        Spacer(Modifier.height(10.dp))

                        // Provider / Style selector (includes all 37 Ephoto360 styles when LOGOS selected)
                        Box {
                            val selectedEp = providersForSelected.firstOrNull { it.id == selectedExplicitProviderId }
                            OutlinedButton(
                                onClick = { providerMenuExpanded = true },
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(
                                    text = selectedEp?.let { "Provider: ${it.name}" }
                                        ?: "Provider: Auto Failover (${providersForSelected.size} Primary → Fallback)",
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                            DropdownMenu(
                                expanded = providerMenuExpanded,
                                onDismissRequest = { providerMenuExpanded = false },
                                modifier = Modifier.heightIn(max = 340.dp)
                            ) {
                                DropdownMenuItem(
                                    text = { Text("Auto Failover (Primary → Fallback)") },
                                    onClick = {
                                        selectedExplicitProviderId = null
                                        providerMenuExpanded = false
                                    }
                                )
                                providersForSelected.forEach { ep ->
                                    DropdownMenuItem(
                                        text = {
                                            Text("${ep.name} • ${if (ep.isPrimary) "Primary" else "Fallback"}")
                                        },
                                        onClick = {
                                            selectedExplicitProviderId = ep.id
                                            providerMenuExpanded = false
                                        }
                                    )
                                }
                            }
                        }

                        if (feat == FeatureGroup.TRANSLATE) {
                            Spacer(Modifier.height(8.dp))
                            LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                items(TRANSLATE_TARGETS) { (code, name) ->
                                    FilterChip(
                                        selected = targetLang == code,
                                        onClick = { targetLang = code },
                                        label = { Text("→ $name") }
                                    )
                                }
                            }
                        }

                        Spacer(Modifier.height(10.dp))
                        OutlinedTextField(
                            value = queryInput,
                            onValueChange = { queryInput = it },
                            placeholder = { Text(feat.inputHint) },
                            singleLine = feat != FeatureGroup.TRANSLATE,
                            maxLines = 4,
                            shape = RoundedCornerShape(16.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("hub_query_input")
                        )

                        Spacer(Modifier.height(10.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Button(
                                onClick = {
                                    val extra = if (feat == FeatureGroup.TRANSLATE) {
                                        mapOf("tl" to targetLang, "to" to targetLang, "langpair" to "auto|$targetLang")
                                    } else emptyMap()
                                    onRunTool(feat, queryInput, extra, selectedExplicitProviderId, false)
                                },
                                enabled = !hubState.isLoading,
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("hub_run_tool_button")
                            ) {
                                Icon(Icons.Default.PlayArrow, contentDescription = null, modifier = Modifier.size(18.dp))
                                Spacer(Modifier.width(6.dp))
                                Text(if (hubState.isLoading) "Running..." else "Run Tool")
                            }
                            if (hubState.response != null) {
                                OutlinedButton(
                                    onClick = {
                                        val extra = if (feat == FeatureGroup.TRANSLATE) {
                                            mapOf("tl" to targetLang, "to" to targetLang, "langpair" to "auto|$targetLang")
                                        } else emptyMap()
                                        onRunTool(feat, queryInput, extra, null, true)
                                    },
                                    enabled = !hubState.isLoading,
                                    modifier = Modifier.testTag("hub_try_next_provider_button")
                                ) {
                                    Icon(Icons.Default.SkipNext, contentDescription = null, modifier = Modifier.size(18.dp))
                                    Spacer(Modifier.width(4.dp))
                                    Text("Next Provider")
                                }
                            }
                        }
                    }
                }
            }

            // Skeleton Loading Card
            if (hubState.isLoading) {
                item {
                    GlassCard(shape = RoundedCornerShape(20.dp), modifier = Modifier.fillMaxWidth()) {
                        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            Text("Contacting API providers...", style = MaterialTheme.typography.titleSmall)
                            LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(72.dp)
                                    .clip(RoundedCornerShape(14.dp))
                                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                            )
                        }
                    }
                }
            }

            // Normalized Response Card
            hubState.response?.let { res ->
                item {
                    GlassCard(shape = RoundedCornerShape(22.dp), modifier = Modifier.fillMaxWidth()) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = res.title,
                                    style = MaterialTheme.typography.titleMedium,
                                    modifier = Modifier.weight(1f),
                                    maxLines = 2,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Text(
                                    text = "${res.latencyMs}ms",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            Text(
                                text = "Source: ${res.source} ${if (res.isFallback) "(Fallback)" else "(Primary)"}",
                                style = MaterialTheme.typography.labelSmall,
                                color = if (res.ok) SecureGreen else DangerCrimson
                            )

                            if (res.imageBitmap != null) {
                                Spacer(Modifier.height(10.dp))
                                Image(
                                    bitmap = res.imageBitmap.asImageBitmap(),
                                    contentDescription = res.title,
                                    contentScale = ContentScale.Fit,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .heightIn(max = 300.dp)
                                        .clip(RoundedCornerShape(16.dp))
                                )
                            } else if (!res.imageUrl.isNullOrBlank()) {
                                Spacer(Modifier.height(10.dp))
                                AsyncImage(
                                    model = res.imageUrl,
                                    contentDescription = res.title,
                                    contentScale = ContentScale.Fit,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .heightIn(max = 300.dp)
                                        .clip(RoundedCornerShape(16.dp))
                                )
                            }

                            Spacer(Modifier.height(10.dp))
                            Text(
                                text = res.text,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurface
                            )

                            Spacer(Modifier.height(10.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                IconButton(onClick = {
                                    val cm = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                    cm.setPrimaryClip(ClipData.newPlainText(res.title, res.text))
                                    onCopiedNotice("Result copied")
                                }) {
                                    Icon(Icons.Default.ContentCopy, contentDescription = "Copy result")
                                }
                                IconButton(onClick = {
                                    ttsEngine?.speak(res.text.take(600), TextToSpeech.QUEUE_FLUSH, null, "hub_tts")
                                }) {
                                    Icon(Icons.Default.VolumeUp, contentDescription = "Speak result aloud")
                                }
                                IconButton(onClick = {
                                    val intent = Intent(Intent.ACTION_SEND).apply {
                                        type = "text/plain"
                                        putExtra(Intent.EXTRA_TEXT, res.url ?: res.text)
                                    }
                                    context.startActivity(Intent.createChooser(intent, "Share result"))
                                }) {
                                    Icon(Icons.Default.Share, contentDescription = "Share result")
                                }
                                res.url?.let { link ->
                                    IconButton(onClick = { onOpenInBrowser(link) }) {
                                        Icon(Icons.Default.OpenInBrowser, contentDescription = "Open link in browser")
                                    }
                                    IconButton(onClick = { onDownloadUrl(link) }) {
                                        Icon(Icons.Default.Download, contentDescription = "Download file")
                                    }
                                }
                                Spacer(Modifier.weight(1f))
                                if (res.raw.isNotBlank()) {
                                    TextButton(onClick = { showRawJson = !showRawJson }) {
                                        Text(if (showRawJson) "Hide Raw" else "Raw JSON")
                                    }
                                }
                            }

                            if (res.items.isNotEmpty()) {
                                HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
                                res.items.take(20).forEach { item ->
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clip(RoundedCornerShape(10.dp))
                                            .clickable {
                                                item.url?.let(onOpenInBrowser)
                                            }
                                            .padding(vertical = 6.dp, horizontal = 4.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(item.title, style = MaterialTheme.typography.bodyMedium, maxLines = 1, overflow = TextOverflow.Ellipsis)
                                            if (item.subtitle.isNotBlank()) {
                                                Text(item.subtitle, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 2, overflow = TextOverflow.Ellipsis)
                                            }
                                        }
                                        item.url?.let { u ->
                                            IconButton(onClick = { onDownloadUrl(u) }, modifier = Modifier.size(36.dp)) {
                                                Icon(Icons.Default.Download, contentDescription = "Download", modifier = Modifier.size(18.dp))
                                            }
                                        }
                                    }
                                }
                            }

                            if (showRawJson && res.raw.isNotBlank()) {
                                Spacer(Modifier.height(8.dp))
                                Text(
                                    text = res.raw.take(2000),
                                    style = MaterialTheme.typography.labelSmall.copy(fontFamily = MonoFontFamily),
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f))
                                        .padding(10.dp)
                                )
                            }
                        }
                    }
                }
            }

            // Recent Tool History with One-Tap Re-run
            if (toolHistory.isNotEmpty()) {
                item {
                    GlassCard(shape = RoundedCornerShape(20.dp), modifier = Modifier.fillMaxWidth()) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.History, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(18.dp))
                                    Spacer(Modifier.width(8.dp))
                                    Text("Recent Tool Runs", style = MaterialTheme.typography.titleSmall)
                                }
                                IconButton(onClick = onClearHistory, modifier = Modifier.size(36.dp)) {
                                    Icon(Icons.Default.DeleteSweep, contentDescription = "Clear tool history", modifier = Modifier.size(18.dp))
                                }
                            }
                            Spacer(Modifier.height(6.dp))
                            toolHistory.take(8).forEach { entry ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(10.dp))
                                        .clickable { onRerunHistory(entry) }
                                        .padding(vertical = 6.dp, horizontal = 4.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = "${entry.toolLabel}: ${entry.inputQuery}",
                                            style = MaterialTheme.typography.bodySmall,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                        Text(
                                            text = entry.providerSource,
                                            style = MaterialTheme.typography.labelSmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                    }
                                    TextButton(onClick = { onRerunHistory(entry) }) {
                                        Text("Re-run")
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
