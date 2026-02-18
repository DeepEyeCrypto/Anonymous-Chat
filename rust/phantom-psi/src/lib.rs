use curve25519_dalek::ristretto::{CompressedRistretto, RistrettoPoint};
use curve25519_dalek::scalar::Scalar;
use rand::rngs::OsRng;
use sha2::{Digest, Sha512};

/// Represents a blinded contact identifier.
pub struct BlindedContact(pub RistrettoPoint);

/// The PSI Engine for client-side contact discovery.
pub struct PsiClient {
    secret_scalar: Scalar,
}

impl PsiClient {
    pub fn new() -> Self {
        Self {
            secret_scalar: Scalar::random(&mut OsRng),
        }
    }

    /// Step 1: Hash and Blind local contact identifiers.
    /// The input is a list of raw identifiers (e.g., phone numbers).
    pub fn blind_contacts(&self, identifiers: &[&str]) -> Vec<CompressedRistretto> {
        identifiers
            .iter()
            .map(|id| {
                let point = self.hash_to_point(id);
                let blinded = point * self.secret_scalar;
                blinded.compress()
            })
            .collect()
    }

    /// Step 3: Unblind the double-blinded points returned by the server.
    /// Resulting points can be used to check against the server's public set.
    pub fn unblind_points(
        &self,
        double_blinded: &[CompressedRistretto],
    ) -> Vec<CompressedRistretto> {
        let inv_scalar = self.secret_scalar.invert();
        double_blinded
            .iter()
            .map(|cp| {
                let p = cp.decompress().expect("Invalid point from server");
                let unblinded = p * inv_scalar;
                unblinded.compress()
            })
            .collect()
    }

    fn hash_to_point(&self, id: &str) -> RistrettoPoint {
        let mut hasher = Sha512::new();
        hasher.update(id.as_bytes());
        let result = hasher.finalize();
        RistrettoPoint::from_uniform_bytes(result.as_slice().try_into().unwrap())
    }
}

/// Simulated Directory Service for testing PSI.
pub struct PsiDirectory {
    secret_scalar: Scalar,
}

impl PsiDirectory {
    pub fn new() -> Self {
        Self {
            secret_scalar: Scalar::random(&mut OsRng),
        }
    }

    /// Simulated Step 2: Server further blinds the client's points.
    pub fn further_blind(
        &self,
        blinded_points: &[CompressedRistretto],
    ) -> Vec<CompressedRistretto> {
        blinded_points
            .iter()
            .map(|cp| {
                let p = cp.decompress().expect("Invalid point from client");
                let double_blinded = p * self.secret_scalar;
                double_blinded.compress()
            })
            .collect()
    }
}

#[cfg(test)]
mod tests {
    use super::*;

    #[test]
    fn test_psi_intersection_flow() {
        let client = PsiClient::new();
        let directory = PsiDirectory::new();

        let contacts = vec!["+1234567890", "+0987654321", "activist@phantom.net"];

        // 1. Client blinds contacts
        let blinded = client.blind_contacts(&contacts);

        // 2. Directory doubles-blinds
        let double_blinded = directory.further_blind(&blinded);

        // 3. Client unblinds
        let unblinded = client.unblind_points(&double_blinded);

        // Verification: Check if a known point matches
        let test_point_raw = client.hash_to_point("+1234567890");
        let expected_point = (test_point_raw * directory.secret_scalar).compress();

        assert_eq!(unblinded[0], expected_point);
        println!("PSI Match Verified: Identifier found without ever revealing raw string.");
    }
}
