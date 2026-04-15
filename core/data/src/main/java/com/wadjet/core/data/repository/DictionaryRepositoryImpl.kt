package com.wadjet.core.data.repository

import com.wadjet.core.common.EgyptianPronunciation
import com.wadjet.core.common.suspendRunCatching
import com.wadjet.core.data.ApiException
import com.wadjet.core.database.dao.SignDao
import com.wadjet.core.database.entity.SignEntity
import com.wadjet.core.domain.model.Category
import com.wadjet.core.domain.model.ExampleUsage
import com.wadjet.core.domain.model.ExampleWord
import com.wadjet.core.domain.model.Lesson
import com.wadjet.core.domain.model.PaletteSign
import com.wadjet.core.domain.model.PracticeWord
import com.wadjet.core.domain.model.RelatedSign
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
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DictionaryRepositoryImpl @Inject constructor(
    private val dictionaryApi: DictionaryApiService,
    private val writeApi: WriteApiService,
    private val audioApi: AudioApiService,
    private val signDao: SignDao,
    private val categoryDao: com.wadjet.core.database.dao.CategoryDao,
) : DictionaryRepository {

    override suspend fun getSigns(
        category: String?,
        type: String?,
        search: String?,
        page: Int,
        perPage: Int,
        lang: String,
    ): Result<SignPage> = suspendRunCatching {
        try {
            val response = dictionaryApi.getSigns(
                category = category,
                search = search,
                type = type,
                page = page,
                perPage = perPage,
                lang = lang,
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
        } catch (e: java.io.IOException) {
            // Offline fallback — serve from Room cache
            Timber.w(e, "Network unavailable, falling back to cached signs")
            val limit = perPage
            val offset = (page - 1) * perPage

            // Use FTS search when the user has a search query (T084)
            if (!search.isNullOrBlank()) {
                val ftsResults = searchOffline(search)
                if (ftsResults.isNotEmpty()) {
                    return@suspendRunCatching SignPage(
                        signs = ftsResults.take(limit),
                        total = ftsResults.size,
                        page = page,
                        totalPages = 1,
                        isOfflineData = true,
                    )
                }
            }

            val cached = when {
                category != null && type != null -> signDao.getByFilter(category, type, limit, offset)
                category != null -> signDao.getByCategory(category, limit, offset)
                type != null -> signDao.getByType(type, limit, offset)
                else -> signDao.getAll(limit, offset)
            }
            if (cached.isNotEmpty()) {
                SignPage(signs = cached.map { it.toDomain() }, total = cached.size, page = page, totalPages = 1, isOfflineData = true)
            } else {
                throw e
            }
        }
    }

    override suspend fun getSign(code: String, lang: String): Result<Sign> = suspendRunCatching {
        try {
            val response = dictionaryApi.getSign(code, lang = lang)
            if (response.isSuccessful) {
                val dto = response.body()!!
                signDao.insertAll(listOf(dto.toEntity()))
                dto.toDomain()
            } else {
                // Fallback to cache on non-200
                signDao.getByCode(code)?.toDomain()
                    ?: throw ApiException("Sign not found: $code")
            }
        } catch (e: java.io.IOException) {
            // Offline fallback (T085)
            Timber.w(e, "Network unavailable for sign $code, falling back to cache")
            signDao.getByCode(code)?.toDomain()
                ?: throw e
        }
    }

    override suspend fun getCategories(lang: String): Result<List<Category>> = suspendRunCatching {
        try {
            val response = dictionaryApi.getCategories(lang = lang)
            if (response.isSuccessful) {
                val categories = response.body()!!.categories.map {
                    Category(code = it.code, name = it.name, count = it.count)
                }
                // Cache for offline (T088)
                categoryDao.insertAll(categories.map {
                    com.wadjet.core.database.entity.CategoryEntity(code = it.code, name = it.name, count = it.count)
                })
                categories
            } else {
                throw ApiException("Failed to load categories: ${response.code()}")
            }
        } catch (e: java.io.IOException) {
            Timber.w(e, "Network unavailable, falling back to cached categories")
            val cached = categoryDao.getAll()
            if (cached.isNotEmpty()) {
                cached.map { Category(code = it.code, name = it.name, count = it.count) }
            } else {
                throw e
            }
        }
    }

    override suspend fun getLesson(level: Int, lang: String): Result<Lesson> = suspendRunCatching {
        val response = dictionaryApi.getLesson(level, lang = lang)
        if (response.isSuccessful) {
            val body = response.body()!!
            Lesson(
                level = body.level,
                title = body.title,
                subtitle = body.subtitle,
                description = body.description,
                tip = body.tip,
                introParagraphs = body.introParagraphs,
                signs = body.signs.map { it.toDomain() },
                exampleWords = body.exampleWords.map {
                    ExampleWord(
                        hieroglyphs = it.hieroglyphs,
                        codes = it.codes,
                        transliteration = it.transliteration,
                        translation = it.translation,
                        highlightCodes = it.highlightCodes,
                        speechText = it.speechText,
                    )
                },
                practiceWords = body.practiceWords.map {
                    PracticeWord(
                        hieroglyphs = it.hieroglyphs,
                        transliteration = it.transliteration,
                        translation = it.translation,
                        hint = it.hint,
                        speechText = it.speechText,
                    )
                },
                prevLessonLevel = body.prevLesson?.level,
                nextLessonLevel = body.nextLesson?.level,
                totalLessons = body.totalLessons,
            )
        } else {
            throw ApiException("Failed to load lesson $level: ${response.code()}")
        }
    }

    override suspend fun getAlphabet(lang: String): Result<List<Sign>> = suspendRunCatching {
        val response = dictionaryApi.getAlphabet(lang = lang)
        if (response.isSuccessful) {
            response.body()!!.signs.map { it.toDomain() }
        } else {
            throw ApiException("Failed to load alphabet: ${response.code()}")
        }
    }

    override suspend fun write(text: String, mode: String): Result<WriteResult> = suspendRunCatching {
        val response = writeApi.write(WriteRequest(text = text, mode = mode))
        if (response.isSuccessful) {
            val body = response.body()!!
            WriteResult(
                hieroglyphs = body.hieroglyphs,
                glyphs = body.glyphs.map {
                    WriteGlyph(
                        code = it.code,
                        glyph = it.unicodeChar,
                        transliteration = it.transliteration,
                        description = it.description,
                        type = it.type,
                    )
                },
                mode = body.mode,
            )
        } else {
            throw ApiException("Write failed: ${response.code()}")
        }
    }

    override suspend fun getPalette(): Result<List<PaletteSign>> = suspendRunCatching {
        val response = writeApi.getPalette()
        if (response.isSuccessful) {
            val groups = response.body()!!.groups
            (groups.uniliteral + groups.biliteral + groups.triliteral + groups.logogram).map {
                PaletteSign(code = it.code, glyph = it.unicodeChar, transliteration = it.transliteration)
            }
        } else {
            throw ApiException("Failed to load palette: ${response.code()}")
        }
    }

    override suspend fun speakPhonetic(text: String): Result<ByteArray?> = suspendRunCatching {
        val response = audioApi.speak(SpeakRequest(
            text = text,
            lang = "en",
            context = EgyptianPronunciation.CONTEXT,
            voice = EgyptianPronunciation.VOICE,
            style = EgyptianPronunciation.STYLE,
        ))
        when (response.code()) {
            200 -> response.body()?.bytes()
            204 -> null
            else -> throw ApiException("TTS failed: ${response.code()}")
        }
    }

    override suspend fun searchOffline(query: String): List<Sign> {
        return try {
            val sanitized = sanitizeFtsQuery(query)
            if (sanitized.isNullOrBlank()) return emptyList()
            signDao.search(sanitized).map { it.toDomain() }
        } catch (e: Exception) {
            Timber.w(e, "FTS search failed, falling back to empty")
            emptyList()
        }
    }
}

/**
 * Sanitize user input into a safe FTS5 query.
 * Preserves Unicode characters (Arabic, diacritics like ḥ ḫ š) while stripping FTS operators.
 * Returns null if the sanitized query is too short.
 */
private fun sanitizeFtsQuery(query: String): String? {
    // Strip FTS5 operators and punctuation — preserve letters (any script), digits, spaces
    val cleaned = query.trim()
        .replace(Regex("""[^\p{L}\p{N}\s]"""), "")
        .replace(Regex("""\s+"""), " ")
        .trim()
    if (cleaned.length < 2) return null
    // Convert multi-word to FTS5 prefix query: "eye of horus" → "eye" "of" "horus"*
    val words = cleaned.split(" ").filter { it.isNotBlank() }
    if (words.isEmpty()) return null
    return words.joinToString(" ") { "\"$it\"" } + "*"
}

private val json = Json { ignoreUnknownKeys = true }

private fun SignDetailDto.toDomain() = Sign(
    code = code,
    glyph = unicodeChar,
    transliteration = transliteration,
    description = description,
    type = type,
    typeName = typeName,
    category = category,
    categoryName = categoryName,
    reading = reading,
    isPhonetic = isPhonetic,
    funFact = funFact,
    speechText = speechText,
    pronunciationSound = pronunciation?.sound,
    pronunciationExample = pronunciation?.example,
    logographicValue = logographicValue,
    determinativeClass = determinativeClass,
    exampleUsages = exampleUsages?.map {
        ExampleUsage(hieroglyphs = it.hieroglyphs, transliteration = it.transliteration, translation = it.translation)
    } ?: emptyList(),
    relatedSigns = relatedSigns?.map {
        RelatedSign(code = it.code, glyph = it.unicodeChar, transliteration = it.transliteration, reading = it.reading, type = it.type)
    } ?: emptyList(),
)

private fun SignDetailDto.toEntity() = SignEntity(
    code = code,
    glyph = unicodeChar,
    transliteration = transliteration,
    description = description,
    type = type,
    typeName = typeName,
    category = category,
    categoryName = categoryName,
    reading = reading,
    isPhonetic = isPhonetic,
    funFact = funFact,
    speechText = speechText,
    pronunciationSound = pronunciation?.sound,
    pronunciationExample = pronunciation?.example,
    logographicValue = logographicValue,
    determinativeClass = determinativeClass,
    exampleUsagesJson = exampleUsages?.let { json.encodeToString(it) },
    relatedSignsJson = relatedSigns?.let { json.encodeToString(it) },
)

private fun SignEntity.toDomain() = Sign(
    code = code,
    glyph = glyph,
    transliteration = transliteration,
    description = description,
    type = type,
    typeName = typeName,
    category = category,
    categoryName = categoryName,
    reading = reading,
    isPhonetic = isPhonetic,
    funFact = funFact,
    speechText = speechText,
    pronunciationSound = pronunciationSound,
    pronunciationExample = pronunciationExample,
    logographicValue = logographicValue,
    determinativeClass = determinativeClass,
    exampleUsages = exampleUsagesJson?.let {
        try { json.decodeFromString<List<com.wadjet.core.network.model.ExampleUsageDto>>(it).map { dto ->
            ExampleUsage(hieroglyphs = dto.hieroglyphs, transliteration = dto.transliteration, translation = dto.translation)
        } } catch (_: Exception) { emptyList() }
    } ?: emptyList(),
    relatedSigns = relatedSignsJson?.let {
        try { json.decodeFromString<List<com.wadjet.core.network.model.RelatedSignDto>>(it).map { dto ->
            RelatedSign(code = dto.code, glyph = dto.unicodeChar, transliteration = dto.transliteration, reading = dto.reading, type = dto.type)
        } } catch (_: Exception) { emptyList() }
    } ?: emptyList(),
)
