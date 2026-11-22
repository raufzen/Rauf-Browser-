package com.example.data.local

import android.content.Context
import android.webkit.CookieManager
import android.webkit.WebStorage
import android.webkit.WebView
import com.example.core.SearchEngine
import com.example.ui.theme.ThemeMode
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject

enum class HistoryTimeRange(val label: String, val durationMillis: Long?) {
    LAST_HOUR("Last hour", 60L * 60L * 1000L),
    LAST_24_HOURS("Last 24 hours", 24L * 60L * 60L * 1000L),
    LAST_7_DAYS("Last 7 days", 7L * 24L * 60L * 60L * 1000L),
    LAST_30_DAYS("Last 30 days", 30L * 24L * 60L * 60L * 1000L),
    ALL_TIME("All time", null)
}

class BrowserRepository(
    private val context: Context,
    private val database: BrowserDatabase,
    private val preferences: BrowserPreferences
) {
    private val bookmarkDao = database.bookmarkDao()
    private val historyDao = database.historyDao()
    private val downloadDao = database.downloadDao()
    private val shortcutDao = database.shortcutDao()
    private val toolHistoryDao = database.toolHistoryDao()
    private val aiChatDao = database.aiChatDao()
    private val utilityDao = database.utilityDao()

    val settingsFlow: Flow<BrowserSettings> = preferences.settingsFlow
    val bookmarksFlow: Flow<List<BookmarkEntity>> = bookmarkDao.observeAll()
    val bookmarkFoldersFlow: Flow<List<String>> = bookmarkDao.observeFolders()
    val recentHistoryFlow: Flow<List<HistoryEntity>> = historyDao.observeRecent()
    val frequentSitesFlow: Flow<List<HistoryEntity>> = historyDao.observeFrequent()
    val downloadsFlow: Flow<List<DownloadEntity>> = downloadDao.observeAll()
    val shortcutsFlow: Flow<List<ShortcutEntity>> = shortcutDao.observeAll()
    val toolHistoryFlow: Flow<List<ToolHistoryEntity>> = toolHistoryDao.observeAll()
    val aiSessionsFlow: Flow<List<AiSessionEntity>> = aiChatDao.observeSessions()
    val notesFlow: Flow<List<NoteEntity>> = utilityDao.observeNotes()
    val todosFlow: Flow<List<TodoEntity>> = utilityDao.observeTodos()
    val remindersFlow: Flow<List<ReminderEntity>> = utilityDao.observeReminders()

    fun observeAiMessages(sessionId: Long): Flow<List<AiMessageEntity>> =
        aiChatDao.observeMessages(sessionId)

    suspend fun ensureDefaultShortcuts() {
        if (shortcutDao.count() == 0) {
            shortcutDao.insertAll(
                listOf(
                    ShortcutEntity(title = "Google", url = "https://www.google.com", accentHex = 0xFF4285F4L, sortOrder = 0),
                    ShortcutEntity(title = "Wikipedia", url = "https://www.wikipedia.org", accentHex = 0xFF9D4EDDL, sortOrder = 1),
                    ShortcutEntity(title = "GitHub", url = "https://github.com", accentHex = 0xFF7B2CBFL, sortOrder = 2),
                    ShortcutEntity(title = "YouTube", url = "https://m.youtube.com", accentHex = 0xFFE11D48L, sortOrder = 3),
                    ShortcutEntity(title = "DuckDuckGo", url = "https://duckduckgo.com", accentHex = 0xFFEA580CL, sortOrder = 4),
                    ShortcutEntity(title = "News", url = "https://news.google.com", accentHex = 0xFF0EA5E9L, sortOrder = 5),
                    ShortcutEntity(title = "Reddit", url = "https://www.reddit.com", accentHex = 0xFFF97316L, sortOrder = 6),
                    ShortcutEntity(title = "MDN Docs", url = "https://developer.mozilla.org", accentHex = 0xFF10B981L, sortOrder = 7)
                )
            )
        }
    }

    suspend fun updateSettings(transform: (BrowserSettings) -> BrowserSettings) =
        preferences.updateSettings(transform)

    // Bookmarks
    suspend fun addOrUpdateBookmark(title: String, url: String, folder: String = "Favorites") {
        val existing = bookmarkDao.findByUrl(url)
        if (existing != null) {
            bookmarkDao.update(existing.copy(title = title.ifBlank { url }, folder = folder.ifBlank { "Favorites" }))
        } else {
            bookmarkDao.insert(BookmarkEntity(title = title.ifBlank { url }, url = url, folder = folder.ifBlank { "Favorites" }))
        }
    }

    suspend fun editBookmark(bookmark: BookmarkEntity) = bookmarkDao.update(bookmark)
    suspend fun removeBookmark(id: Long) = bookmarkDao.deleteById(id)
    suspend fun removeBookmarkByUrl(url: String) = bookmarkDao.deleteByUrl(url)

    // Shortcuts
    suspend fun addShortcut(title: String, url: String) {
        val count = shortcutDao.count()
        shortcutDao.insert(ShortcutEntity(title = title.trim(), url = url.trim(), sortOrder = count))
    }

    suspend fun deleteShortcut(id: Long) = shortcutDao.deleteById(id)

    // History
    suspend fun recordHistoryVisit(title: String, url: String) {
        if (url.isBlank() || url.startsWith("rauf://") || url == "about:blank") return
        val existing = historyDao.findByUrl(url)
        val now = System.currentTimeMillis()
        if (existing != null) {
            historyDao.update(existing.copy(title = title.ifBlank { existing.title }, visitedAt = now, visitCount = existing.visitCount + 1))
        } else {
            historyDao.insert(HistoryEntity(title = title.ifBlank { url }, url = url, visitedAt = now, visitCount = 1))
        }
    }

    suspend fun deleteHistoryItem(id: Long) = historyDao.deleteById(id)

    suspend fun clearHistoryByRange(range: HistoryTimeRange) {
        val duration = range.durationMillis
        if (duration == null) historyDao.clearAll()
        else historyDao.deleteSince(System.currentTimeMillis() - duration)
    }

    // Tool Usage History
    suspend fun recordToolRun(entry: ToolHistoryEntity) = toolHistoryDao.insert(entry)
    suspend fun deleteToolHistory(id: Long) = toolHistoryDao.deleteById(id)
    suspend fun clearToolHistory() = toolHistoryDao.clearAll()

    // AI Chat Persistence
    suspend fun createAiSession(title: String, modelId: String, systemPrompt: String): Long =
        aiChatDao.insertSession(AiSessionEntity(title = title, modelId = modelId, systemPrompt = systemPrompt))

    suspend fun updateAiSession(session: AiSessionEntity) = aiChatDao.updateSession(session)

    suspend fun deleteAiSession(sessionId: Long) {
        aiChatDao.deleteMessagesForSession(sessionId)
        aiChatDao.deleteSession(sessionId)
    }

    suspend fun insertAiMessage(msg: AiMessageEntity): Long = aiChatDao.insertMessage(msg)
    suspend fun updateAiMessage(msg: AiMessageEntity) = aiChatDao.updateMessage(msg)
    suspend fun deleteAiMessage(id: Long) = aiChatDao.deleteMessage(id)
    suspend fun getAiMessagesOnce(sessionId: Long): List<AiMessageEntity> = aiChatDao.getMessagesOnce(sessionId)

    // Offline Utilities (Notes, Todos, Reminders)
    suspend fun saveNote(note: NoteEntity) = utilityDao.upsertNote(note.copy(updatedAt = System.currentTimeMillis()))
    suspend fun deleteNote(id: Long) = utilityDao.deleteNote(id)
    suspend fun saveTodo(todo: TodoEntity) = utilityDao.upsertTodo(todo)
    suspend fun deleteTodo(id: Long) = utilityDao.deleteTodo(id)
    suspend fun saveReminder(reminder: ReminderEntity) = utilityDao.upsertReminder(reminder)
    suspend fun deleteReminder(id: Long) = utilityDao.deleteReminder(id)

    // Downloads
    suspend fun insertDownload(download: DownloadEntity): Long = downloadDao.insert(download)
    suspend fun updateDownload(download: DownloadEntity) = downloadDao.update(download)
    suspend fun getActiveDownloads(): List<DownloadEntity> = downloadDao.getActiveDownloads()
    suspend fun deleteDownloadRecord(id: Long) = downloadDao.deleteById(id)
    suspend fun clearFinishedDownloads() = downloadDao.clearFinished()

    // Full JSON Export / Import
    suspend fun exportBackupJson(): String = withContext(Dispatchers.IO) {
        val settings = preferences.getSettingsSnapshot()
        val root = JSONObject()
        root.put("app", "Rauf Browser")
        root.put("exportedAt", System.currentTimeMillis())

        val settingsObj = JSONObject().apply {
            put("themeMode", settings.themeMode.name)
            put("accentColorHex", settings.accentColorHex)
            put("fontScale", settings.fontScale.toDouble())
            put("reducedMotion", settings.reducedMotion)
            put("searchEngine", settings.searchEngine.name)
            put("wallpaper", settings.wallpaper.name)
            put("customHomepageUrl", settings.customHomepageUrl)
            put("lastAiModelId", settings.lastAiModelId)
            put("aiSystemPrompt", settings.aiSystemPrompt)
            put("apiKeys", JSONObject(settings.apiKeys))
            put("disabledProviderIds", JSONArray(settings.disabledProviderIds.toList()))
            put("providerPriorityOrder", JSONArray(settings.providerPriorityOrder))
            put("favoriteToolIds", JSONArray(settings.favoriteToolIds.toList()))
        }
        root.put("settings", settingsObj)

        root.put("bookmarks", JSONArray().apply {
            bookmarkDao.getAllOnce().forEach { bm ->
                put(JSONObject().put("title", bm.title).put("url", bm.url).put("folder", bm.folder))
            }
        })
        root.put("notes", JSONArray().apply {
            utilityDao.getNotesOnce().forEach { n ->
                put(JSONObject().put("title", n.title).put("content", n.content))
            }
        })
        root.put("todos", JSONArray().apply {
            utilityDao.getTodosOnce().forEach { t ->
                put(JSONObject().put("text", t.text).put("isDone", t.isDone))
            }
        })
        root.put("reminders", JSONArray().apply {
            utilityDao.getRemindersOnce().forEach { r ->
                put(JSONObject().put("title", r.title).put("dueLabel", r.dueLabel).put("isCompleted", r.isCompleted))
            }
        })
        root.toString(2)
    }

    suspend fun importBackupJson(rawJson: String): Result<String> = withContext(Dispatchers.IO) {
        runCatching {
            val root = JSONObject(rawJson)
            root.optJSONObject("settings")?.let { s ->
                preferences.updateSettings { current ->
                    val keysMap = linkedMapOf<String, String>()
                    s.optJSONObject("apiKeys")?.let { ak ->
                        val it = ak.keys()
                        while (it.hasNext()) {
                            val k = it.next()
                            keysMap[k] = ak.optString(k)
                        }
                    }
                    val disabled = s.optJSONArray("disabledProviderIds")?.let { arr ->
                        (0 until arr.length()).map { arr.optString(it) }.toSet()
                    } ?: current.disabledProviderIds
                    val order = s.optJSONArray("providerPriorityOrder")?.let { arr ->
                        (0 until arr.length()).map { arr.optString(it) }
                    } ?: current.providerPriorityOrder
                    val favs = s.optJSONArray("favoriteToolIds")?.let { arr ->
                        (0 until arr.length()).map { arr.optString(it) }.toSet()
                    } ?: current.favoriteToolIds

                    current.copy(
                        themeMode = runCatching { ThemeMode.valueOf(s.optString("themeMode")) }.getOrDefault(current.themeMode),
                        accentColorHex = s.optLong("accentColorHex", current.accentColorHex),
                        fontScale = s.optDouble("fontScale", current.fontScale.toDouble()).toFloat(),
                        reducedMotion = s.optBoolean("reducedMotion", current.reducedMotion),
                        searchEngine = runCatching { SearchEngine.valueOf(s.optString("searchEngine")) }.getOrDefault(current.searchEngine),
                        wallpaper = runCatching { HomepageWallpaper.valueOf(s.optString("wallpaper")) }.getOrDefault(current.wallpaper),
                        customHomepageUrl = s.optString("customHomepageUrl", current.customHomepageUrl),
                        lastAiModelId = s.optString("lastAiModelId", current.lastAiModelId),
                        aiSystemPrompt = s.optString("aiSystemPrompt", current.aiSystemPrompt),
                        apiKeys = if (keysMap.isNotEmpty()) keysMap else current.apiKeys,
                        disabledProviderIds = disabled,
                        providerPriorityOrder = order,
                        favoriteToolIds = favs
                    )
                }
            }
            var importedCount = 0
            root.optJSONArray("bookmarks")?.let { arr ->
                for (i in 0 until arr.length()) {
                    val o = arr.optJSONObject(i) ?: continue
                    addOrUpdateBookmark(o.optString("title"), o.optString("url"), o.optString("folder", "Favorites"))
                    importedCount++
                }
            }
            root.optJSONArray("notes")?.let { arr ->
                for (i in 0 until arr.length()) {
                    val o = arr.optJSONObject(i) ?: continue
                    utilityDao.upsertNote(NoteEntity(title = o.optString("title"), content = o.optString("content")))
                    importedCount++
                }
            }
            root.optJSONArray("todos")?.let { arr ->
                for (i in 0 until arr.length()) {
                    val o = arr.optJSONObject(i) ?: continue
                    utilityDao.upsertTodo(TodoEntity(text = o.optString("text"), isDone = o.optBoolean("isDone")))
                    importedCount++
                }
            }
            root.optJSONArray("reminders")?.let { arr ->
                for (i in 0 until arr.length()) {
                    val o = arr.optJSONObject(i) ?: continue
                    utilityDao.upsertReminder(ReminderEntity(title = o.optString("title"), dueLabel = o.optString("dueLabel"), isCompleted = o.optBoolean("isCompleted")))
                    importedCount++
                }
            }
            "Imported settings and $importedCount records"
        }
    }

    // Privacy & Browsing Data Clearing
    suspend fun clearCookies() = withContext(Dispatchers.Main) {
        val cookieManager = CookieManager.getInstance()
        cookieManager.removeAllCookies(null)
        cookieManager.flush()
    }

    suspend fun clearCacheAndSiteStorage(activeWebViews: Collection<WebView>) = withContext(Dispatchers.Main) {
        activeWebViews.forEach { it.clearCache(true) }
        WebStorage.getInstance().deleteAllData()
        runCatching { context.cacheDir.deleteRecursively() }
    }

    suspend fun clearAllBrowsingData(activeWebViews: Collection<WebView>) {
        historyDao.clearAll()
        clearCookies()
        clearCacheAndSiteStorage(activeWebViews)
    }

    suspend fun clearIncognitoSessionData(incognitoWebViews: Collection<WebView>) = withContext(Dispatchers.Main) {
        incognitoWebViews.forEach { webView ->
            webView.clearHistory()
            webView.clearFormData()
            webView.clearCache(true)
        }
        val cookieManager = CookieManager.getInstance()
        cookieManager.removeSessionCookies(null)
        cookieManager.flush()
    }
}
