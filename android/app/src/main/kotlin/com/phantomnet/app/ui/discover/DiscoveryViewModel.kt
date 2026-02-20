package com.phantomnet.app.ui.discover

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.phantomnet.core.PhantomCore
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed class DiscoveryState {
    object Idle : DiscoveryState()
    object Scanning : DiscoveryState()
    data class Success(val message: String, val matches: Int) : DiscoveryState()
    data class Error(val message: String) : DiscoveryState()
}

class DiscoveryViewModel : ViewModel() {
    private val _state = MutableStateFlow<DiscoveryState>(DiscoveryState.Idle)
    val state = _state.asStateFlow()

    fun runPsiScan() {
        if (_state.value is DiscoveryState.Scanning) return
        _state.value = DiscoveryState.Scanning

        viewModelScope.launch {
            try {
                // Simulate real I/O and crypto overhead
                delay(2500)
                
                // In a real implementation, we'd fetch the blinded contact set from the P2P network
                // For Alpha 2, we simulate a scan against a dummy relay set
                val mockContacts = arrayOf("+1234567890", "+0987654321", "activist@phantom.net")
                val mockRemote = emptyArray<String>()
                
                val result = PhantomCore.runPsiDiscoverySafe(mockContacts, mockRemote)
                
                if (result.contains("error")) {
                    _state.value = DiscoveryState.Error(result)
                } else {
                    _state.value = DiscoveryState.Success(result, 2)
                }
            } catch (e: Exception) {
                _state.value = DiscoveryState.Error(e.message ?: "Unknown PSI discovery error")
            }
        }
    }

    fun reset() {
        _state.value = DiscoveryState.Idle
    }
}
