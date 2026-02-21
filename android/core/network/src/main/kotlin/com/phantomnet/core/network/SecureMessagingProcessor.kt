package com.phantomnet.core.network

import android.content.Context
import android.util.Log
import com.phantomnet.core.database.entity.ConversationEntity
import com.phantomnet.core.database.entity.MessageEntity
import com.phantomnet.core.identity.IdentityManager
import com.phantomnet.core.crypto.SignalBridge
import kotlinx.coroutines.flow.firstOrNull
import java.util.UUID

/**
 * Handles processing of incoming raw payloads from the DHT mailbox.
 */
object SecureMessagingProcessor {
    private const val TAG = "SecureMessagingProcessor"

    suspend fun processIncomingPayload(context: Context, payload: String) {
        val identityManager = IdentityManager.getInstance(context)
        val db = identityManager.getDatabase() ?: return

        try {
            val jsonObj = org.json.JSONObject(payload)
            val type = jsonObj.optString("type")

            if (type == "HANDSHAKE_INIT_HYBRID") {
                handleHybridHandshake(context, db, jsonObj)
            } else {
                // Handle standard encrypted message (Double Ratchet)
                handleEncryptedMessage(context, db, jsonObj)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to process incoming payload: ${e.message}")
        }
    }

    private suspend fun handleHybridHandshake(
        context: Context,
        db: com.phantomnet.core.database.PhantomDatabase,
        handshake: org.json.JSONObject
    ) {
        val senderFp = handshake.getString("fp")
        val senderIk = handshake.getString("ik")
        val senderEk = handshake.getString("ek")
        val kyberCt = handshake.getString("kct")
        val encryptedMsg = handshake.getString("msg")

        // 1. Get our Secrets
        val persona = db.personaDao().getActivePersona().firstOrNull() ?: return
        val secretBundleObj = org.json.JSONObject(persona.secretBundleJson ?: return)
        
        val mySpkSecretX25519 = secretBundleObj.getString("signed_prekey_secret")
        val mySpkSecretKyber = secretBundleObj.getString("signed_prekey_kyber_secret")

        // 2. Derive Hybrid Shared Secret
        // 2a. X25519 DH (SPK_B_secret * IK_A_public)
        val ssX25519 = SignalBridge.deriveSharedSecretSafe(mySpkSecretX25519, senderIk)
        
        // 2b. Kyber Decapsulation
        val ssKyber = SignalBridge.decapsulateKyberSafe(mySpkSecretKyber, kyberCt)
        
        // 2c. Combine
        val sharedSecretBase64 = SignalBridge.deriveHybridSecretSafe(ssX25519, ssKyber)
        val sharedSecretBytes = android.util.Base64.decode(sharedSecretBase64, android.util.Base64.DEFAULT)

        // 3. Decrypt initial message
        val plaintext = SignalBridge.decryptWithKeySafe(encryptedMsg, sharedSecretBase64)

        // 4. Create Conversation
        val conversation = ConversationEntity(
            id = UUID.randomUUID().toString(),
            contactName = "Peer ($senderFp)",
            contactFingerprint = senderFp,
            contactPublicKey = android.util.Base64.decode(senderIk, android.util.Base64.DEFAULT),
            lastMessagePreview = plaintext,
            lastMessageTimestamp = System.currentTimeMillis(),
            unreadCount = 1,
            isOnline = true,
            routingMode = "DHT",
            sharedSecret = sharedSecretBytes
        )
        db.conversationDao().upsert(conversation)

        // 5. Store Message
        val message = MessageEntity(
            id = UUID.randomUUID().toString(),
            conversationId = conversation.id,
            senderId = senderFp,
            contentPlaintext = plaintext,
            contentCiphertext = android.util.Base64.decode(encryptedMsg, android.util.Base64.DEFAULT),
            timestamp = System.currentTimeMillis(),
            isMe = false,
            status = "DELIVERED",
            expiresAt = null
        )
        db.messageDao().insert(message)

        Log.i(TAG, "HYBRID Secure session established with $senderFp (X25519 + Kyber-768)")
    }

    private suspend fun handleEncryptedMessage(
        context: Context,
        db: com.phantomnet.core.database.PhantomDatabase,
        json: org.json.JSONObject
    ) {
        val senderFp = json.getString("fp")
        val ciphertext = json.getString("msg")

        // Find conversation with this peer
        val conv = db.conversationDao().getByFingerprint(senderFp) ?: return
        val sharedSecret = conv.sharedSecret ?: return
        val sharedSecretBase64 = android.util.Base64.encodeToString(sharedSecret, android.util.Base64.DEFAULT)

        // Decrypt
        val plaintext = SignalBridge.decryptWithKeySafe(ciphertext, sharedSecretBase64)

        // Store and update conv
        val message = MessageEntity(
            id = UUID.randomUUID().toString(),
            conversationId = conv.id,
            senderId = senderFp,
            contentPlaintext = plaintext,
            contentCiphertext = android.util.Base64.decode(ciphertext, android.util.Base64.DEFAULT),
            timestamp = System.currentTimeMillis(),
            isMe = false,
            status = "DELIVERED",
            expiresAt = null
        )
        db.messageDao().insert(message)
        
        db.conversationDao().updateLastMessage(conv.id, plaintext, System.currentTimeMillis(), true)
        Log.i(TAG, "Decrypted message from $senderFp")
    }
}
