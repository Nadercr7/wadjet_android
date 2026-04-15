package com.wadjet.app.screen

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.wadjet.core.domain.model.ScanHistorySummary
import com.wadjet.core.domain.model.Sign
import com.wadjet.core.domain.repository.DictionaryRepository
import com.wadjet.core.domain.repository.ScanRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class HieroglyphsHubUiState(
    val recentScans: List<ScanHistorySummary> = emptyList(),
    val suggestedSigns: List<Sign> = emptyList(),
    val signsLearned: Int = 0,
    val totalSigns: Int = 1071,
    val isLoading: Boolean = true,
)

@HiltViewModel
class HieroglyphsHubViewModel @Inject constructor(
    private val scanRepository: ScanRepository,
    private val dictionaryRepository: DictionaryRepository,
) : ViewModel() {

    private val _state = MutableStateFlow(HieroglyphsHubUiState())
    val state: StateFlow<HieroglyphsHubUiState> = _state.asStateFlow()

    init {
        load()
    }

    private fun load() {
        viewModelScope.launch {
            val scans = scanRepository.getScanHistory().firstOrNull()?.take(3) ?: emptyList()
            _state.update { it.copy(recentScans = scans) }
        }
        viewModelScope.launch {
            val randomPage = (1..30).random()
            dictionaryRepository.getSigns(page = randomPage, perPage = 3)
                .onSuccess { signPage ->
                    _state.update { it.copy(suggestedSigns = signPage.signs) }
                }
        }
        viewModelScope.launch {
            dictionaryRepository.getSigns(page = 1, perPage = 1)
                .onSuccess { signPage ->
                    _state.update { it.copy(totalSigns = signPage.total, isLoading = false) }
                }
                .onFailure {
                    _state.update { s -> s.copy(isLoading = false) }
                }
        }
    }
}
