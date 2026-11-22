package com.example.ui.screens

import android.os.Build
import android.webkit.WebView
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.BuildConfig
import com.example.R
import com.example.ui.components.GlassCard
import com.example.ui.components.SubScreenTopBar
import com.example.ui.theme.MonoFontFamily

@Composable
fun PrivacyPolicyScreen(onBack: () -> Unit) {
    BackHandler(onBack = onBack)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .navigationBarsPadding()
    ) {
        SubScreenTopBar(
            title = "Privacy Policy",
            subtitle = "Rauf Browser • Developer: RAUF",
            onBack = onBack
        )

        LazyColumn(
            modifier = Modifier.weight(1f),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            item {
                PolicyBlock(
                    heading = "1. Local-First Data Architecture",
                    body = "Rauf Browser stores your bookmarks, browsing history, homepage shortcuts, download logs, and browser settings locally on your Android device using Room and DataStore. We do not operate a tracking server or sell your browsing history."
                )
            }
            item {
                PolicyBlock(
                    heading = "2. Incognito Mode Transparency",
                    body = "Private tabs do not write to local browsing history and clear session cookies, cache, and form data when all private tabs are closed. Important: Incognito mode does NOT make you anonymous to the websites you visit, your employer or school network, Wi-Fi operators, or your internet service provider (ISP)."
                )
            }
            item {
                PolicyBlock(
                    heading = "3. Security & SSL Certificate Protection",
                    body = "Rauf Browser uses Android's Chromium-based WebView engine, enforces HTTPS-First upgrades when enabled, blocks mixed content by default, and never bypasses invalid or expired SSL certificates."
                )
            }
            item {
                PolicyBlock(
                    heading = "4. Optional AI Hub & Web Tool Endpoints",
                    body = "Only when you explicitly open the AI Hub or Rauf API Hub and run a request does Rauf Browser send your prompt or query parameters to your configured external API backend (techxkamran.onrender.com) to retrieve AI responses, translations, currency rates, tool outputs, or media lookup metadata."
                )
            }
            item {
                PolicyBlock(
                    heading = "5. Least-Privilege Android Permissions",
                    body = "Rauf Browser only requests optional runtime permissions (Location, Camera, Microphone, Notifications) when you enable them and a website or download action specifically asks for access. File uploads use Android's zero-permission system document picker."
                )
            }
        }
    }
}

@Composable
fun AboutBrowserScreen(onBack: () -> Unit) {
    BackHandler(onBack = onBack)

    val webViewVersion = remember {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val pkg = WebView.getCurrentWebViewPackage()
            if (pkg != null) "${pkg.packageName} (${pkg.versionName})" else "Android System WebView"
        } else {
            "Android System WebView (Chromium)"
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .navigationBarsPadding()
    ) {
        SubScreenTopBar(
            title = "About Rauf Browser",
            subtitle = "Developed by RAUF",
            onBack = onBack
        )

        LazyColumn(
            modifier = Modifier.weight(1f),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            item {
                GlassCard(
                    shape = RoundedCornerShape(24.dp),
                    accentHighlight = true,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Image(
                            painter = painterResource(id = R.drawable.rauf_browser_icon_fg_1795338224771),
                            contentDescription = "Rauf Browser icon",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier
                                .size(84.dp)
                                .clip(RoundedCornerShape(24.dp))
                        )
                        Spacer(Modifier.height(14.dp))
                        Text(
                            text = "RAUF BROWSER",
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Version ${BuildConfig.VERSION_NAME} (Build ${BuildConfig.VERSION_CODE})",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Spacer(Modifier.height(4.dp))
                        Text(
                            text = "Developer: RAUF",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            item {
                GlassCard(shape = RoundedCornerShape(20.dp), modifier = Modifier.fillMaxWidth()) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        AboutSpecRow("Rendering Engine", "Chromium-based Android WebView")
                        AboutSpecRow("WebView Package", webViewVersion)
                        AboutSpecRow("Application ID", BuildConfig.APPLICATION_ID)
                        AboutSpecRow("Android API", "API ${Build.VERSION.SDK_INT} (Min 24 / Target 36)")
                        AboutSpecRow("SSL Policy", "Strict — Certificate errors never bypassed")
                        AboutSpecRow("Mixed Content", "Blocked by default")
                        AboutSpecRow("Local Persistence", "Room SQLite + Preferences DataStore")
                    }
                }
            }
        }
    }
}

@Composable
private fun PolicyBlock(heading: String, body: String) {
    GlassCard(shape = RoundedCornerShape(20.dp), modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = heading,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.primary
            )
            Spacer(Modifier.height(6.dp))
            Text(
                text = body,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

@Composable
private fun AboutSpecRow(label: String, value: String) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium.copy(fontFamily = MonoFontFamily),
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}
