# PHANTOM NET: Decentralized Secure Messenger

**Mission**: Build a fully decentralized, censorship-resistant, end-to-end encrypted messenger combining ProtonMail's zero-knowledge architecture, Tor's onion routing, Signal Protocol, and P2P mesh networking.

## 🎯 Core Objectives
1.  **Zero-Server Dependency**: DHT-based peer discovery. No central authority.
2.  **Military-Grade Encryption**: Signal Protocol (Double Ratchet + X3DH) + OpenPGP.
3.  **Censorship Resistance**: Tor onion routing + multi-transport support (I2P, Mesh).
4.  **Bulletproof Security**: Perfect Forward Secrecy (PFS) + Post-Compromise Security (PCS).

## 🏗️ Architecture Overview

The system is built on a hybrid architecture:

*   **Layer 1: Transport Abstraction**: Tor (Online), DHT-P2P (Fallback), Mesh (Offline/Bluetooth/WiFi).
*   **Layer 2: Peer Discovery**: Custom Kademlia DHT.
*   **Layer 3: E2EE**: Signal Protocol (libsignal-rust) + Sealed Sender.
*   **Layer 4: Onion Routing**: Tor Hidden Services Integration.
*   **Layer 5: Offline Mesh**: Briar-style store-and-forward DTN.

## 📦 Project Structure

```
phantom-net/
├── android/               # Android App (Kotlin + Jetpack Compose)
├── rust/                  # Core Logic (Signal, DHT, Tor in Rust)
├── bootstrap/             # Lightweight Bootstrap Node (Go)
├── docs/                  # Architecture & Protocol Specs
└── scripts/               # Dev & Build Scripts
```

## 🚀 Roadmap (MVP v0.1)
- [ ] 1:1 Messaging via DHT-P2P
- [ ] Signal Protocol E2EE (X3DH + Double Ratchet)
- [ ] Contact Exchange via QR Code
- [ ] Local Encrypted Storage (SQLCipher)
- [ ] Basic DHT Implementation

_Powered by Antigravity & User Collaboration_
