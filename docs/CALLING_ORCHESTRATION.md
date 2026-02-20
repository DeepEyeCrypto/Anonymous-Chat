# PHANTOM NET: AUDIO / VIDEO CALLING ORCHESTRATION

## Phase O-Call: ORCHESTRATION (Data Contracts, ViewModels, and State Transitions)

> **Status**: ACTIVE
> **Version**: 1.0.0
> **Depends On**: `docs/CALLING_LAYOUT.md`
> **Focus**: State management, domain models, and WebRTC/Native bridge contracts for Android.

---

## 1. ORCHESTRATION MISSION

Translate the Calling LAYOUT and FRAME definitions into technical contracts for the Android client. This document specifies:

1. **State Management**: The UI state shapes and ViewModel contracts driving the calling screens.
2. **Domain Models**: Precise persistence and runtime data structures used in calls.
3. **WebRTC/Native Bridge**: Interfaces separating the Android app from the native Rust/C++ media stack.
4. **Signaling Handlers**: Expected inputs and outputs for call control messages over the existing E2EE channels.

---

## 2. CALL STATE MACHINE (ViewModel State)

The core lifecycle of any call is managed centrally by a Foreground Service and observed by the UI via `StateFlow`s.

### 2.1 Complete Call State Enum

```kotlin
enum class CallState {
    IDLE,
    OUTGOING_INIT,       // Generating offer, sending message
    RINGING_REMOTE,      // Offer sent, waiting for answer
    INCOMING_RINGING,    // Offer received, waiting for user action
    ESTABLISHING_MEDIA,  // ICE negotiation in progress
    ACTIVE_AUDIO,        // Connected (Audio only)
    ACTIVE_VIDEO,        // Connected (Audio + Video)
    RECONNECTING,        // ICE disconnection detected, attempting ice-restart
    TERMINATING,         // Teardown in progress
    ENDED,               // Cleanup complete
    FAILED               // Abort due to error (network, rejected, timeout)
}
```

### 2.2 CallUiState

The contract exposed by `CallViewModel` for the primary UI (Incoming, Outgoing, Active screens).

```kotlin
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
    
    // Network Details (for bottom sheet)
    val latencyMs: Int = 0,
    val packetLossPct: Float = 0f
)

enum class CallQuality { GOOD, DEGRADED, POOR }
```

---

## 3. VIEWMODEL CONTRACTS

### 3.1 CallViewModel

**Responsibility**: Act as a proxy between the UI screens and the underlying Foreground `CallService`.

| Event | Action | Expected Output State |
| :--- | :--- | :--- |
| `placeCall(contactId, mode, isVideo)` | Dispatch offer via message layer | `OUTGOING_INIT` -> `RINGING_REMOTE` |
| `acceptCall(videoEnabled)` | Accept hardware permissions, reply Answer | `ESTABLISHING_MEDIA` |
| `declineCall()` | Send rejection signal, end local service | `ENDED` |
| `toggleMute()` | Suspend audio track capture | State updates `isMuted` |
| `toggleCamera()` | Start/Stop video track | `ACTIVE_AUDIO` ↔ `ACTIVE_VIDEO` |
| `endCall()` | Send hangup signal, stop tracks | `TERMINATING` -> `ENDED` |

### 3.2 Call Actions & Intents

The UI will not hold WebRTC tracks. It communicates with the `CallService` via standard intents:

* `ACTION_START_CALL`
* `ACTION_ACCEPT_CALL`
* `ACTION_DECLINE_CALL`
* `ACTION_END_CALL`

---

## 4. SIGNALING (E2EE MESSAGE PAYLOADS)

Signaling happens over the existing Double Ratchet E2EE message infrastructure. New Message types must be inserted into the payload definition.

### 4.1 Signaling Envelopes

```protobuf
message CallSignal {
    string session_id = 1;
    
    oneof signal_type {
        CallOffer offer = 2;
        CallAnswer answer = 3;
        CallCandidate candidate = 4;
        CallHangup hangup = 5;
    }
}

message CallOffer {
    string sdp = 1;                 // Session Description Protocol blob
    bool request_video = 2;         // True if video is desired
    PrivacyMode requested_mode = 3; // Fast/Private/Paranoid
}

message CallAnswer {
    string sdp = 1; 
    bool accepted_video = 2;
}

message CallCandidate {
    string sdp_mid = 1;
    int32 sdp_mline_index = 2;
    string candidate = 3;
}
```

### 4.2 Handling Flow

1. Rust DoubleRatchet processes incoming encrypted blob.
2. Identifies payload as `CallSignal`.
3. Dispatches via JNI callback to `CallSignalingManager` in Kotlin.
4. `CallSignalingManager` drops signal into the WebRTC state machine or initiates `CallService` (for new incoming offers).

---

## 5. NATIVE MEDIA BRIDGE (WebRTC / DTLS-SRTP)

To keep Android APK sizes contained and decouple standard WebRTC overhead, Phantom Net uses a carefully stripped native stack (via Rust/JNI) or a precompiled `libwebrtc` injected with custom DTLS policies.

### 5.1 Native Interface Requirements

```kotlin
interface NativeMediaEngine {
    // Lifecycle
    fun initializeEngine(config: MediaEngineConfig)
    fun destroyEngine()
    
    // Offer / Answer
    fun createOffer(constraints: MediaConstraints): String
    fun processAnswer(sdp: String)
    fun receiveOffer(sdp: String): String // Returns Answer
    
    // ICE Candidates
    fun addIceCandidate(sdpMid: String, sdpMLineIndex: Int, candidate: String)
    
    // Hardware control
    fun toggleMicrophone(muted: Boolean)
    fun toggleCamera(enabled: Boolean)
    fun switchCamera()
    
    // Streams
    // (Actual Android implementation uses SurfaceView renderers tied to track IDs)
}
```

### 5.2 Mode Constraints (Privacy Binding)

When passing `MediaEngineConfig` to the engine, it MUST enforce the privacy modes dictated by LAYOUT:

* **FAST (Mode A)**: `iceTransportPolicy = "all"`
* **PRIVATE (Mode B)**: `iceTransportPolicy = "relay"` (Drop host/srflx entirely)
* **PARANOID (Mode C)**: Same as PRIVATE, but configures a synthetic DTLS tunnel that binds directly to the Phantom Mixnet raw packet sender.

---

## 6. PERSISTENCE LAYER

Only call metadata (history) is saved. No media is ever recorded.

### 6.1 CallLogEntity (Room DB)

```kotlin
@Entity(tableName = "call_logs")
data class CallLogEntity(
    @PrimaryKey val sessionId: String,
    val conversationId: String,       // FK to contacts/chats
    val direction: String,            // INCOMING / OUTGOING
    val durationSec: Int,
    val timestamp: Long,              // Start time
    val privacyMode: String,          // FAST/PRIVATE/PARANOID
    val outcome: String               // COMPLETED / MISSED / REJECTED / FAILED
)
```

---

## 7. ORCHESTRATION CHECKLIST (For Implementation Phase)

* [ ] Scaffold `CallUiState` and `CallViewModel`.
* [ ] Implement `CallService` (Android Foreground Service) to keep connectivity alive.
* [ ] Define Protobuf schemas for `CallSignal` inside the existing serialization crate.
* [ ] Scaffold `NativeMediaEngine` JNI stub.
* [ ] Implement `CallRepository` + DAO for persisting logs.
* [ ] Implement audio permission prompt UX flow.

---
*Authorized O-Call Baseline for Phantom Net*
