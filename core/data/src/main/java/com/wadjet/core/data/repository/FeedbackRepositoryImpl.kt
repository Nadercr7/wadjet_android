package com.wadjet.core.data.repository

import com.wadjet.core.common.suspendRunCatching
import com.wadjet.core.domain.model.FeedbackData
import com.wadjet.core.domain.repository.FeedbackRepository
import com.wadjet.core.network.api.FeedbackApiService
import com.wadjet.core.network.model.FeedbackRequest
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FeedbackRepositoryImpl @Inject constructor(
    private val feedbackApi: FeedbackApiService,
) : FeedbackRepository {

    override suspend fun submitFeedback(data: FeedbackData): Result<Int> = suspendRunCatching {
        val response = feedbackApi.submit(
            FeedbackRequest(
                category = data.category,
                message = data.message,
                name = data.name,
                email = data.email,
            ),
        )
        if (!response.isSuccessful) {
            throw Exception("Failed to submit feedback: ${response.code()}")
        }
        val body = response.body() ?: throw Exception("Failed to submit feedback")
        body.id
    }
}
