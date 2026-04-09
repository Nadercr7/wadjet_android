package com.wadjet.feature.explore

import app.cash.turbine.test
import com.wadjet.core.domain.model.Landmark
import com.wadjet.core.domain.model.LandmarkPage
import com.wadjet.core.domain.repository.ExploreRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
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
class ExploreViewModelTest {

    private val repository: ExploreRepository = mockk(relaxed = true)
    private lateinit var viewModel: ExploreViewModel
    private val testDispatcher = UnconfinedTestDispatcher()

    private val testLandmark = Landmark(
        slug = "pyramids-of-giza",
        name = "Pyramids of Giza",
        nameAr = "أهرامات الجيزة",
        city = "Giza",
        type = "Pharaonic",
        era = "Old Kingdom",
        thumbnail = "https://example.com/pyramids.jpg",
        featured = true,
        popularity = 100,
    )

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        coEvery { repository.getLandmarks(any(), any(), any(), any(), any()) } returns Result.success(
            LandmarkPage(landmarks = listOf(testLandmark), total = 1, page = 1, totalPages = 1),
        )
        coEvery { repository.getCities() } returns listOf("Cairo", "Giza", "Luxor")
        every { repository.getFavorites() } returns flowOf(emptySet())
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun createViewModel() = ExploreViewModel(repository).also { viewModel = it }

    @Test
    fun `init loads landmarks and cities`() = runTest {
        createViewModel()

        val state = viewModel.state.value
        assertFalse(state.isLoading)
        assertEquals(1, state.landmarks.size)
        assertEquals("Pyramids of Giza", state.landmarks.first().name)
        assertEquals(3, state.cities.size)
    }

    @Test
    fun `selectCategory reloads landmarks`() = runTest {
        createViewModel()
        viewModel.selectCategory("Islamic")

        assertEquals("Islamic", viewModel.state.value.selectedCategory)
        coVerify { repository.getLandmarks(category = "Islamic", any(), any(), any(), any()) }
    }

    @Test
    fun `selectCity reloads landmarks`() = runTest {
        createViewModel()
        viewModel.selectCity("Luxor")

        assertEquals("Luxor", viewModel.state.value.selectedCity)
    }

    @Test
    fun `error state on failure`() = runTest {
        coEvery { repository.getLandmarks(any(), any(), any(), any(), any()) } returns
            Result.failure(RuntimeException("API down"))
        createViewModel()

        assertEquals("API down", viewModel.state.value.error)
    }

    @Test
    fun `loadMore appends landmarks`() = runTest {
        coEvery { repository.getLandmarks(any(), any(), any(), page = 1, any()) } returns Result.success(
            LandmarkPage(listOf(testLandmark), total = 2, page = 1, totalPages = 2),
        )
        createViewModel()

        val secondLandmark = testLandmark.copy(slug = "karnak-temple", name = "Karnak Temple")
        coEvery { repository.getLandmarks(any(), any(), any(), page = 2, any()) } returns Result.success(
            LandmarkPage(listOf(secondLandmark), total = 2, page = 2, totalPages = 2),
        )
        viewModel.loadMore()

        assertEquals(2, viewModel.state.value.landmarks.size)
        assertEquals(2, viewModel.state.value.currentPage)
    }

    @Test
    fun `loadMore does nothing at last page`() = runTest {
        createViewModel() // totalPages=1

        viewModel.loadMore()
        // getSigns called only once (initial load), not for page 2
        coVerify(exactly = 1) { repository.getLandmarks(any(), any(), any(), page = 1, any()) }
    }

    @Test
    fun `toggleFavorite calls repository`() = runTest {
        createViewModel()
        viewModel.toggleFavorite(testLandmark)

        coVerify {
            repository.toggleFavorite(
                slug = "pyramids-of-giza",
                name = "Pyramids of Giza",
                thumbnail = any(),
                isFavorite = false,
            )
        }
    }

    @Test
    fun `refresh resets page and reloads`() = runTest {
        createViewModel()

        viewModel.state.test {
            val initial = awaitItem()
            assertFalse(initial.isRefreshing)

            viewModel.refresh()
            // refresh might emit isRefreshing=true, then done
            val refreshed = expectMostRecentItem()
            assertFalse(refreshed.isRefreshing)
            assertEquals(1, refreshed.currentPage)

            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `dismissError clears error`() = runTest {
        coEvery { repository.getLandmarks(any(), any(), any(), any(), any()) } returns
            Result.failure(RuntimeException("fail"))
        createViewModel()

        assertTrue(viewModel.state.value.error != null)
        viewModel.dismissError()
        assertNull(viewModel.state.value.error)
    }
}
