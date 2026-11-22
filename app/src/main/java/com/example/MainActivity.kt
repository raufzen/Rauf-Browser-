package com.example

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.view.ViewGroup
import android.webkit.PermissionRequest
import android.webkit.ValueCallback
import android.webkit.WebChromeClient
import android.widget.FrameLayout
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.displayCutout
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.ime
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Apps
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.CloudOff
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.FullscreenExit
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.MonitorHeart
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationRail
import androidx.compose.material3.NavigationRailItem
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.isCtrlPressed
import androidx.compose.ui.input.key.isMetaPressed
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onPreviewKeyEvent
import androidx.compose.ui.input.key.type
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.engine.SitePermissionPrompt
import com.example.ui.components.AddShortcutDialog
import com.example.ui.components.BrowserBottomBar
import com.example.ui.components.BrowserTopAddressBar
import com.example.ui.components.CommandPaletteDialog
import com.example.ui.components.CustomizeHomepageDialog
import com.example.ui.components.ExternalLinkConfirmDialog
import com.example.ui.components.FindInPageBar
import com.example.ui.components.GlassCard
import com.example.ui.components.SitePermissionDialog
import com.example.ui.components.SslErrorInterstitial
import com.example.ui.screens.AboutBrowserScreen
import com.example.ui.screens.AiAssistantOverlay
import com.example.ui.screens.ApiHubScreen
import com.example.ui.screens.ApiStatusScreen
import com.example.ui.screens.BookmarksScreen
import com.example.ui.screens.BrowserHomeScreen
import com.example.ui.screens.DownloaderHubScreen
import com.example.ui.screens.DownloadsScreen
import com.example.ui.screens.HistoryScreen
import com.example.ui.screens.PrivacyPolicyScreen
import com.example.ui.screens.SettingsScreen
import com.example.ui.screens.TabSwitcherScreen
import com.example.ui.screens.UtilitiesScreen
import com.example.ui.theme.RaufBrowserTheme
import com.example.ui.theme.WarningAmber
import com.example.viewmodel.BrowserScreen
import com.example.viewmodel.BrowserViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class MainActivity : ComponentActivity() {
    private val viewModel: BrowserViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        enableEdgeToEdge()
        viewModel.handleIncomingViewIntent(intent)

        setContent {
            RaufBrowserApp(viewModel = viewModel)
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        viewModel.handleIncomingViewIntent(intent)
    }
}

@Composable
fun RaufBrowserApp(viewModel: BrowserViewModel) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    val settings by viewModel.settings.collectAsStateWithLifecycle()
    val tabs by viewModel.tabs.collectAsStateWithLifecycle()
    val activeTabId by viewModel.activeTabId.collectAsStateWithLifecycle()
    val closedTabs by viewModel.closedTabs.collectAsStateWithLifecycle()
    val currentScreen by viewModel.currentScreen.collectAsStateWithLifecycle()
    val bookmarks by viewModel.bookmarks.collectAsStateWithLifecycle()
    val bookmarkFolders by viewModel.bookmarkFolders.collectAsStateWithLifecycle()
    val recentHistory by viewModel.recentHistory.collectAsStateWithLifecycle()
    val frequentSites by viewModel.frequentSites.collectAsStateWithLifecycle()
    val downloads by viewModel.downloads.collectAsStateWithLifecycle()
    val shortcuts by viewModel.shortcuts.collectAsStateWithLifecycle()
    val toolHistory by viewModel.toolHistory.collectAsStateWithLifecycle()
    val aiSessions by viewModel.aiSessions.collectAsStateWithLifecycle()
    val activeAiSessionId by viewModel.activeAiSessionId.collectAsStateWithLifecycle()
    val activeAiMessages by viewModel.activeAiMessages.collectAsStateWithLifecycle()
    val notes by viewModel.notes.collectAsStateWithLifecycle()
    val todos by viewModel.todos.collectAsStateWithLifecycle()
    val reminders by viewModel.reminders.collectAsStateWithLifecycle()
    val isOnline by viewModel.isOnline.collectAsStateWithLifecycle()
    val isFullScreen by viewModel.isFullScreenMode.collectAsStateWithLifecycle()
    val isCommandPaletteVisible by viewModel.isCommandPaletteVisible.collectAsStateWithLifecycle()
    val sslAlert by viewModel.sslAlert.collectAsStateWithLifecycle()
    val externalLinkPrompt by viewModel.externalLinkPrompt.collectAsStateWithLifecycle()
    val sitePermissionPrompt by viewModel.sitePermissionPrompt.collectAsStateWithLifecycle()
    val fullScreenVideo by viewModel.fullScreenVideo.collectAsStateWithLifecycle()
    val findInPage by viewModel.findInPage.collectAsStateWithLifecycle()
    val aiPanel by viewModel.aiPanel.collectAsStateWithLifecycle()
    val downloaderState by viewModel.downloaderState.collectAsStateWithLifecycle()
    val toolHubState by viewModel.toolHubState.collectAsStateWithLifecycle()
    val providerHealthMap by viewModel.providerHealthMap.collectAsStateWithLifecycle()
    val isTestingAllProviders by viewModel.isTestingAllProviders.collectAsStateWithLifecycle()

    val activeTab = remember(tabs, activeTabId) {
        tabs.firstOrNull { it.id == activeTabId } ?: tabs.first()
    }
    val isBookmarked = remember(bookmarks, activeTab.url) {
        !activeTab.isHome && bookmarks.any { it.url == activeTab.url }
    }

    val snackbarHostState = remember { SnackbarHostState() }
    var showCustomizeHome by remember { mutableStateOf(false) }
    var showAddShortcut by remember { mutableStateOf(false) }
    var pendingFileCallback by remember { mutableStateOf<ValueCallback<Array<Uri>>?>(null) }

    // SAF JSON Backup Export & Import launchers (zero storage permission needed)
    val exportBackupLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("application/json")
    ) { uri ->
        if (uri != null) {
            scope.launch {
                val json = viewModel.exportBackupJson()
                val written = withContext(Dispatchers.IO) {
                    runCatching {
                        context.contentResolver.openOutputStream(uri)?.use { out ->
                            out.write(json.toByteArray(Charsets.UTF_8))
                        }
                    }.isSuccess
                }
                viewModel.emitSnackbar(if (written) "Backup exported to JSON" else "Unable to write backup file")
            }
        }
    }

    val importBackupLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri ->
        if (uri != null) {
            scope.launch {
                val text = withContext(Dispatchers.IO) {
                    runCatching {
                        context.contentResolver.openInputStream(uri)?.bufferedReader()?.use { it.readText() }
                    }.getOrNull()
                }
                if (!text.isNullOrBlank()) {
                    viewModel.importBackupJson(text)
                } else {
                    viewModel.emitSnackbar("Selected file is empty or unreadable")
                }
            }
        }
    }

    val fileChooserLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        val uris = WebChromeClient.FileChooserParams.parseResult(result.resultCode, result.data)
        pendingFileCallback?.onReceiveValue(uris)
        pendingFileCallback = null
    }

    val siteRuntimePermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { grants ->
        viewModel.resolveSitePermission(grants.values.all { it })
    }

    val notificationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (!granted) viewModel.emitSnackbar("Notification permission denied by system")
    }

    DisposableEffect(viewModel) {
        viewModel.fileChooserHandler = { callback, params ->
            pendingFileCallback?.onReceiveValue(null)
            pendingFileCallback = callback
            runCatching {
                fileChooserLauncher.launch(params.createIntent())
                true
            }.getOrElse {
                pendingFileCallback?.onReceiveValue(null)
                pendingFileCallback = null
                false
            }
        }
        onDispose {
            viewModel.fileChooserHandler = null
            pendingFileCallback?.onReceiveValue(null)
        }
    }

    LaunchedEffect(Unit) {
        viewModel.snackbarMessages.collect { msg ->
            snackbarHostState.showSnackbar(msg)
        }
    }

    BackHandler(enabled = fullScreenVideo != null || isFullScreen || findInPage.isVisible || !activeTab.isHome) {
        when {
            fullScreenVideo != null -> viewModel.exitFullScreenVideo()
            isFullScreen -> viewModel.exitFullScreenMode()
            findInPage.isVisible -> viewModel.closeFindInPage()
            else -> viewModel.goBack()
        }
    }

    val view = LocalView.current
    val immersiveActive = isFullScreen || fullScreenVideo != null
    LaunchedEffect(immersiveActive) {
        val window = context.findHostActivity()?.window ?: return@LaunchedEffect
        val controller = WindowCompat.getInsetsController(window, view)
        if (immersiveActive) {
            controller.systemBarsBehavior =
                WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
            controller.hide(WindowInsetsCompat.Type.systemBars())
        } else {
            controller.show(WindowInsetsCompat.Type.systemBars())
        }
    }

    RaufBrowserTheme(
        themeMode = settings.themeMode,
        accentColorHex = settings.accentColorHex,
        fontScale = settings.fontScale,
        isIncognito = activeTab.isIncognito
    ) {
        val density = LocalDensity.current
        val isImeVisible = WindowInsets.ime.getBottom(density) > 0
        val webViewBgArgb = MaterialTheme.colorScheme.background.toArgb()

        BoxWithConstraints(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .windowInsetsPadding(WindowInsets.displayCutout.only(WindowInsetsSides.Horizontal))
                .onPreviewKeyEvent { keyEvent ->
                    if (keyEvent.type == KeyEventType.KeyDown &&
                        keyEvent.key == Key.K &&
                        (keyEvent.isCtrlPressed || keyEvent.isMetaPressed)
                    ) {
                        viewModel.openCommandPalette()
                        true
                    } else {
                        false
                    }
                }
        ) {
            val isExpandedScreen = maxWidth >= 840.dp

            Row(modifier = Modifier.fillMaxSize()) {
                // Adaptive NavigationRail on tablets, foldables, and ChromeOS
                if (isExpandedScreen && !isFullScreen) {
                    NavigationRail(
                        modifier = Modifier
                            .fillMaxHeight()
                            .statusBarsPadding()
                            .navigationBarsPadding(),
                        containerColor = MaterialTheme.colorScheme.surface
                    ) {
                        NavigationRailItem(
                            selected = currentScreen == BrowserScreen.BROWSER,
                            onClick = { viewModel.navigateScreen(BrowserScreen.BROWSER) },
                            icon = { Icon(Icons.Default.Home, contentDescription = "Browser") },
                            label = { Text("Home") }
                        )
                        NavigationRailItem(
                            selected = aiPanel.isVisible,
                            onClick = { viewModel.openAiPanel() },
                            icon = { Icon(Icons.Default.AutoAwesome, contentDescription = "AI Chat") },
                            label = { Text("AI") }
                        )
                        NavigationRailItem(
                            selected = currentScreen == BrowserScreen.DOWNLOADER_HUB,
                            onClick = { viewModel.navigateScreen(BrowserScreen.DOWNLOADER_HUB) },
                            icon = { Icon(Icons.Default.Download, contentDescription = "Downloaders") },
                            label = { Text("DL") }
                        )
                        NavigationRailItem(
                            selected = currentScreen == BrowserScreen.API_HUB,
                            onClick = { viewModel.navigateScreen(BrowserScreen.API_HUB) },
                            icon = { Icon(Icons.Default.Apps, contentDescription = "Tools") },
                            label = { Text("Tools") }
                        )
                        NavigationRailItem(
                            selected = currentScreen == BrowserScreen.UTILITIES,
                            onClick = { viewModel.navigateScreen(BrowserScreen.UTILITIES) },
                            icon = { Icon(Icons.Default.Build, contentDescription = "Utilities") },
                            label = { Text("Utils") }
                        )
                        NavigationRailItem(
                            selected = currentScreen == BrowserScreen.API_STATUS,
                            onClick = { viewModel.navigateScreen(BrowserScreen.API_STATUS) },
                            icon = { Icon(Icons.Default.MonitorHeart, contentDescription = "Status") },
                            label = { Text("Status") }
                        )
                        NavigationRailItem(
                            selected = false,
                            onClick = { viewModel.openCommandPalette() },
                            icon = { Icon(Icons.Default.Search, contentDescription = "Command Palette") },
                            label = { Text("⌘K") }
                        )
                        NavigationRailItem(
                            selected = currentScreen == BrowserScreen.SETTINGS,
                            onClick = { viewModel.navigateScreen(BrowserScreen.SETTINGS) },
                            icon = { Icon(Icons.Default.Settings, contentDescription = "Settings") },
                            label = { Text("Settings") }
                        )
                    }
                }

                Box(modifier = Modifier.weight(1f).fillMaxHeight()) {
                    // Core Browser Workspace
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .imePadding()
                    ) {
                        if (!isFullScreen) {
                            BrowserTopAddressBar(
                                tab = activeTab,
                                searchEngine = settings.searchEngine,
                                isBookmarked = isBookmarked,
                                bookmarks = bookmarks,
                                history = recentHistory,
                                onSubmitUrl = { viewModel.submitOmniboxInput(it) },
                                onAskSearchAi = { query -> viewModel.openAiPanel(autoPrompt = query) },
                                onReloadOrStop = { viewModel.reloadOrStop() },
                                onToggleBookmark = { viewModel.toggleBookmarkForActiveTab() },
                                onToggleDesktop = { viewModel.toggleDesktopMode() },
                                onOpenAiPanel = { viewModel.openAiPanel() }
                            )
                        }

                        AnimatedVisibility(visible = !isOnline) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(WarningAmber.copy(alpha = 0.2f))
                                    .padding(horizontal = 14.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(Icons.Default.CloudOff, contentDescription = "Offline", tint = WarningAmber, modifier = Modifier.size(16.dp))
                                Spacer(Modifier.width(8.dp))
                                Text(
                                    text = stringResource(R.string.offline_banner),
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }

                        AnimatedVisibility(visible = findInPage.isVisible && !activeTab.isHome) {
                            FindInPageBar(
                                state = findInPage,
                                onQueryChange = { viewModel.updateFindQuery(it) },
                                onPrev = { viewModel.findNext(false) },
                                onNext = { viewModel.findNext(true) },
                                onClose = { viewModel.closeFindInPage() }
                            )
                        }

                        Box(modifier = Modifier.weight(1f).fillMaxWidth()) {
                            if (activeTab.isHome) {
                                BrowserHomeScreen(
                                    isIncognito = activeTab.isIncognito,
                                    settings = settings,
                                    shortcuts = shortcuts,
                                    frequentSites = frequentSites,
                                    recentHistory = recentHistory,
                                    bookmarks = bookmarks,
                                    onSearchOrNavigate = { viewModel.submitOmniboxInput(it) },
                                    onOpenUrl = { viewModel.loadUrlInTab(it) },
                                    onAskSearchAi = { query -> viewModel.openAiPanel(autoPrompt = query) },
                                    onOpenIncognitoTab = { viewModel.openNewTab(isIncognito = true) },
                                    onOpenBookmarks = { viewModel.navigateScreen(BrowserScreen.BOOKMARKS) },
                                    onOpenHistory = { viewModel.navigateScreen(BrowserScreen.HISTORY) },
                                    onOpenApiHub = { viewModel.navigateScreen(BrowserScreen.API_HUB) },
                                    onOpenDownloaderHub = { viewModel.navigateScreen(BrowserScreen.DOWNLOADER_HUB) },
                                    onOpenUtilities = { viewModel.navigateScreen(BrowserScreen.UTILITIES) },
                                    onOpenApiStatus = { viewModel.navigateScreen(BrowserScreen.API_STATUS) },
                                    onOpenCommandPalette = { viewModel.openCommandPalette() },
                                    onOpenFeatureTool = { feat -> viewModel.openToolInHub(feat) },
                                    onOpenSettings = { viewModel.navigateScreen(BrowserScreen.SETTINGS) },
                                    onCustomizeHome = { showCustomizeHome = true },
                                    onAddShortcutClick = { showAddShortcut = true },
                                    onDeleteShortcut = { viewModel.deleteShortcut(it) }
                                )
                            } else {
                                AndroidView(
                                    factory = { ctx -> FrameLayout(ctx) },
                                    update = { container ->
                                        val targetWebView = viewModel.webViewManager.getOrCreateWebView(activeTab)
                                        targetWebView.setBackgroundColor(webViewBgArgb)
                                        if (container.childCount != 1 || container.getChildAt(0) !== targetWebView) {
                                            container.removeAllViews()
                                            (targetWebView.parent as? ViewGroup)?.removeView(targetWebView)
                                            container.addView(
                                                targetWebView,
                                                FrameLayout.LayoutParams(
                                                    ViewGroup.LayoutParams.MATCH_PARENT,
                                                    ViewGroup.LayoutParams.MATCH_PARENT
                                                )
                                            )
                                        }
                                    },
                                    modifier = Modifier.fillMaxSize()
                                )

                                activeTab.errorMessage?.let { err ->
                                    Box(
                                        modifier = Modifier
                                            .fillMaxSize()
                                            .background(MaterialTheme.colorScheme.background.copy(alpha = 0.94f))
                                            .padding(24.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        GlassCard(shape = RoundedCornerShape(22.dp)) {
                                            Column(
                                                modifier = Modifier.padding(22.dp),
                                                horizontalAlignment = Alignment.CenterHorizontally
                                            ) {
                                                Icon(Icons.Default.CloudOff, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(44.dp))
                                                Spacer(Modifier.size(12.dp))
                                                Text("Page couldn't load", style = MaterialTheme.typography.titleLarge)
                                                Spacer(Modifier.size(6.dp))
                                                Text(err, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                                Spacer(Modifier.size(16.dp))
                                                Button(onClick = { viewModel.reloadOrStop() }) {
                                                    Icon(Icons.Default.Refresh, contentDescription = null)
                                                    Spacer(Modifier.width(6.dp))
                                                    Text("Retry")
                                                }
                                            }
                                        }
                                    }
                                }
                            }

                            if (isFullScreen) {
                                Box(
                                    modifier = Modifier
                                        .align(Alignment.TopEnd)
                                        .statusBarsPadding()
                                        .padding(12.dp)
                                        .clip(RoundedCornerShape(20.dp))
                                        .background(Color(0xCC140E24))
                                        .clickable { viewModel.exitFullScreenMode() }
                                        .padding(horizontal = 12.dp, vertical = 8.dp)
                                        .testTag("exit_fullscreen_pill")
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(Icons.Default.FullscreenExit, contentDescription = "Exit full-screen mode", tint = Color.White, modifier = Modifier.size(18.dp))
                                        Spacer(Modifier.width(6.dp))
                                        Text("Exit Fullscreen", style = MaterialTheme.typography.labelSmall, color = Color.White)
                                    }
                                }
                            }
                        }

                        if (!isFullScreen && !isImeVisible) {
                            BrowserBottomBar(
                                tab = activeTab,
                                tabCount = tabs.size,
                                onBack = { viewModel.goBack() },
                                onForward = { viewModel.goForward() },
                                onHome = { viewModel.goHome() },
                                onOpenTabs = { viewModel.navigateScreen(BrowserScreen.TABS) },
                                onNewTab = { viewModel.openNewTab(isIncognito = false) },
                                onNewIncognitoTab = { viewModel.openNewTab(isIncognito = true) },
                                onOpenBookmarks = { viewModel.navigateScreen(BrowserScreen.BOOKMARKS) },
                                onOpenHistory = { viewModel.navigateScreen(BrowserScreen.HISTORY) },
                                onOpenDownloads = { viewModel.navigateScreen(BrowserScreen.DOWNLOADS) },
                                onOpenApiHub = { viewModel.navigateScreen(BrowserScreen.API_HUB) },
                                onOpenDownloaderHub = { viewModel.navigateScreen(BrowserScreen.DOWNLOADER_HUB) },
                                onOpenUtilities = { viewModel.navigateScreen(BrowserScreen.UTILITIES) },
                                onOpenApiStatus = { viewModel.navigateScreen(BrowserScreen.API_STATUS) },
                                onOpenCommandPalette = { viewModel.openCommandPalette() },
                                onToggleDesktop = { viewModel.toggleDesktopMode() },
                                onToggleFullScreen = { viewModel.toggleFullScreenMode() },
                                onOpenFindInPage = { viewModel.openFindInPage() },
                                onSharePage = {
                                    if (!activeTab.isHome) {
                                        val shareIntent = Intent(Intent.ACTION_SEND).apply {
                                            type = "text/plain"
                                            putExtra(Intent.EXTRA_SUBJECT, activeTab.title)
                                            putExtra(Intent.EXTRA_TEXT, activeTab.url)
                                        }
                                        context.startActivity(Intent.createChooser(shareIntent, "Share via"))
                                    }
                                },
                                onOpenSettings = { viewModel.navigateScreen(BrowserScreen.SETTINGS) }
                            )
                        }
                    }

                    // Secondary Screens
                    when (currentScreen) {
                        BrowserScreen.BROWSER -> Unit
                        BrowserScreen.TABS -> TabSwitcherScreen(
                            tabs = tabs,
                            activeTabId = activeTabId,
                            closedTabs = closedTabs,
                            onBack = { viewModel.navigateBackToBrowser() },
                            onSelectTab = { viewModel.switchToTab(it) },
                            onCloseTab = { viewModel.closeTab(it) },
                            onCloseAllTabs = { viewModel.closeAllTabs(it) },
                            onNewTab = { isPrivate -> viewModel.openNewTab(isIncognito = isPrivate) },
                            onRestoreClosedTab = { viewModel.restoreLastClosedTab() }
                        )
                        BrowserScreen.BOOKMARKS -> BookmarksScreen(
                            bookmarks = bookmarks,
                            folders = bookmarkFolders,
                            onBack = { viewModel.navigateBackToBrowser() },
                            onOpenUrl = { viewModel.loadUrlInTab(it) },
                            onAddBookmark = { t, u, f -> viewModel.saveBookmark(t, u, f) },
                            onEditBookmark = { viewModel.editBookmark(it) },
                            onDeleteBookmark = { viewModel.removeBookmark(it) }
                        )
                        BrowserScreen.HISTORY -> HistoryScreen(
                            history = recentHistory,
                            onBack = { viewModel.navigateBackToBrowser() },
                            onOpenUrl = { viewModel.loadUrlInTab(it) },
                            onDeleteItem = { viewModel.deleteHistoryItem(it) },
                            onClearByRange = { viewModel.clearHistoryByRange(it) }
                        )
                        BrowserScreen.DOWNLOADS -> DownloadsScreen(
                            downloads = downloads,
                            onBack = { viewModel.navigateBackToBrowser() },
                            onStartDirectDownload = { viewModel.startDownload(it) },
                            onOpenFile = { viewModel.openDownloadedFile(it) },
                            onCancelDownload = { viewModel.cancelDownload(it) },
                            onRetryDownload = { viewModel.retryDownload(it) },
                            onDeleteRecord = { viewModel.deleteDownloadRecord(it) },
                            onClearFinished = { viewModel.clearFinishedDownloads() }
                        )
                        BrowserScreen.DOWNLOADER_HUB -> DownloaderHubScreen(
                            state = downloaderState,
                            onBack = { viewModel.navigateBackToBrowser() },
                            onInputChanged = { url, fmt -> viewModel.updateDownloaderInput(url, fmt) },
                            onRunDownload = { next -> viewModel.runDownloader(tryNextProvider = next) },
                            onEnqueueSystemDownload = { url -> viewModel.startDownload(url) },
                            onOpenInBrowser = { url -> viewModel.loadUrlInTab(url) },
                            onNotify = { viewModel.emitSnackbar(it) }
                        )
                        BrowserScreen.API_HUB -> ApiHubScreen(
                            hubState = toolHubState,
                            favoriteToolIds = settings.favoriteToolIds,
                            disabledProviderIds = settings.disabledProviderIds,
                            providerPriorityOrder = settings.providerPriorityOrder,
                            toolHistory = toolHistory,
                            onBack = { viewModel.navigateBackToBrowser() },
                            onSelectFeature = { viewModel.selectToolFeature(it) },
                            onRunTool = { feat, q, extra, explicitId, tryNext ->
                                viewModel.runToolFeature(feat, q, extra, explicitId, tryNext)
                            },
                            onToggleFavorite = { viewModel.toggleFavoriteTool(it) },
                            onRerunHistory = { viewModel.rerunToolHistory(it) },
                            onClearHistory = { viewModel.clearToolHistory() },
                            onOpenAiChat = { viewModel.openAiPanel() },
                            onOpenDownloaderHub = { viewModel.navigateScreen(BrowserScreen.DOWNLOADER_HUB) },
                            onOpenUtilities = { viewModel.navigateScreen(BrowserScreen.UTILITIES) },
                            onOpenApiStatus = { viewModel.navigateScreen(BrowserScreen.API_STATUS) },
                            onOpenInBrowser = { url -> viewModel.loadUrlInTab(url) },
                            onDownloadUrl = { url -> viewModel.startDownload(url) },
                            onCopiedNotice = { viewModel.emitSnackbar(it) }
                        )
                        BrowserScreen.UTILITIES -> UtilitiesScreen(
                            notes = notes,
                            todos = todos,
                            reminders = reminders,
                            onBack = { viewModel.navigateBackToBrowser() },
                            onSaveNote = { id, t, c -> viewModel.saveNote(id, t, c) },
                            onDeleteNote = { viewModel.deleteNote(it) },
                            onAddTodo = { viewModel.addTodo(it) },
                            onToggleTodo = { viewModel.toggleTodo(it) },
                            onDeleteTodo = { viewModel.deleteTodo(it) },
                            onAddReminder = { t, d -> viewModel.addReminder(t, d) },
                            onToggleReminder = { viewModel.toggleReminder(it) },
                            onDeleteReminder = { viewModel.deleteReminder(it) },
                            onNotify = { viewModel.emitSnackbar(it) }
                        )
                        BrowserScreen.API_STATUS -> ApiStatusScreen(
                            healthMap = providerHealthMap,
                            disabledProviderIds = settings.disabledProviderIds,
                            providerPriorityOrder = settings.providerPriorityOrder,
                            isTestingAll = isTestingAllProviders,
                            onBack = { viewModel.navigateBackToBrowser() },
                            onRetestAll = { viewModel.retestAllProviders() },
                            onTestSingle = { viewModel.testSingleProvider(it) },
                            onToggleEnabled = { viewModel.toggleProviderEnabled(it) },
                            onMovePriority = { id, delta -> viewModel.moveProviderPriority(id, delta) },
                            onResetPriority = { viewModel.resetProviderPriority() }
                        )
                        BrowserScreen.SETTINGS -> SettingsScreen(
                            settings = settings,
                            isDesktopMode = activeTab.isDesktopMode,
                            onToggleDesktopMode = { viewModel.toggleDesktopMode() },
                            onBack = { viewModel.navigateBackToBrowser() },
                            onUpdateSettings = { viewModel.updateSetting(it) },
                            onSetApiKey = { k, v -> viewModel.setApiKey(k, v) },
                            onOpenApiStatus = { viewModel.navigateScreen(BrowserScreen.API_STATUS) },
                            onExportBackup = { exportBackupLauncher.launch("rauf_browser_backup.json") },
                            onImportBackup = { importBackupLauncher.launch(arrayOf("application/json", "text/plain", "*/*")) },
                            onClearCache = { viewModel.clearCache() },
                            onClearCookies = { viewModel.clearCookies() },
                            onClearAllData = { viewModel.clearAllBrowsingData() },
                            onOpenDownloads = { viewModel.navigateScreen(BrowserScreen.DOWNLOADS) },
                            onOpenDefaultBrowserSettings = {
                                runCatching {
                                    context.startActivity(Intent(Settings.ACTION_MANAGE_DEFAULT_APPS_SETTINGS))
                                }.onFailure {
                                    viewModel.emitSnackbar("Open Android Settings > Apps > Default apps to set Rauf Browser")
                                }
                            },
                            onRequestNotificationPermission = {
                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                                    val granted = ContextCompat.checkSelfPermission(
                                        context, Manifest.permission.POST_NOTIFICATIONS
                                    ) == PackageManager.PERMISSION_GRANTED
                                    if (!granted) notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                                }
                            },
                            onOpenPrivacyPolicy = { viewModel.navigateScreen(BrowserScreen.PRIVACY_POLICY) },
                            onOpenAbout = { viewModel.navigateScreen(BrowserScreen.ABOUT) }
                        )
                        BrowserScreen.PRIVACY_POLICY -> PrivacyPolicyScreen(onBack = { viewModel.navigateBackToBrowser() })
                        BrowserScreen.ABOUT -> AboutBrowserScreen(onBack = { viewModel.navigateBackToBrowser() })
                    }

                    // Unified AI Chat Overlay
                    if (aiPanel.isVisible) {
                        AiAssistantOverlay(
                            panelState = aiPanel,
                            sessions = aiSessions,
                            activeSessionId = activeAiSessionId,
                            messages = activeAiMessages,
                            systemPrompt = settings.aiSystemPrompt,
                            activeTab = activeTab,
                            onDismiss = { viewModel.closeAiPanel() },
                            onSelectModel = { viewModel.selectAiModel(it) },
                            onTogglePageContext = { viewModel.toggleAiPageContext(it) },
                            onSendPrompt = { viewModel.sendAiPrompt(it) },
                            onStopGenerating = { viewModel.stopAiGenerating() },
                            onRegenerate = { tryNext -> viewModel.regenerateLastAiMessage(tryNext) },
                            onCreateSession = { viewModel.createNewAiSession() },
                            onSelectSession = { viewModel.selectAiSession(it) },
                            onRenameSession = { id, title -> viewModel.renameAiSession(id, title) },
                            onDeleteSession = { viewModel.deleteAiSession(it) },
                            onUpdateSystemPrompt = { viewModel.updateAiSystemPrompt(it) },
                            onDownloadImageUrl = { url -> viewModel.startDownload(url) },
                            onNotify = { viewModel.emitSnackbar(it) }
                        )
                    }

                    if (isCommandPaletteVisible) {
                        CommandPaletteDialog(
                            onDismiss = { viewModel.closeCommandPalette() },
                            onOpenAi = { viewModel.openAiPanel() },
                            onOpenDownloaderHub = { viewModel.navigateScreen(BrowserScreen.DOWNLOADER_HUB) },
                            onOpenToolHub = { feat -> viewModel.openToolInHub(feat) },
                            onOpenUtilities = { viewModel.navigateScreen(BrowserScreen.UTILITIES) },
                            onOpenApiStatus = { viewModel.navigateScreen(BrowserScreen.API_STATUS) },
                            onOpenSettings = { viewModel.navigateScreen(BrowserScreen.SETTINGS) },
                            onOpenIncognito = { viewModel.openNewTab(isIncognito = true) },
                            onSelectTheme = { mode -> viewModel.updateThemeMode(mode) }
                        )
                    }

                    // Fullscreen HTML5 Video Host
                    fullScreenVideo?.let { video ->
                        Box(modifier = Modifier.fillMaxSize().background(Color.Black)) {
                            AndroidView(
                                factory = { ctx ->
                                    FrameLayout(ctx).apply {
                                        (video.customView.parent as? ViewGroup)?.removeView(video.customView)
                                        addView(video.customView)
                                    }
                                },
                                modifier = Modifier.fillMaxSize()
                            )
                        }
                    }

                    sslAlert?.let { alert ->
                        SslErrorInterstitial(alert = alert, onBackToSafety = { viewModel.dismissSslAlertAndGoSafe() })
                    }

                    externalLinkPrompt?.let { prompt ->
                        ExternalLinkConfirmDialog(
                            prompt = prompt,
                            onConfirm = {
                                viewModel.dismissExternalLinkPrompt()
                                runCatching {
                                    val intent = if (prompt.url.startsWith("intent://")) {
                                        Intent.parseUri(prompt.url, Intent.URI_INTENT_SCHEME)
                                    } else {
                                        Intent(Intent.ACTION_VIEW, Uri.parse(prompt.url))
                                    }
                                    context.startActivity(intent)
                                }.onFailure {
                                    viewModel.emitSnackbar("No installed app can open this link")
                                }
                            },
                            onDismiss = { viewModel.dismissExternalLinkPrompt() }
                        )
                    }

                    sitePermissionPrompt?.let { prompt ->
                        SitePermissionDialog(
                            prompt = prompt,
                            onAllow = {
                                val neededPermissions = when (prompt) {
                                    is SitePermissionPrompt.Geolocation -> listOf(
                                        Manifest.permission.ACCESS_FINE_LOCATION,
                                        Manifest.permission.ACCESS_COARSE_LOCATION
                                    )
                                    is SitePermissionPrompt.Media -> buildList {
                                        if (PermissionRequest.RESOURCE_VIDEO_CAPTURE in prompt.resources) add(Manifest.permission.CAMERA)
                                        if (PermissionRequest.RESOURCE_AUDIO_CAPTURE in prompt.resources) add(Manifest.permission.RECORD_AUDIO)
                                    }
                                }
                                val missing = neededPermissions.filter { perm ->
                                    ContextCompat.checkSelfPermission(context, perm) != PackageManager.PERMISSION_GRANTED
                                }
                                if (missing.isEmpty()) viewModel.resolveSitePermission(true)
                                else siteRuntimePermissionLauncher.launch(missing.toTypedArray())
                            },
                            onDeny = { viewModel.resolveSitePermission(false) }
                        )
                    }

                    if (showCustomizeHome) {
                        CustomizeHomepageDialog(
                            settings = settings,
                            onUpdateSettings = { viewModel.updateSetting(it) },
                            onDismiss = { showCustomizeHome = false }
                        )
                    }

                    if (showAddShortcut) {
                        AddShortcutDialog(
                            onConfirm = { title, url ->
                                viewModel.addShortcut(title, url)
                                showAddShortcut = false
                            },
                            onDismiss = { showAddShortcut = false }
                        )
                    }

                    SnackbarHost(
                        hostState = snackbarHostState,
                        modifier = Modifier
                            .align(Alignment.BottomCenter)
                            .navigationBarsPadding()
                            .padding(bottom = 68.dp)
                    )
                }
            }
        }
    }
}

private tailrec fun Context.findHostActivity(): Activity? = when (this) {
    is Activity -> this
    is ContextWrapper -> baseContext.findHostActivity()
    else -> null
}
