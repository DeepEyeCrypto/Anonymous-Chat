package com.phantomnet.core.database.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(tableName = "personas")
data class PersonaEntity(
    @PrimaryKey
    val id: String,
    val publicKeyX25519: ByteArray,
    val publicKeyKyber: ByteArray,
    val privateKeyEncrypted: ByteArray,
    val fingerprint: String,
    val createdAt: Long,
    val isActive: Boolean
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is PersonaEntity) return false
        return id == other.id
    }

    override fun hashCode(): Int = id.hashCode()
}

@Entity(tableName = "conversations")
data class ConversationEntity(
    @PrimaryKey
    val id: String,
    val contactName: String,
    val contactFingerprint: String,
    val contactPublicKey: ByteArray,
    val lastMessagePreview: String,
    val lastMessageTimestamp: Long,
    val unreadCount: Int,
    val isOnline: Boolean,
    val routingMode: String  // "ONION" | "DHT" | "MESH" | "DCNET"
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is ConversationEntity) return false
        return id == other.id
    }

    override fun hashCode(): Int = id.hashCode()
}

@Entity(
    tableName = "messages",
    foreignKeys = [ForeignKey(
        entity = ConversationEntity::class,
        parentColumns = ["id"],
        childColumns = ["conversationId"],
        onDelete = ForeignKey.CASCADE
    )],
    indices = [Index("conversationId")]
)
data class MessageEntity(
    @PrimaryKey
    val id: String,
    val conversationId: String,
    val senderId: String,
    val contentPlaintext: String,
    val contentCiphertext: ByteArray?,
    val timestamp: Long,
    val isMe: Boolean,
    val status: String,   // "SENDING" | "SENT" | "DELIVERED" | "READ" | "FAILED"
    val expiresAt: Long?
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is MessageEntity) return false
        return id == other.id
    }

    override fun hashCode(): Int = id.hashCode()
}
