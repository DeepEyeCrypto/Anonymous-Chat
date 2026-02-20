use aes_gcm::{
    aead::{Aead, AeadCore, KeyInit},
    Aes256Gcm,
};
use base64::{engine::general_purpose, Engine as _};
use jni::objects::{JClass, JString};
use jni::sys::jstring;
use jni::JNIEnv;
use rand_core::OsRng;
use serde::{Deserialize, Serialize};
use x25519_dalek::{PublicKey, StaticSecret};

pub struct Identity {
    _secret: StaticSecret,
    public: PublicKey,
}

impl Identity {
    pub fn generate() -> Self {
        // Updated to use random_from_rng as new is deprecated
        let secret = StaticSecret::random_from_rng(OsRng);
        let public = PublicKey::from(&secret);
        Self {
            _secret: secret,
            public,
        }
    }

    pub fn public_key_base64(&self) -> String {
        general_purpose::STANDARD.encode(self.public.as_bytes())
    }
}

#[derive(Serialize, Deserialize)]
pub struct PrekeyBundle {
    pub identity_key: String,
    pub identity_key_kyber: String,
    pub signed_prekey: String,
    pub signed_prekey_kyber: String,
    pub one_time_prekey: String,
    pub one_time_prekey_kyber: String,
}

#[derive(Serialize, Deserialize)]
pub struct PrekeySecretBundle {
    pub identity_key_secret: String,
    pub identity_key_kyber_secret: String,
    pub signed_prekey_secret: String,
    pub signed_prekey_kyber_secret: String,
}

#[derive(Serialize, Deserialize)]
pub struct BundleContainer {
    pub public_bundle: PrekeyBundle,
    pub secret_bundle: PrekeySecretBundle,
}

#[no_mangle]
pub extern "system" fn Java_com_phantomnet_core_crypto_SignalBridge_generatePrekeyBundle(
    env: JNIEnv,
    _class: JClass,
) -> jstring {
    let ik = Identity::generate();
    let spk = Identity::generate();
    let opk = Identity::generate();

    let (ik_kyber_pk, ik_kyber_sk) = phantom_pqc::generate_keypair();
    let (spk_kyber_pk, spk_kyber_sk) = phantom_pqc::generate_keypair();
    let (opk_kyber_pk, _) = phantom_pqc::generate_keypair();

    let public_bundle = PrekeyBundle {
        identity_key: ik.public_key_base64(),
        identity_key_kyber: general_purpose::STANDARD.encode(ik_kyber_pk.0),
        signed_prekey: spk.public_key_base64(),
        signed_prekey_kyber: general_purpose::STANDARD.encode(spk_kyber_pk.0),
        one_time_prekey: opk.public_key_base64(),
        one_time_prekey_kyber: general_purpose::STANDARD.encode(opk_kyber_pk.0),
    };

    let secret_bundle = PrekeySecretBundle {
        identity_key_secret: general_purpose::STANDARD.encode(ik._secret.to_bytes()),
        identity_key_kyber_secret: general_purpose::STANDARD.encode(ik_kyber_sk.0),
        signed_prekey_secret: general_purpose::STANDARD.encode(spk._secret.to_bytes()),
        signed_prekey_kyber_secret: general_purpose::STANDARD.encode(spk_kyber_sk.0),
    };

    let container = BundleContainer {
        public_bundle,
        secret_bundle,
    };

    let output = serde_json::to_string(&container).unwrap();
    env.new_string(output).unwrap().into_raw()
}

#[no_mangle]
pub extern "system" fn Java_com_phantomnet_core_crypto_SignalBridge_encapsulateKyber(
    mut env: JNIEnv,
    _class: JClass,
    their_public_base64: JString,
) -> jstring {
    let pk_str: String = env.get_string(&their_public_base64).unwrap().into();
    let pk_bytes = general_purpose::STANDARD.decode(pk_str).unwrap();

    let mut pk_array = [0u8; 1184]; // KYBER_PUBLICKEYBYTES for Kyber-768
    pk_array.copy_from_slice(&pk_bytes);

    let pk = phantom_pqc::PqcPublicKey(pk_array);
    let (ss, ct) = phantom_pqc::encapsulate_secret(&pk);

    let result = serde_json::json!({
        "ss": general_purpose::STANDARD.encode(ss),
        "ct": general_purpose::STANDARD.encode(ct)
    });

    let output = serde_json::to_string(&result).unwrap();
    env.new_string(output).unwrap().into_raw()
}

#[no_mangle]
pub extern "system" fn Java_com_phantomnet_core_crypto_SignalBridge_decapsulateKyber(
    mut env: JNIEnv,
    _class: JClass,
    my_secret_base64: JString,
    their_ciphertext_base64: JString,
) -> jstring {
    let sk_str: String = env.get_string(&my_secret_base64).unwrap().into();
    let ct_str: String = env.get_string(&their_ciphertext_base64).unwrap().into();

    let sk_bytes = general_purpose::STANDARD.decode(sk_str).unwrap();
    let ct_bytes = general_purpose::STANDARD.decode(ct_str).unwrap();

    let mut sk_array = [0u8; 2400]; // KYBER_SECRETKEYBYTES
    sk_array.copy_from_slice(&sk_bytes);

    let mut ct_array = [0u8; 1088]; // KYBER_CIPHERTEXTBYTES
    ct_array.copy_from_slice(&ct_bytes);

    let sk = phantom_pqc::PqcSecretKey(sk_array);
    let ss = phantom_pqc::decapsulate_secret(&sk, &ct_array);

    let output = general_purpose::STANDARD.encode(ss);
    env.new_string(output).unwrap().into_raw()
}

#[no_mangle]
pub extern "system" fn Java_com_phantomnet_core_crypto_SignalBridge_deriveHybridSecret(
    mut env: JNIEnv,
    _class: JClass,
    ss_x25519_base64: JString,
    ss_kyber_base64: JString,
) -> jstring {
    let x_str: String = env.get_string(&ss_x25519_base64).unwrap().into();
    let k_str: String = env.get_string(&ss_kyber_base64).unwrap().into();

    let x_bytes = general_purpose::STANDARD.decode(x_str).unwrap();
    let k_bytes = general_purpose::STANDARD.decode(k_str).unwrap();

    use sha2::{Digest, Sha256};
    let mut hasher = Sha256::new();
    hasher.update(&x_bytes);
    hasher.update(&k_bytes);
    let result = hasher.finalize();

    let output = general_purpose::STANDARD.encode(result);
    env.new_string(output).unwrap().into_raw()
}

#[no_mangle]
pub extern "system" fn Java_com_phantomnet_core_crypto_SignalBridge_deriveSharedSecret(
    mut env: JNIEnv,
    _class: JClass,
    my_secret_base64: JString,
    their_public_base64: JString,
) -> jstring {
    let sk_str: String = env.get_string(&my_secret_base64).unwrap().into();
    let pk_str: String = env.get_string(&their_public_base64).unwrap().into();

    let sk_bytes = match general_purpose::STANDARD.decode(sk_str) {
        Ok(b) => b,
        Err(_) => {
            return env
                .new_string("Error: Invalid sk base64")
                .unwrap()
                .into_raw()
        }
    };
    let pk_bytes = match general_purpose::STANDARD.decode(pk_str) {
        Ok(b) => b,
        Err(_) => {
            return env
                .new_string("Error: Invalid pk base64")
                .unwrap()
                .into_raw()
        }
    };

    if sk_bytes.len() != 32 || pk_bytes.len() != 32 {
        return env
            .new_string("Error: Invalid key length")
            .unwrap()
            .into_raw();
    }

    let mut sk_array = [0u8; 32];
    sk_array.copy_from_slice(&sk_bytes);
    let sk = x25519_dalek::StaticSecret::from(sk_array);

    let mut pk_array = [0u8; 32];
    pk_array.copy_from_slice(&pk_bytes);
    let pk = x25519_dalek::PublicKey::from(pk_array);

    let shared_secret = sk.diffie_hellman(&pk);

    let output = general_purpose::STANDARD.encode(shared_secret.as_bytes());
    env.new_string(output).unwrap().into_raw()
}

#[no_mangle]
pub extern "system" fn Java_com_phantomnet_core_crypto_SignalBridge_generateEphemeralKeyPair(
    env: JNIEnv,
    _class: JClass,
) -> jstring {
    let pair = Identity::generate();
    let result = serde_json::json!({
        "public": pair.public_key_base64(),
        "secret": general_purpose::STANDARD.encode(pair._secret.to_bytes())
    });
    let output = serde_json::to_string(&result).unwrap();
    env.new_string(output).unwrap().into_raw()
}

#[no_mangle]
pub extern "system" fn Java_com_phantomnet_core_crypto_SignalBridge_encryptWithKey(
    mut env: JNIEnv,
    _class: JClass,
    message: JString,
    key_base64: JString,
) -> jstring {
    let message_str: String = env.get_string(&message).unwrap().into();
    let key_str: String = env.get_string(&key_base64).unwrap().into();
    let key_bytes = general_purpose::STANDARD.decode(key_str).unwrap();

    let key = aes_gcm::Key::<Aes256Gcm>::from_slice(&key_bytes);
    let cipher = Aes256Gcm::new(key);
    let nonce = Aes256Gcm::generate_nonce(OsRng);

    let ciphertext = cipher.encrypt(&nonce, message_str.as_bytes()).unwrap();

    let mut payload = Vec::new();
    payload.extend_from_slice(nonce.as_slice());
    payload.extend_from_slice(&ciphertext);

    let output = general_purpose::STANDARD.encode(payload);
    env.new_string(output).unwrap().into_raw()
}

#[no_mangle]
pub extern "system" fn Java_com_phantomnet_core_crypto_SignalBridge_decryptWithKey(
    mut env: JNIEnv,
    _class: JClass,
    encrypted_base64: JString,
    key_base64: JString,
) -> jstring {
    let encrypted_str: String = env.get_string(&encrypted_base64).unwrap().into();
    let key_str: String = env.get_string(&key_base64).unwrap().into();
    let key_bytes = general_purpose::STANDARD.decode(key_str).unwrap();
    let payload = general_purpose::STANDARD.decode(encrypted_str).unwrap();

    if payload.len() < 12 {
        return env
            .new_string("Error: Payload too short")
            .unwrap()
            .into_raw();
    }

    let nonce_bytes = &payload[0..12];
    let ciphertext_bytes = &payload[12..];

    let key = aes_gcm::Key::<Aes256Gcm>::from_slice(&key_bytes);
    let cipher = Aes256Gcm::new(key);
    let nonce = aes_gcm::Nonce::from_slice(nonce_bytes);

    let plaintext = cipher.decrypt(nonce, ciphertext_bytes).unwrap();
    let output = String::from_utf8(plaintext).unwrap();

    env.new_string(output).unwrap().into_raw()
}
