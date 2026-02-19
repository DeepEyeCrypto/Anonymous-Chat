# PHANTOM NET v2.0: FRAME

> **Status**: ACTIVE  
> **Version**: 2.0.0-alpha  
> **Date**: 2026-02-20  
> **Mission**: Transform the crash-proofed v1.x skeleton into a functional, installable, and eventually stealth-capable anonymous messenger.

---

## 1. CORE NORTH STAR (UNCHANGED)

Phantom Net provides **"Messages without Metadata"**. We protect conversation content, participant identity, the social graph, and communicative fingerprints (timing, volume, patterns).

---

## 2. v2.0 PHASED DEVELOPMENT PLAN

### Phase 1: SOLID GROUND (v2.0-alpha) — "Make it Installable & Explorable"

**Objective**: A side-loadable APK that feels like a real app, not a prototype.

| # | Deliverable | Acceptance Criteria |
|---|-------------|---------------------|
| 1 | Bottom tab navigation (Chats / Discovery / Vault / Settings) | All 4 tabs render, navigate, persist across config changes |
| 2 | Persona creation flow | User generates a PQC identity on first launch, stored in encrypted prefs |
| 3 | Onboarding (3-screen intro) | Animated walkthrough → identity generation → home screen |
| 4 | Settings screen | Theme toggle, identity fingerprint display, wipe button |
| 5 | SQLCipher local database | Messages, conversations, persona keys persist across restarts |
| 6 | App branding | Custom icon, splash screen, branded status bar |
| 7 | Empty/loading/error states for all screens | No blank screens — always a clear CTA or status |

### Phase 2: SIGNAL PATH (v2.0-beta) — "Make the Chat Real"

**Objective**: Alice sends an encrypted message to Bob. Bob receives and decrypts it. No servers.

| # | Deliverable | Acceptance Criteria |
|---|-------------|---------------------|
| 1 | X3DH key exchange | Two peers establish a shared session via out-of-band public key swap |
| 2 | Double Ratchet | Per-message key derivation with forward secrecy |
| 3 | DHT message relay | Sender publishes encrypted payload to DHT; recipient polls/subscribes |
| 4 | Real conversation state | Messages stored in SQLCipher with delivery status |
| 5 | Contact management | Add contact via QR code or paste-pubkey, stored locally |
| 6 | Notifications | Background polling with local notification for new DHT messages |

### Phase 3: GHOST MODE (v2.0-rc) — "Stealth & Deniability"

**Objective**: The features that make Phantom Net a survival tool.

| # | Deliverable | Acceptance Criteria |
|---|-------------|---------------------|
| 1 | Deniable profiles (Cover/Hidden PIN) | Cover PIN → fake chat list; Hidden PIN → real workspace |
| 2 | Panic wipe (gesture + button) | Triple-tap or PANIC → keys zeroed, DB dropped, app resets |
| 3 | Disappearing messages | Per-conversation self-destruct timer (5s / 30s / 5m / 1h / off) |
| 4 | Stealth app icon | User can switch launcher icon to "Calculator" or "System Config" |
| 5 | Cover traffic injection | Background dummy heartbeat packets on jittered timer |
| 6 | Sentinel DB wipe (Rust) | Forensic-grade wipe of SQLCipher DB, shared prefs, cached keys |

---

## 3. FEATURE ROADMAP

### MUST-HAVE (Phase 1 — v2.0-alpha)

- Tab navigation (Chats / Discovery / Vault / Settings)
- Persona creation with Kyber-768 identity
- Onboarding walkthrough (3 screens)
- SQLCipher local persistence
- Settings with identity fingerprint + wipe
- Empty/loading/error states everywhere
- App icon + splash screen

### SHOULD-HAVE (Phase 2 — v2.0-beta)

- X3DH + Double Ratchet E2EE (real, not AES-random-key demo)
- DHT-based message delivery (publish/subscribe)
- Contact exchange via QR code
- Delivery receipts (sent/delivered/read)
- Background polling for new messages

### NICE-TO-HAVE (Phase 3 — v2.0-rc)

- Deniable profiles (Cover/Hidden PIN)
- Panic gesture (triple-tap wipe)
- Disappearing messages with visual animation
- Stealth launcher icon
- Cover traffic injection
- Forensic-grade file shredding

---

## 4. ARCHITECTURE

### Pattern: Clean Architecture + MVVM

```
┌─────────────────────────────────────────────┐
│                 ANDROID UI                   │
│  Jetpack Compose + Navigation + ViewModels   │
├─────────────────────────────────────────────┤
│              DOMAIN LAYER                    │
│  UseCases, Repositories (interfaces),        │
│  Models (Message, Conversation, Persona)     │
├─────────────────────────────────────────────┤
│              DATA LAYER                      │
│  SQLCipher DAOs, EncryptedSharedPrefs,        │
│  DHT MessageStore, JNI Bridge wrappers       │
├─────────────────────────────────────────────┤
│        RUST NATIVE CORE (via JNI)            │
│  libsignal-ffi, kademlia-dht, tor-client,    │
│  phantom-core, phantom-sentinel, etc.        │
└─────────────────────────────────────────────┘
```

### New Modules for v2.0

| Module | Layer | Purpose |
|--------|-------|---------|
| `:core:database` | Data | SQLCipher Room DB — MessageEntity, ConversationEntity, PersonaEntity |
| `:core:identity` | Domain | Persona lifecycle — generation, serialization, fingerprint derivation |
| `:feature:onboarding` | UI | 3-screen intro + persona creation wizard |
| `:feature:settings` | UI | Identity display, theme, wipe, about |
| `:feature:contacts` | UI + Domain | QR scan/generate, contact storage, key exchange |

### Cross-Cutting Concerns

- **Logging**: Timber (Android) + android_logger (Rust). NEVER log keys, plaintext, or PII.
- **Secrets**: EncryptedSharedPreferences for persona key material. No hardcoded secrets.
- **Error Handling**: All JNI calls use `*Safe()` wrappers (implemented in v1.4.5).
- **Threading**: All crypto/network ops on Dispatchers.IO. UI never blocks.

---

## 5. TECH STACK

| Layer | Technology |
|-------|-----------|
| Language (Android) | Kotlin 2.1, JDK 21 |
| UI | Jetpack Compose + Material 3 |
| Navigation | Navigation Compose (type-safe routes) |
| Local DB | Room + SQLCipher |
| Encrypted Prefs | AndroidX Security Crypto |
| DI | Manual / Koin |
| Async | Kotlin Coroutines + StateFlow |
| QR | ML Kit Barcode + ZXing (generation) |
| Language (Core) | Rust (edition 2021) |
| Crypto | x25519-dalek, aes-gcm, pqcrypto-kem (Kyber) |
| Session Protocol | vodozemac (Matrix's Double Ratchet) — preferred over rolling our own |
| Networking | libp2p (Kademlia), arti-client (Tor) |
| JNI | jni crate |
| CI | GitHub Actions |
| Distribution | GitHub Releases (APK), future F-Droid |

---

## 6. RISKS & MITIGATIONS

| Risk | Severity | Mitigation |
|------|----------|------------|
| SQLCipher adds ~5MB to APK | Low | Acceptable for security benefit |
| Double Ratchet complexity | High | Use vodozemac instead of custom impl |
| DHT messages have no TTL | Medium | Implement TTL in Kademlia value store |
| Two-device testing | Medium | Use emulator bridge or local WiFi |
| Cover/Hidden PIN timing side-channel | High | Both modes must have identical launch timing |
| Panic wipe must be forensic-grade | High | Overwrite with random bytes before unlink (Rust) |

---

## 7. HANDOFF CONTRACTS

- **LAYOUT**: Design tab navigation, onboarding flow, settings screen, contact exchange UX, empty states.
- **ORCHESTRATION**: Define SQLCipher schema, persona lifecycle API, DHT message store/retrieve contract.
- **WORLD**: Update CI for SQLCipher dependency, signing, F-Droid metadata.

---
*Authorized by the Phantom Net Architecture Squad — v2.0 Cycle.*
