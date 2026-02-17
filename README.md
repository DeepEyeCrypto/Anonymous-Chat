# PHANTOM NET: Decentralized Secure Messenger

**Mission**: Build a fully decentralized, censorship-resistant, end-to-end encrypted messenger.

## 🎯 Core Objectives

1. **Zero-Server Dependency**: DHT-based peer discovery.
2. **Military-Grade Encryption**: Signal Protocol.
3. **Censorship Resistance**: Tor onion routing (Arti).

## 🏗️ Architecture Overview

* **Layer 1**: Tor (Online), DHT-P2P (Fallback), Mesh (Offline).
* **Layer 2**: Custom Kademlia DHT.
* **Layer 3**: Signal Protocol E2EE.

## 🚀 Roadmap

- [x] **Phase 1: Orchestra**: Project Structure & Build System.
* [x] **Phase 2: Layout**: Android UI (Jetpack Compose).
* [x] **Phase 3: Logic**:
  * [x] Signal Protocol FFI (Rust -> Kotlin).
  * [x] Kademlia DHT Node (Rust -> Kotlin).
  * [x] E2EE Loopback Simulation.
* [x] **Phase 4: Network**:
  * [x] **Bootstrap**: Private Go Rendezvous Server.
  * [x] **Discovery**: DHT Auto-Bootstrap & Announcement.
  * [x] **Anonymity**: Tor Client (Arti) Integration.
  * [x] **Mesh**: Bluetooth LE Scanning (Stub/Simulation for MVP).
  * [ ] **Routing**: Tunneling DHT over Tor.

## 💻 Developer Setup

1. **Rust**: Install & `cargo build`.
2. **Android**: `export ANDROID_NDK_HOME=...` & `./scripts/build_android_core.sh`.
3. **Bootstrap**: `cd bootstrap && docker build -t bootstrap . && docker run -p 3000:3000 bootstrap`
4. **Run**: `./gradlew installDebug`.

_Powered by Antigravity_
