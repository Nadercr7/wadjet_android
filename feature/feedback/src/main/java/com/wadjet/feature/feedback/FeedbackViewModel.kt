package com.wadjet.feature.feedback

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.wadjet.core.domain.model.FeedbackData
import com.wadjet.core.domain.repository.FeedbackRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

val FEEDBACK_CATEGORIES = listOf("Bug", "Suggestion", "Praise", "Other")

data class FeedbackUiState(
    val selectedCategory: String = "",
    val message: String = "",
    val name: String = "",
    val email: String = "",
    val isSubmitting: Boolean = false,
    val isSuccess: Boolean = false,
    val error: String? = null,
)

@HiltViewModel
class FeedbackViewModel @Inject constructor(
    private val feedbackRepository: FeedbackRepository,
) : ViewModel() {

    private val _state = MutableStateFlow(FeedbackUiState())
    val state: StateFlow<FeedbackUiState> = _state.asStateFlow()

    fun selectCategory(category: String) {
        _state.update { it.copy(selectedCategory = category) }
    }

    fun updateMessage(message: String) {
        if (message.length <= 1000) {
            _state.update { it.copy(message = message) }
        }
    }

    fun updateName(name: String) {
        _state.update { it.copy(name = name) }
    }

    fun updateEmail(email: String) {
        _state.update { it.copy(email = email) }
    }

    fun submit() {
        val s = _state.value
        if (s.isSubmitting) return
        if (s.selectedCategory.isBlank()) {
            _state.update { it.copy(error = "Please select a category") }
            return
        }
        if (s.message.length < 10) {
            _state.update { it.copy(error = "Message must be at least 10 characters") }
            return
        }
        _state.update { it.copy(isSubmitting = true, error = null) }
        viewModelScope.launch {
            feedbackRepository.submitFeedback(
                FeedbackData(
                    category = s.selectedCategory.lowercase(),
                    message = s.message,
                    name = s.name,
                    email = s.email,
                ),
            ).onSuccess {
                _state.update { it.copy(isSubmitting = false, isSuccess = true) }
            }.onFailure { error ->
                _state.update { it.copy(isSubmitting = false, error = error.message) }
            }
        }
    }

    fun dismissError() {
        _state.update { it.copy(error = null) }
    }
}
