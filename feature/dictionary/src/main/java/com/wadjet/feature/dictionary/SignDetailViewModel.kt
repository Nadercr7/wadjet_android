package com.wadjet.feature.dictionary

import com.wadjet.core.common.audio.AudioPlaybackManager
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.wadjet.core.domain.model.Sign
import com.wadjet.core.domain.repository.DictionaryRepository
import com.wadjet.core.domain.repository.TtsPreferencesRepository
import com.wadjet.core.domain.repository.UserRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import timber.log.Timber
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
    private val ttsPreferences: TtsPreferencesRepository,
    private val toastController: com.wadjet.core.common.ToastController,
    private val audioPlayer: AudioPlaybackManager,
) : ViewModel() {

    private val code: String = savedStateHandle.get<String>("code") ?: ""

    private val _state = MutableStateFlow(SignDetailUiState())
    val state: StateFlow<SignDetailUiState> = _state.asStateFlow()

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

    private var isTogglingFavorite = false

    fun toggleFavorite() {
        if (isTogglingFavorite) return
        val sign = _state.value.sign ?: return
        val isFav = _state.value.isFavorite
        isTogglingFavorite = true
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
            isTogglingFavorite = false
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

    fun showToast(message: String) {
        toastController.success(message)
    }

    fun speakSign(text: String) {
        viewModelScope.launch {
            val speed = ttsPreferences.ttsSpeed.first()
            // H-05: server TTS disabled or unavailable -> on-device voice (web parity)
            if (!ttsPreferences.ttsEnabled.first()) {
                audioPlayer.speakLocal(text, speed)
                return@launch
            }
            toastController.info("Generating pronunciation…")
            repository.speakPhonetic(text).onSuccess { bytes ->
                if (bytes != null) {
                    audioPlayer.playWavBytes(
                        bytes = bytes,
                        prefix = "sign_tts_",
                        speed = speed,
                        onError = { audioPlayer.speakLocal(text, speed) },
                    )
                } else {
                    audioPlayer.speakLocal(text, speed)
                }
            }.onFailure { e ->
                Timber.e(e, "Sign TTS failed; falling back to local voice")
                audioPlayer.speakLocal(text, speed)
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        audioPlayer.stop()
    }
}
