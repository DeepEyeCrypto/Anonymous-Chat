# PHANTOM NET: ADVANCED SECURE MESSENGER

## ARCHITECTURAL FRAMEWORK (PHASE F: FRAME)

> **Status**: AUTHORITATIVE BASELINE (EXTREME ANONYMITY UPGRADE)  
> **Version**: 1.2.0  
> **Role**: Security & Product Architect Squad  
> **Mission**: Messages without Metadata — Unlinkable, Untraceable, and Plausibly Deniable.

---

## 1. CORE NORTH STAR

Phantom Net provides **"Messages without Metadata"**. We protect the conversation content, the identity of the participants, the social graph, and the communicative fingerprints (timing, volume, and patterns).

### Non-Negotiable Principles

1. **Universal E2EE**: Mandatory Signal/MLS E2EE. No unencrypted fallback.
2. **Identity Without Identifiers**: No phone numbers, emails, or central usernames.
3. **Graph Isolation**: Per-relationship cryptographic domains (no global IDs).
4. **Extreme Transport Layer**: Adaptive Onion-Routing, Mixnets with cover traffic, and DC-Nets for broadcast anonymity.
5. **Deniability by Design**: Stealth installation and plausibly deniable storage profiles.

---

## 2. THE ROUTING MATRIX

To counter Global Passive Adversaries (GPA), Phantom Net offers three modular transport tiers.

### 2.1 Mixnet: Paranoia Mode

Beyond basic onion routing, **Paranoia Mode** targets timing/volume analysis:

* **Constant Throughput**: Client sends $N$ packets per minute regardless of activity. If no message is pending, dummy "noise" packets are injected.
* **Uniform Packet Size**: All packets are padded/fragmented to an identical MTU.
* **Epoch Batching**: Messages are released in discrete time-steps to prevent packet-for-packet correlation.

### 2.2 DC-Net Rooms (Untraceable Broadcast)

For ultra-sensitive groups (5–30 members), optional **DC-Net Rooms** provide sender-untraceability:

* **Dining Cryptographers Protocol**: Every round, members contribute randomized pads such that the XOR sum reveals a message without identifying the author.
* **Jamming Detection**: Fault-tolerant variants to prevent malicious members from disrupting the room.

### 2.3 Censorship Resistance & Traffic Hardening

* **Always-on Link**: High-risk profiles maintain a persistent background connection sending small cover packets.
* **Pluggable Transports**: OBFS4, Meek-style domain fronting, and CDN-mimicry to bypass state-level firewalls.

---

## 3. IDENTITY & CONTACT DISCOVERY

### 3.1 Private Set Intersection (PSI) Discovery

Users can find contacts without **ever** uploading their address book:

* **Zero-Knowledge Discovery**: Using PSI, the client and service learn only the intersection of their sets (local contacts vs. app users).
* **Local-Only Crypto**: Raw identifiers never leave the device; only blinded hashes are used in the protocol runtime.

### 3.2 Stable ID Elimination (SimpleX-style)

* **No Global Handles**: Phantom Net has no stable user ID. Each relationship is a pair of unlinked queues/tags.
* **Queue Rotation**: Users can rotate per-relationship routing addresses at any time, instantly breaking any correlation at the relay level.

---

## 4. STEALTH & DEVICE SECURITY

### 4.1 Plausible Deniability

* **Stealth Icon**: Option to disguise the app as a generic system utility (e.g., "System Config").
* **Deniable Profiles**: A "Cover Persona" (benign) protects a "Hidden Persona" (high-risk) accessible only via a secret gesture or secondary passphrase.
* **Panic Button**: Rapid-action trigger to wipe high-risk keys and message DB instantly.

### 4.2 Traffic Pattern Hardening

* **Randomized Batching**: Outgoing traffic is jittered and grouped to defeat per-device fingerprinters.
* **Uniform Push**: Polling mode or generic push topics to avoid leaking app usage via OS-level notifications.

---

## 5. INFRASTRUCTURE & ANTI-ABUSE

### 5.1 Anonymous Credits

Preventing spam without identity:

* **ZKTokens (PrivacyPass)**: Users obtain blinded tokens proving authorization to send. Relays verify tokens but cannot link messages to a specific user.
* **PoW Throttling**: Computational challenges required for mass-sending or creating new invitations.

### 5.2 Infrastructure Governance (No-Data Policy)

* **Multi-Operator Paths**: Clients are encouraged to build circuits across different countries/ISPs.
* **Aggregated Only**: Mixnet nodes log zero per-user data; only operational health metrics (bytes/latency) are stored.

---

## 6. ANON SANITY CHECKER

A built-in diagnostic tool to help users select the right protection level:

1. **Risk Assessment**: Guided questions about the user's adversary (Local Thief vs. State Surveillance).
2. **Configuration**: Automatically suggests the appropriate profile (Normal vs. High-Risk) and transport (Onion vs. Paranoia Mixnet).
3. **Visual Threat Map**: Simple diagrams explaining what metadata is currently exposed.

---

## 7. HANDOFF CONTRACTS (V1.2.0)

* **LAYOUT**: Design the **Stealth Persona switch**, the **PSI discovery flow**, and the **Anon Sanity Checker diagrams**.
* **ORCHESTRATION**: Define the **DC-Net XOR-sum protocol** and the **Blind Signature issuance service** APIs.
* **WORLD**: Architect the **Distributed PSI Directory** and the **Traffic Hardening test suite**.

---
*Authorized by the Phantom Net Architecture Squad.*
