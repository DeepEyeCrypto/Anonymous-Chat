# PHANTOM NET: GLOBAL ECOSYSTEM & OPS

## Phase W: WORLD (Infrastructure, Deployment & Distribution)

> **Status**: ACTIVE (WORLD PHASE - FINAL FRAMEWORK COMPLETION)  
> **Version**: 1.0.0  
> **Focus**: CI/CD, Decentralized Distribution, and Service Node Sustainability.

---

## 1. CI/CD: THE COMPILING FORTRESS

To maintain cryptographic integrity across updates, the build system must be reproducible and resilient.

### 1.1 Multi-Target Rust Pipeline (GitHub Actions)

Builds for Android (ARM64, x86_64) and Desktop (macOS, Linux) targets simultaneously.

* **Security Audit Step**: Automated `cargo audit` and `cargo deny` on every PR.
* **Reproducible Builds**: Use of deterministic toolchains to ensure the final APK/Binary matches the source exactly.
* **Release Signing**: Keys stored in hardware-backed GitHub Environments.

### 1.2 Binary Integrity

* **Artifact Attestation**: Signed SLSA provenance for all released binaries.
* **In-App Verification**: Client periodically checks its own hash against the decentralized registry.

---

## 2. DECENTRALIZED DISTRIBUTION

Phantom Net cannot rely on a single central app store.

* **F-Droid Repository**: Primary source for the privacy community.
* **Distributed APK Sharing**: Built-in "Relay the App" feature using Bluetooth/Wi-Fi Mesh to share the installer in internet-dark environments.
* **Onion-Mirrors**: Host installers on .onion hidden services to bypass state-level blocking of domain-fronting.

---

## 3. SERVICE NODE ECOSYSTEM (THE INFRASTRUCTURE)

The network is powered by independently operated "Service Nodes."

### 3.1 Incentives & Staking

* **Community Contribution**: Purely voluntary/donation-based initially (FOSS model).
* **DAO Governance**: Future transition to a decentralized autonomous organization to managed relay reputation.
* **Operator Tools**: One-command Docker deployment (`docker-compose up -d phantom-node`).

### 3.2 Privacy-Safe Observability

Monitoring the network without monitoring the users:

* **Aggregated Metrics**: Nodes report global bandwidth and latency only.
* **Zero-Knowledge Heartbeats**: Nodes prove they are online and following the TTL/Purge policies via anonymous proofs of uptime.

---

## 4. CENSORSHIP RESILIENCE DYNAMICS

* **Dynamic Bootstrap**: If initial bootstrap nodes are blocked, use DHT-based rendezvous or "Friend-to-Friend" bootstrap.
* **Traffic Obfuscation**: Automatic rotation of pluggable transports based on network conditions (e.g., switching to Meek/Azure if OBFS4 is throttled).

---

## 5. FINAL MISSION READINESS (FLOW SUMMARY)

| Phase | Delivered Artifacts | Final Status |
| :--- | :--- | :--- |
| **F**RAME | [PROTOCOL.md](./docs/PROTOCOL.md) | 🟢 **LOCKED** (V1.2.0) |
| **L**AYOUT | [LAYOUT.md](./docs/LAYOUT.md) + Mockups | 🟢 **LOCKED** |
| **O**RCHESTRATION | [ORCHESTRATION.md](./docs/ORCHESTRATION.md) + Proto | 🟢 **LOCKED** |
| **W**ORLD | [WORLD.md](./docs/WORLD.md) + CI Strategy | 🟢 **LOCKED** |

---

## 🏁 Handoff to IMPLEMENTATION

The **Architectural Framework** for Phantom Net is now complete. We have moved from a "God Prompt" concept to a fully realized technical blueprint.

**The protocol is ready for coding.**

*Authorized by the Phantom Net Architecture Squad.*
