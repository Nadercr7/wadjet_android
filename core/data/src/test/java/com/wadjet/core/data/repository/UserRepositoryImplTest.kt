package com.wadjet.core.data.repository

import com.wadjet.core.database.dao.FavoriteDao
import com.wadjet.core.database.entity.FavoriteEntity
import com.wadjet.core.network.api.UserApiService
import com.wadjet.core.network.model.AddFavoriteRequest
import com.wadjet.core.network.model.FavoriteItemDto
import com.wadjet.core.network.model.OkResponse
import com.wadjet.core.network.model.ScanHistoryItemDto
import com.wadjet.core.network.model.StoryProgressItemDto
import com.wadjet.core.network.model.UserLimitsResponse
import com.wadjet.core.network.model.LimitsDto
import com.wadjet.core.network.model.UsageDto
import com.wadjet.core.network.model.UserResponse
import com.wadjet.core.network.model.UserStatsResponse
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import retrofit2.Response

class UserRepositoryImplTest {

    private val userApi: UserApiService = mockk()
    private val favoriteDao: FavoriteDao = mockk(relaxed = true)
    private lateinit var repository: UserRepositoryImpl

    private val testUserResponse = UserResponse(
        id = "user-1",
        email = "nader@test.com",
        displayName = "Nader",
        preferredLang = "en",
        tier = "free",
        authProvider = "email",
        emailVerified = true,
        avatarUrl = "https://avatar.test/nader.jpg",
    )

    @Before
    fun setup() {
        repository = UserRepositoryImpl(userApi, favoriteDao)
    }

    @Test
    fun `getProfile maps response to User domain model`() = runTest {
        coEvery { userApi.getProfile() } returns Response.success(testUserResponse)

        val result = repository.getProfile()
        assertTrue(result.isSuccess)

        val user = result.getOrThrow()
        assertEquals("user-1", user.id)
        assertEquals("nader@test.com", user.email)
        assertEquals("Nader", user.displayName)
        assertEquals("en", user.preferredLang)
        assertEquals("free", user.tier)
        assertEquals(true, user.emailVerified)
    }

    @Test
    fun `getProfile fails on null body`() = runTest {
        coEvery { userApi.getProfile() } returns Response.success(null)

        val result = repository.getProfile()
        assertTrue(result.isFailure)
    }

    @Test
    fun `updateProfile maps response to User`() = runTest {
        val updatedResponse = testUserResponse.copy(displayName = "Nader M", preferredLang = "ar")
        coEvery { userApi.updateProfile(any()) } returns Response.success(updatedResponse)

        val result = repository.updateProfile(displayName = "Nader M", preferredLang = "ar")
        assertTrue(result.isSuccess)
        assertEquals("Nader M", result.getOrThrow().displayName)
        assertEquals("ar", result.getOrThrow().preferredLang)
    }

    @Test
    fun `getStats maps response to UserStats`() = runTest {
        coEvery { userApi.getStats() } returns Response.success(
            UserStatsResponse(scansToday = 5, totalScans = 42, storiesCompleted = 3, glyphsLearned = 25),
        )

        val result = repository.getStats()
        assertTrue(result.isSuccess)

        val stats = result.getOrThrow()
        assertEquals(5, stats.scansToday)
        assertEquals(42, stats.totalScans)
        assertEquals(3, stats.storiesCompleted)
        assertEquals(25, stats.glyphsLearned)
    }

    @Test
    fun `getFavorites returns items and caches to Room`() = runTest {
        coEvery { userApi.getFavorites() } returns Response.success(
            listOf(
                FavoriteItemDto(id = 1, itemType = "sign", itemId = "A1", createdAt = "2024-01-01"),
                FavoriteItemDto(id = 2, itemType = "landmark", itemId = "pyramids", createdAt = "2024-01-02"),
            ),
        )

        val result = repository.getFavorites()
        assertTrue(result.isSuccess)

        val favorites = result.getOrThrow()
        assertEquals(2, favorites.size)
        assertEquals("sign", favorites[0].itemType)
        assertEquals("A1", favorites[0].itemId)

        coVerify { favoriteDao.insertAll(any()) }
    }

    @Test
    fun `getFavorites falls back to Room on IOException`() = runTest {
        coEvery { userApi.getFavorites() } throws java.io.IOException("no network")
        coEvery { favoriteDao.getAll() } returns listOf(
            FavoriteEntity(itemType = "sign", itemId = "A1"),
        )

        val result = repository.getFavorites()
        assertTrue(result.isSuccess)

        val favorites = result.getOrThrow()
        assertEquals(1, favorites.size)
        assertEquals("sign", favorites[0].itemType)
        assertEquals(0, favorites[0].id) // cached favorites have id=0
    }

    @Test
    fun `addFavorite calls API and persists to Room`() = runTest {
        coEvery { userApi.addFavorite(any()) } returns Response.success(
            FavoriteItemDto(id = 5, itemType = "sign", itemId = "B3", createdAt = "2024-06-01"),
        )

        val result = repository.addFavorite("sign", "B3")
        assertTrue(result.isSuccess)

        coVerify { favoriteDao.insert(match { it.itemType == "sign" && it.itemId == "B3" }) }
    }

    @Test
    fun `addFavorite fails on HTTP error`() = runTest {
        coEvery { userApi.addFavorite(any()) } returns Response.error(
            409,
            "already exists".toResponseBody(),
        )

        val result = repository.addFavorite("sign", "A1")
        assertTrue(result.isFailure)
    }

    @Test
    fun `removeFavorite calls API and deletes from Room`() = runTest {
        coEvery { userApi.removeFavorite("sign", "A1") } returns Response.success(OkResponse(ok = true))

        val result = repository.removeFavorite("sign", "A1")
        assertTrue(result.isSuccess)

        coVerify { favoriteDao.delete("sign", "A1") }
    }

    @Test
    fun `getLimits maps nested response correctly`() = runTest {
        coEvery { userApi.getLimits() } returns Response.success(
            UserLimitsResponse(
                tier = "premium",
                limits = LimitsDto(scansPerDay = 50, chatMessagesPerDay = 100, storiesAccessible = 10),
                usage = UsageDto(scansToday = 3, chatMessagesToday = 12),
            ),
        )

        val result = repository.getLimits()
        assertTrue(result.isSuccess)

        val limits = result.getOrThrow()
        assertEquals("premium", limits.tier)
        assertEquals(50, limits.scansPerDay)
        assertEquals(100, limits.chatMessagesPerDay)
        assertEquals(10, limits.storiesAccessible)
        assertEquals(3, limits.scansToday)
        assertEquals(12, limits.chatMessagesToday)
    }

    @Test
    fun `getScanHistory maps items`() = runTest {
        coEvery { userApi.getScanHistory() } returns Response.success(
            listOf(
                ScanHistoryItemDto(id = 1, glyphCount = 3, confidenceAvg = 0.9, createdAt = "2024-01-01"),
            ),
        )

        val result = repository.getScanHistory()
        assertTrue(result.isSuccess)
        assertEquals(1, result.getOrThrow().size)
        assertEquals(3, result.getOrThrow()[0].glyphCount)
    }

    @Test
    fun `getStoryProgress maps items`() = runTest {
        coEvery { userApi.getStoryProgress() } returns Response.success(
            listOf(
                StoryProgressItemDto(id = 1, storyId = "story-1", chapterIndex = 2, glyphsLearned = "[\"A1\",\"A2\",\"A3\",\"A4\",\"A5\"]", score = 80, completed = false),
            ),
        )

        val result = repository.getStoryProgress()
        assertTrue(result.isSuccess)
        assertEquals(1, result.getOrThrow().size)
        assertEquals("story-1", result.getOrThrow()[0].storyId)
        assertEquals(80, result.getOrThrow()[0].score)
    }

    @Test
    fun `changePassword succeeds on 200`() = runTest {
        coEvery { userApi.changePassword(any()) } returns Response.success(OkResponse(ok = true))

        val result = repository.changePassword("oldPass", "newPass")
        assertTrue(result.isSuccess)
    }

    @Test
    fun `changePassword fails with detail message`() = runTest {
        coEvery { userApi.changePassword(any()) } returns Response.error(
            400,
            """{"detail":"Incorrect current password"}""".toResponseBody(),
        )

        val result = repository.changePassword("wrong", "newPass")
        assertTrue(result.isFailure)
    }
}
