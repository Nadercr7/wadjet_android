package com.wadjet.core.data.repository

import com.wadjet.core.data.datastore.UserPreferencesDataStore
import com.wadjet.core.domain.repository.TtsPreferencesRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TtsPreferencesRepositoryImpl @Inject constructor(
    private val dataStore: UserPreferencesDataStore,
) : TtsPreferencesRepository {
    override val ttsEnabled: Flow<Boolean> = dataStore.ttsEnabled
    override val ttsSpeed: Flow<Float> = dataStore.ttsSpeed
}
