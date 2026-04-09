package com.wadjet.core.network.api

import com.wadjet.core.network.model.ClearChatRequest
import com.wadjet.core.network.model.ClearChatResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST

interface ChatApiService {

    @POST("api/chat/clear")
    suspend fun clearChat(
        @Body body: ClearChatRequest,
    ): Response<ClearChatResponse>
}
