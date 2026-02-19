# Changelog

All notable changes to the **Phantom Net** project will be documented in this file.

## [2.0.0-alpha.1] - 2026-02-20

### Added

- **Onboarding flow**: 3-screen intro (Welcome → Privacy Promise → Identity Creation) with animated splash screen, emerald glow pulse, and live fingerprint hex grid.
- **Tab navigation**: 4-tab bottom nav (Chats / Discover / Vault / Settings) with per-tab back stacks and state restoration.
- **Settings screen**: Identity fingerprint card (tap-to-copy), security section links, Rust Core status, and WIPE IDENTITY button with "type DELETE" confirmation.
- **Discovery screen**: Animated radar sweep with PSI info chips (coming soon placeholder).
- **Vault screen**: Pulsing lock animation (coming soon placeholder).
- **`core:database` module**: Room + SQLCipher encrypted persistence with 3 entities (Persona, Conversation, Message), 3 DAOs, and `PhantomDatabaseFactory`.
- **`core:identity` module**: `IdentityManager` with EncryptedSharedPreferences, key generation, passphrase derivation, and forensic-grade wipe.
- **Domain models**: `Persona`, `Conversation`, `Message`, `RoutingMode`, `MessageStatus`.
- **CI pipelines**: `android-ci.yml` for fast dev/PR builds, updated `rust-ci.yml` with clippy + cargo audit.

### Changed

- **Design system**: Upgraded to "Obsidian & Emerald" palette (12 semantic color tokens). Forced dark theme, no dynamic color.
- **Theme**: Status bar and navigation bar now use Obsidian (`#0B0E11`) instead of primary green.
- **Architecture**: Multi-module Gradle structure with `core:database`, `core:identity`, `feature:onboarding`, `feature:settings`.
- **CI**: Renamed `android.yml` → `android-release.yml`; Rust CI now triggers on `dev` branch.
- Android app version bumped to `2.0.0-alpha.1` (`versionCode 8`).
- **Branching**: Trunk-based workflow with `dev` integration branch.

## [1.4.5] - 2026-02-20

### Fixed

- **PhantomCore init crash**: `initLogging()` was being called even when `System.loadLibrary("phantom_core")` failed, causing an uncaught `UnsatisfiedLinkError` that killed the process on any screen that touches `PhantomCore`.
- **Privacy Dashboard composition crash**: `PhantomCore.runPrivacyAudit()` was invoked during Compose `remember{}` — if the native lib wasn't present, the crash happened immediately on navigation, not on a user action.
- **SignalBridge JNI crash**: raw `external` calls on a library that failed to load cause fatal JNI errors that bypass `try/catch`. Added a `nativeLoaded` guard to throw a catchable `IllegalStateException` instead.
- All JNI-backed calls now use `*Safe` wrappers that check library availability before touching JNI, returning fallback values or no-ops when the native layer is missing.

### Changed

- `PhantomCore` external functions are now private; all callers use guarded `*Safe()` public API.
- `SignalBridge` exposes `encryptMessageSafe` / `decryptMessageSafe` with pre-flight guard.
- Android app version updated to `1.4.5` (`versionCode 7`).

## [1.4.4] - 2026-02-20

### Fixed

- **Release startup crash (R8 shrinker)**: disabled app release minification/resource shrinking in `android/app/build.gradle.kts` (`isMinifyEnabled = false`, `isShrinkResources = false`) to prevent runtime class stripping used indirectly by Compose/Navigation/ViewModel flows.
- **Release stability for JNI-backed UI actions**: hardened UI screens against native-link/runtime failures by wrapping `PhantomCore` calls with safe fallbacks instead of letting uncaught throwables crash the process.
  - `PrivacyDashboardScreen`: catch `Throwable` for audit parsing path and protect PANIC action call.
  - `ShardWizardScreen`: protect `splitSecret(...)` and show a user-safe fallback message when unavailable.
  - `DcNetRoomScreen`: protect `computeDcNetContribution(...)` and degrade gracefully.

### Changed

- Android app version updated to `1.4.4` (`versionCode 6`) for release packaging.

## [1.4.0] - 2026-02-19

### Added

- **Unit test suite**: `NetworkStatusTest` and `ChatViewModelTest` with JUnit4 + kotlinx-coroutines-test.
- **QA strategy document**: `docs/TESTSPRITE_STAGE_PLAN.md` covering L0–L4 test layers and coverage matrix.
- **CryptoEngine interface**: Extracted testable abstraction from `ChatViewModel` for encrypt/decrypt operations.
- **Test dependencies**: Added JUnit 4.13.2, kotlinx-coroutines-test, and core-testing to `build.gradle.kts`.

### Fixed

- Prevented Android app launch crashes caused by missing native `.so` libraries during startup.
- Hardened `PhantomApp` initialization to load native libraries individually and only start Tor/DHT/Mesh when their corresponding library is available.
- Added graceful fallback statuses (`Unavailable: native lib missing`) and broader startup error handling so missing native dependencies no longer crash the app.
- `ChatViewModel` now rejects blank messages and handles encryption/decryption failures with system messages instead of crashing.
- Removed unused imports (`rand::Rng` in `phantom-dcnet`, `zeroize::Zeroize` in `phantom-sharding`).
- Removed unnecessary `mut` bindings in `phantom-sharding` Lagrange interpolation.
- Fixed CI Gradle `cache-enabled` warning by using the correct `cache-disabled` input.

### Changed

- `ChatViewModel` constructor now accepts injectable `CryptoEngine` and `echoDelayMs` for testability.
- Android app version bumped to `1.4.0` (versionCode 2).

## [0.1.0] - 2026-02-17

### Initial Features

- **Signal Protocol FFI**: Initial implementation of E2EE using Rust and JNI.
- **Kademlia DHT**: Multi-hop peer discovery system for zero-server operation.
- **Tor Integration**: Anonymous routing using the Arti (Rust) client.
- **Mesh Networking**: Bluetooth LE discovery layer (stubbed for MVP).
- **Android UI**: Core screens (Home, Chat, Settings) built with Jetpack Compose.

### Bug Fixes

- Resolved build errors in `kademlia-dht` related to `libp2p 0.53` API changes.
- Fixed `tor-client` build issues with `arti-client` configuration.
- Unified global state management using thread-safe `OnceLock` across all Rust crates.
- Cleaned up native bridge (JNI) to avoid memory safety issues and Undefined Behavior.

### Initial Changes

- Standardized Android project versioning to `0.1.0`.
- Optimised native build script for multiple Android ABIs.

---

<!-- Generated by Phantom Net Release System -->
