package com.wadjet.core.data.repository

import com.wadjet.core.data.ApiException
import com.wadjet.core.database.dao.FavoriteDao
import com.wadjet.core.database.dao.LandmarkDao
import com.wadjet.core.database.entity.LandmarkEntity
import com.wadjet.core.network.api.LandmarkApiService
import com.wadjet.core.network.api.UserApiService
import com.wadjet.core.network.model.LandmarkListResponse
import com.wadjet.core.network.model.LandmarkSummaryDto
import com.wadjet.core.network.model.OkResponse
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.json.Json
import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import retrofit2.Response

class ExploreRepositoryImplTest {

    private val landmarkApi: LandmarkApiService = mockk()
    private val landmarkDao: LandmarkDao = mockk(relaxed = true)
    private val userApi: UserApiService = mockk()
    private val favoriteDao: FavoriteDao = mockk(relaxed = true)
    private val json = Json { ignoreUnknownKeys = true }

    private lateinit var repository: ExploreRepositoryImpl

    private val testSummaryDto = LandmarkSummaryDto(
        slug = "pyramids-giza",
        name = "Great Pyramids of Giza",
        nameAr = "أهرامات الجيزة الكبرى",
        city = "Giza",
        type = "archaeological_site",
        era = "Old Kingdom",
        thumbnail = "https://img.test/pyramids.jpg",
        featured = true,
        popularity = 100,
    )

    @Before
    fun setup() {
        repository = ExploreRepositoryImpl(landmarkApi, landmarkDao, userApi, favoriteDao, json)
    }

    @Test
    fun `getLandmarks maps response to domain model`() = runTest {
        coEvery { landmarkApi.getLandmarks(any(), any(), any(), any(), any(), any(), any()) } returns Response.success(
            LandmarkListResponse(
                landmarks = listOf(testSummaryDto),
                total = 1,
                page = 1,
                totalPages = 1,
            ),
        )

        val result = repository.getLandmarks(page = 1, perPage = 24)
        assertTrue(result.isSuccess)

        val page = result.getOrThrow()
        assertEquals(1, page.landmarks.size)
        assertEquals("pyramids-giza", page.landmarks[0].slug)
        assertEquals("Great Pyramids of Giza", page.landmarks[0].name)
        assertEquals("Giza", page.landmarks[0].city)
        assertEquals(true, page.landmarks[0].featured)
    }

    @Test
    fun `getLandmarks caches first page to Room`() = runTest {
        coEvery { landmarkApi.getLandmarks(any(), any(), any(), any(), any(), any(), any()) } returns Response.success(
            LandmarkListResponse(landmarks = listOf(testSummaryDto), total = 1, page = 1, totalPages = 1),
        )

        repository.getLandmarks(page = 1, perPage = 24)

        coVerify { landmarkDao.insertAll(any()) }
    }

    @Test
    fun `getLandmarks falls back to Room on IOException`() = runTest {
        coEvery { landmarkApi.getLandmarks(any(), any(), any(), any(), any(), any(), any()) } throws
            java.io.IOException("no network")

        val cachedEntity = LandmarkEntity(
            slug = "pyramids-giza",
            name = "Great Pyramids of Giza",
            city = "Giza",
            type = "archaeological_site",
            era = "Old Kingdom",
        )
        coEvery { landmarkDao.getFiltered(any(), any(), any(), any()) } returns listOf(cachedEntity)

        val result = repository.getLandmarks(page = 1, perPage = 24)
        assertTrue(result.isSuccess)
        assertEquals(1, result.getOrThrow().landmarks.size)
        assertEquals("pyramids-giza", result.getOrThrow().landmarks[0].slug)
    }

    @Test
    fun `getLandmarks fails on IOException with no cache`() = runTest {
        coEvery { landmarkApi.getLandmarks(any(), any(), any(), any(), any(), any(), any()) } throws
            java.io.IOException("no network")
        coEvery { landmarkDao.getFiltered(any(), any(), any(), any()) } returns emptyList()

        val result = repository.getLandmarks(page = 1, perPage = 24)
        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull() is java.io.IOException)
    }

    @Test
    fun `getLandmarks fails on HTTP error`() = runTest {
        coEvery { landmarkApi.getLandmarks(any(), any(), any(), any(), any(), any(), any()) } returns Response.error(
            500,
            "error".toResponseBody(),
        )

        val result = repository.getLandmarks(page = 1, perPage = 24)
        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull() is ApiException)
    }

    @Test
    fun `getCachedLandmarks returns Flow from Room`() = runTest {
        val entity = LandmarkEntity(
            slug = "karnak",
            name = "Karnak Temple",
            city = "Luxor",
            type = "temple",
            era = "New Kingdom",
        )
        every { landmarkDao.getAll() } returns flowOf(listOf(entity))

        val landmarks = repository.getCachedLandmarks().first()
        assertEquals(1, landmarks.size)
        assertEquals("karnak", landmarks[0].slug)
    }

    @Test
    fun `toggleFavorite removes when already favorite`() = runTest {
        coEvery { userApi.removeFavorite("landmark", "pyramids-giza") } returns Response.success(OkResponse())

        val result = repository.toggleFavorite("pyramids-giza", "Pyramids", null, isFavorite = true)
        assertTrue(result.isSuccess)
        coVerify { favoriteDao.delete("landmark", "pyramids-giza") }
    }

    @Test
    fun `toggleFavorite adds when not favorite`() = runTest {
        coEvery { userApi.addFavorite(any()) } returns Response.success(
            com.wadjet.core.network.model.FavoriteItemDto(id = 1, itemType = "landmark", itemId = "karnak"),
        )

        val result = repository.toggleFavorite("karnak", "Karnak", null, isFavorite = false)
        assertTrue(result.isSuccess)
        coVerify { favoriteDao.insert(match { it.itemType == "landmark" && it.itemId == "karnak" }) }
    }
}
