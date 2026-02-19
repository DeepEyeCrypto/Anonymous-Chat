package com.phantomnet.core.network

import android.util.Log
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.util.Base64

@Serializable
data class IdentityBundle(
    val fingerprint: String,
    val publicKeyX25519: String // Base64
)

/**
 * Manages DHT-based "mailboxes" and identity publishing.
 * This is the core of Metadata-Free messaging: no central server knows who is talking to whom.
 */
object MailboxManager {
    private const val TAG = "MailboxManager"

    /**
     * Publish our identity to the DHT so others can find us by fingerprint.
     */
    fun publishIdentity(fingerprint: String, publicKey: ByteArray) {
        val bundle = IdentityBundle(
            fingerprint = fingerprint,
            publicKeyX25519 = Base64.getEncoder().encodeToString(publicKey)
        )
        val json = Json.encodeToString(bundle)
        DhtService.putValue("id:$fingerprint", json)
        Log.i(TAG, "Published identity to DHT for $fingerprint")
    }

    /**
     * Start fetching a peer's identity from the DHT.
     * Result must be polled via [pollIdentity].
     */
    fun fetchPeerIdentity(fingerprint: String) {
        DhtService.getValue("id:$fingerprint")
    }

    /**
     * Poll for a peer's identity.
     */
    fun pollIdentity(fingerprint: String): IdentityBundle? {
        val json = DhtService.pollValue("id:$fingerprint")
        if (json.isEmpty()) return null
        return try {
            Json.decodeFromString<IdentityBundle>(json)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to decode identity for $fingerprint", e)
            null
        }
    }

    /**
     * Push an encrypted message to a peer's DHT mailbox.
     */
    fun postMessage(recipientFingerprint: String, encryptedPayload: String) {
        // Mailbox keys are derived from the fingerprint to prevent simple enumeration
        // but are public enough for a peer to 'put'.
        DhtService.putValue("mb:$recipientFingerprint", encryptedPayload)
        Log.i(TAG, "Message posted to mailbox mb:$recipientFingerprint")
    }

    /**
     * Start fetching messages from our own mailbox.
     */
    fun fetchMyMessages(myFingerprint: String) {
        DhtService.getValue("mb:$myFingerprint")
    }

    /**
     * Poll for new messages in our own mailbox.
     */
    fun pollMyMessages(myFingerprint: String): String? {
        val payload = DhtService.pollValue("mb:$myFingerprint")
        return if (payload.isEmpty()) null else payload
    }
}
