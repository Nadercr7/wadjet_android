package com.wadjet.feature.dictionary

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.wadjet.core.domain.model.Exercise
import com.wadjet.core.domain.model.Lesson
import com.wadjet.core.domain.model.Sign
import com.wadjet.core.domain.repository.DictionaryRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class LessonUiState(
    val lesson: Lesson? = null,
    val isLoading: Boolean = true,
    val error: String? = null,
    val currentExerciseIndex: Int = 0,
    val selectedAnswer: String? = null,
    val isAnswerRevealed: Boolean = false,
    val correctCount: Int = 0,
    val answeredCount: Int = 0,
    val isCompleted: Boolean = false,
)

@HiltViewModel
class LessonViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val repository: DictionaryRepository,
) : ViewModel() {

    private val level: Int = savedStateHandle["level"] ?: 1

    private val _state = MutableStateFlow(LessonUiState())
    val state: StateFlow<LessonUiState> = _state.asStateFlow()

    init {
        loadLesson()
    }

    private fun loadLesson() {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, error = null) }
            repository.getLesson(level)
                .onSuccess { lesson ->
                    _state.update { it.copy(lesson = lesson, isLoading = false) }
                }
                .onFailure { e ->
                    _state.update { it.copy(isLoading = false, error = e.message) }
                }
        }
    }

    val currentExercise: Exercise?
        get() {
            val s = _state.value
            val exercises = s.lesson?.exercises ?: return null
            return exercises.getOrNull(s.currentExerciseIndex)
        }

    fun selectAnswer(answer: String) {
        if (_state.value.isAnswerRevealed) return
        _state.update { it.copy(selectedAnswer = answer) }
    }

    fun revealAnswer() {
        val s = _state.value
        val exercise = currentExercise ?: return
        val isCorrect = s.selectedAnswer == exercise.correctAnswer
        _state.update {
            it.copy(
                isAnswerRevealed = true,
                correctCount = if (isCorrect) it.correctCount + 1 else it.correctCount,
                answeredCount = it.answeredCount + 1,
            )
        }
    }

    fun nextExercise() {
        val s = _state.value
        val total = s.lesson?.exercises?.size ?: return
        if (s.currentExerciseIndex + 1 >= total) {
            _state.update { it.copy(isCompleted = true) }
        } else {
            _state.update {
                it.copy(
                    currentExerciseIndex = it.currentExerciseIndex + 1,
                    selectedAnswer = null,
                    isAnswerRevealed = false,
                )
            }
        }
    }

    fun retry() {
        _state.update {
            it.copy(
                currentExerciseIndex = 0,
                selectedAnswer = null,
                isAnswerRevealed = false,
                correctCount = 0,
                answeredCount = 0,
                isCompleted = false,
            )
        }
    }
}
