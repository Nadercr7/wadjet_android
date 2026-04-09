package com.wadjet.core.network.api

import com.wadjet.core.network.model.IdentifyResponse
import com.wadjet.core.network.model.LandmarkCategoriesResponse
import com.wadjet.core.network.model.LandmarkChildrenResponse
import com.wadjet.core.network.model.LandmarkDetailDto
import com.wadjet.core.network.model.LandmarkListResponse
import okhttp3.MultipartBody
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part
import retrofit2.http.Path
import retrofit2.http.Query

interface LandmarkApiService {

    @GET("api/landmarks")
    suspend fun getLandmarks(
        @Query("category") category: String? = null,
        @Query("city") city: String? = null,
        @Query("search") search: String? = null,
        @Query("page") page: Int = 1,
        @Query("per_page") perPage: Int = 24,
        @Query("lang") lang: String = "en",
        @Query("featured") featured: Boolean? = null,
    ): Response<LandmarkListResponse>

    @GET("api/landmarks/categories")
    suspend fun getCategories(): Response<LandmarkCategoriesResponse>

    @GET("api/landmarks/{slug}")
    suspend fun getLandmarkDetail(
        @Path("slug") slug: String,
        @Query("lang") lang: String = "en",
    ): Response<LandmarkDetailDto>

    @GET("api/landmarks/{slug}/children")
    suspend fun getLandmarkChildren(
        @Path("slug") slug: String,
    ): Response<LandmarkChildrenResponse>

    @Multipart
    @POST("api/explore/identify")
    suspend fun identifyLandmark(
        @Part file: MultipartBody.Part,
    ): Response<IdentifyResponse>
}
