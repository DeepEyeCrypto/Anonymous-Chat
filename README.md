# 🛡️ PHANTOM NET

> **"Messages without Metadata"**
>
> A decentralized, mixnet-powered, post-quantum resilient messenger designed for plausible deniability and extreme anonymity.

---

## 🌌 The Vision: Absolute Metadata Erasure

Phantom Net is the ultimate communication toolkit for high-risk individuals. We go beyond encryption to erase the fingerprints of your digital presence. By combining **DC-Nets**, **Mixnets**, and **Private Set Intersection (PSI)**, Phantom Net ensures that your social graph and communication patterns are a black hole to any adversary.

### 🎭 Core Pillars

* **Paranoia Transport**: Mixnets with mandatory cover traffic and DC-Net rooms for untraceable group broadcasts.
* **Contact Discovery with 0 Leak**: Find friends via **PSI** without ever uploading your address book.
* **Plausible Deniability**: Stealth icons, hidden personas, and panic-wiping for local safety.
* **Zero Stable Identifiers**: Every relationship is isolated and rotatable; no global phone numbers or handles.

---

## 🏗️ Technical Architecture (Tier 2)

1. **Discovery**: Private Set Intersection (PSI) for local-only contact mapping.
2. **Transport**: Adaptive Mixnet (Stealth) + DC-Net (Untraceable) + Onion (Fast).
3. **Identity**: No Global ID; Rotating per-relationship queues.
4. **Anti-Abuse**: Anonymous ZK-Tokens for rate-limiting without accounts.
5. **Local Hardening**: Deniable personas + Panic buttons + Traffic jittering.

---

## 🚀 F.L.O.W. Implementation Status

| Phase | Description | Status |
| :--- | :--- | :--- |
| **F**rame | Architecture & Extreme Anonymity Module | 🟢 **Locked (V1.2)** |
| **F-Call** | Audio/Video Calling FRAME | 🟢 **Locked (V1.0)** |
| **F-Stealth** | Stealth Icon & App Disguise FRAME | 🟢 **Locked (V1.0)** |
| **L**ayout | Advanced UI & Stealth Persona Flows + Calling Flows | 🟡 *In Progress* |
| **O**rchestration | DC-Net, PSI & Calling Proto Contracts | 🟡 *In Progress* |
| **W**orld | Distributed PSI Directory | ⚪ *Planned* |

---

## 📋 Privacy Benchmark

| Feature | Phantom Net | Signal | SimpleX |
| :--- | :--- | :--- | :--- |
| **Contact Discovery** | **PSI (No Upload)** | Hashed Upload | None/Manual |
| **Metadata Protection** | **Mixnet + DC-Net** | Sealed Sender | Onion-only |
| **Persistent ID?** | **No** | Phone/Username | No |
| **Cover Traffic** | **Always-On Opt-in** | No | No |
| **Local Secrecy** | **Deniable Personas** | Standard Pin | Standard Pin |

---

## 💻 Developer Quickstart

```bash
# High-Risk Profile Build
./scripts/build_android_core.sh --profile paranoia

# Run DC-Net Simulator
cargo run --package dc-net-engine --example whistleblower_board
```

Complete specifications: [docs/PROTOCOL.md](./docs/PROTOCOL.md), [docs/CALLING_FRAME.md](./docs/CALLING_FRAME.md), [docs/CALLING_LAYOUT.md](./docs/CALLING_LAYOUT.md), and [docs/STEALTH_FRAME.md](./docs/STEALTH_FRAME.md).

---
*Built for the whistleblower, the journalist, and the advocate.*
