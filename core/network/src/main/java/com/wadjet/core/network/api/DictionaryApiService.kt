package com.wadjet.core.network.api

import com.wadjet.core.network.model.AlphabetResponse
import com.wadjet.core.network.model.CategoriesResponse
import com.wadjet.core.network.model.DictionaryResponse
import com.wadjet.core.network.model.LessonResponse
import com.wadjet.core.network.model.SignDetailDto
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface DictionaryApiService {

    @GET("api/dictionary")
    suspend fun getSigns(
        @Query("category") category: String? = null,
        @Query("search") search: String? = null,
        @Query("type") type: String? = null,
        @Query("page") page: Int = 1,
        @Query("per_page") perPage: Int = 30,
        @Query("lang") lang: String = "en",
    ): Response<DictionaryResponse>

    @GET("api/dictionary/categories")
    suspend fun getCategories(
        @Query("lang") lang: String = "en",
    ): Response<CategoriesResponse>

    @GET("api/dictionary/alphabet")
    suspend fun getAlphabet(
        @Query("lang") lang: String = "en",
    ): Response<AlphabetResponse>

    @GET("api/dictionary/lesson/{level}")
    suspend fun getLesson(
        @Path("level") level: Int,
        @Query("lang") lang: String = "en",
    ): Response<LessonResponse>

    @GET("api/dictionary/{code}")
    suspend fun getSign(
        @Path("code") code: String,
        @Query("lang") lang: String = "en",
    ): Response<SignDetailDto>
}
