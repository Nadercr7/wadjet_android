package com.wadjet.core.network.api

import com.wadjet.core.network.model.SpeakRequest
import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST

interface AudioApiService {

    @POST("api/audio/speak")
    suspend fun speak(@Body body: SpeakRequest): Response<ResponseBody>
}
