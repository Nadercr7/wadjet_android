package com.wadjet.core.data.repository

import com.wadjet.core.common.suspendRunCatching
import com.wadjet.core.database.dao.FavoriteDao
import com.wadjet.core.database.entity.FavoriteEntity
import com.wadjet.core.domain.model.DashboardStoryProgress
import com.wadjet.core.domain.model.FavoriteItem
import com.wadjet.core.domain.model.ScanHistoryItem
import com.wadjet.core.domain.model.User
import com.wadjet.core.domain.model.UserLimits
import com.wadjet.core.domain.model.UserStats
import com.wadjet.core.domain.repository.UserRepository
import com.wadjet.core.network.api.UserApiService
import com.wadjet.core.network.model.AddFavoriteRequest
import com.wadjet.core.network.model.ChangePasswordRequest
import com.wadjet.core.network.model.UpdateProfileRequest
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class UserRepositoryImpl @Inject constructor(
    private val userApi: UserApiService,
    private val favoriteDao: FavoriteDao,
) : UserRepository {

    override suspend fun getProfile(): Result<User> = suspendRunCatching {
        val response = userApi.getProfile()
        val body = response.body() ?: throw Exception("Failed to load profile")
        User(
            id = body.id,
            email = body.email,
            displayName = body.displayName,
            preferredLang = body.preferredLang,
            tier = body.tier,
            authProvider = body.authProvider,
            emailVerified = body.emailVerified,
            avatarUrl = body.avatarUrl,
        )
    }

    override suspend fun updateProfile(
        displayName: String?,
        preferredLang: String?,
    ): Result<User> = suspendRunCatching {
        val response = userApi.updateProfile(
            UpdateProfileRequest(displayName = displayName, preferredLang = preferredLang),
        )
        val body = response.body() ?: throw Exception("Failed to update profile")
        User(
            id = body.id,
            email = body.email,
            displayName = body.displayName,
            preferredLang = body.preferredLang,
            tier = body.tier,
            authProvider = body.authProvider,
            emailVerified = body.emailVerified,
            avatarUrl = body.avatarUrl,
        )
    }

    override suspend fun changePassword(
        currentPassword: String,
        newPassword: String,
    ): Result<Unit> = suspendRunCatching {
        val response = userApi.changePassword(
            ChangePasswordRequest(currentPassword = currentPassword, newPassword = newPassword),
        )
        if (!response.isSuccessful) {
            val errorBody = response.errorBody()?.string()
            val detail = parseError(errorBody) ?: "Password change failed"
            throw Exception(detail)
        }
    }

    override suspend fun getStats(): Result<UserStats> = suspendRunCatching {
        val response = userApi.getStats()
        val body = response.body() ?: throw Exception("Failed to load stats")
        UserStats(
            scansToday = body.scansToday,
            totalScans = body.totalScans,
            storiesCompleted = body.storiesCompleted,
            glyphsLearned = body.glyphsLearned,
        )
    }

    override suspend fun getScanHistory(): Result<List<ScanHistoryItem>> = suspendRunCatching {
        val response = userApi.getScanHistory()
        val body = response.body() ?: emptyList()
        body.map {
            ScanHistoryItem(
                id = it.id,
                glyphCount = it.glyphCount,
                confidenceAvg = it.confidenceAvg,
                createdAt = it.createdAt,
            )
        }
    }

    override suspend fun getFavorites(): Result<List<FavoriteItem>> = try {
        val response = userApi.getFavorites()
        val body = response.body() ?: emptyList()
        val items = body.map {
            FavoriteItem(
                id = it.id,
                itemType = it.itemType,
                itemId = it.itemId,
                createdAt = it.createdAt,
            )
        }
        // Cache to Room
        favoriteDao.insertAll(items.map { FavoriteEntity(itemType = it.itemType, itemId = it.itemId) })
        Result.success(items)
    } catch (e: java.io.IOException) {
        Timber.w(e, "Failed to load favorites from API, falling back to Room")
        val cached = favoriteDao.getAll().map {
            FavoriteItem(id = 0, itemType = it.itemType, itemId = it.itemId, createdAt = null)
        }
        Result.success(cached)
    }

    override suspend fun addFavorite(
        itemType: String,
        itemId: String,
    ): Result<Unit> = suspendRunCatching {
        val response = userApi.addFavorite(AddFavoriteRequest(itemType, itemId))
        if (!response.isSuccessful) {
            throw Exception("Failed to add favorite: ${response.code()}")
        }
        favoriteDao.insert(FavoriteEntity(itemType = itemType, itemId = itemId))
    }

    override suspend fun removeFavorite(
        itemType: String,
        itemId: String,
    ): Result<Unit> = suspendRunCatching {
        val response = userApi.removeFavorite(itemType, itemId)
        if (!response.isSuccessful) {
            throw Exception("Failed to remove favorite: ${response.code()}")
        }
        favoriteDao.delete(itemType, itemId)
    }

    override suspend fun getStoryProgress(): Result<List<DashboardStoryProgress>> = suspendRunCatching {
        val response = userApi.getStoryProgress()
        val body = response.body() ?: emptyList()
        body.map {
            DashboardStoryProgress(
                storyId = it.storyId,
                chapterIndex = it.chapterIndex,
                glyphsLearned = it.glyphsLearnedCount,
                score = it.score,
                completed = it.completed,
            )
        }
    }

    override suspend fun getLimits(): Result<UserLimits> = suspendRunCatching {
        val response = userApi.getLimits()
        val body = response.body() ?: throw Exception("Failed to load limits")
        UserLimits(
            tier = body.tier,
            scansPerDay = body.limits.scansPerDay,
            chatMessagesPerDay = body.limits.chatMessagesPerDay,
            storiesAccessible = body.limits.storiesAccessible,
            scansToday = body.usage.scansToday,
            chatMessagesToday = body.usage.chatMessagesToday,
        )
    }

    private fun parseError(body: String?): String? {
        if (body == null) return null
        val regex = """"detail"\s*:\s*"([^"]+)"""".toRegex()
        return regex.find(body)?.groupValues?.get(1)
    }
}
