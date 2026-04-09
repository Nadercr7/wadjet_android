package com.wadjet.core.network.api

import com.wadjet.core.network.model.PaletteResponse
import com.wadjet.core.network.model.WriteRequest
import com.wadjet.core.network.model.WriteResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST

interface WriteApiService {

    @POST("api/write")
    suspend fun write(@Body body: WriteRequest): Response<WriteResponse>

    @GET("api/write/palette")
    suspend fun getPalette(): Response<PaletteResponse>
}
