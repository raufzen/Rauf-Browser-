package com.example.ui.screens

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
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.RestartAlt
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Button
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.api.HealthDot
import com.example.api.ProviderHealthResult
import com.example.api.ProviderRegistry
import com.example.ui.components.GlassCard
import com.example.ui.components.SubScreenTopBar
import com.example.ui.theme.DangerCrimson
import com.example.ui.theme.MonoFontFamily
import com.example.ui.theme.SecureGreen
import com.example.ui.theme.WarningAmber

private val STATUS_FILTERS = listOf("All", "Primary", "Fallback", "Downloaders", "AI", "Search", "Media & Fun")

@Composable
fun ApiStatusScreen(
    healthMap: Map<String, ProviderHealthResult>,
    disabledProviderIds: Set<String>,
    providerPriorityOrder: List<String>,
    isTestingAll: Boolean,
    onBack: () -> Unit,
    onRetestAll: () -> Unit,
    onTestSingle: (String) -> Unit,
    onToggleEnabled: (String) -> Unit,
    onMovePriority: (String, Int) -> Unit,
    onResetPriority: () -> Unit
) {
    BackHandler(onBack = onBack)
    var search by remember { mutableStateOf("") }
    var filter by remember { mutableStateOf("All") }

    val orderedAll = remember(providerPriorityOrder) {
        val customMap = providerPriorityOrder.withIndex().associate { it.value to it.index }
        ProviderRegistry.ALL_PROVIDERS.sortedWith(
            compareBy<com.example.api.ProviderEndpoint> { it.feature.ordinal }
                .thenBy { customMap[it.id] ?: Int.MAX_VALUE }
                .thenBy { if (it.isPrimary) 0 else 1 }
                .thenBy { it.priority }
        )
    }

    val filtered = remember(orderedAll, search, filter) {
        val q = search.trim().lowercase()
        orderedAll.filter { ep ->
            val catMatch = when (filter) {
                "All" -> true
                "Primary" -> ep.isPrimary
                "Fallback" -> !ep.isPrimary
                else -> ep.feature.category == filter
            }
            val queryMatch = q.isEmpty() ||
                ep.name.lowercase().contains(q) ||
                ep.host.lowercase().contains(q) ||
                ep.path.lowercase().contains(q) ||
                ep.feature.label.lowercase().contains(q)
            catMatch && queryMatch
        }
    }

    val greenCount = healthMap.values.count { it.status == HealthDot.GREEN }
    val yellowCount = healthMap.values.count { it.status == HealthDot.YELLOW }
    val redCount = healthMap.values.count { it.status == HealthDot.RED }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .navigationBarsPadding()
    ) {
        SubScreenTopBar(
            title = "API Status & Priority",
            subtitle = "${ProviderRegistry.ALL_PROVIDERS.size} registered endpoints",
            onBack = onBack,
            actions = {
                IconButton(onClick = onResetPriority, modifier = Modifier.testTag("api_status_reset_order")) {
                    Icon(Icons.Default.RestartAlt, contentDescription = "Reset provider order")
                }
            }
        )

        if (isTestingAll) {
            LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
        }

        LazyColumn(
            modifier = Modifier.weight(1f),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            item {
                GlassCard(shape = RoundedCornerShape(20.dp), accentHighlight = true, modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            StatusCountPill("Green", greenCount, SecureGreen)
                            StatusCountPill("Yellow", yellowCount, WarningAmber)
                            StatusCountPill("Red", redCount, DangerCrimson)
                            StatusCountPill("Total", ProviderRegistry.ALL_PROVIDERS.size, MaterialTheme.colorScheme.primary)
                        }
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Button(
                                onClick = onRetestAll,
                                enabled = !isTestingAll,
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("api_status_retest_all")
                            ) {
                                Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(18.dp))
                                Spacer(Modifier.width(6.dp))
                                Text(if (isTestingAll) "Testing (${healthMap.size}/${ProviderRegistry.ALL_PROVIDERS.size})..." else "Retest All")
                            }
                            OutlinedButton(onClick = onResetPriority) {
                                Text("Reset Defaults")
                            }
                        }
                    }
                }
            }

            item {
                OutlinedTextField(
                    value = search,
                    onValueChange = { search = it },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                    placeholder = { Text("Filter by provider, host, or path...") },
                    singleLine = true,
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.fillMaxWidth()
                )
            }

            item {
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(STATUS_FILTERS) { f ->
                        FilterChip(selected = filter == f, onClick = { filter = f }, label = { Text(f) })
                    }
                }
            }

            items(filtered, key = { it.id }) { ep ->
                val health = healthMap[ep.id]
                val isEnabled = ep.id !in disabledProviderIds
                val dotColor = when (health?.status) {
                    HealthDot.GREEN -> SecureGreen
                    HealthDot.YELLOW -> WarningAmber
                    HealthDot.RED -> DangerCrimson
                    else -> Color.Gray
                }

                GlassCard(shape = RoundedCornerShape(16.dp), modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(12.dp)
                                    .clip(CircleShape)
                                    .background(dotColor)
                            )
                            Spacer(Modifier.width(10.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = ep.name,
                                        style = MaterialTheme.typography.titleSmall,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis,
                                        modifier = Modifier.weight(1f, fill = false)
                                    )
                                    Spacer(Modifier.width(6.dp))
                                    Text(
                                        text = if (ep.isPrimary) "PRIMARY" else "FALLBACK",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = if (ep.isPrimary) SecureGreen else MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                                Text(
                                    text = "${ep.feature.label} • ${ep.host}${ep.path}",
                                    style = MaterialTheme.typography.labelSmall.copy(fontFamily = MonoFontFamily),
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Text(
                                    text = health?.detail ?: "Not tested yet",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = dotColor
                                )
                            }
                            Switch(
                                checked = isEnabled,
                                onCheckedChange = { onToggleEnabled(ep.id) }
                            )
                        }
                        Spacer(Modifier.height(4.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.End,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            IconButton(onClick = { onMovePriority(ep.id, -1) }, modifier = Modifier.size(36.dp)) {
                                Icon(Icons.Default.ArrowUpward, contentDescription = "Increase priority", modifier = Modifier.size(16.dp))
                            }
                            IconButton(onClick = { onMovePriority(ep.id, 1) }, modifier = Modifier.size(36.dp)) {
                                Icon(Icons.Default.ArrowDownward, contentDescription = "Decrease priority", modifier = Modifier.size(16.dp))
                            }
                            IconButton(onClick = { onTestSingle(ep.id) }, modifier = Modifier.size(36.dp)) {
                                Icon(Icons.Default.PlayArrow, contentDescription = "Test endpoint", modifier = Modifier.size(18.dp))
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun StatusCountPill(label: String, count: Int, color: Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text("$count", style = MaterialTheme.typography.titleMedium, color = color)
        Text(label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}
