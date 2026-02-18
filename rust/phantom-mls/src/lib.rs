use serde::{Deserialize, Serialize};
use std::collections::HashSet;
use uuid::Uuid;

/// Represents an epoch in the messaging layer security protocol.
#[derive(Debug, Clone, Serialize, Deserialize, PartialEq)]
pub struct MlsEpoch {
    pub id: u64,
    pub group_id: Uuid,
    pub membership_hash: Vec<u8>,
    pub timestamp: i64,
}

/// A simplified state machine for MLS Group management and Epoch synchronization.
pub struct MlsGroup {
    pub id: Uuid,
    pub name: String,
    pub current_epoch: u64,
    pub members: HashSet<Vec<u8>>, // Identifiers (e.g., relationship IDs)
}

impl MlsGroup {
    pub fn new(name: &str) -> Self {
        Self {
            id: Uuid::new_v4(),
            name: name.to_string(),
            current_epoch: 0,
            members: HashSet::new(),
        }
    }

    /// Advances the group to a new epoch.
    pub fn advance_epoch(&mut self) -> MlsEpoch {
        self.current_epoch += 1;

        MlsEpoch {
            id: self.current_epoch,
            group_id: self.id,
            membership_hash: self.calculate_membership_hash(),
            timestamp: chrono::Utc::now().timestamp(),
        }
    }

    pub fn add_member(&mut self, member_id: Vec<u8>) {
        self.members.insert(member_id);
    }

    pub fn remove_member(&mut self, member_id: &Vec<u8>) {
        self.members.remove(member_id);
    }

    fn calculate_membership_hash(&self) -> Vec<u8> {
        use sha2::{Digest, Sha256};
        let mut hasher = Sha256::new();
        let mut sorted_members: Vec<_> = self.members.iter().collect();
        sorted_members.sort(); // Sort to ensure deterministic hashing

        for member in sorted_members {
            hasher.update(member);
        }
        hasher.finalize().to_vec()
    }
}

#[cfg(test)]
mod tests {
    use super::*;

    #[test]
    fn test_mls_epoch_progression() {
        let mut group = MlsGroup::new("Secret Resistance Circle");

        group.add_member(vec![1, 2, 3]);
        let epoch_1 = group.advance_epoch();
        assert_eq!(epoch_1.id, 1);

        group.add_member(vec![4, 5, 6]);
        let epoch_2 = group.advance_epoch();
        assert_eq!(epoch_2.id, 2);
        assert_ne!(epoch_1.membership_hash, epoch_2.membership_hash);

        println!(
            "MLS Group '{}' advanced to Epoch {}",
            group.name, group.current_epoch
        );
    }
}
