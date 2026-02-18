use serde::{Deserialize, Serialize};

#[derive(Debug, Clone, Serialize, Deserialize)]
pub enum SentinelEvent {
    FailedLoginAttempt,
    UnauthorizedDbAccess,
    PanicGestureTriggered,
    ForensicExtractionDetected,
}

#[derive(Debug, Clone, Serialize, Deserialize)]
pub struct SentinelPolicy {
    pub max_failed_logins: u32,
    pub auto_wipe_on_forensics: bool,
    pub shred_keys_on_panic: bool,
}

#[derive(Debug, Clone, Serialize, Deserialize)]
pub struct SentinelStatus {
    pub is_active: bool,
    pub last_event: Option<SentinelEvent>,
    pub threat_level: u32, // 0-10
}

pub struct LocalSentinel {
    policy: SentinelPolicy,
    failed_login_count: u32,
}

impl LocalSentinel {
    pub fn new(policy: SentinelPolicy) -> Self {
        Self {
            policy,
            failed_login_count: 0,
        }
    }

    /// Processes a security event and returns whether a 'WIPE' is recommended.
    pub fn process_event(&mut self, event: SentinelEvent) -> bool {
        match event {
            SentinelEvent::FailedLoginAttempt => {
                self.failed_login_count += 1;
                self.failed_login_count >= self.policy.max_failed_logins
            }
            SentinelEvent::UnauthorizedDbAccess => self.policy.auto_wipe_on_forensics,
            SentinelEvent::PanicGestureTriggered => self.policy.shred_keys_on_panic,
            SentinelEvent::ForensicExtractionDetected => true, // Always wipe on forensics
        }
    }

    /// "Shreds" keys by overwriting them with random data (Simulated).
    pub fn shred_identity(&self) {
        log::warn!("🚀 SENTINEL: SHREDDING IDENTITY KEYS IMMEDIATELY.");
        // In a real implementation, this would zeroize the memory buffers
        // and delete key files from the Secure Enclave / Keystore.
    }
}

#[cfg(test)]
mod tests {
    use super::*;

    #[test]
    fn test_sentinel_auto_wipe() {
        let policy = SentinelPolicy {
            max_failed_logins: 3,
            auto_wipe_on_forensics: true,
            shred_keys_on_panic: true,
        };
        let mut sentinel = LocalSentinel::new(policy);

        assert!(!sentinel.process_event(SentinelEvent::FailedLoginAttempt));
        assert!(!sentinel.process_event(SentinelEvent::FailedLoginAttempt));
        assert!(sentinel.process_event(SentinelEvent::FailedLoginAttempt)); // 3rd attempt
    }
}
