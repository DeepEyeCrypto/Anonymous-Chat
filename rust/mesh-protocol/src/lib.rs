use std::time::Duration;
use jni::objects::{JClass, JString};
use jni::sys::{jstring, jint, JNI_VERSION_1_6};
use jni::JNIEnv;
use jni::JavaVM;
use std::ffi::c_void;
use tokio::runtime::Runtime;

// Global Runtime
static mut RUNTIME: Option<Runtime> = None;

#[no_mangle]
pub extern "system" fn Java_com_phantomnet_core_network_MeshService_startMesh(
    mut env: JNIEnv,
    _class: JClass,
) -> jstring {
    unsafe {
        if RUNTIME.is_none() {
            let runtime = Runtime::new().expect("Failed to create Tokio runtime");
             
            runtime.spawn(async {
                 log::info!("Rust Mesh Service: Started Scanning simulation...");
            });

            RUNTIME = Some(runtime);
            
            // Need to return a valid jstring for Java side to consume
            let output = env.new_string("Mesh Service Started").expect("Couldn't create java string!");
            output.into_raw()
        } else {
             let output = env.new_string("Mesh Service Already Running").expect("Couldn't create java string!");
             output.into_raw()
        }
    }
}

#[cfg(target_os = "android")]
#[no_mangle]
pub unsafe extern "system" fn JNI_OnLoad(vm: JavaVM, _reserved: *mut c_void) -> jint {
    android_logger::init_once(
        android_logger::Config::default().with_min_level(log::Level::Info),
    );
     
    let vm_ptr = vm.get_java_vm_pointer() as *mut c_void;
    ndk_context::initialize_android_context(vm_ptr);
    
    JNI_VERSION_1_6
}
