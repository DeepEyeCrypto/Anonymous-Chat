package com.phantomnet.core.crypto

class SignalBridge {
    companion object {
        @JvmStatic
        external fun generateIdentity(): String

        @JvmStatic
        external fun encryptMessage(message: String, recipientPublicKey: String): String

        @JvmStatic
        external fun decryptMessage(encryptedMessage: String): String
    }
}
