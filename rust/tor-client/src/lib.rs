use arti_client::{TorClient, TorClientConfig};
use jni::JNIEnv;
use jni::objects::{JClass, JString};
use jni::sys::jstring;
use std::path::PathBuf;
use std::sync::Arc;
use tokio::runtime::Runtime;
use tor_rtcompat::PreferredRuntime;

// Global Runtime & Client
static mut RUNTIME: Option<Runtime> = None;
static mut TOR_CLIENT: Option<Arc<TorClient<PreferredRuntime>>> = None;

async fn bootstrap_tor(cache_dir: String) -> Result<String, anyhow::Error> {
    // 1. Configure Tor
    let mut config = TorClientConfig::builder();
    config
        .storage()
        .cache_dir(PathBuf::from(format!("{}/arti_cache", cache_dir)));
    config
        .storage()
        .state_dir(PathBuf::from(format!("{}/arti_state", cache_dir)));

    let config = config.build()?;

    println!("Starting Tor bootstrap...");

    // 2. Create Client
    let client = TorClient::create_bootstrapped(config).await?;

    println!("Tor bootstrap complete!");

    unsafe {
        TOR_CLIENT = Some(Arc::new(client));
    }

    Ok("Tor Bootstrapped Successfully".to_string())
}

#[no_mangle]
pub extern "system" fn Java_com_phantomnet_core_network_TorService_startTor(
    mut env: JNIEnv,
    _class: JClass,
    cache_dir: JString,
) -> jstring {
    let cache_dir_str: String = match env.get_string(&cache_dir) {
        Ok(s) => s.into(),
        Err(_) => {
            return env
                .new_string("Error: Invalid cache dir string")
                .unwrap()
                .into_raw();
        }
    };

    unsafe {
        if RUNTIME.is_none() {
            let runtime = Runtime::new().expect("Failed to create Tokio runtime");

            // Spawn bootstrap task
            runtime.spawn(async move {
                match bootstrap_tor(cache_dir_str).await {
                    Ok(msg) => println!("{}", msg),
                    Err(e) => eprintln!("Tor Bootstrap Failed: {:?}", e),
                }
            });

            RUNTIME = Some(runtime);
            return env.new_string("Tor Starting...").unwrap().into_raw();
        } else {
            return env.new_string("Tor Already Running").unwrap().into_raw();
        }
    }
}
