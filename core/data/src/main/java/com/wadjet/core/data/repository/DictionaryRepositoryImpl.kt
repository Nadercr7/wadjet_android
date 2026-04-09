package com.wadjet.core.data.repository

import com.wadjet.core.database.dao.SignDao
import com.wadjet.core.database.entity.SignEntity
import com.wadjet.core.domain.model.Category
import com.wadjet.core.domain.model.Exercise
import com.wadjet.core.domain.model.ExerciseOption
import com.wadjet.core.domain.model.Lesson
import com.wadjet.core.domain.model.PaletteSign
import com.wadjet.core.domain.model.Sign
import com.wadjet.core.domain.model.SignPage
import com.wadjet.core.domain.model.WriteGlyph
import com.wadjet.core.domain.model.WriteResult
import com.wadjet.core.network.api.AudioApiService
import com.wadjet.core.network.api.DictionaryApiService
import com.wadjet.core.network.api.WriteApiService
import com.wadjet.core.network.model.SignDetailDto
import com.wadjet.core.network.model.SpeakRequest
import com.wadjet.core.network.model.WriteRequest
import com.wadjet.core.domain.repository.DictionaryRepository
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DictionaryRepositoryImpl @Inject constructor(
    private val dictionaryApi: DictionaryApiService,
    private val writeApi: WriteApiService,
    private val audioApi: AudioApiService,
    private val signDao: SignDao,
) : DictionaryRepository {

    override suspend fun getSigns(
        category: String?,
        type: String?,
        search: String?,
        page: Int,
        perPage: Int,
    ): Result<SignPage> = runCatching {
        val response = dictionaryApi.getSigns(
            category = category,
            search = search,
            type = type,
            page = page,
            perPage = perPage,
        )
        if (response.isSuccessful) {
            val body = response.body()!!
            val signs = body.signs.map { it.toDomain() }
            // Cache signs for offline search
            signDao.insertAll(body.signs.map { it.toEntity() })
            SignPage(signs = signs, total = body.total, page = body.page, totalPages = body.totalPages)
        } else {
            throw ApiException("Failed to load signs: ${response.code()}")
        }
    }

    override suspend fun getSign(code: String): Result<Sign> = runCatching {
        val response = dictionaryApi.getSign(code)
        if (response.isSuccessful) {
            val dto = response.body()!!
            signDao.insertAll(listOf(dto.toEntity()))
            dto.toDomain()
        } else {
            // Fallback to cache
            signDao.getByCode(code)?.toDomain()
                ?: throw ApiException("Sign not found: $code")
        }
    }

    override suspend fun getCategories(): Result<List<Category>> = runCatching {
        val response = dictionaryApi.getCategories()
        if (response.isSuccessful) {
            response.body()!!.categories.map { Category(code = it.code, name = it.name, count = it.count) }
        } else {
            throw ApiException("Failed to load categories: ${response.code()}")
        }
    }

    override suspend fun getLesson(level: Int): Result<Lesson> = runCatching {
        val response = dictionaryApi.getLesson(level)
        if (response.isSuccessful) {
            val body = response.body()!!
            Lesson(
                level = body.level,
                title = body.title,
                description = body.description,
                signs = body.signs.map { it.toDomain() },
                exercises = body.exercises.map { ex ->
                    Exercise(
                        type = ex.type,
                        question = ex.question,
                        options = ex.options.map { ExerciseOption(code = it.code, glyph = it.glyph, label = it.label) },
                        correctAnswer = ex.correctAnswer,
                        hint = ex.hint,
                    )
                },
            )
        } else {
            throw ApiException("Failed to load lesson $level: ${response.code()}")
        }
    }

    override suspend fun write(text: String, mode: String): Result<WriteResult> = runCatching {
        val response = writeApi.write(WriteRequest(text = text, mode = mode))
        if (response.isSuccessful) {
            val body = response.body()!!
            WriteResult(
                hieroglyphs = body.hieroglyphs,
                glyphs = body.glyphs.map {
                    WriteGlyph(
                        gardinerCode = it.gardinerCode,
                        glyph = it.glyph,
                        transliteration = it.transliteration,
                        phoneticValue = it.phoneticValue,
                        meaning = it.meaning,
                    )
                },
                mode = body.mode,
                mdc = body.mdc,
            )
        } else {
            throw ApiException("Write failed: ${response.code()}")
        }
    }

    override suspend fun getPalette(): Result<List<PaletteSign>> = runCatching {
        val response = writeApi.getPalette()
        if (response.isSuccessful) {
            response.body()!!.signs.map {
                PaletteSign(code = it.code, glyph = it.glyph, transliteration = it.transliteration, category = it.category)
            }
        } else {
            throw ApiException("Failed to load palette: ${response.code()}")
        }
    }

    override suspend fun speakPhonetic(text: String): Result<ByteArray> = runCatching {
        val response = audioApi.speak(SpeakRequest(text = text, lang = "en", context = "pronunciation"))
        if (response.isSuccessful) {
            response.body()?.bytes() ?: throw ApiException("Empty audio response")
        } else {
            throw ApiException("TTS failed: ${response.code()}")
        }
    }

    override suspend fun searchOffline(query: String): List<Sign> {
        return try {
            val ftsQuery = query.trim().replace(Regex("""[^\w\s]"""), "").let { "$it*" }
            signDao.search(ftsQuery).map { it.toDomain() }
        } catch (e: Exception) {
            Timber.w(e, "FTS search failed, falling back to empty")
            emptyList()
        }
    }
}

internal class ApiException(message: String) : Exception(message)

private fun SignDetailDto.toDomain() = Sign(
    code = code,
    glyph = glyph,
    transliteration = transliteration,
    phoneticValue = phoneticValue,
    meaning = meaning,
    type = type,
    category = category,
    categoryName = categoryName,
    examples = examples.orEmpty(),
    funFact = funFact,
    speech = speech,
    pronunciationSound = pronunciationGuide?.sound,
    pronunciationDesc = pronunciationGuide?.description,
)

private fun SignDetailDto.toEntity() = SignEntity(
    code = code,
    glyph = glyph,
    transliteration = transliteration,
    phoneticValue = phoneticValue,
    meaning = meaning,
    type = type,
    category = category,
    categoryName = categoryName,
    examples = examples?.joinToString("|"),
    funFact = funFact,
    speech = speech,
    pronunciationSound = pronunciationGuide?.sound,
    pronunciationDesc = pronunciationGuide?.description,
)

private fun SignEntity.toDomain() = Sign(
    code = code,
    glyph = glyph,
    transliteration = transliteration,
    phoneticValue = phoneticValue,
    meaning = meaning,
    type = type,
    category = category,
    categoryName = categoryName,
    examples = examples?.split("|").orEmpty(),
    funFact = funFact,
    speech = speech,
    pronunciationSound = pronunciationSound,
    pronunciationDesc = pronunciationDesc,
)
