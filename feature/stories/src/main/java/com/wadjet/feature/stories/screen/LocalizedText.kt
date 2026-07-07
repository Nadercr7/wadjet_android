package com.wadjet.feature.stories.screen

import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.ui.platform.LocalConfiguration

/**
 * G-06: server story content is bilingual (En/Ar pairs); render the Arabic
 * variant when the app locale is Arabic (web parity), falling back to English
 * when the Arabic string is blank.
 */
@Composable
@ReadOnlyComposable
internal fun isAppLocaleArabic(): Boolean =
    LocalConfiguration.current.locales[0]?.language == "ar"

@Composable
@ReadOnlyComposable
internal fun localized(en: String, ar: String): String =
    if (isAppLocaleArabic() && ar.isNotBlank()) ar else en

@Composable
@ReadOnlyComposable
internal fun localizedOrNull(en: String?, ar: String?): String? =
    if (isAppLocaleArabic() && !ar.isNullOrBlank()) ar else en
