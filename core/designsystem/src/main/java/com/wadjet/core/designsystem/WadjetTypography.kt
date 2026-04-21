package com.wadjet.core.designsystem

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

val WadjetTypography = Typography(
    displayLarge = TextStyle(
        fontFamily = PlayfairDisplay,
        fontWeight = FontWeight.Bold,
        fontSize = 36.sp,
        lineHeight = 44.sp,
    ),
    displayMedium = TextStyle(
        fontFamily = PlayfairDisplay,
        fontWeight = FontWeight.Bold,
        fontSize = 30.sp,
        lineHeight = 38.sp,
    ),
    displaySmall = TextStyle(
        fontFamily = PlayfairDisplay,
        fontWeight = FontWeight.SemiBold,
        fontSize = 24.sp,
        lineHeight = 32.sp,
    ),
    headlineLarge = TextStyle(
        fontFamily = PlayfairDisplay,
        fontWeight = FontWeight.SemiBold,
        fontSize = 22.sp,
        lineHeight = 28.sp,
    ),
    headlineMedium = TextStyle(
        fontFamily = PlayfairDisplay,
        fontWeight = FontWeight.SemiBold,
        fontSize = 22.sp,
        lineHeight = 28.sp,
    ),
    headlineSmall = TextStyle(
        fontFamily = PlayfairDisplay,
        fontWeight = FontWeight.SemiBold,
        fontSize = 20.sp,
        lineHeight = 26.sp,
    ),
    titleLarge = TextStyle(
        fontFamily = Inter,
        fontWeight = FontWeight.SemiBold,
        fontSize = 18.sp,
        lineHeight = 24.sp,
    ),
    titleMedium = TextStyle(
        fontFamily = Inter,
        fontWeight = FontWeight.SemiBold,
        fontSize = 16.sp,
        lineHeight = 22.sp,
    ),
    titleSmall = TextStyle(
        fontFamily = Inter,
        fontWeight = FontWeight.Medium,
        fontSize = 14.sp,
        lineHeight = 20.sp,
    ),
    bodyLarge = TextStyle(
        fontFamily = Inter,
        fontWeight = FontWeight.Normal,
        fontSize = 16.sp,
        lineHeight = 24.sp,
    ),
    bodyMedium = TextStyle(
        fontFamily = Inter,
        fontWeight = FontWeight.Normal,
        fontSize = 14.sp,
        lineHeight = 20.sp,
    ),
    bodySmall = TextStyle(
        fontFamily = Inter,
        fontWeight = FontWeight.Normal,
        fontSize = 12.sp,
        lineHeight = 16.sp,
    ),
    labelLarge = TextStyle(
        fontFamily = Inter,
        fontWeight = FontWeight.Medium,
        fontSize = 14.sp,
        lineHeight = 20.sp,
    ),
    labelMedium = TextStyle(
        fontFamily = Inter,
        fontWeight = FontWeight.Medium,
        fontSize = 12.sp,
        lineHeight = 16.sp,
    ),
    labelSmall = TextStyle(
        fontFamily = Inter,
        fontWeight = FontWeight.Medium,
        fontSize = 10.sp,
        lineHeight = 14.sp,
    ),
)

/** Hieroglyph display style */
val HieroglyphStyle = TextStyle(
    fontFamily = NotoSansEgyptianHieroglyphs,
    fontSize = 32.sp,
    color = WadjetColors.Gold,
)

/** Gardiner code style */
val GardinerCodeStyle = TextStyle(
    fontFamily = JetBrainsMono,
    fontSize = 14.sp,
    color = WadjetColors.Sand,
)

/** Swap Inter → Cairo for Arabic locale */
fun wadjetTypographyForLang(lang: String): Typography {
    if (lang != "ar") return WadjetTypography
    return WadjetTypography.copy(
        displayLarge = WadjetTypography.displayLarge.copy(fontFamily = Cairo, fontWeight = FontWeight.Bold),
        displayMedium = WadjetTypography.displayMedium.copy(fontFamily = Cairo, fontWeight = FontWeight.Bold),
        displaySmall = WadjetTypography.displaySmall.copy(fontFamily = Cairo, fontWeight = FontWeight.SemiBold),
        headlineLarge = WadjetTypography.headlineLarge.copy(fontFamily = Cairo, fontWeight = FontWeight.SemiBold),
        headlineMedium = WadjetTypography.headlineMedium.copy(fontFamily = Cairo, fontWeight = FontWeight.SemiBold),
        headlineSmall = WadjetTypography.headlineSmall.copy(fontFamily = Cairo, fontWeight = FontWeight.SemiBold),
        bodyLarge = WadjetTypography.bodyLarge.copy(fontFamily = Cairo),
        bodyMedium = WadjetTypography.bodyMedium.copy(fontFamily = Cairo),
        bodySmall = WadjetTypography.bodySmall.copy(fontFamily = Cairo),
        titleLarge = WadjetTypography.titleLarge.copy(fontFamily = Cairo),
        titleMedium = WadjetTypography.titleMedium.copy(fontFamily = Cairo),
        titleSmall = WadjetTypography.titleSmall.copy(fontFamily = Cairo),
        labelLarge = WadjetTypography.labelLarge.copy(fontFamily = Cairo),
        labelMedium = WadjetTypography.labelMedium.copy(fontFamily = Cairo),
        labelSmall = WadjetTypography.labelSmall.copy(fontFamily = Cairo),
    )
}
