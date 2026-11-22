package com.example.ui.theme

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import androidx.core.view.WindowCompat

enum class ThemeMode(
    val displayName: String,
    val isDarkDefault: Boolean,
    val subtitle: String
) {
    SYSTEM("System", true, "Follows Android system appearance"),
    AMOLED_BLACK("AMOLED Black", true, "Pure #000000 deep OLED black"),
    AMOLED_WHITE("AMOLED White", false, "Pure #FFFFFF ultra-clean white"),
    LIQUID_GLASS("Liquid Glass", true, "Frosted translucent glass surfaces"),
    WHITE("White", false, "Soft neutral light theme"),
    BLACK("Black", true, "Deep matte charcoal dark theme")
}

val ACCENT_COLOR_PRESETS: List<Pair<String, Long>> = listOf(
    "Default" to 0L,
    "Ocean Blue" to 0xFF3969EFL,
    "Coral Orange" to 0xFFDE5833L,
    "Electric Violet" to 0xFF8B5CF6L,
    "Emerald" to 0xFF10B981L,
    "Rose" to 0xFFF43F5EL,
    "Cyan" to 0xFF06B6D4L,
    "Amber" to 0xFFF59E0BL
)

@Immutable
data class RaufGlassPalette(
    val glassFill: Color,
    val glassFillStrong: Color,
    val glassBorder: Color,
    val glassHighlight: Color,
    val accentGlow: Color,
    val isDark: Boolean,
    val isIncognito: Boolean,
    val isLiquidGlass: Boolean
)

val LocalRaufGlassPalette = staticCompositionLocalOf {
    RaufGlassPalette(
        glassFill = Color(0xFF141417),
        glassFillStrong = Color(0xFF0D0D10),
        glassBorder = Color(0xFF26262C),
        glassHighlight = Color(0x14FFFFFF),
        accentGlow = Color(0xFF5B8DEF),
        isDark = true,
        isIncognito = false,
        isLiquidGlass = false
    )
}

private data class BuiltScheme(
    val colorScheme: ColorScheme,
    val isDark: Boolean,
    val isLiquidGlass: Boolean
)

private fun buildThemeScheme(mode: ThemeMode, systemDark: Boolean, isIncognito: Boolean): BuiltScheme {
    if (isIncognito) {
        return BuiltScheme(
            colorScheme = darkColorScheme(
                primary = Color(0xFFA78BFA),
                onPrimary = Color(0xFF1A0D33),
                primaryContainer = Color(0xFF281D40),
                onPrimaryContainer = Color(0xFFEDE9FE),
                secondary = Color(0xFF8B5CF6),
                onSecondary = Color.White,
                secondaryContainer = Color(0xFF1E1A2B),
                onSecondaryContainer = Color(0xFFDDD6FE),
                tertiary = SecureGreen,
                background = Color(0xFF0B0A10),
                onBackground = Color(0xFFF4F4F6),
                surface = Color(0xFF14121C),
                onSurface = Color(0xFFF4F4F6),
                surfaceVariant = Color(0xFF1E1B29),
                onSurfaceVariant = Color(0xFFA1A1B5),
                outline = Color(0xFF332D47),
                error = DangerCrimson,
                onError = Color.White
            ),
            isDark = true,
            isLiquidGlass = false
        )
    }

    val effective = if (mode == ThemeMode.SYSTEM) {
        if (systemDark) ThemeMode.BLACK else ThemeMode.WHITE
    } else mode

    return when (effective) {
        ThemeMode.AMOLED_BLACK -> BuiltScheme(
            colorScheme = darkColorScheme(
                primary = Color(0xFF5E8BFF),
                onPrimary = Color.White,
                primaryContainer = Color(0xFF152242),
                onPrimaryContainer = Color(0xFFD9E4FF),
                secondary = Color(0xFFDE5833),
                onSecondary = Color.White,
                secondaryContainer = Color(0xFF141416),
                onSecondaryContainer = Color(0xFFE4E4E7),
                tertiary = SecureGreen,
                background = Color(0xFF000000),
                onBackground = Color(0xFFFAFAFA),
                surface = Color(0xFF0C0C0E),
                onSurface = Color(0xFFFAFAFA),
                surfaceVariant = Color(0xFF16161A),
                onSurfaceVariant = Color(0xFF9E9EA8),
                outline = Color(0xFF24242A),
                error = DangerCrimson,
                onError = Color.White
            ),
            isDark = true,
            isLiquidGlass = false
        )

        ThemeMode.AMOLED_WHITE -> BuiltScheme(
            colorScheme = lightColorScheme(
                primary = Color(0xFF2563EB),
                onPrimary = Color.White,
                primaryContainer = Color(0xFFEFF4FF),
                onPrimaryContainer = Color(0xFF1D4ED8),
                secondary = Color(0xFFDE5833),
                onSecondary = Color.White,
                secondaryContainer = Color(0xFFF4F4F6),
                onSecondaryContainer = Color(0xFF18181B),
                tertiary = Color(0xFF059669),
                background = Color(0xFFFFFFFF),
                onBackground = Color(0xFF09090B),
                surface = Color(0xFFFFFFFF),
                onSurface = Color(0xFF09090B),
                surfaceVariant = Color(0xFFF4F4F6),
                onSurfaceVariant = Color(0xFF52525B),
                outline = Color(0xFFE4E4E7),
                error = Color(0xFFDC2626),
                onError = Color.White
            ),
            isDark = false,
            isLiquidGlass = false
        )

        ThemeMode.LIQUID_GLASS -> BuiltScheme(
            colorScheme = darkColorScheme(
                primary = ElectricPurpleBright,
                onPrimary = Color(0xFF190033),
                primaryContainer = Color(0xFF2B184D),
                onPrimaryContainer = Color(0xFFEDE0FF),
                secondary = Color(0xFF60A5FA),
                onSecondary = Color(0xFF071B36),
                secondaryContainer = Color(0xFF19132E),
                onSecondaryContainer = Color(0xFFE0E7FF),
                tertiary = SecureGreen,
                background = Color(0xFF080712),
                onBackground = Color(0xFFF6F4FF),
                surface = Color(0xFF131024),
                onSurface = Color(0xFFF6F4FF),
                surfaceVariant = Color(0xFF1C1833),
                onSurfaceVariant = Color(0xFFB3ACC9),
                outline = Color(0xFF3A315E),
                error = DangerCrimson,
                onError = Color.White
            ),
            isDark = true,
            isLiquidGlass = true
        )

        ThemeMode.WHITE -> BuiltScheme(
            colorScheme = lightColorScheme(
                primary = Color(0xFF3969EF),
                onPrimary = Color.White,
                primaryContainer = Color(0xFFE8EEFF),
                onPrimaryContainer = Color(0xFF1E3A8A),
                secondary = Color(0xFFDE5833),
                onSecondary = Color.White,
                secondaryContainer = Color(0xFFECEEF2),
                onSecondaryContainer = Color(0xFF1C1E21),
                tertiary = Color(0xFF059669),
                background = Color(0xFFF6F7F9),
                onBackground = Color(0xFF141518),
                surface = Color(0xFFFFFFFF),
                onSurface = Color(0xFF141518),
                surfaceVariant = Color(0xFFECEEF2),
                onSurfaceVariant = Color(0xFF5B606B),
                outline = Color(0xFFDCDEE4),
                error = Color(0xFFDC2626),
                onError = Color.White
            ),
            isDark = false,
            isLiquidGlass = false
        )

        ThemeMode.BLACK, ThemeMode.SYSTEM -> BuiltScheme(
            colorScheme = darkColorScheme(
                primary = Color(0xFF6B93FF),
                onPrimary = Color(0xFF081433),
                primaryContainer = Color(0xFF1C294A),
                onPrimaryContainer = Color(0xFFDBE6FF),
                secondary = Color(0xFFDE5833),
                onSecondary = Color.White,
                secondaryContainer = Color(0xFF222328),
                onSecondaryContainer = Color(0xFFE8E9EE),
                tertiary = SecureGreen,
                background = Color(0xFF111215),
                onBackground = Color(0xFFF2F3F5),
                surface = Color(0xFF1A1B1F),
                onSurface = Color(0xFFF2F3F5),
                surfaceVariant = Color(0xFF24262C),
                onSurfaceVariant = Color(0xFFA2A6B0),
                outline = Color(0xFF30333C),
                error = DangerCrimson,
                onError = Color.White
            ),
            isDark = true,
            isLiquidGlass = false
        )
    }
}

private fun scaleStyle(style: TextStyle, factor: Float): TextStyle =
    style.copy(
        fontSize = style.fontSize * factor,
        lineHeight = style.lineHeight * factor
    )

private fun scaledTypography(factor: Float): Typography {
    val safe = factor.coerceIn(0.85f, 1.25f)
    if (safe == 1.0f) return RaufTypography
    return Typography(
        displayLarge = scaleStyle(RaufTypography.displayLarge, safe),
        displayMedium = scaleStyle(RaufTypography.displayMedium, safe),
        headlineLarge = scaleStyle(RaufTypography.headlineLarge, safe),
        headlineMedium = scaleStyle(RaufTypography.headlineMedium, safe),
        titleLarge = scaleStyle(RaufTypography.titleLarge, safe),
        titleMedium = scaleStyle(RaufTypography.titleMedium, safe),
        titleSmall = scaleStyle(RaufTypography.titleSmall, safe),
        bodyLarge = scaleStyle(RaufTypography.bodyLarge, safe),
        bodyMedium = scaleStyle(RaufTypography.bodyMedium, safe),
        bodySmall = scaleStyle(RaufTypography.bodySmall, safe),
        labelLarge = scaleStyle(RaufTypography.labelLarge, safe),
        labelMedium = scaleStyle(RaufTypography.labelMedium, safe),
        labelSmall = scaleStyle(RaufTypography.labelSmall, safe)
    )
}

private val RaufShapes = Shapes(
    small = RoundedCornerShape(12.dp),
    medium = RoundedCornerShape(16.dp),
    large = RoundedCornerShape(22.dp),
    extraLarge = RoundedCornerShape(28.dp)
)

@Composable
fun RaufBrowserTheme(
    themeMode: ThemeMode = ThemeMode.AMOLED_BLACK,
    accentColorHex: Long = 0L,
    fontScale: Float = 1.0f,
    isIncognito: Boolean = false,
    content: @Composable () -> Unit
) {
    val systemDark = isSystemInDarkTheme()
    val built = buildThemeScheme(themeMode, systemDark, isIncognito)
    val useDark = built.isDark

    val finalScheme = if (!isIncognito && accentColorHex != 0L) {
        val accent = Color(accentColorHex)
        built.colorScheme.copy(
            primary = accent,
            primaryContainer = accent.copy(alpha = if (useDark) 0.24f else 0.14f),
            onPrimaryContainer = if (useDark) Color.White else accent
        )
    } else {
        built.colorScheme
    }

    val glassPalette = when {
        isIncognito -> RaufGlassPalette(
            glassFill = finalScheme.surface,
            glassFillStrong = finalScheme.background,
            glassBorder = finalScheme.outline,
            glassHighlight = Color(0x1AFFFFFF),
            accentGlow = finalScheme.primary,
            isDark = true,
            isIncognito = true,
            isLiquidGlass = false
        )
        built.isLiquidGlass -> RaufGlassPalette(
            glassFill = finalScheme.surface.copy(alpha = 0.72f),
            glassFillStrong = finalScheme.surface.copy(alpha = 0.88f),
            glassBorder = finalScheme.primary.copy(alpha = 0.42f),
            glassHighlight = Color(0x2EFFFFFF),
            accentGlow = finalScheme.primary,
            isDark = true,
            isIncognito = false,
            isLiquidGlass = true
        )
        else -> RaufGlassPalette(
            glassFill = finalScheme.surface,
            glassFillStrong = finalScheme.background,
            glassBorder = finalScheme.outline,
            glassHighlight = if (useDark) Color(0x10FFFFFF) else Color(0x88FFFFFF),
            accentGlow = finalScheme.primary,
            isDark = useDark,
            isIncognito = false,
            isLiquidGlass = false
        )
    }

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = view.context.findActivity()?.window
            if (window != null) {
                val controller = WindowCompat.getInsetsController(window, view)
                controller.isAppearanceLightStatusBars = !useDark
                controller.isAppearanceLightNavigationBars = !useDark
            }
        }
    }

    CompositionLocalProvider(LocalRaufGlassPalette provides glassPalette) {
        MaterialTheme(
            colorScheme = finalScheme,
            typography = scaledTypography(fontScale),
            shapes = RaufShapes,
            content = content
        )
    }
}

private tailrec fun Context.findActivity(): Activity? = when (this) {
    is Activity -> this
    is ContextWrapper -> baseContext.findActivity()
    else -> null
}
