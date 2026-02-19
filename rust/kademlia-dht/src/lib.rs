use futures::StreamExt;
use jni::objects::{JClass, JString};
use jni::sys::jstring;
use jni::JNIEnv;
use libp2p::noise;
use libp2p::tcp;
use libp2p::yamux;
use libp2p::{
    identity,
    kad::{self, store::MemoryStore, Quorum, Record, RecordKey},
    swarm::{NetworkBehaviour, SwarmEvent},
    Multiaddr, PeerId, Swarm,
};
use serde::{Deserialize, Serialize};
use std::collections::HashMap;
use std::error::Error;
use std::str::FromStr;
use std::sync::{Mutex, OnceLock};
use std::time::Duration;
use tokio::runtime::Runtime;
use tokio::sync::mpsc;

// Global Runtime & Command Channel
static RUNTIME: OnceLock<Runtime> = OnceLock::new();
static COMMAND_SENDER: OnceLock<mpsc::Sender<DhtCommand>> = OnceLock::new();
static RESULTS: OnceLock<Mutex<HashMap<Vec<u8>, Vec<u8>>>> = OnceLock::new();

fn get_results() -> &'static Mutex<HashMap<Vec<u8>, Vec<u8>>> {
    RESULTS.get_or_init(|| Mutex::new(HashMap::new()))
}

enum DhtCommand {
    AddBootstrapNode { peer_id: PeerId, address: Multiaddr },
    Bootstrap,
    AnnounceToHttp { bootstrap_url: String },
    PutValue { key: Vec<u8>, value: Vec<u8> },
    GetValue { key: Vec<u8> },
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

        swarm.listen_on("/ip4/0.0.0.0/tcp/0".parse()?)?;
        let mut current_addr: Option<Multiaddr> = None;

        loop {
            tokio::select! {
                event = swarm.select_next_some() => match event {
                    SwarmEvent::NewListenAddr { address, .. } => {
                        if !address.to_string().contains("127.0.0.1") {
                            current_addr = Some(address.clone());
                        } else if current_addr.is_none() {
                            current_addr = Some(address.clone());
                        }
                    }
                    SwarmEvent::Behaviour(PhantomBehaviourEvent::Kademlia(kad::Event::QueryResult { result, .. })) => {
                        match result {
                            kad::QueryResult::GetRecord(Ok(kad::GetRecordOk::FoundRecord(kad::PeerRecord { record, .. }))) => {
                                let mut results = get_results().lock().unwrap();
                                results.insert(record.key.to_vec(), record.value);
                            }
                            _ => {}
                        }
                    }
                    _ => {}
                },
                command_opt = command_receiver.recv() => match command_opt {
                    Some(DhtCommand::AddBootstrapNode { peer_id, address }) => {
                        swarm.behaviour_mut().kademlia.add_address(&peer_id, address);
                    }
                    Some(DhtCommand::Bootstrap) => {
                         let _ = swarm.behaviour_mut().kademlia.bootstrap();
                    }
                    Some(DhtCommand::PutValue { key, value }) => {
                        let record = Record {
                            key: RecordKey::new(&key),
                            value,
                            publisher: None,
                            expires: None,
                        };
                        let _ = swarm.behaviour_mut().kademlia.put_record(record, Quorum::One);
                    }
                    Some(DhtCommand::GetValue { key }) => {
                        let _ = swarm.behaviour_mut().kademlia.get_record(RecordKey::new(&key));
                    }
                    Some(DhtCommand::AnnounceToHttp { bootstrap_url }) => {
                        if let Some(addr) = &current_addr {
                            let pid_str = local_pid.to_string();
                            let addr_str = addr.to_string();
                            let url_clone = bootstrap_url.clone();
                            tokio::spawn(async move {
                                let client = reqwest::Client::new();
                                let peer = PeerInfo { peer_id: pid_str, address: addr_str };
                                let _ = client.post(format!("{}/announce", url_clone)).json(&peer).send().await;
                            });
                        }
                    }
                    None => break,
                }
            }
        }
        Ok(())
    }
}

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
            if let Ok(node) = DhtNode::new(rx).await {
                let _ = node.run().await;
            }
        });
        let _ = RUNTIME.set(runtime);
        env.new_string("DHT Node Started").unwrap().into_raw()
    } else {
        env.new_string("DHT Node Running").unwrap().into_raw()
    }
}

#[no_mangle]
pub extern "system" fn Java_com_phantomnet_core_network_DhtService_putValue(
    mut env: JNIEnv,
    _class: JClass,
    key_str: JString,
    value_str: JString,
) {
    let key: String = env.get_string(&key_str).unwrap().into();
    let value: String = env.get_string(&value_str).unwrap().into();
    if let Some(tx) = COMMAND_SENDER.get() {
        let _ = tx.blocking_send(DhtCommand::PutValue {
            key: key.into_bytes(),
            value: value.into_bytes(),
        });
    }
}

#[no_mangle]
pub extern "system" fn Java_com_phantomnet_core_network_DhtService_getValue(
    mut env: JNIEnv,
    _class: JClass,
    key_str: JString,
) {
    let key: String = env.get_string(&key_str).unwrap().into();
    if let Some(tx) = COMMAND_SENDER.get() {
        let _ = tx.blocking_send(DhtCommand::GetValue {
            key: key.into_bytes(),
        });
    }
}

#[no_mangle]
pub extern "system" fn Java_com_phantomnet_core_network_DhtService_pollValue(
    mut env: JNIEnv,
    _class: JClass,
    key_str: JString,
) -> jstring {
    let key: String = env.get_string(&key_str).unwrap().into();
    let mut results = get_results().lock().unwrap();
    if let Some(value) = results.remove(&key.into_bytes()) {
        env.new_string(String::from_utf8_lossy(&value).to_string())
            .unwrap()
            .into_raw()
    } else {
        env.new_string("").unwrap().into_raw()
    }
}

#[no_mangle]
pub extern "system" fn Java_com_phantomnet_core_network_DhtService_addBootstrapNode(
    mut env: JNIEnv,
    _class: JClass,
    p_str: JString,
    a_str: JString,
) {
    let p: String = env.get_string(&p_str).unwrap().into();
    let a: String = env.get_string(&a_str).unwrap().into();
    if let Some(tx) = COMMAND_SENDER.get() {
        if let (Ok(pid), Ok(addr)) = (PeerId::from_str(&p), Multiaddr::from_str(&a)) {
            let _ = tx.blocking_send(DhtCommand::AddBootstrapNode {
                peer_id: pid,
                address: addr,
            });
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
    let url: String = env.get_string(&url_str).unwrap().into();
    if let Some(tx) = COMMAND_SENDER.get() {
        let _ = tx.blocking_send(DhtCommand::AnnounceToHttp { bootstrap_url: url });
    }
}
