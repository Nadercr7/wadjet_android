package com.wadjet.feature.dictionary

import android.media.MediaPlayer
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.wadjet.core.domain.model.Category
import com.wadjet.core.domain.model.Sign
import com.wadjet.core.domain.model.SignPage
import com.wadjet.core.domain.repository.DictionaryRepository
import com.wadjet.core.domain.repository.UserRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import timber.log.Timber
import java.io.File
import javax.inject.Inject

data class AlphabetUiState(
    val signs: List<Sign> = emptyList(),
    val isLoading: Boolean = false,
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
    val localTtsText: String? = null,
)

val SIGN_TYPES = listOf("All", "uniliteral", "biliteral", "triliteral", "logogram", "determinative")

@HiltViewModel
class DictionaryViewModel @Inject constructor(
    private val repository: DictionaryRepository,
    private val userRepository: UserRepository,
    private val toastController: com.wadjet.core.common.ToastController,
) : ViewModel() {

    private val _state = MutableStateFlow(BrowseUiState())
    val state: StateFlow<BrowseUiState> = _state.asStateFlow()

    private val _alphabetState = MutableStateFlow(AlphabetUiState())
    val alphabetState: StateFlow<AlphabetUiState> = _alphabetState.asStateFlow()

    private var searchJob: Job? = null
    private val lang: String get() = if (java.util.Locale.getDefault().language == "ar") "ar" else "en"

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
        viewModelScope.launch {
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
                        isLoadingMore = false,
                    )
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
            loadSigns()
        }
    }

    fun selectSign(sign: Sign?) {
        _state.update { it.copy(selectedSign = sign) }
    }

    fun dismissError() {
        _state.update { it.copy(error = null) }
    }

    fun dismissLocalTts() {
        _state.update { it.copy(localTtsText = null) }
    }

    fun showToast(message: String) {
        toastController.success(message)
    }

    fun speakSign(text: String) {
        toastController.info("Generating pronunciation\u2026")
        viewModelScope.launch {
            repository.speakPhonetic(text).onSuccess { bytes ->
                if (bytes != null) {
                    var tmp: File? = null
                    try {
                        tmp = File.createTempFile("dict_tts_", ".wav")
                        tmp.writeBytes(bytes)
                        mediaPlayer?.apply { if (isPlaying) stop(); release() }
                        val player = MediaPlayer().apply {
                            setDataSource(tmp.absolutePath)
                            prepare()
                            setOnCompletionListener {
                                it.release()
                                if (mediaPlayer === it) mediaPlayer = null
                                tmp.delete()
                            }
                            start()
                        }
                        mediaPlayer = player
                    } catch (e: Exception) {
                        Timber.e(e, "Dictionary TTS playback failed")
                        tmp?.delete()
                        _state.update { it.copy(localTtsText = text) }
                    }
                } else {
                    _state.update { it.copy(localTtsText = text) }
                }
            }.onFailure {
                Timber.e(it, "Dictionary TTS failed")
                _state.update { it.copy(localTtsText = text) }
            }
        }
    }

    fun loadAlphabet() {
        viewModelScope.launch {
            _alphabetState.update { it.copy(isLoading = true) }
            repository.getAlphabet(lang = lang)
                .onSuccess { signs -> _alphabetState.update { AlphabetUiState(signs = signs) } }
                .onFailure { e ->
                    Timber.e(e, "Failed to load alphabet")
                    _alphabetState.update { it.copy(isLoading = false) }
                }
        }
    }

    private var mediaPlayer: MediaPlayer? = null

    override fun onCleared() {
        super.onCleared()
        mediaPlayer?.apply { if (isPlaying) stop(); release() }
        mediaPlayer = null
    }
}
