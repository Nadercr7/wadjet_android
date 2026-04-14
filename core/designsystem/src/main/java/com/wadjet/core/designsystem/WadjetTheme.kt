package com.wadjet.core.designsystem

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.core.os.ConfigurationCompat

private val WadjetDarkColorScheme = darkColorScheme(
    primary = WadjetColors.Gold,
    onPrimary = WadjetColors.Night,
    primaryContainer = WadjetColors.GoldDark,
    onPrimaryContainer = WadjetColors.Ivory,
    secondary = WadjetColors.Sand,
    onSecondary = WadjetColors.Night,
    secondaryContainer = WadjetColors.SurfaceAlt,
    onSecondaryContainer = WadjetColors.Sand,
    tertiary = WadjetColors.GoldLight,
    onTertiary = WadjetColors.Night,
    background = WadjetColors.Night,
    onBackground = WadjetColors.Text,
    surface = WadjetColors.Surface,
    onSurface = WadjetColors.Text,
    surfaceVariant = WadjetColors.SurfaceAlt,
    onSurfaceVariant = WadjetColors.TextMuted,
    surfaceContainerHighest = WadjetColors.SurfaceHover,
    outline = WadjetColors.Border,
    outlineVariant = WadjetColors.BorderLight,
    error = WadjetColors.Error,
    onError = Color.White,
    errorContainer = Color(0xFF93000A),
    onErrorContainer = Color(0xFFFFDAD6),
)

@Composable
fun WadjetTheme(content: @Composable () -> Unit) {
    val locale = ConfigurationCompat.getLocales(
        LocalContext.current.resources.configuration,
    )[0]?.language ?: "en"
    MaterialTheme(
        colorScheme = WadjetDarkColorScheme,
        typography = wadjetTypographyForLang(locale),
        shapes = WadjetShapes,
        content = content,
    )
}
