package com.wadjet.feature.dictionary

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.wadjet.core.domain.model.PaletteSign
import com.wadjet.core.domain.model.WriteGlyph
import com.wadjet.core.domain.model.WriteResult
import com.wadjet.core.domain.repository.DictionaryRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class WriteUiState(
    val inputText: String = "",
    val result: WriteResult? = null,
    val preview: WriteResult? = null,
    val isPreviewLoading: Boolean = false,
    val palette: List<PaletteSign> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null,
)

@HiltViewModel
class WriteViewModel @Inject constructor(
    private val repository: DictionaryRepository,
) : ViewModel() {

    private val _state = MutableStateFlow(WriteUiState())
    val state: StateFlow<WriteUiState> = _state.asStateFlow()
    private var previewJob: Job? = null

    init {
        loadPalette()
    }

    private fun loadPalette() {
        viewModelScope.launch {
            repository.getPalette()
                .onSuccess { palette -> _state.update { it.copy(palette = palette) } }
        }
    }

    fun onInputChange(text: String) {
        _state.update { it.copy(inputText = text) }
        previewJob?.cancel()
        if (text.isBlank()) {
            _state.update { it.copy(preview = null, isPreviewLoading = false) }
            return
        }
        previewJob = viewModelScope.launch {
            delay(500)
            _state.update { it.copy(isPreviewLoading = true) }
            repository.write(text, "smart")
                .onSuccess { r -> _state.update { it.copy(preview = r, isPreviewLoading = false) } }
                .onFailure { _state.update { it.copy(isPreviewLoading = false) } }
        }
    }

    fun appendGlyph(sign: PaletteSign) {
        val text = sign.glyph
        _state.update { it.copy(inputText = it.inputText + text) }
    }

    fun convert() {
        val s = _state.value
        if (s.inputText.isBlank()) return
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, error = null) }
            repository.write(s.inputText, "smart")
                .onSuccess { result -> _state.update { it.copy(result = result, isLoading = false) } }
                .onFailure { e -> _state.update { it.copy(isLoading = false, error = e.message) } }
        }
    }

    fun clear() {
        _state.update { it.copy(inputText = "", result = null, error = null) }
    }
}
