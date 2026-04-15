package com.wadjet.core.data.repository

import com.wadjet.core.common.suspendRunCatching
import com.wadjet.core.data.ApiException
import com.wadjet.core.domain.model.TranslationResult
import com.wadjet.core.domain.repository.TranslateRepository
import com.wadjet.core.network.api.TranslateApiService
import com.wadjet.core.network.model.TranslateRequest
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TranslateRepositoryImpl @Inject constructor(
    private val translateApi: TranslateApiService,
) : TranslateRepository {

    override suspend fun translate(
        transliteration: String,
        gardinerSequence: String?,
    ): Result<TranslationResult> = suspendRunCatching {
        val response = translateApi.translate(
            TranslateRequest(
                transliteration = transliteration,
                gardinerSequence = gardinerSequence,
            ),
        )
        if (response.isSuccessful) {
            val body = response.body()!!
            if (body.error.isNotBlank()) {
                throw ApiException(body.error)
            }
            TranslationResult(
                transliteration = body.transliteration,
                english = body.english,
                arabic = body.arabic,
                context = body.context,
                provider = body.provider,
                fromCache = body.fromCache,
            )
        } else {
            throw ApiException("Translation failed: ${response.code()}")
        }
    }
}
