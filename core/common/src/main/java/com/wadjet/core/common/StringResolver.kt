package com.wadjet.core.common

import android.content.Context
import android.content.res.Configuration
import androidx.annotation.StringRes
import androidx.appcompat.app.AppCompatDelegate
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

/**
 * G-04: resolves string resources from ViewModels in the CURRENT app language.
 *
 * The plain application context does not follow AppCompat per-app locales on
 * API < 33, so this wraps it in a configuration context built from
 * [AppCompatDelegate.getApplicationLocales] on every call — errors, toasts and
 * snackbars emitted by ViewModels then match the UI language, including right
 * after a runtime language switch.
 */
@Singleton
class StringResolver @Inject constructor(
    @ApplicationContext private val app: Context,
) {
    private val localizedContext: Context
        get() {
            val locales = AppCompatDelegate.getApplicationLocales()
            val locale = if (locales.isEmpty) return app else locales[0] ?: return app
            val config = Configuration(app.resources.configuration)
            config.setLocale(locale)
            return app.createConfigurationContext(config)
        }

    fun get(@StringRes id: Int): String = localizedContext.getString(id)

    fun get(@StringRes id: Int, vararg args: Any): String = localizedContext.getString(id, *args)
}
