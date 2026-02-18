# PHANTOM NET: AUDIO / VIDEO CALLING FRAME

## Phase F-Call: FRAME (Architecture, Threat Model, and Roadmap)

> **Status**: AUTHORITATIVE BASELINE (CALLING FRAME)  
> **Version**: 1.0.0  
> **Role**: VoIP + WebRTC + Privacy + Protocol Architecture Squad  
> **Mission**: Add secure calling without breaking username-only anonymity guarantees.

---

## 1. ROLE, SCOPE, AND BASE ASSUMPTIONS

This document extends Phantom Net's existing E2EE messenger with privacy-preserving real-time calls.

### 1.1 Existing assumptions

1. No phone numbers or email identities.
2. Users already communicate through a Signal-style E2EE messaging protocol (X3DH + Double Ratchet).
3. Existing routing already supports onion/mixnet-style metadata protection.
4. Android is first implementation target, but protocol contracts must be cross-platform.

### 1.2 Scope in this phase

* Architecture and roadmap only (no implementation code).
* 1:1 audio and video as primary target.
* Group audio is planned as a later phase.
* Screen sharing is optional and deferred.

---

## 2. STAGE 0 — REQUIREMENTS & THREAT MODEL

## 2.1 Functional requirements

1. **1:1 audio calls** (baseline MVP).
2. **1:1 video calls** (policy controlled and optionally disabled in high-risk mode).
3. Call setup requires only:
   * existing chat relationship,
   * existing E2EE messaging channel for signaling.
4. No new centralized plaintext signaling service.

## 2.2 Security and privacy requirements

1. Media must be E2EE using SRTP-class protections (DTLS-SRTP stack or equivalent).
2. Remote party authentication must be bound to the same trust root used by messaging identities.
3. Offer/answer/candidate signaling must travel as normal E2EE app messages.
4. Users must be able to choose between speed and anonymity (explicit modes).
5. High-risk mode must avoid direct IP revelation to peers by default.

## 2.3 Calling-specific threats

1. Peer IP leakage through host/srflx ICE candidates.
2. External provider visibility if public STUN/TURN is used.
3. Timing/traffic correlation on encrypted RTP flows.
4. Fingerprinting through codecs, transport parameters, or device behavior.
5. Human-level deanonymization via voice biometrics/video background.

## 2.4 Threat response principles

1. **Safe defaults by risk profile** (not one universal default for all personas).
2. **No silent downgrade** from private mode to fast mode.
3. **User-visible privacy state** before and during calls.
4. **Metadata minimization** in logs, telemetry, and relay infrastructure.

---

## 3. STAGE 1 — ARCHITECTURE CHOICES (NO CODE)

## 3.1 Media and transport baseline

Adopt a WebRTC-class media engine (or equivalent DTLS-SRTP implementation) for:

* real-time jitter control,
* adaptive codecs/bitrates,
* NAT traversal framework,
* mature, audited media encryption stack.

### Mandatory constraints

1. Do not use public default STUN endpoints.
2. ICE policy is mode-dependent (see Stage 2).
3. All signaling remains inside existing E2EE message transport.
4. Media confidentiality remains end-to-end regardless of relay path.

## 3.2 Media keying and authentication model

### Preferred model (Option A: Signal-style binding)

1. Use Double Ratchet channel to establish per-call secret material.
2. Derive call auth keys and policy MACs from this secret.
3. Bind call setup parameters (mode, media type, capabilities, anti-downgrade flags) to authenticated E2EE messages.
4. Use DTLS-SRTP transport while treating identity authenticity as anchored to messaging identity trust.

### Alternate model (Option B: fingerprint binding)

1. Use standard DTLS key exchange in media stack.
2. Exchange DTLS certificate fingerprints over E2EE signaling channel.
3. Verify fingerprints before unmuting media.

**Decision**: Prefer Option A, with Option B compatibility fallback where platform limitations require fingerprint verification workflow.

## 3.3 Signaling architecture

No separate call signaling backend is introduced. Call control uses existing encrypted message envelopes.

Recommended call-control message families:

* `CALL_OFFER`
* `CALL_ANSWER`
* `CALL_CANDIDATE`
* `CALL_ACCEPT` / `CALL_DECLINE`
* `CALL_RING`
* `CALL_REKEY` (optional)
* `CALL_HANGUP`
* `CALL_MODE_SWITCH_REQUEST`
* `CALL_MODE_SWITCH_ACK`

### Signaling hardening

1. Include selected privacy mode in signed/MACed offer.
2. Reject candidate classes not allowed by mode policy.
3. Reject late downgrade attempts unless user explicitly re-approves.
4. Tie call session ID to conversation identity domain (prevents cross-chat replay).

---

## 4. STAGE 2 — CALL MODES (SPEED VS. ANONYMITY)

## 4.1 Mode matrix

| Mode | User Label | Path | IP Exposure | Latency | Recommended Use |
| :--- | :--- | :--- | :--- | :--- | :--- |
| **A** | Fast | Direct P2P ICE (+ relay fallback) | Peer can learn IP | Lowest | Trusted contacts |
| **B** | Private | Phantom relay path (TURN-like relays) | Peer IP hidden | Medium | Default privacy profile |
| **C** | Paranoid | Onion/mixnet wrapped signaling + media | Maximum concealment | Highest | High-risk personas |

## 4.2 Mode A — Direct P2P (fastest, least anonymous)

* ICE may include host/srflx/relay candidates.
* TURN used as backup only.
* Explicit warning required: **"Peer may see your IP."**
* Best quality for full video.

## 4.3 Mode B — Phantom Relay (balanced privacy)

* Force relay-only media candidates through Phantom-operated/community relays.
* Disable host candidates; srflx candidates disabled by default.
* STUN minimized or private/self-hosted only.
* Relays forward encrypted media and do not terminate E2EE media contents.

## 4.4 Mode C — Mixnet/Onion overlay (maximum anonymity)

* Both signaling and media traverse multi-hop onion/mixnet paths.
* Audio-first profile; video off by default and behind explicit warning.
* Strongest resistance to direct peer IP leakage.
* Quality expectations prioritize survivability/privacy over fidelity.

## 4.5 UX policy controls

Per-contact selector:

* **Fast** (A)
* **Private** (B)
* **Paranoid** (C)

Policy-level protections:

1. Contact-level default mode memory.
2. One-tap temporary escalation/de-escalation for current call only.
3. Security logs note mode changes locally (no server sync).

---

## 5. STAGE 3 — FEATURE ROADMAP

## 5.1 Phase 1: Secure audio MVP

Deliverables:

1. 1:1 audio calling.
2. E2EE signaling over existing message channel.
3. Media encryption + identity binding model finalized.
4. Mode A + Mode B production-ready.

Exit criteria:

* Reliable setup/teardown.
* Reconnect handling for short network drops.
* In-call controls: mute, speaker route, end call.

## 5.2 Phase 2: Video + stronger NAT strategy

Deliverables:

1. Add camera video track.
2. Adaptive bitrate and resolution.
3. Distributed private STUN/TURN estate (no public default services).
4. Region-aware relay selection.

Exit criteria:

* Stable video in Mode A and Mode B under expected mobile conditions.

## 5.3 Phase 3: Group audio + multi-device ringing

Deliverables:

1. Small group audio rooms.
2. Relay-forwarded/SFU-like architecture while preserving per-user encryption boundaries.
3. Multi-device incoming call forking (ring all active devices; first accepted device wins).

Exit criteria:

* Deterministic active-device selection.
* Membership verification reuses existing group trust primitives (MLS/Sender-Key policy as available).

## 5.4 Phase 4: Mixnet mode + PQC hybrid enhancements

Deliverables:

1. Mode C production rollout for high-risk chats.
2. Benchmarked voice profile under high latency.
3. Optional hybrid PQ key agreement hardening for call setup.

Exit criteria:

* Explicit user expectations and warnings validated in UX testing.

---

## 6. STAGE 4 — UX AND PRIVACY CONTROLS

## 6.1 Pre-call UX

1. First-call privacy panel per contact.
2. Mode impact preview: IP exposure, expected quality, estimated delay.
3. Display existing identity verification status (safety number/fingerprint trust).

## 6.2 In-call UX

1. Encryption indicator (lock + concise explanation).
2. Real-time mode badge (Fast/Private/Paranoid).
3. Emergency privacy actions:
   * audio-only fallback,
   * disable camera instantly,
   * optional blur/background masking,
   * quick quality downgrade.

## 6.3 Post-call UX

1. Local encrypted call log only.
2. Optional per-chat auto-delete.
3. No cloud sync of call history.
4. Store only minimal metadata required for UX (duration/direction/result).

---

## 7. STAGE 5 — INFRASTRUCTURE FOR RELAYED CALLS

## 7.1 TURN-like relay design principles

1. Relay forwards encrypted media packets without media decryption.
2. Discovery integrated with existing decentralized routing/discovery mechanisms.
3. Encourage multi-operator, multi-region deployment.

## 7.2 Capacity and QoS policy

1. Audio prioritization over video in constrained/high-risk mode.
2. Abuse control via anonymous tokens/rate controls (no user identity logs).
3. Aggregate-only operational telemetry (bandwidth, CPU, error rate).

## 7.3 Censorship resistance

1. Relay endpoints available on common ports.
2. Transport camouflage compatible with existing censorship-resilience stack.
3. User guidance for additional tunnel stacking (VPN/Tor where legal and safe).

---

## 8. STAGE 6 — TESTING & VERIFICATION PLAN

## 8.1 Security verification

1. Packet captures confirm no plaintext media content.
2. Mode-policy tests ensure no candidate leakage in Mode B/C.
3. Downgrade-resistance tests verify forced mode shifts are rejected.
4. Adversarial exercises for IP discovery attempts across all modes.

## 8.2 Performance validation

Track:

* one-way latency,
* jitter,
* packet loss,
* MOS/voice quality proxies.

Target envelopes:

* Mode A: typically < 150 ms one-way (network permitting).
* Mode B: typically < 250 ms one-way.
* Mode C: 300–600 ms acceptable for high-risk voice.

## 8.3 UX validation

1. Test user comprehension of mode selector and IP warnings.
2. Measure successful decision-making under risk-oriented onboarding.
3. Validate that emergency privacy toggles are discoverable under stress.

---

## 9. COMPATIBILITY, SAFETY, AND FAILURE POLICY

1. Legacy clients without calling support receive standard message fallback.
2. Unsupported mode requests fail closed (no silent fallback to Mode A).
3. Network handover logic (Wi-Fi ↔ mobile) attempts session continuity before forced teardown.
4. Client crash/restart must clear ephemeral call secrets from memory.

---

## 10. HANDOFF CONTRACTS FOR NEXT PHASES

If **LAYOUT** is requested next, produce:

* call screen flows (start/incoming/in-call/privacy selector),
* call lifecycle state diagrams,
* non-code data models (`CallSession`, `MediaEndpoint`, `RelayRoute`, `ModePolicy`).

If **ORCHESTRATION** is requested next, split into:

* client media stack team,
* signaling/protocol team,
* relay infrastructure team,
* QA/security validation team.

If **WORLD** is requested next, define:

* CI instrumentation for call latency/jitter,
* canary rollout strategy for relay nodes,
* release compatibility checklist across app versions.

---

## 11. DECISION SUMMARY (LOCK)

1. Keep signaling inside existing E2EE messages.
2. Use WebRTC-class media stack with strict mode-based ICE policy.
3. Prefer Signal-style identity binding for call authentication.
4. Ship in phases: Audio MVP → Video → Group/Multi-device → Mixnet + PQ.
5. Make privacy controls explicit and user-facing at all times.

---

*Authorized as the Phantom Net Calling FRAME baseline.*
