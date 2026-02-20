use btleplug::api::{Central, Manager as _, Peripheral, ScanFilter};
use btleplug::platform::Manager;
use jni::objects::JClass;
use jni::sys::{jint, jstring, JNI_VERSION_1_6};
use jni::JNIEnv;
use jni::JavaVM;
use std::ffi::c_void;
use std::sync::OnceLock;
use std::time::Duration;
use tokio::runtime::Runtime;
use tokio::time;

// Global Statics using OnceLock for thread-safety
static RUNTIME: OnceLock<Runtime> = OnceLock::new();
static JAVA_VM: OnceLock<JavaVM> = OnceLock::new();

#[no_mangle]
pub extern "system" fn Java_com_phantomnet_core_network_MeshService_startMesh(
    env: JNIEnv,
    _class: JClass,
) -> jstring {
    let runtime = RUNTIME.get_or_init(|| {
        Runtime::new().expect("Failed to create Tokio runtime")
    });

    runtime.spawn(async {
        log::info!("Starting BLE Scan...");
        match Manager::new().await {
            Ok(manager) => {
                match manager.adapters().await {
                    Ok(adapters) => {
                        if let Some(central) = adapters.into_iter().next() {
                            if central.start_scan(ScanFilter::default()).await.is_ok() {
                                log::info!("BLE Scan started successfully");
                                
                                // Discovery loop
                                loop {
                                    time::sleep(Duration::from_secs(5)).await;
                                    if let Ok(peripherals) = central.peripherals().await {
                                        for p in peripherals {
                                            if let Ok(Some(props)) = p.properties().await {
                                                let name = props
                                                    .local_name
                                                    .unwrap_or_else(|| "Unknown".to_string());
                                                log::info!("Discovered Device: {} [{}]", name, p.id());
                                                
                                                // Call back to Kotlin
                                                if let Some(vm) = JAVA_VM.get() {
                                                    if let Ok(mut env) = vm.attach_current_thread() {
                                                        if let Ok(class) = env.find_class("com/phantomnet/core/network/MeshService") {
                                                            if let Ok(j_name) = env.new_string(&name) {
                                                                let _ = env.call_static_method(
                                                                    class,
                                                                    "onDeviceFound",
                                                                    "(Ljava/lang/String;)V",
                                                                    &[(&j_name).into()],
                                                                );
                                                            }
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            } else {
                                log::error!("Failed to start BLE scan");
                            }
                        } else {
                            log::error!("No Bluetooth adapters found");
                        }
                    }
                    Err(e) => log::error!("Failed to get adapters: {:?}", e),
                }
            }
            Err(e) => log::error!("Failed to create BLE manager: {:?}", e),
        }
    });

    env.new_string("Mesh Service Started (Scanning)")
        .expect("Couldn't create java string!")
        .into_raw()
}

#[no_mangle]
/// # Safety
///
/// Called by the JVM when this native library is loaded. The provided `JavaVM`
/// handle and `_reserved` pointer come from the JVM runtime and must be valid
/// per JNI contract for the duration of this function.
pub unsafe extern "system" fn JNI_OnLoad(vm: JavaVM, _reserved: *mut c_void) -> jint {
    #[cfg(target_os = "android")]
    {
        android_logger::init_once(
            android_logger::Config::default().with_max_level(log::LevelFilter::Info),
        );
        let vm_ptr = vm.get_java_vm_pointer() as *mut c_void;
        ndk_context::initialize_android_context(vm_ptr, std::ptr::null_mut());
    }

    let _ = JAVA_VM.set(vm);
    JNI_VERSION_1_6
}
