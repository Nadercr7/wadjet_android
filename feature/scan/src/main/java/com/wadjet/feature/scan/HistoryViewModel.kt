package com.wadjet.feature.scan

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.wadjet.core.domain.model.ScanHistorySummary
import com.wadjet.core.domain.repository.ScanRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

data class HistoryUiState(
    val items: List<ScanHistorySummary> = emptyList(),
    val isLoading: Boolean = true,
)

@HiltViewModel
class HistoryViewModel @Inject constructor(
    private val scanRepository: ScanRepository,
) : ViewModel() {

    private val _state = MutableStateFlow(HistoryUiState())
    val state: StateFlow<HistoryUiState> = _state.asStateFlow()

    init {
        loadHistory()
    }

    private fun loadHistory() {
        viewModelScope.launch {
            scanRepository.getScanHistory().collect { items ->
                _state.update { it.copy(items = items, isLoading = false) }
            }
        }
    }

    fun deleteScan(scanId: Int) {
        viewModelScope.launch {
            scanRepository.deleteScan(scanId)
                .onFailure { Timber.e(it, "Failed to delete scan $scanId") }
        }
    }
}
