use android_logger::Config;
use jni::objects::{JClass, JString};
use jni::sys::jstring;
use jni::JNIEnv;
use log::LevelFilter;

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

    // In a real scenario, we'd fetch actual shared secrets from the database
    let participant = phantom_dcnet::DcNetParticipant::new(my_id as u32, msg_bytes.len());

    let contribution = participant.compute_contribution(Some(msg_bytes));

    let output = format!("DC-NET_CONTRIBUTION_{}", hex::encode(contribution));
    env.new_string(output)
        .expect("Couldn't create java string")
        .into_raw()
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

    match action_type {
        1 => {
            // PANIC
            sentinel.shred_identity();
            log::error!("CRITICAL: Identity shredded by User Panic.");
        }
        _ => {}
    }
}
