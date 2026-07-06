package com.wadjet.core.common

import androidx.appcompat.app.AppCompatDelegate
import java.util.Locale

/**
 * H-04: single source of truth for the app content language sent to the
 * backend (`lang` query/body params). Prefers AppCompat per-app locales —
 * `Locale.getDefault()` alone is not guaranteed to track them on all API
 * levels — and falls back to the JVM default.
 */
object AppLanguage {

    fun current(): String {
        val locales = AppCompatDelegate.getApplicationLocales()
        val locale = (if (locales.isEmpty) null else locales[0]) ?: Locale.getDefault()
        return if (locale.language.startsWith("ar")) "ar" else "en"
    }

    fun isArabic(): Boolean = current() == "ar"
}
