use arti_client::{TorClient, TorClientConfig};
use jni::objects::{JClass, JString};
use jni::sys::jstring;
use jni::JNIEnv;
use std::sync::{Arc, OnceLock};
use tokio::runtime::Runtime;
use tor_rtcompat::PreferredRuntime;

// Global Runtime & Client
static RUNTIME: OnceLock<Runtime> = OnceLock::new();
static TOR_CLIENT: OnceLock<Arc<TorClient<PreferredRuntime>>> = OnceLock::new();

async fn bootstrap_tor(cache_dir: String) -> Result<String, anyhow::Error> {
    // 1. Configure Tor
    let mut config = TorClientConfig::builder();
    config
        .storage()
        .cache_dir(arti_client::config::CfgPath::new(format!(
            "{}/arti_cache",
            cache_dir
        )))
        .state_dir(arti_client::config::CfgPath::new(format!(
            "{}/arti_state",
            cache_dir
        )));

    let config = config.build()?;

    println!("Starting Tor bootstrap...");

    // 2. Create Client
    let client = TorClient::create_bootstrapped(config).await?;

    println!("Tor bootstrap complete!");

    let _ = TOR_CLIENT.set(Arc::new(client));

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

    if RUNTIME.get().is_none() {
        let runtime = Runtime::new().expect("Failed to create Tokio runtime");

        // Spawn bootstrap task
        runtime.spawn(async move {
            match bootstrap_tor(cache_dir_str).await {
                Ok(msg) => println!("{}", msg),
                Err(e) => eprintln!("Tor Bootstrap Failed: {:?}", e),
            }
        });

        let _ = RUNTIME.set(runtime);
        env.new_string("Tor Starting...").unwrap().into_raw()
    } else {
        env.new_string("Tor Already Running").unwrap().into_raw()
    }
}
