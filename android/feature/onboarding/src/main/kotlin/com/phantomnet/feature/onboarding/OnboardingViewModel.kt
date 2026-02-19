package com.phantomnet.feature.onboarding

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.phantomnet.core.identity.IdentityManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed interface OnboardingState {
    data object Loading : OnboardingState
    data class Ready(val hasPersona: Boolean) : OnboardingState
}

sealed interface IdentityCreationState {
    data object Idle : IdentityCreationState
    data object Generating : IdentityCreationState
    data class Created(val fingerprint: String) : IdentityCreationState
    data class Error(val message: String) : IdentityCreationState
}

class OnboardingViewModel(
    private val identityManager: IdentityManager
) : ViewModel() {

    private val _splashState = MutableStateFlow<OnboardingState>(OnboardingState.Loading)
    val splashState: StateFlow<OnboardingState> = _splashState.asStateFlow()

    private val _creationState = MutableStateFlow<IdentityCreationState>(IdentityCreationState.Idle)
    val creationState: StateFlow<IdentityCreationState> = _creationState.asStateFlow()

    init {
        checkPersona()
    }

    private fun checkPersona() {
        viewModelScope.launch {
            val hasPersona = identityManager.isOnboardingCompleted
            _splashState.value = OnboardingState.Ready(hasPersona)
        }
    }

    fun generateIdentity() {
        if (_creationState.value is IdentityCreationState.Generating) return

        _creationState.value = IdentityCreationState.Generating
        viewModelScope.launch {
            try {
                val persona = identityManager.createPersona()
                _creationState.value = IdentityCreationState.Created(persona.displayFingerprint)
            } catch (t: Throwable) {
                _creationState.value = IdentityCreationState.Error(
                    t.message ?: "Identity generation failed"
                )
            }
        }
    }
}
