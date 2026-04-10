package com.wadjet.core.network.model

import kotlinx.serialization.Serializable

@Serializable
data class ChatRequest(
    val message: String,
    val session_id: String,
    val landmark: String? = null,
)

@Serializable
data class ChatStreamChunk(
    val text: String = "",
)

@Serializable
data class ClearChatRequest(
    val session_id: String,
)

@Serializable
data class ClearChatResponse(
    val status: String = "",
)
