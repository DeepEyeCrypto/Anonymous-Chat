use pqc_kyber::{decapsulate, encapsulate, keypair};
use zeroize::Zeroize;

/// Represents a Post-Quantum Public Key (Kyber-768).
pub struct PqcPublicKey(pub [u8; pqc_kyber::KYBER_PUBLICKEYBYTES]);

/// Represents a Post-Quantum Secret Key (Kyber-768).
#[derive(Zeroize)]
pub struct PqcSecretKey(pub [u8; pqc_kyber::KYBER_SECRETKEYBYTES]);

/// Generates a new Kyber-768 keypair.
pub fn generate_keypair() -> (PqcPublicKey, PqcSecretKey) {
    let mut rng = rand::thread_rng();
    let keys = keypair(&mut rng).expect("Kyber keygen failed");
    (PqcPublicKey(keys.public), PqcSecretKey(keys.secret))
}

/// Encapsulates a shared secret for a given public key.
/// Returns (SharedSecret, Ciphertext).
pub fn encapsulate_secret(pk: &PqcPublicKey) -> ([u8; 32], [u8; pqc_kyber::KYBER_CIPHERTEXTBYTES]) {
    let mut rng = rand::thread_rng();
    let (ct, ss) = encapsulate(&pk.0, &mut rng).expect("Kyber encapsulation failed");
    (ss, ct)
}

/// Decapsulates a shared secret from a ciphertext using a secret key.
pub fn decapsulate_secret(
    sk: &PqcSecretKey,
    ct: &[u8; pqc_kyber::KYBER_CIPHERTEXTBYTES],
) -> [u8; 32] {
    decapsulate(ct, &sk.0).expect("Kyber decapsulation failed")
}

#[cfg(test)]
mod tests {
    use super::*;

    #[test]
    fn test_kyber_flow() {
        let (pk, sk) = generate_keypair();
        let (ss1, ct) = encapsulate_secret(&pk);
        let ss2 = decapsulate_secret(&sk, &ct);
        assert_eq!(ss1, ss2);
    }
}
