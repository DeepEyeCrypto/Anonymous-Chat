package com.phantomnet.app.ui.call

import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel

@Composable
fun CallOrchestratorScreen(
    contactName: String,
    initialMode: PrivacyMode,
    isVideo: Boolean,
    isIncoming: Boolean = false,
    viewModel: CallViewModel = viewModel(),
    onEndCall: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    var showRouteSheet by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        if (uiState.state == CallState.IDLE) {
            if (isIncoming) {
                viewModel.handleIncomingCall(contactName, initialMode, isVideo, true)
            } else {
                viewModel.placeCall(contactName, initialMode, isVideo)
            }
        }
    }

    LaunchedEffect(uiState.state) {
        if (uiState.state == CallState.ENDED) {
            onEndCall()
        }
    }

    when (uiState.state) {
        CallState.IDLE, CallState.ENDED, CallState.FAILED, CallState.TERMINATING -> {
            // Empty or tear down view, handled by the ending signal
        }
        CallState.OUTGOING_INIT, CallState.RINGING_REMOTE, CallState.ESTABLISHING_MEDIA -> {
            OutgoingCallScreen(
                contactName = uiState.contactName,
                privacyMode = uiState.privacyMode,
                isVideoCall = uiState.isVideoCall,
                onCancel = { viewModel.declineCall() }
            )
        }
        CallState.INCOMING_RINGING -> {
            IncomingCallScreen(
                callerName = uiState.contactName,
                privacyMode = uiState.privacyMode,
                isVideoCall = uiState.isVideoCall,
                isCallerVerified = uiState.isCallerVerified,
                onAcceptRequest = { wantsVideo -> viewModel.acceptCall(wantsVideo) },
                onDecline = { viewModel.declineCall() }
            )
        }
        CallState.ACTIVE_AUDIO -> {
            InCallAudioScreen(
                contactName = uiState.contactName,
                callDuration = "02:30", // Format duration from uiState.callDurationSec
                privacyMode = uiState.privacyMode,
                qualityState = uiState.qualityState.name,
                isMuted = uiState.isMuted,
                isSpeakerOn = uiState.isSpeakerOn,
                onMuteToggle = { viewModel.toggleMute() },
                onSpeakerToggle = { viewModel.toggleSpeaker() },
                onVideoToggleRequest = { viewModel.toggleCamera() },
                onEndCall = { viewModel.endCall() },
                onMoreActions = { showRouteSheet = true }
            )
        }
        CallState.ACTIVE_VIDEO -> {
            InCallVideoScreen(
                contactName = uiState.contactName,
                callDuration = "02:30",
                privacyMode = uiState.privacyMode,
                isMuted = uiState.isMuted,
                isCameraOff = uiState.isCameraOff,
                onMuteToggle = { viewModel.toggleMute() },
                onCameraToggle = { viewModel.toggleCamera() },
                onSwitchCamera = { /* Native Request Switch */ },
                onAudioOnlyFallback = { viewModel.toggleCamera() }, // Switch mapping
                onEndCall = { viewModel.endCall() }
            )
        }
        CallState.RECONNECTING -> {
            OutgoingCallScreen(
                contactName = uiState.contactName,
                privacyMode = uiState.privacyMode,
                isVideoCall = uiState.isVideoCall,
                onCancel = { viewModel.endCall() }
            )
        }
    }

    if (showRouteSheet) {
        RouteDetailsBottomSheet(
            privacyMode = uiState.privacyMode,
            latencyMs = uiState.latencyMs,
            packetLossPct = uiState.packetLossPct,
            onDismiss = { showRouteSheet = false }
        )
    }
}
