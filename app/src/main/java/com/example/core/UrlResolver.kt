package com.example.core

import android.net.Uri
import android.util.Patterns
import java.net.URLEncoder

enum class SearchEngine(
    val displayName: String,
    val queryTemplate: String,
    val homepage: String
) {
    GOOGLE("Google", "https://www.google.com/search?q=%s", "https://www.google.com"),
    DUCKDUCKGO("DuckDuckGo", "https://duckduckgo.com/?q=%s", "https://duckduckgo.com"),
    BING("Bing", "https://www.bing.com/search?q=%s", "https://www.bing.com"),
    BRAVE("Brave Search", "https://search.brave.com/search?q=%s", "https://search.brave.com"),
    ECOSIA("Ecosia", "https://www.ecosia.org/search?q=%s", "https://www.ecosia.org"),
    STARTPAGE("Startpage", "https://www.startpage.com/do/search?query=%s", "https://www.startpage.com")
}

object UrlResolver {
    const val HOME_URL = "rauf://home"

    private val DOMAIN_REGEX = Regex(
        "^(localhost|([a-zA-Z0-9-]+\\.)+[a-zA-Z]{2,}|(\\d{1,3}\\.){3}\\d{1,3})(:\\d{1,5})?(/.*)?$"
    )

    private val EXTERNAL_SCHEMES = setOf(
        "mailto", "tel", "sms", "smsto", "geo", "market", "intent", "whatsapp", "tg", "upi"
    )

    fun isHomeUrl(url: String?): Boolean = url.isNullOrBlank() || url == HOME_URL || url == "about:blank"

    fun isSecureUrl(url: String?): Boolean = url?.startsWith("https://", ignoreCase = true) == true

    fun isExternalScheme(url: String): Boolean {
        val scheme = Uri.parse(url).scheme?.lowercase() ?: return false
        return scheme !in setOf("http", "https", "about", "rauf", "data", "blob") || scheme in EXTERNAL_SCHEMES
    }

    fun resolveInput(
        rawInput: String,
        searchEngine: SearchEngine,
        httpsOnly: Boolean = true
    ): String {
        val trimmed = rawInput.trim()
        if (trimmed.isEmpty()) return HOME_URL
        if (trimmed.equals(HOME_URL, ignoreCase = true)) return HOME_URL

        // Already has explicit web scheme
        if (trimmed.startsWith("https://", ignoreCase = true)) {
            return trimmed
        }
        if (trimmed.startsWith("http://", ignoreCase = true)) {
            return if (httpsOnly && !trimmed.contains("localhost") && !trimmed.contains("127.0.0.1")) {
                "https://" + trimmed.removePrefix("http://")
            } else {
                trimmed
            }
        }

        // Check if input looks like a domain / IP / URL without scheme (no spaces)
        val hasNoSpaces = !trimmed.contains(" ")
        if (hasNoSpaces && (DOMAIN_REGEX.matches(trimmed) || Patterns.WEB_URL.matcher(trimmed).matches())) {
            val scheme = if (trimmed.startsWith("localhost") || trimmed.startsWith("127.0.0.1")) {
                "http://"
            } else {
                "https://"
            }
            return "$scheme$trimmed"
        }

        // Otherwise build search engine URL
        val encoded = URLEncoder.encode(trimmed, "UTF-8")
        return searchEngine.queryTemplate.format(encoded)
    }

    fun extractHost(url: String?): String {
        if (isHomeUrl(url)) return "rauf.browser"
        return try {
            Uri.parse(url).host?.removePrefix("www.") ?: url.orEmpty()
        } catch (_: Exception) {
            url.orEmpty()
        }
    }

    fun formatBytes(bytes: Long): String {
        if (bytes <= 0L) return "0 B"
        val kb = 1024.0
        val mb = kb * 1024.0
        val gb = mb * 1024.0
        return when {
            bytes >= gb -> String.format("%.2f GB", bytes / gb)
            bytes >= mb -> String.format("%.1f MB", bytes / mb)
            bytes >= kb -> String.format("%.0f KB", bytes / kb)
            else -> "$bytes B"
        }
    }
}
