package com.example.ai

import android.graphics.Bitmap
import com.example.api.AiModelSpec
import com.example.api.FeatureGroup
import com.example.api.NormalizedApiEngine
import com.example.api.ProviderEndpoint
import com.example.api.ProviderRegistry
import kotlinx.coroutines.CancellationException

data class GroundingSource(
    val title: String,
    val url: String
)

data class AiQueryOutcome(
    val text: String,
    val imageUrl: String? = null,
    val imageBitmap: Bitmap? = null,
    val sources: List<GroundingSource> = emptyList(),
    val sourceLabel: String = "",
    val providerId: String = "",
    val isFallback: Boolean = false,
    val latencyMs: Long = 0L,
    val attemptedProviders: List<String> = emptyList(),
    val isError: Boolean = false
)

object AiCommandAliases {
    val ALIASES = listOf("ai", "ask", "chat", "gpt", "gemini")

    /**
     * Parses unified command prefixes such as "/ai ...", "/ask ...", "/chat ...", "/gpt ...", "/gemini ..."
     * or "ai: ...". Returns the prompt body if matched, or null for normal browser URL/search inputs.
     */
    fun extractPromptOrNull(rawInput: String): String? {
        val trimmed = rawInput.trim()
        if (trimmed.isEmpty()) return null
        val lower = trimmed.lowercase()
        for (alias in ALIASES) {
            val slashPrefix = "/$alias "
            val colonPrefix = "$alias: "
            when {
                lower == "/$alias" -> return ""
                lower.startsWith(slashPrefix) -> return trimmed.substring(slashPrefix.length).trim()
                lower.startsWith(colonPrefix) -> return trimmed.substring(colonPrefix.length).trim()
            }
        }
        return null
    }
}

class BrowserAiService(
    private val engine: NormalizedApiEngine = NormalizedApiEngine()
) {
    fun resolveModel(modelId: String): AiModelSpec {
        return ProviderRegistry.AI_MODELS.firstOrNull { it.id == modelId }
            ?: ProviderRegistry.AI_MODELS.first()
    }

    suspend fun ask(
        selectedModelId: String,
        userPrompt: String,
        systemPrompt: String = "",
        includePageContext: Boolean = false,
        pageTitle: String? = null,
        pageUrl: String? = null,
        pageSnippet: String? = null,
        apiKeys: Map<String, String> = emptyMap(),
        disabledProviderIds: Set<String> = emptySet(),
        providerPriorityOrder: List<String> = emptyList(),
        providerOffset: Int = 0
    ): AiQueryOutcome {
        val selectedModel = resolveModel(selectedModelId)
        val targetFeature = if (selectedModel.isImageModel) FeatureGroup.AI_IMAGE else FeatureGroup.AI_CHAT

        val orderedEndpoints = ProviderRegistry.orderedProvidersFor(
            feature = targetFeature,
            disabledIds = disabledProviderIds,
            customOrder = providerPriorityOrder
        )

        val selectedEndpoint = ProviderRegistry.byId(selectedModel.providerEndpointId)
        val baseChain: List<ProviderEndpoint> = buildList {
            if (selectedEndpoint != null && selectedEndpoint.id !in disabledProviderIds) {
                add(selectedEndpoint)
            }
            orderedEndpoints.forEach { ep ->
                if (none { it.id == ep.id }) add(ep)
            }
        }

        if (baseChain.isEmpty()) {
            return AiQueryOutcome(
                text = "All AI providers are disabled. Enable at least one AI provider in API Status.",
                sourceLabel = "None",
                isError = true
            )
        }

        val rotatedChain = if (providerOffset > 0) {
            val shift = providerOffset % baseChain.size
            baseChain.drop(shift) + baseChain.take(shift)
        } else {
            baseChain
        }

        val composedPrompt = buildString {
            if (!selectedModel.isImageModel && systemPrompt.isNotBlank() && rotatedChain.first().id != "groq_openai") {
                appendLine("System instruction: ${systemPrompt.trim()}")
            }
            if (includePageContext && !selectedModel.isImageModel &&
                !pageUrl.isNullOrBlank() && !pageUrl.startsWith("rauf://")
            ) {
                appendLine("Current webpage: ${pageTitle.orEmpty()} ($pageUrl)")
                if (!pageSnippet.isNullOrBlank()) {
                    appendLine("Page excerpt: ${pageSnippet.take(1500)}")
                }
                append("User request: ")
            }
            append(userPrompt.trim())
        }

        val extraParams = if (systemPrompt.isNotBlank()) mapOf("system" to systemPrompt.trim()) else emptyMap()
        val attempted = mutableListOf<String>()
        var lastError = "No AI provider responded."

        return try {
            for (provider in rotatedChain) {
                attempted.add(provider.name)
                val response = engine.executeSingleProviderWithRetry(
                    provider = provider,
                    query = composedPrompt,
                    extraParams = extraParams,
                    apiKeys = apiKeys
                )
                if (response.ok && (response.text.isNotBlank() || response.imageUrl != null || response.imageBitmap != null)) {
                    val links = response.items.mapNotNull { item ->
                        item.url?.let { GroundingSource(title = item.title, url = it) }
                    }
                    return AiQueryOutcome(
                        text = response.text.ifBlank { response.raw.ifBlank { "Done." } },
                        imageUrl = response.imageUrl,
                        imageBitmap = response.imageBitmap,
                        sources = links,
                        sourceLabel = response.source,
                        providerId = response.providerId,
                        isFallback = response.isFallback,
                        latencyMs = response.latencyMs,
                        attemptedProviders = attempted.toList(),
                        isError = false
                    )
                } else {
                    lastError = "${provider.name}: ${response.text}"
                }
            }

            AiQueryOutcome(
                text = "All ${attempted.size} AI provider(s) failed. Last error: $lastError",
                sourceLabel = attempted.lastOrNull() ?: "None",
                attemptedProviders = attempted,
                isError = true
            )
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            AiQueryOutcome(
                text = "AI request error: ${e.localizedMessage ?: "Network unavailable"}",
                sourceLabel = "Client",
                attemptedProviders = attempted,
                isError = true
            )
        }
    }
}
