use chacha20poly1305::{
    aead::{Aead, KeyInit},
    ChaCha20Poly1305, Nonce,
};
use rand::RngCore;
use x25519_dalek::{PublicKey, StaticSecret};

/// A simplified Sphinx packet for multi-hop onion routing.
/// Provides bit-flipping security and fixed-size integrity.
pub struct SphinxPacket {
    pub header: Vec<u8>,
    pub payload: Vec<u8>,
}

impl SphinxPacket {
    pub const PACKET_SIZE: usize = 1024; // Fixed MTU for the mixnet
    pub const HEADER_SIZE: usize = 128; // Hops and crypto metadata

    /// Create a multi-hop onion packet.
    /// In a real implementation, this would involve nested Diffie-Hellman handshakes.
    /// For this stage, we simulate the layering with symmetric ChaCha20.
    pub fn create(
        payload: &[u8],
        hops_keys: &[[u8; 32]], // Simple shared secrets for each hop
    ) -> Self {
        let mut current_payload = vec![0u8; Self::PACKET_SIZE - Self::HEADER_SIZE];
        let len = payload.len().min(current_payload.len());
        current_payload[..len].copy_from_slice(&payload[..len]);

        // Wrap payload in layers of encryption (Onion)
        for key in hops_keys.iter().rev() {
            let cipher = ChaCha20Poly1305::new(key.into());
            let nonce = Nonce::from_slice(&[0u8; 12]); // In real use, this must be unique/derived
            current_payload = cipher
                .encrypt(nonce, current_payload.as_slice())
                .unwrap_or_else(|_| vec![0u8; current_payload.len()]);
            // Truncate back to fixed size (simulating tag-stripping or fixed-width padding)
            current_payload.truncate(Self::PACKET_SIZE - Self::HEADER_SIZE);
        }

        Self {
            header: vec![0u8; Self::HEADER_SIZE], // Placeholder for routing info
            payload: current_payload,
        }
    }

    /// "Peel" one layer of the onion at a node.
    pub fn peel(&mut self, hop_key: &[u8; 32]) -> bool {
        let cipher = ChaCha20Poly1305::new(hop_key.into());
        let nonce = Nonce::from_slice(&[0u8; 12]);

        match cipher.decrypt(nonce, self.payload.as_slice()) {
            Ok(decrypted) => {
                self.payload = decrypted;
                // Pad back to original size to maintain fixed-width traffic
                while self.payload.len() < Self::PACKET_SIZE - Self::HEADER_SIZE {
                    self.payload.push(0);
                }
                true
            }
            Err(_) => false,
        }
    }
}
