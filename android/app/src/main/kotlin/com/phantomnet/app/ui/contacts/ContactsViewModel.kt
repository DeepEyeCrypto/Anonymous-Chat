package com.phantomnet.app.ui.contacts

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.phantomnet.core.database.entity.ConversationEntity
import com.phantomnet.core.identity.IdentityManager
import com.phantomnet.core.network.MailboxManager
import com.phantomnet.core.crypto.SignalBridge
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.UUID

class ContactsViewModel(application: Application) : AndroidViewModel(application) {
    private val identityManager = IdentityManager.getInstance(application)

    fun connectToPeer(input: String, onSuccess: () -> Unit) {
        viewModelScope.launch(Dispatchers.IO) {
            val db = identityManager.getDatabase() ?: return@launch
            
            // For Phase 1/2 Demo: 
            // - If input is valid hex (32 bytes = 64 chars), treat as Public Key.
            // - Otherwise, assume it's just a nickname/fingerprint for now.
            val cleanInput = input.trim()
            val contactKey = if (cleanInput.length >= 64) cleanInput.take(64) else "mock_key_0000"
            val contactFingerprint = if (cleanInput.length >= 16) cleanInput.take(16) else "PHANTOM-" + UUID.randomUUID().toString().take(4)
            val contactName = "Peer $contactFingerprint"

            val conversation = ConversationEntity(
                id = UUID.randomUUID().toString(),
                contactName = contactName,
                contactFingerprint = contactFingerprint,
                contactPublicKey = contactKey.toByteArray(), // simplified
                lastMessagePreview = "Initiating encrypted session...",
                lastMessageTimestamp = System.currentTimeMillis(),
                unreadCount = 0,
                isOnline = false,
                routingMode = "DHT"
            )

            db.conversationDao().upsert(conversation)

            // ── Phase 2: Signal Handshake ──
            // Post an "Initial Handshake" message to the peer's mailbox.
            // In a real implementation, this would contain the X3DH pre-keys.
            val myFp = identityManager.fingerprint ?: "unidentified"
            val handshakeMsg = "PHANTOM_HANDSHAKE:my_fp=$myFp"
            
            // Encrypt for the peer (using SignalBridge)
            try {
                val encrypted = SignalBridge.encryptMessageSafe(handshakeMsg, contactKey)
                MailboxManager.postMessage(contactFingerprint, encrypted)
            } catch (e: Exception) {
                // Fallback to plain for demo if native fails
                MailboxManager.postMessage(contactFingerprint, "PLAIN_MODE:$handshakeMsg")
            }

            onSuccess()
        }
    }
}
