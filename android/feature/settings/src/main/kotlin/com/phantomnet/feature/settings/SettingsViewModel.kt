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
    val isWiping: Boolean = false
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
            appVersion = appVersion
        )
    )
    val state: StateFlow<SettingsUiState> = _state.asStateFlow()

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
