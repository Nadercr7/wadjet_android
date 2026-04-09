package com.wadjet.feature.landing

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.wadjet.core.domain.model.ScanHistorySummary
import com.wadjet.core.domain.model.StoryProgress
import com.wadjet.core.domain.model.StorySummary
import com.wadjet.core.domain.model.User
import com.wadjet.core.domain.repository.AuthRepository
import com.wadjet.core.domain.repository.ScanRepository
import com.wadjet.core.domain.repository.StoriesRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class LandingUiState(
    val userName: String? = null,
    val recentScan: ScanHistorySummary? = null,
    val inProgressStory: StorySummary? = null,
    val inProgressStoryChapter: Int = 0,
)

@HiltViewModel
class LandingViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val scanRepository: ScanRepository,
    private val storiesRepository: StoriesRepository,
) : ViewModel() {

    private val _state = MutableStateFlow(LandingUiState())
    val state: StateFlow<LandingUiState> = _state.asStateFlow()

    init {
        loadUserData()
    }

    private fun loadUserData() {
        viewModelScope.launch {
            // Load user name
            val user: User? = authRepository.currentUser.firstOrNull()
            _state.update { it.copy(userName = user?.displayName) }
        }
        viewModelScope.launch {
            // Load most recent scan
            val history = scanRepository.getScanHistory().firstOrNull()
            _state.update { it.copy(recentScan = history?.firstOrNull()) }
        }
        viewModelScope.launch {
            // Load in-progress story
            val allProgress: Map<String, StoryProgress>? =
                storiesRepository.getAllProgress().firstOrNull()
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
        }
    }
}
