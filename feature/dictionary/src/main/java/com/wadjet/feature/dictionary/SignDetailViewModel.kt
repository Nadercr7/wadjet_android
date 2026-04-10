package com.wadjet.feature.dictionary

import android.media.MediaPlayer
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.wadjet.core.domain.model.Sign
import com.wadjet.core.domain.repository.DictionaryRepository
import com.wadjet.core.domain.repository.UserRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.io.File
import javax.inject.Inject

data class SignDetailUiState(
    val sign: Sign? = null,
    val isLoading: Boolean = true,
    val error: String? = null,
    val isFavorite: Boolean = false,
)

@HiltViewModel
class SignDetailViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val repository: DictionaryRepository,
    private val userRepository: UserRepository,
) : ViewModel() {

    private val code: String = savedStateHandle.get<String>("code") ?: ""

    private val _state = MutableStateFlow(SignDetailUiState())
    val state: StateFlow<SignDetailUiState> = _state.asStateFlow()

    private var mediaPlayer: MediaPlayer? = null

    init {
        loadSign()
        loadFavoriteState()
    }

    private fun loadFavoriteState() {
        viewModelScope.launch {
            userRepository.getFavorites().onSuccess { items ->
                val isFav = items.any { it.itemType == "glyph" && it.itemId == code }
                _state.update { it.copy(isFavorite = isFav) }
            }
        }
    }

    fun toggleFavorite() {
        val sign = _state.value.sign ?: return
        val isFav = _state.value.isFavorite
        viewModelScope.launch {
            _state.update { it.copy(isFavorite = !isFav) }
            val result = if (isFav) {
                userRepository.removeFavorite("glyph", sign.code)
            } else {
                userRepository.addFavorite("glyph", sign.code)
            }
            result.onFailure {
                _state.update { it.copy(isFavorite = isFav) }
            }
        }
    }

    private fun loadSign() {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, error = null) }
            repository.getSign(code)
                .onSuccess { sign -> _state.update { it.copy(sign = sign, isLoading = false) } }
                .onFailure { e -> _state.update { it.copy(isLoading = false, error = e.message) } }
        }
    }

    fun speakSign(text: String) {
        viewModelScope.launch {
            repository.speakPhonetic(text).onSuccess { bytes ->
                if (bytes != null) {
                    try {
                        val tmp = File.createTempFile("sign_tts_", ".wav")
                        tmp.writeBytes(bytes)
                        mediaPlayer?.apply { if (isPlaying) stop(); release() }
                        mediaPlayer = MediaPlayer().apply {
                            setDataSource(tmp.absolutePath)
                            prepare()
                            start()
                            setOnCompletionListener { tmp.delete() }
                        }
                    } catch (_: Exception) { }
                }
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        mediaPlayer?.release()
    }
}
