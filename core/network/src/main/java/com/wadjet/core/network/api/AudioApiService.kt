package com.wadjet.core.network.api

import com.wadjet.core.network.model.SpeakRequest
import com.wadjet.core.network.model.SttResponse
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part

interface AudioApiService {

    @POST("api/audio/speak")
    suspend fun speak(@Body body: SpeakRequest): Response<ResponseBody>

    @Multipart
    @POST("api/audio/stt")
    suspend fun stt(
        @Part file: MultipartBody.Part,
        @Part("lang") lang: RequestBody,
    ): Response<SttResponse>
}
