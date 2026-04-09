package com.wadjet.core.network.api

import com.wadjet.core.network.model.FeedbackRequest
import com.wadjet.core.network.model.FeedbackResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST

interface FeedbackApiService {

    @POST("api/feedback")
    suspend fun submit(@Body body: FeedbackRequest): Response<FeedbackResponse>
}
