# PHANTOM NET PROTOCOL SPECIFICATION

## 📋 Phase F - FRAME: Strategic Operating Procedure (SOP)

### 🎯 Core Mission Objectives

1. **ZERO-SERVER DEPENDENCY**: Messages route peer-to-peer via DHT.
2. **MILITARY-GRADE ENCRYPTION**: Signal Protocol (Double Ratchet + X3DH).
3. **CENSORSHIP-RESISTANCE**: Onion routing, DHT-based discovery.
4. **BULLETPROOF SECURITY MODEL**: PFS, PCS, Sealed Sender.

### 🧬 Hybrid Architecture Design

#### Layer 1: Transport Abstraction

* **Tor Transport (Primary)**: 3-hop onion circuits, .onion hidden services.
* **DHT-P2P Transport (Fallback)**: Direct encrypted P2P via Kademlia.
* **Mesh Transport (Offline)**: Bluetooth LE mesh, WiFi Direct.

#### Layer 2: Peer Discovery & Routing

* **Kademlia DHT**: NodeID = SHA256(PublicKey).
* **Routing**: XOR-based distance metric.
* **Anti-Sybil**: PoW on NodeID generation.

#### Layer 3: End-to-End Encryption

* **Signal Protocol**: X3DH Handshake for session init.
* **Double Ratchet**: Per-message key derivation.
* **Metadata Protection**: Sealed Sender (server sees only ciphertext).

#### Layer 4: Onion Routing Integration

* **Circuit**: Guard -> Middle -> Exit.
* **Hidden Services**: Rendezvous points for anonymous communication.

#### Layer 5: Offline Mesh Networking

* **Briar-style DTN**: Store-and-forward.
* **Discovery**: BLE scanning, WiFi P2P.

### 🛠️ Tech Stack

* **Android**: Kotlin, Jetpack Compose, SQLCipher.
* **Core (Rust)**: libsignal-ffi, kademlia-dht, tor-client.
* **Bootstrap**: Go, Fiber, Redis.

### 🔐 Security Threat Model

* **Passive Surveillance**: Mitigated by Tor & Sealed Sender.
* **Active MITM**: Mitigated by E2EE & SAS/QR verification.
* **Compromised Device**: Mitigated by DB encryption & Screen lock.
* **Quantum Future**: Mitigated by PFS/PCS (Signal).

### 📊 Success Metrics (MVP)

* Alice sends text to Bob via DHT-P2P.
* <5 seconds delivery.
* No central server.
* Encrypted at rest and in transit.

---
*Generated from GOD PROMPT v2.0*
