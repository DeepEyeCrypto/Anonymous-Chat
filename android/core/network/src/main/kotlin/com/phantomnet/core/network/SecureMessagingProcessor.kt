package com.phantomnet.core.network

import android.content.Context
import android.util.Log
import com.phantomnet.core.database.entity.ConversationEntity
import com.phantomnet.core.database.entity.MessageEntity
import com.phantomnet.core.identity.IdentityManager
import com.phantomnet.core.crypto.SignalBridge
import java.util.UUID

/**
 * Handles processing of incoming raw payloads from the DHT mailbox.
 */
object SecureMessagingProcessor {
    private const val TAG = "SecureMessagingProcessor"

    suspend fun processIncomingPayload(context: Context, payload: String) {
        val identityManager = IdentityManager.getInstance(context)
        val db = identityManager.getDatabase() ?: return

        // 1. Try to decrypt
        val decrypted = try {
            SignalBridge.decryptMessageSafe(payload)
        } catch (e: Exception) {
            Log.e(TAG, "Decryption failed for incoming payload")
            return
        }

        // 2. Check for Handshake
        if (decrypted.startsWith("PHANTOM_HANDSHAKE:")) {
            handleHandshake(db, decrypted)
        } else {
            // 3. Handle data message
            // In a real app, we'd look up the conversation by the sender's fingerprint.
            // For MVP Phase 2, we find the most recent conversation and append.
            val latestConv = com.phantomnet.core.database.PhantomDatabaseFactory.getInstance(context, "placeholder".toByteArray())
                .conversationDao().getAll().let { /* flow collecting is complex here, simplified for demo */ }
                
            // simplified: append to any conversation for now or log
            Log.i(TAG, "Message received: $decrypted")
        }
    }

    private suspend fun handleHandshake(db: com.phantomnet.core.database.PhantomDatabase, handshake: String) {
        val params = handshake.removePrefix("PHANTOM_HANDSHAKE:").split(",")
        val senderFp = params.find { it.startsWith("my_fp=") }?.removePrefix("my_fp=") ?: "Unknown"

        val conversation = ConversationEntity(
            id = UUID.randomUUID().toString(),
            contactName = "New Peer ($senderFp)",
            contactFingerprint = senderFp,
            contactPublicKey = "handshake_exchange".toByteArray(),
            lastMessagePreview = "Handshake received. Secure session established.",
            lastMessageTimestamp = System.currentTimeMillis(),
            unreadCount = 1,
            isOnline = true,
            routingMode = "DHT"
        )
        db.conversationDao().upsert(conversation)
        Log.i(TAG, "New conversation created from handshake: $senderFp")
    }
}
