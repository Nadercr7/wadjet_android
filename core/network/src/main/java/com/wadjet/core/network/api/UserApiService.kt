package com.wadjet.core.network.api

import com.wadjet.core.network.model.AddFavoriteRequest
import com.wadjet.core.network.model.ChangePasswordRequest
import com.wadjet.core.network.model.FavoriteItemDto
import com.wadjet.core.network.model.OkResponse
import com.wadjet.core.network.model.SaveProgressRequest
import com.wadjet.core.network.model.ScanHistoryItemDto
import com.wadjet.core.network.model.StoryProgressItemDto
import com.wadjet.core.network.model.UpdateProfileRequest
import com.wadjet.core.network.model.UserLimitsResponse
import com.wadjet.core.network.model.UserResponse
import com.wadjet.core.network.model.UserStatsResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.Path

interface UserApiService {

    @GET("api/user/profile")
    suspend fun getProfile(): Response<UserResponse>

    @PATCH("api/user/profile")
    suspend fun updateProfile(@Body body: UpdateProfileRequest): Response<UserResponse>

    @PATCH("api/user/password")
    suspend fun changePassword(@Body body: ChangePasswordRequest): Response<OkResponse>

    @GET("api/user/history")
    suspend fun getScanHistory(): Response<List<ScanHistoryItemDto>>

    @GET("api/user/favorites")
    suspend fun getFavorites(): Response<List<FavoriteItemDto>>

    @POST("api/user/favorites")
    suspend fun addFavorite(@Body body: AddFavoriteRequest): Response<FavoriteItemDto>

    @DELETE("api/user/favorites/{item_type}/{item_id}")
    suspend fun removeFavorite(
        @Path("item_type") itemType: String,
        @Path("item_id") itemId: String,
    ): Response<OkResponse>

    @GET("api/user/stats")
    suspend fun getStats(): Response<UserStatsResponse>

    @GET("api/user/progress")
    suspend fun getStoryProgress(): Response<List<StoryProgressItemDto>>

    @POST("api/user/progress")
    suspend fun saveProgress(@Body body: SaveProgressRequest): Response<OkResponse>

    @GET("api/user/limits")
    suspend fun getLimits(): Response<UserLimitsResponse>
}
