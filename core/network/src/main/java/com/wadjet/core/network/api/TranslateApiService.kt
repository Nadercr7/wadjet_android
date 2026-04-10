package com.wadjet.core.network.api

import com.wadjet.core.network.model.TranslateRequest
import com.wadjet.core.network.model.TranslateResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST

interface TranslateApiService {

    @POST("api/translate")
    suspend fun translate(@Body body: TranslateRequest): Response<TranslateResponse>
}
