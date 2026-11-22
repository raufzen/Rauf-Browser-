package com.example

import com.example.ai.AiCommandAliases
import com.example.api.FeatureGroup
import com.example.api.ProviderRegistry
import com.example.ui.theme.ThemeMode
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class ProviderFailoverTest {

    @Test
    fun primaryProvidersAlwaysPrecededFallbacksByDefault() {
        val featuresWithFallbacks = listOf(
            FeatureGroup.AI_CHAT,
            FeatureGroup.AI_IMAGE,
            FeatureGroup.AIO,
            FeatureGroup.APK,
            FeatureGroup.ANIME,
            FeatureGroup.TRANSLATE,
            FeatureGroup.TOOLS
        )
        for (feature in featuresWithFallbacks) {
            val ordered = ProviderRegistry.orderedProvidersFor(feature)
            assertTrue("Expected providers for ${feature.name}", ordered.isNotEmpty())
            val firstFallbackIndex = ordered.indexOfFirst { !it.isPrimary }
            val lastPrimaryIndex = ordered.indexOfLast { it.isPrimary }
            if (firstFallbackIndex >= 0 && lastPrimaryIndex >= 0) {
                assertTrue(
                    "Primary must come before fallback for ${feature.name}",
                    lastPrimaryIndex < firstFallbackIndex
                )
            }
        }
    }

    @Test
    fun disablingPrimaryProvidersFallsOverToFallbackProviders() {
        val allAi = ProviderRegistry.orderedProvidersFor(FeatureGroup.AI_CHAT)
        val primaryIds = allAi.filter { it.isPrimary }.map { it.id }.toSet()
        assertTrue(primaryIds.isNotEmpty())

        val afterDisable = ProviderRegistry.orderedProvidersFor(
            feature = FeatureGroup.AI_CHAT,
            disabledIds = primaryIds
        )
        assertTrue("Fallback AI providers should remain available", afterDisable.isNotEmpty())
        assertFalse("First remaining AI provider should be a fallback", afterDisable.first().isPrimary)
        assertEquals("techxkamran.onrender.com", afterDisable.first().host)
    }

    @Test
    fun customPriorityOrderOverridesDefaultOrdering() {
        val customOrder = listOf("techx_gemini", "eliteprotech_chatgpt")
        val ordered = ProviderRegistry.orderedProvidersFor(
            feature = FeatureGroup.AI_CHAT,
            customOrder = customOrder
        )
        assertEquals("techx_gemini", ordered.first().id)
        assertEquals("eliteprotech_chatgpt", ordered[1].id)
    }

    @Test
    fun downloaderAutoDetectsAllSupportedPlatforms() {
        assertEquals(FeatureGroup.YTDL, ProviderRegistry.detectDownloaderFeature("https://youtube.com/watch?v=abc", false))
        assertEquals(FeatureGroup.YTMP3, ProviderRegistry.detectDownloaderFeature("https://youtu.be/abc", true))
        assertEquals(FeatureGroup.IGDL, ProviderRegistry.detectDownloaderFeature("https://www.instagram.com/reel/123"))
        assertEquals(FeatureGroup.FBDL, ProviderRegistry.detectDownloaderFeature("https://fb.watch/xyz"))
        assertEquals(FeatureGroup.TTDL, ProviderRegistry.detectDownloaderFeature("https://vt.tiktok.com/abc"))
        assertEquals(FeatureGroup.TWITTER, ProviderRegistry.detectDownloaderFeature("https://x.com/user/status/123"))
        assertEquals(FeatureGroup.PINTEREST, ProviderRegistry.detectDownloaderFeature("https://pin.it/abc"))
        assertEquals(FeatureGroup.SPOTIFY, ProviderRegistry.detectDownloaderFeature("https://open.spotify.com/track/123"))
        assertEquals(FeatureGroup.MEDIAFIRE, ProviderRegistry.detectDownloaderFeature("https://www.mediafire.com/file/123"))
        assertEquals(FeatureGroup.CAPCUT, ProviderRegistry.detectDownloaderFeature("https://www.capcut.com/t/123"))
        assertEquals(FeatureGroup.TERABOX, ProviderRegistry.detectDownloaderFeature("https://terabox.com/s/123"))
        assertEquals(FeatureGroup.APK, ProviderRegistry.detectDownloaderFeature("WhatsApp Messenger"))
        assertEquals(FeatureGroup.AIO, ProviderRegistry.detectDownloaderFeature("https://vimeo.com/123456"))
    }

    @Test
    fun aiCommandAliasesParseCleanly() {
        assertEquals("hello world", AiCommandAliases.extractPromptOrNull("/ai hello world"))
        assertEquals("explain ssl", AiCommandAliases.extractPromptOrNull("/ask explain ssl"))
        assertEquals("write kotlin", AiCommandAliases.extractPromptOrNull("/chat write kotlin"))
        assertEquals("summarize", AiCommandAliases.extractPromptOrNull("/gpt summarize"))
        assertEquals("hi there", AiCommandAliases.extractPromptOrNull("/gemini hi there"))
        assertEquals("quick test", AiCommandAliases.extractPromptOrNull("ai: quick test"))
        assertNull(AiCommandAliases.extractPromptOrNull("https://google.com"))
        assertNull(AiCommandAliases.extractPromptOrNull("android compose tutorial"))
    }

    @Test
    fun registryIncludesAll37EphotoEffectsThemesAndFlagshipAiModels() {
        assertEquals(37, ProviderRegistry.EPHOTO_EFFECTS.size)
        assertEquals(6, ThemeMode.entries.size)
        assertTrue(ProviderRegistry.ALL_PROVIDERS.size >= 150)

        val defaultModel = ProviderRegistry.AI_MODELS.first()
        assertEquals("chatgpt_gpt6", defaultModel.id)
        assertEquals("ChatGPT", defaultModel.brandName)
        assertEquals("GPT-6", defaultModel.name)
        assertTrue(ProviderRegistry.AI_MODELS.any { it.name == "GPT-6 Extra" })
    }

    @Test
    fun desktopModeNormalizesMobileSubdomains() {
        assertEquals(
            "https://www.youtube.com/watch?v=123",
            com.example.engine.WebViewEngineManager.normalizeUrlForDesktopMode("https://m.youtube.com/watch?v=123", true)
        )
        assertEquals(
            "https://en.wikipedia.org/wiki/Android",
            com.example.engine.WebViewEngineManager.normalizeUrlForDesktopMode("https://en.m.wikipedia.org/wiki/Android", true)
        )
        assertEquals(
            "https://m.youtube.com/watch?v=123",
            com.example.engine.WebViewEngineManager.normalizeUrlForDesktopMode("https://m.youtube.com/watch?v=123", false)
        )
    }
}
