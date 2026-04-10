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
    }

    val ttsEnabled: Flow<Boolean> = context.dataStore.data.map { it[Keys.TTS_ENABLED] ?: true }
    val ttsSpeed: Flow<Float> = context.dataStore.data.map { it[Keys.TTS_SPEED] ?: 1.0f }

    suspend fun setTtsEnabled(enabled: Boolean) {
        context.dataStore.edit { it[Keys.TTS_ENABLED] = enabled }
    }

    suspend fun setTtsSpeed(speed: Float) {
        context.dataStore.edit { it[Keys.TTS_SPEED] = speed }
    }
}
