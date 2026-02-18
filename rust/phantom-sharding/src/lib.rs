use gf256::gf256;
use rand::Rng;
use zeroize::Zeroize;

/// Represents a single shard of a secret.
#[derive(Debug, Clone, PartialEq, Eq)]
pub struct Shard {
    pub x: u8,
    pub y: Vec<u8>,
}

/// Shamir Secret Sharing implementation over GF(256).
pub struct ShardingManager;

impl ShardingManager {
    /// Splits a secret into N shards, requiring T to reconstruct.
    pub fn split(secret: &[u8], threshold: usize, total: usize) -> Vec<Shard> {
        assert!(
            threshold <= total,
            "Threshold cannot be greater than total shards"
        );
        assert!(threshold > 0, "Threshold must be at least 1");

        let mut shards = vec![vec![0u8; secret.len()]; total];
        let mut rng = rand::thread_rng();

        for (i, &byte) in secret.iter().enumerate() {
            // Create a polynomial f(x) = a0 + a1*x + ... + a{T-1}*x^{T-1}
            // where a0 is the secret byte.
            let mut poly = vec![gf256(0); threshold];
            poly[0] = gf256(byte);
            for j in 1..threshold {
                poly[j] = gf256(rng.gen());
            }

            // Evaluate the polynomial for x = 1, 2, ..., total
            for x in 1..=total {
                let mut val = gf256(0);
                let x_gf = gf256(x as u8);
                let mut x_pow = gf256(1);

                for a in &poly {
                    val += *a * x_pow;
                    x_pow *= x_gf;
                }
                shards[x - 1][i] = val.0;
            }
        }

        shards
            .into_iter()
            .enumerate()
            .map(|(i, y)| Shard {
                x: (i + 1) as u8,
                y,
            })
            .collect()
    }

    /// Reconstructs the secret from the given shards.
    pub fn reconstruct(shards: &[Shard]) -> Vec<u8> {
        assert!(!shards.is_empty(), "Need at least one shard to reconstruct");
        let len = shards[0].y.len();
        let mut secret = vec![0u8; len];

        for i in 0..len {
            let mut val = gf256(0);

            for shard_j in shards {
                let mut xj = gf256(shard_j.x);
                let mut yj = gf256(shard_j.y[i]);
                let mut li = gf256(1);

                for shard_m in shards {
                    if shard_j.x == shard_m.x {
                        continue;
                    }
                    let xm = gf256(shard_m.x);
                    // li = li * (x - xm) / (xj - xm) where x = 0 for reconstruction
                    li *= (gf256(0) - xm) / (xj - xm);
                }
                val += yj * li;
            }
            secret[i] = val.0;
        }

        secret
    }
}

#[cfg(test)]
mod tests {
    use super::*;

    #[test]
    fn test_sharding_integrity() {
        let secret = b"PHANTOM_ROOT_KEY_ED25519_SECRET_777";
        let threshold = 3;
        let total = 5;

        let shards = ShardingManager::split(secret, threshold, total);
        assert_eq!(shards.len(), total);

        // Reconstruct with exactly 3 shards
        let subset = &shards[0..3];
        let recovered = ShardingManager::reconstruct(subset);
        assert_eq!(secret.to_vec(), recovered);

        // Reconstruct with first, third, and fifth shard
        let jumping_subset = vec![shards[0].clone(), shards[2].clone(), shards[4].clone()];
        let recovered_jumping = ShardingManager::reconstruct(&jumping_subset);
        assert_eq!(secret.to_vec(), recovered_jumping);
    }
}
