package com.wadjet.core.domain.repository

import com.wadjet.core.domain.model.Category
import com.wadjet.core.domain.model.Lesson
import com.wadjet.core.domain.model.PaletteSign
import com.wadjet.core.domain.model.Sign
import com.wadjet.core.domain.model.SignPage
import com.wadjet.core.domain.model.WriteResult

interface DictionaryRepository {

    suspend fun getSigns(
        category: String? = null,
        type: String? = null,
        search: String? = null,
        page: Int = 1,
        perPage: Int = 30,
        lang: String = "en",
    ): Result<SignPage>

    suspend fun getSign(code: String, lang: String = "en"): Result<Sign>

    suspend fun getCategories(lang: String = "en"): Result<List<Category>>

    suspend fun getLesson(level: Int, lang: String = "en"): Result<Lesson>

    suspend fun getAlphabet(lang: String = "en"): Result<List<Sign>>

    suspend fun write(text: String, mode: String): Result<WriteResult>

    suspend fun getPalette(): Result<List<PaletteSign>>

    suspend fun speakPhonetic(text: String): Result<ByteArray?>

    suspend fun searchOffline(query: String): List<Sign>
}
