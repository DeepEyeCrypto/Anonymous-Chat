use serde::{Deserialize, Serialize};

#[derive(Debug, Clone, Copy, Serialize, Deserialize, PartialEq)]
pub enum Adversary {
    LocalThief,
    NetworkMonitor, // ISP / School / Work
    GlobalNationState,
}

#[derive(Debug, Clone, Serialize, Deserialize)]
pub struct PrivacyConfig {
    pub use_mixnet: bool,
    pub use_psi: bool,
    pub is_sharded: bool,
    pub cover_traffic_enabled: bool,
    pub selected_adversary: Adversary,
}

#[derive(Debug, Clone, Serialize, Deserialize)]
pub struct AuditReport {
    pub risk_score: u32, // 0-100 (higher is safer)
    pub recommendations: Vec<String>,
    pub status_color: String,
}

pub struct PrivacyAuditor;

impl PrivacyAuditor {
    pub fn audit(config: &PrivacyConfig) -> AuditReport {
        let mut score: u32 = 50; // Starting baseline
        let mut recommendations = Vec::new();

        // 1. Evaluate Transport
        if config.use_mixnet {
            score += 20;
            if config.cover_traffic_enabled {
                score += 10;
            } else {
                recommendations.push("Enable Cover Traffic to defeat timing attacks.".to_string());
            }
        } else {
            recommendations.push(
                "Switch to Mixnet for metadata resistance against passive observers.".to_string(),
            );
        }

        // 2. Evaluate Discovery
        if config.use_psi {
            score += 10;
        } else {
            recommendations.push("Use PSI discovery to prevent address book exposure.".to_string());
        }

        // 3. Evaluate Resilience
        if config.is_sharded {
            score += 10;
        } else {
            recommendations
                .push("Shard your identity to prevent single-point device compromise.".to_string());
        }

        // 4. Evaluate against selected Adversary
        match config.selected_adversary {
            Adversary::GlobalNationState => {
                if !config.use_mixnet || !config.cover_traffic_enabled {
                    score = score.saturating_sub(30);
                    recommendations.push("CRITICAL: Onion routing is insufficient against a Global Nation State. Use Paranoia Mixnet.".to_string());
                }
            }
            Adversary::NetworkMonitor => {
                if !config.use_mixnet {
                    score = score.saturating_sub(10);
                }
            }
            Adversary::LocalThief => {
                // Metadata matters less here, but sharding matters more (handled above)
            }
        }

        let clamped_score = score.clamp(0, 100);
        let status_color = match clamped_score {
            0..=40 => "#FF5252",  // Red
            41..=75 => "#FFD600", // Yellow
            _ => "#00E676",       // Green
        };

        AuditReport {
            risk_score: clamped_score,
            recommendations,
            status_color: status_color.to_string(),
        }
    }
}

#[cfg(test)]
mod tests {
    use super::*;

    #[test]
    fn test_audit_high_risk() {
        let config = PrivacyConfig {
            use_mixnet: false,
            use_psi: false,
            is_sharded: false,
            cover_traffic_enabled: false,
            selected_adversary: Adversary::GlobalNationState,
        };
        let report = PrivacyAuditor::audit(&config);
        assert!(report.risk_score < 40);
        assert!(report.recommendations.len() >= 3);
    }

    #[test]
    fn test_audit_paranoia_gold() {
        let config = PrivacyConfig {
            use_mixnet: true,
            use_psi: true,
            is_sharded: true,
            cover_traffic_enabled: true,
            selected_adversary: Adversary::GlobalNationState,
        };
        let report = PrivacyAuditor::audit(&config);
        assert_eq!(report.risk_score, 100);
    }
}
