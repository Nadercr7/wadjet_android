package com.wadjet.core.data.seed

import android.content.Context
import com.wadjet.core.database.dao.CategoryDao
import com.wadjet.core.database.dao.LandmarkDao
import com.wadjet.core.database.dao.SignDao
import com.wadjet.core.database.entity.CategoryEntity
import com.wadjet.core.database.entity.LandmarkEntity
import com.wadjet.core.database.entity.SignEntity
import com.wadjet.core.network.model.CategoriesResponse
import com.wadjet.core.network.model.LandmarkSummaryDto
import com.wadjet.core.network.model.SignDetailDto
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.decodeFromJsonElement
import kotlinx.serialization.json.jsonObject
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

/**
 * E-04: seeds the Room cache from bundled JSON snapshots (assets/seed) so a
 * fresh install has a full dictionary (1023 signs + categories) and the
 * landmark list without any network. Live responses overwrite seed rows on the
 * next successful fetch (same REPLACE upserts the repositories already use).
 */
@Singleton
class SeedImporter @Inject constructor(
    @ApplicationContext private val context: Context,
    private val signDao: SignDao,
    private val categoryDao: CategoryDao,
    private val landmarkDao: LandmarkDao,
) {
    private val json = Json { ignoreUnknownKeys = true }

    suspend fun seedIfEmpty() {
        runCatching {
            if (signDao.count() == 0) {
                // Parse via JsonElement: the DTO serializers are generated in
                // core:network; core:data itself has no serialization plugin.
                val signsRoot = json.parseToJsonElement(readAsset("seed/signs.json")).jsonObject
                val signs = json.decodeFromJsonElement<List<SignDetailDto>>(signsRoot.getValue("signs"))
                signDao.insertAll(signs.map { it.toEntity() })
                val categories = json.decodeFromString<CategoriesResponse>(readAsset("seed/categories.json")).categories
                categoryDao.insertAll(categories.map { CategoryEntity(code = it.code, name = it.name, count = it.count) })
                Timber.i("E-04 seeded %d signs, %d categories", signs.size, categories.size)
            }
            if (landmarkDao.count() == 0) {
                val lmRoot = json.parseToJsonElement(readAsset("seed/landmarks.json")).jsonObject
                val landmarks = json.decodeFromJsonElement<List<LandmarkSummaryDto>>(lmRoot.getValue("landmarks"))
                landmarkDao.insertAll(landmarks.map { it.toEntity() })
                Timber.i("E-04 seeded %d landmarks", landmarks.size)
            }
        }.onFailure { Timber.w(it, "E-04 seed import failed") }
    }

    private fun readAsset(path: String): String =
        context.assets.open(path).bufferedReader().use { it.readText() }

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

    // Relative thumbnails resolve through Coil's BaseUrlInterceptor at render time.
    private fun LandmarkSummaryDto.toEntity() = LandmarkEntity(
        slug = slug,
        name = name,
        nameAr = nameAr,
        city = city,
        type = type,
        era = era,
        thumbnail = thumbnail,
        featured = featured ?: false,
        popularity = popularity ?: 0,
    )
}
