use prost_build::Config;
use std::env;
use std::path::PathBuf;

fn main() {
    let out_dir = PathBuf::from(env::var("OUT_DIR").unwrap());

    // Configure prost to compile the proto file into the OUT_DIR
    Config::new()
        .out_dir(out_dir)
        .compile_protos(&["../../proto/phantom.proto"], &["../../proto"])
        .expect("Failed to compile protobuf definitions");

    // Tell Cargo that if the proto file changes, to rerun this build script
    println!("cargo:rerun-if-changed=../../proto/phantom.proto");
}
