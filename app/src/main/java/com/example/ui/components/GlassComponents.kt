package com.example.ui.components

import android.graphics.Bitmap
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.R
import com.example.data.local.HomepageWallpaper
import com.example.ui.theme.DarkPurple
import com.example.ui.theme.DeepBlack
import com.example.ui.theme.ElectricPurple
import com.example.ui.theme.ElectricPurpleBright
import com.example.ui.theme.IncognitoAccent
import com.example.ui.theme.IncognitoBg
import com.example.ui.theme.LocalRaufGlassPalette

@Composable
fun GlassCard(
    modifier: Modifier = Modifier,
    shape: Shape = RoundedCornerShape(20.dp),
    strong: Boolean = false,
    accentHighlight: Boolean = false,
    content: @Composable BoxScope.() -> Unit
) {
    val glass = LocalRaufGlassPalette.current
    val fillColor = if (strong) glass.glassFillStrong else glass.glassFill

    if (glass.isLiquidGlass) {
        val borderBrush = Brush.linearGradient(
            colors = if (accentHighlight) {
                listOf(
                    glass.accentGlow.copy(alpha = 0.8f),
                    glass.glassHighlight,
                    glass.accentGlow.copy(alpha = 0.3f)
                )
            } else {
                listOf(
                    glass.glassHighlight,
                    glass.glassBorder,
                    glass.glassBorder.copy(alpha = 0.16f)
                )
            }
        )
        Box(
            modifier = modifier
                .clip(shape)
                .background(fillColor)
                .border(width = 1.dp, brush = borderBrush, shape = shape),
            content = content
        )
    } else {
        val borderColor = if (accentHighlight) {
            glass.accentGlow.copy(alpha = 0.45f)
        } else {
            glass.glassBorder.copy(alpha = 0.75f)
        }
        Box(
            modifier = modifier
                .clip(shape)
                .background(fillColor)
                .border(width = 0.75.dp, color = borderColor, shape = shape),
            content = content
        )
    }
}

@Composable
fun WallpaperBackground(
    wallpaper: HomepageWallpaper,
    isIncognito: Boolean,
    modifier: Modifier = Modifier
) {
    val glass = LocalRaufGlassPalette.current
    val isDark = glass.isDark

    Box(modifier = modifier.fillMaxSize()) {
        if (isIncognito) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.background)
            )
            return@Box
        }

        // On non-Liquid-Glass themes with CLEAN/AMOLED_PURE, render an ultra-clean DuckDuckGo-style canvas
        if (!glass.isLiquidGlass && wallpaper == HomepageWallpaper.AMOLED_PURE) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.background)
            )
            return@Box
        }

        when (wallpaper) {
            HomepageWallpaper.NEBULA -> {
                Image(
                    painter = painterResource(id = R.drawable.img_wallpaper_nebula_1795338222100),
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.verticalGradient(
                                colors = if (isDark) {
                                    listOf(Color(0xA605040A), Color(0x7705040A), Color(0xD905040A))
                                } else {
                                    listOf(Color(0xDDFAF8FF), Color(0xBBF3EEFC), Color(0xEEFAF8FF))
                                }
                            )
                        )
                )
            }
            HomepageWallpaper.PRISM -> {
                Image(
                    painter = painterResource(id = R.drawable.img_wallpaper_prism_1795338223498),
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.verticalGradient(
                                colors = if (isDark) {
                                    listOf(Color(0xAD040308), Color(0x77040308), Color(0xE0040308))
                                } else {
                                    listOf(Color(0xDDF7F5FC), Color(0xBBEDE8FA), Color(0xEEF7F5FC))
                                }
                            )
                        )
                )
            }
            HomepageWallpaper.FROST -> {
                Image(
                    painter = painterResource(id = R.drawable.img_wallpaper_frost_1795338222332),
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(if (isDark) Color(0xBA090712) else Color(0x55FFFFFF))
                )
            }
            HomepageWallpaper.AMOLED_PURE -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            if (glass.isLiquidGlass) {
                                Brush.verticalGradient(
                                    listOf(Color(0xFF140B29), Color(0xFF080712), Color(0xFF05040A))
                                )
                            } else {
                                Brush.verticalGradient(
                                    listOf(MaterialTheme.colorScheme.background, MaterialTheme.colorScheme.background)
                                )
                            }
                        )
                )
            }
            HomepageWallpaper.GRADIENT_VIOLET -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.verticalGradient(
                                colors = if (isDark) {
                                    listOf(DarkPurple, Color(0xFF10071F), DeepBlack)
                                } else {
                                    listOf(Color(0xFFE9D8FD), Color(0xFFF6F2FC), Color(0xFFFFFFFF))
                                }
                            )
                        )
                )
            }
        }
    }
}

@Composable
fun FaviconBadge(
    title: String,
    favicon: Bitmap? = null,
    accentColor: Color = ElectricPurple,
    size: Dp = 42.dp,
    modifier: Modifier = Modifier
) {
    val letter = title.trim().firstOrNull()?.uppercaseChar()?.toString() ?: "W"
    Box(
        modifier = modifier
            .size(size)
            .clip(RoundedCornerShape(14.dp))
            .background(
                Brush.linearGradient(
                    colors = listOf(
                        accentColor.copy(alpha = 0.28f),
                        accentColor.copy(alpha = 0.10f)
                    )
                )
            )
            .border(
                width = 1.dp,
                color = accentColor.copy(alpha = 0.45f),
                shape = RoundedCornerShape(14.dp)
            ),
        contentAlignment = Alignment.Center
    ) {
        if (favicon != null) {
            Image(
                bitmap = favicon.asImageBitmap(),
                contentDescription = title,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .size(size * 0.56f)
                    .clip(CircleShape)
            )
        } else {
            Text(
                text = letter,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

@Composable
fun BrowserProgressBar(
    progress: Int,
    isLoading: Boolean,
    isIncognito: Boolean,
    modifier: Modifier = Modifier
) {
    val target = if (isLoading) (progress.coerceIn(5, 98) / 100f) else 0f
    val animatedProgress by animateFloatAsState(
        targetValue = target,
        animationSpec = tween(durationMillis = 180),
        label = "browser_load_progress"
    )

    if (isLoading || animatedProgress > 0.01f) {
        Box(
            modifier = modifier
                .fillMaxWidth()
                .height(3.dp)
                .background(Color.Transparent)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(animatedProgress)
                    .height(3.dp)
                    .background(
                        Brush.horizontalGradient(
                            colors = if (isIncognito) {
                                listOf(IncognitoAccent, Color(0xFFE0AAFF))
                            } else {
                                listOf(ElectricPurple, ElectricPurpleBright, Color(0xFFE0AAFF))
                            }
                        )
                    )
            )
        }
    }
}

@Composable
fun SubScreenTopBar(
    title: String,
    subtitle: String? = null,
    onBack: () -> Unit,
    actions: @Composable RowScope.() -> Unit = {}
) {
    val glass = LocalRaufGlassPalette.current
    androidx.compose.foundation.layout.Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(glass.glassFillStrong)
            .statusBarsPadding()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = onBack,
                modifier = Modifier
                    .size(48.dp)
                    .testTag("subscreen_back_button")
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Navigate back",
                    tint = MaterialTheme.colorScheme.onSurface
                )
            }
            Spacer(Modifier.width(4.dp))
            Box(modifier = Modifier.weight(1f)) {
                androidx.compose.foundation.layout.Column {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleLarge,
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    if (!subtitle.isNullOrBlank()) {
                        Text(
                            text = subtitle,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }
            actions()
        }
        androidx.compose.material3.HorizontalDivider(
            thickness = 0.5.dp,
            color = MaterialTheme.colorScheme.outline.copy(alpha = 0.45f)
        )
    }
}
