package com.wadjet.feature.explore

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.wadjet.core.domain.model.Landmark
import com.wadjet.core.domain.model.LandmarkPage
import com.wadjet.core.domain.repository.ExploreRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

val CATEGORIES = listOf("All", "Pharaonic", "Islamic", "Coptic", "Greco-Roman", "Museum", "Natural")

data class ExploreUiState(
    val landmarks: List<Landmark> = emptyList(),
    val isLoading: Boolean = false,
    val isLoadingMore: Boolean = false,
    val error: String? = null,
    val selectedCategory: String = "All",
    val selectedCity: String? = null,
    val searchQuery: String = "",
    val cities: List<String> = emptyList(),
    val favorites: Set<String> = emptySet(),
    val currentPage: Int = 1,
    val totalPages: Int = 1,
    val isRefreshing: Boolean = false,
)

@HiltViewModel
class ExploreViewModel @Inject constructor(
    private val exploreRepository: ExploreRepository,
) : ViewModel() {

    private val _state = MutableStateFlow(ExploreUiState())
    val state: StateFlow<ExploreUiState> = _state.asStateFlow()

    private var searchJob: Job? = null

    init {
        loadLandmarks()
        loadCities()
        observeFavorites()
    }

    fun selectCategory(category: String) {
        _state.update { it.copy(selectedCategory = category, currentPage = 1, landmarks = emptyList()) }
        loadLandmarks()
    }

    fun selectCity(city: String?) {
        _state.update { it.copy(selectedCity = city, currentPage = 1, landmarks = emptyList()) }
        loadLandmarks()
    }

    fun updateSearch(query: String) {
        _state.update { it.copy(searchQuery = query, currentPage = 1) }
        searchJob?.cancel()
        searchJob = viewModelScope.launch {
            kotlinx.coroutines.delay(400) // Debounce
            _state.update { it.copy(landmarks = emptyList()) }
            loadLandmarks()
        }
    }

    fun loadMore() {
        val s = _state.value
        if (s.isLoadingMore || s.currentPage >= s.totalPages) return
        _state.update { it.copy(isLoadingMore = true) }
        viewModelScope.launch {
            val nextPage = s.currentPage + 1
            exploreRepository.getLandmarks(
                category = s.selectedCategory.takeIf { it != "All" },
                city = s.selectedCity,
                search = s.searchQuery.takeIf { it.isNotBlank() },
                page = nextPage,
            ).onSuccess { page ->
                _state.update {
                    it.copy(
                        landmarks = it.landmarks + page.landmarks,
                        currentPage = nextPage,
                        totalPages = page.totalPages,
                        isLoadingMore = false,
                    )
                }
            }.onFailure { error ->
                Timber.e(error, "Load more failed")
                _state.update { it.copy(isLoadingMore = false) }
            }
        }
    }

    fun refresh() {
        _state.update { it.copy(isRefreshing = true, currentPage = 1, landmarks = emptyList()) }
        loadLandmarks()
    }

    fun toggleFavorite(landmark: Landmark) {
        val isFav = _state.value.favorites.contains(landmark.slug)
        viewModelScope.launch {
            exploreRepository.toggleFavorite(
                slug = landmark.slug,
                name = landmark.name,
                thumbnail = landmark.thumbnail,
                isFavorite = isFav,
            ).onFailure { Timber.e(it, "Toggle favorite failed") }
        }
    }

    fun dismissError() {
        _state.update { it.copy(error = null) }
    }

    private fun loadLandmarks() {
        _state.update { it.copy(isLoading = true, error = null) }
        viewModelScope.launch {
            val s = _state.value
            exploreRepository.getLandmarks(
                category = s.selectedCategory.takeIf { it != "All" },
                city = s.selectedCity,
                search = s.searchQuery.takeIf { it.isNotBlank() },
                page = 1,
            ).onSuccess { page ->
                _state.update {
                    it.copy(
                        landmarks = page.landmarks,
                        totalPages = page.totalPages,
                        currentPage = 1,
                        isLoading = false,
                        isRefreshing = false,
                    )
                }
            }.onFailure { error ->
                Timber.e(error, "Load landmarks failed")
                // Fallback to offline cache
                val cached = if (s.searchQuery.isNotBlank()) {
                    exploreRepository.searchOffline(s.searchQuery)
                } else {
                    emptyList()
                }
                _state.update {
                    it.copy(
                        landmarks = cached.ifEmpty { it.landmarks },
                        error = error.message ?: "Failed to load landmarks",
                        isLoading = false,
                        isRefreshing = false,
                    )
                }
            }
        }
    }

    private fun loadCities() {
        viewModelScope.launch {
            val cities = exploreRepository.getCities()
            _state.update { it.copy(cities = cities) }
        }
    }

    private fun observeFavorites() {
        viewModelScope.launch {
            exploreRepository.getFavorites().collect { favs ->
                _state.update { it.copy(favorites = favs) }
            }
        }
    }
}
