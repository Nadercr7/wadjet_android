package com.wadjet.feature.stories

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.wadjet.core.domain.model.StoryProgress
import com.wadjet.core.domain.model.StorySummary
import com.wadjet.core.domain.repository.StoriesRepository
import com.wadjet.core.domain.repository.UserRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

val DIFFICULTY_FILTERS = listOf("All", "Beginner", "Intermediate", "Advanced")

data class StoriesUiState(
    val stories: List<StorySummary> = emptyList(),
    val progress: Map<String, StoryProgress> = emptyMap(),
    val selectedDifficulty: String = "All",
    val isLoading: Boolean = false,
    val error: String? = null,
    val favorites: Set<String> = emptySet(),
) {
    val filteredStories: List<StorySummary>
        get() {
            val list = if (selectedDifficulty == "All") stories
                       else stories.filter { it.difficulty.equals(selectedDifficulty, ignoreCase = true) }
            return list.sortedBy { DIFFICULTY_ORDER[it.difficulty.lowercase()] ?: 99 }
        }
}

private val DIFFICULTY_ORDER = mapOf("beginner" to 0, "intermediate" to 1, "advanced" to 2)

@HiltViewModel
class StoriesViewModel @Inject constructor(
    private val storiesRepository: StoriesRepository,
    private val userRepository: UserRepository,
) : ViewModel() {

    private val _state = MutableStateFlow(StoriesUiState())
    val state: StateFlow<StoriesUiState> = _state.asStateFlow()

    init {
        loadStories()
        observeProgress()
        loadFavorites()
    }

    fun toggleStoryFavorite(storyId: String) {
        val isFav = storyId in _state.value.favorites
        viewModelScope.launch {
            if (isFav) {
                _state.update { it.copy(favorites = it.favorites - storyId) }
                userRepository.removeFavorite("story", storyId).onFailure {
                    _state.update { it.copy(favorites = it.favorites + storyId) }
                }
            } else {
                _state.update { it.copy(favorites = it.favorites + storyId) }
                userRepository.addFavorite("story", storyId).onFailure {
                    _state.update { it.copy(favorites = it.favorites - storyId) }
                }
            }
        }
    }

    fun selectDifficulty(difficulty: String) {
        _state.update { it.copy(selectedDifficulty = difficulty) }
    }

    fun refresh() {
        loadStories()
    }

    fun dismissError() {
        _state.update { it.copy(error = null) }
    }

    private fun loadStories() {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, error = null) }
            storiesRepository.getStories()
                .onSuccess { stories ->
                    _state.update { it.copy(stories = stories, isLoading = false) }
                }
                .onFailure { error ->
                    Timber.e(error, "Failed to load stories")
                    _state.update { it.copy(isLoading = false, error = error.message) }
                }
        }
    }

    private fun observeProgress() {
        viewModelScope.launch {
            storiesRepository.getAllProgress().collect { progress ->
                _state.update { it.copy(progress = progress) }
            }
        }
    }

    private fun loadFavorites() {
        viewModelScope.launch {
            userRepository.getFavorites()
                .onSuccess { items ->
                    val storyIds = items.filter { it.itemType == "story" }.map { it.itemId }.toSet()
                    _state.update { it.copy(favorites = storyIds) }
                }
                .onFailure { e ->
                    Timber.w(e, "Failed to load favorites")
                }
        }
    }
}
