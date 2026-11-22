package com.example.ui.screens

import androidx.activity.compose.BackHandler
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
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Cancel
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.OpenInNew
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.core.UrlResolver
import com.example.data.local.DownloadEntity
import com.example.data.local.DownloadStatus
import com.example.ui.components.GlassCard
import com.example.ui.components.SubScreenTopBar
import com.example.ui.theme.DangerCrimson
import com.example.ui.theme.SecureGreen
import com.example.ui.theme.WarningAmber

@Composable
fun DownloadsScreen(
    downloads: List<DownloadEntity>,
    onBack: () -> Unit,
    onStartDirectDownload: (String) -> Unit,
    onOpenFile: (DownloadEntity) -> Unit,
    onCancelDownload: (DownloadEntity) -> Unit,
    onRetryDownload: (DownloadEntity) -> Unit,
    onDeleteRecord: (DownloadEntity) -> Unit,
    onClearFinished: () -> Unit
) {
    BackHandler(onBack = onBack)

    var showNewDownloadDialog by remember { mutableStateOf(false) }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            SubScreenTopBar(
                title = "Downloads",
                subtitle = "${downloads.size} files • Android DownloadManager",
                onBack = onBack,
                actions = {
                    if (downloads.isNotEmpty()) {
                        IconButton(
                            onClick = onClearFinished,
                            modifier = Modifier.testTag("clear_finished_downloads_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.DeleteSweep,
                                contentDescription = "Clear finished downloads",
                                tint = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showNewDownloadDialog = true },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
                modifier = Modifier
                    .navigationBarsPadding()
                    .testTag("new_download_fab")
            ) {
                Icon(Icons.Default.Add, contentDescription = "Download from URL")
            }
        }
    ) { innerPadding ->
        if (downloads.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .padding(32.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        imageVector = Icons.Default.Download,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(52.dp)
                    )
                    Spacer(Modifier.height(12.dp))
                    Text(
                        text = "No downloads yet",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(Modifier.height(6.dp))
                    Text(
                        text = "Files downloaded from websites or added via direct URL appear here with live progress.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 12.dp, bottom = 92.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(downloads, key = { it.id }) { item ->
                    val status = runCatching { DownloadStatus.valueOf(item.status) }
                        .getOrDefault(DownloadStatus.PENDING)
                    val isActive = status == DownloadStatus.RUNNING ||
                        status == DownloadStatus.PENDING ||
                        status == DownloadStatus.PAUSED
                    val progressFraction = if (item.totalBytes > 0L) {
                        (item.downloadedBytes.toFloat() / item.totalBytes.toFloat()).coerceIn(0f, 1f)
                    } else null

                    GlassCard(
                        shape = RoundedCornerShape(18.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable(enabled = status == DownloadStatus.COMPLETED) {
                                onOpenFile(item)
                            }
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = when (status) {
                                        DownloadStatus.COMPLETED -> Icons.Default.CheckCircle
                                        DownloadStatus.FAILED, DownloadStatus.CANCELLED -> Icons.Default.ErrorOutline
                                        else -> Icons.Default.Description
                                    },
                                    contentDescription = status.label,
                                    tint = when (status) {
                                        DownloadStatus.COMPLETED -> SecureGreen
                                        DownloadStatus.FAILED, DownloadStatus.CANCELLED -> DangerCrimson
                                        DownloadStatus.PAUSED -> WarningAmber
                                        else -> MaterialTheme.colorScheme.primary
                                    },
                                    modifier = Modifier.size(28.dp)
                                )
                                Spacer(Modifier.width(12.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = item.fileName,
                                        style = MaterialTheme.typography.titleSmall,
                                        color = MaterialTheme.colorScheme.onSurface,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                    val sizeLabel = if (item.totalBytes > 0L) {
                                        "${UrlResolver.formatBytes(item.downloadedBytes)} / ${UrlResolver.formatBytes(item.totalBytes)}"
                                    } else if (item.downloadedBytes > 0L) {
                                        UrlResolver.formatBytes(item.downloadedBytes)
                                    } else {
                                        UrlResolver.extractHost(item.url)
                                    }
                                    Text(
                                        text = "${status.label} • $sizeLabel",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }

                                if (isActive) {
                                    IconButton(onClick = { onCancelDownload(item) }) {
                                        Icon(
                                            imageVector = Icons.Default.Cancel,
                                            contentDescription = "Cancel download",
                                            tint = DangerCrimson
                                        )
                                    }
                                } else if (status == DownloadStatus.COMPLETED) {
                                    IconButton(onClick = { onOpenFile(item) }) {
                                        Icon(
                                            imageVector = Icons.Default.OpenInNew,
                                            contentDescription = "Open file",
                                            tint = MaterialTheme.colorScheme.primary
                                        )
                                    }
                                } else {
                                    IconButton(onClick = { onRetryDownload(item) }) {
                                        Icon(
                                            imageVector = Icons.Default.Refresh,
                                            contentDescription = "Retry download",
                                            tint = MaterialTheme.colorScheme.primary
                                        )
                                    }
                                }

                                IconButton(onClick = { onDeleteRecord(item) }) {
                                    Icon(
                                        imageVector = Icons.Default.Delete,
                                        contentDescription = "Remove download",
                                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }

                            if (isActive) {
                                Spacer(Modifier.height(10.dp))
                                if (progressFraction != null) {
                                    LinearProgressIndicator(
                                        progress = { progressFraction },
                                        modifier = Modifier.fillMaxWidth()
                                    )
                                } else {
                                    LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    if (showNewDownloadDialog) {
        var directUrl by remember { mutableStateOf("") }
        AlertDialog(
            onDismissRequest = { showNewDownloadDialog = false },
            title = { Text("Download File by URL") },
            text = {
                OutlinedTextField(
                    value = directUrl,
                    onValueChange = { directUrl = it },
                    label = { Text("Direct HTTPS file URL") },
                    placeholder = { Text("https://example.com/file.pdf") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        val trimmed = directUrl.trim()
                        if (trimmed.startsWith("http://") || trimmed.startsWith("https://")) {
                            onStartDirectDownload(trimmed)
                            showNewDownloadDialog = false
                        }
                    }
                ) {
                    Text("Download")
                }
            },
            dismissButton = {
                TextButton(onClick = { showNewDownloadDialog = false }) { Text("Cancel") }
            }
        )
    }
}
