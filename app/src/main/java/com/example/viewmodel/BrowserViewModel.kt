package com.example.viewmodel

import android.app.Application
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.view.View
import android.webkit.ValueCallback
import android.webkit.WebChromeClient
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.ai.AiCommandAliases
import com.example.ai.BrowserAiService
import com.example.api.FeatureGroup
import com.example.api.NormalizedApiEngine
import com.example.api.NormalizedResponse
import com.example.api.ProviderHealthResult
import com.example.api.ProviderRegistry
import com.example.core.NetworkMonitor
import com.example.core.SearchEngine
import com.example.core.UrlResolver
import com.example.data.local.AiMessageEntity
import com.example.data.local.AiSessionEntity
import com.example.data.local.BookmarkEntity
import com.example.data.local.BrowserDatabase
import com.example.data.local.BrowserPreferences
import com.example.data.local.BrowserRepository
import com.example.data.local.BrowserSettings
import com.example.data.local.DownloadEntity
import com.example.data.local.HistoryEntity
import com.example.data.local.HistoryTimeRange
import com.example.data.local.HomepageWallpaper
import com.example.data.local.NoteEntity
import com.example.data.local.ReminderEntity
import com.example.data.local.ShortcutEntity
import com.example.data.local.TodoEntity
import com.example.data.local.ToolHistoryEntity
import com.example.engine.BrowserTab
import com.example.engine.ClosedTabRecord
import com.example.engine.ExternalLinkPrompt
import com.example.engine.FindInPageState
import com.example.engine.NativeDownloadController
import com.example.engine.SitePermissionPrompt
import com.example.engine.SslAlertState
import com.example.engine.WebViewCallbacks
import com.example.engine.WebViewEngineManager
import com.example.ui.theme.ThemeMode
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.Job
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Semaphore
import kotlinx.coroutines.sync.withPermit

enum class BrowserScreen {
    BROWSER,
    TABS,
    BOOKMARKS,
    HISTORY,
    DOWNLOADS,
    API_HUB,
    DOWNLOADER_HUB,
    UTILITIES,
    API_STATUS,
    SETTINGS,
    PRIVACY_POLICY,
    ABOUT
}

enum class DownloaderFormat(val label: String) {
    AUTO("Auto Best"),
    MP4("Video (MP4)"),
    MP3("Audio (MP3)")
}

data class AiPanelState(
    val isVisible: Boolean = false,
    val selectedModelId: String = "chatgpt_gpt6",
    val includePageContext: Boolean = false,
    val isStreaming: Boolean = false,
    val streamingMessageId: Long? = null,
    val streamingDisplayText: String = "",
    val lastUserPrompt: String = "",
    val providerOffset: Int = 0,
    val liveBitmaps: Map<Long, Bitmap> = emptyMap()
)

data class DownloaderUiState(
    val urlInput: String = "",
    val format: DownloaderFormat = DownloaderFormat.AUTO,
    val detectedFeature: FeatureGroup = FeatureGroup.AIO,
    val isLoading: Boolean = false,
    val providerOffset: Int = 0,
    val response: NormalizedResponse? = null
)

data class ToolHubUiState(
    val selectedFeature: FeatureGroup = FeatureGroup.WEATHER,
    val isLoading: Boolean = false,
    val lastQuery: String = "",
    val lastExtraParams: Map<String, String> = emptyMap(),
    val lastExplicitProviderId: String? = null,
    val providerOffset: Int = 0,
    val response: NormalizedResponse? = null
)

data class FullScreenVideoState(
    val customView: View,
    val callback: WebChromeClient.CustomViewCallback?
)

@OptIn(ExperimentalCoroutinesApi::class)
class BrowserViewModel(application: Application) : AndroidViewModel(application) {
    private val database = BrowserDatabase.getInstance(application)
    private val preferences = BrowserPreferences(application)
    private val repository = BrowserRepository(application, database, preferences)
    private val downloadController = NativeDownloadController(application, repository)
    val apiEngine = NormalizedApiEngine()
    private val aiService = BrowserAiService(apiEngine)
    private val networkMonitor = NetworkMonitor(application)

    val isOnline: StateFlow<Boolean> = networkMonitor.isOnline

    val settings: StateFlow<BrowserSettings> = repository.settingsFlow.stateIn(
        viewModelScope,
        SharingStarted.Eagerly,
        BrowserSettings()
    )

    val bookmarks: StateFlow<List<BookmarkEntity>> = repository.bookmarksFlow.stateIn(
        viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList()
    )
    val bookmarkFolders: StateFlow<List<String>> = repository.bookmarkFoldersFlow.stateIn(
        viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList()
    )
    val recentHistory: StateFlow<List<HistoryEntity>> = repository.recentHistoryFlow.stateIn(
        viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList()
    )
    val frequentSites: StateFlow<List<HistoryEntity>> = repository.frequentSitesFlow.stateIn(
        viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList()
    )
    val downloads: StateFlow<List<DownloadEntity>> = repository.downloadsFlow.stateIn(
        viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList()
    )
    val shortcuts: StateFlow<List<ShortcutEntity>> = repository.shortcutsFlow.stateIn(
        viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList()
    )
    val toolHistory: StateFlow<List<ToolHistoryEntity>> = repository.toolHistoryFlow.stateIn(
        viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList()
    )
    val aiSessions: StateFlow<List<AiSessionEntity>> = repository.aiSessionsFlow.stateIn(
        viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList()
    )
    val notes: StateFlow<List<NoteEntity>> = repository.notesFlow.stateIn(
        viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList()
    )
    val todos: StateFlow<List<TodoEntity>> = repository.todosFlow.stateIn(
        viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList()
    )
    val reminders: StateFlow<List<ReminderEntity>> = repository.remindersFlow.stateIn(
        viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList()
    )

    private val _activeAiSessionId = MutableStateFlow<Long?>(null)
    val activeAiSessionId: StateFlow<Long?> = _activeAiSessionId.asStateFlow()

    val activeAiMessages: StateFlow<List<AiMessageEntity>> = _activeAiSessionId
        .flatMapLatest { sid -> if (sid == null) flowOf(emptyList()) else repository.observeAiMessages(sid) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    private val initialTab = BrowserTab()
    private val _tabs = MutableStateFlow(listOf(initialTab))
    val tabs: StateFlow<List<BrowserTab>> = _tabs.asStateFlow()

    private val _activeTabId = MutableStateFlow(initialTab.id)
    val activeTabId: StateFlow<String> = _activeTabId.asStateFlow()

    private val _closedTabs = MutableStateFlow<List<ClosedTabRecord>>(emptyList())
    val closedTabs: StateFlow<List<ClosedTabRecord>> = _closedTabs.asStateFlow()

    private val _currentScreen = MutableStateFlow(BrowserScreen.BROWSER)
    val currentScreen: StateFlow<BrowserScreen> = _currentScreen.asStateFlow()

    private val _isFullScreenMode = MutableStateFlow(false)
    val isFullScreenMode: StateFlow<Boolean> = _isFullScreenMode.asStateFlow()

    private val _isCommandPaletteVisible = MutableStateFlow(false)
    val isCommandPaletteVisible: StateFlow<Boolean> = _isCommandPaletteVisible.asStateFlow()

    private val _sslAlert = MutableStateFlow<SslAlertState?>(null)
    val sslAlert: StateFlow<SslAlertState?> = _sslAlert.asStateFlow()

    private val _externalLinkPrompt = MutableStateFlow<ExternalLinkPrompt?>(null)
    val externalLinkPrompt: StateFlow<ExternalLinkPrompt?> = _externalLinkPrompt.asStateFlow()

    private val _sitePermissionPrompt = MutableStateFlow<SitePermissionPrompt?>(null)
    val sitePermissionPrompt: StateFlow<SitePermissionPrompt?> = _sitePermissionPrompt.asStateFlow()

    private val _fullScreenVideo = MutableStateFlow<FullScreenVideoState?>(null)
    val fullScreenVideo: StateFlow<FullScreenVideoState?> = _fullScreenVideo.asStateFlow()

    private val _findInPage = MutableStateFlow(FindInPageState())
    val findInPage: StateFlow<FindInPageState> = _findInPage.asStateFlow()

    private val _aiPanel = MutableStateFlow(AiPanelState())
    val aiPanel: StateFlow<AiPanelState> = _aiPanel.asStateFlow()

    private val _downloaderState = MutableStateFlow(DownloaderUiState())
    val downloaderState: StateFlow<DownloaderUiState> = _downloaderState.asStateFlow()

    private val _toolHubState = MutableStateFlow(ToolHubUiState())
    val toolHubState: StateFlow<ToolHubUiState> = _toolHubState.asStateFlow()

    private val _providerHealthMap = MutableStateFlow<Map<String, ProviderHealthResult>>(emptyMap())
    val providerHealthMap: StateFlow<Map<String, ProviderHealthResult>> = _providerHealthMap.asStateFlow()

    private val _isTestingAllProviders = MutableStateFlow(false)
    val isTestingAllProviders: StateFlow<Boolean> = _isTestingAllProviders.asStateFlow()

    private val _snackbarMessages = MutableSharedFlow<String>(extraBufferCapacity = 10)
    val snackbarMessages: SharedFlow<String> = _snackbarMessages.asSharedFlow()

    var fileChooserHandler: ((ValueCallback<Array<Uri>>, WebChromeClient.FileChooserParams) -> Boolean)? = null
    private var aiStreamJob: Job? = null
    private var hubRequestJob: Job? = null
    private var downloaderJob: Job? = null
    private var healthCheckJob: Job? = null

    val webViewManager: WebViewEngineManager = WebViewEngineManager(
        context = application,
        callbacks = object : WebViewCallbacks {
            override fun onTabUpdated(tabId: String, transform: (BrowserTab) -> BrowserTab) {
                _tabs.update { list -> list.map { if (it.id == tabId) transform(it) else it } }
            }

            override fun onRecordHistory(tabId: String, isIncognito: Boolean, title: String, url: String) {
                if (!isIncognito) {
                    viewModelScope.launch { repository.recordHistoryVisit(title, url) }
                }
            }

            override fun onSslError(alert: SslAlertState) {
                _sslAlert.value = alert
            }

            override fun onExternalLink(prompt: ExternalLinkPrompt) {
                _externalLinkPrompt.value = prompt
            }

            override fun onDownloadRequested(
                url: String,
                userAgent: String,
                contentDisposition: String,
                mimeType: String
            ) {
                startDownload(url, userAgent, contentDisposition, mimeType)
            }

            override fun onFileChooserRequested(
                callback: ValueCallback<Array<Uri>>,
                params: WebChromeClient.FileChooserParams
            ): Boolean {
                return fileChooserHandler?.invoke(callback, params) ?: false
            }

            override fun onSitePermissionPrompt(prompt: SitePermissionPrompt) {
                _sitePermissionPrompt.value = prompt
            }

            override fun onFullScreenVideoChanged(
                customView: View?,
                callback: WebChromeClient.CustomViewCallback?
            ) {
                _fullScreenVideo.value = customView?.let { FullScreenVideoState(it, callback) }
            }

            override fun onFindMatchesUpdated(activeOrdinal: Int, totalMatches: Int) {
                _findInPage.update {
                    it.copy(
                        activeMatchIndex = if (totalMatches > 0) activeOrdinal + 1 else 0,
                        totalMatches = totalMatches
                    )
                }
            }

            override fun getCurrentSettings(): BrowserSettings = settings.value
        }
    )

    init {
        viewModelScope.launch {
            repository.ensureDefaultShortcuts()
        }
        viewModelScope.launch {
            settings.collect { currentSettings ->
                webViewManager.applySettingsToAll(currentSettings, _tabs.value)
                _aiPanel.update { panel ->
                    if (panel.selectedModelId != currentSettings.lastAiModelId && !panel.isStreaming) {
                        panel.copy(selectedModelId = currentSettings.lastAiModelId)
                    } else panel
                }
            }
        }
        viewModelScope.launch {
            while (isActive) {
                downloadController.syncActiveDownloads()
                delay(1_500L)
            }
        }
    }

    fun getActiveTab(): BrowserTab {
        val currentList = _tabs.value
        return currentList.firstOrNull { it.id == _activeTabId.value } ?: currentList.first()
    }

    fun navigateScreen(screen: BrowserScreen) {
        if (screen == BrowserScreen.TABS) {
            webViewManager.capturePreview(_activeTabId.value)
        }
        _currentScreen.value = screen
    }

    fun navigateBackToBrowser() {
        _currentScreen.value = when (_currentScreen.value) {
            BrowserScreen.PRIVACY_POLICY, BrowserScreen.ABOUT -> BrowserScreen.SETTINGS
            else -> BrowserScreen.BROWSER
        }
    }

    fun openCommandPalette() { _isCommandPaletteVisible.value = true }
    fun closeCommandPalette() { _isCommandPaletteVisible.value = false }

    // Navigation & Omnibox (with unified /ai, /ask, /chat, /gpt, /gemini alias interception)
    fun submitOmniboxInput(rawInput: String, targetTabId: String = _activeTabId.value) {
        val aiAliasPrompt = AiCommandAliases.extractPromptOrNull(rawInput)
        if (aiAliasPrompt != null) {
            openAiPanel(autoPrompt = aiAliasPrompt.ifBlank { null })
            return
        }
        val currentSettings = settings.value
        val resolvedUrl = UrlResolver.resolveInput(
            rawInput = rawInput,
            searchEngine = currentSettings.searchEngine,
            httpsOnly = currentSettings.httpsOnlyMode
        )
        loadUrlInTab(resolvedUrl, targetTabId)
    }

    fun loadUrlInTab(url: String, targetTabId: String = _activeTabId.value) {
        _currentScreen.value = BrowserScreen.BROWSER
        if (UrlResolver.isHomeUrl(url)) {
            goHome(targetTabId)
            return
        }
        _tabs.update { list ->
            list.map {
                if (it.id == targetTabId) {
                    it.copy(
                        url = url,
                        title = UrlResolver.extractHost(url),
                        isLoading = true,
                        progress = 8,
                        isSecure = UrlResolver.isSecureUrl(url),
                        errorMessage = null,
                        lastAccessedAt = System.currentTimeMillis()
                    )
                } else it
            }
        }
        val tab = _tabs.value.firstOrNull { it.id == targetTabId } ?: return
        webViewManager.loadUrl(tab, url)
    }

    fun goHome(targetTabId: String = _activeTabId.value) {
        val customHome = settings.value.customHomepageUrl.trim()
        if (customHome.isNotEmpty()) {
            submitOmniboxInput(customHome, targetTabId)
            return
        }
        webViewManager.getWebView(targetTabId)?.stopLoading()
        _tabs.update { list ->
            list.map {
                if (it.id == targetTabId) {
                    it.copy(
                        url = UrlResolver.HOME_URL,
                        title = if (it.isIncognito) "Incognito Tab" else "New Tab",
                        isLoading = false,
                        progress = 0,
                        isSecure = true,
                        errorMessage = null
                    )
                } else it
            }
        }
        _currentScreen.value = BrowserScreen.BROWSER
    }

    fun goBack(): Boolean {
        val tab = getActiveTab()
        if (tab.isHome) return false
        val wv = webViewManager.getWebView(tab.id)
        return if (wv != null && wv.canGoBack()) {
            wv.goBack()
            true
        } else {
            goHome(tab.id)
            true
        }
    }

    fun goForward() {
        val tab = getActiveTab()
        val wv = webViewManager.getWebView(tab.id)
        if (wv != null && wv.canGoForward()) wv.goForward()
    }

    fun reloadOrStop() {
        val tab = getActiveTab()
        if (tab.isHome) return
        val wv = webViewManager.getWebView(tab.id) ?: return
        if (tab.isLoading) {
            wv.stopLoading()
            _tabs.update { list ->
                list.map { if (it.id == tab.id) it.copy(isLoading = false, progress = 0) else it }
            }
        } else {
            wv.reload()
        }
    }

    fun toggleDesktopMode() {
        val tab = getActiveTab()
        val nextDesktop = !tab.isDesktopMode
        _tabs.update { list ->
            list.map { if (it.id == tab.id) it.copy(isDesktopMode = nextDesktop) else it }
        }
        webViewManager.switchDesktopMode(tab, nextDesktop, settings.value)
        emitSnackbar(if (nextDesktop) "Desktop site fitted to screen" else "Mobile site enabled")
    }

    fun toggleFullScreenMode() { _isFullScreenMode.update { !it } }
    fun exitFullScreenMode() { _isFullScreenMode.value = false }

    // Multi-Tab Management
    fun openNewTab(isIncognito: Boolean = false, initialUrl: String? = null) {
        webViewManager.capturePreview(_activeTabId.value)
        val newTab = BrowserTab(
            isIncognito = isIncognito,
            title = if (isIncognito) "Incognito Tab" else "New Tab"
        )
        _tabs.update { it + newTab }
        _activeTabId.value = newTab.id
        webViewManager.pauseInactiveTabs(newTab.id)
        _currentScreen.value = BrowserScreen.BROWSER
        if (!initialUrl.isNullOrBlank()) loadUrlInTab(initialUrl, newTab.id)
    }

    fun switchToTab(tabId: String) {
        if (_tabs.value.none { it.id == tabId }) return
        webViewManager.capturePreview(_activeTabId.value)
        _activeTabId.value = tabId
        webViewManager.pauseInactiveTabs(tabId)
        _currentScreen.value = BrowserScreen.BROWSER
    }

    fun closeTab(tabId: String) {
        val current = _tabs.value
        val target = current.firstOrNull { it.id == tabId } ?: return
        if (!target.isIncognito && !target.isHome) {
            _closedTabs.update { stack ->
                (listOf(ClosedTabRecord(target.url, target.title, false, target.isDesktopMode)) + stack).take(15)
            }
        }
        webViewManager.destroyTab(tabId, target.isIncognito)

        val remaining = current.filterNot { it.id == tabId }
        val wasLastIncognito = target.isIncognito && remaining.none { it.isIncognito }
        if (wasLastIncognito) {
            viewModelScope.launch {
                repository.clearIncognitoSessionData(emptyList())
                emitSnackbar("Private session data cleared")
            }
        }
        if (remaining.isEmpty()) {
            val fresh = BrowserTab()
            _tabs.value = listOf(fresh)
            _activeTabId.value = fresh.id
        } else {
            _tabs.value = remaining
            if (_activeTabId.value == tabId) {
                val nextActive = remaining.lastOrNull { it.isIncognito == target.isIncognito } ?: remaining.last()
                _activeTabId.value = nextActive.id
                webViewManager.pauseInactiveTabs(nextActive.id)
            }
        }
    }

    fun closeAllTabs(incognitoOnly: Boolean) {
        val targets = _tabs.value.filter { it.isIncognito == incognitoOnly }
        targets.forEach { tab ->
            if (!tab.isIncognito && !tab.isHome) {
                _closedTabs.update { stack ->
                    (listOf(ClosedTabRecord(tab.url, tab.title, false, tab.isDesktopMode)) + stack).take(15)
                }
            }
            webViewManager.destroyTab(tab.id, tab.isIncognito)
        }
        if (incognitoOnly) {
            viewModelScope.launch {
                repository.clearIncognitoSessionData(emptyList())
                emitSnackbar("All private tabs & session data cleared")
            }
        }
        val remaining = _tabs.value.filterNot { it.isIncognito == incognitoOnly }
        if (remaining.isEmpty()) {
            val fresh = BrowserTab()
            _tabs.value = listOf(fresh)
            _activeTabId.value = fresh.id
        } else {
            _tabs.value = remaining
            if (remaining.none { it.id == _activeTabId.value }) _activeTabId.value = remaining.last().id
        }
    }

    fun restoreLastClosedTab() {
        val stack = _closedTabs.value
        val record = stack.firstOrNull() ?: return
        _closedTabs.value = stack.drop(1)
        openNewTab(isIncognito = record.isIncognito, initialUrl = record.url)
        emitSnackbar("Restored ${record.title}")
    }

    // Bookmarks & Shortcuts
    fun toggleBookmarkForActiveTab() {
        val tab = getActiveTab()
        if (tab.isHome) {
            emitSnackbar("Open a webpage first to bookmark it")
            return
        }
        val exists = bookmarks.value.any { it.url == tab.url }
        viewModelScope.launch {
            if (exists) {
                repository.removeBookmarkByUrl(tab.url)
                emitSnackbar("Bookmark removed")
            } else {
                repository.addOrUpdateBookmark(tab.title, tab.url, "Favorites")
                emitSnackbar("Saved to Bookmarks")
            }
        }
    }

    fun saveBookmark(title: String, url: String, folder: String) {
        val normalizedUrl = UrlResolver.resolveInput(url, settings.value.searchEngine, settings.value.httpsOnlyMode)
        viewModelScope.launch {
            repository.addOrUpdateBookmark(title, normalizedUrl, folder)
            emitSnackbar("Bookmark saved")
        }
    }

    fun editBookmark(bookmark: BookmarkEntity) {
        viewModelScope.launch {
            repository.editBookmark(bookmark)
            emitSnackbar("Bookmark updated")
        }
    }

    fun removeBookmark(id: Long) {
        viewModelScope.launch {
            repository.removeBookmark(id)
            emitSnackbar("Bookmark deleted")
        }
    }

    fun addShortcut(title: String, url: String) {
        val resolved = UrlResolver.resolveInput(url, settings.value.searchEngine, settings.value.httpsOnlyMode)
        viewModelScope.launch {
            repository.addShortcut(title.ifBlank { UrlResolver.extractHost(resolved) }, resolved)
            emitSnackbar("Shortcut added to homepage")
        }
    }

    fun deleteShortcut(id: Long) { viewModelScope.launch { repository.deleteShortcut(id) } }

    // History
    fun deleteHistoryItem(id: Long) { viewModelScope.launch { repository.deleteHistoryItem(id) } }
    fun clearHistoryByRange(range: HistoryTimeRange) {
        viewModelScope.launch {
            repository.clearHistoryByRange(range)
            emitSnackbar("Cleared history (${range.label})")
        }
    }

    // Downloads
    fun startDownload(
        url: String,
        userAgent: String? = null,
        contentDisposition: String? = null,
        mimeType: String? = null
    ) {
        viewModelScope.launch {
            val result = downloadController.enqueueDownload(
                url = url,
                userAgent = userAgent,
                contentDisposition = contentDisposition,
                mimeType = mimeType,
                showNotification = settings.value.notificationsEnabled
            )
            result.onSuccess { entity ->
                emitSnackbar("Downloading ${entity.fileName}")
            }.onFailure { err ->
                emitSnackbar("Download failed: ${err.localizedMessage ?: "Invalid URL"}")
            }
        }
    }

    fun cancelDownload(item: DownloadEntity) {
        viewModelScope.launch {
            downloadController.cancelDownload(item)
            emitSnackbar("Cancelled ${item.fileName}")
        }
    }

    fun retryDownload(item: DownloadEntity) { startDownload(item.url, mimeType = item.mimeType) }
    fun deleteDownloadRecord(item: DownloadEntity) { viewModelScope.launch { downloadController.deleteDownload(item) } }
    fun clearFinishedDownloads() {
        viewModelScope.launch {
            repository.clearFinishedDownloads()
            emitSnackbar("Cleared completed downloads")
        }
    }

    fun openDownloadedFile(item: DownloadEntity) {
        downloadController.openDownloadedFile(item).onFailure { err ->
            emitSnackbar(err.localizedMessage ?: "No compatible app found to open this file")
        }
    }

    // Find in Page
    fun openFindInPage() { _findInPage.value = FindInPageState(isVisible = true) }
    fun updateFindQuery(query: String) {
        _findInPage.update { it.copy(query = query) }
        val wv = webViewManager.getWebView(_activeTabId.value) ?: return
        if (query.isBlank()) {
            wv.clearMatches()
            _findInPage.update { it.copy(activeMatchIndex = 0, totalMatches = 0) }
        } else {
            wv.findAllAsync(query)
        }
    }
    fun findNext(forward: Boolean) { webViewManager.getWebView(_activeTabId.value)?.findNext(forward) }
    fun closeFindInPage() {
        webViewManager.getWebView(_activeTabId.value)?.clearMatches()
        _findInPage.value = FindInPageState(isVisible = false)
    }

    // Security & Dialogs
    fun dismissSslAlertAndGoSafe() {
        val alert = _sslAlert.value
        _sslAlert.value = null
        if (alert != null) {
            val wv = webViewManager.getWebView(alert.tabId)
            if (wv != null && wv.canGoBack()) wv.goBack() else goHome(alert.tabId)
        }
    }
    fun dismissExternalLinkPrompt() { _externalLinkPrompt.value = null }
    fun resolveSitePermission(grant: Boolean) {
        val prompt = _sitePermissionPrompt.value ?: return
        _sitePermissionPrompt.value = null
        when (prompt) {
            is SitePermissionPrompt.Geolocation -> prompt.callback.invoke(prompt.origin, grant, false)
            is SitePermissionPrompt.Media -> {
                if (grant) prompt.request.grant(prompt.resources.toTypedArray())
                else prompt.request.deny()
            }
        }
    }
    fun exitFullScreenVideo() {
        _fullScreenVideo.value?.callback?.onCustomViewHidden()
        _fullScreenVideo.value = null
    }

    // Privacy Data Clearing
    fun clearCache() {
        viewModelScope.launch {
            repository.clearCacheAndSiteStorage(webViewManager.getAllWebViews())
            emitSnackbar("Browser cache and site storage cleared")
        }
    }
    fun clearCookies() {
        viewModelScope.launch {
            repository.clearCookies()
            emitSnackbar("All cookies cleared")
        }
    }
    fun clearAllBrowsingData() {
        viewModelScope.launch {
            repository.clearAllBrowsingData(webViewManager.getAllWebViews())
            emitSnackbar("History, cookies, and cache cleared")
        }
    }

    // Settings updaters
    fun updateThemeMode(mode: ThemeMode) = updateSetting { it.copy(themeMode = mode) }
    fun updateSearchEngine(engine: SearchEngine) = updateSetting { it.copy(searchEngine = engine) }
    fun updateWallpaper(wallpaper: HomepageWallpaper) = updateSetting { it.copy(wallpaper = wallpaper) }
    fun updateSetting(transform: (BrowserSettings) -> BrowserSettings) {
        viewModelScope.launch { repository.updateSettings(transform) }
    }

    // =========================================================================
    // UNIFIED AI SECTION (Room History + Model Selector + Failover + Streaming)
    // =========================================================================
    fun openAiPanel(modelId: String? = null, autoPrompt: String? = null) {
        val activeTab = getActiveTab()
        val chosenModel = modelId ?: settings.value.lastAiModelId
        val modelSpec = aiService.resolveModel(chosenModel)
        _aiPanel.update {
            it.copy(
                isVisible = true,
                selectedModelId = modelSpec.id,
                includePageContext = !activeTab.isHome && !modelSpec.isImageModel
            )
        }
        viewModelScope.launch {
            if (_activeAiSessionId.value == null) {
                val existing = aiSessions.value.firstOrNull()
                if (existing != null) {
                    _activeAiSessionId.value = existing.id
                } else {
                    val newId = repository.createAiSession(
                        title = "New Chat",
                        modelId = modelSpec.id,
                        systemPrompt = settings.value.aiSystemPrompt
                    )
                    _activeAiSessionId.value = newId
                }
            }
            if (!autoPrompt.isNullOrBlank()) {
                sendAiPrompt(autoPrompt, explicitModelId = modelSpec.id)
            }
        }
    }

    fun closeAiPanel() { _aiPanel.update { it.copy(isVisible = false) } }

    fun selectAiModel(modelId: String) {
        _aiPanel.update { it.copy(selectedModelId = modelId, providerOffset = 0) }
        updateSetting { it.copy(lastAiModelId = modelId) }
    }

    fun toggleAiPageContext(enabled: Boolean) {
        _aiPanel.update { it.copy(includePageContext = enabled) }
    }

    fun updateAiSystemPrompt(prompt: String) {
        val cleaned = prompt.trim()
        updateSetting { it.copy(aiSystemPrompt = cleaned) }
        val sid = _activeAiSessionId.value
        val currentSession = aiSessions.value.firstOrNull { it.id == sid }
        if (currentSession != null) {
            viewModelScope.launch {
                repository.updateAiSession(currentSession.copy(systemPrompt = cleaned))
            }
        }
        emitSnackbar("System prompt updated")
    }

    fun createNewAiSession() {
        stopAiGenerating()
        viewModelScope.launch {
            val sid = repository.createAiSession(
                title = "New Chat",
                modelId = _aiPanel.value.selectedModelId,
                systemPrompt = settings.value.aiSystemPrompt
            )
            _activeAiSessionId.value = sid
            _aiPanel.update { it.copy(providerOffset = 0) }
        }
    }

    fun selectAiSession(sessionId: Long) {
        stopAiGenerating()
        _activeAiSessionId.value = sessionId
        val session = aiSessions.value.firstOrNull { it.id == sessionId }
        if (session != null) {
            _aiPanel.update { it.copy(selectedModelId = session.modelId) }
        }
    }

    fun renameAiSession(sessionId: Long, newTitle: String) {
        val session = aiSessions.value.firstOrNull { it.id == sessionId } ?: return
        if (newTitle.isBlank()) return
        viewModelScope.launch {
            repository.updateAiSession(session.copy(title = newTitle.trim(), updatedAt = System.currentTimeMillis()))
        }
    }

    fun deleteAiSession(sessionId: Long) {
        viewModelScope.launch {
            repository.deleteAiSession(sessionId)
            if (_activeAiSessionId.value == sessionId) {
                val next = aiSessions.value.firstOrNull { it.id != sessionId }
                _activeAiSessionId.value = next?.id
            }
        }
    }

    fun stopAiGenerating() {
        val streamingId = _aiPanel.value.streamingMessageId
        val partial = _aiPanel.value.streamingDisplayText
        aiStreamJob?.cancel()
        aiStreamJob = null
        if (streamingId != null && partial.isNotBlank()) {
            viewModelScope.launch {
                val msg = activeAiMessages.value.firstOrNull { it.id == streamingId }
                if (msg != null) {
                    repository.updateAiMessage(msg.copy(content = partial))
                }
            }
        }
        _aiPanel.update { it.copy(isStreaming = false, streamingMessageId = null, streamingDisplayText = "") }
    }

    fun regenerateLastAiMessage(tryNextProvider: Boolean = false) {
        val lastPrompt = _aiPanel.value.lastUserPrompt.ifBlank {
            activeAiMessages.value.lastOrNull { it.isUser }?.content.orEmpty()
        }
        if (lastPrompt.isBlank()) return
        val nextOffset = if (tryNextProvider) _aiPanel.value.providerOffset + 1 else _aiPanel.value.providerOffset
        _aiPanel.update { it.copy(providerOffset = nextOffset) }
        sendAiPrompt(lastPrompt, skipUserInsert = true, providerOffsetOverride = nextOffset)
    }

    fun sendAiPrompt(
        prompt: String,
        explicitModelId: String? = null,
        skipUserInsert: Boolean = false,
        providerOffsetOverride: Int = 0
    ) {
        val cleanPrompt = prompt.trim()
        if (cleanPrompt.isEmpty()) return

        val modelId = explicitModelId ?: _aiPanel.value.selectedModelId
        val modelSpec = aiService.resolveModel(modelId)
        val includeContext = _aiPanel.value.includePageContext && !modelSpec.isImageModel
        val activeTab = getActiveTab()

        stopAiGenerating()
        _aiPanel.update {
            it.copy(
                selectedModelId = modelSpec.id,
                isStreaming = true,
                lastUserPrompt = cleanPrompt,
                providerOffset = providerOffsetOverride
            )
        }

        val launchCall: (String) -> Unit = { snippet ->
            aiStreamJob = viewModelScope.launch {
                var sid = _activeAiSessionId.value
                if (sid == null) {
                    sid = repository.createAiSession(
                        title = cleanPrompt.take(36),
                        modelId = modelSpec.id,
                        systemPrompt = settings.value.aiSystemPrompt
                    )
                    _activeAiSessionId.value = sid
                } else {
                    val existing = aiSessions.value.firstOrNull { it.id == sid }
                    if (existing != null && existing.title == "New Chat") {
                        repository.updateAiSession(existing.copy(title = cleanPrompt.take(36), updatedAt = System.currentTimeMillis()))
                    }
                }

                if (!skipUserInsert) {
                    repository.insertAiMessage(
                        AiMessageEntity(
                            sessionId = sid,
                            isUser = true,
                            content = cleanPrompt,
                            modelLabel = modelSpec.name
                        )
                    )
                }

                val placeholderId = repository.insertAiMessage(
                    AiMessageEntity(
                        sessionId = sid,
                        isUser = false,
                        content = "",
                        modelLabel = modelSpec.name
                    )
                )
                _aiPanel.update { it.copy(streamingMessageId = placeholderId, streamingDisplayText = "") }

                val currentSettings = settings.value
                val outcome = aiService.ask(
                    selectedModelId = modelSpec.id,
                    userPrompt = cleanPrompt,
                    systemPrompt = currentSettings.aiSystemPrompt,
                    includePageContext = includeContext,
                    pageTitle = activeTab.title,
                    pageUrl = activeTab.url,
                    pageSnippet = snippet,
                    apiKeys = currentSettings.apiKeys,
                    disabledProviderIds = currentSettings.disabledProviderIds,
                    providerPriorityOrder = currentSettings.providerPriorityOrder,
                    providerOffset = providerOffsetOverride
                )

                outcome.imageBitmap?.let { bmp ->
                    _aiPanel.update { it.copy(liveBitmaps = it.liveBitmaps + (placeholderId to bmp)) }
                }

                // Streaming-style typing effect (skipped cleanly if Reduced Motion is enabled)
                val fullText = outcome.text
                if (!currentSettings.reducedMotion && fullText.length > 24 && !outcome.isError) {
                    val step = (fullText.length / 26).coerceIn(6, 48)
                    var cursor = 0
                    while (cursor < fullText.length) {
                        cursor = (cursor + step).coerceAtMost(fullText.length)
                        _aiPanel.update { it.copy(streamingDisplayText = fullText.substring(0, cursor)) }
                        delay(18L)
                    }
                }

                repository.updateAiMessage(
                    AiMessageEntity(
                        id = placeholderId,
                        sessionId = sid,
                        isUser = false,
                        content = fullText,
                        modelLabel = modelSpec.name,
                        sourceHost = outcome.sourceLabel,
                        imageUrl = outcome.imageUrl,
                        isError = outcome.isError
                    )
                )

                repository.recordToolRun(
                    ToolHistoryEntity(
                        featureKey = if (modelSpec.isImageModel) FeatureGroup.AI_IMAGE.key else FeatureGroup.AI_CHAT.key,
                        toolLabel = "AI (${modelSpec.name})",
                        inputQuery = cleanPrompt.take(180),
                        providerSource = outcome.sourceLabel,
                        resultPreview = fullText.take(160)
                    )
                )

                _aiPanel.update {
                    it.copy(
                        isStreaming = false,
                        streamingMessageId = null,
                        streamingDisplayText = ""
                    )
                }
            }
        }

        if (includeContext && !activeTab.isHome) {
            webViewManager.extractVisiblePageText(activeTab.id, launchCall)
        } else {
            launchCall("")
        }
    }

    // =========================================================================
    // DOWNLOADER HUB (Auto-Detect Platform + Primary->Fallback Failover)
    // =========================================================================
    fun updateDownloaderInput(url: String, format: DownloaderFormat = _downloaderState.value.format) {
        val detected = ProviderRegistry.detectDownloaderFeature(url, audioOnly = format == DownloaderFormat.MP3)
        _downloaderState.update {
            it.copy(urlInput = url, format = format, detectedFeature = detected, providerOffset = 0)
        }
    }

    fun runDownloader(
        rawUrl: String = _downloaderState.value.urlInput,
        format: DownloaderFormat = _downloaderState.value.format,
        tryNextProvider: Boolean = false
    ) {
        val cleanUrl = rawUrl.trim()
        if (cleanUrl.isBlank()) {
            emitSnackbar("Paste a video, audio, or file link first")
            return
        }
        val detected = ProviderRegistry.detectDownloaderFeature(cleanUrl, audioOnly = format == DownloaderFormat.MP3)
        val nextOffset = if (tryNextProvider) _downloaderState.value.providerOffset + 1 else 0

        downloaderJob?.cancel()
        downloaderJob = viewModelScope.launch {
            _downloaderState.update {
                it.copy(
                    urlInput = cleanUrl,
                    format = format,
                    detectedFeature = detected,
                    isLoading = true,
                    providerOffset = nextOffset
                )
            }
            val s = settings.value
            val response = apiEngine.executeFeature(
                feature = detected,
                query = cleanUrl,
                extraParams = if (format == DownloaderFormat.MP3) mapOf("format" to "mp3") else emptyMap(),
                apiKeys = s.apiKeys,
                disabledProviderIds = s.disabledProviderIds,
                providerPriorityOrder = s.providerPriorityOrder,
                startProviderIndex = nextOffset,
                bypassCache = tryNextProvider
            )
            _downloaderState.update { it.copy(isLoading = false, response = response) }
            repository.recordToolRun(
                ToolHistoryEntity(
                    featureKey = detected.key,
                    toolLabel = detected.label,
                    inputQuery = cleanUrl,
                    providerSource = response.source,
                    resultPreview = response.url ?: response.text.take(140),
                    resultUrl = response.url
                )
            )
        }
    }

    // =========================================================================
    // MULTI-TOOL HUB (All 104 Hosts / 258 Endpoints + Favorites + History Re-run)
    // =========================================================================
    fun selectToolFeature(feature: FeatureGroup) {
        _toolHubState.update { it.copy(selectedFeature = feature, providerOffset = 0, lastExplicitProviderId = null) }
    }

    fun openToolInHub(feature: FeatureGroup, presetQuery: String? = null) {
        if (feature == FeatureGroup.AI_CHAT) {
            openAiPanel(autoPrompt = presetQuery)
            return
        }
        if (feature.category == "Downloaders" && feature != FeatureGroup.APK) {
            _currentScreen.value = BrowserScreen.DOWNLOADER_HUB
            if (!presetQuery.isNullOrBlank()) {
                updateDownloaderInput(presetQuery)
                runDownloader(presetQuery)
            }
            return
        }
        _toolHubState.update {
            it.copy(
                selectedFeature = feature,
                lastQuery = presetQuery ?: it.lastQuery,
                providerOffset = 0,
                lastExplicitProviderId = null
            )
        }
        _currentScreen.value = BrowserScreen.API_HUB
        if (!presetQuery.isNullOrBlank()) {
            runToolFeature(feature, presetQuery)
        }
    }

    fun runToolFeature(
        feature: FeatureGroup,
        query: String,
        extraParams: Map<String, String> = emptyMap(),
        explicitProviderId: String? = null,
        tryNextProvider: Boolean = false
    ) {
        val nextOffset = if (tryNextProvider) _toolHubState.value.providerOffset + 1 else 0
        hubRequestJob?.cancel()
        hubRequestJob = viewModelScope.launch {
            _toolHubState.update {
                it.copy(
                    selectedFeature = feature,
                    isLoading = true,
                    lastQuery = query,
                    lastExtraParams = extraParams,
                    lastExplicitProviderId = if (tryNextProvider) null else explicitProviderId,
                    providerOffset = nextOffset
                )
            }
            val s = settings.value
            val response = apiEngine.executeFeature(
                feature = feature,
                query = query,
                extraParams = extraParams,
                apiKeys = s.apiKeys,
                disabledProviderIds = s.disabledProviderIds,
                providerPriorityOrder = s.providerPriorityOrder,
                startProviderIndex = nextOffset,
                explicitProviderId = if (tryNextProvider) null else explicitProviderId,
                bypassCache = tryNextProvider
            )
            _toolHubState.update { it.copy(isLoading = false, response = response) }
            repository.recordToolRun(
                ToolHistoryEntity(
                    featureKey = feature.key,
                    toolLabel = feature.label,
                    inputQuery = query.ifBlank { "(default)" },
                    providerSource = response.source,
                    resultPreview = response.text.take(140),
                    resultUrl = response.url ?: response.imageUrl
                )
            )
        }
    }

    fun toggleFavoriteTool(featureKey: String) {
        updateSetting { current ->
            val updated = if (featureKey in current.favoriteToolIds) {
                current.favoriteToolIds - featureKey
            } else {
                current.favoriteToolIds + featureKey
            }
            current.copy(favoriteToolIds = updated)
        }
    }

    fun rerunToolHistory(entry: ToolHistoryEntity) {
        val feature = FeatureGroup.entries.firstOrNull { it.key == entry.featureKey } ?: FeatureGroup.TOOLS
        openToolInHub(feature, presetQuery = entry.inputQuery.takeIf { it != "(default)" })
    }

    fun deleteToolHistory(id: Long) { viewModelScope.launch { repository.deleteToolHistory(id) } }
    fun clearToolHistory() {
        viewModelScope.launch {
            repository.clearToolHistory()
            emitSnackbar("Tool history cleared")
        }
    }

    // =========================================================================
    // API STATUS & PROVIDER PRIORITY MANAGER
    // =========================================================================
    fun testSingleProvider(providerId: String) {
        val provider = ProviderRegistry.byId(providerId) ?: return
        viewModelScope.launch {
            val res = apiEngine.testProviderHealth(provider, settings.value.apiKeys)
            _providerHealthMap.update { it + (provider.id to res) }
        }
    }

    fun retestAllProviders() {
        if (_isTestingAllProviders.value) return
        healthCheckJob?.cancel()
        healthCheckJob = viewModelScope.launch {
            _isTestingAllProviders.value = true
            val keys = settings.value.apiKeys
            val semaphore = Semaphore(10)
            try {
                ProviderRegistry.ALL_PROVIDERS.map { provider ->
                    async {
                        semaphore.withPermit {
                            val res = apiEngine.testProviderHealth(provider, keys)
                            _providerHealthMap.update { it + (provider.id to res) }
                        }
                    }
                }.awaitAll()
                emitSnackbar("API health check finished (${ProviderRegistry.ALL_PROVIDERS.size} endpoints)")
            } finally {
                _isTestingAllProviders.value = false
            }
        }
    }

    fun toggleProviderEnabled(providerId: String) {
        updateSetting { current ->
            val next = if (providerId in current.disabledProviderIds) {
                current.disabledProviderIds - providerId
            } else {
                current.disabledProviderIds + providerId
            }
            current.copy(disabledProviderIds = next)
        }
    }

    fun moveProviderPriority(providerId: String, delta: Int) {
        val provider = ProviderRegistry.byId(providerId) ?: return
        updateSetting { current ->
            val siblings = ProviderRegistry.orderedProvidersFor(
                feature = provider.feature,
                disabledIds = emptySet(),
                customOrder = current.providerPriorityOrder
            ).map { it.id }.toMutableList()
            val idx = siblings.indexOf(providerId)
            val target = (idx + delta).coerceIn(0, siblings.lastIndex)
            if (idx >= 0 && idx != target) {
                siblings.removeAt(idx)
                siblings.add(target, providerId)
                val others = current.providerPriorityOrder.filterNot { it in siblings }
                current.copy(providerPriorityOrder = siblings + others)
            } else current
        }
    }

    fun resetProviderPriority() {
        updateSetting { it.copy(providerPriorityOrder = emptyList(), disabledProviderIds = emptySet()) }
        emitSnackbar("Reset provider priority to Primary → Fallback defaults")
    }

    fun setApiKey(slotKeyName: String, value: String) {
        updateSetting { current ->
            val next = current.apiKeys.toMutableMap()
            if (value.isBlank()) next.remove(slotKeyName) else next[slotKeyName] = value.trim()
            current.copy(apiKeys = next)
        }
    }

    // =========================================================================
    // OFFLINE UTILITIES (Notes, To-Do, Reminders)
    // =========================================================================
    fun saveNote(id: Long = 0L, title: String, content: String) {
        if (title.isBlank() && content.isBlank()) return
        viewModelScope.launch {
            repository.saveNote(NoteEntity(id = id, title = title.ifBlank { "Untitled Note" }, content = content))
            emitSnackbar("Note saved")
        }
    }
    fun deleteNote(id: Long) { viewModelScope.launch { repository.deleteNote(id) } }

    fun addTodo(text: String) {
        if (text.isBlank()) return
        viewModelScope.launch { repository.saveTodo(TodoEntity(text = text.trim())) }
    }
    fun toggleTodo(todo: TodoEntity) {
        viewModelScope.launch { repository.saveTodo(todo.copy(isDone = !todo.isDone)) }
    }
    fun deleteTodo(id: Long) { viewModelScope.launch { repository.deleteTodo(id) } }

    fun addReminder(title: String, dueLabel: String) {
        if (title.isBlank()) return
        viewModelScope.launch {
            repository.saveReminder(ReminderEntity(title = title.trim(), dueLabel = dueLabel.ifBlank { "Today" }))
            emitSnackbar("Reminder added")
        }
    }
    fun toggleReminder(reminder: ReminderEntity) {
        viewModelScope.launch { repository.saveReminder(reminder.copy(isCompleted = !reminder.isCompleted)) }
    }
    fun deleteReminder(id: Long) { viewModelScope.launch { repository.deleteReminder(id) } }

    // =========================================================================
    // SINGLE-FILE JSON BACKUP (Export / Import)
    // =========================================================================
    suspend fun exportBackupJson(): String = repository.exportBackupJson()

    fun importBackupJson(rawJson: String) {
        viewModelScope.launch {
            repository.importBackupJson(rawJson)
                .onSuccess { msg -> emitSnackbar(msg) }
                .onFailure { err -> emitSnackbar("Import failed: ${err.localizedMessage ?: "Invalid JSON"}") }
        }
    }

    fun handleIncomingViewIntent(intent: Intent?) {
        if (intent?.action == Intent.ACTION_VIEW) {
            val incomingUrl = intent.dataString
            if (!incomingUrl.isNullOrBlank() &&
                (incomingUrl.startsWith("http://") || incomingUrl.startsWith("https://"))
            ) {
                openNewTab(isIncognito = false, initialUrl = incomingUrl)
            }
        }
    }

    fun emitSnackbar(message: String) { _snackbarMessages.tryEmit(message) }

    override fun onCleared() {
        super.onCleared()
        aiStreamJob?.cancel()
        hubRequestJob?.cancel()
        downloaderJob?.cancel()
        healthCheckJob?.cancel()
        networkMonitor.unregister()
        webViewManager.destroyAll()
    }
}
