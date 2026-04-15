package com.wadjet.core.data.repository

import com.wadjet.core.network.api.AudioApiService
import com.wadjet.core.network.api.ChatApiService
import com.wadjet.core.network.model.ClearChatResponse
import com.wadjet.core.network.model.SttResponse
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import retrofit2.Response

class ChatRepositoryImplTest {

    private val chatApi: ChatApiService = mockk()
    private val audioApi: AudioApiService = mockk()
    private val okHttpClient: OkHttpClient = mockk()
    private val json = Json { ignoreUnknownKeys = true }
    private val baseUrl = "https://api.wadjet.test/"

    private lateinit var repository: ChatRepositoryImpl

    @Before
    fun setup() {
        repository = ChatRepositoryImpl(chatApi, audioApi, okHttpClient, json, baseUrl)
    }

    @Test
    fun `clearSession succeeds on 200`() = runTest {
        coEvery { chatApi.clearChat(any()) } returns Response.success(ClearChatResponse(status = "ok"))

        val result = repository.clearSession("session-123")
        assertTrue(result.isSuccess)
    }

    @Test
    fun `clearSession fails on HTTP error`() = runTest {
        coEvery { chatApi.clearChat(any()) } returns Response.error(
            500,
            "error".toResponseBody(),
        )

        val result = repository.clearSession("session-123")
        assertTrue(result.isFailure)
    }

    @Test
    fun `speak returns bytes on 200`() = runTest {
        val audioBytes = "audio-data".toByteArray()
        val responseBody = audioBytes.toResponseBody("audio/mpeg".toMediaType())
        coEvery { audioApi.speak(any()) } returns Response.success(responseBody)

        val result = repository.speak("hello", "en")
        assertTrue(result.isSuccess)
        assertEquals("audio-data", String(result.getOrThrow()!!))
    }

    @Test
    fun `speak returns null on 204`() = runTest {
        val rawResponse = okhttp3.Response.Builder()
            .request(okhttp3.Request.Builder().url("https://test.wadjet.test/api/audio/speak").build())
            .protocol(okhttp3.Protocol.HTTP_1_1)
            .code(204)
            .message("No Content")
            .build()
        coEvery { audioApi.speak(any()) } returns Response.success(null, rawResponse)

        val result = repository.speak("hello", "en")
        assertTrue(result.isSuccess)
        assertNull(result.getOrThrow())
    }

    @Test
    fun `speak fails on error code`() = runTest {
        coEvery { audioApi.speak(any()) } returns Response.error(
            500,
            "error".toResponseBody(),
        )

        val result = repository.speak("hello", "en")
        assertTrue(result.isFailure)
    }

    @Test
    fun `transcribe returns text on success`() = runTest {
        coEvery { audioApi.stt(any(), any()) } returns Response.success(
            SttResponse(text = "transcribed text", language = "en"),
        )

        val result = repository.transcribe(java.io.File("test.wav"), "en")
        assertTrue(result.isSuccess)
        assertEquals("transcribed text", result.getOrThrow())
    }

    @Test
    fun `transcribe fails on HTTP error`() = runTest {
        coEvery { audioApi.stt(any(), any()) } returns Response.error(
            400,
            "bad request".toResponseBody(),
        )

        val result = repository.transcribe(java.io.File("test.wav"), "en")
        assertTrue(result.isFailure)
    }
}
