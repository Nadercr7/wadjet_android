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
import kotlinx.coroutines.async
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
            val statsDeferred = async { userRepository.getStats() }
            val scansDeferred = async { userRepository.getScanHistory() }
            val favsDeferred = async { userRepository.getFavorites() }
            val progressDeferred = async { userRepository.getStoryProgress() }

            statsDeferred.await()
                .onSuccess { stats -> _state.update { it.copy(stats = stats) } }
                .onFailure { e ->
                    Timber.w(e, "Stats load failed")
                    _state.update { it.copy(error = e.message ?: "Failed to load stats") }
                }

            scansDeferred.await()
                .onSuccess { scans -> _state.update { it.copy(recentScans = scans) } }
                .onFailure { e ->
                    Timber.w(e, "Scan history load failed")
                    _state.update { it.copy(error = e.message ?: "Failed to load scan history") }
                }

            favsDeferred.await()
                .onSuccess { favs -> _state.update { it.copy(favorites = favs) } }
                .onFailure { e ->
                    Timber.w(e, "Favorites load failed")
                    _state.update { it.copy(error = e.message ?: "Failed to load favorites") }
                }

            progressDeferred.await()
                .onSuccess { progress -> _state.update { it.copy(storyProgress = progress) } }
                .onFailure { e ->
                    Timber.w(e, "Story progress load failed")
                    _state.update { it.copy(error = e.message ?: "Failed to load story progress") }
                }

            _state.update { it.copy(isLoading = false) }
        }
    }
}
