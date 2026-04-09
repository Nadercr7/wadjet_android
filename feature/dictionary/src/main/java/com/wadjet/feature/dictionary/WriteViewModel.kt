package com.wadjet.feature.dictionary

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.wadjet.core.domain.model.PaletteSign
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
    val selectedMode: String = "alpha",
    val result: WriteResult? = null,
    val palette: List<PaletteSign> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null,
)

val WRITE_MODES = listOf("alpha" to "Alphabetic", "smart" to "Smart", "mdc" to "MdC")

@HiltViewModel
class WriteViewModel @Inject constructor(
    private val repository: DictionaryRepository,
) : ViewModel() {

    private val _state = MutableStateFlow(WriteUiState())
    val state: StateFlow<WriteUiState> = _state.asStateFlow()

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
    }

    fun selectMode(mode: String) {
        _state.update { it.copy(selectedMode = mode) }
    }

    fun appendGlyph(glyph: String) {
        _state.update { it.copy(inputText = it.inputText + glyph) }
    }

    fun convert() {
        val s = _state.value
        if (s.inputText.isBlank()) return
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, error = null) }
            repository.write(s.inputText, s.selectedMode)
                .onSuccess { result -> _state.update { it.copy(result = result, isLoading = false) } }
                .onFailure { e -> _state.update { it.copy(isLoading = false, error = e.message) } }
        }
    }

    fun clear() {
        _state.update { it.copy(inputText = "", result = null, error = null) }
    }
}
