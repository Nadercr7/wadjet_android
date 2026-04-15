package com.wadjet.core.data.repository

import com.wadjet.core.data.ApiException
import com.wadjet.core.network.api.TranslateApiService
import com.wadjet.core.network.model.TranslateResponse
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import retrofit2.Response

class TranslateRepositoryImplTest {

    private val translateApi: TranslateApiService = mockk()
    private lateinit var repository: TranslateRepositoryImpl

    @Before
    fun setup() {
        repository = TranslateRepositoryImpl(translateApi)
    }

    @Test
    fun `translate maps successful response to domain model`() = runTest {
        coEvery { translateApi.translate(any()) } returns Response.success(
            TranslateResponse(
                transliteration = "nfr",
                english = "beautiful",
                arabic = "جميل",
                context = "An adjective meaning beautiful or good",
                provider = "gemini",
                fromCache = false,
            ),
        )

        val result = repository.translate("nfr", "F35")
        assertTrue(result.isSuccess)

        val translation = result.getOrThrow()
        assertEquals("nfr", translation.transliteration)
        assertEquals("beautiful", translation.english)
        assertEquals("جميل", translation.arabic)
        assertEquals("gemini", translation.provider)
        assertEquals(false, translation.fromCache)
    }

    @Test
    fun `translate throws ApiException on 200 with error body`() = runTest {
        coEvery { translateApi.translate(any()) } returns Response.success(
            TranslateResponse(
                error = "Unknown transliteration",
            ),
        )

        val result = repository.translate("xyz", null)
        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull() is ApiException)
        assertEquals("Unknown transliteration", result.exceptionOrNull()?.message)
    }

    @Test
    fun `translate throws ApiException on HTTP error`() = runTest {
        coEvery { translateApi.translate(any()) } returns Response.error(
            500,
            "server error".toResponseBody(),
        )

        val result = repository.translate("nfr", null)
        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull() is ApiException)
    }

    @Test
    fun `translate with null gardiner sequence`() = runTest {
        coEvery { translateApi.translate(any()) } returns Response.success(
            TranslateResponse(
                transliteration = "ra",
                english = "sun god",
                arabic = "إله الشمس",
                context = "The sun god Ra",
                provider = "groq",
                fromCache = true,
            ),
        )

        val result = repository.translate("ra", null)
        assertTrue(result.isSuccess)
        assertEquals("sun god", result.getOrThrow().english)
        assertEquals(true, result.getOrThrow().fromCache)
    }
}
