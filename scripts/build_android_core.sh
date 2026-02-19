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

# Detect NDK strip tool
STRIP_TOOL=$(find "$ANDROID_NDK_HOME/toolchains/llvm/prebuilt" -name "llvm-strip" | head -1)
if [ -z "$STRIP_TOOL" ]; then
    echo "⚠️  llvm-strip not found in NDK, symbols will not be stripped"
    STRIP_TOOL="true"  # no-op
fi

# Build all libraries in RELEASE mode
CRATES="libsignal-ffi kademlia-dht tor-client mesh-protocol phantom-core"

for crate in $CRATES; do
    echo "🔨 Building $crate (release)..."
    cargo ndk -t arm64-v8a -t armeabi-v7a -t x86_64 -t x86 -o $JNI_LIBS_DIR build --release -p $crate
done

# Strip debug symbols from all .so files
echo "🗜️  Stripping debug symbols..."
find $JNI_LIBS_DIR -name "*.so" -exec "$STRIP_TOOL" --strip-unneeded {} \;

# Report sizes
echo ""
echo "📊 Library sizes after strip:"
find $JNI_LIBS_DIR -name "*.so" -exec ls -lh {} \; | awk '{print $5, $NF}'

echo ""
echo "✅ Build Complete! Shared libraries are in $JNI_LIBS_DIR"
