package com.wadjet.core.domain.repository

import com.wadjet.core.domain.model.ChatMessage
import kotlinx.coroutines.flow.Flow

interface ChatRepository {

    /**
     * Stream a chat response via SSE. Emits partial text chunks as they arrive.
     */
    fun streamChat(
        message: String,
        sessionId: String,
        landmark: String? = null,
    ): Flow<String>

    /**
     * Clear chat session on the server.
     */
    suspend fun clearSession(sessionId: String): Result<Unit>

    /**
     * Speak text via server TTS. Returns WAV bytes, or null for 204 (use local TTS).
     */
    suspend fun speak(text: String, lang: String = "en"): Result<ByteArray?>

    /**
     * Transcribe audio via server STT (Groq Whisper). Returns transcribed text.
     */
    suspend fun transcribe(audioFile: java.io.File, lang: String = "en"): Result<String>
}
