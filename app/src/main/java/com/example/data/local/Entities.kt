package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "bookmarks")
data class BookmarkEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0L,
    val title: String,
    val url: String,
    val folder: String = "Favorites",
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "history")
data class HistoryEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0L,
    val title: String,
    val url: String,
    val visitedAt: Long = System.currentTimeMillis(),
    val visitCount: Int = 1
)

@Entity(tableName = "downloads")
data class DownloadEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0L,
    val systemDownloadId: Long = -1L,
    val fileName: String,
    val url: String,
    val mimeType: String = "*/*",
    val totalBytes: Long = -1L,
    val downloadedBytes: Long = 0L,
    val status: String = DownloadStatus.PENDING.name,
    val localUri: String? = null,
    val createdAt: Long = System.currentTimeMillis()
)

enum class DownloadStatus(val label: String) {
    PENDING("Queued"),
    RUNNING("Downloading"),
    PAUSED("Paused"),
    COMPLETED("Completed"),
    FAILED("Failed"),
    CANCELLED("Cancelled")
}

@Entity(tableName = "shortcuts")
data class ShortcutEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0L,
    val title: String,
    val url: String,
    val accentHex: Long = 0xFF9D4EDDL,
    val sortOrder: Int = 0
)

@Entity(tableName = "tool_history")
data class ToolHistoryEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0L,
    val featureKey: String,
    val toolLabel: String,
    val inputQuery: String,
    val secondaryInput: String = "",
    val providerSource: String,
    val resultPreview: String,
    val resultUrl: String? = null,
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "ai_sessions")
data class AiSessionEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0L,
    val title: String,
    val modelId: String,
    val systemPrompt: String = "",
    val updatedAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "ai_messages")
data class AiMessageEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0L,
    val sessionId: Long,
    val isUser: Boolean,
    val content: String,
    val modelLabel: String,
    val sourceHost: String = "",
    val imageUrl: String? = null,
    val isError: Boolean = false,
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "notes")
data class NoteEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0L,
    val title: String,
    val content: String,
    val updatedAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "todos")
data class TodoEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0L,
    val text: String,
    val isDone: Boolean = false,
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "reminders")
data class ReminderEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0L,
    val title: String,
    val dueLabel: String,
    val isCompleted: Boolean = false,
    val createdAt: Long = System.currentTimeMillis()
)
