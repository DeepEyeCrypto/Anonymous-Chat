# PHANTOM NET — Autonomous QA Plan (Stages 1–2)

This document captures the Stage 1 strategy blueprint and Stage 2 structured plan for the Android + Rust workspace.

## Stage 1 — Layered Test Strategy Blueprint

### L0: Contract & Schema Integrity

- **Scope:** `proto/phantom.proto` (`PhantomPacket`, `ApplicationPayload`, `CallControl`, mode/candidate constraints).
- **Goal:** prevent signaling regressions and unsafe mode-policy drift.
- **Primary Method:** parser/serialization round-trip tests in protocol consumers (Rust and Android-generated bindings when available).
- **Priority:** High.

### L1: Deterministic Unit Tests (Fast Feedback)

- **Android targets:**
  - `NetworkStatus` state transitions.
  - `ChatViewModel` message flow, crypto failure handling, blank input handling.
- **Rust targets:** keep and extend existing crate-level `#[test]` modules (mixnet, sharding, PSI, DC-net, PQC, sentinel, MLS, audit).
- **Frameworks:** JUnit4 + kotlinx-coroutines-test (Android JVM tests), Rust `cargo test`.
- **Priority:** Highest.

### L2: Integration Tests (Boundary Safety)

- **Android JNI/service boundaries:**
  - App startup with missing native libraries must degrade gracefully (no crash).
  - Service startup status propagation (`TorService`, `DhtService`, `MeshService`) reflected via `NetworkStatus`.
- **Goal:** validate fail-closed behavior and startup resilience.
- **Priority:** High.

### L3: UI/Navigation/Compose Reliability

- **Targets:** `ConversationListScreen`, `ChatScreen`, privacy/backup/room navigation.
- **Method:** Compose UI tests for core user flows and empty/error states.
- **Priority:** Medium (after L1 stability gate).

### L4: Security/Privacy Non-Functional Testing

- **Targets:** metadata minimization constraints from FRAME docs, mode downgrade protections, stealth/calling policy behavior.
- **Method:** policy conformance tests + adversarial scenario scripts.
- **Priority:** Medium/High for release hardening.

### Environment Constraints (Current)

- Android local test execution is currently blocked in this environment (no `android/gradlew`, no `gradle` binary).
- Rust `cargo` checks/tests remain runnable and are used as executable baseline checks.

---

## Stage 2 — Structured Test Plan + Compact Coverage Matrix

## Planned Test Cases (initial tranche)

### TP-01 (P0): `NetworkStatus` default values are deterministic

- Verify initial states: Tor=`Initializing...`, DHT=`Waiting...`, Mesh=`Inactive`.

### TP-02 (P0): `NetworkStatus` update methods atomically update exposed state

- Verify `updateTorStatus`, `updateDhtStatus`, `updateMeshStatus` set expected values.

### TP-03 (P0): `ChatViewModel.sendMessage` ignores blank input

- Prevent empty/whitespace message insertion.

### TP-04 (P0): `ChatViewModel.sendMessage` success path

- Valid text -> outgoing message + simulated echo reply after delay.

### TP-05 (P0): `ChatViewModel` encryption failure path

- Crypto failure must not crash; add system error message.

### TP-06 (P0): `ChatViewModel` decryption failure path

- Incoming decrypt failure must not crash; add system error message.

### TP-07 (P1): App startup native-missing resilience (planned integration)

- `PhantomApp` startup should set "Unavailable: native lib missing" statuses and continue startup.

### TP-08 (P1): CALL_SIGNAL policy conformance (planned contract)

- Validate mode/candidate constraints for Fast/Private/Paranoid signaling paths.

## Compact Coverage Matrix

| Area | Risk | Existing Coverage | New/Planned Coverage | Status |
| --- | --- | --- | --- | --- |
| Rust crypto/network primitives | High | Multiple crate tests | Continue regression runs | Active |
| Android app launch/native loading | High | Manual fix + changelog | Planned integration tests (TP-07) | Planned |
| Android chat state management | High | None before this cycle | TP-03..TP-06 unit tests | Implemented |
| Android network status state | Medium | None before this cycle | TP-01..TP-02 unit tests | Implemented |
| Protocol call signaling contract | High | Schema only | TP-08 contract tests | Planned |
| Compose navigation/core flows | Medium | None | UI tests for home/chat/privacy flows | Planned |
