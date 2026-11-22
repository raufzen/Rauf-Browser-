package com.example.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
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
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.AddComment
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.RadioButtonUnchecked
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material.icons.filled.StopCircle
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
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
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.api.ProviderRegistry
import com.example.data.local.AiMessageEntity
import com.example.data.local.AiSessionEntity
import com.example.engine.BrowserTab
import com.example.ui.components.AiModelLogo
import com.example.ui.components.GlassCard
import com.example.ui.theme.DangerCrimson
import com.example.ui.theme.MonoFontFamily
import com.example.viewmodel.AiPanelState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AiAssistantOverlay(
    panelState: AiPanelState,
    sessions: List<AiSessionEntity>,
    activeSessionId: Long?,
    messages: List<AiMessageEntity>,
    systemPrompt: String,
    activeTab: BrowserTab,
    onDismiss: () -> Unit,
    onSelectModel: (String) -> Unit,
    onTogglePageContext: (Boolean) -> Unit,
    onSendPrompt: (String) -> Unit,
    onStopGenerating: () -> Unit,
    onRegenerate: (Boolean) -> Unit,
    onCreateSession: () -> Unit,
    onSelectSession: (Long) -> Unit,
    onRenameSession: (Long, String) -> Unit,
    onDeleteSession: (Long) -> Unit,
    onUpdateSystemPrompt: (String) -> Unit,
    onDownloadImageUrl: (String) -> Unit,
    onNotify: (String) -> Unit
) {
    BackHandler(onBack = onDismiss)
    val context = LocalContext.current
    val listState = rememberLazyListState()

    var input by remember { mutableStateOf("") }
    var showModelSheet by remember { mutableStateOf(false) }
    var showHistorySheet by remember { mutableStateOf(false) }
    var showSystemPromptDialog by remember { mutableStateOf(false) }
    var renameTarget by remember { mutableStateOf<AiSessionEntity?>(null) }

    val selectedModel = remember(panelState.selectedModelId) {
        ProviderRegistry.AI_MODELS.firstOrNull { it.id == panelState.selectedModelId }
            ?: ProviderRegistry.AI_MODELS.first()
    }

    LaunchedEffect(messages.size, panelState.streamingDisplayText.length) {
        if (messages.isNotEmpty()) {
            listState.animateScrollToItem(messages.lastIndex)
        }
    }

    Surface(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .statusBarsPadding()
            .navigationBarsPadding()
            .imePadding(),
        color = MaterialTheme.colorScheme.background
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Duck.ai-style clean minimal top bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.background)
                    .padding(horizontal = 10.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = { showHistorySheet = true },
                    modifier = Modifier
                        .size(44.dp)
                        .testTag("ai_history_button")
                ) {
                    Icon(Icons.Default.History, contentDescription = "Chat history")
                }

                Spacer(Modifier.width(4.dp))

                // Centered Duck.ai-style Model Selector Pill with Brand Logo
                Row(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(22.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f))
                        .border(0.75.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.7f), RoundedCornerShape(22.dp))
                        .clickable { showModelSheet = true }
                        .padding(horizontal = 10.dp, vertical = 7.dp)
                        .testTag("ai_model_selector_button"),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    AiModelLogo(style = selectedModel.logoStyle, size = 28.dp)
                    Spacer(Modifier.width(10.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "${selectedModel.brandName} · ${selectedModel.name}",
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.SemiBold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = "Rauf.ai • Tap to switch model",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                    Icon(Icons.Default.ExpandMore, contentDescription = "Switch AI model", modifier = Modifier.size(18.dp))
                }

                IconButton(
                    onClick = { showSystemPromptDialog = true },
                    modifier = Modifier
                        .size(42.dp)
                        .testTag("ai_system_prompt_button")
                ) {
                    Icon(Icons.Default.Tune, contentDescription = "System prompt", modifier = Modifier.size(20.dp))
                }

                IconButton(
                    onClick = onCreateSession,
                    modifier = Modifier
                        .size(42.dp)
                        .testTag("ai_new_chat_button")
                ) {
                    Icon(Icons.Default.AddComment, contentDescription = "New chat", modifier = Modifier.size(20.dp))
                }

                IconButton(
                    onClick = onDismiss,
                    modifier = Modifier
                        .size(42.dp)
                        .testTag("ai_close_button")
                ) {
                    Icon(Icons.Default.Close, contentDescription = "Close Rauf.ai", modifier = Modifier.size(20.dp))
                }
            }

            HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.35f))

            if (!activeTab.isHome && !selectedModel.isImageModel) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "Page: ${activeTab.title}",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f)
                    )
                    Spacer(Modifier.width(8.dp))
                    FilterChip(
                        selected = panelState.includePageContext,
                        onClick = { onTogglePageContext(!panelState.includePageContext) },
                        label = { Text("Include Page", style = MaterialTheme.typography.labelSmall) }
                    )
                }
            }

            if (panelState.isStreaming) {
                LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
            }

            // Spacious Chat Feed / Duck.ai Welcome State
            LazyColumn(
                state = listState,
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                contentPadding = PaddingValues(horizontal = 18.dp, vertical = 20.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                if (messages.isEmpty()) {
                    item {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 24.dp, bottom = 12.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            AiModelLogo(style = selectedModel.logoStyle, size = 56.dp)
                            Spacer(Modifier.height(14.dp))
                            Text(
                                text = "Rauf.ai",
                                style = MaterialTheme.typography.headlineMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(Modifier.height(4.dp))
                            Text(
                                text = "${selectedModel.brandName} • ${selectedModel.name}",
                                style = MaterialTheme.typography.titleSmall,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Spacer(Modifier.height(6.dp))
                            Text(
                                text = "Private, fast AI chat built right into Rauf Browser.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                textAlign = TextAlign.Center
                            )

                            Spacer(Modifier.height(22.dp))

                            // Duck.ai-style Quick Flagship Model Selector Cards
                            Text(
                                text = "Choose Model",
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.align(Alignment.Start)
                            )
                            Spacer(Modifier.height(10.dp))
                            LazyRow(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                items(ProviderRegistry.AI_MODELS.take(6), key = { "quick_${it.id}" }) { model ->
                                    val active = model.id == selectedModel.id
                                    Column(
                                        modifier = Modifier
                                            .width(136.dp)
                                            .clip(RoundedCornerShape(18.dp))
                                            .background(
                                                if (active) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.55f)
                                                else MaterialTheme.colorScheme.surface
                                            )
                                            .border(
                                                width = if (active) 1.5.dp else 0.75.dp,
                                                color = if (active) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline.copy(alpha = 0.7f),
                                                shape = RoundedCornerShape(18.dp)
                                            )
                                            .clickable { onSelectModel(model.id) }
                                            .padding(12.dp)
                                    ) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            AiModelLogo(style = model.logoStyle, size = 30.dp)
                                            Text(
                                                text = model.tierBadge,
                                                style = MaterialTheme.typography.labelSmall,
                                                color = if (active) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }
                                        Spacer(Modifier.height(10.dp))
                                        Text(
                                            text = model.name,
                                            style = MaterialTheme.typography.titleSmall,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                        Text(
                                            text = model.brandName,
                                            style = MaterialTheme.typography.labelSmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                                            maxLines = 1
                                        )
                                    }
                                }
                            }

                            Spacer(Modifier.height(10.dp))
                            TextButton(onClick = { showModelSheet = true }) {
                                Text("View all ${ProviderRegistry.AI_MODELS.size} models")
                            }

                            Spacer(Modifier.height(16.dp))

                            // Minimal prompt suggestions
                            Column(
                                modifier = Modifier.fillMaxWidth(),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                listOf(
                                    "Explain quantum computing simply",
                                    "Write a clean Kotlin helper function",
                                    "Summarize the page I'm reading"
                                ).forEach { prompt ->
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clip(RoundedCornerShape(14.dp))
                                            .background(MaterialTheme.colorScheme.surface)
                                            .border(0.75.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.6f), RoundedCornerShape(14.dp))
                                            .clickable { onSendPrompt(prompt) }
                                            .padding(horizontal = 14.dp, vertical = 12.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = prompt,
                                            style = MaterialTheme.typography.bodyMedium,
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                items(messages, key = { it.id }) { msg ->
                    val isCurrentlyStreaming = panelState.isStreaming && panelState.streamingMessageId == msg.id
                    val displayText = if (isCurrentlyStreaming && panelState.streamingDisplayText.isNotEmpty()) {
                        panelState.streamingDisplayText
                    } else if (isCurrentlyStreaming) {
                        "Thinking..."
                    } else {
                        msg.content
                    }
                    val bitmap = panelState.liveBitmaps[msg.id]

                    AiMessageBubble(
                        message = msg,
                        displayText = displayText,
                        bitmap = bitmap,
                        isLastAssistant = !msg.isUser && msg.id == messages.lastOrNull { !it.isUser }?.id,
                        isStreaming = panelState.isStreaming,
                        onCopyText = { textToCopy ->
                            copyTextToClipboard(context, textToCopy)
                            onNotify("Copied")
                        },
                        onShareText = { textToShare ->
                            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                                type = "text/plain"
                                putExtra(Intent.EXTRA_TEXT, textToShare)
                            }
                            context.startActivity(Intent.createChooser(shareIntent, "Share"))
                        },
                        onRegenerate = { onRegenerate(false) },
                        onTryNextProvider = { onRegenerate(true) },
                        onDownloadImage = onDownloadImageUrl
                    )
                }
            }

            // Clean Minimal Composer Bar
            HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.35f))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.background)
                    .padding(horizontal = 14.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = input,
                    onValueChange = { input = it },
                    placeholder = {
                        Text(
                            if (selectedModel.isImageModel) "Describe image for ${selectedModel.name}..."
                            else "Message ${selectedModel.brandName} (${selectedModel.name})..."
                        )
                    },
                    maxLines = 4,
                    shape = RoundedCornerShape(24.dp),
                    modifier = Modifier
                        .weight(1f)
                        .testTag("ai_prompt_input")
                )
                Spacer(Modifier.width(10.dp))
                if (panelState.isStreaming) {
                    IconButton(
                        onClick = onStopGenerating,
                        modifier = Modifier
                            .size(48.dp)
                            .clip(CircleShape)
                            .background(DangerCrimson.copy(alpha = 0.18f))
                            .testTag("ai_stop_button")
                    ) {
                        Icon(Icons.Default.StopCircle, contentDescription = "Stop generating", tint = DangerCrimson)
                    }
                } else {
                    IconButton(
                        onClick = {
                            if (input.isNotBlank()) {
                                val toSend = input
                                input = ""
                                onSendPrompt(toSend)
                            }
                        },
                        modifier = Modifier
                            .size(48.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.primary)
                            .testTag("ai_send_button")
                    ) {
                        Icon(
                            Icons.AutoMirrored.Filled.Send,
                            contentDescription = "Send",
                            tint = MaterialTheme.colorScheme.onPrimary
                        )
                    }
                }
            }
        }
    }

    // DuckDuckGo-style Model Selector Sheet with Logos
    if (showModelSheet) {
        var modelQuery by remember { mutableStateOf("") }
        val filteredModels = remember(modelQuery) {
            val q = modelQuery.trim().lowercase()
            if (q.isEmpty()) ProviderRegistry.AI_MODELS
            else ProviderRegistry.AI_MODELS.filter {
                it.name.lowercase().contains(q) ||
                    it.brandName.lowercase().contains(q) ||
                    it.description.lowercase().contains(q)
            }
        }
        ModalBottomSheet(
            onDismissRequest = { showModelSheet = false },
            containerColor = MaterialTheme.colorScheme.surface
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp)
                    .navigationBarsPadding()
            ) {
                Text("Rauf.ai Models", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                Spacer(Modifier.height(4.dp))
                Text(
                    "Default is ChatGPT (GPT-6). Switch anytime — automatic failover stays active.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(Modifier.height(14.dp))
                OutlinedTextField(
                    value = modelQuery,
                    onValueChange = { modelQuery = it },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                    placeholder = { Text("Search GPT-6, Claude, Gemini, Llama...") },
                    singleLine = true,
                    shape = RoundedCornerShape(18.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("ai_model_search_input")
                )
                Spacer(Modifier.height(14.dp))
                LazyColumn(
                    modifier = Modifier.heightIn(max = 440.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    contentPadding = PaddingValues(bottom = 24.dp)
                ) {
                    items(filteredModels, key = { it.id }) { model ->
                        val isSelected = model.id == selectedModel.id
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(18.dp))
                                .background(
                                    if (isSelected) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)
                                    else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f)
                                )
                                .border(
                                    width = if (isSelected) 1.5.dp else 0.75.dp,
                                    color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline.copy(alpha = 0.55f),
                                    shape = RoundedCornerShape(18.dp)
                                )
                                .clickable {
                                    onSelectModel(model.id)
                                    showModelSheet = false
                                }
                                .padding(horizontal = 14.dp, vertical = 12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            AiModelLogo(style = model.logoStyle, size = 40.dp)
                            Spacer(Modifier.width(12.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = "${model.brandName} · ${model.name}",
                                        style = MaterialTheme.typography.titleSmall,
                                        fontWeight = FontWeight.SemiBold,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis,
                                        modifier = Modifier.weight(1f, fill = false)
                                    )
                                    Spacer(Modifier.width(8.dp))
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.14f))
                                            .padding(horizontal = 6.dp, vertical = 2.dp)
                                    ) {
                                        Text(
                                            text = model.tierBadge,
                                            style = MaterialTheme.typography.labelSmall,
                                            color = MaterialTheme.colorScheme.primary
                                        )
                                    }
                                }
                                Spacer(Modifier.height(2.dp))
                                Text(
                                    text = model.description,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                            Spacer(Modifier.width(8.dp))
                            Icon(
                                imageVector = if (isSelected) Icons.Default.CheckCircle else Icons.Default.RadioButtonUnchecked,
                                contentDescription = if (isSelected) "Selected" else "Select",
                                tint = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                                modifier = Modifier.size(22.dp)
                            )
                        }
                    }
                }
            }
        }
    }

    // Conversation History Sheet
    if (showHistorySheet) {
        ModalBottomSheet(
            onDismissRequest = { showHistorySheet = false },
            containerColor = MaterialTheme.colorScheme.surface
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp)
                    .navigationBarsPadding()
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Rauf.ai Chats", style = MaterialTheme.typography.titleLarge)
                    TextButton(onClick = {
                        onCreateSession()
                        showHistorySheet = false
                    }) { Text("+ New Chat") }
                }
                Spacer(Modifier.height(10.dp))
                LazyColumn(
                    modifier = Modifier.heightIn(max = 380.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    contentPadding = PaddingValues(bottom = 24.dp)
                ) {
                    items(sessions, key = { it.id }) { session ->
                        val active = session.id == activeSessionId
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(14.dp))
                                .background(
                                    if (active) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.55f)
                                    else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f)
                                )
                                .clickable {
                                    onSelectSession(session.id)
                                    showHistorySheet = false
                                }
                                .padding(horizontal = 14.dp, vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(session.title, style = MaterialTheme.typography.titleSmall, maxLines = 1, overflow = TextOverflow.Ellipsis)
                                val label = ProviderRegistry.AI_MODELS.firstOrNull { it.id == session.modelId }
                                    ?.let { "${it.brandName} · ${it.name}" } ?: session.modelId
                                Text(label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                            IconButton(onClick = { renameTarget = session }) {
                                Icon(Icons.Default.Edit, contentDescription = "Rename", modifier = Modifier.size(18.dp))
                            }
                            IconButton(onClick = { onDeleteSession(session.id) }) {
                                Icon(Icons.Default.Delete, contentDescription = "Delete", tint = DangerCrimson, modifier = Modifier.size(18.dp))
                            }
                        }
                    }
                }
            }
        }
    }

    renameTarget?.let { target ->
        var newTitle by remember(target.id) { mutableStateOf(target.title) }
        AlertDialog(
            onDismissRequest = { renameTarget = null },
            title = { Text("Rename Chat") },
            text = {
                OutlinedTextField(value = newTitle, onValueChange = { newTitle = it }, singleLine = true, modifier = Modifier.fillMaxWidth())
            },
            confirmButton = {
                TextButton(onClick = { onRenameSession(target.id, newTitle); renameTarget = null }) { Text("Save") }
            },
            dismissButton = {
                TextButton(onClick = { renameTarget = null }) { Text("Cancel") }
            }
        )
    }

    if (showSystemPromptDialog) {
        var draftPrompt by remember(systemPrompt) { mutableStateOf(systemPrompt) }
        AlertDialog(
            onDismissRequest = { showSystemPromptDialog = false },
            title = { Text("System Prompt") },
            text = {
                OutlinedTextField(
                    value = draftPrompt,
                    onValueChange = { draftPrompt = it },
                    label = { Text("Customize Rauf.ai behavior") },
                    minLines = 3,
                    maxLines = 6,
                    modifier = Modifier.fillMaxWidth()
                )
            },
            confirmButton = {
                TextButton(onClick = { onUpdateSystemPrompt(draftPrompt); showSystemPromptDialog = false }) { Text("Save") }
            },
            dismissButton = {
                TextButton(onClick = { showSystemPromptDialog = false }) { Text("Cancel") }
            }
        )
    }
}

@Composable
private fun AiMessageBubble(
    message: AiMessageEntity,
    displayText: String,
    bitmap: android.graphics.Bitmap?,
    isLastAssistant: Boolean,
    isStreaming: Boolean,
    onCopyText: (String) -> Unit,
    onShareText: (String) -> Unit,
    onRegenerate: () -> Unit,
    onTryNextProvider: () -> Unit,
    onDownloadImage: (String) -> Unit
) {
    val alignment = if (message.isUser) Alignment.End else Alignment.Start
    Column(modifier = Modifier.fillMaxWidth(), horizontalAlignment = alignment) {
        GlassCard(
            shape = RoundedCornerShape(18.dp),
            accentHighlight = message.isUser,
            modifier = Modifier.widthIn(max = 560.dp)
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                if (!message.isUser && message.sourceHost.isNotBlank()) {
                    Text(
                        text = "${message.modelLabel} • ${message.sourceHost}",
                        style = MaterialTheme.typography.labelSmall,
                        color = if (message.isError) DangerCrimson else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(Modifier.height(6.dp))
                }

                if (bitmap != null) {
                    Image(
                        bitmap = bitmap.asImageBitmap(),
                        contentDescription = "Generated image",
                        contentScale = ContentScale.Fit,
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(max = 320.dp)
                            .clip(RoundedCornerShape(14.dp))
                    )
                    Spacer(Modifier.height(8.dp))
                } else if (!message.imageUrl.isNullOrBlank()) {
                    AsyncImage(
                        model = message.imageUrl,
                        contentDescription = "Generated image",
                        contentScale = ContentScale.Fit,
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(max = 320.dp)
                            .clip(RoundedCornerShape(14.dp))
                    )
                    Spacer(Modifier.height(6.dp))
                    TextButton(onClick = { onDownloadImage(message.imageUrl) }) {
                        Icon(Icons.Default.Download, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(Modifier.width(4.dp))
                        Text("Download Image")
                    }
                }

                MarkdownContent(text = displayText, onCopyCode = onCopyText)

                if (!message.isUser && !isStreaming && displayText.isNotBlank()) {
                    Spacer(Modifier.height(8.dp))
                    HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.25f))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(2.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(onClick = { onCopyText(displayText) }, modifier = Modifier.size(38.dp)) {
                            Icon(Icons.Default.ContentCopy, contentDescription = "Copy", modifier = Modifier.size(16.dp))
                        }
                        IconButton(onClick = { onShareText(displayText) }, modifier = Modifier.size(38.dp)) {
                            Icon(Icons.Default.Share, contentDescription = "Share", modifier = Modifier.size(16.dp))
                        }
                        if (isLastAssistant) {
                            IconButton(onClick = onRegenerate, modifier = Modifier.size(38.dp)) {
                                Icon(Icons.Default.Refresh, contentDescription = "Regenerate", modifier = Modifier.size(16.dp))
                            }
                            TextButton(
                                onClick = onTryNextProvider,
                                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                                modifier = Modifier.testTag("ai_try_next_provider")
                            ) {
                                Icon(Icons.Default.SkipNext, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(Modifier.width(4.dp))
                                Text("Next Provider", style = MaterialTheme.typography.labelSmall)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun MarkdownContent(text: String, onCopyCode: (String) -> Unit) {
    val segments = remember(text) { parseMarkdownSegments(text) }
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        segments.forEach { seg ->
            if (seg.isCode) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.75f))
                        .border(0.5.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.6f), RoundedCornerShape(12.dp))
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.7f))
                            .padding(horizontal = 10.dp, vertical = 4.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = seg.language.ifBlank { "code" },
                            style = MaterialTheme.typography.labelSmall,
                            fontFamily = MonoFontFamily,
                            color = MaterialTheme.colorScheme.primary
                        )
                        TextButton(
                            onClick = { onCopyCode(seg.content) },
                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 0.dp)
                        ) {
                            Icon(Icons.Default.ContentCopy, contentDescription = null, modifier = Modifier.size(14.dp))
                            Spacer(Modifier.width(4.dp))
                            Text("Copy", style = MaterialTheme.typography.labelSmall)
                        }
                    }
                    Text(
                        text = seg.content,
                        style = MaterialTheme.typography.bodySmall.copy(fontFamily = MonoFontFamily),
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.padding(10.dp)
                    )
                }
            } else if (seg.content.isNotBlank()) {
                Text(
                    text = renderInlineMarkdown(seg.content),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
        }
    }
}

private data class MdSegment(val isCode: Boolean, val language: String = "", val content: String)

private fun parseMarkdownSegments(raw: String): List<MdSegment> {
    val result = mutableListOf<MdSegment>()
    val regex = Regex("```([a-zA-Z0-9_+-]*)\\n?([\\s\\S]*?)```")
    var lastEnd = 0
    for (m in regex.findAll(raw)) {
        if (m.range.first > lastEnd) {
            result.add(MdSegment(false, content = raw.substring(lastEnd, m.range.first).trim()))
        }
        result.add(MdSegment(true, language = m.groupValues[1].trim(), content = m.groupValues[2].trimEnd()))
        lastEnd = m.range.last + 1
    }
    if (lastEnd < raw.length) {
        result.add(MdSegment(false, content = raw.substring(lastEnd).trim()))
    }
    return if (result.isEmpty()) listOf(MdSegment(false, content = raw)) else result
}

private fun renderInlineMarkdown(text: String) = buildAnnotatedString {
    val tokenRegex = Regex("(\\*\\*[^*]+\\*\\*|`[^`]+`)")
    var cursor = 0
    for (match in tokenRegex.findAll(text)) {
        if (match.range.first > cursor) append(text.substring(cursor, match.range.first))
        val token = match.value
        when {
            token.startsWith("**") -> withStyle(SpanStyle(fontWeight = FontWeight.Bold)) {
                append(token.removeSurrounding("**"))
            }
            token.startsWith("`") -> withStyle(SpanStyle(fontFamily = MonoFontFamily, fontWeight = FontWeight.Medium)) {
                append(token.removeSurrounding("`"))
            }
        }
        cursor = match.range.last + 1
    }
    if (cursor < text.length) append(text.substring(cursor))
}

private fun copyTextToClipboard(context: Context, text: String) {
    val cm = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
    cm.setPrimaryClip(ClipData.newPlainText("Rauf.ai", text))
}
