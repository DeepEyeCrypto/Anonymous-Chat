package com.phantomnet.feature.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.phantomnet.core.identity.IdentityManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class SettingsUiState(
    val fingerprint: String = "",
    val coreAvailable: Boolean = false,
    val appVersion: String = "",
    val isWiping: Boolean = false,
    val stealthMode: Int = 0,
    val mixnetEnabled: Boolean = false,
    val paranoiaMode: Boolean = false
)

class SettingsViewModel(
    private val identityManager: IdentityManager,
    coreAvailable: Boolean,
    appVersion: String
) : ViewModel() {

    private val _state = MutableStateFlow(
        SettingsUiState(
            fingerprint = identityManager.fingerprint?.let { fp ->
                fp.chunked(2).joinToString(" ").uppercase()
            } ?: "Unknown",
            coreAvailable = coreAvailable,
            appVersion = appVersion,
            stealthMode = identityManager.stealthMode,
            mixnetEnabled = identityManager.mixnetEnabled,
            paranoiaMode = identityManager.paranoiaMode
        )
    )
    val state: StateFlow<SettingsUiState> = _state.asStateFlow()

    fun setStealthMode(mode: Int) {
        identityManager.stealthMode = mode
        _state.value = _state.value.copy(stealthMode = mode)
    }

    fun setMixnetEnabled(enabled: Boolean) {
        identityManager.mixnetEnabled = enabled
        _state.value = _state.value.copy(mixnetEnabled = enabled)
    }

    fun setParanoiaMode(enabled: Boolean) {
        identityManager.paranoiaMode = enabled
        _state.value = _state.value.copy(paranoiaMode = enabled)
    }

    fun setRealPin(pin: String) {
        identityManager.setRealPin(pin)
    }

    fun setDecoyPin(pin: String) {
        identityManager.setDecoyPin(pin)
    }

    fun initializeDecoyPersona(onComplete: () -> Unit) {
        viewModelScope.launch {
            identityManager.createDecoyPersona()
            onComplete()
        }
    }

    fun wipeIdentity(onComplete: () -> Unit) {
        if (_state.value.isWiping) return
        _state.value = _state.value.copy(isWiping = true)

        viewModelScope.launch {
            try {
                identityManager.wipeAll()
                onComplete()
            } catch (t: Throwable) {
                _state.value = _state.value.copy(isWiping = false)
            }
        }
    }
}
