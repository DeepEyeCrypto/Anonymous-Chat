package com.phantomnet.app.ui.call

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

enum class CallState {
    IDLE,
    OUTGOING_INIT,
    RINGING_REMOTE,
    INCOMING_RINGING,
    ESTABLISHING_MEDIA,
    ACTIVE_AUDIO,
    ACTIVE_VIDEO,
    RECONNECTING,
    TERMINATING,
    ENDED,
    FAILED
}

enum class CallQuality { GOOD, DEGRADED, POOR }

data class CallUiState(
    val callSessionId: String? = null,
    val contactName: String = "",
    val state: CallState = CallState.IDLE,
    val privacyMode: PrivacyMode = PrivacyMode.PRIVATE,
    val isVideoCall: Boolean = false,
    val isCallerVerified: Boolean = false,
    
    // In-Call specifics
    val callDurationSec: Int = 0,
    val qualityState: CallQuality = CallQuality.GOOD,
    val isMuted: Boolean = false,
    val isCameraOff: Boolean = true,
    val isSpeakerOn: Boolean = false,
    
    // Network Details
    val latencyMs: Int = 0,
    val packetLossPct: Float = 0f
)

class CallViewModel : ViewModel() {
    private val _uiState = MutableStateFlow(CallUiState())
    val uiState: StateFlow<CallUiState> = _uiState.asStateFlow()

    fun placeCall(contactName: String, mode: PrivacyMode, isVideo: Boolean) {
        _uiState.update {
            it.copy(
                callSessionId = "mock-uuid-1234",
                contactName = contactName,
                state = CallState.OUTGOING_INIT,
                privacyMode = mode,
                isVideoCall = isVideo,
                isCameraOff = !isVideo
            )
        }
        
        // Mock progression for layout demo
        viewModelScope.launch {
            kotlinx.coroutines.delay(1000)
            _uiState.update { it.copy(state = CallState.RINGING_REMOTE) }
            kotlinx.coroutines.delay(2000)
            
            val activeState = if (isVideo) CallState.ACTIVE_VIDEO else CallState.ACTIVE_AUDIO
            _uiState.update { it.copy(state = activeState) }
        }
    }

    fun handleIncomingCall(contactName: String, mode: PrivacyMode, isVideo: Boolean, isVerified: Boolean) {
        _uiState.update {
            it.copy(
                callSessionId = "mock-uuid-5678",
                contactName = contactName,
                state = CallState.INCOMING_RINGING,
                privacyMode = mode,
                isVideoCall = isVideo,
                isCallerVerified = isVerified,
                isCameraOff = !isVideo
            )
        }
    }

    fun acceptCall(enableVideo: Boolean) {
        val nextState = if (enableVideo) CallState.ACTIVE_VIDEO else CallState.ACTIVE_AUDIO
        _uiState.update {
            it.copy(
                state = nextState,
                isVideoCall = enableVideo,
                isCameraOff = !enableVideo
            )
        }
    }

    fun declineCall() {
        endCallAndReset()
    }

    fun toggleMute() {
        _uiState.update { it.copy(isMuted = !it.isMuted) }
    }

    fun toggleCamera() {
        _uiState.update { state -> 
            val newCameraOff = !state.isCameraOff
            val newState = if (newCameraOff) CallState.ACTIVE_AUDIO else CallState.ACTIVE_VIDEO
            state.copy(isCameraOff = newCameraOff, state = newState, isVideoCall = !newCameraOff)
        }
    }

    fun toggleSpeaker() {
        _uiState.update { it.copy(isSpeakerOn = !it.isSpeakerOn) }
    }

    fun endCall() {
        _uiState.update { it.copy(state = CallState.TERMINATING) }
        // Mock Teardown
        viewModelScope.launch {
            kotlinx.coroutines.delay(500)
            endCallAndReset()
        }
    }

    private fun endCallAndReset() {
        _uiState.update { it.copy(state = CallState.ENDED) }
        // Typically, you would signal UI to popBackStack here via an event channel,
        // and then reset to IDLE.
    }
}
