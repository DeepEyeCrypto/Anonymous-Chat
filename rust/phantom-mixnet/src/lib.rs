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
#[derive(Debug, Clone)]
pub struct Packet {
    pub payload: Vec<u8>,
    pub mode: TransportMode,
}

/// The Orchestrator responsible for constant-throughput traffic injection.
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

    /// Starts the background loop that releases batches at fixed (jittered) intervals.
    pub async fn run(mut self) {
        let mut interval = interval(Duration::from_millis(self.interval_ms));
        let mut rng = rand::thread_rng();

        loop {
            interval.tick().await;

            // Apply jitter to prevent exact timing correlation
            let jitter: u64 = rng.gen_range(0..200);
            sleep(Duration::from_millis(jitter)).await;

            let mut batch = Vec::new();

            // Attempt to fill the batch from the incoming queue
            while batch.len() < self.batch_size {
                match self.incoming_rx.try_recv() {
                    Ok(packet) => batch.push(packet),
                    Err(_) => break, // Queue empty for now
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
        // Log the batch processing - in a real app, this would send to the network
        println!(
            "🚀 Dispatching batch of {} packets (Paranoia: {})",
            batch.len(),
            self.paranoia_enabled
        );
        for packet in batch {
            // Logic to send through the entry node or mix hop
            drop(packet);
        }
    }

    fn generate_dummy_packet() -> Packet {
        let mut rng = rand::thread_rng();
        let mut dummy_payload = vec![0u8; 1024]; // Standard MTU padding
        rng.fill(&mut dummy_payload[..]);

        Packet {
            payload: dummy_payload,
            mode: TransportMode::MixnetParanoia,
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
