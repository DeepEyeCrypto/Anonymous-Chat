# PHANTOM NET: AUDIO / VIDEO CALLING LAYOUT

## Phase L-Call: LAYOUT (Screen Flows, State Diagrams, and Non-Code Models)

> **Status**: ACTIVE (CALLING LAYOUT PHASE START)  
> **Version**: 1.0.0  
> **Depends On**: `docs/CALLING_FRAME.md`  
> **Focus**: Turn calling architecture into concrete UX flows and deterministic client states.

---

## 1. LAYOUT MISSION

This document converts the Calling FRAME into implementation-ready UI/UX structure (without code):

1. End-to-end call screen flows.
2. Deterministic call lifecycle states (including reconnect and media upgrade/downgrade).
3. Non-code data contracts for client UX state.

Design target: preserve anonymity controls while keeping call UX understandable under stress.

---

## 2. IA ADDITIONS FOR CALLING

Calling introduces new surfaces inside existing app IA.

### 2.1 Entry points

1. **Chat Header Call Actions**
   * Audio call button
   * Video call button
2. **Chat Settings → Calling & Privacy**
   * Default mode: Fast / Private / Paranoid
   * Video default policy
3. **Conversation List Incoming Call Card**
   * Ringing indicator with accept/decline actions

### 2.2 New calling screens

1. **Pre-Call Privacy Panel** (first call per contact, or when mode changed)
2. **Outgoing Call Screen**
3. **Incoming Call Screen**
4. **In-Call Audio Screen**
5. **In-Call Video Screen**
6. **Call Route Details Bottom Sheet**
7. **Reconnection Overlay**
8. **Call End Summary Sheet**

---

## 3. SCREEN FLOWS

## 3.1 Outgoing call flow (Audio baseline)

1. User taps **Audio Call** from chat header.
2. If first call / policy changed, show **Pre-Call Privacy Panel**:
   * Selected mode (Fast / Private / Paranoid)
   * IP exposure warning (Fast only)
   * Latency expectation per mode
3. User confirms call.
4. Show **Outgoing Call Screen**:
   * contact avatar/name
   * "Calling…" status
   * selected privacy mode badge
   * cancel button
5. On remote accept → transition to **In-Call Audio Screen**.
6. On timeout/decline/failure → show **Call Failed/Declined sheet** with retry action.

## 3.2 Incoming call flow

1. Incoming call event arrives over E2EE signaling.
2. Show **Incoming Call Screen**:
   * caller identity
   * verification state (trusted/unverified)
   * privacy mode requested by caller
   * actions: Accept / Decline / "Accept Audio-Only"
3. If requested mode conflicts local policy:
   * show forced confirmation dialog (e.g., caller requests Fast)
4. On accept:
   * establish media
   * enter **In-Call Audio** or **In-Call Video** as negotiated

## 3.3 First-time privacy selector flow

Shown when:

* first call with a contact,
* local mode changed,
* caller proposes mode downgrade.

Panel content:

1. **Mode selector** (Fast / Private / Paranoid)
2. **Risk label**
   * Fast: "Peer may see your IP"
   * Private: "Relayed via Phantom nodes"
   * Paranoid: "Max anonymity, higher delay"
3. **Estimated quality card** (Voice/Video expectation)
4. **Remember choice for this contact** toggle

## 3.4 In-call controls flow

Primary controls (bottom bar):

1. Mute/unmute mic
2. Speaker/earpiece route
3. End call
4. Video toggle (if camera-capable call)
5. More actions (sheet)

More actions sheet:

* Switch to audio-only
* Lower quality preset
* View route details
* Request mode switch (if policy allows)
* Safety code/fingerprint status

## 3.5 Video upgrade / downgrade flow

Upgrade:

1. User taps "Turn on video".
2. Local camera permission check.
3. Send upgrade request to peer.
4. Peer accepts/rejects.
5. On accept → transition audio UI to video UI.

Downgrade:

1. User taps "Audio-only" OR bandwidth degrades.
2. Video track disabled.
3. Return to audio call layout while call continues.

## 3.6 Reconnection flow

1. Detect network degradation/drop.
2. Show **Reconnection Overlay** (non-dismissable transient):
   * "Reconnecting securely…"
   * mode badge remains visible
3. Attempt route recovery within policy timeout window.
4. On success: resume call in prior media state.
5. On failure: end call with reason + one-tap redial.

---

## 4. CALL LIFECYCLE STATE DIAGRAMS (TEXT)

## 4.1 Primary call lifecycle

`IDLE`
→ `OUTGOING_INIT`
→ `RINGING_REMOTE`
→ `ESTABLISHING_MEDIA`
→ `ACTIVE_AUDIO` / `ACTIVE_VIDEO`
→ (`RECONNECTING` optional)
→ `TERMINATING`
→ `ENDED`

Failure branches:

* `OUTGOING_INIT` → `FAILED_SETUP`
* `RINGING_REMOTE` → `DECLINED` / `NO_ANSWER`
* `ESTABLISHING_MEDIA` → `FAILED_MEDIA`

## 4.2 Incoming lifecycle

`IDLE`
→ `INCOMING_RINGING`
→ (`DECLINED` | `ACCEPTED`)
→ `ESTABLISHING_MEDIA`
→ `ACTIVE_AUDIO` / `ACTIVE_VIDEO`

## 4.3 Media upgrade lifecycle

`ACTIVE_AUDIO`
→ `VIDEO_UPGRADE_REQUESTED`
→ (`VIDEO_UPGRADE_ACCEPTED` → `ACTIVE_VIDEO`) OR (`VIDEO_UPGRADE_REJECTED` → `ACTIVE_AUDIO`)

`ACTIVE_VIDEO`
→ (`VIDEO_DOWNGRADE_USER` | `VIDEO_DOWNGRADE_NETWORK`)
→ `ACTIVE_AUDIO`

## 4.4 Mode switch lifecycle

`MODE_LOCKED_CURRENT`
→ `MODE_SWITCH_REQUESTED`
→ (`PEER_ACCEPTED` + `POLICY_VALIDATED` → `MODE_SWITCH_APPLYING` → `MODE_LOCKED_NEW`)
or (`PEER_REJECTED` / `POLICY_BLOCKED` → `MODE_LOCKED_CURRENT`)

---

## 5. UI BLUEPRINT BY SCREEN

## 5.1 Pre-Call Privacy Panel

Required elements:

1. Contact identity + verification status row.
2. Mode segmented control.
3. Warning banner (mode-specific).
4. Quality/latency estimate chip set.
5. Confirm / Cancel actions.

## 5.2 Outgoing/Incoming Call Screens

Shared elements:

1. Large avatar or profile glyph.
2. Call status text.
3. Mode badge (Fast/Private/Paranoid).
4. E2EE lock indicator.

Incoming-only:

* Accept, Accept Audio-Only, Decline.

Outgoing-only:

* Cancel.

## 5.3 In-Call Audio Screen

1. Participant tile(s) with call duration.
2. Real-time quality chip (Good/Degraded).
3. Bottom control row.
4. Privacy badge always visible.

## 5.4 In-Call Video Screen

1. Remote video full canvas.
2. Local preview PiP tile.
3. Quick toggles overlay:
   * camera on/off,
   * mic,
   * switch camera,
   * audio-only fallback.

## 5.5 Route Details Bottom Sheet

1. Current mode + explanation.
2. Route class:
   * direct / relay / onion-mixnet
3. Connection quality metrics (coarse-grained, no sensitive IDs).
4. Link to "Why this matters" privacy explainer.

## 5.6 End Call Summary Sheet

1. Call duration.
2. Direction (incoming/outgoing).
3. Mode used.
4. Optional quick actions:
   * redial,
   * report quality,
   * set default mode for contact.

---

## 6. NON-CODE DATA MODELS

## 6.1 `CallSession`

| Field | Type | Notes |
| :--- | :--- | :--- |
| `sessionId` | String | Unique per call, bound to conversation domain |
| `conversationId` | String | Existing chat relationship ID |
| `direction` | Enum | Incoming / Outgoing |
| `callType` | Enum | Audio / Video |
| `mode` | Enum | Fast / Private / Paranoid |
| `state` | Enum | Lifecycle state machine value |
| `startedAt` | Timestamp | Local start time |
| `endedAt` | Timestamp? | Local end time |
| `endReason` | Enum? | User hangup / decline / timeout / network |
| `verificationState` | Enum | Trusted / Unverified / Changed |

## 6.2 `MediaEndpoint`

| Field | Type | Notes |
| :--- | :--- | :--- |
| `endpointId` | String | Device endpoint identifier (ephemeral) |
| `supportsVideo` | Bool | Capability advertisement |
| `supportsModeSwitch` | Bool | Runtime policy support |
| `currentMediaState` | Enum | AudioOnly / AudioVideo |
| `networkClass` | Enum | Wifi / Cellular / Unknown |
| `qualityState` | Enum | Good / Degraded / Poor |

## 6.3 `RelayRoute`

| Field | Type | Notes |
| :--- | :--- | :--- |
| `routeType` | Enum | Direct / Relay / OnionMixnet |
| `relayClass` | Enum? | Community / Dedicated / Unknown |
| `hopCountBucket` | Enum | 0 / 1 / 3+ (coarse only) |
| `policyCompliant` | Bool | Matches selected mode policy |
| `lastUpdatedAt` | Timestamp | For route diagnostics |

## 6.4 `ModePolicy`

| Field | Type | Notes |
| :--- | :--- | :--- |
| `mode` | Enum | Fast / Private / Paranoid |
| `allowDirectCandidates` | Bool | True for Fast only |
| `allowRelayCandidates` | Bool | Required for Private/Paranoid |
| `allowVideoByDefault` | Bool | False in Paranoid default |
| `requiresDowngradeConfirmation` | Bool | Always true |
| `fallbackBehavior` | Enum | FailClosed / PromptUser |

## 6.5 `CallLogEntry`

| Field | Type | Notes |
| :--- | :--- | :--- |
| `sessionId` | String | Reference to call session |
| `conversationId` | String | Local-only mapping |
| `durationSec` | Int | Minimal UX metric |
| `direction` | Enum | Incoming / Outgoing |
| `mode` | Enum | Fast / Private / Paranoid |
| `result` | Enum | Completed / Missed / Failed |
| `storedLocalOnly` | Bool | Must always be true |

---

## 7. COPY & WARNING SPEC

## 7.1 Mode warning copy (short)

* **Fast**: "Fastest quality. Your contact may see your IP."
* **Private**: "Relayed for privacy. Better anonymity, moderate delay."
* **Paranoid**: "Maximum anonymity path. Voice-first, high latency."

## 7.2 Safety copy

* "Call media is end-to-end encrypted."
* "Unverified safety code—verify before discussing sensitive details."
* "Mode downgrade requires your confirmation."

---

## 8. ACCESSIBILITY & RESILIENCE REQUIREMENTS

1. All critical call actions reachable within one tap from in-call primary view.
2. Color is not sole carrier of privacy state; pair with icon + label.
3. Incoming call actions support screen readers with explicit role labels.
4. Reconnection and failure states include clear recovery action text.

---

## 9. QA ACCEPTANCE CHECKLIST (LAYOUT)

1. User can place and receive calls in all three modes.
2. Privacy selector appears at correct moments (first call + downgrade attempts).
3. In-call screen never hides current mode indicator.
4. Video upgrade and downgrade transitions are deterministic.
5. Reconnection overlay behavior is clear and non-confusing.
6. Call end summary matches actual call outcome.

---

## 10. HANDOFF TO ORCHESTRATION

The following outputs are ready for ORCHESTRATION:

1. Deterministic state machine contracts.
2. Non-code data models (`CallSession`, `MediaEndpoint`, `RelayRoute`, `ModePolicy`, `CallLogEntry`).
3. UI-triggered protocol events list:
   * Place call,
   * Accept/decline,
   * Candidate updates,
   * Upgrade/downgrade media,
   * Request mode switch,
   * Hangup/reconnect.

---

*Calling LAYOUT baseline prepared from `CALLING_FRAME`.*
