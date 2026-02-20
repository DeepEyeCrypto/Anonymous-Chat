pub mod sphinx;

use crate::sphinx::SphinxPacket;
use rand::Rng;
use std::time::Duration;
use tokio::sync::mpsc;
use tokio::time::{interval, sleep};

/// Represents the transport mode for a packet.
#[derive(Debug, Clone, Copy, PartialEq)]
pub enum TransportMode {
    DirectOnion,
    MixnetParanoia,
}

/// A generic packet that the mixnet will process.
#[derive(Clone)]
pub struct Packet {
    pub payload: Vec<u8>,
    pub mode: TransportMode,
    pub hops: usize, // Simulation: number of remaining hops
}

/// The MixnetDispatcher orchestrates constant-throughput traffic.
/// Release batches at fixed (jittered) intervals to frustrate timing analysis.
pub struct MixnetDispatcher {
    incoming_rx: mpsc::Receiver<Packet>,
    interval_ms: u64,
    batch_size: usize,
    paranoia_enabled: bool,
}

impl MixnetDispatcher {
    pub fn new(
        incoming_rx: mpsc::Receiver<Packet>,
        interval_ms: u64,
        batch_size: usize,
        paranoia_enabled: bool,
    ) -> Self {
        Self {
            incoming_rx,
            interval_ms,
            batch_size,
            paranoia_enabled,
        }
    }

    pub async fn run(mut self) {
        let mut interval = interval(Duration::from_millis(self.interval_ms));

        log::info!(
            "🌀 Mixnet Dispatcher (Multi-Hop) started. Interval: {}ms, Batch: {}, Paranoia: {}",
            self.interval_ms,
            self.batch_size,
            self.paranoia_enabled
        );

        loop {
            interval.tick().await;

            // Apply jitter to prevent exact timing correlation
            let jitter: u64 = rand::thread_rng().gen_range(0..200);
            sleep(Duration::from_millis(jitter)).await;

            let mut batch = Vec::new();

            // Attempt to fill the batch from the incoming queue
            while batch.len() < self.batch_size {
                match self.incoming_rx.try_recv() {
                    Ok(packet) => batch.push(packet),
                    Err(_) => break,
                }
            }

            // In Paranoia Mode, if the batch is not full, we inject dummy (cover) traffic
            if self.paranoia_enabled {
                while batch.len() < self.batch_size {
                    batch.push(Self::generate_dummy_packet());
                }
            }

            if !batch.is_empty() {
                self.dispatch_batch(batch).await;
            }
        }
    }

    async fn dispatch_batch(&self, batch: Vec<Packet>) {
        let real_count = batch
            .iter()
            .filter(|p| p.mode != TransportMode::MixnetParanoia)
            .count();

        log::debug!(
            "🚀 Multi-Hop Batch Dispatched: {} Sphinx packets (Real: {})",
            batch.len(),
            real_count
        );

        for mut packet in batch {
            // Simulate multi-hop routing
            if packet.hops > 0 {
                // In a real network, we'd send to the next relay.
                // Here we just decrement hops to simulate the relay chain.
                packet.hops -= 1;
                // Peel layer simulation (Simplified)
                let dummy_key = [0u8; 32];
                let mut sphinx = SphinxPacket {
                    header: vec![0u8; SphinxPacket::HEADER_SIZE],
                    payload: packet.payload.clone(),
                };
                sphinx.peel(&dummy_key);
                packet.payload = sphinx.payload;
            }

            // Log final delivery or drop if still in transit
            if packet.hops == 0 && packet.mode != TransportMode::MixnetParanoia {
                log::trace!("🏁 Final Sphinx delivery node reached.");
            }
        }
    }

    fn generate_dummy_packet() -> Packet {
        let mut rng = rand::thread_rng();
        // Generate a cryptographically random payload indistinguishable from Sphinx
        let mut dummy_payload = vec![0u8; SphinxPacket::PACKET_SIZE - SphinxPacket::HEADER_SIZE];
        rng.fill(&mut dummy_payload[..]);

        Packet {
            payload: dummy_payload,
            mode: TransportMode::MixnetParanoia,
            hops: rng.gen_range(3..7), // Random hops for cover traffic to simulate network spread
        }
    }
}

#[cfg(test)]
mod tests {
    use super::*;
    use tokio::sync::mpsc;

    #[tokio::test]
    async fn test_dispatcher_paranoia() {
        let (_tx, rx) = mpsc::channel(10);
        let dispatcher = MixnetDispatcher::new(rx, 500, 5, true);

        // Run for a short duration to see if it generates dummy batches
        tokio::select! {
            _ = dispatcher.run() => {},
            _ = sleep(Duration::from_secs(2)) => {
                println!("Test duration reached.");
            }
        }
    }
}
