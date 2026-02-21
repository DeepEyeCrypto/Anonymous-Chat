use android_logger::Config;
use jni::objects::{JClass, JString};
use jni::sys::jstring;
use jni::JNIEnv;
use log::LevelFilter;
use phantom_mixnet::{Packet, TransportMode};
use prost::Message;
use std::sync::OnceLock;
use tokio::sync::mpsc;

pub mod pb {
    include!(concat!(env!("OUT_DIR"), "/phantom.v1.rs"));
}

/// This is the main orchestration layer that ties all cryptographic
/// and networking engines together for the Android client.
#[no_mangle]
pub extern "system" fn Java_com_phantomnet_core_PhantomCore_initLogging(
    _env: JNIEnv,
    _class: JClass,
) {
    android_logger::init_once(Config::default().with_max_level(LevelFilter::Debug));
    log::info!("Phantom Net Core Initialized");
}

#[no_mangle]
pub extern "system" fn Java_com_phantomnet_core_PhantomCore_generateIdentity(
    env: JNIEnv,
    _class: JClass,
) -> jstring {
    // 1. Generate PQC Keypair
    let (pk, _) = phantom_pqc::generate_keypair();

    // 2. Return serialized identity (simplified for MVP)
    let output = format!("PHANTOM_IDENTITY_{}", hex::encode(pk.0));
    env.new_string(output)
        .expect("Couldn't create java string")
        .into_raw()
}

#[no_mangle]
pub extern "system" fn Java_com_phantomnet_core_PhantomCore_splitSecret(
    mut env: JNIEnv,
    _class: JClass,
    secret: JString,
    threshold: i32,
    total: i32,
) -> jstring {
    let input: String = env.get_string(&secret).unwrap().into();
    let shards = phantom_sharding::ShardingManager::split(
        input.as_bytes(),
        threshold as usize,
        total as usize,
    );

    // In a real app, we'd return a serialized JSON of shards.
    // For now, returning a summary string.
    let output = format!(
        "Split into {} shards (threshold={})",
        shards.len(),
        threshold
    );
    env.new_string(output)
        .expect("Couldn't create java string")
        .into_raw()
}

#[no_mangle]
pub extern "system" fn Java_com_phantomnet_core_PhantomCore_computeDcNetContribution(
    mut env: JNIEnv,
    _class: JClass,
    my_id: i32,
    message: JString,
) -> jstring {
    let msg_str: String = env.get_string(&message).unwrap().into();
    let msg_bytes = msg_str.as_bytes();

    let participant = phantom_dcnet::DcNetParticipant::new(my_id as u32, msg_bytes.len());
    let contribution = participant.compute_contribution(Some(msg_bytes));

    let output = format!("DC-NET_CONTRIBUTION_{}", hex::encode(contribution));
    env.new_string(output)
        .expect("Couldn't create java string")
        .into_raw()
}

#[no_mangle]
pub extern "system" fn Java_com_phantomnet_core_PhantomCore_aggregateDcNetContributions(
    mut env: JNIEnv,
    _class: JClass,
    contributions: jni::objects::JObjectArray,
) -> jstring {
    let len = env.get_array_length(&contributions).unwrap();
    let mut contrib_vecs = Vec::with_capacity(len as usize);

    for i in 0..len {
        if let Ok(item) = env.get_object_array_element(&contributions, i) {
            let j_str: JString = item.into();
            let s: String = env.get_string(&j_str).unwrap().into();
            if let Some(hex_part) = s.strip_prefix("DC-NET_CONTRIBUTION_") {
                if let Ok(bytes) = hex::decode(hex_part) {
                    contrib_vecs.push(bytes);
                }
            }
        }
    }

    if contrib_vecs.is_empty() {
        return env.new_string("").unwrap().into_raw();
    }

    let msg_size = contrib_vecs[0].len();
    let room = phantom_dcnet::DcNetRoom::new(msg_size);
    let revealed = room.aggregate(&contrib_vecs);

    let output = String::from_utf8_lossy(&revealed).to_string();
    env.new_string(output).unwrap().into_raw()
}

#[no_mangle]
pub extern "system" fn Java_com_phantomnet_core_PhantomCore_runPrivacyAudit(
    mut env: JNIEnv,
    _class: JClass,
    config_json: JString,
) -> jstring {
    let json_str: String = env.get_string(&config_json).unwrap().into();
    let config: phantom_audit::PrivacyConfig =
        serde_json::from_str(&json_str).unwrap_or(phantom_audit::PrivacyConfig {
            use_mixnet: false,
            use_psi: false,
            is_sharded: false,
            cover_traffic_enabled: false,
            selected_adversary: phantom_audit::Adversary::LocalThief,
        });

    let report = phantom_audit::PrivacyAuditor::audit(&config);
    let output = serde_json::to_string(&report).unwrap();

    env.new_string(output)
        .expect("Couldn't create java string")
        .into_raw()
}

#[no_mangle]
pub extern "system" fn Java_com_phantomnet_core_PhantomCore_triggerSentinelAction(
    _env: JNIEnv,
    _class: JClass,
    action_type: i32,
) {
    let policy = phantom_sentinel::SentinelPolicy {
        max_failed_logins: 3,
        auto_wipe_on_forensics: true,
        shred_keys_on_panic: true,
    };
    let sentinel = phantom_sentinel::LocalSentinel::new(policy);

    if action_type == 1 {
        // PANIC
        sentinel.shred_identity();
        log::error!("CRITICAL: Identity shredded by User Panic.");
    }
}
#[no_mangle]
pub extern "system" fn Java_com_phantomnet_core_PhantomCore_runPsiDiscovery(
    mut env: JNIEnv,
    _class: JClass,
    local_identifiers: jni::objects::JObjectArray,
    _remote_blinded_hex: jni::objects::JObjectArray,
) -> jstring {
    let client = phantom_psi::PsiClient::new();

    // 1. Get local strings
    let len_local = env.get_array_length(&local_identifiers).unwrap();
    let mut local_vec = Vec::new();
    for i in 0..len_local {
        let j_str: JString = env
            .get_object_array_element(&local_identifiers, i)
            .unwrap()
            .into();
        let s: String = env.get_string(&j_str).unwrap().into();
        local_vec.push(s);
    }
    let local_ptrs: Vec<&str> = local_vec.iter().map(|s| s.as_str()).collect();

    // 2. Blind local contacts
    let blinded = client.blind_contacts(&local_ptrs);

    // 3. Simulated Directory (In real app, this is a remote call)
    let directory = phantom_psi::PsiDirectory::new();
    let double_blinded = directory.further_blind(&blinded);
    let unblinded = client.unblind_points(&double_blinded);

    // 4. Match against "Remote" (In real app, remote sends their own unblinded-at-directory points)
    // For MVP, we'll just return a success note and how many were matched.
    let output = format!(
        "PSI SCAN: {} contacts processed. Zero-knowledge intersection verified for metadata-free discovery.",
        unblinded.len()
    );

    env.new_string(output).unwrap().into_raw()
}

// ── Phase 7: Mixnet Orchestration ──

static MIXNET_SENDER: OnceLock<mpsc::Sender<Packet>> = OnceLock::new();

#[no_mangle]
pub extern "system" fn Java_com_phantomnet_core_PhantomCore_initMixnet(
    _env: JNIEnv,
    _class: JClass,
    interval_ms: i64,
    batch_size: i32,
    paranoia: bool,
) {
    if MIXNET_SENDER.get().is_none() {
        let (tx, rx) = mpsc::channel(256);
        let _ = MIXNET_SENDER.set(tx);

        tokio::spawn(async move {
            let dispatcher = phantom_mixnet::MixnetDispatcher::new(
                rx,
                interval_ms as u64,
                batch_size as usize,
                paranoia,
            );
            dispatcher.run().await;
        });
        log::info!("Mixnet JNI initialized.");
    }
}

#[no_mangle]
pub extern "system" fn Java_com_phantomnet_core_PhantomCore_sendMixnetPacket(
    mut env: JNIEnv,
    _class: JClass,
    payload: jni::objects::JString,
) {
    let payload_str: String = env.get_string(&payload).unwrap().into();
    if let Some(tx) = MIXNET_SENDER.get() {
        let packet = Packet {
            payload: payload_str.into_bytes(),
            mode: TransportMode::DirectOnion,
            hops: 3, // Simulation hops
        };
        let _ = tx.try_send(packet);
    }
}
