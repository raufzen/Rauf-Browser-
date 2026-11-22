package com.example.engine

import android.graphics.Bitmap
import android.webkit.GeolocationPermissions
import android.webkit.PermissionRequest
import com.example.core.UrlResolver
import java.util.UUID

data class BrowserTab(
    val id: String = UUID.randomUUID().toString(),
    val url: String = UrlResolver.HOME_URL,
    val title: String = "New Tab",
    val isIncognito: Boolean = false,
    val isLoading: Boolean = false,
    val progress: Int = 0,
    val canGoBack: Boolean = false,
    val canGoForward: Boolean = false,
    val isDesktopMode: Boolean = false,
    val isSecure: Boolean = true,
    val favicon: Bitmap? = null,
    val previewBitmap: Bitmap? = null,
    val errorMessage: String? = null,
    val lastAccessedAt: Long = System.currentTimeMillis()
) {
    val isHome: Boolean get() = UrlResolver.isHomeUrl(url)
}

data class ClosedTabRecord(
    val url: String,
    val title: String,
    val isIncognito: Boolean,
    val isDesktopMode: Boolean
)

data class SslAlertState(
    val tabId: String,
    val url: String,
    val reason: String,
    val issuedTo: String,
    val issuedBy: String
)

data class ExternalLinkPrompt(
    val url: String,
    val scheme: String
)

sealed interface SitePermissionPrompt {
    data class Geolocation(
        val origin: String,
        val callback: GeolocationPermissions.Callback
    ) : SitePermissionPrompt

    data class Media(
        val origin: String,
        val resources: List<String>,
        val request: PermissionRequest
    ) : SitePermissionPrompt
}

data class FindInPageState(
    val isVisible: Boolean = false,
    val query: String = "",
    val activeMatchIndex: Int = 0,
    val totalMatches: Int = 0
)
