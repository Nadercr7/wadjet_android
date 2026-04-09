package com.wadjet.feature.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.wadjet.core.domain.model.DashboardStoryProgress
import com.wadjet.core.domain.model.FavoriteItem
import com.wadjet.core.domain.model.ScanHistoryItem
import com.wadjet.core.domain.model.User
import com.wadjet.core.domain.model.UserStats
import com.wadjet.core.domain.repository.AuthRepository
import com.wadjet.core.domain.repository.UserRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

data class DashboardUiState(
    val user: User? = null,
    val stats: UserStats = UserStats(),
    val recentScans: List<ScanHistoryItem> = emptyList(),
    val favorites: List<FavoriteItem> = emptyList(),
    val storyProgress: List<DashboardStoryProgress> = emptyList(),
    val selectedFavTab: String = "landmark",
    val isLoading: Boolean = false,
    val error: String? = null,
) {
    val filteredFavorites: List<FavoriteItem>
        get() = favorites.filter { it.itemType == selectedFavTab }
}

val FAV_TABS = listOf("landmark", "glyph", "story")

@HiltViewModel
class DashboardViewModel @Inject constructor(
    private val userRepository: UserRepository,
    private val authRepository: AuthRepository,
) : ViewModel() {

    private val _state = MutableStateFlow(DashboardUiState())
    val state: StateFlow<DashboardUiState> = _state.asStateFlow()

    init {
        loadDashboard()
        observeUser()
    }

    fun selectFavTab(tab: String) {
        _state.update { it.copy(selectedFavTab = tab) }
    }

    fun refresh() {
        loadDashboard()
    }

    fun removeFavorite(itemType: String, itemId: String) {
        viewModelScope.launch {
            userRepository.removeFavorite(itemType, itemId).onSuccess {
                _state.update { state ->
                    state.copy(favorites = state.favorites.filterNot { it.itemType == itemType && it.itemId == itemId })
                }
            }
        }
    }

    private fun observeUser() {
        viewModelScope.launch {
            authRepository.currentUser.collect { user ->
                _state.update { it.copy(user = user) }
            }
        }
    }

    private fun loadDashboard() {
        _state.update { it.copy(isLoading = true, error = null) }
        viewModelScope.launch {
            // Load stats
            userRepository.getStats()
                .onSuccess { stats -> _state.update { it.copy(stats = stats) } }
                .onFailure { Timber.w(it, "Stats load failed") }

            // Load scan history
            userRepository.getScanHistory()
                .onSuccess { scans -> _state.update { it.copy(recentScans = scans) } }
                .onFailure { Timber.w(it, "Scan history load failed") }

            // Load favorites
            userRepository.getFavorites()
                .onSuccess { favs -> _state.update { it.copy(favorites = favs) } }
                .onFailure { Timber.w(it, "Favorites load failed") }

            // Load story progress
            userRepository.getStoryProgress()
                .onSuccess { progress -> _state.update { it.copy(storyProgress = progress) } }
                .onFailure { Timber.w(it, "Story progress load failed") }

            _state.update { it.copy(isLoading = false) }
        }
    }
}
