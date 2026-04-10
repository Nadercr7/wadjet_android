package com.wadjet.core.domain.repository

import com.wadjet.core.domain.model.DashboardStoryProgress
import com.wadjet.core.domain.model.FavoriteItem
import com.wadjet.core.domain.model.ScanHistoryItem
import com.wadjet.core.domain.model.User
import com.wadjet.core.domain.model.UserLimits
import com.wadjet.core.domain.model.UserStats

interface UserRepository {
    suspend fun getProfile(): Result<User>
    suspend fun updateProfile(displayName: String?, preferredLang: String?): Result<User>
    suspend fun changePassword(currentPassword: String, newPassword: String): Result<Unit>
    suspend fun getStats(): Result<UserStats>
    suspend fun getScanHistory(): Result<List<ScanHistoryItem>>
    suspend fun getFavorites(): Result<List<FavoriteItem>>
    suspend fun addFavorite(itemType: String, itemId: String): Result<Unit>
    suspend fun removeFavorite(itemType: String, itemId: String): Result<Unit>
    suspend fun getStoryProgress(): Result<List<DashboardStoryProgress>>
    suspend fun getLimits(): Result<UserLimits>
}
