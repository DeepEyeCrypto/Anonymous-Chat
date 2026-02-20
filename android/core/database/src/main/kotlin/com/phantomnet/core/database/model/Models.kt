package com.phantomnet.core.database.model

/**
 * Domain model for a Phantom Net persona (user identity).
 */
data class Persona(
    val id: String,
    val fingerprint: String,
    val publicKeyX25519: ByteArray,
    val publicKeyKyber: ByteArray,
    val createdAt: Long
) {
    /** Display-friendly fingerprint: "A7 3F B2 1C 9D E4 51 F8" */
    val displayFingerprint: String
        get() = fingerprint.chunked(2).joinToString(" ").uppercase()

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is Persona) return false
        return id == other.id
    }

    override fun hashCode(): Int = id.hashCode()
}

/**
 * Routing mode for a conversation.
 */
enum class RoutingMode {
    ONION, DHT, MESH, DCNET
}

/**
 * Domain model for a conversation.
 */
data class Conversation(
    val id: String,
    val contactName: String,
    val contactFingerprint: String,
    val lastMessage: String,
    val lastMessageTimestamp: Long,
    val unreadCount: Int = 0,
    val isOnline: Boolean = false,
    val routingMode: RoutingMode = RoutingMode.DHT,
    val sharedSecret: ByteArray? = null // Phase 2: Signal shared secret
)

/**
 * Delivery status of a message.
 */
enum class MessageStatus {
    SENDING, SENT, DELIVERED, READ, FAILED
}

/**
 * Domain model for a message.
 */
data class Message(
    val id: String,
    val senderId: String,
    val content: String,
    val timestamp: Long,
    val isMe: Boolean,
    val status: MessageStatus = MessageStatus.SENT,
    val expiresAt: Long? = null
)

/**
 * Domain model for an anonymous Room (DC-Net or MLS).
 */
data class Room(
    val id: String,
    val name: String,
    val type: String,
    val lastMessage: String,
    val lastMessageTimestamp: Long,
    val unreadCount: Int = 0,
    val isActive: Boolean = true
)
