package com.wadjet.core.domain.repository

import com.wadjet.core.domain.model.FeedbackData

interface FeedbackRepository {
    suspend fun submitFeedback(data: FeedbackData): Result<Int>
}
