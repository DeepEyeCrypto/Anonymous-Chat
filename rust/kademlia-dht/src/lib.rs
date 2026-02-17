use futures::StreamExt;
use jni::objects::{JClass, JString};
use jni::sys::jstring;
use jni::JNIEnv;
use libp2p::noise;
use libp2p::tcp;
use libp2p::yamux;
use libp2p::{
    identity,
    kad::{self, store::MemoryStore},
    swarm::{NetworkBehaviour, SwarmEvent},
    Multiaddr, PeerId, Swarm,
};
use serde::{Deserialize, Serialize};
use std::error::Error;
use std::str::FromStr;
use std::time::Duration;
use tokio::runtime::Runtime;
use tokio::sync::mpsc;

use std::sync::OnceLock;

// Global Runtime & Command Channel
static RUNTIME: OnceLock<Runtime> = OnceLock::new();
static COMMAND_SENDER: OnceLock<mpsc::Sender<DhtCommand>> = OnceLock::new();

enum DhtCommand {
    AddBootstrapNode { peer_id: PeerId, address: Multiaddr },
    Bootstrap,
    AnnounceToHttp { bootstrap_url: String },
}

#[derive(Serialize, Deserialize, Debug)]
struct PeerInfo {
    peer_id: String,
    address: String,
}

#[derive(NetworkBehaviour)]
struct PhantomBehaviour {
    kademlia: kad::Behaviour<MemoryStore>,
}

struct DhtNode {
    swarm: Swarm<PhantomBehaviour>,
    command_receiver: mpsc::Receiver<DhtCommand>,
    local_peer_id: PeerId,
}

impl DhtNode {
    async fn new(
        receiver: mpsc::Receiver<DhtCommand>,
    ) -> Result<Self, Box<dyn Error + Send + Sync>> {
        let local_key = identity::Keypair::generate_ed25519();
        let local_peer_id = PeerId::from(local_key.public());

        let swarm = libp2p::SwarmBuilder::with_existing_identity(local_key)
            .with_tokio()
            .with_tcp(
                tcp::Config::default(),
                noise::Config::new,
                yamux::Config::default,
            )?
            .with_behaviour(|_| {
                let store = MemoryStore::new(local_peer_id);
                let mut kad_config = kad::Config::default();
                kad_config.set_query_timeout(Duration::from_secs(60));
                PhantomBehaviour {
                    kademlia: kad::Behaviour::with_config(local_peer_id, store, kad_config),
                }
            })?
            .build();

        Ok(Self {
            swarm,
            command_receiver: receiver,
            local_peer_id,
        })
    }

    async fn run(self) -> Result<(), Box<dyn Error + Send + Sync>> {
        let mut swarm = self.swarm;
        let mut command_receiver = self.command_receiver;
        let local_pid = self.local_peer_id;

        // Listen on random port
        swarm.listen_on("/ip4/0.0.0.0/tcp/0".parse()?)?;

        let mut current_addr: Option<Multiaddr> = None;

        loop {
            tokio::select! {
                event = swarm.select_next_some() => match event {
                    SwarmEvent::NewListenAddr { address, .. } => {
                        println!("DHT Node listening on {:?}", address);
                         // Keep track of our public address (simple heuristic)
                        if !address.to_string().contains("127.0.0.1") {
                             current_addr = Some(address.clone());
                        } else if current_addr.is_none() {
                             current_addr = Some(address.clone());
                        }
                    }
                    SwarmEvent::Behaviour(PhantomBehaviourEvent::Kademlia(_)) => {
                         // discard Kademlia events for now
                    }
                    _ => {}
                },
                command_opt = command_receiver.recv() => match command_opt {
                    Some(DhtCommand::AddBootstrapNode { peer_id, address }) => {
                        let _ = swarm.behaviour_mut().kademlia.add_address(&peer_id, address);
                    }
                    Some(DhtCommand::Bootstrap) => {
                         let _ = swarm.behaviour_mut().kademlia.bootstrap();
                    }
                    Some(DhtCommand::AnnounceToHttp { bootstrap_url }) => {
                        if let Some(addr) = &current_addr {
                            let pid_str = local_pid.to_string();
                            let addr_str = addr.to_string();

                            // Clone values for async move block
                            let url_clone = bootstrap_url.clone();

                            // Spawn separate task for HTTP request
                            tokio::spawn(async move {
                                let client = reqwest::Client::new();
                                let peer = PeerInfo { peer_id: pid_str, address: addr_str };
                                let _ = client.post(format!("{}/announce", url_clone))
                                    .json(&peer)
                                    .send()
                                    .await;
                            });
                        }
                    }
                    None => break, // Channel closed
                }
            }
        }
        Ok(())
    }
}

// ... JNI Definitions ...
#[no_mangle]
pub extern "system" fn Java_com_phantomnet_core_network_DhtService_startDhtNode(
    env: JNIEnv,
    _class: JClass,
) -> jstring {
    if RUNTIME.get().is_none() {
        let runtime = Runtime::new().expect("Failed to create Tokio runtime");

        let (tx, rx) = mpsc::channel(32);
        let _ = COMMAND_SENDER.set(tx);

        runtime.spawn(async move {
            match DhtNode::new(rx).await {
                Ok(node) => {
                    if let Err(e) = node.run().await {
                        eprintln!("DHT Node crashed: {:?}", e);
                    }
                }
                Err(e) => eprintln!("Failed to start DHT Node: {:?}", e),
            }
        });

        let _ = RUNTIME.set(runtime);
        env.new_string("DHT Node Started").unwrap().into_raw()
    } else {
        env.new_string("DHT Node Already Running")
            .unwrap()
            .into_raw()
    }
}

// ... Other JNI functions ...
#[no_mangle]
pub extern "system" fn Java_com_phantomnet_core_network_DhtService_addBootstrapNode(
    mut env: JNIEnv,
    _class: JClass,
    peer_id_str: JString,
    address_str: JString,
) {
    let peer_id_string: String = match env.get_string(&peer_id_str) {
        Ok(s) => s.into(),
        Err(_) => return,
    };
    let address_string: String = match env.get_string(&address_str) {
        Ok(s) => s.into(),
        Err(_) => return,
    };

    if let Some(tx) = COMMAND_SENDER.get() {
        if let (Ok(peer_id), Ok(address)) = (
            PeerId::from_str(&peer_id_string),
            Multiaddr::from_str(&address_string),
        ) {
            // Try blocking send, if not full
            let _ = tx.blocking_send(DhtCommand::AddBootstrapNode { peer_id, address });
            let _ = tx.blocking_send(DhtCommand::Bootstrap);
        }
    }
}

#[no_mangle]
pub extern "system" fn Java_com_phantomnet_core_network_DhtService_announceToBootstrap(
    mut env: JNIEnv,
    _class: JClass,
    url_str: JString,
) {
    let url_string: String = match env.get_string(&url_str) {
        Ok(s) => s.into(),
        Err(_) => return,
    };

    if let Some(tx) = COMMAND_SENDER.get() {
        let _ = tx.blocking_send(DhtCommand::AnnounceToHttp {
            bootstrap_url: url_string,
        });
    }
}
