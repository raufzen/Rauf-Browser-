package com.example.api

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Log
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.HttpUrl.Companion.toHttpUrlOrNull
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import org.json.JSONTokener
import java.net.URLEncoder
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.TimeUnit

private const val TAG = "NormalizedApiEngine"

enum class NormalizedType { TEXT, IMAGE, VIDEO, AUDIO, FILE, LIST }

enum class HealthDot { GREEN, YELLOW, RED, UNTESTED }

data class ProviderHealthResult(
    val providerId: String,
    val status: HealthDot,
    val latencyMs: Long,
    val detail: String,
    val checkedAt: Long = System.currentTimeMillis()
)

data class NormalizedResponse(
    val ok: Boolean,
    val type: NormalizedType,
    val title: String,
    val text: String,
    val url: String? = null,
    val imageUrl: String? = null,
    val imageBitmap: Bitmap? = null,
    val items: List<ApiParsedItem> = emptyList(),
    val raw: String = "",
    val source: String = "",
    val providerId: String = "",
    val isFallback: Boolean = false,
    val latencyMs: Long = 0L,
    val attemptedProviders: List<String> = emptyList()
)

private data class CacheEntry(
    val response: NormalizedResponse,
    val expiresAt: Long
)

class NormalizedApiEngine {
    private val client = OkHttpClient.Builder()
        .connectTimeout(16, TimeUnit.SECONDS)
        .readTimeout(28, TimeUnit.SECONDS)
        .writeTimeout(16, TimeUnit.SECONDS)
        .followRedirects(true)
        .build()

    private val pingClient = OkHttpClient.Builder()
        .connectTimeout(8, TimeUnit.SECONDS)
        .readTimeout(10, TimeUnit.SECONDS)
        .followRedirects(true)
        .build()

    private val cache = ConcurrentHashMap<String, CacheEntry>()
    private val inFlight = ConcurrentHashMap<String, CompletableDeferred<NormalizedResponse>>()

    fun clearCache() {
        cache.clear()
    }

    suspend fun executeFeature(
        feature: FeatureGroup,
        query: String,
        extraParams: Map<String, String> = emptyMap(),
        apiKeys: Map<String, String> = emptyMap(),
        disabledProviderIds: Set<String> = emptySet(),
        providerPriorityOrder: List<String> = emptyList(),
        startProviderIndex: Int = 0,
        explicitProviderId: String? = null,
        bypassCache: Boolean = false
    ): NormalizedResponse {
        val candidates = if (explicitProviderId != null) {
            listOfNotNull(ProviderRegistry.byId(explicitProviderId))
        } else {
            ProviderRegistry.orderedProvidersFor(feature, disabledProviderIds, providerPriorityOrder)
        }

        if (candidates.isEmpty()) {
            return NormalizedResponse(
                ok = false,
                type = NormalizedType.TEXT,
                title = feature.label,
                text = "All providers for ${feature.label} are disabled. Re-enable them in API Status.",
                source = "None"
            )
        }

        val sliced = if (startProviderIndex > 0 && startProviderIndex < candidates.size) {
            candidates.drop(startProviderIndex) + candidates.take(startProviderIndex)
        } else {
            candidates
        }

        val cacheKey = "${feature.key}|$query|$extraParams|${explicitProviderId ?: startProviderIndex}"
        if (!bypassCache) {
            val cached = cache[cacheKey]
            if (cached != null && cached.expiresAt > System.currentTimeMillis()) {
                return cached.response
            }
        }

        // Request de-duplication for identical in-flight calls
        val existingDeferred = inFlight[cacheKey]
        if (existingDeferred != null && !bypassCache) {
            return existingDeferred.await()
        }

        val deferred = CompletableDeferred<NormalizedResponse>()
        inFlight[cacheKey] = deferred

        return try {
            val attempted = mutableListOf<String>()
            var lastFailureText = "No providers returned a usable response."

            for (provider in sliced) {
                attempted.add(provider.name)
                val res = executeSingleProviderWithRetry(
                    provider = provider,
                    query = query,
                    extraParams = extraParams,
                    apiKeys = apiKeys
                )
                if (res.ok) {
                    val finalized = res.copy(attemptedProviders = attempted.toList())
                    cache[cacheKey] = CacheEntry(finalized, System.currentTimeMillis() + 60_000L)
                    deferred.complete(finalized)
                    return finalized
                } else {
                    lastFailureText = "${provider.name}: ${res.text}"
                }
            }

            val failed = NormalizedResponse(
                ok = false,
                type = NormalizedType.TEXT,
                title = feature.label,
                text = "Tried ${attempted.size} provider(s) without usable output. Last error: $lastFailureText",
                source = attempted.lastOrNull() ?: "None",
                attemptedProviders = attempted
            )
            deferred.complete(failed)
            failed
        } catch (e: CancellationException) {
            deferred.cancel(e)
            throw e
        } catch (e: Exception) {
            val err = NormalizedResponse(
                ok = false,
                type = NormalizedType.TEXT,
                title = feature.label,
                text = e.localizedMessage ?: "Unexpected network error.",
                source = "Client"
            )
            deferred.complete(err)
            err
        } finally {
            inFlight.remove(cacheKey, deferred)
        }
    }

    suspend fun executeSingleProviderWithRetry(
        provider: ProviderEndpoint,
        query: String,
        extraParams: Map<String, String> = emptyMap(),
        apiKeys: Map<String, String> = emptyMap()
    ): NormalizedResponse {
        // Attempt 1 + 1 automatic retry on transient failure
        val first = executeSingleAttempt(provider, query, extraParams, apiKeys)
        if (first.ok) return first
        return executeSingleAttempt(provider, query, extraParams, apiKeys)
    }

    private suspend fun executeSingleAttempt(
        provider: ProviderEndpoint,
        query: String,
        extraParams: Map<String, String>,
        apiKeys: Map<String, String>
    ): NormalizedResponse = withContext(Dispatchers.IO) {
        val startMs = System.currentTimeMillis()
        try {
            // Special handling for Ephoto360 web pages: open directly with preview URL
            if (provider.host == "en.ephoto360.com") {
                return@withContext NormalizedResponse(
                    ok = true,
                    type = NormalizedType.FILE,
                    title = provider.name,
                    text = "Open this Ephoto360 generator directly in Rauf Browser to render \"$query\" with ${provider.name}.",
                    url = provider.fullEndpointUrl,
                    source = "${provider.name} (${provider.host})",
                    providerId = provider.id,
                    isFallback = !provider.isPrimary,
                    latencyMs = System.currentTimeMillis() - startMs
                )
            }

            // Special handling for Groq OpenAI-compatible POST endpoint
            if (provider.id == "groq_openai") {
                val apiKey = apiKeys[AuthKeySlot.GROQ.keyName].orEmpty().trim()
                if (apiKey.isBlank()) {
                    return@withContext fail(provider, "Groq API key not set in Settings", startMs)
                }
                val sysPrompt = extraParams["system"].orEmpty().ifBlank { "You are a helpful AI assistant." }
                val payload = JSONObject().apply {
                    put("model", "llama-3.3-70b-versatile")
                    put(
                        "messages",
                        JSONArray().apply {
                            put(JSONObject().put("role", "system").put("content", sysPrompt))
                            put(JSONObject().put("role", "user").put("content", query))
                        }
                    )
                }.toString()
                val req = Request.Builder()
                    .url(provider.fullEndpointUrl)
                    .post(payload.toRequestBody("application/json; charset=utf-8".toMediaType()))
                    .header("Authorization", "Bearer $apiKey")
                    .build()
                client.newCall(req).execute().use { resp ->
                    val body = resp.body?.string().orEmpty()
                    if (!resp.isSuccessful) return@withContext fail(provider, "HTTP ${resp.code}", startMs)
                    val content = JSONObject(body)
                        .optJSONArray("choices")
                        ?.optJSONObject(0)
                        ?.optJSONObject("message")
                        ?.optString("content")
                        .orEmpty()
                    if (content.isBlank()) return@withContext fail(provider, "Empty Groq completion", startMs)
                    return@withContext NormalizedResponse(
                        ok = true,
                        type = NormalizedType.TEXT,
                        title = provider.name,
                        text = content.trim(),
                        raw = body,
                        source = "${provider.name} (${provider.host})",
                        providerId = provider.id,
                        isFallback = !provider.isPrimary,
                        latencyMs = System.currentTimeMillis() - startMs
                    )
                }
            }

            // Build endpoint URL (handling path templates like {q} or {token})
            val safeQ = query.trim().ifBlank {
                when (provider.feature) {
                    FeatureGroup.CURRENCY -> "USD"
                    FeatureGroup.CRYPTO -> "btc"
                    FeatureGroup.ANIME -> "waifu"
                    else -> ""
                }
            }
            val encodedPathQ = URLEncoder.encode(safeQ, "UTF-8").replace("+", "%20")
            val botToken = apiKeys[AuthKeySlot.TELEGRAM.keyName].orEmpty()
            var resolvedPath = provider.path
                .replace("{q}", encodedPathQ)
                .replace("{token}", botToken)

            val rawUrl = "${provider.baseUrl}$resolvedPath"
            val httpUrlBuilder = rawUrl.toHttpUrlOrNull()?.newBuilder()
                ?: return@withContext fail(provider, "Invalid URL", startMs)

            // Attach static extra params + caller overrides
            val mergedParams = linkedMapOf<String, String>()
            mergedParams.putAll(provider.extraParams)
            extraParams.forEach { (k, v) -> if (k != "system" && v.isNotBlank()) mergedParams[k] = v }

            if (!provider.isPathTemplate && provider.paramKey.isNotBlank() && safeQ.isNotBlank()) {
                mergedParams[provider.paramKey] = safeQ
            }

            // Attach optional API key from Settings if configured (or public default for shizo)
            if (provider.authKeySlot != AuthKeySlot.NONE && provider.authKeySlot != AuthKeySlot.TELEGRAM) {
                val keyVal = apiKeys[provider.authKeySlot.keyName].orEmpty().ifBlank {
                    if (provider.authKeySlot == AuthKeySlot.SHIZO) "shizo" else ""
                }
                if (keyVal.isNotBlank()) {
                    mergedParams[provider.authParamName] = keyVal
                }
            }

            mergedParams.forEach { (k, v) -> httpUrlBuilder.addQueryParameter(k, v) }
            val builtUrl = httpUrlBuilder.build()

            val requestBuilder = Request.Builder()
                .url(builtUrl)
                .header("Accept", "application/json, image/*, text/plain, */*")
                .header("User-Agent", "RaufBrowser/2.0 (Android)")

            val request = if (provider.method.equals("POST", ignoreCase = true)) {
                val formBody = mergedParams.entries.joinToString("&") { (k, v) ->
                    "${URLEncoder.encode(k, "UTF-8")}=${URLEncoder.encode(v, "UTF-8")}"
                }
                requestBuilder
                    .post(formBody.toRequestBody("application/x-www-form-urlencoded".toMediaType()))
                    .build()
            } else {
                requestBuilder.get().build()
            }

            client.newCall(request).execute().use { resp ->
                val latency = System.currentTimeMillis() - startMs
                val contentType = resp.header("Content-Type").orEmpty().lowercase()
                val bytes = resp.body?.bytes() ?: ByteArray(0)

                if (!resp.isSuccessful) {
                    return@withContext fail(provider, "HTTP ${resp.code}", startMs)
                }

                if (contentType.startsWith("image/") || (provider.expectsBinaryImage && bytes.size > 200 && !bytes.startsWithJson())) {
                    val bmp = BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
                    if (bmp != null) {
                        return@withContext NormalizedResponse(
                            ok = true,
                            type = NormalizedType.IMAGE,
                            title = provider.name,
                            text = "Image generated (${bmp.width}×${bmp.height})",
                            url = builtUrl.toString(),
                            imageUrl = builtUrl.toString(),
                            imageBitmap = bmp,
                            source = "${provider.name} (${provider.host})",
                            providerId = provider.id,
                            isFallback = !provider.isPrimary,
                            latencyMs = latency
                        )
                    }
                }

                val bodyText = bytes.toString(Charsets.UTF_8).trim()
                if (bodyText.isBlank()) {
                    return@withContext fail(provider, "Empty response", startMs)
                }

                normalizeBody(provider, bodyText, builtUrl.toString(), latency)
            }
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Log.w(TAG, "Provider ${provider.id} failed: ${e.message}")
            fail(provider, e.localizedMessage ?: "Network error", startMs)
        }
    }

    private fun normalizeBody(
        provider: ProviderEndpoint,
        bodyText: String,
        requestUrl: String,
        latency: Long
    ): NormalizedResponse {
        // Special Google Translate nested array parser
        if (provider.id == "google_translate" && bodyText.startsWith("[")) {
            val translated = runCatching {
                val arr = JSONArray(bodyText).optJSONArray(0)
                buildString {
                    if (arr != null) {
                        for (i in 0 until arr.length()) {
                            append(arr.optJSONArray(i)?.optString(0).orEmpty())
                        }
                    }
                }.trim()
            }.getOrDefault("")
            if (translated.isNotBlank()) {
                return NormalizedResponse(
                    ok = true,
                    type = NormalizedType.TEXT,
                    title = "Translation",
                    text = translated,
                    raw = bodyText,
                    source = "${provider.name} (${provider.host})",
                    providerId = provider.id,
                    isFallback = !provider.isPrimary,
                    latencyMs = latency
                )
            }
        }

        // Plain URL output (e.g., TinyURL)
        if (bodyText.startsWith("http://") || bodyText.startsWith("https://")) {
            val singleLine = bodyText.lineSequence().first().trim()
            return NormalizedResponse(
                ok = true,
                type = if (looksLikeImage(singleLine)) NormalizedType.IMAGE else NormalizedType.TEXT,
                title = provider.name,
                text = singleLine,
                url = singleLine,
                imageUrl = singleLine.takeIf(::looksLikeImage),
                raw = bodyText,
                source = "${provider.name} (${provider.host})",
                providerId = provider.id,
                isFallback = !provider.isPrimary,
                latencyMs = latency
            )
        }

        val parsed = runCatching { JSONTokener(bodyText).nextValue() }.getOrNull()
        if (parsed == null) {
            // HTML or plain text
            val cleanText = if (bodyText.contains("<html", ignoreCase = true)) {
                "Page reachable at $requestUrl"
            } else {
                bodyText.take(2400)
            }
            return NormalizedResponse(
                ok = true,
                type = NormalizedType.TEXT,
                title = provider.name,
                text = cleanText,
                url = requestUrl.takeIf { bodyText.contains("<html", ignoreCase = true) },
                raw = bodyText.take(4000),
                source = "${provider.name} (${provider.host})",
                providerId = provider.id,
                isFallback = !provider.isPrimary,
                latencyMs = latency
            )
        }

        val urls = mutableListOf<String>()
        val texts = mutableListOf<String>()
        val items = mutableListOf<ApiParsedItem>()
        var title = provider.name
        var imageUrl: String? = null

        fun walk(node: Any?, depth: Int) {
            if (node == null || depth > 5) return
            when (node) {
                is JSONObject -> {
                    val statusVal = node.opt("status")
                    if (statusVal == false || statusVal == "error") {
                        val errMsg = node.optString("message").ifBlank { node.optString("error") }
                        if (errMsg.isNotBlank()) texts.add(errMsg)
                    }
                    node.firstNonBlank("title", "name", "word", "author", "song")?.let {
                        if (title == provider.name) title = it
                    }
                    for (k in listOf(
                        "answer", "response", "reply", "joke", "fact", "advice", "quote", "q",
                        "lyrics", "extract", "translatedText", "translated_text", "translation",
                        "text", "result", "output", "message", "description", "synopsis", "content", "shortUrl"
                    )) {
                        val v = node.opt(k)
                        if (v is String && v.isNotBlank() && !v.equals("success", true) && !v.equals("ok", true)) {
                            if (v.startsWith("http://") || v.startsWith("https://")) {
                                urls.add(v)
                                if (looksLikeImage(v) && imageUrl == null) imageUrl = v
                            } else {
                                texts.add(v.trim())
                            }
                        }
                    }
                    for (uKey in listOf(
                        "download", "downloadUrl", "download_url", "dl_link", "video", "hdplay", "play",
                        "wmplay", "audio", "mp3", "mp4", "link", "media", "url", "hd", "sd", "music"
                    )) {
                        val v = node.opt(uKey)
                        if (v is String && (v.startsWith("http://") || v.startsWith("https://"))) {
                            urls.add(v)
                            if (looksLikeImage(v) && imageUrl == null) imageUrl = v
                        }
                    }
                    for (imgKey in listOf("thumbnail", "thumb", "cover", "poster", "image", "imageUrl", "avatar", "icon")) {
                        val v = node.optString(imgKey)
                        if ((v.startsWith("http://") || v.startsWith("https://")) && imageUrl == null) {
                            imageUrl = v
                        }
                    }

                    val keys = node.keys()
                    while (keys.hasNext()) {
                        val k = keys.next()
                        val v = node.opt(k)
                        if (v is JSONObject || v is JSONArray) walk(v, depth + 1)
                    }
                }
                is JSONArray -> {
                    for (i in 0 until node.length().coerceAtMost(40)) {
                        val el = node.opt(i)
                        if (el is JSONObject) {
                            val itemTitle = el.firstNonBlank("title", "name", "question", "definition", "word", "q", "label", "quality", "server")
                            val itemUrl = el.firstNonBlank("url", "link", "download", "downloadUrl", "play", "audio", "video", "html_url")
                            val itemImg = el.firstNonBlank("thumbnail", "thumb", "image", "poster", "cover", "avatar_url")
                            val itemSub = el.firstNonBlank("description", "snippet", "author", "a", "correct_answer", "partOfSpeech", "size", "quality")
                            if (itemTitle != null || itemUrl != null) {
                                items.add(
                                    ApiParsedItem(
                                        title = itemTitle ?: itemUrl ?: "Result ${i + 1}",
                                        subtitle = itemSub ?: itemUrl.orEmpty(),
                                        url = itemUrl?.takeIf { it.startsWith("http") },
                                        imageUrl = itemImg?.takeIf { it.startsWith("http") }
                                    )
                                )
                            }
                            if (itemUrl != null && itemUrl.startsWith("http")) urls.add(itemUrl)
                            if (itemImg != null && itemImg.startsWith("http") && imageUrl == null) imageUrl = itemImg
                        }
                        walk(el, depth + 1)
                    }
                }
            }
        }

        walk(parsed, 0)

        val primaryUrl = urls.firstOrNull { !looksLikeImage(it) } ?: urls.firstOrNull()
        val distinctTexts = texts.distinct()
        val finalText = when {
            distinctTexts.isNotEmpty() -> distinctTexts.take(3).joinToString("\n\n")
            items.isNotEmpty() -> "Loaded ${items.size} results from ${provider.name}."
            primaryUrl != null -> primaryUrl
            parsed is JSONObject -> parsed.toString(2).take(2000)
            else -> bodyText.take(2000)
        }

        val resolvedType = when {
            primaryUrl != null && looksLikeVideo(primaryUrl) -> NormalizedType.VIDEO
            primaryUrl != null && looksLikeAudio(primaryUrl) -> NormalizedType.AUDIO
            imageUrl != null && (provider.feature == FeatureGroup.WALLPAPERS || provider.feature == FeatureGroup.AI_IMAGE || provider.feature == FeatureGroup.EFFECTS || provider.feature == FeatureGroup.QR) -> NormalizedType.IMAGE
            items.size >= 2 -> NormalizedType.LIST
            primaryUrl != null && provider.feature.category == "Downloaders" -> NormalizedType.FILE
            else -> NormalizedType.TEXT
        }

        val usable = finalText.isNotBlank() || primaryUrl != null || imageUrl != null || items.isNotEmpty()
        return NormalizedResponse(
            ok = usable,
            type = resolvedType,
            title = title,
            text = finalText,
            url = primaryUrl,
            imageUrl = imageUrl,
            items = items.distinctBy { "${it.title}|${it.url}" }.take(50),
            raw = when (parsed) {
                is JSONObject -> parsed.toString(2).take(4000)
                is JSONArray -> parsed.toString(2).take(4000)
                else -> bodyText.take(4000)
            },
            source = "${provider.name} (${provider.host})",
            providerId = provider.id,
            isFallback = !provider.isPrimary,
            latencyMs = latency
        )
    }

    suspend fun testProviderHealth(provider: ProviderEndpoint, apiKeys: Map<String, String>): ProviderHealthResult =
        withContext(Dispatchers.IO) {
            val start = System.currentTimeMillis()
            try {
                val probeUrl = provider.baseUrl + provider.path
                    .replace("{q}", "test")
                    .replace("{token}", apiKeys[AuthKeySlot.TELEGRAM.keyName].orEmpty().ifBlank { "test" })
                val request = Request.Builder()
                    .url(probeUrl)
                    .get()
                    .header("User-Agent", "RaufBrowser/2.0 (HealthCheck)")
                    .build()
                pingClient.newCall(request).execute().use { resp ->
                    val latency = System.currentTimeMillis() - start
                    val code = resp.code
                    val dot = when {
                        code in 200..299 && latency < 2500L -> HealthDot.GREEN
                        code in 200..499 -> HealthDot.YELLOW
                        else -> HealthDot.RED
                    }
                    ProviderHealthResult(
                        providerId = provider.id,
                        status = dot,
                        latencyMs = latency,
                        detail = "HTTP $code (${latency}ms)"
                    )
                }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                ProviderHealthResult(
                    providerId = provider.id,
                    status = HealthDot.RED,
                    latencyMs = System.currentTimeMillis() - start,
                    detail = e.javaClass.simpleName + ": " + (e.message?.take(48) ?: "unreachable")
                )
            }
        }

    private fun fail(provider: ProviderEndpoint, reason: String, startMs: Long) = NormalizedResponse(
        ok = false,
        type = NormalizedType.TEXT,
        title = provider.name,
        text = reason,
        source = "${provider.name} (${provider.host})",
        providerId = provider.id,
        isFallback = !provider.isPrimary,
        latencyMs = System.currentTimeMillis() - startMs
    )

    private fun JSONObject.firstNonBlank(vararg keys: String): String? {
        for (k in keys) {
            val v = optString(k)
            if (v.isNotBlank() && v != "null") return v.trim()
        }
        return null
    }

    private fun ByteArray.startsWithJson(): Boolean {
        val first = firstOrNull { it != ' '.code.toByte() && it != '\n'.code.toByte() && it != '\r'.code.toByte() }
        return first == '{'.code.toByte() || first == '['.code.toByte()
    }

    private fun looksLikeImage(url: String): Boolean {
        val l = url.lowercase()
        return l.contains(".png") || l.contains(".jpg") || l.contains(".jpeg") || l.contains(".webp") || l.contains(".gif")
    }

    private fun looksLikeVideo(url: String): Boolean {
        val l = url.lowercase()
        return l.contains(".mp4") || l.contains(".mkv") || l.contains("video") || l.contains("googlevideo")
    }

    private fun looksLikeAudio(url: String): Boolean {
        val l = url.lowercase()
        return l.contains(".mp3") || l.contains(".m4a") || l.contains(".ogg") || l.contains("audio")
    }
}
