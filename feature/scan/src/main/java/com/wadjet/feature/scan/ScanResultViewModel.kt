package com.wadjet.feature.scan

import com.wadjet.core.common.audio.AudioPlaybackManager
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.wadjet.core.common.EgyptianPronunciation
import com.wadjet.core.designsystem.component.TtsState
import com.wadjet.core.domain.model.ScanResult
import com.wadjet.core.domain.repository.ScanRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

data class ScanResultUiState(
    val result: ScanResult? = null,
    val isLoading: Boolean = true,
    val error: String? = null,
    val ttsStates: Map<String, TtsState> = emptyMap(),
    val localTtsText: String? = null,
    val localTtsLang: String? = null,
)

@HiltViewModel
class ScanResultViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val scanRepository: ScanRepository,
    private val audioPlayer: AudioPlaybackManager,
) : ViewModel() {

    private val scanId: String = savedStateHandle.get<String>("scanId") ?: ""

    private val _state = MutableStateFlow(ScanResultUiState())
    val state: StateFlow<ScanResultUiState> = _state.asStateFlow()

    init {
        loadResult()
    }

    private fun loadResult() {
        val id = scanId.toIntOrNull()
        if (id == null) {
            _state.update { it.copy(isLoading = false, error = "Invalid scan ID") }
            return
        }
        viewModelScope.launch {
            scanRepository.getScanResult(id)
                .onSuccess { result ->
                    _state.update { it.copy(result = result, isLoading = false) }
                }
                .onFailure { error ->
                    Timber.e(error, "Failed to load scan result $scanId")
                    _state.update { it.copy(isLoading = false, error = error.message) }
                }
        }
    }

    fun speak(key: String, text: String, lang: String = "en") {
        val current = _state.value.ttsStates[key]
        if (current == TtsState.PLAYING || current == TtsState.LOADING) {
            stopTts(key)
            return
        }
        _state.update { it.copy(ttsStates = it.ttsStates + (key to TtsState.LOADING)) }
        viewModelScope.launch {
            val isHieroglyphic = key == "translit"
            val ttsText = if (isHieroglyphic) EgyptianPronunciation.toSpeech(text) else text
            val ctx = if (isHieroglyphic) EgyptianPronunciation.CONTEXT else "scan_pronunciation"
            scanRepository.speak(ttsText, lang, ctx).onSuccess { bytes ->
                if (bytes != null) {
                    playWavBytes(key, bytes)
                } else {
                    _state.update {
                        it.copy(
                            ttsStates = it.ttsStates + (key to TtsState.IDLE),
                            localTtsText = text,
                            localTtsLang = lang,
                        )
                    }
                }
            }.onFailure {
                Timber.e(it, "TTS failed for key=$key")
                _state.update { s -> s.copy(ttsStates = s.ttsStates + (key to TtsState.IDLE)) }
            }
        }
    }

    private fun playWavBytes(key: String, bytes: ByteArray) {
        _state.update { it.copy(ttsStates = it.ttsStates + (key to TtsState.PLAYING)) }
        audioPlayer.playWavBytes(
            bytes = bytes,
            onCompletion = { _state.update { s -> s.copy(ttsStates = s.ttsStates + (key to TtsState.IDLE)) } },
            onError = { _state.update { s -> s.copy(ttsStates = s.ttsStates + (key to TtsState.IDLE)) } },
        )
    }

    private fun stopTts(key: String) {
        audioPlayer.stop()
        _state.update { it.copy(ttsStates = it.ttsStates + (key to TtsState.IDLE)) }
    }

    fun dismissLocalTts() {
        _state.update { it.copy(localTtsText = null, localTtsLang = null) }
    }

    override fun onCleared() {
        super.onCleared()
        audioPlayer.stop()
    }
}
