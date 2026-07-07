package com.wadjet.core.data.datastore

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.floatPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "user_preferences")

@Singleton
class UserPreferencesDataStore @Inject constructor(
    @ApplicationContext private val context: Context,
) {
    private object Keys {
        val TTS_ENABLED = booleanPreferencesKey("tts_enabled")
        val TTS_SPEED = floatPreferencesKey("tts_speed")
        val PREFETCH_STORIES_WIFI = booleanPreferencesKey("prefetch_stories_wifi")
        val ONBOARDING_SEEN = booleanPreferencesKey("onboarding_seen")
    }

    val ttsEnabled: Flow<Boolean> = context.dataStore.data.map { it[Keys.TTS_ENABLED] ?: true }
    val ttsSpeed: Flow<Float> = context.dataStore.data.map { it[Keys.TTS_SPEED] ?: 1.0f }

    /** E-P1: prefetch story content on unmetered Wi-Fi for offline reading (default on). */
    val prefetchStoriesOnWifi: Flow<Boolean> =
        context.dataStore.data.map { it[Keys.PREFETCH_STORIES_WIFI] ?: true }

    /** U7: whether the first-run onboarding carousel has been completed or skipped. */
    val onboardingSeen: Flow<Boolean> =
        context.dataStore.data.map { it[Keys.ONBOARDING_SEEN] ?: false }

    /**
     * U7: blocking read of the onboarding gate for the cold-start start-destination
     * decision. Reads a single small preference off the splash thread; used only once
     * in [android] onCreate, mirroring the synchronous auth check.
     */
    fun onboardingSeenBlocking(): Boolean =
        kotlinx.coroutines.runBlocking { onboardingSeen.first() }

    suspend fun setTtsEnabled(enabled: Boolean) {
        context.dataStore.edit { it[Keys.TTS_ENABLED] = enabled }
    }

    suspend fun setTtsSpeed(speed: Float) {
        context.dataStore.edit { it[Keys.TTS_SPEED] = speed }
    }

    suspend fun setPrefetchStoriesOnWifi(enabled: Boolean) {
        context.dataStore.edit { it[Keys.PREFETCH_STORIES_WIFI] = enabled }
    }

    /** U7: record that the user finished (or skipped) the first-run onboarding. */
    suspend fun setOnboardingSeen() {
        context.dataStore.edit { it[Keys.ONBOARDING_SEEN] = true }
    }
}
