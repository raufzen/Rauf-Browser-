package com.example.data.local

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.floatPreferencesKey
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.example.core.SearchEngine
import com.example.ui.theme.ThemeMode
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import org.json.JSONArray
import org.json.JSONObject

private val Context.browserDataStore: DataStore<Preferences> by preferencesDataStore(
    name = "rauf_browser_settings"
)

enum class HomepageWallpaper(val displayName: String) {
    AMOLED_PURE("Clean Minimal"),
    NEBULA("Liquid Nebula"),
    PRISM("Obsidian Prism"),
    FROST("Porcelain Frost"),
    GRADIENT_VIOLET("Velvet Ultraviolet")
}

data class BrowserSettings(
    val themeMode: ThemeMode = ThemeMode.AMOLED_BLACK,
    val accentColorHex: Long = 0L,
    val fontScale: Float = 1.0f,
    val reducedMotion: Boolean = false,
    val searchEngine: SearchEngine = SearchEngine.GOOGLE,
    val wallpaper: HomepageWallpaper = HomepageWallpaper.AMOLED_PURE,
    val customHomepageUrl: String = "",
    val showQuickShortcuts: Boolean = true,
    val showFrequentSites: Boolean = true,
    val showRecentSites: Boolean = true,
    val showBookmarksOnHome: Boolean = true,
    val showDateOnHome: Boolean = false,
    val javaScriptEnabled: Boolean = true,
    val blockPopups: Boolean = true,
    val httpsOnlyMode: Boolean = true,
    val acceptCookies: Boolean = true,
    val thirdPartyCookies: Boolean = false,
    val doNotTrack: Boolean = true,
    val safeExternalLinks: Boolean = true,
    val allowLocation: Boolean = false,
    val allowCamera: Boolean = false,
    val allowMicrophone: Boolean = false,
    val languageCode: String = "en",
    val notificationsEnabled: Boolean = true,
    val restoreTabsOnStartup: Boolean = true,
    val textZoomPercent: Int = 100,
    val lastAiModelId: String = "chatgpt_gpt6",
    val aiSystemPrompt: String = "You are Rauf.ai powered by ChatGPT, a clean, fast, accurate assistant inside Rauf Browser.",
    val apiKeys: Map<String, String> = emptyMap(),
    val disabledProviderIds: Set<String> = emptySet(),
    val providerPriorityOrder: List<String> = emptyList(),
    val favoriteToolIds: Set<String> = setOf("ytdl", "igdl", "ttdl", "weather", "translate", "qr")
)

class BrowserPreferences(private val context: Context) {
    private object Keys {
        val THEME_MODE = stringPreferencesKey("theme_mode")
        val ACCENT_COLOR_HEX = longPreferencesKey("accent_color_hex")
        val FONT_SCALE = floatPreferencesKey("font_scale")
        val REDUCED_MOTION = booleanPreferencesKey("reduced_motion")
        val SEARCH_ENGINE = stringPreferencesKey("search_engine")
        val WALLPAPER = stringPreferencesKey("wallpaper")
        val CUSTOM_HOMEPAGE_URL = stringPreferencesKey("custom_homepage_url")
        val SHOW_SHORTCUTS = booleanPreferencesKey("show_shortcuts")
        val SHOW_FREQUENT = booleanPreferencesKey("show_frequent")
        val SHOW_RECENT = booleanPreferencesKey("show_recent")
        val SHOW_BOOKMARKS = booleanPreferencesKey("show_bookmarks")
        val SHOW_DATE = booleanPreferencesKey("show_date")
        val JS_ENABLED = booleanPreferencesKey("js_enabled")
        val BLOCK_POPUPS = booleanPreferencesKey("block_popups")
        val HTTPS_ONLY = booleanPreferencesKey("https_only")
        val ACCEPT_COOKIES = booleanPreferencesKey("accept_cookies")
        val THIRD_PARTY_COOKIES = booleanPreferencesKey("third_party_cookies")
        val DO_NOT_TRACK = booleanPreferencesKey("do_not_track")
        val SAFE_EXTERNAL_LINKS = booleanPreferencesKey("safe_external_links")
        val ALLOW_LOCATION = booleanPreferencesKey("allow_location")
        val ALLOW_CAMERA = booleanPreferencesKey("allow_camera")
        val ALLOW_MICROPHONE = booleanPreferencesKey("allow_microphone")
        val LANGUAGE_CODE = stringPreferencesKey("language_code")
        val NOTIFICATIONS_ENABLED = booleanPreferencesKey("notifications_enabled")
        val RESTORE_TABS = booleanPreferencesKey("restore_tabs")
        val TEXT_ZOOM = intPreferencesKey("text_zoom")
        val LAST_AI_MODEL = stringPreferencesKey("last_ai_model")
        val AI_SYSTEM_PROMPT = stringPreferencesKey("ai_system_prompt")
        val API_KEYS_JSON = stringPreferencesKey("api_keys_json")
        val DISABLED_PROVIDERS_JSON = stringPreferencesKey("disabled_providers_json")
        val PROVIDER_ORDER_JSON = stringPreferencesKey("provider_order_json")
        val FAVORITE_TOOLS_JSON = stringPreferencesKey("favorite_tools_json")
    }

    val settingsFlow: Flow<BrowserSettings> = context.browserDataStore.data.map(::decodePrefs)

    suspend fun getSettingsSnapshot(): BrowserSettings = settingsFlow.first()

    private fun decodePrefs(prefs: Preferences): BrowserSettings {
        val defaultFavs = setOf("ytdl", "igdl", "ttdl", "weather", "translate", "qr")
        return BrowserSettings(
            themeMode = prefs[Keys.THEME_MODE]?.let { runCatching { ThemeMode.valueOf(it) }.getOrNull() }
                ?: ThemeMode.AMOLED_BLACK,
            accentColorHex = prefs[Keys.ACCENT_COLOR_HEX] ?: 0L,
            fontScale = prefs[Keys.FONT_SCALE] ?: 1.0f,
            reducedMotion = prefs[Keys.REDUCED_MOTION] ?: false,
            searchEngine = prefs[Keys.SEARCH_ENGINE]?.let { runCatching { SearchEngine.valueOf(it) }.getOrNull() }
                ?: SearchEngine.GOOGLE,
            wallpaper = prefs[Keys.WALLPAPER]?.let { runCatching { HomepageWallpaper.valueOf(it) }.getOrNull() }
                ?: HomepageWallpaper.AMOLED_PURE,
            customHomepageUrl = prefs[Keys.CUSTOM_HOMEPAGE_URL] ?: "",
            showQuickShortcuts = prefs[Keys.SHOW_SHORTCUTS] ?: true,
            showFrequentSites = prefs[Keys.SHOW_FREQUENT] ?: true,
            showRecentSites = prefs[Keys.SHOW_RECENT] ?: true,
            showBookmarksOnHome = prefs[Keys.SHOW_BOOKMARKS] ?: true,
            showDateOnHome = prefs[Keys.SHOW_DATE] ?: false,
            javaScriptEnabled = prefs[Keys.JS_ENABLED] ?: true,
            blockPopups = prefs[Keys.BLOCK_POPUPS] ?: true,
            httpsOnlyMode = prefs[Keys.HTTPS_ONLY] ?: true,
            acceptCookies = prefs[Keys.ACCEPT_COOKIES] ?: true,
            thirdPartyCookies = prefs[Keys.THIRD_PARTY_COOKIES] ?: false,
            doNotTrack = prefs[Keys.DO_NOT_TRACK] ?: true,
            safeExternalLinks = prefs[Keys.SAFE_EXTERNAL_LINKS] ?: true,
            allowLocation = prefs[Keys.ALLOW_LOCATION] ?: false,
            allowCamera = prefs[Keys.ALLOW_CAMERA] ?: false,
            allowMicrophone = prefs[Keys.ALLOW_MICROPHONE] ?: false,
            languageCode = prefs[Keys.LANGUAGE_CODE] ?: "en",
            notificationsEnabled = prefs[Keys.NOTIFICATIONS_ENABLED] ?: true,
            restoreTabsOnStartup = prefs[Keys.RESTORE_TABS] ?: true,
            textZoomPercent = prefs[Keys.TEXT_ZOOM] ?: 100,
            lastAiModelId = prefs[Keys.LAST_AI_MODEL] ?: "chatgpt_gpt6",
            aiSystemPrompt = prefs[Keys.AI_SYSTEM_PROMPT]
                ?: "You are Rauf.ai powered by ChatGPT, a clean, fast, accurate assistant inside Rauf Browser.",
            apiKeys = decodeStringMap(prefs[Keys.API_KEYS_JSON]),
            disabledProviderIds = decodeStringList(prefs[Keys.DISABLED_PROVIDERS_JSON]).toSet(),
            providerPriorityOrder = decodeStringList(prefs[Keys.PROVIDER_ORDER_JSON]),
            favoriteToolIds = prefs[Keys.FAVORITE_TOOLS_JSON]?.let { decodeStringList(it).toSet() } ?: defaultFavs
        )
    }

    suspend fun updateSettings(transform: (BrowserSettings) -> BrowserSettings) {
        context.browserDataStore.edit { prefs ->
            val updated = transform(decodePrefs(prefs))
            prefs[Keys.THEME_MODE] = updated.themeMode.name
            prefs[Keys.ACCENT_COLOR_HEX] = updated.accentColorHex
            prefs[Keys.FONT_SCALE] = updated.fontScale.coerceIn(0.85f, 1.25f)
            prefs[Keys.REDUCED_MOTION] = updated.reducedMotion
            prefs[Keys.SEARCH_ENGINE] = updated.searchEngine.name
            prefs[Keys.WALLPAPER] = updated.wallpaper.name
            prefs[Keys.CUSTOM_HOMEPAGE_URL] = updated.customHomepageUrl
            prefs[Keys.SHOW_SHORTCUTS] = updated.showQuickShortcuts
            prefs[Keys.SHOW_FREQUENT] = updated.showFrequentSites
            prefs[Keys.SHOW_RECENT] = updated.showRecentSites
            prefs[Keys.SHOW_BOOKMARKS] = updated.showBookmarksOnHome
            prefs[Keys.SHOW_DATE] = updated.showDateOnHome
            prefs[Keys.JS_ENABLED] = updated.javaScriptEnabled
            prefs[Keys.BLOCK_POPUPS] = updated.blockPopups
            prefs[Keys.HTTPS_ONLY] = updated.httpsOnlyMode
            prefs[Keys.ACCEPT_COOKIES] = updated.acceptCookies
            prefs[Keys.THIRD_PARTY_COOKIES] = updated.thirdPartyCookies
            prefs[Keys.DO_NOT_TRACK] = updated.doNotTrack
            prefs[Keys.SAFE_EXTERNAL_LINKS] = updated.safeExternalLinks
            prefs[Keys.ALLOW_LOCATION] = updated.allowLocation
            prefs[Keys.ALLOW_CAMERA] = updated.allowCamera
            prefs[Keys.ALLOW_MICROPHONE] = updated.allowMicrophone
            prefs[Keys.LANGUAGE_CODE] = updated.languageCode
            prefs[Keys.NOTIFICATIONS_ENABLED] = updated.notificationsEnabled
            prefs[Keys.RESTORE_TABS] = updated.restoreTabsOnStartup
            prefs[Keys.TEXT_ZOOM] = updated.textZoomPercent
            prefs[Keys.LAST_AI_MODEL] = updated.lastAiModelId
            prefs[Keys.AI_SYSTEM_PROMPT] = updated.aiSystemPrompt
            prefs[Keys.API_KEYS_JSON] = encodeStringMap(updated.apiKeys)
            prefs[Keys.DISABLED_PROVIDERS_JSON] = encodeStringList(updated.disabledProviderIds.toList())
            prefs[Keys.PROVIDER_ORDER_JSON] = encodeStringList(updated.providerPriorityOrder)
            prefs[Keys.FAVORITE_TOOLS_JSON] = encodeStringList(updated.favoriteToolIds.toList())
        }
    }

    private fun decodeStringMap(raw: String?): Map<String, String> {
        if (raw.isNullOrBlank()) return emptyMap()
        return runCatching {
            val obj = JSONObject(raw)
            val map = linkedMapOf<String, String>()
            val keys = obj.keys()
            while (keys.hasNext()) {
                val k = keys.next()
                map[k] = obj.optString(k)
            }
            map
        }.getOrDefault(emptyMap())
    }

    private fun encodeStringMap(map: Map<String, String>): String {
        val obj = JSONObject()
        map.forEach { (k, v) -> if (v.isNotBlank()) obj.put(k, v) }
        return obj.toString()
    }

    private fun decodeStringList(raw: String?): List<String> {
        if (raw.isNullOrBlank()) return emptyList()
        return runCatching {
            val arr = JSONArray(raw)
            (0 until arr.length()).map { arr.optString(it) }.filter { it.isNotBlank() }
        }.getOrDefault(emptyList())
    }

    private fun encodeStringList(list: List<String>): String {
        val arr = JSONArray()
        list.forEach { arr.put(it) }
        return arr.toString()
    }
}
