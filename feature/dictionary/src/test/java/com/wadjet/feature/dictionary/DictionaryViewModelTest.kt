package com.wadjet.feature.dictionary

import app.cash.turbine.test
import com.wadjet.core.domain.model.Category
import com.wadjet.core.domain.model.Sign
import com.wadjet.core.domain.model.SignPage
import com.wadjet.core.domain.repository.DictionaryRepository
import com.wadjet.core.domain.repository.UserRepository
import com.wadjet.core.common.ToastController
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class DictionaryViewModelTest {

    private val repository: DictionaryRepository = mockk(relaxed = true)
    private val userRepository: UserRepository = mockk(relaxed = true)
    private val toastController: ToastController = mockk(relaxed = true)
    private lateinit var viewModel: DictionaryViewModel
    private val testDispatcher = UnconfinedTestDispatcher()

    private val testSign = Sign(
        code = "A1",
        glyph = "\uD80C\uDC02",
        transliteration = "s",
        description = "seated man",
        type = "logogram",
        typeName = "Logogram",
        category = "A",
        categoryName = "Man and his activities",
        reading = null,
        isPhonetic = false,
        funFact = "Most common sign",
        speechText = null,
        pronunciationSound = null,
        pronunciationExample = null,
    )

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        coEvery { repository.getCategories(any()) } returns Result.success(
            listOf(Category(code = "A", name = "Man and his activities", count = 55)),
        )
        coEvery { repository.getSigns(any(), any(), any(), any(), any()) } returns Result.success(
            SignPage(signs = listOf(testSign), total = 1, page = 1, totalPages = 1),
        )
        coEvery { userRepository.getFavorites() } returns Result.success(emptyList())
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun createViewModel() = DictionaryViewModel(repository, userRepository, toastController).also { viewModel = it }

    @Test
    fun `init loads categories and signs`() = runTest {
        createViewModel()

        val state = viewModel.state.value
        assertFalse(state.isLoading)
        assertEquals(1, state.categories.size)
        assertEquals("A", state.categories.first().code)
        assertEquals(1, state.signs.size)
        assertEquals("A1", state.signs.first().code)
    }

    @Test
    fun `selectCategory updates state and reloads`() = runTest {
        createViewModel()
        viewModel.selectCategory("A")

        assertEquals("A", viewModel.state.value.selectedCategory)
        coVerify(exactly = 1) { repository.getSigns(category = "A", any(), any(), any(), any()) }
        coVerify(atLeast = 2) { repository.getSigns(any(), any(), any(), any(), any()) }
    }

    @Test
    fun `selectType filters correctly`() = runTest {
        createViewModel()
        viewModel.selectType("logogram")

        assertEquals("logogram", viewModel.state.value.selectedType)

        viewModel.selectType("All")
        assertNull(viewModel.state.value.selectedType)
    }

    @Test
    fun `loadSigns failure sets error`() = runTest {
        coEvery { repository.getSigns(any(), any(), any(), any(), any()) } returns
            Result.failure(RuntimeException("Network error"))

        createViewModel()

        assertEquals("Network error", viewModel.state.value.error)
    }

    @Test
    fun `loadMore increments page`() = runTest {
        coEvery { repository.getSigns(any(), any(), any(), any(), any()) } returns Result.success(
            SignPage(signs = listOf(testSign), total = 60, page = 1, totalPages = 2),
        )
        createViewModel()

        coEvery { repository.getSigns(any(), any(), any(), page = 2, any()) } returns Result.success(
            SignPage(signs = listOf(testSign.copy(code = "A2")), total = 60, page = 2, totalPages = 2),
        )
        viewModel.loadMore()

        assertEquals(2, viewModel.state.value.signs.size)
        assertEquals(2, viewModel.state.value.page)
    }

    @Test
    fun `loadMore does nothing at last page`() = runTest {
        createViewModel() // page=1, totalPages=1

        viewModel.loadMore()
        // Should not call getSigns for page 2
        coVerify(exactly = 1) { repository.getSigns(any(), any(), any(), page = any(), any()) }
    }

    @Test
    fun `dismissError clears error`() = runTest {
        coEvery { repository.getSigns(any(), any(), any(), any(), any()) } returns
            Result.failure(RuntimeException("fail"))
        createViewModel()

        assertTrue(viewModel.state.value.error != null)
        viewModel.dismissError()
        assertNull(viewModel.state.value.error)
    }

    @Test
    fun `selectSign sets and clears selection`() = runTest {
        createViewModel()

        viewModel.selectSign(testSign)
        assertEquals(testSign, viewModel.state.value.selectedSign)

        viewModel.selectSign(null)
        assertNull(viewModel.state.value.selectedSign)
    }

    @Test
    fun `state flow emits updates`() = runTest {
        createViewModel()

        viewModel.state.test {
            val initial = awaitItem()
            assertFalse(initial.isLoading)
            assertEquals(1, initial.signs.size)

            viewModel.selectSign(testSign)
            val updated = awaitItem()
            assertEquals(testSign, updated.selectedSign)

            cancelAndIgnoreRemainingEvents()
        }
    }
}
