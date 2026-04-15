package com.wadjet.feature.scan

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.wadjet.core.domain.model.ScanHistorySummary
import com.wadjet.core.domain.repository.ScanRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
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
    val error: String? = null,
)

@HiltViewModel
class HistoryViewModel @Inject constructor(
    private val scanRepository: ScanRepository,
) : ViewModel() {

    private val _state = MutableStateFlow(HistoryUiState())
    val state: StateFlow<HistoryUiState> = _state.asStateFlow()

    private var collectorJob: Job? = null

    init {
        loadHistory()
    }

    private fun loadHistory() {
        collectorJob?.cancel()
        collectorJob = viewModelScope.launch {
            try {
                scanRepository.getScanHistory().collect { items ->
                    _state.update { it.copy(items = items, isLoading = false, error = null) }
                }
            } catch (e: Exception) {
                Timber.e(e, "Failed to load scan history")
                _state.update { it.copy(isLoading = false, error = e.message) }
            }
        }
    }

    fun refresh() {
        _state.update { it.copy(isLoading = true, error = null) }
        loadHistory()
    }

    fun deleteScan(scanId: Int) {
        viewModelScope.launch {
            scanRepository.deleteScan(scanId)
                .onFailure { Timber.e(it, "Failed to delete scan $scanId") }
        }
    }
}
