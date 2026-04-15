package com.wadjet.feature.landing

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.wadjet.core.domain.model.ScanHistorySummary
import com.wadjet.core.domain.model.StoryProgress
import com.wadjet.core.domain.model.StorySummary
import com.wadjet.core.domain.model.User
import com.wadjet.core.domain.model.UserLimits
import com.wadjet.core.domain.repository.AuthRepository
import com.wadjet.core.domain.repository.ScanRepository
import com.wadjet.core.domain.repository.StoriesRepository
import com.wadjet.core.domain.repository.UserRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class LandingUiState(
    val isLoading: Boolean = true,
    val isRefreshing: Boolean = false,
    val error: String? = null,
    val userName: String? = null,
    val recentScan: ScanHistorySummary? = null,
    val inProgressStory: StorySummary? = null,
    val inProgressStoryChapter: Int = 0,
    val limits: UserLimits? = null,
)

@HiltViewModel
class LandingViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val scanRepository: ScanRepository,
    private val storiesRepository: StoriesRepository,
    private val userRepository: UserRepository,
) : ViewModel() {

    private val _state = MutableStateFlow(LandingUiState())
    val state: StateFlow<LandingUiState> = _state.asStateFlow()

    init {
        loadUserData()
    }

    fun refresh() {
        _state.update { it.copy(isRefreshing = true, error = null) }
        loadUserData()
    }

    private fun loadUserData() {
        viewModelScope.launch {
            try {
                // Load user name
                val user: User? = authRepository.currentUser.firstOrNull()
                _state.update { it.copy(userName = user?.displayName) }

                // Launch parallel requests
                val limitsDeferred = async { userRepository.getLimits() }
                val historyDeferred = async { scanRepository.getScanHistory().firstOrNull() }
                val progressDeferred = async { storiesRepository.getAllProgress().firstOrNull() }

                // Process results
                limitsDeferred.await()
                    .onSuccess { limits -> _state.update { it.copy(limits = limits) } }

                val history = historyDeferred.await()
                _state.update { it.copy(recentScan = history?.firstOrNull()) }

                val allProgress: Map<String, StoryProgress>? = progressDeferred.await()
                val inProgress = allProgress?.entries
                    ?.firstOrNull { !it.value.completed }
                if (inProgress != null) {
                    val stories = storiesRepository.getStories().getOrNull()
                    val story = stories?.firstOrNull { it.id == inProgress.key }
                    _state.update {
                        it.copy(
                            inProgressStory = story,
                            inProgressStoryChapter = inProgress.value.chapterIndex,
                        )
                    }
                }
                _state.update { it.copy(isLoading = false, isRefreshing = false) }
            } catch (e: Exception) {
                _state.update { it.copy(isLoading = false, isRefreshing = false, error = e.message) }
            }
        }
    }
}
