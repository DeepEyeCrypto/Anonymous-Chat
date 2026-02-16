use futures::StreamExt;
use jni::JNIEnv;
use jni::objects::{JClass, JString};
use jni::sys::jstring;
use libp2p::core::upgrade::Version;
use libp2p::noise;
use libp2p::tcp;
use libp2p::yamux;
use libp2p::{
    Multiaddr, PeerId, Swarm, Transport, identity,
    kad::{Kademlia, KademliaConfig, KademliaEvent, store::MemoryStore},
    swarm::{NetworkBehaviour, SwarmEvent},
};
use serde::{Deserialize, Serialize};
use std::error::Error;
use std::str::FromStr;
use std::time::Duration;
use tokio::runtime::Runtime;
use tokio::sync::mpsc;

// Global Runtime & Command Channel
static mut RUNTIME: Option<Runtime> = None;
static mut COMMAND_SENDER: Option<mpsc::Sender<DhtCommand>> = None;

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
    kademlia: Kademlia<MemoryStore>,
}

pub struct DhtNode {
    swarm: Swarm<PhantomBehaviour>,
    command_receiver: mpsc::Receiver<DhtCommand>,
    local_peer_id: PeerId,
}

impl DhtNode {
    pub async fn new(receiver: mpsc::Receiver<DhtCommand>) -> Result<Self, Box<dyn Error>> {
        let local_key = identity::Keypair::generate_ed25519();
        let local_peer_id = PeerId::from(local_key.public());

        let transport = tcp::tokio::Transport::new(tcp::Config::default().nodelay(true))
            .upgrade(Version::V1)
            .authenticate(noise::Config::new(&local_key)?)
            .multiplex(yamux::Config::default())
            .boxed();

        let store = MemoryStore::new(local_peer_id);
        let mut kad_config = KademliaConfig::default();
        kad_config.set_query_timeout(Duration::from_secs(60));
        let kademlia = Kademlia::with_config(local_peer_id, store, kad_config);

        let behaviour = PhantomBehaviour { kademlia };
        let swarm = Swarm::with_tokio_executor(transport, behaviour, local_peer_id);

        Ok(Self {
            swarm,
            command_receiver: receiver,
            local_peer_id,
        })
    }

    pub async fn run(mut self) -> Result<(), Box<dyn Error>> {
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
    mut env: JNIEnv,
    _class: JClass,
) -> jstring {
    unsafe {
        if RUNTIME.is_none() {
            let runtime = Runtime::new().expect("Failed to create Tokio runtime");

            let (tx, rx) = mpsc::channel(32);
            COMMAND_SENDER = Some(tx);

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

            RUNTIME = Some(runtime);
            return env.new_string("DHT Node Started").unwrap().into_raw();
        } else {
            return env
                .new_string("DHT Node Already Running")
                .unwrap()
                .into_raw();
        }
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

    unsafe {
        if let Some(tx) = &COMMAND_SENDER {
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

    unsafe {
        if let Some(tx) = &COMMAND_SENDER {
            let _ = tx.blocking_send(DhtCommand::AnnounceToHttp {
                bootstrap_url: url_string,
            });
        }
    }
}
