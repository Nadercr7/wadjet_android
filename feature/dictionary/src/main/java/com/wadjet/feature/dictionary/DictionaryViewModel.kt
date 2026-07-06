package com.wadjet.feature.dictionary

import com.wadjet.core.common.audio.AudioPlaybackManager
import androidx.lifecycle.ViewModel
import com.wadjet.core.common.StringResolver
import androidx.lifecycle.viewModelScope
import com.wadjet.core.domain.model.Category
import com.wadjet.core.domain.model.Sign
import com.wadjet.core.domain.model.SignPage
import com.wadjet.core.domain.repository.DictionaryRepository
import com.wadjet.core.domain.repository.TtsPreferencesRepository
import com.wadjet.core.domain.repository.UserRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

data class AlphabetUiState(
    val signs: List<Sign> = emptyList(),
    val isLoading: Boolean = false,
    val lessonCount: Int = 5,
)

data class BrowseUiState(
    val signs: List<Sign> = emptyList(),
    val categories: List<Category> = emptyList(),
    val selectedCategory: String? = null,
    val selectedType: String? = null,
    val searchQuery: String = "",
    val page: Int = 1,
    val totalPages: Int = 1,
    val isLoading: Boolean = false,
    val isLoadingMore: Boolean = false,
    val error: String? = null,
    val selectedSign: Sign? = null,
    val favorites: Set<String> = emptySet(),
    val isOfflineData: Boolean = false,
)

val SIGN_TYPES = listOf("All", "uniliteral", "biliteral", "triliteral", "logogram", "determinative")

@HiltViewModel
class DictionaryViewModel @Inject constructor(
    private val repository: DictionaryRepository,
    private val userRepository: UserRepository,
    private val ttsPreferences: TtsPreferencesRepository,
    private val toastController: com.wadjet.core.common.ToastController,
    private val audioPlayer: AudioPlaybackManager,
    private val strings: StringResolver,
) : ViewModel() {

    private val _state = MutableStateFlow(BrowseUiState())
    val state: StateFlow<BrowseUiState> = _state.asStateFlow()

    private val _alphabetState = MutableStateFlow(AlphabetUiState())
    val alphabetState: StateFlow<AlphabetUiState> = _alphabetState.asStateFlow()

    private var searchJob: Job? = null
    private var loadSignsJob: Job? = null
    private var isSpeaking = false
    private val lang: String get() = com.wadjet.core.common.AppLanguage.current()

    init {
        loadCategories()
        loadSigns()
        loadFavorites()
    }

    private fun loadFavorites() {
        viewModelScope.launch {
            userRepository.getFavorites().onSuccess { items ->
                val glyphIds = items.filter { it.itemType == "glyph" }.map { it.itemId }.toSet()
                _state.update { it.copy(favorites = glyphIds) }
            }
        }
    }

    fun toggleGlyphFavorite(signCode: String) {
        val isFav = signCode in _state.value.favorites
        viewModelScope.launch {
            if (isFav) {
                _state.update { it.copy(favorites = it.favorites - signCode) }
                userRepository.removeFavorite("glyph", signCode).onFailure {
                    _state.update { it.copy(favorites = it.favorites + signCode) }
                }
            } else {
                _state.update { it.copy(favorites = it.favorites + signCode) }
                userRepository.addFavorite("glyph", signCode).onFailure {
                    _state.update { it.copy(favorites = it.favorites - signCode) }
                }
            }
        }
    }

    private fun loadCategories() {
        viewModelScope.launch {
            repository.getCategories(lang = lang)
                .onSuccess { cats -> _state.update { it.copy(categories = cats) } }
        }
    }

    fun loadSigns(page: Int = 1) {
        if (page > 1 && _state.value.isLoadingMore) return
        if (page == 1) loadSignsJob?.cancel()
        loadSignsJob = viewModelScope.launch {
            _state.update {
                if (page == 1) it.copy(isLoading = true, error = null)
                else it.copy(isLoadingMore = true)
            }
            val current = _state.value
            repository.getSigns(
                category = current.selectedCategory,
                type = current.selectedType,
                search = current.searchQuery.ifBlank { null },
                page = page,
                lang = lang,
            ).onSuccess { result ->
                _state.update {
                    it.copy(
                        signs = if (page == 1) result.signs else it.signs + result.signs,
                        page = result.page,
                        totalPages = result.totalPages,
                        isLoading = false,
                        isLoadingMore = false,                        isOfflineData = result.isOfflineData,                    )
                }
            }.onFailure { e ->
                _state.update { it.copy(isLoading = false, isLoadingMore = false, error = e.message) }
            }
        }
    }

    fun loadMore() {
        val s = _state.value
        if (s.page < s.totalPages && !s.isLoadingMore) {
            loadSigns(s.page + 1)
        }
    }

    fun selectCategory(category: String?) {
        _state.update { it.copy(selectedCategory = category, page = 1) }
        loadSigns()
    }

    fun selectType(type: String?) {
        _state.update { it.copy(selectedType = if (type == "All") null else type, page = 1) }
        loadSigns()
    }

    fun onSearchQueryChange(query: String) {
        _state.update { it.copy(searchQuery = query) }
        searchJob?.cancel()
        searchJob = viewModelScope.launch {
            delay(400) // debounce
            // Minimum 2 chars for search, or blank to clear search
            if (query.isNotBlank() && query.trim().length < 2) return@launch
            loadSigns()
        }
    }

    fun selectSign(sign: Sign?) {
        _state.update { it.copy(selectedSign = sign) }
    }

    fun dismissError() {
        _state.update { it.copy(error = null) }
    }

    fun showToast(message: String) {
        toastController.success(message)
    }

    fun speakSign(text: String) {
        if (isSpeaking) return
        isSpeaking = true
        viewModelScope.launch {
            val speed = ttsPreferences.ttsSpeed.first()
            // H-05: server TTS disabled or unavailable -> on-device voice (web parity)
            if (!ttsPreferences.ttsEnabled.first()) {
                audioPlayer.speakLocal(text, speed)
                isSpeaking = false
                return@launch
            }
            toastController.info(strings.get(R.string.dict_generating_pronunciation))
            repository.speakPhonetic(text).onSuccess { bytes ->
                if (bytes != null) {
                    audioPlayer.playWavBytes(
                        bytes = bytes,
                        prefix = "dict_tts_",
                        speed = speed,
                        onError = { audioPlayer.speakLocal(text, speed) },
                    )
                } else {
                    audioPlayer.speakLocal(text, speed)
                }
            }.onFailure {
                Timber.e(it, "Dictionary TTS failed; falling back to local voice")
                audioPlayer.speakLocal(text, speed)
            }
            isSpeaking = false
        }
    }

    fun loadAlphabet() {
        viewModelScope.launch {
            _alphabetState.update { it.copy(isLoading = true) }
            repository.getAlphabet(lang = lang)
                .onSuccess { signs -> _alphabetState.update { it.copy(signs = signs, isLoading = false) } }
                .onFailure { e ->
                    Timber.e(e, "Failed to load alphabet")
                    _alphabetState.update { it.copy(isLoading = false) }
                }
            // Fetch lesson count from first lesson metadata
            repository.getLesson(1, lang)
                .onSuccess { lesson ->
                    _alphabetState.update { it.copy(lessonCount = lesson.totalLessons) }
                }
        }
    }

    override fun onCleared() {
        super.onCleared()
        audioPlayer.stop()
    }
}
