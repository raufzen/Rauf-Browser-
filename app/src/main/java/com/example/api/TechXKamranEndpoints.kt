package com.example.api

object TechXKamranEndpoints {
    const val BASE_URL = "https://techxkamran.onrender.com"

    // AI Endpoints
    const val AI_GEMINI = "$BASE_URL/api/ai/gemini"
    const val AI_GEMINI_LITE = "$BASE_URL/api/ai/gemini-lite"
    const val AI_POWERBRAIN = "$BASE_URL/api/ai/powerbrainai"
    const val AI_MUSLIM = "$BASE_URL/api/ai/muslimai"
    const val AI_GITA = "$BASE_URL/api/ai/gita"
    const val AI_BIBLE = "$BASE_URL/api/ai/bibleai"
    const val AI_BARD = "$BASE_URL/api/ai/bard"
    const val AI_FLUX = "$BASE_URL/api/ai/flux"

    // Cloudflare AI Endpoints
    const val CF_CHAT = "$BASE_URL/api/cf/chat"
    const val CF_TRANSLATION = "$BASE_URL/api/cf/translation"
    const val CF_SENTIMENT = "$BASE_URL/api/cf/sentiment"
    const val CF_IMAGE_CLASSIFICATION = "$BASE_URL/api/cf/image-classification"
    const val CF_EMBEDDING = "$BASE_URL/api/cf/embedding"

    // Currency & Crypto Endpoints
    const val CURRENCY_RATES = "$BASE_URL/api/currency/rates"
    const val CURRENCY_LIST = "$BASE_URL/api/currency/list"
    const val CURRENCY_CONVERT = "$BASE_URL/api/currency/convert"
    fun currencyCrypto(symbol: String): String =
        "$BASE_URL/api/currency/crypto/${symbol.trim().lowercase()}"

    // Tools Endpoints
    const val TOOLS_SHORTURL = "$BASE_URL/api/tools/shorturl"
    const val TOOLS_BASE64_TO_TEXT = "$BASE_URL/api/tools/base642text"
    const val TOOLS_BINARY_TO_TEXT = "$BASE_URL/api/tools/binary2text"
    const val TOOLS_SUBDOMAINS = "$BASE_URL/api/tools/subdomains"
    const val TOOLS_VOICES = "$BASE_URL/api/tools/voices"
    const val TOOLS_IDENTIFY_ANIME = "$BASE_URL/api/tools/identify-anime"
    const val TOOLS_FAKE_DATA = "$BASE_URL/api/tools/fake-data"

    // Anime Endpoints
    const val ANIME_SAMEHADAKU_LATEST = "$BASE_URL/api/anime/samehadaku/latest"
    const val ANIME_SAMEHADAKU_DETAIL = "$BASE_URL/api/anime/samehadaku/detail"
    const val ANIME_SAMEHADAKU_DOWNLOAD = "$BASE_URL/api/anime/samehadaku/download"

    const val ANIME_OTAKUDESU_SEARCH = "$BASE_URL/api/anime/otakudesu/search"
    const val ANIME_OTAKUDESU_ONGOING = "$BASE_URL/api/anime/otakudesu/ongoing"
    const val ANIME_OTAKUDESU_DETAIL = "$BASE_URL/api/anime/otakudesu/detail"
    const val ANIME_OTAKUDESU_DOWNLOAD = "$BASE_URL/api/anime/otakudesu/download"

    const val ANIME_OPLOVERZ_SEARCH = "$BASE_URL/api/anime/oploverz-search"
    const val ANIME_OPLOVERZ_ONGOING = "$BASE_URL/api/anime/oploverz-ongoing"
    const val ANIME_OPLOVERZ_EPISODE = "$BASE_URL/api/anime/oploverz-episode"
    const val ANIME_OPLOVERZ_DOWNLOAD = "$BASE_URL/api/anime/oploverz-download"

    const val ANIME_AURATAIL_SEARCH = "$BASE_URL/api/anime/auratail-search"
    const val ANIME_AURATAIL_POPULAR = "$BASE_URL/api/anime/auratail-popular"
    const val ANIME_AURATAIL_LATEST = "$BASE_URL/api/anime/auratail-latest"
    const val ANIME_AURATAIL_DETAIL = "$BASE_URL/api/anime/auratail-detail"

    const val ANIME_ANICHIN_SEARCH = "$BASE_URL/api/anime/anichin-search"

    // Media & APK Lookup Endpoints
    const val DOWNLOAD_AIO = "$BASE_URL/api/download/aio"
    const val APK_OPENAPK = "$BASE_URL/api/apk/openapk"
    const val APK_AN1 = "$BASE_URL/api/apk/an1"
}
