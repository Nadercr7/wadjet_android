package com.wadjet.core.data.repository

import com.wadjet.core.data.ApiException
import com.wadjet.core.domain.model.FeedbackData
import com.wadjet.core.network.api.FeedbackApiService
import com.wadjet.core.network.model.FeedbackResponse
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import retrofit2.Response

class FeedbackRepositoryImplTest {

    private val feedbackApi: FeedbackApiService = mockk()
    private lateinit var repository: FeedbackRepositoryImpl

    private val testFeedback = FeedbackData(
        category = "bug",
        message = "The scan feature crashes",
        name = "Nader",
        email = "test@example.com",
    )

    @Before
    fun setup() {
        repository = FeedbackRepositoryImpl(feedbackApi)
    }

    @Test
    fun `submitFeedback returns ID on success`() = runTest {
        coEvery { feedbackApi.submit(any()) } returns Response.success(
            FeedbackResponse(ok = true, id = 42),
        )

        val result = repository.submitFeedback(testFeedback)
        assertTrue(result.isSuccess)
        assertEquals(42, result.getOrThrow())
    }

    @Test
    fun `submitFeedback throws ApiException on HTTP error`() = runTest {
        coEvery { feedbackApi.submit(any()) } returns Response.error(
            400,
            """{"detail":"Invalid category"}""".toResponseBody(),
        )

        val result = repository.submitFeedback(testFeedback)
        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull() is ApiException)
    }

    @Test
    fun `submitFeedback throws ApiException on null body`() = runTest {
        coEvery { feedbackApi.submit(any()) } returns Response.success(null)

        val result = repository.submitFeedback(testFeedback)
        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull() is ApiException)
    }
}
