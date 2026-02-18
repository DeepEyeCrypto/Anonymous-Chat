use aes_gcm::{
    aead::{Aead, AeadCore, KeyInit},
    Aes256Gcm,
};
use base64::{engine::general_purpose, Engine as _};
use jni::objects::{JClass, JString};
use jni::sys::jstring;
use jni::JNIEnv;
use rand_core::OsRng;
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

#[no_mangle]
pub extern "system" fn Java_com_phantomnet_core_crypto_SignalBridge_generateIdentity(
    env: JNIEnv,
    _class: JClass,
) -> jstring {
    let identity = Identity::generate();
    let output = identity.public_key_base64();
    let output_jstring = env
        .new_string(output)
        .expect("Couldn't create java string!");
    output_jstring.into_raw()
}

#[no_mangle]
pub extern "system" fn Java_com_phantomnet_core_crypto_SignalBridge_encryptMessage(
    mut env: JNIEnv,
    _class: JClass,
    message: JString,
    _recipient_public_key: JString,
) -> jstring {
    // 1. Decode inputs
    let message_str: String = match env.get_string(&message) {
        Ok(s) => s.into(),
        Err(_) => return env.new_string("Error: Invalid string").unwrap().into_raw(),
    };

    // MOCK: Generate a random key (simulate shared secret) for MVP
    let key = Aes256Gcm::generate_key(OsRng);
    let cipher = Aes256Gcm::new(&key);
    let nonce = Aes256Gcm::generate_nonce(OsRng); // 96-bits; unique per message

    // Encrypt
    let ciphertext = match cipher.encrypt(&nonce, message_str.as_bytes()) {
        Ok(ct) => ct,
        Err(_) => {
            return env
                .new_string("Error: Encryption failed")
                .unwrap()
                .into_raw();
        }
    };

    // Return base64 encoded payload (Key + Nonce + Ciphertext for demo)
    let mut payload = Vec::new();
    payload.extend_from_slice(key.as_slice());
    payload.extend_from_slice(nonce.as_slice());
    payload.extend_from_slice(&ciphertext);

    let output = general_purpose::STANDARD.encode(payload);

    env.new_string(output)
        .expect("Couldn't create java string!")
        .into_raw()
}

#[no_mangle]
pub extern "system" fn Java_com_phantomnet_core_crypto_SignalBridge_decryptMessage(
    mut env: JNIEnv,
    _class: JClass,
    encrypted_message: JString,
) -> jstring {
    let encrypted_str: String = match env.get_string(&encrypted_message) {
        Ok(s) => s.into(),
        Err(_) => return env.new_string("Error: Invalid string").unwrap().into_raw(),
    };

    let payload = match general_purpose::STANDARD.decode(encrypted_str) {
        Ok(p) => p,
        Err(_) => {
            return env
                .new_string("Error: Base64 decode failed")
                .unwrap()
                .into_raw();
        }
    };

    // Extract parts (Key: 32 bytes, Nonce: 12 bytes, Rest: Ciphertext)
    if payload.len() < 44 {
        return env
            .new_string("Error: Invalid payload length")
            .unwrap()
            .into_raw();
    }

    let key_bytes = &payload[0..32];
    let nonce_bytes = &payload[32..44];
    let ciphertext_bytes = &payload[44..];

    let key = aes_gcm::Key::<Aes256Gcm>::from_slice(key_bytes);
    let cipher = Aes256Gcm::new(key);
    let nonce = aes_gcm::Nonce::from_slice(nonce_bytes);

    let plaintext = match cipher.decrypt(nonce, ciphertext_bytes) {
        Ok(pt) => pt,
        Err(_) => {
            return env
                .new_string("Error: Decryption failed")
                .unwrap()
                .into_raw();
        }
    };

    let output = match String::from_utf8(plaintext) {
        Ok(s) => s,
        Err(_) => return env.new_string("Error: UTF-8 error").unwrap().into_raw(),
    };

    env.new_string(output)
        .expect("Couldn't create java string!")
        .into_raw()
}
