package com.wadjet.core.data.repository

import com.wadjet.core.database.dao.SignDao
import com.wadjet.core.database.entity.SignEntity
import com.wadjet.core.network.api.AudioApiService
import com.wadjet.core.network.api.DictionaryApiService
import com.wadjet.core.network.api.WriteApiService
import com.wadjet.core.network.model.CategoriesResponse
import com.wadjet.core.network.model.CategoryDto
import com.wadjet.core.network.model.DictionaryResponse
import com.wadjet.core.network.model.SignDetailDto
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import retrofit2.Response

class DictionaryRepositoryImplTest {

    private val dictionaryApi: DictionaryApiService = mockk()
    private val writeApi: WriteApiService = mockk()
    private val audioApi: AudioApiService = mockk()
    private val signDao: SignDao = mockk(relaxed = true)

    private lateinit var repository: DictionaryRepositoryImpl

    private val testDto = SignDetailDto(
        code = "A1",
        unicodeChar = "\uD80C\uDC02",
        transliteration = "s",
        description = "seated man",
        type = "logogram",
        category = "A",
        categoryName = "Man and his activities",
    )

    @Before
    fun setup() {
        repository = DictionaryRepositoryImpl(dictionaryApi, writeApi, audioApi, signDao)
    }

    @Test
    fun `getSigns returns mapped domain models`() = runTest {
        coEvery { dictionaryApi.getSigns(any(), any(), any(), any(), any(), any()) } returns Response.success(
            DictionaryResponse(signs = listOf(testDto), total = 1, page = 1, perPage = 30, totalPages = 1),
        )

        val result = repository.getSigns()
        assertTrue(result.isSuccess)

        val page = result.getOrThrow()
        assertEquals(1, page.signs.size)
        assertEquals("A1", page.signs.first().code)
        assertEquals("seated man", page.signs.first().description)
    }

    @Test
    fun `getSigns caches to Room`() = runTest {
        coEvery { dictionaryApi.getSigns(any(), any(), any(), any(), any(), any()) } returns Response.success(
            DictionaryResponse(signs = listOf(testDto), total = 1, page = 1, perPage = 30, totalPages = 1),
        )

        repository.getSigns()

        coVerify { signDao.insertAll(any()) }
    }

    @Test
    fun `getSigns falls back to Room on network failure`() = runTest {
        coEvery { dictionaryApi.getSigns(any(), any(), any(), any(), any(), any()) } throws
            java.io.IOException("No network")

        val cachedEntity = SignEntity(
            code = "A1",
            glyph = "\uD80C\uDC02",
            transliteration = "s",
            description = "seated man",
            type = "logogram",
            category = "A",
            categoryName = "Man and his activities",
        )
        coEvery { signDao.getAll(any(), any()) } returns listOf(cachedEntity)

        val result = repository.getSigns()
        assertTrue(result.isSuccess)
        assertEquals("A1", result.getOrThrow().signs.first().code)
    }

    @Test
    fun `getSign falls back to cache`() = runTest {
        coEvery { dictionaryApi.getSign(any(), any()) } returns Response.error(
            500,
            okhttp3.ResponseBody.create(null, "error"),
        )

        val cachedEntity = SignEntity(
            code = "A1",
            glyph = "\uD80C\uDC02",
            transliteration = "s",
            description = "seated man",
            type = "logogram",
            category = "A",
            categoryName = "Man and his activities",
        )
        coEvery { signDao.getByCode("A1") } returns cachedEntity

        val result = repository.getSign("A1")
        assertTrue(result.isSuccess)
        assertEquals("seated man", result.getOrThrow().description)
    }

    @Test
    fun `getCategories maps correctly`() = runTest {
        coEvery { dictionaryApi.getCategories(any()) } returns Response.success(
            CategoriesResponse(categories = listOf(CategoryDto(code = "A", name = "Man", count = 55))),
        )

        val result = repository.getCategories()
        assertTrue(result.isSuccess)
        assertEquals("A", result.getOrThrow().first().code)
        assertEquals(55, result.getOrThrow().first().count)
    }

    @Test
    fun `searchOffline uses FTS`() = runTest {
        val cached = SignEntity(
            code = "A1",
            glyph = "\uD80C\uDC02",
            transliteration = "s",
            description = "seated man",
            type = "logogram",
            category = "A",
            categoryName = "Man and his activities",
        )
        coEvery { signDao.search(any(), any()) } returns listOf(cached)

        val results = repository.searchOffline("seated")
        assertEquals(1, results.size)
        assertEquals("A1", results.first().code)
    }
}
