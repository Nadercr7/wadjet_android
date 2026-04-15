package com.wadjet.feature.dictionary

import android.media.MediaPlayer
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.wadjet.core.common.ToastController
import com.wadjet.core.domain.model.Lesson
import com.wadjet.core.domain.repository.DictionaryRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import timber.log.Timber
import java.io.File
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
) : ViewModel() {

    private val level: Int = savedStateHandle["level"] ?: 1
    private var mediaPlayer: MediaPlayer? = null

    private val _state = MutableStateFlow(LessonUiState())
    val state: StateFlow<LessonUiState> = _state.asStateFlow()

    init {
        loadLesson()
    }

    private fun loadLesson() {
        val lang = if (java.util.Locale.getDefault().language == "ar") "ar" else "en"
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
        toastController.info("Generating pronunciation…")
        viewModelScope.launch {
            repository.speakPhonetic(text).onSuccess { bytes ->
                if (bytes != null) {
                    try {
                        val tmp = File.createTempFile("lesson_tts_", ".wav")
                        tmp.writeBytes(bytes)
                        mediaPlayer?.apply { if (isPlaying) stop(); release() }
                        mediaPlayer = MediaPlayer().apply {
                            setDataSource(tmp.absolutePath)
                            prepare()
                            setOnCompletionListener { release(); mediaPlayer = null; tmp.delete() }
                            start()
                        }
                    } catch (e: Exception) {
                        Timber.e(e, "Lesson TTS playback failed")
                    }
                }
            }.onFailure {
                Timber.e(it, "Lesson TTS failed")
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        mediaPlayer?.apply { if (isPlaying) stop(); release() }
        mediaPlayer = null
    }
}
