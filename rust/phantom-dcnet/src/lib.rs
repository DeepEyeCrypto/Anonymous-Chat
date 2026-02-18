use rand::Rng;
use std::collections::HashMap;

/// A participant in the DC-Net room.
pub struct DcNetParticipant {
    pub id: u32,
    /// Shared secrets with other participants. Key is common participant ID.
    shared_secrets: HashMap<u32, Vec<u8>>,
    message_size: usize,
}

impl DcNetParticipant {
    pub fn new(id: u32, message_size: usize) -> Self {
        Self {
            id,
            shared_secrets: HashMap::new(),
            message_size,
        }
    }

    /// Add a shared secret with another participant.
    /// In a real protocol, these would be generated via Diffie-Hellman.
    pub fn add_shared_secret(&mut self, other_id: u32, secret: Vec<u8>) {
        assert_eq!(secret.len(), self.message_size);
        self.shared_secrets.insert(other_id, secret);
    }

    /// Compute the local contribution for a round.
    /// If `message` is Some, this participant is the sender.
    pub fn compute_contribution(&self, message: Option<&[u8]>) -> Vec<u8> {
        let mut contribution = vec![0u8; self.message_size];

        // XOR all shared secrets
        for (&_other_id, secret) in &self.shared_secrets {
            for (c, s) in contribution.iter_mut().zip(secret.iter()) {
                *c ^= s;
            }
        }

        // If we are the sender, XOR the message in
        if let Some(msg) = message {
            assert_eq!(msg.len(), self.message_size);
            for (c, m) in contribution.iter_mut().zip(msg.iter()) {
                *c ^= m;
            }
        }

        contribution
    }
}

/// The DC-Net Room orchestrator (simulated or decentralized aggregator).
pub struct DcNetRoom {
    pub participants: Vec<u32>,
    pub message_size: usize,
}

impl DcNetRoom {
    pub fn new(message_size: usize) -> Self {
        Self {
            participants: Vec::new(),
            message_size,
        }
    }

    /// Aggregate all contributions to reveal the transmitted message.
    pub fn aggregate(&self, contributions: &[Vec<u8>]) -> Vec<u8> {
        let mut result = vec![0u8; self.message_size];
        for contribution in contributions {
            for (r, c) in result.iter_mut().zip(contribution.iter()) {
                *r ^= c;
            }
        }
        result
    }
}

#[cfg(test)]
mod tests {
    use super::*;

    #[test]
    fn test_dcnet_untraceable_broadcast() {
        let msg_size = 16;
        let mut p1 = DcNetParticipant::new(1, msg_size);
        let mut p2 = DcNetParticipant::new(2, msg_size);
        let mut p3 = DcNetParticipant::new(3, msg_size);

        // Simulate key exchange (DH)
        let mut rng = rand::thread_rng();
        let k12: Vec<u8> = (0..msg_size).map(|_| rng.gen()).collect();
        let k13: Vec<u8> = (0..msg_size).map(|_| rng.gen()).collect();
        let k23: Vec<u8> = (0..msg_size).map(|_| rng.gen()).collect();

        p1.add_shared_secret(2, k12.clone());
        p1.add_shared_secret(3, k13.clone());

        p2.add_shared_secret(1, k12.clone());
        p2.add_shared_secret(3, k23.clone());

        p3.add_shared_secret(1, k13.clone());
        p3.add_shared_secret(2, k23.clone());

        // Participant 2 wants to send a secret message
        let secret_message = b"HELLOPROTOCOL77";

        let c1 = p1.compute_contribution(None);
        let c2 = p2.compute_contribution(Some(secret_message));
        let c3 = p3.compute_contribution(None);

        let room = DcNetRoom::new(msg_size);
        let revealed = room.aggregate(&[c1, c2, c3]);

        assert_eq!(revealed, secret_message);
        println!(
            "DC-Net Revealed Message: {}",
            String::from_utf8_lossy(&revealed)
        );
    }
}
