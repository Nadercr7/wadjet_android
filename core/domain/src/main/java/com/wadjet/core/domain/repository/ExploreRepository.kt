package com.wadjet.core.domain.repository

import com.wadjet.core.domain.model.IdentifyResult
import com.wadjet.core.domain.model.Landmark
import com.wadjet.core.domain.model.LandmarkDetail
import com.wadjet.core.domain.model.LandmarkPage
import kotlinx.coroutines.flow.Flow
import java.io.File

interface ExploreRepository {

    suspend fun getLandmarks(
        category: String? = null,
        city: String? = null,
        search: String? = null,
        page: Int = 1,
        perPage: Int = 24,
    ): Result<LandmarkPage>

    suspend fun getLandmarkDetail(slug: String): Result<LandmarkDetail>

    suspend fun identifyLandmark(imageFile: File): Result<IdentifyResult>

    fun getCachedLandmarks(): Flow<List<Landmark>>

    suspend fun getCities(): List<String>

    suspend fun getCategories(): Result<Pair<List<String>, List<String>>>

    suspend fun searchOffline(query: String): List<Landmark>

    suspend fun toggleFavorite(
        slug: String,
        name: String,
        thumbnail: String?,
        isFavorite: Boolean,
    ): Result<Unit>

    fun getFavorites(): Flow<Set<String>>
}
