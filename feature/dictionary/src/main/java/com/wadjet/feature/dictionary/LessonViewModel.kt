package com.wadjet.feature.dictionary

import com.wadjet.core.common.audio.AudioPlaybackManager
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import com.wadjet.core.common.StringResolver
import androidx.lifecycle.viewModelScope
import com.wadjet.core.common.ToastController
import com.wadjet.core.domain.model.Lesson
import com.wadjet.core.domain.repository.DictionaryRepository
import com.wadjet.core.domain.repository.TtsPreferencesRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

data class LessonUiState(
    val lesson: Lesson? = null,
    val isLoading: Boolean = true,
    val error: String? = null,
)

@HiltViewModel
class LessonViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val repository: DictionaryRepository,
    private val toastController: ToastController,
    private val audioPlayer: AudioPlaybackManager,
    private val ttsPreferences: TtsPreferencesRepository,
    private val strings: StringResolver,
) : ViewModel() {

    private val level: Int = savedStateHandle["level"] ?: 1

    private val _state = MutableStateFlow(LessonUiState())
    val state: StateFlow<LessonUiState> = _state.asStateFlow()

    init {
        loadLesson()
    }

    private fun loadLesson() {
        val lang = com.wadjet.core.common.AppLanguage.current()
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, error = null) }
            repository.getLesson(level, lang = lang)
                .onSuccess { lesson ->
                    _state.update { it.copy(lesson = lesson, isLoading = false) }
                }
                .onFailure { e ->
                    _state.update { it.copy(isLoading = false, error = e.message) }
                }
        }
    }

    fun retry() {
        loadLesson()
    }

    fun speakSign(text: String) {
        viewModelScope.launch {
            val speed = ttsPreferences.ttsSpeed.first()
            // H-05: respect the TTS toggle and always degrade to the on-device voice
            if (!ttsPreferences.ttsEnabled.first()) {
                audioPlayer.speakLocal(text, speed)
                return@launch
            }
            toastController.info(strings.get(R.string.dict_generating_pronunciation))
            repository.speakPhonetic(text).onSuccess { bytes ->
                if (bytes != null) {
                    audioPlayer.playWavBytes(
                        bytes = bytes,
                        prefix = "lesson_tts_",
                        speed = speed,
                        onError = { audioPlayer.speakLocal(text, speed) },
                    )
                } else {
                    audioPlayer.speakLocal(text, speed)
                }
            }.onFailure {
                Timber.e(it, "Lesson TTS failed; falling back to local voice")
                audioPlayer.speakLocal(text, speed)
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        audioPlayer.stop()
    }
}
