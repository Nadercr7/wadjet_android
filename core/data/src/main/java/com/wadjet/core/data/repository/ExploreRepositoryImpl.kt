package com.wadjet.core.data.repository

import com.wadjet.core.common.suspendRunCatching
import com.wadjet.core.database.dao.LandmarkDao
import com.wadjet.core.database.entity.LandmarkEntity
import com.wadjet.core.domain.model.IdentifyMatch
import com.wadjet.core.domain.model.IdentifyResult
import com.wadjet.core.domain.model.Landmark
import com.wadjet.core.domain.model.LandmarkDetail
import com.wadjet.core.domain.model.LandmarkImage
import com.wadjet.core.domain.model.LandmarkPage
import com.wadjet.core.domain.model.LandmarkSection
import com.wadjet.core.domain.model.Recommendation
import com.wadjet.core.domain.repository.ExploreRepository
import com.wadjet.core.network.api.LandmarkApiService
import com.wadjet.core.network.api.UserApiService
import com.wadjet.core.network.model.AddFavoriteRequest
import com.wadjet.core.network.model.LandmarkDetailDto
import com.wadjet.core.network.model.LandmarkSummaryDto
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.update
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import timber.log.Timber
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ExploreRepositoryImpl @Inject constructor(
    private val landmarkApi: LandmarkApiService,
    private val landmarkDao: LandmarkDao,
    private val userApi: UserApiService,
    private val json: Json,
) : ExploreRepository {

    private val _favorites = MutableStateFlow<Set<String>>(emptySet())
    private var favoritesLoaded = false

    override suspend fun getLandmarks(
        category: String?,
        city: String?,
        search: String?,
        page: Int,
        perPage: Int,
    ): Result<LandmarkPage> = suspendRunCatching {
        try {
            val response = landmarkApi.getLandmarks(
                category = category,
                city = city,
                search = search,
                page = page,
                perPage = perPage,
            )
            if (response.isSuccessful) {
                val body = response.body()!!
                // Cache first page to Room
                if (page == 1 && search == null) {
                    val entities = body.landmarks.map { it.toEntity() }
                    landmarkDao.insertAll(entities)
                }
                LandmarkPage(
                    landmarks = body.landmarks.map { it.toDomain() },
                    total = body.total,
                    page = body.page,
                    totalPages = body.totalPages,
                )
            } else {
                throw ApiException("Failed to load landmarks: ${response.code()}")
            }
        } catch (e: java.io.IOException) {
            // Offline fallback — serve from Room cache
            Timber.w(e, "Network unavailable, falling back to cached landmarks")
            val cached = landmarkDao.getFiltered(
                category = category,
                city = city,
                limit = perPage,
                offset = (page - 1) * perPage,
            )
            if (cached.isNotEmpty()) {
                LandmarkPage(
                    landmarks = cached.map { it.toDomain() },
                    total = cached.size,
                    page = page,
                    totalPages = 1,
                )
            } else {
                throw e
            }
        }
    }

    override suspend fun getLandmarkDetail(slug: String): Result<LandmarkDetail> = suspendRunCatching {
        val response = landmarkApi.getLandmarkDetail(slug)
        if (response.isSuccessful) {
            val dto = response.body()!!
            // Cache detail JSON
            val detailJson = json.encodeToString(dto)
            landmarkDao.insert(dto.toEntity(detailJson))
            dto.toDomain()
        } else {
            // Fallback to cached
            val cached = landmarkDao.getBySlug(slug)
            val cachedJson = cached?.detailJson
            if (cachedJson != null) {
                json.decodeFromString<LandmarkDetailDto>(cachedJson).toDomain()
            } else {
                throw ApiException("Landmark not found: ${response.code()}")
            }
        }
    }

    override suspend fun identifyLandmark(imageFile: File): Result<IdentifyResult> = suspendRunCatching {
        val filePart = MultipartBody.Part.createFormData(
            "file",
            imageFile.name,
            imageFile.asRequestBody("image/jpeg".toMediaType()),
        )
        val response = landmarkApi.identifyLandmark(filePart)
        if (response.isSuccessful) {
            val body = response.body()!!
            val topSlug = body.slug
            val topName = body.name
            IdentifyResult(
                topMatch = if (topSlug != null && topName != null) {
                    IdentifyMatch(topSlug, topName, body.confidence, body.source)
                } else {
                    null
                },
                matches = body.top3.map { IdentifyMatch(it.slug, it.name, it.confidence, it.source) },
                source = body.source,
                agreement = body.agreement,
                description = body.description,
                isKnownLandmark = body.isKnownLandmark,
                isEgyptian = body.isEgyptian,
            )
        } else {
            throw ApiException("Identify failed: ${response.code()}")
        }
    }

    override fun getCachedLandmarks(): Flow<List<Landmark>> =
        landmarkDao.getAll().map { entities ->
            entities.map { it.toDomain() }
        }

    override suspend fun getCities(): List<String> = landmarkDao.getCities()

    override suspend fun getCategories(): Result<Pair<List<String>, List<String>>> = suspendRunCatching {
        val response = landmarkApi.getCategories()
        if (response.isSuccessful) {
            val body = response.body()!!
            val types = body.types.map { it.name }
            val cities = body.cities.map { it.name }
            Pair(types, cities)
        } else {
            throw ApiException("Failed to load categories: ${response.code()}")
        }
    }

    override suspend fun searchOffline(query: String): List<Landmark> =
        landmarkDao.search(query).map { it.toDomain() }

    override suspend fun toggleFavorite(
        slug: String,
        name: String,
        thumbnail: String?,
        isFavorite: Boolean,
    ): Result<Unit> = suspendRunCatching {
        if (isFavorite) {
            val response = userApi.removeFavorite("landmark", slug)
            if (!response.isSuccessful) {
                throw ApiException("Failed to remove favorite: ${response.code()}")
            }
            _favorites.update { it - slug }
        } else {
            val response = userApi.addFavorite(AddFavoriteRequest("landmark", slug))
            if (!response.isSuccessful) {
                throw ApiException("Failed to add favorite: ${response.code()}")
            }
            _favorites.update { it + slug }
        }
    }

    override fun getFavorites(): Flow<Set<String>> = _favorites
        .onStart {
            if (!favoritesLoaded) {
                loadFavoritesFromApi()
            }
        }

    private suspend fun loadFavoritesFromApi() {
        try {
            val response = userApi.getFavorites()
            if (response.isSuccessful) {
                val slugs = response.body()
                    ?.filter { it.itemType == "landmark" }
                    ?.map { it.itemId }
                    ?.toSet() ?: emptySet()
                _favorites.value = slugs
                favoritesLoaded = true
            }
        } catch (e: Exception) {
            Timber.w(e, "Failed to load favorites from API")
        }
    }

    // --- Mappers ---

    private fun LandmarkSummaryDto.toDomain() = Landmark(
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

    private fun LandmarkDetailDto.toEntity(detailJson: String) = LandmarkEntity(
        slug = slug,
        name = name,
        nameAr = nameAr,
        city = city,
        type = type,
        era = era,
        thumbnail = thumbnail,
        featured = featured ?: false,
        popularity = popularity ?: 0,
        detailJson = detailJson,
    )

    private fun LandmarkDetailDto.toDomain() = LandmarkDetail(
        slug = slug,
        name = name,
        nameAr = nameAr,
        city = city,
        type = type,
        subcategory = subcategory,
        era = era,
        period = period,
        popularity = popularity ?: 0,
        description = description,
        coordinates = coordinates?.let { if (it.size >= 2) Pair(it[0], it[1]) else null },
        mapsUrl = mapsUrl,
        thumbnail = thumbnail,
        originalImage = originalImage,
        tags = tags ?: emptyList(),
        relatedSites = relatedSites ?: emptyList(),
        featured = featured ?: false,
        images = images?.map { LandmarkImage(it.url, it.caption) } ?: emptyList(),
        sections = sections?.map { LandmarkSection(it.title, it.content) } ?: emptyList(),
        highlights = highlights,
        visitingTips = visitingTips,
        historicalSignificance = historicalSignificance,
        dynasty = dynasty,
        notablePharaohs = notablePharaohs ?: emptyList(),
        notableTombs = notableTombs ?: emptyList(),
        notableFeatures = notableFeatures ?: emptyList(),
        keyArtifacts = keyArtifacts ?: emptyList(),
        architecturalFeatures = architecturalFeatures ?: emptyList(),
        wikipediaExtract = wikipediaExtract,
        wikipediaUrl = wikipediaUrl,
        children = children?.map {
            com.wadjet.core.domain.model.LandmarkChild(it.slug, it.name, it.nameAr, it.description, it.thumbnail)
        } ?: emptyList(),
        parent = parent?.let {
            com.wadjet.core.domain.model.LandmarkParent(it.slug, it.name, it.nameAr)
        },
        recommendations = recommendations?.map {
            Recommendation(it.slug, it.name, it.score, it.reasons)
        } ?: emptyList(),
    )

    private fun LandmarkEntity.toDomain() = Landmark(
        slug = slug,
        name = name,
        nameAr = nameAr,
        city = city,
        type = type,
        era = era,
        thumbnail = thumbnail,
        featured = featured,
        popularity = popularity,
    )
}
