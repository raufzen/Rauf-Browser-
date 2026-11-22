package com.example.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface BookmarkDao {
    @Query("SELECT * FROM bookmarks ORDER BY createdAt DESC")
    fun observeAll(): Flow<List<BookmarkEntity>>

    @Query("SELECT * FROM bookmarks ORDER BY createdAt DESC")
    suspend fun getAllOnce(): List<BookmarkEntity>

    @Query("SELECT DISTINCT folder FROM bookmarks ORDER BY folder ASC")
    fun observeFolders(): Flow<List<String>>

    @Query("SELECT * FROM bookmarks WHERE url = :url LIMIT 1")
    suspend fun findByUrl(url: String): BookmarkEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(bookmark: BookmarkEntity): Long

    @Update
    suspend fun update(bookmark: BookmarkEntity)

    @Query("DELETE FROM bookmarks WHERE id = :id")
    suspend fun deleteById(id: Long)

    @Query("DELETE FROM bookmarks WHERE url = :url")
    suspend fun deleteByUrl(url: String)
}

@Dao
interface HistoryDao {
    @Query("SELECT * FROM history ORDER BY visitedAt DESC LIMIT 200")
    fun observeRecent(): Flow<List<HistoryEntity>>

    @Query("SELECT * FROM history ORDER BY visitCount DESC, visitedAt DESC LIMIT 12")
    fun observeFrequent(): Flow<List<HistoryEntity>>

    @Query("SELECT * FROM history WHERE title LIKE '%' || :query || '%' OR url LIKE '%' || :query || '%' ORDER BY visitedAt DESC LIMIT 100")
    fun search(query: String): Flow<List<HistoryEntity>>

    @Query("SELECT * FROM history WHERE url = :url LIMIT 1")
    suspend fun findByUrl(url: String): HistoryEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(entry: HistoryEntity): Long

    @Update
    suspend fun update(entry: HistoryEntity)

    @Query("DELETE FROM history WHERE id = :id")
    suspend fun deleteById(id: Long)

    @Query("DELETE FROM history WHERE visitedAt >= :sinceTimestamp")
    suspend fun deleteSince(sinceTimestamp: Long)

    @Query("DELETE FROM history")
    suspend fun clearAll()
}

@Dao
interface DownloadDao {
    @Query("SELECT * FROM downloads ORDER BY createdAt DESC")
    fun observeAll(): Flow<List<DownloadEntity>>

    @Query("SELECT * FROM downloads WHERE status IN ('PENDING', 'RUNNING', 'PAUSED')")
    suspend fun getActiveDownloads(): List<DownloadEntity>

    @Query("SELECT * FROM downloads WHERE id = :id LIMIT 1")
    suspend fun findById(id: Long): DownloadEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(download: DownloadEntity): Long

    @Update
    suspend fun update(download: DownloadEntity)

    @Query("DELETE FROM downloads WHERE id = :id")
    suspend fun deleteById(id: Long)

    @Query("DELETE FROM downloads WHERE status IN ('COMPLETED', 'FAILED', 'CANCELLED')")
    suspend fun clearFinished()
}

@Dao
interface ShortcutDao {
    @Query("SELECT * FROM shortcuts ORDER BY sortOrder ASC, id ASC")
    fun observeAll(): Flow<List<ShortcutEntity>>

    @Query("SELECT * FROM shortcuts ORDER BY sortOrder ASC, id ASC")
    suspend fun getAllOnce(): List<ShortcutEntity>

    @Query("SELECT COUNT(*) FROM shortcuts")
    suspend fun count(): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(shortcut: ShortcutEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(shortcuts: List<ShortcutEntity>)

    @Query("DELETE FROM shortcuts WHERE id = :id")
    suspend fun deleteById(id: Long)
}

@Dao
interface ToolHistoryDao {
    @Query("SELECT * FROM tool_history ORDER BY createdAt DESC LIMIT 150")
    fun observeAll(): Flow<List<ToolHistoryEntity>>

    @Query("SELECT * FROM tool_history ORDER BY createdAt DESC LIMIT 150")
    suspend fun getAllOnce(): List<ToolHistoryEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(item: ToolHistoryEntity): Long

    @Query("DELETE FROM tool_history WHERE id = :id")
    suspend fun deleteById(id: Long)

    @Query("DELETE FROM tool_history")
    suspend fun clearAll()
}

@Dao
interface AiChatDao {
    @Query("SELECT * FROM ai_sessions ORDER BY updatedAt DESC")
    fun observeSessions(): Flow<List<AiSessionEntity>>

    @Query("SELECT * FROM ai_messages WHERE sessionId = :sessionId ORDER BY createdAt ASC, id ASC")
    fun observeMessages(sessionId: Long): Flow<List<AiMessageEntity>>

    @Query("SELECT * FROM ai_messages WHERE sessionId = :sessionId ORDER BY createdAt ASC, id ASC")
    suspend fun getMessagesOnce(sessionId: Long): List<AiMessageEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSession(session: AiSessionEntity): Long

    @Update
    suspend fun updateSession(session: AiSessionEntity)

    @Query("DELETE FROM ai_sessions WHERE id = :id")
    suspend fun deleteSession(id: Long)

    @Query("DELETE FROM ai_messages WHERE sessionId = :sessionId")
    suspend fun deleteMessagesForSession(sessionId: Long)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMessage(message: AiMessageEntity): Long

    @Update
    suspend fun updateMessage(message: AiMessageEntity)

    @Query("DELETE FROM ai_messages WHERE id = :id")
    suspend fun deleteMessage(id: Long)
}

@Dao
interface UtilityDao {
    @Query("SELECT * FROM notes ORDER BY updatedAt DESC")
    fun observeNotes(): Flow<List<NoteEntity>>

    @Query("SELECT * FROM notes ORDER BY updatedAt DESC")
    suspend fun getNotesOnce(): List<NoteEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertNote(note: NoteEntity): Long

    @Query("DELETE FROM notes WHERE id = :id")
    suspend fun deleteNote(id: Long)

    @Query("SELECT * FROM todos ORDER BY isDone ASC, createdAt DESC")
    fun observeTodos(): Flow<List<TodoEntity>>

    @Query("SELECT * FROM todos ORDER BY createdAt DESC")
    suspend fun getTodosOnce(): List<TodoEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertTodo(todo: TodoEntity): Long

    @Query("DELETE FROM todos WHERE id = :id")
    suspend fun deleteTodo(id: Long)

    @Query("SELECT * FROM reminders ORDER BY isCompleted ASC, createdAt DESC")
    fun observeReminders(): Flow<List<ReminderEntity>>

    @Query("SELECT * FROM reminders ORDER BY createdAt DESC")
    suspend fun getRemindersOnce(): List<ReminderEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertReminder(reminder: ReminderEntity): Long

    @Query("DELETE FROM reminders WHERE id = :id")
    suspend fun deleteReminder(id: Long)
}
