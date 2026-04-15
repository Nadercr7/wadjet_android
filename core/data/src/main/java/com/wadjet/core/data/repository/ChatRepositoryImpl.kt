package com.wadjet.core.data.repository

import com.wadjet.core.common.suspendRunCatching
import com.wadjet.core.domain.repository.ChatRepository
import com.wadjet.core.network.api.AudioApiService
import com.wadjet.core.network.api.ChatApiService
import com.wadjet.core.network.model.ClearChatRequest
import com.wadjet.core.network.model.SpeakRequest
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.Response
import okhttp3.sse.EventSource
import okhttp3.sse.EventSourceListener
import okhttp3.sse.EventSources
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Named
import javax.inject.Singleton

@Singleton
class ChatRepositoryImpl @Inject constructor(
    private val chatApi: ChatApiService,
    private val audioApi: AudioApiService,
    private val okHttpClient: OkHttpClient,
    private val json: Json,
    @Named("baseUrl") private val baseUrl: String,
) : ChatRepository {

    override fun streamChat(
        message: String,
        sessionId: String,
        landmark: String?,
    ): Flow<String> = callbackFlow {
        val requestBody = buildString {
            append("{\"message\":")
            append(json.encodeToString(kotlinx.serialization.serializer<String>(), message))
            append(",\"session_id\":")
            append(json.encodeToString(kotlinx.serialization.serializer<String>(), sessionId))
            if (landmark != null) {
                append(",\"landmark\":")
                append(json.encodeToString(kotlinx.serialization.serializer<String>(), landmark))
            }
            append("}")
        }

        val request = Request.Builder()
            .url("${baseUrl.trimEnd('/')}/" + "api/chat/stream")
            .post(requestBody.toRequestBody("application/json".toMediaType()))
            .header("Accept", "text/event-stream")
            .build()

        val listener = object : EventSourceListener() {
            override fun onEvent(
                eventSource: EventSource,
                id: String?,
                type: String?,
                data: String,
            ) {
                if (data == "[DONE]") {
                    close()
                    return
                }
                try {
                    val chunk = json.decodeFromString<com.wadjet.core.network.model.ChatStreamChunk>(data)
                    trySend(chunk.text)
                } catch (e: Exception) {
                    Timber.w(e, "Failed to parse SSE chunk: $data")
                    // Try raw text fallback
                    trySend(data)
                }
            }

            override fun onFailure(
                eventSource: EventSource,
                t: Throwable?,
                response: Response?,
            ) {
                val msg = t?.message ?: response?.message ?: "SSE stream failed"
                Timber.e(t, "Chat SSE failure: $msg")
                close(Exception(msg))
            }

            override fun onClosed(eventSource: EventSource) {
                close()
            }
        }

        val factory = EventSources.createFactory(okHttpClient)
        val eventSource = factory.newEventSource(request, listener)

        awaitClose {
            eventSource.cancel()
        }
    }

    override suspend fun clearSession(sessionId: String): Result<Unit> = suspendRunCatching {
        val response = chatApi.clearChat(ClearChatRequest(sessionId))
        if (!response.isSuccessful) {
            throw Exception("Failed to clear chat: ${response.code()}")
        }
    }

    override suspend fun speak(text: String, lang: String): Result<ByteArray?> = suspendRunCatching {
        val response = audioApi.speak(SpeakRequest(text = text, lang = lang, context = "thoth_chat"))
        when (response.code()) {
            200 -> response.body()?.bytes()
            204 -> null // Use local TTS fallback
            else -> throw Exception("TTS failed: ${response.code()}")
        }
    }

    override suspend fun transcribe(audioFile: java.io.File, lang: String): Result<String> = suspendRunCatching {
        val filePart = MultipartBody.Part.createFormData(
            "file",
            audioFile.name,
            audioFile.asRequestBody("audio/wav".toMediaType()),
        )
        val langPart = lang.toRequestBody("text/plain".toMediaType())
        val response = audioApi.stt(filePart, langPart)
        if (response.isSuccessful) {
            response.body()?.text ?: ""
        } else {
            throw Exception("STT failed: ${response.code()}")
        }
    }
}
