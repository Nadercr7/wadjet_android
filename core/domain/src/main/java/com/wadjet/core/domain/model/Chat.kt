package com.wadjet.core.domain.model

data class ChatMessage(
    val id: String,
    val role: Role,
    val content: String,
    val timestamp: Long = System.currentTimeMillis(),
    val isStreaming: Boolean = false,
) {
    enum class Role { USER, ASSISTANT }
}
