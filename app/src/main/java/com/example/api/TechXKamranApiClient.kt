package com.example.api

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Log
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.HttpUrl.Companion.toHttpUrlOrNull
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONArray
import org.json.JSONObject
import org.json.JSONTokener
import java.util.concurrent.TimeUnit

private const val TAG = "TechXKamranApi"

data class ApiParsedItem(
    val title: String,
    val subtitle: String = "",
    val url: String? = null,
    val imageUrl: String? = null,
    val badge: String? = null
)

data class ApiCallResult(
    val endpoint: String,
    val summaryText: String = "",
    val items: List<ApiParsedItem> = emptyList(),
    val imageUrl: String? = null,
    val imageBitmap: Bitmap? = null,
    val rawPrettyJson: String? = null
)

class TechXKamranApiClient {
    private val client = OkHttpClient.Builder()
        .connectTimeout(45, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .writeTimeout(45, TimeUnit.SECONDS)
        .followRedirects(true)
        .build()

    suspend fun get(
        endpoint: String,
        params: Map<String, String> = emptyMap(),
        fallbackParamKeys: List<Map<String, String>> = emptyList()
    ): Result<ApiCallResult> = withContext(Dispatchers.IO) {
        try {
            val attempts = buildList {
                add(params)
                addAll(fallbackParamKeys)
            }

            var lastError: Throwable? = null
            for (attemptParams in attempts) {
                val urlBuilder = endpoint.toHttpUrlOrNull()?.newBuilder()
                    ?: return@withContext Result.failure(IllegalArgumentException("Invalid endpoint URL: $endpoint"))

                attemptParams.forEach { (k, v) ->
                    if (v.isNotBlank()) urlBuilder.addQueryParameter(k, v)
                }

                val builtUrl = urlBuilder.build()
                val request = Request.Builder()
                    .url(builtUrl)
                    .get()
                    .header("Accept", "application/json, image/*, text/plain, */*")
                    .header("User-Agent", "RaufBrowser/1.0 (Android)")
                    .build()

                client.newCall(request).execute().use { response ->
                    val contentType = response.header("Content-Type").orEmpty().lowercase()
                    val bodyBytes = response.body?.bytes() ?: ByteArray(0)

                    if (response.isSuccessful) {
                        if (contentType.startsWith("image/")) {
                            val bmp = BitmapFactory.decodeByteArray(bodyBytes, 0, bodyBytes.size)
                            return@withContext Result.success(
                                ApiCallResult(
                                    endpoint = endpoint,
                                    summaryText = "Generated image received (${bodyBytes.size / 1024} KB)",
                                    imageBitmap = bmp
                                )
                            )
                        }

                        val bodyText = bodyBytes.toString(Charsets.UTF_8).trim()
                        if (bodyText.isEmpty()) {
                            return@withContext Result.failure(IllegalStateException("Endpoint returned an empty response."))
                        }

                        return@withContext Result.success(parseResponseBody(endpoint, bodyText))
                    } else {
                        val errBody = bodyBytes.toString(Charsets.UTF_8).take(260)
                        val detail = extractErrorMessage(errBody) ?: "HTTP ${response.code}"
                        lastError = IllegalStateException(detail)
                        // Retry alternate query parameter schema only on 400/422 validation errors
                        if (response.code != 400 && response.code != 422) {
                            return@withContext Result.failure(lastError!!)
                        }
                    }
                }
            }

            Result.failure(lastError ?: IllegalStateException("Request failed."))
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Log.e(TAG, "API call failed for $endpoint", e)
            Result.failure(e)
        }
    }

    private fun extractErrorMessage(body: String): String? {
        if (body.isBlank()) return null
        return try {
            val obj = JSONObject(body)
            obj.optString("error").takeIf { it.isNotBlank() }
                ?: obj.optString("message").takeIf { it.isNotBlank() }
                ?: obj.optString("msg").takeIf { it.isNotBlank() }
        } catch (_: Exception) {
            body.takeIf { it.length < 180 }
        }
    }

    private fun parseResponseBody(endpoint: String, bodyText: String): ApiCallResult {
        val parsed = runCatching { JSONTokener(bodyText).nextValue() }.getOrNull()
        return when (parsed) {
            is JSONObject -> parseJsonObject(endpoint, parsed)
            is JSONArray -> {
                val items = extractItemsFromArray(parsed)
                ApiCallResult(
                    endpoint = endpoint,
                    summaryText = if (items.isNotEmpty()) "${items.size} items loaded" else parsed.toString(2),
                    items = items,
                    rawPrettyJson = parsed.toString(2)
                )
            }
            else -> ApiCallResult(endpoint = endpoint, summaryText = bodyText)
        }
    }

    private fun parseJsonObject(endpoint: String, root: JSONObject): ApiCallResult {
        val items = mutableListOf<ApiParsedItem>()
        var foundImageUrl: String? = null
        val textCandidates = mutableListOf<String>()

        fun inspectObject(obj: JSONObject, depth: Int) {
            if (depth > 4) return

            // Direct text response keys commonly used across TechXKamran endpoints
            val primaryTextKeys = listOf(
                "answer", "response", "reply", "output", "translated_text", "translation",
                "shortUrl", "short_url", "shortened", "text", "decoded", "result", "data",
                "message", "content", "description", "synopsis"
            )
            for (key in primaryTextKeys) {
                val value = obj.opt(key)
                when (value) {
                    is String -> {
                        val trimmed = value.trim()
                        if (trimmed.isNotBlank()) {
                            if (looksLikeImageUrl(trimmed) && foundImageUrl == null) {
                                foundImageUrl = trimmed
                            } else if (trimmed !in textCandidates && key != "message" || textCandidates.isEmpty()) {
                                if (key == "message" && trimmed.equals("success", ignoreCase = true)) continue
                                textCandidates.add(trimmed)
                            }
                        }
                    }
                    is JSONObject -> inspectObject(value, depth + 1)
                    is JSONArray -> {
                        if (items.isEmpty()) {
                            items.addAll(extractItemsFromArray(value))
                        }
                    }
                }
            }

            // Image keys
            for (imgKey in listOf("image", "imageUrl", "image_url", "img", "thumbnail", "poster", "url")) {
                val str = obj.optString(imgKey)
                if (str.isNotBlank() && looksLikeImageUrl(str) && foundImageUrl == null) {
                    foundImageUrl = str
                }
            }

            // Check any remaining array fields (e.g. animeList, episodes, downloads, subdomains, rates, currencies, voices)
            val keys = obj.keys()
            while (keys.hasNext()) {
                val k = keys.next()
                val v = obj.opt(k)
                when {
                    v is JSONArray && items.isEmpty() -> {
                        val extracted = extractItemsFromArray(v)
                        if (extracted.isNotEmpty()) items.addAll(extracted)
                    }
                    v is JSONObject && (k.equals("rates", true) || k.equals("currencies", true) || k.equals("downloads", true)) -> {
                        val mapItems = extractItemsFromMapObject(v)
                        if (mapItems.isNotEmpty()) items.addAll(mapItems)
                    }
                }
            }
        }

        inspectObject(root, 0)

        // Format key-value pairs cleanly when no nested text/list matched (e.g. currency convert or crypto price)
        val summary = when {
            textCandidates.isNotEmpty() -> textCandidates.first()
            items.isNotEmpty() -> "Found ${items.size} results"
            else -> formatFlatJson(root)
        }

        return ApiCallResult(
            endpoint = endpoint,
            summaryText = summary,
            items = items.take(80),
            imageUrl = foundImageUrl,
            rawPrettyJson = root.toString(2)
        )
    }

    private fun extractItemsFromArray(array: JSONArray): List<ApiParsedItem> {
        val result = mutableListOf<ApiParsedItem>()
        for (i in 0 until array.length().coerceAtMost(80)) {
            val elem = array.opt(i)
            when (elem) {
                is String -> {
                    val trimmed = elem.trim()
                    if (trimmed.isNotBlank()) {
                        result.add(
                            ApiParsedItem(
                                title = trimmed,
                                url = if (trimmed.startsWith("http://") || trimmed.startsWith("https://")) trimmed else null
                            )
                        )
                    }
                }
                is JSONObject -> {
                    val title = elem.firstNonBlank("title", "name", "anime", "episode", "label", "voice", "domain", "subdomain", "quality", "server", "symbol", "code")
                        ?: elem.firstNonBlank("url", "link", "href")
                        ?: "Item ${i + 1}"
                    val url = elem.firstNonBlank("url", "link", "href", "download", "downloadUrl", "download_url", "episodeUrl", "slug")
                    val image = elem.firstNonBlank("thumbnail", "thumb", "image", "poster", "img", "icon", "cover")
                    val badge = elem.firstNonBlank("episode", "status", "rating", "score", "quality", "size", "type", "version")
                    val subtitleParts = listOfNotNull(
                        elem.firstNonBlank("release", "date", "updated", "genre", "developer", "description", "price", "rate", "label"),
                        url?.takeIf { it != title }
                    ).distinct()

                    result.add(
                        ApiParsedItem(
                            title = title,
                            subtitle = subtitleParts.joinToString(" • ").take(140),
                            url = url?.takeIf { it.startsWith("http://") || it.startsWith("https://") },
                            imageUrl = image?.takeIf { it.startsWith("http://") || it.startsWith("https://") },
                            badge = badge?.takeIf { it != title }?.take(24)
                        )
                    )
                }
            }
        }
        return result
    }

    private fun extractItemsFromMapObject(obj: JSONObject): List<ApiParsedItem> {
        val result = mutableListOf<ApiParsedItem>()
        val keys = obj.keys()
        while (keys.hasNext() && result.size < 80) {
            val k = keys.next()
            val v = obj.opt(k)?.toString().orEmpty()
            result.add(
                ApiParsedItem(
                    title = k,
                    subtitle = v,
                    url = v.takeIf { it.startsWith("http://") || it.startsWith("https://") }
                )
            )
        }
        return result
    }

    private fun formatFlatJson(obj: JSONObject): String {
        val sb = StringBuilder()
        val keys = obj.keys()
        while (keys.hasNext()) {
            val k = keys.next()
            if (k.equals("status", true) || k.equals("creator", true) || k.equals("author", true)) continue
            val v = obj.opt(k)
            if (v != null && v != JSONObject.NULL) {
                val valueStr = when (v) {
                    is JSONObject -> v.toString(2)
                    is JSONArray -> v.toString(2)
                    else -> v.toString()
                }
                sb.appendLine("${k.replaceFirstChar { it.uppercase() }}: $valueStr")
            }
        }
        return sb.toString().trim().ifBlank { obj.toString(2) }
    }

    private fun JSONObject.firstNonBlank(vararg keys: String): String? {
        for (k in keys) {
            val v = optString(k)
            if (v.isNotBlank() && v != "null") return v.trim()
        }
        return null
    }

    private fun looksLikeImageUrl(url: String): Boolean {
        val lower = url.lowercase()
        return (lower.startsWith("http://") || lower.startsWith("https://")) &&
            (lower.contains(".png") || lower.contains(".jpg") || lower.contains(".jpeg") ||
                lower.contains(".webp") || lower.contains("image") || lower.contains("flux"))
    }
}
