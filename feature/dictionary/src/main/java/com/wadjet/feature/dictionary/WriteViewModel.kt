package com.wadjet.feature.dictionary

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.wadjet.core.domain.model.WriteGlyph
import com.wadjet.core.domain.model.WriteResult
import com.wadjet.core.domain.repository.DictionaryRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class WriteUiState(
    val inputText: String = "",
    val result: WriteResult? = null,
    val isLoading: Boolean = false,
    val error: String? = null,
)

@HiltViewModel
class WriteViewModel @Inject constructor(
    private val repository: DictionaryRepository,
) : ViewModel() {

    private val _state = MutableStateFlow(WriteUiState())
    val state: StateFlow<WriteUiState> = _state.asStateFlow()

    fun onInputChange(text: String) {
        _state.update { it.copy(inputText = text, result = null, error = null) }
    }

    fun convert() {
        val s = _state.value
        if (s.inputText.isBlank()) return
        if (s.isLoading) return
        viewModelScope.launch {
            // Clear any previous result before issuing the new request so the UI
            // never shows stale output alongside the loading state.
            _state.update { it.copy(isLoading = true, error = null, result = null) }
            repository.write(s.inputText, "smart")
                .onSuccess { result -> _state.update { it.copy(result = result, isLoading = false) } }
                .onFailure { e -> _state.update { it.copy(isLoading = false, error = e.message) } }
        }
    }

    fun clear() {
        _state.update { it.copy(inputText = "", result = null, error = null) }
    }
}
