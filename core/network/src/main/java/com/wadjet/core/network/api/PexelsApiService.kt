package com.wadjet.core.network.api

import com.wadjet.core.network.model.PexelsSearchResponse
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Query

interface PexelsApiService {
    @GET("v1/search")
    suspend fun search(
        @Query("query") query: String,
        @Query("per_page") perPage: Int = 1,
        @Query("orientation") orientation: String = "landscape",
    ): Response<PexelsSearchResponse>
}
