package com.wadjet.core.domain.repository

import kotlinx.coroutines.flow.Flow

interface TtsPreferencesRepository {
    val ttsEnabled: Flow<Boolean>
    val ttsSpeed: Flow<Float>
}
