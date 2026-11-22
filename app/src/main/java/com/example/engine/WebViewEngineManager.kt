package com.example.engine

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.net.Uri
import android.net.http.SslError
import android.os.Build
import android.view.View
import android.view.ViewGroup
import android.webkit.CookieManager
import android.webkit.GeolocationPermissions
import android.webkit.PermissionRequest
import android.webkit.SslErrorHandler
import android.webkit.URLUtil
import android.webkit.ValueCallback
import android.webkit.WebChromeClient
import android.webkit.WebResourceError
import android.webkit.WebResourceRequest
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import com.example.core.UrlResolver
import com.example.data.local.BrowserSettings
import org.json.JSONTokener
import java.util.concurrent.ConcurrentHashMap

interface WebViewCallbacks {
    fun onTabUpdated(tabId: String, transform: (BrowserTab) -> BrowserTab)
    fun onRecordHistory(tabId: String, isIncognito: Boolean, title: String, url: String)
    fun onSslError(alert: SslAlertState)
    fun onExternalLink(prompt: ExternalLinkPrompt)
    fun onDownloadRequested(url: String, userAgent: String, contentDisposition: String, mimeType: String)
    fun onFileChooserRequested(callback: ValueCallback<Array<Uri>>, params: WebChromeClient.FileChooserParams): Boolean
    fun onSitePermissionPrompt(prompt: SitePermissionPrompt)
    fun onFullScreenVideoChanged(customView: View?, callback: WebChromeClient.CustomViewCallback?)
    fun onFindMatchesUpdated(activeOrdinal: Int, totalMatches: Int)
    fun getCurrentSettings(): BrowserSettings
}

class WebViewEngineManager(
    private val context: Context,
    private val callbacks: WebViewCallbacks
) {
    private val webViews = ConcurrentHashMap<String, WebView>()
    private val desktopModeByTab = ConcurrentHashMap<String, Boolean>()
    private var defaultMobileUserAgent: String? = null

    companion object {
        private const val DESKTOP_USER_AGENT =
            "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/131.0.0.0 Safari/537.36"
        private const val DESKTOP_VIEWPORT_WIDTH_PX = 1280

        fun normalizeUrlForDesktopMode(url: String, desktopMode: Boolean): String {
            if (!desktopMode || url.isBlank()) return url
            return runCatching {
                val parsed = java.net.URI(url)
                val host = parsed.host ?: return url
                val lowerHost = host.lowercase()
                val desktopHost = when {
                    lowerHost.startsWith("m.") -> "www." + host.substring(2)
                    lowerHost.startsWith("mobile.") -> "www." + host.substring(7)
                    lowerHost.contains(".m.") -> host.replace(".m.", ".", ignoreCase = true)
                    else -> host
                }
                if (desktopHost == host) url
                else java.net.URI(
                    parsed.scheme,
                    parsed.userInfo,
                    desktopHost,
                    parsed.port,
                    parsed.path,
                    parsed.query,
                    parsed.fragment
                ).toString()
            }.getOrDefault(url)
        }
    }

    fun getOrCreateWebView(tab: BrowserTab): WebView {
        return webViews[tab.id] ?: createWebView(tab).also {
            webViews[tab.id] = it
        }
    }

    fun getWebView(tabId: String): WebView? = webViews[tabId]

    fun getAllWebViews(): Collection<WebView> = webViews.values

    fun getIncognitoWebViews(incognitoTabIds: Set<String>): List<WebView> =
        incognitoTabIds.mapNotNull { webViews[it] }

    @SuppressLint("SetJavaScriptEnabled")
    private fun createWebView(tab: BrowserTab): WebView {
        val webView = WebView(context).apply {
            layoutParams = ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
            )
            isFocusable = true
            isFocusableInTouchMode = true
            setLayerType(View.LAYER_TYPE_HARDWARE, null)
        }

        if (defaultMobileUserAgent == null) {
            defaultMobileUserAgent = webView.settings.userAgentString
        }

        applySettingsToWebView(webView, callbacks.getCurrentSettings(), tab.isDesktopMode, tab.isIncognito)

        webView.setFindListener { activeMatchOrdinal, numberOfMatches, _ ->
            callbacks.onFindMatchesUpdated(activeMatchOrdinal, numberOfMatches)
        }

        webView.setDownloadListener { url, userAgent, contentDisposition, mimeType, _ ->
            callbacks.onDownloadRequested(url, userAgent, contentDisposition, mimeType)
        }

        webView.webViewClient = object : WebViewClient() {
            override fun shouldOverrideUrlLoading(view: WebView, request: WebResourceRequest): Boolean {
                val url = request.url?.toString() ?: return false
                val settings = callbacks.getCurrentSettings()

                if (UrlResolver.isExternalScheme(url)) {
                    if (settings.safeExternalLinks) {
                        callbacks.onExternalLink(
                            ExternalLinkPrompt(
                                url = url,
                                scheme = request.url.scheme.orEmpty()
                            )
                        )
                    }
                    return true
                }

                // Upgrade http -> https when HTTPS-Only mode is active
                if (settings.httpsOnlyMode &&
                    url.startsWith("http://", ignoreCase = true) &&
                    !url.contains("localhost") &&
                    !url.contains("127.0.0.1")
                ) {
                    val upgraded = "https://" + url.removePrefix("http://")
                    val desktopActive = desktopModeByTab[tab.id] == true
                    view.loadUrl(upgraded, buildRequestHeaders(settings, desktopActive))
                    return true
                }

                return false
            }

            override fun onPageStarted(view: WebView, url: String?, favicon: Bitmap?) {
                val safeUrl = url ?: return
                callbacks.onTabUpdated(tab.id) { current ->
                    current.copy(
                        url = safeUrl,
                        isLoading = true,
                        progress = 10,
                        isSecure = UrlResolver.isSecureUrl(safeUrl),
                        canGoBack = view.canGoBack(),
                        canGoForward = view.canGoForward(),
                        favicon = favicon ?: current.favicon,
                        errorMessage = null
                    )
                }
            }

            override fun onPageCommitVisible(view: WebView, url: String?) {
                super.onPageCommitVisible(view, url)
                if (desktopModeByTab[tab.id] == true) {
                    injectDesktopViewportFit(view)
                }
            }

            override fun onPageFinished(view: WebView, url: String?) {
                val safeUrl = url ?: return
                if (desktopModeByTab[tab.id] == true) {
                    injectDesktopViewportFit(view)
                }
                val resolvedTitle = view.title?.takeIf { it.isNotBlank() } ?: UrlResolver.extractHost(safeUrl)
                callbacks.onTabUpdated(tab.id) { current ->
                    current.copy(
                        url = safeUrl,
                        title = resolvedTitle,
                        isLoading = false,
                        progress = 100,
                        isSecure = UrlResolver.isSecureUrl(safeUrl),
                        canGoBack = view.canGoBack(),
                        canGoForward = view.canGoForward()
                    )
                }
                callbacks.onRecordHistory(tab.id, tab.isIncognito, resolvedTitle, safeUrl)
                capturePreviewAsync(tab.id, view)
            }

            override fun doUpdateVisitedHistory(view: WebView, url: String?, isReload: Boolean) {
                if (url.isNullOrBlank()) return
                callbacks.onTabUpdated(tab.id) { current ->
                    current.copy(
                        url = url,
                        isSecure = UrlResolver.isSecureUrl(url),
                        canGoBack = view.canGoBack(),
                        canGoForward = view.canGoForward()
                    )
                }
            }

            override fun onReceivedError(
                view: WebView,
                request: WebResourceRequest,
                error: WebResourceError
            ) {
                if (request.isForMainFrame) {
                    val desc = error.description?.toString() ?: "Unable to load webpage."
                    callbacks.onTabUpdated(tab.id) { current ->
                        current.copy(
                            isLoading = false,
                            progress = 0,
                            errorMessage = desc
                        )
                    }
                }
            }

            override fun onReceivedSslError(view: WebView, handler: SslErrorHandler, error: SslError) {
                // CRITICAL SECURITY RULE: Never bypass SSL certificate errors!
                handler.cancel()
                val reason = when (error.primaryError) {
                    SslError.SSL_EXPIRED -> "The security certificate has expired."
                    SslError.SSL_IDMISMATCH -> "Certificate hostname does not match this website."
                    SslError.SSL_NOTYETVALID -> "The security certificate is not yet valid."
                    SslError.SSL_UNTRUSTED -> "The certificate authority is not trusted."
                    SslError.SSL_DATE_INVALID -> "The certificate date is invalid."
                    else -> "An untrusted or malformed SSL certificate was detected."
                }
                val cert = error.certificate
                callbacks.onTabUpdated(tab.id) { current ->
                    current.copy(
                        isLoading = false,
                        progress = 0,
                        isSecure = false
                    )
                }
                callbacks.onSslError(
                    SslAlertState(
                        tabId = tab.id,
                        url = error.url ?: view.url.orEmpty(),
                        reason = reason,
                        issuedTo = cert?.issuedTo?.cName ?: "Unknown host",
                        issuedBy = cert?.issuedBy?.oName ?: cert?.issuedBy?.cName ?: "Unknown issuer"
                    )
                )
            }
        }

        webView.webChromeClient = object : WebChromeClient() {
            override fun onProgressChanged(view: WebView, newProgress: Int) {
                callbacks.onTabUpdated(tab.id) { current ->
                    current.copy(
                        progress = newProgress,
                        isLoading = newProgress in 1..99
                    )
                }
            }

            override fun onReceivedTitle(view: WebView, title: String?) {
                if (!title.isNullOrBlank()) {
                    callbacks.onTabUpdated(tab.id) { it.copy(title = title) }
                }
            }

            override fun onReceivedIcon(view: WebView, icon: Bitmap?) {
                if (icon != null) {
                    callbacks.onTabUpdated(tab.id) { it.copy(favicon = icon) }
                }
            }

            override fun onShowFileChooser(
                webView: WebView,
                filePathCallback: ValueCallback<Array<Uri>>,
                fileChooserParams: FileChooserParams
            ): Boolean {
                return callbacks.onFileChooserRequested(filePathCallback, fileChooserParams)
            }

            override fun onGeolocationPermissionsShowPrompt(
                origin: String,
                callback: GeolocationPermissions.Callback
            ) {
                val settings = callbacks.getCurrentSettings()
                if (!settings.allowLocation) {
                    callback.invoke(origin, false, false)
                    return
                }
                callbacks.onSitePermissionPrompt(
                    SitePermissionPrompt.Geolocation(origin = origin, callback = callback)
                )
            }

            override fun onPermissionRequest(request: PermissionRequest) {
                val settings = callbacks.getCurrentSettings()
                val allowedRequested = request.resources.filter { res ->
                    when (res) {
                        PermissionRequest.RESOURCE_VIDEO_CAPTURE -> settings.allowCamera
                        PermissionRequest.RESOURCE_AUDIO_CAPTURE -> settings.allowMicrophone
                        else -> false
                    }
                }
                if (allowedRequested.isEmpty()) {
                    request.deny()
                    return
                }
                callbacks.onSitePermissionPrompt(
                    SitePermissionPrompt.Media(
                        origin = request.origin?.toString() ?: "Website",
                        resources = allowedRequested,
                        request = request
                    )
                )
            }

            override fun onShowCustomView(view: View?, callback: CustomViewCallback?) {
                callbacks.onFullScreenVideoChanged(view, callback)
            }

            override fun onHideCustomView() {
                callbacks.onFullScreenVideoChanged(null, null)
            }
        }

        return webView
    }

    @SuppressLint("SetJavaScriptEnabled")
    fun applySettingsToWebView(
        webView: WebView,
        settings: BrowserSettings,
        desktopMode: Boolean,
        isIncognito: Boolean
    ) {
        webView.settings.apply {
            javaScriptEnabled = settings.javaScriptEnabled
            domStorageEnabled = !isIncognito
            databaseEnabled = !isIncognito
            mixedContentMode = WebSettings.MIXED_CONTENT_NEVER_ALLOW
            allowFileAccess = false
            allowContentAccess = true
            @Suppress("DEPRECATION")
            allowFileAccessFromFileURLs = false
            @Suppress("DEPRECATION")
            allowUniversalAccessFromFileURLs = false
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                safeBrowsingEnabled = true
            }
            builtInZoomControls = true
            displayZoomControls = false
            setSupportZoom(true)
            useWideViewPort = true
            loadWithOverviewMode = true
            layoutAlgorithm = WebSettings.LayoutAlgorithm.NORMAL
            mediaPlaybackRequiresUserGesture = false
            cacheMode = if (isIncognito) WebSettings.LOAD_NO_CACHE else WebSettings.LOAD_DEFAULT
            javaScriptCanOpenWindowsAutomatically = !settings.blockPopups
            setSupportMultipleWindows(!settings.blockPopups)
            textZoom = if (desktopMode) 100 else settings.textZoomPercent.coerceIn(75, 175)
            userAgentString = if (desktopMode) {
                DESKTOP_USER_AGENT
            } else {
                defaultMobileUserAgent ?: WebSettings.getDefaultUserAgent(context)
            }
        }

        val cookieManager = CookieManager.getInstance()
        cookieManager.setAcceptCookie(!isIncognito && settings.acceptCookies)
        cookieManager.setAcceptThirdPartyCookies(
            webView,
            !isIncognito && settings.acceptCookies && settings.thirdPartyCookies
        )
    }

    fun applySettingsToAll(settings: BrowserSettings, tabs: List<BrowserTab>) {
        tabs.forEach { tab ->
            desktopModeByTab[tab.id] = tab.isDesktopMode
            webViews[tab.id]?.let { wv ->
                applySettingsToWebView(wv, settings, tab.isDesktopMode, tab.isIncognito)
            }
        }
    }

    fun switchDesktopMode(tab: BrowserTab, desktopMode: Boolean, settings: BrowserSettings) {
        desktopModeByTab[tab.id] = desktopMode
        val webView = webViews[tab.id] ?: return
        applySettingsToWebView(webView, settings, desktopMode, tab.isIncognito)
        // Reset scale cleanly so Chromium fits overview width on both desktop & mobile transitions
        webView.setInitialScale(0)
        if (!tab.isHome) {
            val currentUrl = webView.url ?: tab.url
            val targetUrl = normalizeUrlForDesktopMode(currentUrl, desktopMode)
            if (targetUrl.isNotBlank()) {
                webView.loadUrl(targetUrl, buildRequestHeaders(settings, desktopMode))
            } else {
                webView.reload()
            }
        }
    }

    fun loadUrl(tab: BrowserTab, url: String) {
        desktopModeByTab[tab.id] = tab.isDesktopMode
        val webView = getOrCreateWebView(tab)
        val settings = callbacks.getCurrentSettings()
        applySettingsToWebView(webView, settings, tab.isDesktopMode, tab.isIncognito)
        webView.setInitialScale(0)
        val finalUrl = normalizeUrlForDesktopMode(url, tab.isDesktopMode)
        webView.loadUrl(finalUrl, buildRequestHeaders(settings, tab.isDesktopMode))
    }

    private fun injectDesktopViewportFit(webView: WebView) {
        val density = webView.resources.displayMetrics.density.coerceAtLeast(1f)
        val widthDp = (if (webView.width > 0) webView.width / density else 390f).coerceAtLeast(300f)
        val initialScale = (widthDp / DESKTOP_VIEWPORT_WIDTH_PX.toFloat()).coerceIn(0.24f, 1.0f)
        val scaleFormatted = String.format(java.util.Locale.US, "%.3f", initialScale)
        val js = """
            (function() {
                try {
                    var head = document.head || document.getElementsByTagName('head')[0];
                    if (!head) return;
                    var content = 'width=$DESKTOP_VIEWPORT_WIDTH_PX, initial-scale=$scaleFormatted, minimum-scale=0.2, maximum-scale=5.0, user-scalable=yes';
                    var meta = document.querySelector('meta[name="viewport"]');
                    if (meta) {
                        meta.setAttribute('content', content);
                    } else {
                        meta = document.createElement('meta');
                        meta.name = 'viewport';
                        meta.content = content;
                        head.appendChild(meta);
                    }
                    window.dispatchEvent(new Event('resize'));
                } catch (e) {}
            })();
        """.trimIndent()
        webView.evaluateJavascript(js, null)
    }

    private fun buildRequestHeaders(
        settings: BrowserSettings,
        desktopMode: Boolean = false
    ): Map<String, String> {
        val headers = mutableMapOf<String, String>()
        if (settings.doNotTrack) {
            headers["DNT"] = "1"
            headers["Sec-GPC"] = "1"
        }
        if (desktopMode) {
            headers["Sec-CH-UA-Mobile"] = "?0"
            headers["Sec-CH-UA-Platform"] = "\"Windows\""
        }
        return headers
    }

    fun capturePreview(tabId: String) {
        val view = webViews[tabId] ?: return
        capturePreviewAsync(tabId, view)
    }

    private fun capturePreviewAsync(tabId: String, view: WebView) {
        if (view.width <= 0 || view.height <= 0) return
        try {
            val scale = 0.25f
            val targetWidth = (view.width * scale).toInt().coerceIn(120, 360)
            val targetHeight = (view.height * scale).toInt().coerceIn(160, 480)
            val bitmap = Bitmap.createBitmap(targetWidth, targetHeight, Bitmap.Config.RGB_565)
            val canvas = Canvas(bitmap)
            canvas.scale(targetWidth.toFloat() / view.width, targetHeight.toFloat() / view.height)
            view.draw(canvas)
            callbacks.onTabUpdated(tabId) { it.copy(previewBitmap = bitmap) }
        } catch (_: Exception) {
            // Skip preview capture if WebView is not currently laid out
        }
    }

    fun extractVisiblePageText(tabId: String, onResult: (String) -> Unit) {
        val webView = webViews[tabId]
        if (webView == null) {
            onResult("")
            return
        }
        val script = "(function(){return document.body && document.body.innerText ? document.body.innerText.substring(0, 7000) : '';})();"
        webView.evaluateJavascript(script) { raw ->
            val decoded = try {
                if (raw.isNullOrBlank() || raw == "null") "" else JSONTokener(raw).nextValue().toString()
            } catch (_: Exception) {
                raw.orEmpty().trim('"')
            }
            onResult(decoded)
        }
    }

    fun destroyTab(tabId: String, isIncognito: Boolean) {
        desktopModeByTab.remove(tabId)
        val webView = webViews.remove(tabId) ?: return
        (webView.parent as? ViewGroup)?.removeView(webView)
        webView.stopLoading()
        if (isIncognito) {
            webView.clearHistory()
            webView.clearFormData()
            webView.clearCache(true)
        }
        webView.loadUrl("about:blank")
        webView.destroy()
    }

    fun pauseInactiveTabs(activeTabId: String) {
        webViews.forEach { (id, webView) ->
            if (id == activeTabId) {
                webView.onResume()
            } else {
                webView.onPause()
            }
        }
    }

    fun destroyAll() {
        webViews.keys.toList().forEach { id ->
            destroyTab(id, isIncognito = false)
        }
    }

    fun guessFileName(url: String, contentDisposition: String?, mimeType: String?): String {
        return URLUtil.guessFileName(url, contentDisposition, mimeType)
    }
}
