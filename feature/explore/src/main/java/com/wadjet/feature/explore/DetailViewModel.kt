package com.wadjet.feature.explore

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import com.wadjet.core.domain.model.LandmarkDetail
import com.wadjet.core.domain.repository.ExploreRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

data class DetailUiState(
    val detail: LandmarkDetail? = null,
    val isLoading: Boolean = true,
    val error: String? = null,
    val isFavorite: Boolean = false,
    val selectedTab: Int = 0,
)

@HiltViewModel
class DetailViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val exploreRepository: ExploreRepository,
) : ViewModel() {

    private val slug: String = savedStateHandle.toRoute<LandmarkDetailRoute>().slug

    private val _state = MutableStateFlow(DetailUiState())
    val state: StateFlow<DetailUiState> = _state.asStateFlow()

    init {
        loadDetail()
        observeFavorite()
    }

    fun selectTab(index: Int) {
        _state.update { it.copy(selectedTab = index) }
    }

    fun toggleFavorite() {
        val detail = _state.value.detail ?: return
        val isFav = _state.value.isFavorite
        viewModelScope.launch {
            exploreRepository.toggleFavorite(
                slug = detail.slug,
                name = detail.name,
                thumbnail = detail.thumbnail,
                isFavorite = isFav,
            ).onFailure { Timber.e(it, "Toggle favorite failed") }
        }
    }

    fun retry() {
        loadDetail()
    }

    private fun loadDetail() {
        _state.update { it.copy(isLoading = true, error = null) }
        viewModelScope.launch {
            exploreRepository.getLandmarkDetail(slug)
                .onSuccess { detail ->
                    _state.update { it.copy(detail = detail, isLoading = false) }
                }
                .onFailure { error ->
                    Timber.e(error, "Failed to load landmark detail")
                    _state.update {
                        it.copy(
                            error = error.message ?: "Failed to load landmark",
                            isLoading = false,
                        )
                    }
                }
        }
    }

    private fun observeFavorite() {
        viewModelScope.launch {
            exploreRepository.getFavorites().collect { favs ->
                _state.update { it.copy(isFavorite = favs.contains(slug)) }
            }
        }
    }
}

/** Marker for SavedStateHandle.toRoute — must match Route.LandmarkDetail shape */
@kotlinx.serialization.Serializable
data class LandmarkDetailRoute(val slug: String)
