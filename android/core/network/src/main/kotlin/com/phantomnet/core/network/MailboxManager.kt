package com.phantomnet.core.network

import android.util.Log
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.util.Base64

@Serializable
data class IdentityBundle(
    val fingerprint: String,
    val publicKeyX25519: String, // Identity Key (Base64)
    val prekeyBundleJson: String? = null // Phase 2: Signal X3DH material
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
    fun publishIdentity(fingerprint: String, publicKey: ByteArray, prekeyBundleJson: String? = null) {
        val bundle = IdentityBundle(
            fingerprint = fingerprint,
            publicKeyX25519 = Base64.getEncoder().encodeToString(publicKey),
            prekeyBundleJson = prekeyBundleJson
        )
        val json = Json.encodeToString(bundle)
        DhtService.putValue("id:$fingerprint", json)
        Log.i(TAG, "Published identity to DHT for $fingerprint (X3DH: ${prekeyBundleJson != null})")
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
     * Optional hook for routing messages via the Mixnet.
     * Must be set by the app layer at startup (e.g. in PhantomApp.onCreate).
     */
    var mixnetSender: ((String) -> Unit)? = null

    /**
     * Push an encrypted message to a peer's DHT mailbox.
     */
    fun postMessage(recipientFingerprint: String, encryptedPayload: String, useMixnet: Boolean = false) {
        if (useMixnet) {
            // Route through Mixnet via registered hook (set by app layer to avoid circular deps)
            val sender = mixnetSender
            if (sender != null) {
                sender("mb:$recipientFingerprint|$encryptedPayload")
                Log.i(TAG, "Message routed through Mixnet for mb:$recipientFingerprint")
            } else {
                Log.w(TAG, "Mixnet sender not registered, falling back to direct DHT")
                DhtService.putValue("mb:$recipientFingerprint", encryptedPayload)
            }
        } else {
            // Direct DHT put
            DhtService.putValue("mb:$recipientFingerprint", encryptedPayload)
            Log.i(TAG, "Message posted directly to DHT mb:$recipientFingerprint")
        }
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

    /**
     * Post a DC-Net contribution to a shared room channel.
     * To avoid collisions, we index by participant ID.
     */
    fun postRoomContribution(roomId: String, participantId: Int, contribution: String) {
        DhtService.putValue("rm:$roomId:$participantId", contribution)
        Log.d(TAG, "Contribution posted for room $roomId by participant $participantId")
    }

    /**
     * Start fetching contributions for all participants in a room.
     */
    fun fetchRoomContributions(roomId: String, participantCount: Int) {
        for (i in 0 until participantCount) {
            DhtService.getValue("rm:$roomId:$i")
        }
    }

    /**
     * Poll for all contributions in a room.
     */
    fun pollRoomContributions(roomId: String, participantCount: Int): List<String> {
        val results = mutableListOf<String>()
        for (i in 0 until participantCount) {
            val valStr = DhtService.pollValue("rm:$roomId:$i")
            if (valStr.isNotEmpty()) {
                results.add(valStr)
            }
        }
        return results
    }
}
