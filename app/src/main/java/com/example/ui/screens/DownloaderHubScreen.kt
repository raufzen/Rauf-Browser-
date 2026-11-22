package com.example.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
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
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.ContentPaste
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Link
import androidx.compose.material.icons.filled.OpenInBrowser
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material3.Button
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.api.FeatureGroup
import com.example.api.ProviderRegistry
import com.example.ui.components.GlassCard
import com.example.ui.components.SubScreenTopBar
import com.example.ui.theme.DangerCrimson
import com.example.ui.theme.SecureGreen
import com.example.viewmodel.DownloaderFormat
import com.example.viewmodel.DownloaderUiState

private val SUPPORTED_PLATFORMS = listOf(
    "YouTube MP4/MP3", "Instagram", "TikTok", "Facebook", "Twitter / X",
    "Pinterest", "Spotify", "MediaFire", "Terabox", "CapCut", "Google Drive",
    "SoundCloud", "Threads", "Mega", "Sfile", "Videy", "APK Catalog"
)

@Composable
fun DownloaderHubScreen(
    state: DownloaderUiState,
    onBack: () -> Unit,
    onInputChanged: (String, DownloaderFormat) -> Unit,
    onRunDownload: (Boolean) -> Unit,
    onEnqueueSystemDownload: (String) -> Unit,
    onOpenInBrowser: (String) -> Unit,
    onNotify: (String) -> Unit
) {
    BackHandler(onBack = onBack)
    val context = LocalContext.current
    val providerCount = ProviderRegistry.orderedProvidersFor(state.detectedFeature).size

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .navigationBarsPadding()
    ) {
        SubScreenTopBar(
            title = "Downloader Hub",
            subtitle = "Auto-detect & multi-provider failover",
            onBack = onBack
        )

        LazyColumn(
            modifier = Modifier.weight(1f),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            item {
                GlassCard(
                    shape = RoundedCornerShape(22.dp),
                    accentHighlight = true,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("Paste Media or File Link", style = MaterialTheme.typography.titleMedium)
                        Spacer(Modifier.height(4.dp))
                        Text(
                            text = "Auto-detected: ${state.detectedFeature.label} ($providerCount provider(s) ready)",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Spacer(Modifier.height(10.dp))

                        OutlinedTextField(
                            value = state.urlInput,
                            onValueChange = { onInputChanged(it, state.format) },
                            placeholder = { Text("https://youtube.com/..., tiktok.com/..., instagram.com/...") },
                            leadingIcon = { Icon(Icons.Default.Link, contentDescription = null) },
                            trailingIcon = {
                                IconButton(
                                    onClick = {
                                        val cm = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                        val clipText = cm.primaryClip?.getItemAt(0)?.coerceToText(context)?.toString().orEmpty()
                                        if (clipText.isNotBlank()) {
                                            onInputChanged(clipText, state.format)
                                        } else {
                                            onNotify("Clipboard is empty")
                                        }
                                    },
                                    modifier = Modifier.testTag("downloader_paste_button")
                                ) {
                                    Icon(Icons.Default.ContentPaste, contentDescription = "Paste link")
                                }
                            },
                            singleLine = true,
                            shape = RoundedCornerShape(16.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("downloader_url_input")
                        )

                        Spacer(Modifier.height(10.dp))
                        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            items(DownloaderFormat.entries) { fmt ->
                                FilterChip(
                                    selected = state.format == fmt,
                                    onClick = { onInputChanged(state.urlInput, fmt) },
                                    label = { Text(fmt.label) }
                                )
                            }
                        }

                        Spacer(Modifier.height(12.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Button(
                                onClick = { onRunDownload(false) },
                                enabled = !state.isLoading,
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("downloader_fetch_button")
                            ) {
                                Icon(Icons.Default.Download, contentDescription = null, modifier = Modifier.size(18.dp))
                                Spacer(Modifier.width(6.dp))
                                Text(if (state.isLoading) "Resolving..." else "Fetch Links")
                            }
                            if (state.response != null) {
                                OutlinedButton(
                                    onClick = { onRunDownload(true) },
                                    enabled = !state.isLoading,
                                    modifier = Modifier.testTag("downloader_try_next_button")
                                ) {
                                    Icon(Icons.Default.SkipNext, contentDescription = null, modifier = Modifier.size(18.dp))
                                    Spacer(Modifier.width(4.dp))
                                    Text("Next")
                                }
                            }
                        }
                    }
                }
            }

            if (state.isLoading) {
                item {
                    GlassCard(shape = RoundedCornerShape(20.dp), modifier = Modifier.fillMaxWidth()) {
                        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            Text(
                                "Querying Primary → Fallback providers for ${state.detectedFeature.label}...",
                                style = MaterialTheme.typography.titleSmall
                            )
                            LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(84.dp)
                                    .clip(RoundedCornerShape(14.dp))
                                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.55f))
                            )
                        }
                    }
                }
            }

            state.response?.let { res ->
                item {
                    GlassCard(shape = RoundedCornerShape(22.dp), modifier = Modifier.fillMaxWidth()) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                                    Icon(
                                        imageVector = Icons.Default.CheckCircle,
                                        contentDescription = null,
                                        tint = if (res.ok) SecureGreen else DangerCrimson,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Spacer(Modifier.width(6.dp))
                                    Text(
                                        text = res.title,
                                        style = MaterialTheme.typography.titleMedium,
                                        maxLines = 2,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                                Text(
                                    text = "${res.latencyMs}ms",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }

                            Spacer(Modifier.height(4.dp))
                            Text(
                                text = "Source: ${res.source} ${if (res.isFallback) "(Fallback)" else "(Primary)"}",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.primary
                            )

                            if (!res.imageUrl.isNullOrBlank()) {
                                Spacer(Modifier.height(10.dp))
                                AsyncImage(
                                    model = res.imageUrl,
                                    contentDescription = "Media preview",
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .heightIn(max = 220.dp)
                                        .clip(RoundedCornerShape(16.dp))
                                )
                            }

                            Spacer(Modifier.height(10.dp))
                            Text(
                                text = res.text,
                                style = MaterialTheme.typography.bodySmall,
                                color = if (res.ok) MaterialTheme.colorScheme.onSurface else DangerCrimson
                            )

                            if (!res.url.isNullOrBlank()) {
                                Spacer(Modifier.height(12.dp))
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Button(
                                        onClick = { onEnqueueSystemDownload(res.url) },
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        Icon(Icons.Default.Download, contentDescription = null, modifier = Modifier.size(18.dp))
                                        Spacer(Modifier.width(6.dp))
                                        Text("Download File")
                                    }
                                    OutlinedButton(onClick = { onOpenInBrowser(res.url) }) {
                                        Icon(Icons.Default.OpenInBrowser, contentDescription = "Open in browser", modifier = Modifier.size(18.dp))
                                    }
                                    OutlinedButton(onClick = {
                                        val cm = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                        cm.setPrimaryClip(ClipData.newPlainText("Media URL", res.url))
                                        onNotify("Media link copied")
                                    }) {
                                        Icon(Icons.Default.ContentCopy, contentDescription = "Copy link", modifier = Modifier.size(18.dp))
                                    }
                                }
                            }

                            if (res.items.isNotEmpty()) {
                                Spacer(Modifier.height(12.dp))
                                HorizontalDivider()
                                Spacer(Modifier.height(8.dp))
                                Text("Available Streams & Formats", style = MaterialTheme.typography.titleSmall)
                                Spacer(Modifier.height(6.dp))
                                res.items.take(12).forEach { item ->
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(vertical = 6.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(item.title, style = MaterialTheme.typography.bodyMedium, maxLines = 1, overflow = TextOverflow.Ellipsis)
                                            if (item.subtitle.isNotBlank()) {
                                                Text(item.subtitle, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 1, overflow = TextOverflow.Ellipsis)
                                            }
                                        }
                                        item.url?.let { link ->
                                            TextButton(onClick = { onEnqueueSystemDownload(link) }) {
                                                Text("Download")
                                            }
                                        }
                                    }
                                }
                            }

                            Spacer(Modifier.height(8.dp))
                            TextButton(
                                onClick = { onRunDownload(true) },
                                modifier = Modifier.align(Alignment.End)
                            ) {
                                Icon(Icons.Default.SkipNext, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(Modifier.width(4.dp))
                                Text("Try Next Provider")
                            }
                        }
                    }
                }
            }

            // Supported Platforms Overview
            item {
                GlassCard(shape = RoundedCornerShape(20.dp), modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("Supported Platforms & Auto-Failover", style = MaterialTheme.typography.titleSmall)
                        Spacer(Modifier.height(6.dp))
                        Text(
                            text = SUPPORTED_PLATFORMS.joinToString(" • "),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(Modifier.height(8.dp))
                        Text(
                            text = "Note: Downloaders try all Primary API LIST endpoints first, then automatically fall back to TechXKamran AIO/APK endpoints.",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }
        }
    }
}
