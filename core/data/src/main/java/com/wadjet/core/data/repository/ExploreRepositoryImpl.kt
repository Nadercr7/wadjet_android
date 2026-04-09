package com.wadjet.core.data.repository

import com.wadjet.core.common.suspendRunCatching
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
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
import com.wadjet.core.network.model.LandmarkDetailDto
import com.wadjet.core.network.model.LandmarkSummaryDto
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.tasks.await
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
    private val firestore: FirebaseFirestore,
    private val firebaseAuth: FirebaseAuth,
    private val json: Json,
) : ExploreRepository {

    override suspend fun getLandmarks(
        category: String?,
        city: String?,
        search: String?,
        page: Int,
        perPage: Int,
    ): Result<LandmarkPage> = suspendRunCatching {
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
                    IdentifyMatch(topSlug, topName, body.confidence)
                } else {
                    null
                },
                matches = body.top3.map { IdentifyMatch(it.slug, it.name, it.confidence) },
                detail = body.landmark?.toDomain(),
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

    override suspend fun searchOffline(query: String): List<Landmark> =
        landmarkDao.search(query).map { it.toDomain() }

    override suspend fun toggleFavorite(
        slug: String,
        name: String,
        thumbnail: String?,
        isFavorite: Boolean,
    ): Result<Unit> = suspendRunCatching {
        val uid = firebaseAuth.currentUser?.uid
            ?: throw IllegalStateException("Not signed in")
        val favRef = firestore.collection("users").document(uid)
            .collection("favorites")

        if (isFavorite) {
            // Remove favorite
            val snapshot = favRef
                .whereEqualTo("item_type", "landmark")
                .whereEqualTo("item_id", slug)
                .get().await()
            for (doc in snapshot.documents) {
                doc.reference.delete().await()
            }
        } else {
            // Add favorite
            val data = hashMapOf(
                "item_type" to "landmark",
                "item_id" to slug,
                "display_name" to name,
                "thumbnail" to (thumbnail ?: ""),
                "created_at" to com.google.firebase.Timestamp.now(),
            )
            favRef.add(data).await()
        }
    }

    override fun getFavorites(): Flow<Set<String>> = callbackFlow {
        val uid = firebaseAuth.currentUser?.uid
        if (uid == null) {
            trySend(emptySet())
            awaitClose()
            return@callbackFlow
        }
        val listener = firestore.collection("users").document(uid)
            .collection("favorites")
            .whereEqualTo("item_type", "landmark")
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Timber.e(error, "Favorites listen failed")
                    trySend(emptySet())
                    return@addSnapshotListener
                }
                val slugs = snapshot?.documents
                    ?.mapNotNull { it.getString("item_id") }
                    ?.toSet() ?: emptySet()
                trySend(slugs)
            }
        awaitClose { listener.remove() }
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
        tags = tags ?: emptyList(),
        relatedSites = relatedSites ?: emptyList(),
        featured = featured ?: false,
        images = images?.map { LandmarkImage(it.url, it.caption) } ?: emptyList(),
        sections = sections?.map { LandmarkSection(it.title, it.content) } ?: emptyList(),
        highlights = highlights,
        visitingTips = visitingTips,
        historicalSignificance = historicalSignificance,
        wikipediaUrl = wikipediaUrl,
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
