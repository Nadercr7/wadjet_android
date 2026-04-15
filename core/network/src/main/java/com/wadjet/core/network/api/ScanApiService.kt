package com.wadjet.core.network.api

import com.wadjet.core.network.model.ScanResponse
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Response
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part

interface ScanApiService {

    @Multipart
    @POST("api/scan")
    suspend fun scan(
        @Part file: MultipartBody.Part,
        @Part("mode") mode: RequestBody,
    ): Response<ScanResponse>
}
