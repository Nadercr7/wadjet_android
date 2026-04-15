package com.wadjet.feature.dictionary

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.wadjet.core.domain.model.TranslationResult
import com.wadjet.core.domain.repository.TranslateRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

data class TranslateUiState(
    val input: String = "",
    val gardinerInput: String = "",
    val isLoading: Boolean = false,
    val result: TranslationResult? = null,
    val error: String? = null,
)

@HiltViewModel
class TranslateViewModel @Inject constructor(
    private val translateRepository: TranslateRepository,
) : ViewModel() {

    private val _state = MutableStateFlow(TranslateUiState())
    val state: StateFlow<TranslateUiState> = _state.asStateFlow()

    fun onInputChange(text: String) {
        _state.update { it.copy(input = text) }
    }

    fun onGardinerChange(text: String) {
        _state.update { it.copy(gardinerInput = text) }
    }

    fun translate() {
        val input = _state.value.input.trim()
        if (input.isBlank()) return
        if (_state.value.isLoading) return
        _state.update { it.copy(isLoading = true, error = null) }
        viewModelScope.launch {
            translateRepository.translate(
                transliteration = input,
                gardinerSequence = _state.value.gardinerInput.trim().takeIf { it.isNotBlank() },
            ).onSuccess { result ->
                _state.update { it.copy(result = result, isLoading = false) }
            }.onFailure { error ->
                Timber.e(error, "Translation failed")
                _state.update { it.copy(error = error.message, isLoading = false) }
            }
        }
    }

    fun clear() {
        _state.update { TranslateUiState() }
    }

    fun dismissError() {
        _state.update { it.copy(error = null) }
    }
}
