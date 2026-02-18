# PHANTOM NET: TECHNICAL ORCHESTRATION

## Phase O: ORCHESTRATION (Contracts & API Synthesis)

> **Status**: ACTIVE (ORCHESTRATION PHASE START)  
> **Version**: 1.0.0  
> **Focus**: Defining the Protobuf schemas, FFI boundaries, and Protocol State Machines.

---

## 1. MESSAGE ENVELOPE (PROTOBUF)

All messages in Phantom Net, whether Onion or Mixnet, share a common packet structure to prevent fingerprinting.

```protobuf
syntax = "proto3";

package phantom.v1;

message PhantomPacket {
    // Shared fixed-size padding (MTU alignment)
    bytes padding = 1;
    
    oneof content {
        OnionLayer onion = 2;
        MixnetLayer mixnet = 3;
        DCNetRound dc_net = 4;
    }
}

message OnionLayer {
    bytes next_hop_pubkey = 1;
    bytes routing_token = 2;
    bytes encrypted_payload = 3; // Recursive OnionLayer or ApplicationPayload
}

message MixnetLayer {
    uint32 epoch_id = 1;
    bytes batch_signature = 2; // For verifiable shuffle
    bytes encrypted_payload = 3; 
}

message ApplicationPayload {
    bytes relationship_id = 1;
    bytes signal_ciphertext = 2; // X3DH + Double Ratchet + Kyber Hybrid
    uint64 timestamp = 3;
    MessageType type = 4;
}

enum MessageType {
    TEXT = 0;
    MEDIA = 1;
    GROUP_SYNC_MLS = 2;
    PSI_REQUEST = 3;
    HEARTBEAT_COVER = 4;
}
```

---

## 2. PQC-HYBRID JNI BRIDGE (RUST <-> KOTLIN)

The bridge between the Android UI and the Rust Crypto Core requires strictly defined FFI boundaries.

### 2.1 Identity & Session Management

```rust
// rust/phantom-core/src/ffi.rs

pub struct PqcIdentity {
    pub x25519_pub: [u8; 32],
    pub kyber_pub: [u8; 1184], // Kyber-768 public key
    pub dilithium_pub: Option<Vec<u8>>, // ML-DSA (optional experimental)
}

#[no_mangle]
pub extern "C" fn generate_hybrid_identity() -> *mut PqcIdentity;

#[no_mangle]
pub extern "C" fn encrypt_message_hybrid(
    recipient_identity: *const PqcIdentity,
    plaintext: *const u8,
    len: usize
) -> *mut u8;
```

---

## 3. PSI STATE MACHINE (CONTACT DISCOVERY)

To discover contacts without uploading the address book, we orchestrate the following state machine:

1. **INITIATE**: Client blinds local contact list using a per-session random scalar $r$.
2. **EXCHANGE**: Client sends blinded hashes to the Distributed PSI Directory.
3. **BLIND-SIGN**: Directory signs the hashes and returns them.
4. **UNBLIND**: Client removes the scalar $r$ and compares the result against its local pre-computed user database.
5. **MATCH**: Intersection is revealed ONLY to the client.

---

## 4. MLS EPOCH SYNCHRONIZATION

For group scalability, the `Orchestration` layer manages the "Epoch Clock":

* **Proposal**: A member proposes a member add/remove.
* **Commit**: The group root (or rotating leader) commits the change, advancing the `epoch_id`.
* **Rekey**: All members update their TreeKEM nodes.
* **Pruning**: Messages from `Epoch - 2` are purged from the device to maintain forward secrecy.

---

## 5. TRAFFIC ORCHESTRATOR (THE COVER ENGINE)

The background worker handles the "Paranoia" throughput:

* **Interval Pulse**: Every $T$ seconds (jittered), the orchestrator checks the `OutgoingQueue`.
* **Dummy Injection**: If `Queue.is_empty()`, generate a `MessageType::HEARTBEAT_COVER` packet.
* **Mixnet Release**: Batch $N$ packets into the current `Mixnet Epoch` for simultaneous dispatch.

---
*Next Step: Architect the WORLD phase (CI/CD, Service Node Staking, and Decentralized Discovery).*
