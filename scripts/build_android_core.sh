#!/bin/bash
set -e

# Configuration
ANDROID_TARGETS="aarch64-linux-android armv7-linux-androideabi x86_64-linux-android i686-linux-android"
JNI_LIBS_DIR="android/app/src/main/jniLibs"

echo "======================================"
echo "🚀 Building Phantom Net Core (Rust)"
echo "======================================"

# Check for NDK
if [ -z "$ANDROID_NDK_HOME" ]; then
    echo "⚠️  ANDROID_NDK_HOME is not set. Please set it to your NDK installation path."
    # Try to use Android Studio's default NDK if available
    if [ -d "$ANDROID_HOME/ndk/25.1.8937393" ]; then
         export ANDROID_NDK_HOME="$ANDROID_HOME/ndk/25.1.8937393"
    elif [ -d "$HOME/Library/Android/sdk/ndk/25.1.8937393" ]; then
         export ANDROID_NDK_HOME="$HOME/Library/Android/sdk/ndk/25.1.8937393"
    elif [ -d "$HOME/Library/Android/sdk/ndk-bundle" ]; then
         export ANDROID_NDK_HOME="$HOME/Library/Android/sdk/ndk-bundle"
    else
         echo "ℹ️  Proceeding with host build for verification..."
         cargo build --workspace
         exit 0
    fi
fi


# Install cargo-ndk if needed
if ! command -v cargo-ndk &> /dev/null; then
    echo "📦 Installing cargo-ndk..."
    cargo install cargo-ndk
fi

# Add targets
echo "➕ Adding Rust targets..."
for target in $ANDROID_TARGETS; do
    rustup target add $target
done

# Build Libraries
echo "🔨 Building libsignal-ffi..."
cargo ndk -t arm64-v8a -t armeabi-v7a -t x86_64 -t x86 -o $JNI_LIBS_DIR build -p libsignal-ffi

echo "🔨 Building kademlia-dht..."
cargo ndk -t arm64-v8a -t armeabi-v7a -t x86_64 -t x86 -o $JNI_LIBS_DIR build -p kademlia-dht

echo "🔨 Building tor-client..."
cargo ndk -t arm64-v8a -t armeabi-v7a -t x86_64 -t x86 -o $JNI_LIBS_DIR build -p tor-client

echo "🔨 Building mesh-protocol..."
cargo ndk -t arm64-v8a -t armeabi-v7a -t x86_64 -t x86 -o $JNI_LIBS_DIR build -p mesh-protocol

echo "🔨 Building phantom-core..."
cargo ndk -t arm64-v8a -t armeabi-v7a -t x86_64 -t x86 -o $JNI_LIBS_DIR build -p phantom-core

echo "✅ Build Complete! Shared libraries are in $JNI_LIBS_DIR"
