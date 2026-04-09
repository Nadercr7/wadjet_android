package com.wadjet.core.domain.model

data class FeedbackData(
    val category: String,
    val message: String,
    val name: String = "",
    val email: String = "",
)
