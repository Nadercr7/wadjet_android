package com.wadjet.core.domain.repository

import com.wadjet.core.domain.model.TranslationResult

interface TranslateRepository {
    suspend fun translate(transliteration: String, gardinerSequence: String? = null): Result<TranslationResult>
}
