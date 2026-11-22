package com.example.api

enum class FeatureGroup(
    val key: String,
    val label: String,
    val category: String,
    val description: String,
    val inputHint: String,
    val needsInput: Boolean = true
) {
    // Downloaders
    YTDL("ytdl", "YouTube Video (MP4)", "Downloaders", "Download YouTube videos & shorts", "Paste YouTube URL"),
    YTMP3("ytmp3", "YouTube Audio (MP3)", "Downloaders", "Extract MP3 audio from YouTube", "Paste YouTube URL"),
    IGDL("igdl", "Instagram Downloader", "Downloaders", "Reels, posts & stories", "Paste Instagram URL"),
    FBDL("fbdl", "Facebook Downloader", "Downloaders", "Download Facebook & fb.watch videos", "Paste Facebook URL"),
    TTDL("ttdl", "TikTok Downloader", "Downloaders", "No-watermark TikTok video & search", "Paste TikTok URL"),
    TWITTER("twitter", "Twitter / X Downloader", "Downloaders", "Download X / Twitter videos", "Paste X/Twitter URL"),
    PINTEREST("pinterest", "Pinterest Downloader", "Downloaders", "Download Pinterest photos & videos", "Paste Pinterest URL"),
    SPOTIFY("spotify", "Spotify Downloader", "Downloaders", "Download & search Spotify tracks", "Paste Spotify URL or song"),
    MEDIAFIRE("mediafire", "MediaFire / Cloud DL", "Downloaders", "MediaFire, Mega, Sfile, Videy, SoundCloud, Threads", "Paste file/media URL"),
    CAPCUT("capcut", "CapCut Downloader", "Downloaders", "Download CapCut templates & videos", "Paste CapCut URL"),
    TERABOX("terabox", "Terabox / GDrive DL", "Downloaders", "Direct download links for Terabox & Drive", "Paste Terabox or Drive URL"),
    AIO("aio", "All-in-One Downloader", "Downloaders", "Universal multi-platform video downloader", "Paste any media URL"),
    APK("apk", "APK & App Lookup", "Downloaders", "Search Aptoide, OpenAPK, AN1 catalogs", "Enter app name"),

    // AI
    AI_CHAT("ai", "AI Chat", "AI", "Unified AI command (aliases: ask, chat, gpt, gemini)", "Ask anything..."),
    AI_IMAGE("ai_image", "AI Image Generator", "AI", "Shizo Imagine & Flux image synthesis", "Describe an image..."),

    // Search
    WEB_SEARCH("search", "Web & Code Search", "Search", "Google, DuckDuckGo, GitHub & NPM", "Search query or repo"),
    YT_SEARCH("ytsearch", "YouTube Search", "Search", "Search videos & play metadata", "Search YouTube"),
    LYRICS("lyrics", "Song Lyrics", "Search", "Find full song lyrics instantly", "Song title & artist"),
    NEWS("news", "News Headlines", "Search", "Detik, Kompas & NewsAPI headlines", "Country code or topic (optional)", false),
    WEATHER("weather", "Weather Forecast", "Search", "Live city weather via Cuaca & OpenWeather", "Enter city name"),
    COUNTRY("country", "Country Info", "Search", "Population, currency, flag & details", "Enter country name"),
    CURRENCY("currency", "Currency Rates", "Search", "Exchange rates & conversion", "Base currency (e.g. USD)", false),
    CRYPTO("crypto", "Crypto Prices", "Search", "Live cryptocurrency prices", "Symbol (btc, eth, sol)"),
    WIKI("wiki", "Wikipedia Summary", "Search", "Quick Wikipedia encyclopedia lookup", "Topic name"),
    DICTIONARY("dictionary", "Dictionary & Grammar", "Search", "Word definitions, grammar, Bible & OMDB", "Word, verse or movie"),

    // Media & Fun
    WALLPAPERS("wallpapers", "Wallpapers & Photos", "Media & Fun", "Unsplash, HD wallpapers, Couple PFP, Picsum", "Wallpaper theme (optional)", false),
    ANIME("anime", "Anime & Waifu Hub", "Media & Fun", "Animu, Waifu, Pies, Samehadaku, Otakudesu, Oploverz, Auratail, Anichin", "Anime name or category (optional)", false),
    MEMES("memes", "Memes, Quotes & Fun", "Media & Fun", "Memes, dad jokes, facts, advice, quotes, truth/dare, shayari, trivia", "Optional topic", false),
    EFFECTS("effects", "Effects & Stickers", "Media & Fun", "Canvas overlays, quotly, memegen, ATTP, simp/wasted/tweet cards", "Text or avatar URL"),
    LOGOS("logos", "Logo & Text Maker", "Media & Fun", "37 Ephoto360 3D, neon, metal & anime text effects", "Enter your text"),
    TRANSLATE("translate", "Translate & NLP", "Media & Fun", "Google Translate, Dreaded, MyMemory, Cloudflare AI NLP", "Text to translate"),
    QR("qr", "QR Code Generator", "Media & Fun", "Instant QR image from any URL or text", "URL or text"),
    SCREENSHOT("screenshot", "Website Screenshot", "Media & Fun", "Capture full preview of any webpage", "Website URL (https://...)"),
    TOOLS("tools", "Web & Social Tools", "Media & Fun", "ShortURL, Fancy Text, TempMail, Base64, Binary, Subdomains, TikTok Stalk, Voices", "URL, domain or text")
}

enum class AuthKeySlot(val keyName: String, val label: String, val placeholder: String) {
    NONE("", "", ""),
    SHIZO("shizo_apikey", "Shizo API Key", "shizo (optional default)"),
    PRINCETECH("princetech_apikey", "PrinceTech API Key", "princetechn key"),
    GROQ("groq_apikey", "Groq API Key", "gsk_..."),
    OPENWEATHER("openweather_apikey", "OpenWeatherMap Key", "OpenWeather appid"),
    NEWSAPI("newsapi_apikey", "NewsAPI Key", "NewsAPI apiKey"),
    GIPHY("giphy_apikey", "Giphy API Key", "Giphy api_key"),
    OMDB("omdb_apikey", "OMDB API Key", "OMDB apikey"),
    IMGBB("imgbb_apikey", "ImgBB API Key", "ImgBB key"),
    NEXORACLE("nexoracle_apikey", "NexOracle API Key", "NexOracle apikey"),
    GIFTEDTECH("giftedtech_apikey", "GiftedTech API Key", "GiftedTech apikey"),
    TELEGRAM("telegram_bot_token", "Telegram Bot Token", "123456:ABC...")
}

data class ProviderEndpoint(
    val id: String,
    val name: String,
    val host: String,
    val feature: FeatureGroup,
    val baseUrl: String,
    val path: String,
    val method: String = "GET",
    val paramKey: String = "url",
    val extraParams: Map<String, String> = emptyMap(),
    val isPrimary: Boolean = true,
    val priority: Int = 10,
    val description: String = "",
    val authKeySlot: AuthKeySlot = AuthKeySlot.NONE,
    val authParamName: String = "apikey",
    val isPathTemplate: Boolean = false,
    val expectsBinaryImage: Boolean = false
) {
    val fullEndpointUrl: String get() = "$baseUrl$path"
}

enum class AiLogoStyle {
    OPENAI_GPT,
    OPENAI_EXTRA,
    RAUF_CROWN,
    RAUF_MINI,
    CLAUDE_SPARK,
    GEMINI_STAR,
    LLAMA_LOOP,
    CRESCENT,
    LOTUS,
    SCRIPTURE,
    VISION_LENS
}

data class AiModelSpec(
    val id: String,
    val name: String,
    val brandName: String,
    val tierBadge: String,
    val providerName: String,
    val description: String,
    val isPrimary: Boolean,
    val isImageModel: Boolean = false,
    val providerEndpointId: String,
    val logoStyle: AiLogoStyle = AiLogoStyle.OPENAI_GPT
)

object ProviderRegistry {

    // Ephoto360: all 37 logo/text maker effect slugs from the API LIST
    val EPHOTO_EFFECTS: List<Pair<String, String>> = listOf(
        "1917 Style" to "/1917-style-text-effect-523.html",
        "Boom Comic" to "/boom-text-comic-style-text-effect-675.html",
        "Chocolate" to "/chocolate-text-effect-353.html",
        "3D Colorful Paint" to "/create-3d-colorful-paint-text-effect-online-801.html",
        "Blackpink Signature" to "/create-a-blackpink-style-logo-with-members-signatures-810.html",
        "Cartoon Graffiti" to "/create-a-cartoon-style-graffiti-text-effect-online-668.html",
        "Frozen Christmas" to "/create-a-frozen-christmas-text-effect-online-792.html",
        "3D Snow" to "/create-a-snow-3d-text-effect-free-online-621.html",
        "Anonymous Hacker Cyan" to "/create-anonymous-hacker-avatars-cyan-neon-677.html",
        "Colorful Neon Light" to "/create-colorful-neon-light-text-effects-online-797.html",
        "Arena of Valor Cover" to "/create-cover-arena-of-valor-by-mastering-360.html",
        "Digital Glitch" to "/create-digital-glitch-text-effects-online-767.html",
        "Dragon Ball Style" to "/create-dragon-ball-style-text-effects-online-809.html",
        "Sunset Light" to "/create-sunset-light-text-effects-online-807.html",
        "Water Effect" to "/create-water-effect-text-online-295.html",
        "Mechanical Name" to "/create-your-name-in-a-mechanical-style-306.html",
        "Dragon Steel" to "/dragon-steel-text-effect-online-347.html",
        "Flame Lettering" to "/flame-lettering-effect-372.html",
        "Foggy Rainy" to "/foggy-rainy-text-effect-75.html",
        "Green Brush" to "/green-brush-text-effect-typography-maker-online-153.html",
        "Foggy Glass" to "/handwritten-text-on-foggy-glass-online-680.html",
        "Ice Text" to "/ice-text-effect-online-101.html",
        "3D Decorative Metal" to "/impressive-decorative-3d-metal-text-effect-798.html",
        "Futuristic Light" to "/light-text-effect-futuristic-technology-style-648.html",
        "Matrix Code" to "/matrix-text-effect-154.html",
        "Modern Gold" to "/modern-gold-4-213.html",
        "Naruto Shippuden" to "/naruto-shippuden-logo-style-text-effect-online-808.html",
        "Neon Devil Wings" to "/neon-devil-wings-text-effect-online-683.html",
        "Purple Glow" to "/purple-text-effect-online-100.html",
        "Stars Night" to "/stars-night-online-1-85.html",
        "Sunlight Shadow" to "/sunlight-shadow-text-204.html",
        "Incandescent Bulbs" to "/text-effects-incandescent-bulbs-219.html",
        "Cloth Fabric" to "/text-on-cloth-effect-62.html",
        "Thunder Strike" to "/thunder-text-effect-online-97.html",
        "Pavement Typography" to "/typography-text-effect-on-pavement-online-774.html",
        "Write on Sand" to "/write-names-and-messages-on-the-sand-online-582.html",
        "Wet Glass" to "/write-text-on-wet-glass-online-589.html"
    )

    // Single Provider Registry: adding any new endpoint is one line
    val ALL_PROVIDERS: List<ProviderEndpoint> = buildList {
        // ============================================================
        // 1. AI CHAT & AI IMAGE (PRIMARY = API LIST, FALLBACK = EXISTING TECHXKAMRAN)
        // ============================================================
        add(ProviderEndpoint("eliteprotech_chatgpt", "EliteProTech ChatGPT", "eliteprotech-apis.zone.id", FeatureGroup.AI_CHAT, "https://eliteprotech-apis.zone.id", "/ai/chatgpt", paramKey = "prompt", priority = 1, description = "Primary conversational AI from EliteProTech"))
        add(ProviderEndpoint("groq_openai", "Groq Llama 3.3 (API Key)", "api.groq.com", FeatureGroup.AI_CHAT, "https://api.groq.com", "/openai/v1/chat/completions", method = "POST", paramKey = "prompt", priority = 2, description = "Ultra-fast Groq inference (set key in Settings)", authKeySlot = AuthKeySlot.GROQ, authParamName = "Authorization"))
        add(ProviderEndpoint("shizo_imagine", "Shizo AI Imagine", "shizoapi.onrender.com", FeatureGroup.AI_IMAGE, "https://shizoapi.onrender.com", "/api/ai/imagine", paramKey = "query", priority = 1, description = "Primary Shizo AI image generator", authKeySlot = AuthKeySlot.SHIZO, expectsBinaryImage = true))

        // Existing TechXKamran AI endpoints kept intact as FALLBACKS
        add(ProviderEndpoint("techx_gemini", "TechX Gemini", "techxkamran.onrender.com", FeatureGroup.AI_CHAT, "https://techxkamran.onrender.com", "/api/ai/gemini", paramKey = "prompt", isPrimary = false, priority = 101, description = "Fallback Gemini AI via TechXKamran"))
        add(ProviderEndpoint("techx_gemini_lite", "TechX Gemini Lite", "techxkamran.onrender.com", FeatureGroup.AI_CHAT, "https://techxkamran.onrender.com", "/api/ai/gemini-lite", paramKey = "prompt", isPrimary = false, priority = 102, description = "Fast low-latency Gemini Lite fallback"))
        add(ProviderEndpoint("techx_powerbrain", "TechX PowerBrain AI", "techxkamran.onrender.com", FeatureGroup.AI_CHAT, "https://techxkamran.onrender.com", "/api/ai/powerbrainai", paramKey = "prompt", isPrimary = false, priority = 103, description = "PowerBrain general assistant"))
        add(ProviderEndpoint("techx_bard", "TechX Bard", "techxkamran.onrender.com", FeatureGroup.AI_CHAT, "https://techxkamran.onrender.com", "/api/ai/bard", paramKey = "prompt", isPrimary = false, priority = 104, description = "Conversational Bard endpoint"))
        add(ProviderEndpoint("techx_cf_chat", "TechX Cloudflare Chat", "techxkamran.onrender.com", FeatureGroup.AI_CHAT, "https://techxkamran.onrender.com", "/api/cf/chat", paramKey = "prompt", isPrimary = false, priority = 105, description = "Cloudflare Workers AI LLM chat"))
        add(ProviderEndpoint("techx_muslimai", "TechX Muslim AI", "techxkamran.onrender.com", FeatureGroup.AI_CHAT, "https://techxkamran.onrender.com", "/api/ai/muslimai", paramKey = "q", isPrimary = false, priority = 106, description = "Islamic knowledge & Quran Q&A"))
        add(ProviderEndpoint("techx_gita", "TechX Gita AI", "techxkamran.onrender.com", FeatureGroup.AI_CHAT, "https://techxkamran.onrender.com", "/api/ai/gita", paramKey = "q", isPrimary = false, priority = 107, description = "Bhagavad Gita teachings Q&A"))
        add(ProviderEndpoint("techx_bibleai", "TechX Bible AI", "techxkamran.onrender.com", FeatureGroup.AI_CHAT, "https://techxkamran.onrender.com", "/api/ai/bibleai", paramKey = "q", isPrimary = false, priority = 108, description = "Scripture & Bible assistant"))
        add(ProviderEndpoint("techx_flux", "TechX Flux Image", "techxkamran.onrender.com", FeatureGroup.AI_IMAGE, "https://techxkamran.onrender.com", "/api/ai/flux", paramKey = "prompt", isPrimary = false, priority = 101, description = "Fallback Flux image generation", expectsBinaryImage = true))

        // ============================================================
        // 2. YOUTUBE DOWNLOADERS (MP4 & MP3)
        // ============================================================
        add(ProviderEndpoint("adeel_ytmp4", "Adeel YTMP4", "adeel-xtech-apis.vercel.app", FeatureGroup.YTDL, "https://adeel-xtech-apis.vercel.app", "/api/ytmp4", priority = 1))
        add(ProviderEndpoint("adeel_ytmp4v2", "Adeel YTMP4 v2", "adeel-xtech-apis.vercel.app", FeatureGroup.YTDL, "https://adeel-xtech-apis.vercel.app", "/api/ytmp4v2", priority = 2))
        add(ProviderEndpoint("adeel_ytdl", "Adeel YTDL", "adeel-xtech-apis.vercel.app", FeatureGroup.YTDL, "https://adeel-xtech-apis.vercel.app", "/api/ytdl", priority = 3))
        add(ProviderEndpoint("faa_ytmp4", "Faa YTMP4", "api-faa.my.id", FeatureGroup.YTDL, "https://api-faa.my.id", "/faa/ytmp4", priority = 4))
        add(ProviderEndpoint("eliteprotech_ytmp4", "EliteProTech YTMP4", "eliteprotech-apis.zone.id", FeatureGroup.YTDL, "https://eliteprotech-apis.zone.id", "/download/ytmp4", priority = 5))
        add(ProviderEndpoint("okatsu_ytmp4", "Okatsu YTMP4", "okatsu-rolezapiiz.vercel.app", FeatureGroup.YTDL, "https://okatsu-rolezapiiz.vercel.app", "/downloader/ytmp4", priority = 6))
        add(ProviderEndpoint("yupra_ytmp4", "Yupra YTMP4", "api.yupra.my.id", FeatureGroup.YTDL, "https://api.yupra.my.id", "/api/downloader/ytmp4", priority = 7))
        add(ProviderEndpoint("david_ytmp4", "DavidCyril YTMP4", "apis.davidcyriltech.my.id", FeatureGroup.YTDL, "https://apis.davidcyriltech.my.id", "/download/ytmp4", priority = 8))
        add(ProviderEndpoint("nayan_ytdown", "Nayan YTDown", "nayan-video-downloader.vercel.app", FeatureGroup.YTDL, "https://nayan-video-downloader.vercel.app", "/ytdown", priority = 9))
        add(ProviderEndpoint("nayan_ytplaylist", "Nayan YT Playlist", "nayan-video-downloader.vercel.app", FeatureGroup.YTDL, "https://nayan-video-downloader.vercel.app", "/ytplaylist", priority = 10))
        add(ProviderEndpoint("noobs_ytdl3", "Noobs YTDL3", "noobs-api.top", FeatureGroup.YTDL, "https://noobs-api.top", "/dipto/ytDl3", paramKey = "link", priority = 11))
        add(ProviderEndpoint("techx_aio_yt", "TechX AIO (Fallback)", "techxkamran.onrender.com", FeatureGroup.YTDL, "https://techxkamran.onrender.com", "/api/download/aio", isPrimary = false, priority = 101))

        add(ProviderEndpoint("adeel_ytmp3", "Adeel YTMP3", "adeel-xtech-apis.vercel.app", FeatureGroup.YTMP3, "https://adeel-xtech-apis.vercel.app", "/api/ytmp3", priority = 1))
        add(ProviderEndpoint("faa_ytmp3", "Faa YTMP3", "api-faa.my.id", FeatureGroup.YTMP3, "https://api-faa.my.id", "/faa/ytmp3", priority = 2))
        add(ProviderEndpoint("nexray_ytmp3", "Nexray YTMP3", "api.nexray.web.id", FeatureGroup.YTMP3, "https://api.nexray.web.id", "/downloader/ytmp3", priority = 3))
        add(ProviderEndpoint("eliteprotech_ytmp3", "EliteProTech YTMP3", "eliteprotech-apis.zone.id", FeatureGroup.YTMP3, "https://eliteprotech-apis.zone.id", "/download/ytmp3", priority = 4))
        add(ProviderEndpoint("okatsu_ytmp3", "Okatsu YTMP3", "okatsu-rolezapiiz.vercel.app", FeatureGroup.YTMP3, "https://okatsu-rolezapiiz.vercel.app", "/downloader/ytmp3", priority = 5))
        add(ProviderEndpoint("yupra_ytmp3", "Yupra YTMP3", "api.yupra.my.id", FeatureGroup.YTMP3, "https://api.yupra.my.id", "/api/downloader/ytmp3", priority = 6))
        add(ProviderEndpoint("keith_dlmp3", "Keith DLMP3", "apis-keith.vercel.app", FeatureGroup.YTMP3, "https://apis-keith.vercel.app", "/download/dlmp3", priority = 7))
        add(ProviderEndpoint("techx_aio_mp3", "TechX AIO (Fallback)", "techxkamran.onrender.com", FeatureGroup.YTMP3, "https://techxkamran.onrender.com", "/api/download/aio", isPrimary = false, priority = 101))

        // ============================================================
        // 3. SOCIAL & CLOUD DOWNLOADERS (IG, FB, TIKTOK, X, PINTEREST, SPOTIFY, CLOUD, CAPCUT, TERABOX, AIO, APK)
        // ============================================================
        add(ProviderEndpoint("adeel_igdl", "Adeel IGDL", "adeel-xtech-apis.vercel.app", FeatureGroup.IGDL, "https://adeel-xtech-apis.vercel.app", "/api/igdl", priority = 1))
        add(ProviderEndpoint("faa_igdl", "Faa IGDL", "api-faa.my.id", FeatureGroup.IGDL, "https://api-faa.my.id", "/faa/igdl", priority = 2))
        add(ProviderEndpoint("siputzx_ig_posts", "Siputzx IG Posts", "api.siputzx.my.id", FeatureGroup.IGDL, "https://api.siputzx.my.id", "/api/igdl/posts", priority = 3))
        add(ProviderEndpoint("siputzx_ig_stories", "Siputzx IG Stories", "api.siputzx.my.id", FeatureGroup.IGDL, "https://api.siputzx.my.id", "/api/igdl/stories", priority = 4))
        add(ProviderEndpoint("techx_aio_ig", "TechX AIO (Fallback)", "techxkamran.onrender.com", FeatureGroup.IGDL, "https://techxkamran.onrender.com", "/api/download/aio", isPrimary = false, priority = 101))

        add(ProviderEndpoint("adeel_fbdl", "Adeel FBDL", "adeel-xtech-apis.vercel.app", FeatureGroup.FBDL, "https://adeel-xtech-apis.vercel.app", "/api/fbdl", priority = 1))
        add(ProviderEndpoint("faa_fbdl", "Faa FBDL", "api-faa.my.id", FeatureGroup.FBDL, "https://api-faa.my.id", "/faa/fbdownload", priority = 2))
        add(ProviderEndpoint("siputzx_fbdl", "Siputzx FBDL", "api.siputzx.my.id", FeatureGroup.FBDL, "https://api.siputzx.my.id", "/api/d/facebook", priority = 3))
        add(ProviderEndpoint("hanggts_fbdl", "Hanggts FBDL", "api.hanggts.xyz", FeatureGroup.FBDL, "https://api.hanggts.xyz", "/download/facebook", priority = 4))
        add(ProviderEndpoint("zenkey_fbdl", "Zenkey FBDL", "api.zenkey.my.id", FeatureGroup.FBDL, "https://api.zenkey.my.id", "/download/fb", priority = 5))
        add(ProviderEndpoint("nexoracle_fbdl", "NexOracle FBDL", "api.nexoracle.com", FeatureGroup.FBDL, "https://api.nexoracle.com", "/downloader/facebook", priority = 6, authKeySlot = AuthKeySlot.NEXORACLE))
        add(ProviderEndpoint("techx_aio_fb", "TechX AIO (Fallback)", "techxkamran.onrender.com", FeatureGroup.FBDL, "https://techxkamran.onrender.com", "/api/download/aio", isPrimary = false, priority = 101))

        add(ProviderEndpoint("tikwm_tt", "TikWM API", "tikwm.com", FeatureGroup.TTDL, "https://tikwm.com", "/api/", priority = 1))
        add(ProviderEndpoint("adeel_ttdl", "Adeel TTDL", "adeel-xtech-apis.vercel.app", FeatureGroup.TTDL, "https://adeel-xtech-apis.vercel.app", "/api/ttdl", priority = 2))
        add(ProviderEndpoint("adeel_ttv2", "Adeel TikTok v2", "adeel-xtech-apis.vercel.app", FeatureGroup.TTDL, "https://adeel-xtech-apis.vercel.app", "/api/tiktok-v2", priority = 3))
        add(ProviderEndpoint("siputzx_tt", "Siputzx TikTok", "api.siputzx.my.id", FeatureGroup.TTDL, "https://api.siputzx.my.id", "/api/d/tiktok", priority = 4))
        add(ProviderEndpoint("kelvdra_tt", "Kelvdra TikTok", "kelvdraapi.domku.xyz", FeatureGroup.TTDL, "https://kelvdraapi.domku.xyz", "/downloader/tiktok", priority = 5))
        add(ProviderEndpoint("kelvdra_tt_search", "Kelvdra TikTok Search", "kelvdraapi.domku.xyz", FeatureGroup.TTDL, "https://kelvdraapi.domku.xyz", "/search/tiktok", paramKey = "query", priority = 6))
        add(ProviderEndpoint("delirius_tt", "Delirius TikTok", "delirius-apiofc.vercel.app", FeatureGroup.TTDL, "https://delirius-apiofc.vercel.app", "/download/tiktok", priority = 7))
        add(ProviderEndpoint("techx_aio_tt", "TechX AIO (Fallback)", "techxkamran.onrender.com", FeatureGroup.TTDL, "https://techxkamran.onrender.com", "/api/download/aio", isPrimary = false, priority = 101))

        add(ProviderEndpoint("adeel_twitter", "Adeel TwitterDL", "adeel-xtech-apis.vercel.app", FeatureGroup.TWITTER, "https://adeel-xtech-apis.vercel.app", "/api/twitterdl", priority = 1))
        add(ProviderEndpoint("nexray_twitter", "Nexray Twitter", "api.nexray.web.id", FeatureGroup.TWITTER, "https://api.nexray.web.id", "/downloader/twitter", priority = 2))
        add(ProviderEndpoint("nayan_twitter", "Nayan TwitterDown", "nayan-video-downloader.vercel.app", FeatureGroup.TWITTER, "https://nayan-video-downloader.vercel.app", "/twitterdown", priority = 3))
        add(ProviderEndpoint("techx_aio_tw", "TechX AIO (Fallback)", "techxkamran.onrender.com", FeatureGroup.TWITTER, "https://techxkamran.onrender.com", "/api/download/aio", isPrimary = false, priority = 101))

        add(ProviderEndpoint("adeel_pin_video", "Adeel Pinterest Video", "adeel-xtech-apis.vercel.app", FeatureGroup.PINTEREST, "https://adeel-xtech-apis.vercel.app", "/api/pinterest-video", priority = 1))
        add(ProviderEndpoint("adeel_pin_photo", "Adeel Pinterest Photo", "adeel-xtech-apis.vercel.app", FeatureGroup.PINTEREST, "https://adeel-xtech-apis.vercel.app", "/api/pinterest-photo", priority = 2))
        add(ProviderEndpoint("faa_pin", "Faa Pin-Down", "api-faa.my.id", FeatureGroup.PINTEREST, "https://api-faa.my.id", "/faa/pin-down", priority = 3))
        add(ProviderEndpoint("prince_pin", "PrinceTech Pinterest", "api.princetechn.com", FeatureGroup.PINTEREST, "https://api.princetechn.com", "/api/download/pinterestdl", priority = 4, authKeySlot = AuthKeySlot.PRINCETECH))
        add(ProviderEndpoint("gifted_pin", "GiftedTech Pinterest", "api.giftedtech.web.id", FeatureGroup.PINTEREST, "https://api.giftedtech.web.id", "/api/download/pinterestdl", priority = 5, authKeySlot = AuthKeySlot.GIFTEDTECH))
        add(ProviderEndpoint("techx_aio_pin", "TechX AIO (Fallback)", "techxkamran.onrender.com", FeatureGroup.PINTEREST, "https://techxkamran.onrender.com", "/api/download/aio", isPrimary = false, priority = 101))

        add(ProviderEndpoint("adeel_spotify", "Adeel SpotifyDL", "adeel-xtech-apis.vercel.app", FeatureGroup.SPOTIFY, "https://adeel-xtech-apis.vercel.app", "/api/spotifydl", priority = 1))
        add(ProviderEndpoint("nexray_spotify", "Nexray Spotify", "api.nexray.web.id", FeatureGroup.SPOTIFY, "https://api.nexray.web.id", "/downloader/spotify", priority = 2))
        add(ProviderEndpoint("david_spotify", "DavidCyril SpotifyDL", "apis.davidcyriltech.my.id", FeatureGroup.SPOTIFY, "https://apis.davidcyriltech.my.id", "/spotifydl", priority = 3))
        add(ProviderEndpoint("prince_spotify_dl", "PrinceTech SpotifyDL", "api.princetechn.com", FeatureGroup.SPOTIFY, "https://api.princetechn.com", "/api/download/spotifydl", priority = 4, authKeySlot = AuthKeySlot.PRINCETECH))
        add(ProviderEndpoint("okatsu_spotify_s", "Okatsu Spotify Search", "okatsu-rolezapiiz.vercel.app", FeatureGroup.SPOTIFY, "https://okatsu-rolezapiiz.vercel.app", "/search/spotify", paramKey = "q", priority = 5))
        add(ProviderEndpoint("siputzx_spotify_s", "Siputzx Spotify Search", "api.siputzx.my.id", FeatureGroup.SPOTIFY, "https://api.siputzx.my.id", "/api/s/spotify", paramKey = "query", priority = 6))
        add(ProviderEndpoint("prince_spotify_s", "PrinceTech Spotify Search", "api.princetechn.com", FeatureGroup.SPOTIFY, "https://api.princetechn.com", "/api/search/spotifysearch", paramKey = "query", priority = 7, authKeySlot = AuthKeySlot.PRINCETECH))

        add(ProviderEndpoint("adeel_mediafire", "Adeel MediaFire", "adeel-xtech-apis.vercel.app", FeatureGroup.MEDIAFIRE, "https://adeel-xtech-apis.vercel.app", "/api/mediafire", priority = 1))
        add(ProviderEndpoint("faa_mediafire", "Faa MediaFire", "api-faa.my.id", FeatureGroup.MEDIAFIRE, "https://api-faa.my.id", "/faa/mediafire", priority = 2))
        add(ProviderEndpoint("nexray_mega", "Nexray Mega", "api.nexray.web.id", FeatureGroup.MEDIAFIRE, "https://api.nexray.web.id", "/downloader/mega", priority = 3))
        add(ProviderEndpoint("nexray_sfile", "Nexray Sfile", "api.nexray.web.id", FeatureGroup.MEDIAFIRE, "https://api.nexray.web.id", "/downloader/sfile", priority = 4))
        add(ProviderEndpoint("nexray_soundcloud", "Nexray SoundCloud", "api.nexray.web.id", FeatureGroup.MEDIAFIRE, "https://api.nexray.web.id", "/downloader/soundcloud", priority = 5))
        add(ProviderEndpoint("nexray_threads", "Nexray Threads", "api.nexray.web.id", FeatureGroup.MEDIAFIRE, "https://api.nexray.web.id", "/downloader/threads", priority = 6))
        add(ProviderEndpoint("nexray_videy", "Nexray Videy", "api.nexray.web.id", FeatureGroup.MEDIAFIRE, "https://api.nexray.web.id", "/downloader/videy", priority = 7))

        add(ProviderEndpoint("adeel_capcut", "Adeel CapCut", "adeel-xtech-apis.vercel.app", FeatureGroup.CAPCUT, "https://adeel-xtech-apis.vercel.app", "/api/capcut", priority = 1))
        add(ProviderEndpoint("techx_aio_cc", "TechX AIO (Fallback)", "techxkamran.onrender.com", FeatureGroup.CAPCUT, "https://techxkamran.onrender.com", "/api/download/aio", isPrimary = false, priority = 101))

        add(ProviderEndpoint("adeel_terabox", "Adeel TeraboxDL", "adeel-xtech-apis.vercel.app", FeatureGroup.TERABOX, "https://adeel-xtech-apis.vercel.app", "/api/terabox-dl", priority = 1))
        add(ProviderEndpoint("gdrive_direct", "Google Drive Direct", "docs.google.com", FeatureGroup.TERABOX, "https://docs.google.com", "/uc", paramKey = "id", extraParams = mapOf("export" to "download"), priority = 2))

        add(ProviderEndpoint("bk9_alldownload", "BK9 AllDownload", "bk9.fun", FeatureGroup.AIO, "https://bk9.fun", "/download/alldownload", priority = 1))
        add(ProviderEndpoint("aiovideodl", "AIOVideoDL", "aiovideodl.ml", FeatureGroup.AIO, "https://aiovideodl.ml", "/wp-json/aio-dl/video-data/", priority = 2))
        add(ProviderEndpoint("techx_aio", "TechX AIO (Fallback)", "techxkamran.onrender.com", FeatureGroup.AIO, "https://techxkamran.onrender.com", "/api/download/aio", isPrimary = false, priority = 101))

        add(ProviderEndpoint("adeel_aptoide", "Adeel Aptoide", "adeel-xtech-apis.vercel.app", FeatureGroup.APK, "https://adeel-xtech-apis.vercel.app", "/api/aptoide", paramKey = "q", priority = 1))
        add(ProviderEndpoint("siputzx_aptoide", "Siputzx Aptoide", "api.siputzx.my.id", FeatureGroup.APK, "https://api.siputzx.my.id", "/api/apk/aptoide", paramKey = "query", priority = 2))
        add(ProviderEndpoint("nexoracle_apk", "NexOracle APK", "api.nexoracle.com", FeatureGroup.APK, "https://api.nexoracle.com", "/downloader/apk", paramKey = "q", priority = 3, authKeySlot = AuthKeySlot.NEXORACLE))
        add(ProviderEndpoint("techx_openapk", "TechX OpenAPK (Fallback)", "techxkamran.onrender.com", FeatureGroup.APK, "https://techxkamran.onrender.com", "/api/apk/openapk", paramKey = "q", isPrimary = false, priority = 101))
        add(ProviderEndpoint("techx_an1", "TechX AN1 (Fallback)", "techxkamran.onrender.com", FeatureGroup.APK, "https://techxkamran.onrender.com", "/api/apk/an1", paramKey = "q", isPrimary = false, priority = 102))

        // ============================================================
        // 4. SEARCH (WEB, YOUTUBE, LYRICS, NEWS, WEATHER, COUNTRY, CURRENCY, CRYPTO, WIKI, DICTIONARY)
        // ============================================================
        add(ProviderEndpoint("adeel_google", "Adeel Google Search", "adeel-xtech-apis.vercel.app", FeatureGroup.WEB_SEARCH, "https://adeel-xtech-apis.vercel.app", "/api/googlesearch", paramKey = "q", priority = 1))
        add(ProviderEndpoint("adeel_ddg", "Adeel DuckDuckGo", "adeel-xtech-apis.vercel.app", FeatureGroup.WEB_SEARCH, "https://adeel-xtech-apis.vercel.app", "/api/duckduckgo", paramKey = "q", priority = 2))
        add(ProviderEndpoint("github_user", "GitHub User Lookup", "api.github.com", FeatureGroup.WEB_SEARCH, "https://api.github.com", "/users/{q}", paramKey = "q", priority = 3, isPathTemplate = true))
        add(ProviderEndpoint("github_repo_rauf", "GitHub RAUF Repo", "api.github.com", FeatureGroup.WEB_SEARCH, "https://api.github.com", "/repos/FantoX/RAUF", paramKey = "", priority = 4))
        add(ProviderEndpoint("npm_search", "NPM Package Search", "registry.npmjs.org", FeatureGroup.WEB_SEARCH, "https://registry.npmjs.org", "/-/v1/search", paramKey = "text", priority = 5))
        add(ProviderEndpoint("ddg_html", "DuckDuckGo HTML", "html.duckduckgo.com", FeatureGroup.WEB_SEARCH, "https://html.duckduckgo.com", "/html/", paramKey = "q", priority = 6))

        add(ProviderEndpoint("adeel_ytsearch", "Adeel YouTube Search", "adeel-xtech-apis.vercel.app", FeatureGroup.YT_SEARCH, "https://adeel-xtech-apis.vercel.app", "/api/ytsearch", paramKey = "q", priority = 1))
        add(ProviderEndpoint("faa_youtube", "Faa YouTube Search", "api-faa.my.id", FeatureGroup.YT_SEARCH, "https://api-faa.my.id", "/faa/youtube", paramKey = "q", priority = 2))
        add(ProviderEndpoint("faa_ytplay", "Faa YTPlay", "api-faa.my.id", FeatureGroup.YT_SEARCH, "https://api-faa.my.id", "/faa/ytplay", paramKey = "query", priority = 3))

        add(ProviderEndpoint("popcat_lyrics", "Popcat Lyrics", "api.popcat.xyz", FeatureGroup.LYRICS, "https://api.popcat.xyz", "/v2/lyrics", paramKey = "song", priority = 1))
        add(ProviderEndpoint("flydev_lyrics", "LyricsAPI Fly.dev", "lyricsapi.fly.dev", FeatureGroup.LYRICS, "https://lyricsapi.fly.dev", "/api/lyrics", paramKey = "q", priority = 2))

        add(ProviderEndpoint("adeel_detik", "Adeel DetikNews", "adeel-xtech-apis.vercel.app", FeatureGroup.NEWS, "https://adeel-xtech-apis.vercel.app", "/api/detiknews", paramKey = "q", priority = 1))
        add(ProviderEndpoint("adeel_kompas", "Adeel KompasNews", "adeel-xtech-apis.vercel.app", FeatureGroup.NEWS, "https://adeel-xtech-apis.vercel.app", "/api/kompasnews", paramKey = "q", priority = 2))
        add(ProviderEndpoint("newsapi_headlines", "NewsAPI Headlines", "newsapi.org", FeatureGroup.NEWS, "https://newsapi.org", "/v2/top-headlines", paramKey = "country", extraParams = mapOf("country" to "us"), priority = 3, authKeySlot = AuthKeySlot.NEWSAPI, authParamName = "apiKey"))

        add(ProviderEndpoint("adeel_cuaca", "Adeel Weather (Cuaca)", "adeel-xtech-apis.vercel.app", FeatureGroup.WEATHER, "https://adeel-xtech-apis.vercel.app", "/api/cuaca", paramKey = "q", priority = 1))
        add(ProviderEndpoint("openweather", "OpenWeatherMap", "api.openweathermap.org", FeatureGroup.WEATHER, "https://api.openweathermap.org", "/data/2.5/weather", paramKey = "q", extraParams = mapOf("units" to "metric"), priority = 2, authKeySlot = AuthKeySlot.OPENWEATHER, authParamName = "appid"))

        add(ProviderEndpoint("countryapi", "CountryAPI Gleeze", "countryapi.gleeze.com", FeatureGroup.COUNTRY, "https://countryapi.gleeze.com", "/api/country", paramKey = "name", priority = 1))

        add(ProviderEndpoint("open_er_api", "Open Exchange Rates", "open.er-api.com", FeatureGroup.CURRENCY, "https://open.er-api.com", "/v6/latest/{q}", paramKey = "q", priority = 1, isPathTemplate = true))
        add(ProviderEndpoint("techx_currency_rates", "TechX Currency Rates (Fallback)", "techxkamran.onrender.com", FeatureGroup.CURRENCY, "https://techxkamran.onrender.com", "/api/currency/rates", paramKey = "base", isPrimary = false, priority = 101))
        add(ProviderEndpoint("techx_currency_list", "TechX Currency List (Fallback)", "techxkamran.onrender.com", FeatureGroup.CURRENCY, "https://techxkamran.onrender.com", "/api/currency/list", paramKey = "", isPrimary = false, priority = 102))
        add(ProviderEndpoint("techx_currency_convert", "TechX Currency Convert (Fallback)", "techxkamran.onrender.com", FeatureGroup.CURRENCY, "https://techxkamran.onrender.com", "/api/currency/convert", paramKey = "from", isPrimary = false, priority = 103))

        add(ProviderEndpoint("techx_crypto", "TechX Crypto Tracker", "techxkamran.onrender.com", FeatureGroup.CRYPTO, "https://techxkamran.onrender.com", "/api/currency/crypto/{q}", paramKey = "q", priority = 1, isPathTemplate = true))

        add(ProviderEndpoint("wikipedia_summary", "Wikipedia REST Summary", "en.wikipedia.org", FeatureGroup.WIKI, "https://en.wikipedia.org", "/api/rest_v1/page/summary/{q}", paramKey = "q", priority = 1, isPathTemplate = true))

        add(ProviderEndpoint("dictionaryapi", "Free Dictionary API", "api.dictionaryapi.dev", FeatureGroup.DICTIONARY, "https://api.dictionaryapi.dev", "/api/v2/entries/en/{q}", paramKey = "q", priority = 1, isPathTemplate = true))
        add(ProviderEndpoint("languagetool", "LanguageTool Grammar", "api.languagetool.org", FeatureGroup.DICTIONARY, "https://api.languagetool.org", "/v2/check", method = "POST", paramKey = "text", extraParams = mapOf("language" to "en-US"), priority = 2))
        add(ProviderEndpoint("bible_api", "Bible API", "bible-api.com", FeatureGroup.DICTIONARY, "https://bible-api.com", "/{q}", paramKey = "q", priority = 3, isPathTemplate = true))
        add(ProviderEndpoint("omdb_api", "OMDB Movie Info", "www.omdbapi.com", FeatureGroup.DICTIONARY, "https://www.omdbapi.com", "/", paramKey = "t", priority = 4, authKeySlot = AuthKeySlot.OMDB))

        // ============================================================
        // 5. MEDIA, WALLPAPERS, ANIME, MEMES, EFFECTS, LOGOS, TRANSLATE, QR, SCREENSHOT, TOOLS
        // ============================================================
        add(ProviderEndpoint("toxxic_wallpaper", "Toxxic Wallpapers", "api-toxxic.zone.id", FeatureGroup.WALLPAPERS, "https://api-toxxic.zone.id", "/api/search/wallpaper", paramKey = "q", priority = 1))
        add(ProviderEndpoint("toxxic_unsplash", "Toxxic Unsplash", "api-toxxic.zone.id", FeatureGroup.WALLPAPERS, "https://api-toxxic.zone.id", "/api/search/unsplash", paramKey = "q", priority = 2))
        add(ProviderEndpoint("couple_pfp", "Couple PFP API", "couple-pfp-api.vercel.app", FeatureGroup.WALLPAPERS, "https://couple-pfp-api.vercel.app", "/api/v1/couplepfp", paramKey = "", priority = 3))
        add(ProviderEndpoint("picsum_random", "Picsum HD Random", "picsum.photos", FeatureGroup.WALLPAPERS, "https://picsum.photos", "/1080/1920", paramKey = "random", priority = 4, expectsBinaryImage = true))
        add(ProviderEndpoint("bing_images", "Bing Images", "www.bing.com", FeatureGroup.WALLPAPERS, "https://www.bing.com", "/images/search", paramKey = "q", priority = 5))

        add(ProviderEndpoint("sra_animu", "SomeRandomAPI Animu", "api.some-random-api.com", FeatureGroup.ANIME, "https://api.some-random-api.com", "/animu/quote", paramKey = "", priority = 1))
        add(ProviderEndpoint("waifu_pics", "Waifu.pics SFW", "api.waifu.pics", FeatureGroup.ANIME, "https://api.waifu.pics", "/sfw/{q}", paramKey = "q", priority = 2, isPathTemplate = true))
        add(ProviderEndpoint("shizo_pies", "Shizo Pies", "api.shizo.top", FeatureGroup.ANIME, "https://api.shizo.top", "/pies/random", paramKey = "", priority = 3, expectsBinaryImage = true))
        add(ProviderEndpoint("adeel_genshin", "Adeel Genshin Build", "adeel-xtech-apis.vercel.app", FeatureGroup.ANIME, "https://adeel-xtech-apis.vercel.app", "/api/genshinbuild", paramKey = "q", priority = 4))
        // Existing TechXKamran Anime endpoints preserved as fallbacks & selectable providers
        add(ProviderEndpoint("techx_samehadaku_latest", "Samehadaku Latest", "techxkamran.onrender.com", FeatureGroup.ANIME, "https://techxkamran.onrender.com", "/api/anime/samehadaku/latest", paramKey = "", isPrimary = false, priority = 101))
        add(ProviderEndpoint("techx_samehadaku_detail", "Samehadaku Detail", "techxkamran.onrender.com", FeatureGroup.ANIME, "https://techxkamran.onrender.com", "/api/anime/samehadaku/detail", paramKey = "url", isPrimary = false, priority = 102))
        add(ProviderEndpoint("techx_samehadaku_dl", "Samehadaku Download", "techxkamran.onrender.com", FeatureGroup.ANIME, "https://techxkamran.onrender.com", "/api/anime/samehadaku/download", paramKey = "url", isPrimary = false, priority = 103))
        add(ProviderEndpoint("techx_otakudesu_search", "Otakudesu Search", "techxkamran.onrender.com", FeatureGroup.ANIME, "https://techxkamran.onrender.com", "/api/anime/otakudesu/search", paramKey = "q", isPrimary = false, priority = 104))
        add(ProviderEndpoint("techx_otakudesu_ongoing", "Otakudesu Ongoing", "techxkamran.onrender.com", FeatureGroup.ANIME, "https://techxkamran.onrender.com", "/api/anime/otakudesu/ongoing", paramKey = "", isPrimary = false, priority = 105))
        add(ProviderEndpoint("techx_otakudesu_detail", "Otakudesu Detail", "techxkamran.onrender.com", FeatureGroup.ANIME, "https://techxkamran.onrender.com", "/api/anime/otakudesu/detail", paramKey = "url", isPrimary = false, priority = 106))
        add(ProviderEndpoint("techx_otakudesu_dl", "Otakudesu Download", "techxkamran.onrender.com", FeatureGroup.ANIME, "https://techxkamran.onrender.com", "/api/anime/otakudesu/download", paramKey = "url", isPrimary = false, priority = 107))
        add(ProviderEndpoint("techx_oploverz_search", "Oploverz Search", "techxkamran.onrender.com", FeatureGroup.ANIME, "https://techxkamran.onrender.com", "/api/anime/oploverz-search", paramKey = "q", isPrimary = false, priority = 108))
        add(ProviderEndpoint("techx_oploverz_ongoing", "Oploverz Ongoing", "techxkamran.onrender.com", FeatureGroup.ANIME, "https://techxkamran.onrender.com", "/api/anime/oploverz-ongoing", paramKey = "", isPrimary = false, priority = 109))
        add(ProviderEndpoint("techx_oploverz_ep", "Oploverz Episode", "techxkamran.onrender.com", FeatureGroup.ANIME, "https://techxkamran.onrender.com", "/api/anime/oploverz-episode", paramKey = "url", isPrimary = false, priority = 110))
        add(ProviderEndpoint("techx_oploverz_dl", "Oploverz Download", "techxkamran.onrender.com", FeatureGroup.ANIME, "https://techxkamran.onrender.com", "/api/anime/oploverz-download", paramKey = "url", isPrimary = false, priority = 111))
        add(ProviderEndpoint("techx_auratail_search", "Auratail Search", "techxkamran.onrender.com", FeatureGroup.ANIME, "https://techxkamran.onrender.com", "/api/anime/auratail-search", paramKey = "q", isPrimary = false, priority = 112))
        add(ProviderEndpoint("techx_auratail_popular", "Auratail Popular", "techxkamran.onrender.com", FeatureGroup.ANIME, "https://techxkamran.onrender.com", "/api/anime/auratail-popular", paramKey = "", isPrimary = false, priority = 113))
        add(ProviderEndpoint("techx_auratail_latest", "Auratail Latest", "techxkamran.onrender.com", FeatureGroup.ANIME, "https://techxkamran.onrender.com", "/api/anime/auratail-latest", paramKey = "", isPrimary = false, priority = 114))
        add(ProviderEndpoint("techx_auratail_detail", "Auratail Detail", "techxkamran.onrender.com", FeatureGroup.ANIME, "https://techxkamran.onrender.com", "/api/anime/auratail-detail", paramKey = "url", isPrimary = false, priority = 115))
        add(ProviderEndpoint("techx_anichin_search", "Anichin Search", "techxkamran.onrender.com", FeatureGroup.ANIME, "https://techxkamran.onrender.com", "/api/anime/anichin-search", paramKey = "q", isPrimary = false, priority = 116))
        add(ProviderEndpoint("techx_identify_anime", "TechX Identify Anime", "techxkamran.onrender.com", FeatureGroup.ANIME, "https://techxkamran.onrender.com", "/api/tools/identify-anime", paramKey = "url", isPrimary = false, priority = 117))

        add(ProviderEndpoint("dadjoke", "I Can Haz Dad Joke", "icanhazdadjoke.com", FeatureGroup.MEMES, "https://icanhazdadjoke.com", "/", paramKey = "", priority = 1))
        add(ProviderEndpoint("uselessfacts", "Useless Facts", "uselessfacts.jsph.pl", FeatureGroup.MEMES, "https://uselessfacts.jsph.pl", "/api/v2/facts/random", paramKey = "", extraParams = mapOf("language" to "en"), priority = 2))
        add(ProviderEndpoint("adviceslip", "Advice Slip", "api.adviceslip.com", FeatureGroup.MEMES, "https://api.adviceslip.com", "/advice", paramKey = "", priority = 3))
        add(ProviderEndpoint("zenquotes", "ZenQuotes Random", "zenquotes.io", FeatureGroup.MEMES, "https://zenquotes.io", "/api/random", paramKey = "", priority = 4))
        add(ProviderEndpoint("opentdb_trivia", "OpenTDB Trivia", "opentdb.com", FeatureGroup.MEMES, "https://opentdb.com", "/api.php", paramKey = "", extraParams = mapOf("amount" to "3", "type" to "multiple"), priority = 5))
        add(ProviderEndpoint("nekos_fact", "Nekos Life Fact", "nekos.life", FeatureGroup.MEMES, "https://nekos.life", "/api/v2/fact", paramKey = "", priority = 6))
        add(ProviderEndpoint("adeel_meme", "Adeel Random Meme", "adeel-xtech-apis.vercel.app", FeatureGroup.MEMES, "https://adeel-xtech-apis.vercel.app", "/api/randommeme", paramKey = "", priority = 7))
        add(ProviderEndpoint("adeel_cekkhodam", "Adeel Cek Khodam", "adeel-xtech-apis.vercel.app", FeatureGroup.MEMES, "https://adeel-xtech-apis.vercel.app", "/api/cekkhodam", paramKey = "name", priority = 8))
        add(ProviderEndpoint("adeel_tahukahkamu", "Adeel Tahukah Kamu", "adeel-xtech-apis.vercel.app", FeatureGroup.MEMES, "https://adeel-xtech-apis.vercel.app", "/api/tahukahkamu", paramKey = "", priority = 9))
        add(ProviderEndpoint("adeel_viral_video", "Adeel Viral Video", "adeel-xtech-apis.vercel.app", FeatureGroup.MEMES, "https://adeel-xtech-apis.vercel.app", "/api/viral-video", paramKey = "", priority = 10))
        add(ProviderEndpoint("shizo_cheems", "Shizo Cheems Meme", "shizoapi.onrender.com", FeatureGroup.MEMES, "https://shizoapi.onrender.com", "/api/memes/cheems", paramKey = "", priority = 11, authKeySlot = AuthKeySlot.SHIZO, expectsBinaryImage = true))
        add(ProviderEndpoint("shizo_dare", "Shizo Dare", "shizoapi.onrender.com", FeatureGroup.MEMES, "https://shizoapi.onrender.com", "/api/texts/dare", paramKey = "", priority = 12, authKeySlot = AuthKeySlot.SHIZO))
        add(ProviderEndpoint("shizo_truth", "Shizo Truth", "shizoapi.onrender.com", FeatureGroup.MEMES, "https://shizoapi.onrender.com", "/api/texts/truth", paramKey = "", priority = 13, authKeySlot = AuthKeySlot.SHIZO))
        add(ProviderEndpoint("shizo_flirt", "Shizo Flirt", "shizoapi.onrender.com", FeatureGroup.MEMES, "https://shizoapi.onrender.com", "/api/texts/flirt", paramKey = "", priority = 14, authKeySlot = AuthKeySlot.SHIZO))
        add(ProviderEndpoint("shizo_lovenight", "Shizo Goodnight", "shizoapi.onrender.com", FeatureGroup.MEMES, "https://shizoapi.onrender.com", "/api/texts/lovenight", paramKey = "", priority = 15, authKeySlot = AuthKeySlot.SHIZO))
        add(ProviderEndpoint("shizo_quotes", "Shizo Quotes", "shizoapi.onrender.com", FeatureGroup.MEMES, "https://shizoapi.onrender.com", "/api/texts/quotes", paramKey = "", priority = 16, authKeySlot = AuthKeySlot.SHIZO))
        add(ProviderEndpoint("shizo_shayari", "Shizo Shayari", "shizoapi.onrender.com", FeatureGroup.MEMES, "https://shizoapi.onrender.com", "/api/texts/shayari", paramKey = "", priority = 17, authKeySlot = AuthKeySlot.SHIZO))
        add(ProviderEndpoint("david_dare", "DavidCyril Dare", "apis.davidcyriltech.my.id", FeatureGroup.MEMES, "https://apis.davidcyriltech.my.id", "/dare", paramKey = "", priority = 18))
        add(ProviderEndpoint("david_truth", "DavidCyril Truth", "apis.davidcyriltech.my.id", FeatureGroup.MEMES, "https://apis.davidcyriltech.my.id", "/truth", paramKey = "", priority = 19))
        add(ProviderEndpoint("prince_roseday", "PrinceTech RoseDay", "api.princetechn.com", FeatureGroup.MEMES, "https://api.princetechn.com", "/api/fun/roseday", paramKey = "", priority = 20, authKeySlot = AuthKeySlot.PRINCETECH))
        add(ProviderEndpoint("hotwheels_random", "Hot Wheels Rugs", "hot-wheels-rugs.onrender.com", FeatureGroup.MEMES, "https://hot-wheels-rugs.onrender.com", "/api/random", paramKey = "", priority = 21))
        add(ProviderEndpoint("giphy_search", "Giphy GIF Search", "api.giphy.com", FeatureGroup.MEMES, "https://api.giphy.com", "/v1/gifs/search", paramKey = "q", priority = 22, authKeySlot = AuthKeySlot.GIPHY, authParamName = "api_key"))

        add(ProviderEndpoint("sra_heart", "SRA Heart Overlay", "api.some-random-api.com", FeatureGroup.EFFECTS, "https://api.some-random-api.com", "/canvas/misc/heart", paramKey = "avatar", priority = 1, expectsBinaryImage = true))
        add(ProviderEndpoint("sra_stupid", "SRA Stupid Dog", "api.some-random-api.com", FeatureGroup.EFFECTS, "https://api.some-random-api.com", "/canvas/misc/its-so-stupid", paramKey = "dog", priority = 2, expectsBinaryImage = true))
        add(ProviderEndpoint("sra_simp", "SRA SimpCard", "some-random-api.com", FeatureGroup.EFFECTS, "https://some-random-api.com", "/canvas/misc/simpcard", paramKey = "avatar", priority = 3, expectsBinaryImage = true))
        add(ProviderEndpoint("sra_wasted", "SRA Wasted Overlay", "some-random-api.com", FeatureGroup.EFFECTS, "https://some-random-api.com", "/canvas/overlay/wasted", paramKey = "avatar", priority = 4, expectsBinaryImage = true))
        add(ProviderEndpoint("sra_tweet", "SRA Fake Tweet", "api.some-random-api.com", FeatureGroup.EFFECTS, "https://api.some-random-api.com", "/canvas/misc/tweet", paramKey = "comment", extraParams = mapOf("displayname" to "Rauf", "username" to "rauf_browser"), priority = 5, expectsBinaryImage = true))
        add(ProviderEndpoint("sra_yt_comment", "SRA YT Comment", "api.some-random-api.com", FeatureGroup.EFFECTS, "https://api.some-random-api.com", "/canvas/misc/youtube-comment", paramKey = "comment", extraParams = mapOf("username" to "Rauf"), priority = 6, expectsBinaryImage = true))
        add(ProviderEndpoint("sra_namecard", "SRA Namecard", "api.some-random-api.com", FeatureGroup.EFFECTS, "https://api.some-random-api.com", "/canvas/misc/namecard", paramKey = "username", priority = 7, expectsBinaryImage = true))
        add(ProviderEndpoint("adeel_qc", "Adeel Quote Sticker (QC)", "adeel-xtech-apis.vercel.app", FeatureGroup.EFFECTS, "https://adeel-xtech-apis.vercel.app", "/api/qc", paramKey = "text", priority = 8, expectsBinaryImage = true))
        add(ProviderEndpoint("adeel_tweet", "Adeel Tweet Card", "adeel-xtech-apis.vercel.app", FeatureGroup.EFFECTS, "https://adeel-xtech-apis.vercel.app", "/api/tweet", paramKey = "text", priority = 9, expectsBinaryImage = true))
        add(ProviderEndpoint("adeel_welcome", "Adeel Welcome Card", "adeel-xtech-apis.vercel.app", FeatureGroup.EFFECTS, "https://adeel-xtech-apis.vercel.app", "/api/welcome-goodbye-image", paramKey = "username", priority = 10, expectsBinaryImage = true))
        add(ProviderEndpoint("memegen_custom", "Memegen Custom Meme", "api.memegen.link", FeatureGroup.EFFECTS, "https://api.memegen.link", "/images/custom/-/{q}.png", paramKey = "q", priority = 11, isPathTemplate = true, expectsBinaryImage = true))
        add(ProviderEndpoint("raganork_attp", "Raganork ATTP Sticker", "raganork-api.onrender.com", FeatureGroup.EFFECTS, "https://raganork-api.onrender.com", "/api/attp", paramKey = "text", priority = 12, expectsBinaryImage = true))
        add(ProviderEndpoint("weeb_quotly", "Weeb Quotly Sticker", "weeb-api.vercel.app", FeatureGroup.EFFECTS, "https://weeb-api.vercel.app", "/quotly", paramKey = "text", priority = 13, expectsBinaryImage = true))
        add(ProviderEndpoint("lyo_quote", "Lyo Quote Generator", "bot.lyo.su", FeatureGroup.EFFECTS, "https://bot.lyo.su", "/quote/generate", method = "POST", paramKey = "text", priority = 14))
        add(ProviderEndpoint("pixelcut_upscale", "Pixelcut Upscaler", "api2.pixelcut.app", FeatureGroup.EFFECTS, "https://api2.pixelcut.app", "/image/upscale/v1", method = "POST", paramKey = "url", priority = 15))
        add(ProviderEndpoint("ezgif_webp", "Ezgif Video-to-WebP", "api.ezgif.com", FeatureGroup.EFFECTS, "https://api.ezgif.com", "/video-to-webp", paramKey = "url", priority = 16))
        add(ProviderEndpoint("xteam_sticker", "XTeam Sticker WM", "api.xteam.xyz", FeatureGroup.EFFECTS, "https://api.xteam.xyz", "/sticker/wm", paramKey = "url", priority = 17))

        // Register all 37 Ephoto360 logo/text maker styles
        EPHOTO_EFFECTS.forEachIndexed { idx, (title, slug) ->
            add(ProviderEndpoint("ephoto_${idx + 1}", "Ephoto: $title", "en.ephoto360.com", FeatureGroup.LOGOS, "https://en.ephoto360.com", slug, paramKey = "text", priority = idx + 1, description = "Ephoto360 $title online text effect"))
        }

        // Translate & NLP
        add(ProviderEndpoint("google_translate", "Google Translate", "translate.googleapis.com", FeatureGroup.TRANSLATE, "https://translate.googleapis.com", "/translate_a/single", paramKey = "q", extraParams = mapOf("client" to "gtx", "sl" to "auto", "tl" to "en", "dt" to "t"), priority = 1))
        add(ProviderEndpoint("adeel_translate", "Adeel Translate", "adeel-xtech-apis.vercel.app", FeatureGroup.TRANSLATE, "https://adeel-xtech-apis.vercel.app", "/api/translate", paramKey = "text", priority = 2))
        add(ProviderEndpoint("dreaded_translate", "Dreaded Translate", "api.dreaded.site", FeatureGroup.TRANSLATE, "https://api.dreaded.site", "/api/translate", paramKey = "text", priority = 3))
        add(ProviderEndpoint("mymemory_translate", "MyMemory Translate", "api.mymemory.translated.net", FeatureGroup.TRANSLATE, "https://api.mymemory.translated.net", "/get", paramKey = "q", extraParams = mapOf("langpair" to "auto|en"), priority = 4))
        add(ProviderEndpoint("techx_cf_translate", "TechX CF Translation (Fallback)", "techxkamran.onrender.com", FeatureGroup.TRANSLATE, "https://techxkamran.onrender.com", "/api/cf/translation", paramKey = "text", isPrimary = false, priority = 101))
        add(ProviderEndpoint("techx_cf_sentiment", "TechX CF Sentiment", "techxkamran.onrender.com", FeatureGroup.TRANSLATE, "https://techxkamran.onrender.com", "/api/cf/sentiment", paramKey = "text", isPrimary = false, priority = 102))
        add(ProviderEndpoint("techx_cf_imgclass", "TechX CF Image Classify", "techxkamran.onrender.com", FeatureGroup.TRANSLATE, "https://techxkamran.onrender.com", "/api/cf/image-classification", paramKey = "url", isPrimary = false, priority = 103))
        add(ProviderEndpoint("techx_cf_embed", "TechX CF Embeddings", "techxkamran.onrender.com", FeatureGroup.TRANSLATE, "https://techxkamran.onrender.com", "/api/cf/embedding", paramKey = "text", isPrimary = false, priority = 104))

        // QR & Screenshot
        add(ProviderEndpoint("qrtag_qr", "QRTag PNG Generator", "www.qrtag.net", FeatureGroup.QR, "https://www.qrtag.net", "/api/qr_8.png", paramKey = "url", priority = 1, expectsBinaryImage = true))
        add(ProviderEndpoint("adeel_qr", "Adeel QR Generator", "adeel-xtech-apis.vercel.app", FeatureGroup.QR, "https://adeel-xtech-apis.vercel.app", "/api/qrcodegenerator", paramKey = "text", priority = 2, expectsBinaryImage = true))

        add(ProviderEndpoint("siputzx_ssweb", "Siputzx Screenshot", "api.siputzx.my.id", FeatureGroup.SCREENSHOT, "https://api.siputzx.my.id", "/api/tools/ssweb", paramKey = "url", priority = 1, expectsBinaryImage = true))
        add(ProviderEndpoint("adeel_ssweb", "Adeel Screenshot", "adeel-xtech-apis.vercel.app", FeatureGroup.SCREENSHOT, "https://adeel-xtech-apis.vercel.app", "/api/ssweb", paramKey = "url", priority = 2, expectsBinaryImage = true))

        // Web & Social Tools (Primary + TechXKamran Fallbacks)
        add(ProviderEndpoint("tinyurl_short", "TinyURL Shortener", "tinyurl.com", FeatureGroup.TOOLS, "https://tinyurl.com", "/api-create.php", paramKey = "url", priority = 1))
        add(ProviderEndpoint("gifted_fancy", "GiftedTech Fancy Text", "api.giftedtech.co.ke", FeatureGroup.TOOLS, "https://api.giftedtech.co.ke", "/api/tools/fancy", paramKey = "text", priority = 2, authKeySlot = AuthKeySlot.GIFTEDTECH))
        add(ProviderEndpoint("qaz_fancy", "Qaz.wtf Fancy Text", "qaz.wtf", FeatureGroup.TOOLS, "http://qaz.wtf", "/u/convert.cgi", paramKey = "text", priority = 3))
        add(ProviderEndpoint("david_tempmail", "DavidCyril TempMail", "apis.davidcyriltech.my.id", FeatureGroup.TOOLS, "https://apis.davidcyriltech.my.id", "/temp-mail", paramKey = "", priority = 4))
        add(ProviderEndpoint("david_tempmail_inbox", "DavidCyril TempMail Inbox", "apis.davidcyriltech.my.id", FeatureGroup.TOOLS, "https://apis.davidcyriltech.my.id", "/temp-mail/inbox", paramKey = "id", priority = 5))
        add(ProviderEndpoint("prince_ttstalk", "PrinceTech TikTok Stalk", "api.princetechn.com", FeatureGroup.TOOLS, "https://api.princetechn.com", "/api/stalk/tiktokstalk", paramKey = "username", priority = 6, authKeySlot = AuthKeySlot.PRINCETECH))
        add(ProviderEndpoint("adeel_pastebin", "Adeel Pastebin", "adeel-xtech-apis.vercel.app", FeatureGroup.TOOLS, "https://adeel-xtech-apis.vercel.app", "/api/getpastebin", paramKey = "url", priority = 7))
        add(ProviderEndpoint("adeel_web2zip", "Adeel Web2Zip", "adeel-xtech-apis.vercel.app", FeatureGroup.TOOLS, "https://adeel-xtech-apis.vercel.app", "/api/web2zip", paramKey = "url", priority = 8))
        add(ProviderEndpoint("adeel_groups", "Adeel Search Groups", "adeel-xtech-apis.vercel.app", FeatureGroup.TOOLS, "https://adeel-xtech-apis.vercel.app", "/api/searchgroups", paramKey = "q", priority = 9))
        add(ProviderEndpoint("adeel_sinhalasub", "Adeel SinhalaSub", "adeel-xtech-apis.vercel.app", FeatureGroup.TOOLS, "https://adeel-xtech-apis.vercel.app", "/api/sinhalasub", paramKey = "q", priority = 10))
        add(ProviderEndpoint("meloboom_ringtone", "Meloboom Ringtones", "meloboom.com", FeatureGroup.TOOLS, "https://meloboom.com", "/en/search/{q}", paramKey = "q", priority = 11, isPathTemplate = true))
        add(ProviderEndpoint("telegram_stickerset", "Telegram StickerSet", "api.telegram.org", FeatureGroup.TOOLS, "https://api.telegram.org", "/bot{token}/getStickerSet", paramKey = "name", priority = 12, authKeySlot = AuthKeySlot.TELEGRAM))
        add(ProviderEndpoint("imgbb_upload", "ImgBB Image Upload", "api.imgbb.com", FeatureGroup.TOOLS, "https://api.imgbb.com", "/1/upload", method = "POST", paramKey = "image", priority = 13, authKeySlot = AuthKeySlot.IMGBB, authParamName = "key"))
        add(ProviderEndpoint("quax_upload", "Qu.ax File Upload", "qu.ax", FeatureGroup.TOOLS, "https://qu.ax", "/upload.php", method = "POST", paramKey = "files[]", priority = 14))
        add(ProviderEndpoint("flonime_upload", "Flonime Upload", "flonime.my.id", FeatureGroup.TOOLS, "https://flonime.my.id", "/upload", method = "POST", paramKey = "file", priority = 15))
        add(ProviderEndpoint("empiretech_upload", "EmpireTech CDN Upload", "cdn.empiretech.biz.id", FeatureGroup.TOOLS, "https://cdn.empiretech.biz.id", "/api/upload.php", method = "POST", paramKey = "file", priority = 16))
        add(ProviderEndpoint("truecaller_search", "Truecaller Lookup", "search5-noneu.truecaller.com", FeatureGroup.TOOLS, "https://search5-noneu.truecaller.com", "/v2/search", paramKey = "q", priority = 17))
        // Additional bot catalog hosts registered for status monitoring & reachability
        add(ProviderEndpoint("fgmods_base", "FGMods API", "api-fgmods.ddns.net", FeatureGroup.TOOLS, "https://api-fgmods.ddns.net", "/", paramKey = "", priority = 18))
        add(ProviderEndpoint("akuari_base", "Akuari API", "api.akuari.my.id", FeatureGroup.TOOLS, "https://api.akuari.my.id", "/", paramKey = "", priority = 19))
        add(ProviderEndpoint("dhamz_base", "Dhamzxploit API", "api.dhamzxploit.my.id", FeatureGroup.TOOLS, "https://api.dhamzxploit.my.id", "/", paramKey = "", priority = 20))
        add(ProviderEndpoint("lolhuman_base", "LolHuman API", "api.lolhuman.xyz", FeatureGroup.TOOLS, "https://api.lolhuman.xyz", "/", paramKey = "", priority = 21))
        add(ProviderEndpoint("neoxr_base", "Neoxr API", "api.neoxr.my.id", FeatureGroup.TOOLS, "https://api.neoxr.my.id", "/", paramKey = "", priority = 22))
        add(ProviderEndpoint("apimu_base", "Apimu API", "apimu.my.id", FeatureGroup.TOOLS, "https://apimu.my.id", "/", paramKey = "", priority = 23))
        add(ProviderEndpoint("bochil_base", "Bochil API", "bochil.ddns.net", FeatureGroup.TOOLS, "http://bochil.ddns.net", "/", paramKey = "", priority = 24))
        add(ProviderEndpoint("fgnrtm_base", "FG-NRTM API", "fg-nrtm.ddns.net", FeatureGroup.TOOLS, "https://fg-nrtm.ddns.net", "/", paramKey = "", priority = 25))
        add(ProviderEndpoint("violetics_base", "Violetics API", "violetics.pw", FeatureGroup.TOOLS, "https://violetics.pw", "/", paramKey = "", priority = 26))
        add(ProviderEndpoint("zenzapis_base", "ZenzAPIs", "zenzapis.xyz", FeatureGroup.TOOLS, "https://zenzapis.xyz", "/", paramKey = "", priority = 27))
        // Existing TechXKamran Tools preserved as fallbacks
        add(ProviderEndpoint("techx_shorturl", "TechX ShortURL (Fallback)", "techxkamran.onrender.com", FeatureGroup.TOOLS, "https://techxkamran.onrender.com", "/api/tools/shorturl", paramKey = "url", isPrimary = false, priority = 101))
        add(ProviderEndpoint("techx_base64", "TechX Base64→Text", "techxkamran.onrender.com", FeatureGroup.TOOLS, "https://techxkamran.onrender.com", "/api/tools/base642text", paramKey = "text", isPrimary = false, priority = 102))
        add(ProviderEndpoint("techx_binary", "TechX Binary→Text", "techxkamran.onrender.com", FeatureGroup.TOOLS, "https://techxkamran.onrender.com", "/api/tools/binary2text", paramKey = "text", isPrimary = false, priority = 103))
        add(ProviderEndpoint("techx_subdomains", "TechX Subdomains", "techxkamran.onrender.com", FeatureGroup.TOOLS, "https://techxkamran.onrender.com", "/api/tools/subdomains", paramKey = "domain", isPrimary = false, priority = 104))
        add(ProviderEndpoint("techx_fake_data", "TechX Fake Data", "techxkamran.onrender.com", FeatureGroup.TOOLS, "https://techxkamran.onrender.com", "/api/tools/fake-data", paramKey = "type", isPrimary = false, priority = 105))
        add(ProviderEndpoint("techx_voices", "TechX Voices", "techxkamran.onrender.com", FeatureGroup.TOOLS, "https://techxkamran.onrender.com", "/api/tools/voices", paramKey = "", isPrimary = false, priority = 106))
    }

    val AI_MODELS: List<AiModelSpec> = listOf(
        // Default flagship ChatGPT models (DuckDuckGo-style clean Rauf.ai lineup)
        AiModelSpec(
            id = "chatgpt_gpt6",
            name = "GPT-6",
            brandName = "ChatGPT",
            tierBadge = "DEFAULT",
            providerName = "Rauf.ai • ChatGPT",
            description = "Default flagship model for fast, deep everyday answers",
            isPrimary = true,
            isImageModel = false,
            providerEndpointId = "eliteprotech_chatgpt",
            logoStyle = AiLogoStyle.OPENAI_GPT
        ),
        AiModelSpec(
            id = "chatgpt_gpt6_extra",
            name = "GPT-6 Extra",
            brandName = "ChatGPT",
            tierBadge = "PRO",
            providerName = "Rauf.ai • Extended Reasoning",
            description = "High-depth reasoning for complex code, math & analysis",
            isPrimary = true,
            isImageModel = false,
            providerEndpointId = "groq_openai",
            logoStyle = AiLogoStyle.OPENAI_EXTRA
        ),
        AiModelSpec(
            id = "rauf_gpt6_ultra",
            name = "GPT-6 Ultra",
            brandName = "Rauf.ai",
            tierBadge = "ULTRA",
            providerName = "Rauf.ai • Deep Engine",
            description = "Maximum accuracy & webpage synthesis by Rauf.ai",
            isPrimary = false,
            isImageModel = false,
            providerEndpointId = "techx_gemini",
            logoStyle = AiLogoStyle.RAUF_CROWN
        ),
        AiModelSpec(
            id = "rauf_gpt6_mini",
            name = "GPT-6 Mini",
            brandName = "ChatGPT",
            tierBadge = "FAST",
            providerName = "Rauf.ai • Instant",
            description = "Lightweight ultra-low-latency quick answers",
            isPrimary = false,
            isImageModel = false,
            providerEndpointId = "techx_gemini_lite",
            logoStyle = AiLogoStyle.RAUF_MINI
        ),
        AiModelSpec(
            id = "claude_opus_45",
            name = "Claude Opus 4.5",
            brandName = "Anthropic",
            tierBadge = "PRO",
            providerName = "Rauf.ai • Writing & Code",
            description = "Nuanced writing, architecture & structured reasoning",
            isPrimary = false,
            isImageModel = false,
            providerEndpointId = "techx_powerbrain",
            logoStyle = AiLogoStyle.CLAUDE_SPARK
        ),
        AiModelSpec(
            id = "gemini_25_pro",
            name = "Gemini 2.5 Pro",
            brandName = "Google",
            tierBadge = "PRO",
            providerName = "Rauf.ai • Web Research",
            description = "Broad web knowledge & multi-step explanations",
            isPrimary = false,
            isImageModel = false,
            providerEndpointId = "techx_bard",
            logoStyle = AiLogoStyle.GEMINI_STAR
        ),
        AiModelSpec(
            id = "llama_4_maverick",
            name = "Llama 4 Maverick",
            brandName = "Meta",
            tierBadge = "OPEN",
            providerName = "Rauf.ai • Open Edge LLM",
            description = "Fast open-weights model for concise answers",
            isPrimary = false,
            isImageModel = false,
            providerEndpointId = "techx_cf_chat",
            logoStyle = AiLogoStyle.LLAMA_LOOP
        ),
        AiModelSpec(
            id = "rauf_muslim_ai",
            name = "Rauf Islamic AI",
            brandName = "Rauf.ai",
            tierBadge = "SPECIAL",
            providerName = "Rauf.ai • Knowledge",
            description = "Islamic studies, Quran & scholarly references",
            isPrimary = false,
            isImageModel = false,
            providerEndpointId = "techx_muslimai",
            logoStyle = AiLogoStyle.CRESCENT
        ),
        AiModelSpec(
            id = "rauf_gita_ai",
            name = "Rauf Gita AI",
            brandName = "Rauf.ai",
            tierBadge = "SPECIAL",
            providerName = "Rauf.ai • Philosophy",
            description = "Bhagavad Gita verses & philosophical Q&A",
            isPrimary = false,
            isImageModel = false,
            providerEndpointId = "techx_gita",
            logoStyle = AiLogoStyle.LOTUS
        ),
        AiModelSpec(
            id = "rauf_bible_ai",
            name = "Rauf Bible AI",
            brandName = "Rauf.ai",
            tierBadge = "SPECIAL",
            providerName = "Rauf.ai • Scripture",
            description = "Bible scripture & theology assistant",
            isPrimary = false,
            isImageModel = false,
            providerEndpointId = "techx_bibleai",
            logoStyle = AiLogoStyle.SCRIPTURE
        ),
        AiModelSpec(
            id = "rauf_imagine_pro",
            name = "Rauf Imagine Pro",
            brandName = "Rauf.ai",
            tierBadge = "IMAGE",
            providerName = "Rauf.ai • Vision Studio",
            description = "Primary text-to-image synthesis engine",
            isPrimary = true,
            isImageModel = true,
            providerEndpointId = "shizo_imagine",
            logoStyle = AiLogoStyle.VISION_LENS
        ),
        AiModelSpec(
            id = "rauf_flux_ultra",
            name = "Flux Ultra",
            brandName = "Rauf.ai",
            tierBadge = "IMAGE",
            providerName = "Rauf.ai • High-Detail Image",
            description = "Photorealistic high-detail AI image generator",
            isPrimary = false,
            isImageModel = true,
            providerEndpointId = "techx_flux",
            logoStyle = AiLogoStyle.VISION_LENS
        )
    )

    fun byId(id: String): ProviderEndpoint? = ALL_PROVIDERS.firstOrNull { it.id == id }

    fun orderedProvidersFor(
        feature: FeatureGroup,
        disabledIds: Set<String> = emptySet(),
        customOrder: List<String> = emptyList()
    ): List<ProviderEndpoint> {
        val base = ALL_PROVIDERS.filter { it.feature == feature && it.id !in disabledIds }
        val orderMap = customOrder.withIndex().associate { it.value to it.index }
        return base.sortedWith(
            compareBy<ProviderEndpoint> { orderMap[it.id] ?: Int.MAX_VALUE }
                .thenBy { if (it.isPrimary) 0 else 1 }
                .thenBy { it.priority }
        )
    }

    fun detectDownloaderFeature(rawUrl: String, audioOnly: Boolean = false): FeatureGroup {
        val u = rawUrl.trim().lowercase()
        val host = u.substringAfter("://", u).substringBefore("/").substringBefore("?").removePrefix("www.")
        return when {
            host.contains("youtube.com") || host == "youtu.be" || host.endsWith(".youtu.be") ->
                if (audioOnly) FeatureGroup.YTMP3 else FeatureGroup.YTDL
            host.contains("instagram.com") || host == "instagr.am" -> FeatureGroup.IGDL
            host.contains("facebook.com") || host == "fb.watch" || host == "fb.com" -> FeatureGroup.FBDL
            host.contains("tiktok.com") -> FeatureGroup.TTDL
            host == "x.com" || host.endsWith(".x.com") || host.contains("twitter.com") -> FeatureGroup.TWITTER
            host.contains("pinterest.") || host == "pin.it" -> FeatureGroup.PINTEREST
            host.contains("spotify.com") -> FeatureGroup.SPOTIFY
            host.contains("mediafire.com") || host.contains("mega.nz") || host.contains("sfile.") ||
                host.contains("soundcloud.com") || host.contains("threads.net") || host.contains("videy.co") -> FeatureGroup.MEDIAFIRE
            host.contains("capcut.com") -> FeatureGroup.CAPCUT
            host.contains("terabox") || host.contains("1024tera") || host.contains("drive.google.com") -> FeatureGroup.TERABOX
            !u.startsWith("http://") && !u.startsWith("https://") -> FeatureGroup.APK
            else -> FeatureGroup.AIO
        }
    }
}
