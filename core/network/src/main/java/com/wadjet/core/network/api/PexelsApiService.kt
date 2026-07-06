package com.wadjet.core.network.api

import com.wadjet.core.network.model.PexelsSearchResponse
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Query

/**
 * J-01: Pexels is now proxied by our own backend (GET /api/images/pexels-search),
 * so the API keys live server-side instead of being compiled into the APK.
 * Served by the main Retrofit instance — Bearer auth is attached automatically.
 * The response mirrors the Pexels /v1/search fields the client consumes.
 */
interface PexelsApiService {
    @GET("api/images/pexels-search")
    suspend fun search(
        @Query("query") query: String,
        @Query("per_page") perPage: Int = 1,
        @Query("orientation") orientation: String = "landscape",
    ): Response<PexelsSearchResponse>
}
