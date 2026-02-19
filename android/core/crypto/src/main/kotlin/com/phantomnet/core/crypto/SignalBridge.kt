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
        fun encryptMessageSafe(message: String, recipientPublicKey: String): String {
            if (!nativeLoaded) throw IllegalStateException("Signal native lib not available")
            return encryptMessage(message, recipientPublicKey)
        }

        @JvmStatic
        fun decryptMessageSafe(encryptedMessage: String): String {
            if (!nativeLoaded) throw IllegalStateException("Signal native lib not available")
            return decryptMessage(encryptedMessage)
        }

        @JvmStatic
        private external fun generateIdentity(): String

        @JvmStatic
        private external fun encryptMessage(message: String, recipientPublicKey: String): String

        @JvmStatic
        private external fun decryptMessage(encryptedMessage: String): String
    }
}
