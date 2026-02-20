package com.phantomnet.feature.sync

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.phantomnet.core.identity.IdentityManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class SyncViewModel(
    private val identityManager: IdentityManager
) : ViewModel() {

    private val _state = MutableStateFlow(SyncUiState())
    val state: StateFlow<SyncUiState> = _state.asStateFlow()

    fun startExport() {
        _state.value = _state.value.copy(isProcessing = true, mode = SyncMode.Exporting)
        viewModelScope.launch {
            try {
                val bundle = identityManager.exportPersonaBundle()
                _state.value = _state.value.copy(
                    exportBundle = bundle,
                    isProcessing = false
                )
            } catch (e: Exception) {
                _state.value = _state.value.copy(
                    error = "Failed to export identity: ${e.message}",
                    isProcessing = false
                )
            }
        }
    }

    fun startImport() {
        _state.value = _state.value.copy(mode = SyncMode.Importing)
    }

    fun processImport(bundleJson: String, onComplete: () -> Unit) {
        if (_state.value.isProcessing) return
        
        _state.value = _state.value.copy(isProcessing = true, error = null)
        viewModelScope.launch {
            val success = identityManager.importPersonaBundle(bundleJson)
            if (success) {
                // DON'T call onComplete yet if we want to allow history sync
                _state.value = _state.value.copy(isProcessing = false, isComplete = true)
            } else {
                _state.value = _state.value.copy(
                    isProcessing = false,
                    error = "Invalid sync bundle or import failed."
                )
            }
        }
    }

    fun startHistoryExport() {
        viewModelScope.launch {
            val history = identityManager.exportHistoryBundle()
            _state.value = _state.value.copy(historyBundle = history)
        }
    }

    fun processHistoryImport(historyJson: String) {
        if (_state.value.isHistorySyncing) return
        
        _state.value = _state.value.copy(isHistorySyncing = true, error = null)
        viewModelScope.launch {
            val success = identityManager.importHistoryBundle(historyJson)
            _state.value = _state.value.copy(
                isHistorySyncing = false,
                isHistoryComplete = success
            )
        }
    }

    fun reset() {
        _state.value = SyncUiState()
    }
}
