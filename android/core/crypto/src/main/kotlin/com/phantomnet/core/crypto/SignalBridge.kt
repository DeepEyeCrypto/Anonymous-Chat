package com.phantomnet.core.crypto

import android.util.Log

class SignalBridge {
    companion object {
        private const val TAG = "SignalBridge"

        @Volatile
        private var nativeLoaded: Boolean = false

        init {
            try {
                System.loadLibrary("libsignal_ffi")
                nativeLoaded = true
                Log.i(TAG, "libsignal_ffi loaded")
            } catch (e: UnsatisfiedLinkError) {
                Log.e(TAG, "libsignal_ffi unavailable: ${e.message}")
            }
        }

        @JvmStatic
        fun generatePrekeyBundleSafe(): String {
            if (!nativeLoaded) throw IllegalStateException("Signal native lib not available")
            return generatePrekeyBundle()
        }

        @JvmStatic
        fun generateEphemeralKeyPairSafe(): String {
            if (!nativeLoaded) throw IllegalStateException("Signal native lib not available")
            return generateEphemeralKeyPair()
        }

        @JvmStatic
        fun encapsulateKyberSafe(theirPublicKyber: String): String {
            if (!nativeLoaded) throw IllegalStateException("Signal native lib not available")
            return encapsulateKyber(theirPublicKyber)
        }

        @JvmStatic
        fun decapsulateKyberSafe(mySecretKyber: String, theirCiphertext: String): String {
            if (!nativeLoaded) throw IllegalStateException("Signal native lib not available")
            return decapsulateKyber(mySecretKyber, theirCiphertext)
        }

        @JvmStatic
        fun deriveHybridSecretSafe(ssX25519: String, ssKyber: String): String {
            if (!nativeLoaded) throw IllegalStateException("Signal native lib not available")
            return deriveHybridSecret(ssX25519, ssKyber)
        }

        @JvmStatic
        fun deriveSharedSecretSafe(mySecret: String, theirPublic: String): String {
            if (!nativeLoaded) throw IllegalStateException("Signal native lib not available")
            return deriveSharedSecret(mySecret, theirPublic)
        }

        @JvmStatic
        fun encryptWithKeySafe(message: String, keyBase64: String): String {
            if (!nativeLoaded) throw IllegalStateException("Signal native lib not available")
            return encryptWithKey(message, keyBase64)
        }

        @JvmStatic
        fun decryptWithKeySafe(encryptedBase64: String, keyBase64: String): String {
            if (!nativeLoaded) throw IllegalStateException("Signal native lib not available")
            return decryptWithKey(encryptedBase64, keyBase64)
        }

        @JvmStatic
        private external fun generatePrekeyBundle(): String

        @JvmStatic
        private external fun generateEphemeralKeyPair(): String

        @JvmStatic
        private external fun encapsulateKyber(theirPublicKyber: String): String

        @JvmStatic
        private external fun decapsulateKyber(mySecretKyber: String, theirCiphertext: String): String

        @JvmStatic
        private external fun deriveHybridSecret(ssX25519: String, ssKyber: String): String

        @JvmStatic
        private external fun deriveSharedSecret(mySecret: String, theirPublic: String): String

        @JvmStatic
        private external fun encryptWithKey(message: String, keyBase64: String): String

        @JvmStatic
        private external fun decryptWithKey(encryptedBase64: String, keyBase64: String): String
    }
}
