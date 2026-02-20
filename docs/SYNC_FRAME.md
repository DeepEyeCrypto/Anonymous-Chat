# PHANTOM NET: MULTI-DEVICE SYNC FRAME

## Phase F-Sync: FRAME (Architecture, Threat Model, and Roadmap)

> **Status**: PROPOSED BASELINE (MULTI-DEVICE SYNC)  
> **Version**: 1.0.0  
> **Role**: Decentralized Identity & State Architecture Squad  
> **Mission**: Allow users to share a single Phantom identity across multiple physical devices without a central server.

---

## 1. ROLE, GOAL, AND SECURITY BOUNDARY

This document defines the architecture for cloning and synchronizing Phantom Net personas across devices.

### 1.1 Product goal

Enable users to:

1. Initialize a new device (Phone B) using an existing identity from Phone A.
2. Mirror contacts, conversation keys, and (optionally) message history.
3. Maintain identity consistency across devices (same fingerprint).
4. Receive messages on all linked devices simultaneously.

### 1.2 Security boundary (non-negotiable)

1. **No Cloud Intermediate**: Persona secrets never pass through a central server or cloud storage.
2. **E2EE Sync Channel**: The transfer of the master `rootKey` and `Persona` secrets must be protected by a secondary, out-of-band authenticated key exchange (e.g., QR scan).
3. **Device Isolation**: Even though devices share a persona, they should ideally use distinct device-specific ephemeral keys for the Double Ratchet to prevent state desync issues (MLS-style).

---

## 2. THE SYNC PROTOCOL: "DECENTRALIZED PAIRING"

Since Phantom Net has no servers, we use a **P2P Identity Cloning** model.

### 2.1 The 4-Step Pairing Flow

1. **Step 1: Sync Invitation (Phone A)**
   - Phone A (Source) generates a `PairingInvitation` containing:
     - Its current DHT address (mailbox).
     - An ephemeral `SyncPublicKey` (X25519).
   - This is displayed as a QR code.

2. **Step 2: Sync Request (Phone B)**
   - Phone B (Target) scans the QR.
   - Phone B generates its own `EphemeralSyncKey`.
   - Phone B derives a `PairingSharedSecret` via Diffie-Hellman.
   - Phone B sends an encrypted `SyncRequest` to Phone A via the DHT.

3. **Step 3: User Approval (Phone A)**
   - Phone A receives the request.
   - UI shows: "Link New Device? Verify these 3 words match on the other phone: [PANDA-derived words]."
   - User confirms.

4. **Step 4: Identity Transfer**
   - Phone A encrypts the Persona's **Master Secret** (`rootKey`), **Prekey Bundles**, and **Database Passphrase**.
   - These are sent to Phone B.
   - Phone B imports the secrets, initializes its own SQLCipher DB, and becomes a "Mirror" of Phone A.

### 2.2 Ongoing State Sync (DHT Mailbox)

- **Incoming Messages**: When Alice sends to Bob, she sends to Bob's *Global Persona Mailbox*. All of Bob's devices (Phone A and Phone B) poll the same mailbox.
- **De-duplication**: Devices use message UUIDs to ensure only one "New Message" notification is shown (or dismissed globally).
- **Outbox Sync**: When Phone A sends a message, it optionally "CCs" its own identity (encrypted for itself) so Phone B can see the outgoing history.

---

## 3. THREAT MODEL & MITIGATIONS

| Threat | Mitigation |
| :--- | :--- |
| **QR Shoulder Surfing** | The QR only contains public metadata. Secrets are only transferred after a DH exchange and user approval. |
| **Stolen Sync Container** | The sync payload is encrypted with a key derived from an out-of-band QR exchange. |
| **Identity Theft** | Pairing requires physical access to the unlocked Source device. |
| **State Desync** | Use MLS (Messaging Layer Security) to treat multiple devices as a single group entity, allowing for monotonic epoch counters. |

---

## 4. IMPLEMENTATION ROADMAP (PHASE 6)

### 4.1 Stage 1: Identity Mirroring (MVP) - ✅ COMPLETE

- Implement `SyncManager` in `:core:identity`.
- QR Generation (Source) and QR Scanning (Target).
- Basic "Identity Clone" (Master key transfer).
- **Goal**: One identity, two phones, fresh history on Phone B.

### 4.2 Stage 2: Selective State Sync - ✅ COMPLETE

- Transfer active `Conversation` metadata and keys (X3DH sessions).
- Allow Phone B to resume existing chats immediately.

### 4.3 Stage 3: Full History Catch-up - ✅ COMPLETE (MVP)

- Allow Phone B to request encrypted message blocks from Phone A over the DHT or local WiFi (Onion Relay).

---

## 5. RELEVANT MODULE CONTRACTS

### `SyncManager.kt`

- `generatePairingInvitation()`
- `handleSyncRequest(request: SyncRequest)`
- `decryptAndImportPersona(payload: EncryptedPersonaPayload)`

### `SyncViewModel.kt`

- Manages the pairing state machine (Idle -> Inviting -> Negotiating -> Completed).

---

*Next: Implement the `SyncManager` and update the `Settings` UI to include a "Link Device" option.*
