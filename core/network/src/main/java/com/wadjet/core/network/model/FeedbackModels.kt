package com.wadjet.core.network.model

import kotlinx.serialization.Serializable

@Serializable
data class FeedbackRequest(
    val category: String,
    val message: String,
    val name: String = "",
    val email: String = "",
)

@Serializable
data class FeedbackResponse(
    val ok: Boolean = true,
    val id: Int = 0,
)
